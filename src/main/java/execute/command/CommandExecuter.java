package execute.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandExecuter {
	protected static final Logger logger = LogManager.getLogger(CommandExecuter.class.getName());
	
	public static boolean canExecuteCommand(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.destroy();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static boolean executeCommand(String command) {
		try {
			Process process = Runtime.getRuntime().exec(command);

			// Consume the output and error streams to prevent blocking
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
			outputGobbler.start();
			errorGobbler.start();

			int exitCode = process.waitFor();
			outputGobbler.join();
			errorGobbler.join();

			if (exitCode != 0) {
				logger.error("{}%n-> Failed with exit code: {}%n", command, exitCode);
			} else {
				return true;
			}
		} catch (IOException | InterruptedException e) {
			logger.error(e);
		}
		return false;
	}

	private static class StreamGobbler extends Thread {
		private final BufferedReader reader;

		public StreamGobbler(InputStream inputStream) {
			this.reader = new BufferedReader(new InputStreamReader(inputStream));
		}

		@Override
		public void run() {
			try {
				while (reader.readLine() != null) {
					// consume
				}
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}

}
