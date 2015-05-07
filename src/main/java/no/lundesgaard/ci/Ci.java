package no.lundesgaard.ci;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import no.lundesgaard.ci.model.Type;
import no.lundesgaard.ci.model.data.Data;
import no.lundesgaard.ci.model.data.hazelcast.HazelcastData;
import no.lundesgaard.ci.model.data.simple.SimpleData;
import no.lundesgaard.ci.model.event.EventQueue;
import no.lundesgaard.ci.model.job.JobQueue;
import no.lundesgaard.ci.model.job.Jobs;
import no.lundesgaard.ci.model.repository.Repositories;
import no.lundesgaard.ci.model.task.Tasks;
import no.lundesgaard.ci.processor.CommandProcessor;
import no.lundesgaard.ci.processor.EventProcessor;
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
	private final JobRunner[] jobRunners;
	private String nodeId;
	private Data data;
	public final EventQueue eventQueue = new EventQueue();
	private final CommandProcessor commandProcessor;
	private RepositoryProcessor repositoryProcessor;
	private final EventProcessor eventProcessor;
	private State state;

	public static void main(String[] args) {
		CiOptions ciOptions = new CiOptions(args);
		if (ciOptions.isValid()) {
			new Ci(ciOptions).start();
		} else {
			ciOptions.printHelp();
		}
	}

	public Ci(CiOptions ciOptions) {
		this(ciOptions.type, ciOptions.root, ciOptions.jobRunners);
	}

	private Ci(Ci oldCi) {
		this(oldCi.type, oldCi.rootPath.toString(), oldCi.jobRunners.length);
	}

	public Ci(Type type, String root, int jobRunnerCount) {
		LOGGER.debug("type: {}", type);
		LOGGER.debug("root: {}", root);
		LOGGER.debug("jobRunners: {}", jobRunnerCount);
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
		this.jobRunners = new JobRunner[jobRunnerCount];
		this.commandProcessor = new CommandProcessor(this);
		this.eventProcessor = new EventProcessor(this);
		this.state = State.CREATED;
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
		if (isAlreadyStarted()) {
			LOGGER.error("CI server is already started");
			return;
		}
		LOGGER.debug("CI server starting...");
		createDirectoriesIfNotExists();
		initData();
		initJobRunners();
		commandProcessor.startSubscription();
		startRepositoryProcessor();
		eventProcessor.startSubscription();
		do {
			sleep();
		} while (!processorsIsRunning());
		LOGGER.debug("CI server started");
		this.state = State.RUNNING;
		lifecycle();
	}

	private boolean isAlreadyStarted() {
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

	private void initJobRunners() {
		for (int i = 0; i < jobRunners.length; i++) {
			jobRunners[i] = new JobRunner(this);
			jobRunners[i].startSubscription();
		}
	}

	private void startRepositoryProcessor() {
		this.repositoryProcessor = new RepositoryProcessor(this);
		startNewThread(repositoryProcessor);
	}

	private boolean processorsIsRunning() {
		return repositoryProcessor.isRunning();
	}

	private void lifecycle() {
		while (state == State.RUNNING) {
			sleep();
		}
		try {
			if (state == State.RESTARTING) {
				shutdown(true);
			} else {
				shutdown(false);
			}
		} finally {
			this.state = State.STOPPED;
		}
	}

	private void shutdown(boolean restart) {
		LOGGER.debug("CI server shutting down...");
		try {
			stopProcessors();
			stopJobRunners();
			do {
				sleep();
			} while (processorsIsNotStopped() && jobRunnersIsNotStopped());
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
	}

	private void startNewThread(Runnable target) {
		String name = uncapitalize(target.getClass().getSimpleName());
		startNewThread(target, name);
	}

	private void startNewThread(Runnable target, String name) {
		new Thread(target, name).start();
	}

	public void shutdown() {
		this.state = State.SHUTDOWN;
	}

	public void restart() {
		this.state = State.RESTARTING;
	}

	private void stopProcessors() {
		commandProcessor.stopSubscription();
		repositoryProcessor.stop();
		eventProcessor.stopSubscription();
	}

	private void stopJobRunners() {
		for (JobRunner jobRunner : jobRunners) {
			jobRunner.stopSubscription();
		}
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			LOGGER.warn("Sleep interrupted", e);
		}
	}

	private boolean processorsIsNotStopped() {
		return !repositoryProcessor.isStopped();
	}

	private boolean jobRunnersIsNotStopped() {
		for (JobRunner jobRunner : jobRunners) {
			if (jobRunner.isRunning()) {
				return false;
			}
		}
		return true;
	}

	private enum State {
		CREATED, RUNNING, RESTARTING, SHUTDOWN, STOPPED
	}
}
