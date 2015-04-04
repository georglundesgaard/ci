package no.lundesgaard.ci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.lang.ProcessBuilder.Redirect.appendTo;
import static java.nio.file.Files.createDirectory;

public class JobRunner implements Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);

    private final File workspace;
    private final Job job;
    private Process process;

    public JobRunner(File workspaces, Job job) {
        this.job = job;
        this.workspace = new File(workspaces, job.getId());
    }

    @Override
    public void run() {
        try {
            createDirectory(workspace.toPath());
            writeScriptFile();
            File outputLog = new File(workspace, "output.log");
            process = new ProcessBuilder("sh", job.getName())
                    .directory(workspace)
                    .redirectOutput(appendTo(outputLog))
                    .redirectError(appendTo(outputLog))
                    .start();
            LOGGER.debug("{} started...", job);
        } catch (IOException e) {
            LOGGER.error("Failed to start job process", e);
        }
        try {
            int exitCode = process.waitFor();
            LOGGER.debug("{} finished. Exit code: {}", this, exitCode);
        } catch (InterruptedException e) {
            LOGGER.warn("Waiting for process <{}> was interrupted.", process, e);
        }
    }

    private void writeScriptFile() throws IOException {
        File scriptFile = new File(workspace, job.getName());
        try (FileWriter fileWriter = new FileWriter(scriptFile)) {
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
                LOGGER.warn("Waiting for process <{}> was interrupted.", process, e);
            }
        }).start();
    }
}
