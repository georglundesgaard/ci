package no.lundesgaard.ci;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

public class Ci {
	private final static Logger LOGGER = LoggerFactory.getLogger(Ci.class);

	private final String rootDir;

	public Ci(String rootDir) {
		this.rootDir = rootDir;
	}

	public void run() throws Exception {
		LOGGER.debug("started...");
		Config config = new Config();
		config.setProperty("hazelcast.logging.type", "slf4j");
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
		Job currentJob = null;
		try {
			File root = new File(rootDir);
			File jobs = new File(root, "jobs");
			File commands = new File(root, "commands");
			File workspaces = new File(root, "workspaces");
			mainLoop:
			while (true) {
				if (jobs.exists()) {
					File[] files = jobs.listFiles((dir, name) -> {
						return name.endsWith(".sh");
					});
					for (File file : files) {
						String name = file.getName();
						BufferedReader reader = new BufferedReader(new FileReader(file));
						StringWriter stringWriter = new StringWriter();
						PrintWriter writer = new PrintWriter(stringWriter);
						String line;
						while ((line = reader.readLine()) != null) {
							writer.println(line);
						}
						reader.close();
						Job job = new Job(UUID.randomUUID().toString(), name, stringWriter.toString());
						List<Job> jobList = hazelcastInstance.getList("jobs");
						jobList.add(job);
						file.delete();
						LOGGER.debug("job with id <{}> added: {}", job.getId(), job.getName());
					}
				}
				if (commands.exists()) {
					File[] commandFiles = commands.listFiles();
					for (File commandFile : commandFiles) {
						String command = commandFile.getName();
						commandFile.delete();
						if ("shutdown".equalsIgnoreCase(command)) {
							LOGGER.debug("shutdown command accepted");
							break mainLoop;
						}
					}
				}
				if (currentJob != null && !currentJob.isRunning()) {
					currentJob = null;
				}
				Lock jobsLock = hazelcastInstance.getLock("jobsLock");
				jobsLock.lock();
				try {
					List<Job> jobList = hazelcastInstance.getList("jobs");
					if (!jobList.isEmpty() && currentJob == null) {
						Job job = currentJob = jobList.remove(0);
						LOGGER.debug("job with id <{}> and name <{}> accepted", job.getId(), job.getName());
						if (!workspaces.exists()) {
							workspaces.mkdir();
						}
						job.run(workspaces);
					}
				} finally {
					jobsLock.unlock();
				}
				Thread.sleep(100);
			}
		} finally {
			LOGGER.debug("shutting down...");
			if (currentJob != null) {
				try {
					currentJob.stop();
				} catch (Exception e) {
					LOGGER.warn("Failed to stop job with id <{}> and name <{}>", currentJob.getId(), currentJob.getName(), e);
				}
			}
			hazelcastInstance.shutdown();
			LOGGER.debug("shutdown completed!");
		}
	}

	public static void main(String[] args) throws Exception {
		String root = args[0];
		LOGGER.debug("root: {}", root);
		new Ci(root).run();
	}
}
