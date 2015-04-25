package no.lundesgaard.ci;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import no.lundesgaard.ci.model.event.EventQueue;
import no.lundesgaard.ci.model.Type;
import no.lundesgaard.ci.model.data.Data;
import no.lundesgaard.ci.model.data.hazelcast.HazelcastData;
import no.lundesgaard.ci.model.data.simple.SimpleData;
import no.lundesgaard.ci.model.job.JobQueue;
import no.lundesgaard.ci.model.job.Jobs;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.TaskId;
import no.lundesgaard.ci.model.task.Tasks;
import no.lundesgaard.ci.processor.CommandProcessor;
import no.lundesgaard.ci.processor.EventProcessor;
import no.lundesgaard.ci.processor.JobProcessor;
import no.lundesgaard.ci.processor.RepositoryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static no.lundesgaard.ci.model.Type.HAZELCAST;
import static org.apache.commons.lang3.StringUtils.uncapitalize;

public class Ci {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ci.class);

    public final Path rootPath;
    public final Path repositoriesPath;
    public final Path commandsPath;
    public final Path workspacesPath;
    private final Type type;
    private String nodeId;
    private Data data;
    public final EventQueue eventQueue = new EventQueue();
    private CommandProcessor commandProcessor;
    private JobProcessor jobProcessor;
    private RepositoryProcessor repositoryProcessor;
    private EventProcessor eventProcessor;

    public static void main(String[] args) {
        CiOptions ciOptions = new CiOptions(args);
        if (ciOptions.isValid()) {
            new Ci(ciOptions).start();
        } else {
            ciOptions.printHelp();
        }
    }

    public Ci(CiOptions ciOptions) {
        this(ciOptions.type, ciOptions.root);
    }

    private Ci(Ci oldCi) {
        this(oldCi.type, oldCi.rootPath.toString());
    }

    public Ci(Type type, String root) {
        LOGGER.debug("type: {}", type);
        LOGGER.debug("root: {}", root);
        this.type = type;
        this.rootPath = Paths.get(root);
        if (!exists(rootPath)) {
            try {
                Files.createDirectories(rootPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
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

    public Repositories repositories() {
        return data.repositories();
    }

    public Tasks tasks() {
        return data.tasks();
    }

    public Jobs jobs() {
        return data.jobs();
    }

    public JobQueue jobQueue() {
        return data.jobQueue();
    }

    public void start() {
        if (isRunning()) {
            LOGGER.error("CI server is already running");
            return;
        }
        LOGGER.debug("CI server starting...");
        createDirectoriesIfNotExists();
        initData();
        startCommandProcessor();
        startJobProcessor();
        startRepositoryProcessor();
        startEventProcessor();
        do {
            sleep();
        } while (processorsIsNotStarted());
        LOGGER.debug("CI server started");
    }

    private boolean isRunning() {
        return nodeId != null;
    }

    private void createDirectoriesIfNotExists() {
        createDirectoryIfNotExists(repositoriesPath);
        createDirectoryIfNotExists(commandsPath);
        createDirectoryIfNotExists(workspacesPath);
    }

    public void createDirectoryIfNotExists(Path directoryPath) {
        if (!exists(directoryPath)) {
            try {
                Files.createDirectory(directoryPath);
                LOGGER.debug("Directory created: {}", directoryPath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        if (!isDirectory(directoryPath)) {
            throw new IllegalArgumentException(directoryPath + " is not a directory");
        }
    }

    private void initData() {
        if (type == HAZELCAST) {
            Config config = new Config();
            config.setProperty("hazelcast.logging.type", "slf4j");
            HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
            this.data = new HazelcastData(hazelcastInstance);
            LOGGER.debug("Hazelcast data store initialized");
        } else {
            this.data = new SimpleData();
            LOGGER.debug("Simple data store initialized");
        }
        this.nodeId = data.nodeId();
        LOGGER.debug("Node id: {}", nodeId);
    }

    private void startCommandProcessor() {
        this.commandProcessor = new CommandProcessor(this);
        startNewThread(commandProcessor);
    }

    private void startJobProcessor() {
        this.jobProcessor = new JobProcessor(this);
        startNewThread(jobProcessor);
    }

    private void startRepositoryProcessor() {
        this.repositoryProcessor = new RepositoryProcessor(this);
        startNewThread(repositoryProcessor);
    }

    private void startEventProcessor() {
        this.eventProcessor = new EventProcessor(this);
        startNewThread(eventProcessor);
    }

    private boolean processorsIsNotStarted() {
        return !commandProcessor.isStarted()
                && !jobProcessor.isStarted()
                && !repositoryProcessor.isStarted()
                && !eventProcessor.isStarted();
    }

    private void startNewThread(Runnable target) {
        String name = uncapitalize(target.getClass().getSimpleName());
        startNewThread(target, name);
    }

    private void startNewThread(Runnable target, String name) {
        new Thread(target, name).start();
    }

    public void shutdown() {
        shutdown(false);
    }

    public void restart() {
        shutdown(true);
    }

    private void shutdown(boolean restart) {
        startNewThread(() -> {
            LOGGER.debug("CI server shutting down...");
            try {
                stopProcessors();
                do {
                    sleep();
                } while (processorsIsNotStopped());
            } finally {
                try {
                    this.data.shutdown();
                } finally {
                    this.nodeId = null;
                }
            }
            LOGGER.debug("CI server shutdown completed!");
            if (restart) {
                new Ci(this).start();
            }
        }, restart ? "restart" : "shutdown");
    }

    private void stopProcessors() {
        commandProcessor.stop();
        jobProcessor.stop();
        repositoryProcessor.stop();
        eventProcessor.stop();
    }

    private void sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.warn("Sleep interrupted", e);
        }
    }

    private boolean processorsIsNotStopped() {
        return !commandProcessor.isStopped()
                || !jobProcessor.isStopped()
                || !repositoryProcessor.isStopped()
                || !eventProcessor.isStopped();
    }

    public int nextJobNumberFor(TaskId taskId) {
        return jobs()
                .stream()
                .filter(job -> job.taskId.equals(taskId))
                .mapToInt(job -> job.jobNumber)
                .max().orElse(0) + 1;

    }
}
