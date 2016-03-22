package io.github.phantamanta44.tiabot.boot.launch;

import java.io.File;
import java.io.FileNotFoundException;

public class CommandBuilder {

	private static final String COMMAND_FORMAT = "%s -jar %s %s";
	private final LaunchConfiguration config;
	
	public CommandBuilder(LaunchConfiguration config) {
		this.config = config;
	}
	
	public String buildCommand() throws FileNotFoundException {
		if (!config.getJavaHome().exists())
			throw new FileNotFoundException(String.format("JVM not found on path %s!", config.getJavaHome().getAbsolutePath()));
		File jvmExec = new File(config.getJavaHome(), "bin" + File.separatorChar + "java" + osDependentExt());
		if (!jvmExec.exists())
			throw new FileNotFoundException("Java executable not found in Java home!");
		if (!config.getJarFile().exists())
			throw new FileNotFoundException(String.format("Bot jarfile not found on path %s!", config.getJarFile().getAbsolutePath()));
		return String.format(COMMAND_FORMAT, jvmExec.getAbsolutePath(), config.getJarFile().getAbsolutePath(), config.getVmArgs());
	}

	private String osDependentExt() {
		return System.getProperty("os.name").startsWith("Windows") ? ".exe" : "";
	}
	
}
