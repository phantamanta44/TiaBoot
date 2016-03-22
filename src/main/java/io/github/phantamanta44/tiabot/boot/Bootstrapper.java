package io.github.phantamanta44.tiabot.boot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.phantamanta44.tiabot.boot.launch.CommandBuilder;
import io.github.phantamanta44.tiabot.boot.launch.ExitCode;
import io.github.phantamanta44.tiabot.boot.launch.LaunchConfiguration;
import io.github.phantamanta44.tiabot.boot.util.IniConfig;
import io.github.phantamanta44.tiabot.boot.util.LogWrapper;

public class Bootstrapper {
	
	public static final LogWrapper logger = new LogWrapper("TiaBoot");
	
	private static IniConfig config;
	private static ExecutorService taskPool = Executors.newCachedThreadPool();
	
	public static void main(String[] args) {
		if (args.length < 1)
			config = new IniConfig("tiaboot.conf");
		else
			config = new IniConfig(args[0]);
		try {
			config.read();
		} catch (FileNotFoundException ex) {
			logger.severe("Config file \'%s\' not found!", config.getPath());
			fail(1);
		} catch (IOException ex) {
			logger.severe("Encountered exception while reading config file \'%s\'!", config.getPath());
			ex.printStackTrace();
			fail(1);
		}
		if (config.getBoolean("updateOnStart"))
			updateCheck(true);
		LaunchConfiguration lc = new LaunchConfiguration(
				config.get("jarFile"), config.get("javaHome"), config.get("vmArgs"));
		taskPool.submit(() -> boot(lc));
	}

	private static void boot(LaunchConfiguration lc) {
		try {
			String cmd = new CommandBuilder(lc).build();
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO();
			pb.command(cmd.split("\\s"));
			Process proc = pb.start();
			proc.waitFor();
			handleExit(ExitCode.forCode(proc.exitValue()), lc);
		} catch (FileNotFoundException ex) {
			logger.severe(ex.getMessage());
			fail(1);
		} catch (IOException ex) {
			logger.severe("Error encountered while booting bot!");
			ex.printStackTrace();
			fail(1);
		} catch (InterruptedException ex) {
			logger.severe("Bootloader thread interrupted!");
			ex.printStackTrace();
			fail(1);
		}
	}
	
	private static void handleExit(ExitCode code, LaunchConfiguration lc) {
		switch (code) {
		case CONNECT_FAILURE:
		case LOGIN_FAILURE:
		case UNKNOWN:
			fail(1);
			break;
		case ERRORED:
		case REBOOT_REQUESTED:
			taskPool.submit(() -> boot(lc));
			break;
		case EXITED:
		case TERMINATED:
			logger.info("Bot terminated.");
			Runtime.getRuntime().exit(0);
			break;
		case UPDATE_REQUESTED:
			taskPool.submit(() -> {
				updateCheck(true);
				boot(lc);
			});
			break;
		}
	}

	private static boolean updateCheck(boolean doUpdate) {
		// TODO Update checking
		return false;
	}
	
	private static void fail(int code) {
		logger.severe("Boot failed; terminating!");
		Runtime.getRuntime().exit(code);
	}
	
}
