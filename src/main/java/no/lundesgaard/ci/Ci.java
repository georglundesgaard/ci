package no.lundesgaard.ci;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import no.lundesgaard.ci.command.Command;
import no.lundesgaard.ci.command.shutdown.ShutdownCommand;
import no.lundesgaard.ci.event.Event;
import no.lundesgaard.ci.model.Type;
import no.lundesgaard.ci.model.data.Data;
import no.lundesgaard.ci.model.data.hazelcast.HazelcastData;
import no.lundesgaard.ci.model.data.simple.SimpleData;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.repository.Repository;
import no.lundesgaard.ci.model.task.Task;
import no.lundesgaard.ci.model.task.TaskStatus;
import no.lundesgaard.ci.model.task.TaskStatus.State;
import no.lundesgaard.ci.model.task.TaskStatuses;
import no.lundesgaard.ci.model.task.Tasks;
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static no.lundesgaard.ci.model.Type.HAZELCAST;
import static no.lundesgaard.ci.model.Type.SIMPLE;

public class Ci implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ci.class);
    private static final String ROOT = "root";
    private static final String TYPE = "type";
    private static final String HELP = "help";

    private final Path repositoriesPath;
    private final Path commandsPath;
    private final Path workspacesPath;
    private final Type type;
    private String nodeId;
    private Data data;
    private TaskRunner currentTaskRunner;
    private Queue<Event> eventQueue = new LinkedList<>();

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
    }

    public String nodeId() {
        return nodeId;
    }

    public Repository addRepository(Repository repository) {
        return data.repositories().repository(repository);
    }

    public Task addTask(Task task) {
        return data.tasks().task(task);
    }

    public Path getRepositoriesPath() {
        return repositoriesPath;
    }

    public Path getWorkspacesPath() {
        return workspacesPath;
    }

    @Override
    public void run() {
        if (isRunning()) {
            LOGGER.error("CI-server already started!");
            return;
        }
        try {
            initServer();
            Command command;
            while ((command = nextCommand()) != ShutdownCommand.INSTANCE) {
                processCommand(command);
                scanRepositories();
                processEventQueue();
                runNextTask();
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

    private boolean isRunning() {
        return nodeId != null;
    }

    private void initServer() {
        LOGGER.debug("CI-server starting...");
        createDirectoriesIfNotExists(repositoriesPath, commandsPath, workspacesPath);
        if (type == HAZELCAST) {
            Config config = new Config();
            config.setProperty("hazelcast.logging.type", "slf4j");
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            this.data = new HazelcastData(hazelcastInstance);
            this.nodeId = hazelcastInstance.getLocalEndpoint().getUuid();
        } else {
            this.data = new SimpleData();
            this.nodeId = "simple";
        }
        LOGGER.debug("CI-server started");
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

    private void scanRepositories() {
        data.repositories().scan(this);
    }

    private void processEventQueue() {
        while (!eventQueue.isEmpty()) {
            eventQueue.remove().process(this);
        }
    }

    private void runNextTask() {
        if (currentTaskRunner != null && currentTaskRunner.isRunning()) {
            // a task is already running
            return;
        }
        String taskName = data.taskQueue().next();
        if (taskName != null) {
            Task task = data.tasks().task(taskName);
            LOGGER.debug("{} accepted", task);
            currentTaskRunner = new TaskRunner(this, task);
            new Thread(currentTaskRunner, "taskRunner").start();
        } else {
            currentTaskRunner = null;
        }
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
        if (currentTaskRunner != null) {
            LOGGER.debug("waiting for current task to finish");
            do {
                sleep(100);
            } while (currentTaskRunner.isRunning() && (nextCommand()) != ShutdownCommand.INSTANCE);
            if (currentTaskRunner.isRunning()) {
                LOGGER.debug("shutdown (forced) command accepted");
                currentTaskRunner.stop();
            }
        }
        try {
            this.data.shutdown();
        } finally {
            this.nodeId = null;
        }
        LOGGER.debug("CI-server shutdown completed!");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn("Sleep interrupted", e);
        }
    }

    public Path createRepositoryDirectoryIfNotExists(Repository repository) {
        Path repositoryDirectory = repositoriesPath.resolve(repository.name);
        createDirectoryIfNotExitst(repositoryDirectory);
        return repositoryDirectory;
    }

    public void publishEvent(Event event) {
        this.eventQueue.add(event);
        LOGGER.debug("{} published", event);
    }

    public Repositories repositories() {
        return data.repositories();
    }

    public Tasks tasks() {
        return data.tasks();
    }

    public TaskStatuses taskStatuses() {
        return data.taskStatuses();
    }

    public void addTaskToQueue(String name) {
        data.taskQueue().add(name);
    }

    public void addTaskStatus(TaskRunner taskRunner, State state, String message, Exception exception) {
        TaskStatus taskStatus = new TaskStatus(taskRunner.task.name, taskRunner.id, state, message, exception);
        data.taskStatuses().taskStatus(taskStatus);
    }

    public void updateTaskStatus(TaskRunner taskRunner, State state, String message, Exception exception) {
        TaskStatus taskStatus = data.taskStatuses().taskStatus(taskRunner);
        data.taskStatuses().taskStatus(new TaskStatus(taskStatus, state, message, exception));
    }

    public Path workspace(String workspaceName) {
        return workspacesPath.resolve(workspaceName);
    }
}
