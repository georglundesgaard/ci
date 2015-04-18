package no.lundesgaard.ci;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import no.lundesgaard.ci.data.Data;
import no.lundesgaard.ci.data.Repositories;
import no.lundesgaard.ci.data.Repository;
import no.lundesgaard.ci.data.hazelcast.HazelcastData;
import no.lundesgaard.ci.data.simple.SimpleData;
import no.lundesgaard.ci.event.Event;
import no.lundesgaard.ci.event.RepositoryUpdatedEvent;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.list;
import static java.time.Instant.now;
import static no.lundesgaard.ci.Type.HAZELCAST;
import static no.lundesgaard.ci.Type.SIMPLE;

public class Ci implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ci.class);
    private static final String ROOT = "root";
    private static final String TYPE = "type";
    private static final String HELP = "help";

    private final Path repositoriesPath;
    private final Path commandsPath;
    private final Path workspacesPath;
    private final Path jobsPath;
    private final Path tasksPath;
    private final Type type;
    private String nodeId;
    private Data data;
    private HazelcastInstance hazelcastInstance;
    private JobRunner currentJobRunner;
    private boolean started;

    public static void main(String... args) throws Exception {
        Options options = options();
        CommandLine commandLine = commandLine(options, args);
        if (commandLine == null) {
            return;
        }
        if (commandLine.hasOption(HELP)) {
            printHelp(options);
            return;
        }
        Type type = type(commandLine);
        String root = root(commandLine);
        new Ci(type, root).run();
    }

    private static Options options() {
        Options options = new Options();
        options.addOption(rootOption());
        options.addOption(typeOption());
        options.addOption(helpOption());
        return options;
    }

    private static Option rootOption() {
        Option rootOption = new Option("r", ROOT, true, "Root folder");
        rootOption.setRequired(true);
        return rootOption;
    }

    private static Option typeOption() {
        return new Option("t", TYPE, true, "Type: simple or hazelcast");
    }

    private static Option helpOption() {
        return new Option("h", HELP, false, "Print this message");
    }

    private static CommandLine commandLine(Options options, String[] args) throws ParseException {
        CommandLineParser commandLineParser = new BasicParser();
        try {
            return commandLineParser.parse(options, args);
        } catch (MissingOptionException e) {
            printHelp(options);
            return null;
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("ci", options, true);
    }

    private static Type type(CommandLine commandLine) {
        Type type;
        if (commandLine.hasOption(TYPE)) {
            type = Type.valueOf(commandLine.getOptionValue(TYPE).toUpperCase());
        } else {
            type = SIMPLE;
        }
        LOGGER.debug("type: {}", type);
        return type;
    }

    private static String root(CommandLine commandLine) {
        String root = commandLine.getOptionValue(ROOT);
        LOGGER.debug("root: {}", root);
        return root;
    }

    public Ci(Type type, String root) throws IOException {
        this.type = type;
        Path rootPath = Paths.get(root);
        if (!exists(rootPath)) {
            Files.createDirectories(rootPath);
        }
        if (!isDirectory(rootPath)) {
            throw new IllegalArgumentException("CI root <" + root + "> is not a directory");
        }
        this.repositoriesPath = rootPath.resolve("repositories");
        this.commandsPath = rootPath.resolve("commands");
        this.workspacesPath = rootPath.resolve("workspaces");
        this.jobsPath = rootPath.resolve("jobs");
        this.tasksPath = rootPath.resolve("tasks");
    }

    public String nodeId() {
        return nodeId;
    }

    public Repository addRepository(Repository repository) {
        return data.repositories().repository(repository);
    }

    public Path getRepositoriesPath() {
        return repositoriesPath;
    }

    public Path getWorkspacesPath() {
        return workspacesPath;
    }

    public Path getJobsPath() {
        return jobsPath;
    }

    public Path getTasksPath() {
        return tasksPath;
    }

    @Override
    public void run() {
        if (started) {
            LOGGER.error("CI-server already started!");
            return;
        }
        try {
            initServer();
            Command command;
            while ((command = nextCommand()) != ShutdownCommand.INSTANCE) {
                processCommand(command);
                data.repositories().scan(this);
                createNewJobs();
                handleJobs();
                scanTasks();
                handleTasks();
                sleep(100);
            }
            LOGGER.debug("shutdown (safe) command accepted");
        } catch (IOException | UncheckedIOException e) {
            LOGGER.error("I/O error: {}", e.getMessage(), e);
        } finally {
            try {
                shutdown();
            } catch (IOException e) {
                LOGGER.error("I/O error: {}", e.getMessage(), e);
            }
        }
    }

    private void initServer() {
        LOGGER.debug("CI-server starting...");
        Config config = new Config();
        config.setProperty("hazelcast.logging.type", "slf4j");
        hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        createDirectoriesIfNotExists(repositoriesPath, commandsPath, workspacesPath, jobsPath, tasksPath);
        if (type == HAZELCAST) {
            this.nodeId = hazelcastInstance.getLocalEndpoint().getUuid();
            this.data = new HazelcastData(hazelcastInstance);
        } else {
            this.nodeId = "simple";
            this.data = new SimpleData();
        }
        LOGGER.debug("CI-server started");
        started = true;
    }

    private void createDirectoriesIfNotExists(Path... directoryPaths) {
        for (Path path : directoryPaths) {
            createDirectoryIfNotExitst(path);
        }
    }

    private void createDirectoryIfNotExitst(Path directoryPath) {
        if (!exists(directoryPath)) {
            try {
                Files.createDirectory(directoryPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void processCommand(Command command) {
        if (command == null) {
            return;
        }
        try {
            command.validate();
            command.execute(this);
        } catch (IllegalStateException e) {
            LOGGER.error("Invalid command: {}", command.type(), e);
        }
    }

    private void createNewJobs() throws IOException {
        try (Stream<Path> jobPathStream = list(jobsPath)) {
            jobPathStream
                    .filter(path -> path.toString().endsWith(".sh"))
                    .forEach(path -> {
                        File file = path.toFile();
                        try {
                            String script = readScriptFile(file);
                            String name = file.getName();
                            String taskId = name.substring(0, name.length() - 3);
                            Job job;
                            if (taskMap().containsKey(taskId)) {
                                job = new Job(UUID.randomUUID().toString(), name, script, taskId);
                            } else {
                                job = new Job(UUID.randomUUID().toString(), name, script);
                            }
                            jobList().add(job);
                            LOGGER.debug("{} added", job);
                        } catch (IOException e) {
                            LOGGER.error("Failed to read script file: {}", file, e);
                        } finally {
                            deleteFile(file);
                        }
                    });
        }
    }

    private void handleJobs() {
        if (currentJobRunner != null && currentJobRunner.isRunning()) {
            return;
        }
        Job job = nextJob();
        if (job != null) {
            LOGGER.debug("{} accepted", job);
            currentJobRunner = new JobRunner(this, job);
            new Thread(currentJobRunner).start();
        } else {
            currentJobRunner = null;
        }
    }

    private Job nextJob() {
        Lock jobsLock = hazelcastInstance.getLock("jobsLock");
        jobsLock.lock();
        try {
            List<Job> jobList = jobList();
            if (jobList.isEmpty()) {
                return null;
            }
            LOGGER.debug("jobs: {}", jobList.size());
            return jobList.remove(0);
        } finally {
            jobsLock.unlock();
        }
    }

    private List<Job> jobList() {
        return hazelcastInstance.getList("jobs");
    }

    private void scanTasks() throws IOException {
        try (Stream<Path> taskPathStream = list(tasksPath)) {
            taskPathStream
                    .filter(path -> !path.getFileName().toString().startsWith("temp"))
                    .forEach(this::scanTask);
        }
    }

    private void scanTask(Path taskPath) {
        Map<String, Task> taskMap = taskMap();
        String taskId = taskPath.getFileName().toString();
        if (!taskMap.containsKey(taskId)) {
            try {
                Task task = Task.from(taskPath);
                taskMap.put(taskId, task);
                LOGGER.debug("Task <{}> added: {}", taskId, task);
            } catch (IOException e) {
                LOGGER.error("Failed to scan task: {}", taskPath, e);
            }
        };
    }

    private void handleTasks() {
        Map<String, Task> taskMap = taskMap();
        for (String taskId : taskMap.keySet()) {
            Task task = taskMap.get(taskId);
            if (task.isReady() &&  task.isTriggerExpired()) {
                LOGGER.debug("Task <{}> is ready and trigger has expired: {}", taskId, task);
                tryExecuteTask(taskMap, taskId, task);
                return;
            }
        }
    }

    private void tryExecuteTask(Map<String, Task> taskMap, String taskId, Task task) {
        Lock taskLock = hazelcastInstance.getLock(taskId);
        if (!taskLock.tryLock()) {
            return;
        }
        try {
            task.execute(this, taskId);
            taskMap.put(taskId, task);
            LOGGER.debug("Task <{}> executed: {}", taskId, task);
        } finally {
            taskLock.unlock();
        }
    }

    private void deleteFile(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            LOGGER.warn("Failed to delete file: {}", file, e);
        }
    }

    private String readScriptFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        String line;
        while ((line = reader.readLine()) != null) {
            writer.println(line);
        }
        reader.close();
        return stringWriter.toString();
    }

    private Command nextCommand() throws IOException {
        try {
            return Command.nextFrom(commandsPath);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to resolve next command: {}", e.getMessage(), e);
            return null;
        }
    }

    private void shutdown() throws IOException {
        LOGGER.debug("CI-server shutting down...");
        if (currentJobRunner != null) {
            LOGGER.debug("waiting for current job to finish");
            do {
                sleep(100);
            } while (currentJobRunner.isRunning() && (nextCommand()) != ShutdownCommand.INSTANCE);
            if (currentJobRunner.isRunning()) {
                LOGGER.debug("shutdown (forced) command accepted");
                currentJobRunner.stop();
            }
        }
        hazelcastInstance.shutdown();
        LOGGER.debug("CI-server shutdown completed!");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn("Sleep interrupted", e);
        }
    }

    public void stopTask(String taskId, Path taskPropertiesPath) throws IOException {
        Map<String, Task> taskMap = taskMap();
        Task task = taskMap.get(taskId);
        if (exists(taskPropertiesPath)) {
            Properties taskProperties = new Properties();
            try (FileReader propertiesFile = new FileReader(taskPropertiesPath.toFile())) {
                taskProperties.load(propertiesFile);
            }
            task.stop(taskProperties);
        } else {
            task.stop();
        }
        taskMap.put(taskId, task);
        LOGGER.debug("Task <{}> stopped: {}", taskId, task);
    }

    private Map<String, Task> taskMap() {
        return hazelcastInstance.getMap("tasks");
    }

    public Path createRepositoryDirectoryIfNotExists(Repository repository) {
        Path repositoryDirectory = repositoriesPath.resolve(repository.name);
        createDirectoryIfNotExitst(repositoryDirectory);
        return repositoryDirectory;
    }

    public void publishEvent(Event event) {
        LOGGER.debug("{} published", event);
    }

    public Repositories repositories() {
        return data.repositories();
    }
}
