package no.lundesgaard.ci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static java.lang.ProcessBuilder.Redirect.appendTo;
import static java.nio.file.Files.createDirectory;

public class JobRunner implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

    private final Ci ci;
    private final Job job;
    private Process process;

    public JobRunner(Ci ci, Job job) {
        this.ci = ci;
        this.job = job;
    }

    @Override
    public void run() {
        Path workspacesPath = ci.getWorkspacesPath();
        Path workspacePath = workspacesPath.resolve(job.getId());;
        try {
            createDirectory(workspacePath);
            writeScriptFile(workspacePath);
            File outputLog = workspacePath.resolve("output.log").toFile();
            process = new ProcessBuilder("sh", job.getName())
                    .directory(workspacePath.toFile())
                    .redirectOutput(appendTo(outputLog))
                    .redirectError(appendTo(outputLog))
                    .start();
            LOGGER.debug("{} started...", job);
        } catch (IOException e) {
            LOGGER.error("Failed to start job process", e);
        }
        try {
            int exitCode = process.waitFor();
            String taskId = job.getTaskId();
            if (taskId != null) {
                Path taskPropertiesPath = workspacePath.resolve(taskId);
                ci.stopTask(taskId, taskPropertiesPath);
            }
            LOGGER.debug("{} finished. Exit code: {}", this, exitCode);
        } catch (InterruptedException e) {
            LOGGER.warn("Waiting for process <{}> was interrupted.", process, e);
        } catch (IOException e) {
            LOGGER.error("I/O error", e);
        }
    }

    private void writeScriptFile(Path workspacePath) throws IOException {
        Path scriptPath = workspacePath.resolve(job.getName());
        try (FileWriter fileWriter = new FileWriter(scriptPath.toFile())) {
            fileWriter.write(job.getScript());
        }
    }

    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    public void stop() {
        if (process == null) {
            return;
        }
        process.destroy();
        new Thread(() -> {
            try {
                boolean exited = process.waitFor(1, TimeUnit.MINUTES);
                int exitCode;
                if (!exited) {
                    LOGGER.debug("Timed out waiting for process to stop: {}", process);
                    Process process = this.process.destroyForcibly();
                    exitCode = process.waitFor();
                } else {
                    exitCode = process.exitValue();
                }
                LOGGER.debug("{} stopped. Exit code: {}", this, exitCode);
            } catch (InterruptedException e) {
                LOGGER.warn("Waiting for process <{}> to stop was interrupted.", process, e);
            }
        }).start();
    }
}
