package io.github.phantamanta44.tiabot.boot;

import java.io.FileNotFoundException;
import java.io.IOException;

import io.github.phantamanta44.tiabot.boot.util.IniConfig;
import io.github.phantamanta44.tiabot.boot.util.LogWrapper;

public class Bootstrapper {
	
	public static final LogWrapper logger = new LogWrapper("TiaBoot");
	
	private static IniConfig config;
	
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
	}
	
	private static void fail(int code) {
		logger.severe("Boot failed; terminating!");
		Runtime.getRuntime().exit(code);
	}
	
}
