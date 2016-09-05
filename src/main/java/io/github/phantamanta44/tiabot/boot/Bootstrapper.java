package io.github.phantamanta44.tiabot.boot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

import com.sun.xml.internal.bind.api.impl.NameConverter;
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
        LaunchConfiguration lc = new LaunchConfiguration(
                config.get("jarFile"), config.get("javaHome"), config.get("vmArgs"), config.get("updateUrl"));
		if (config.getBoolean("updateOnStart"))
			updateCheck(lc, true);
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
            logger.info("Bot terminated. Check configuration.");
			fail(1);
			break;
		case ERRORED:
		case REBOOT_REQUESTED:
            logger.info("Bot terminated. Restarting.");
			taskPool.submit(() -> boot(lc));
			break;
		case EXITED:
		case TERMINATED:
			logger.info("Bot terminated. Exiting.");
			Runtime.getRuntime().exit(0);
			break;
		case UPDATE_REQUESTED:
		    logger.info("Bot terminated. Update requested.");
			taskPool.submit(() -> {
				updateCheck(lc, true);
				boot(lc);
			});
			break;
		}
	}

	private static boolean updateCheck(LaunchConfiguration lc, boolean doUpdate) {
		try {
		    Path temp = Files.createTempFile("tiaboot_temp_", ".jar");
            Files.copy(URI.create(lc.getUpdateUrl()).toURL().openStream(), temp);
            CRC32 sum1 = new CRC32(), sum2 = new CRC32();
            sum1.update(Files.readAllBytes(temp));
            sum2.update(Files.readAllBytes(lc.getJarFile().toPath()));
            if (sum1.getValue() == sum2.getValue()) {
                logger.info("No update found.");
                return false;
            }
            logger.info("Update found. Replacing old version...");
            Files.move(temp, lc.getJarFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Update complete.");
            return true;
        } catch (IOException e) {
		    logger.warn("Update failed!");
		    e.printStackTrace();
            return false;
        }
	}
	
	private static void fail(int code) {
		logger.severe("Boot failed; terminating!");
		Runtime.getRuntime().exit(code);
	}
	
}
