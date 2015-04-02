package no.lundesgaard.ci;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

public class Ci {
	private final String rootDir;

	public Ci(String rootDir) {
		this.rootDir = rootDir;
	}

	public void run() throws Exception {
		Config config = new Config();
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
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
					}
				}
				if (commands.exists()) {
					File[] commandFiles = commands.listFiles();
					for (File commandFile : commandFiles) {
						if ("shutdown".equalsIgnoreCase(commandFile.getName())) {
							commandFile.delete();
							break mainLoop;
						}
					}
				}
				List<Job> jobList = hazelcastInstance.getList("jobs");
				if (!jobList.isEmpty()) {
					Job job = jobList.remove(0);
					if (!workspaces.exists()) {
						workspaces.mkdir();
					}
					job.run(workspaces);
				}
			}
		} finally {
			hazelcastInstance.shutdown();
		}
	}

	public static void main(String[] args) throws Exception {
		new Ci(args[0]).run();
	}
}
