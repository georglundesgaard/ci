package no.lundesgaard.ci;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

public class Ci implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Ci.class);

    private final File commandsDir;
    private final File workspacesDir;
    private final File jobsDir;
    private HazelcastInstance hazelcastInstance;
    private JobRunner currentJobRunner;
    private boolean started;

    public Ci(String root) {
        File rootDir = new File(root);
        this.commandsDir = new File(rootDir, "commands");
        this.workspacesDir = new File(rootDir, "workspaces");
        this.jobsDir = new File(rootDir, "jobs");
    }

    @Override
    public void run() {
        if (started) {
            LOGGER.error("CI-server already started!");
        }
        init();
        try {
            while (true) {
                processNewJobs();
                Command command = nextCommand();
                if (isShutdown(command)) {
                    LOGGER.debug("shutdown (safe) command accepted");
                    break;
                }
                handleJobs();
                sleep(100);
            }
        } finally {
            shutdown();
        }
    }

    private void handleJobs() {
        if (currentJobRunner != null && currentJobRunner.isRunning()) {
            return;
        }
        Job job = nextJob();
        if (job != null) {
            LOGGER.debug("{} accepted", job);
            currentJobRunner = new JobRunner(workspacesDir, job);
            new Thread(currentJobRunner).start();
        } else {
            currentJobRunner = null;
        }
    }

    private Job nextJob() {
        Lock jobsLock = hazelcastInstance.getLock("jobsLock");
        jobsLock.lock();
        try {
            List<Job> jobList = hazelcastInstance.getList("jobs");
            if (jobList.isEmpty()) {
                return null;
            }
            LOGGER.debug("jobs: {}", jobList.size());
            return jobList.remove(0);
        } finally {
            jobsLock.unlock();
        }
    }

    private void processNewJobs() {
        if (jobsDir.exists()) {
            File[] files = jobsDir.listFiles((dir, name) -> name.endsWith(".sh"));
            for (File file : files) {
                String name = file.getName();
                try {
                    String script = readScriptFile(file);
                    Job job = new Job(UUID.randomUUID().toString(), name, script);
                    List<Job> jobList = hazelcastInstance.getList("jobs");
                    jobList.add(job);
                    LOGGER.debug("{} added", job);
                } catch (IOException e) {
                    LOGGER.error("Failed to read script file: {}", file, e);
                } finally {
                    deleteFile(file);
                }
            }
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

    private void init() {
        LOGGER.debug("CI-server starting...");
        Config config = new Config();
        config.setProperty("hazelcast.logging.type", "slf4j");
        hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        if (!workspacesDir.exists()) {
            try {
                Files.createDirectory(workspacesDir.toPath());
            } catch (IOException e) {
                LOGGER.error("Failed to create workspaces directory", e);
            }
        }
        LOGGER.debug("CI-server started");
        started = true;
    }

    private Command nextCommand() {
        return Command.nextFrom(commandsDir);
    }

    private void shutdown() {
        LOGGER.debug("CI-server shutting down...");
        if (currentJobRunner != null) {
            LOGGER.debug("waiting for current job to finish");
            while (currentJobRunner.isRunning()) {
                Command command = nextCommand();
                if (isShutdown(command)) {
                    LOGGER.debug("shutdown (forced) command accepted");
                    currentJobRunner.stop();
                }
                sleep(100);
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

    private boolean isShutdown(Command command) {
        return command == ShutdownCommand.INSTANCE;
    }

    public static void main(String[] args) throws Exception {
        String root = args[0];
        LOGGER.debug("root dir: {}", root);
        new Ci(root).run();
    }
}
