package io.github.phantamanta44.tiabot.boot.launch;

import java.io.File;

public class LaunchConfiguration {

	private static final File DEF_JAR_FILE = new File("tiabot.jar");
	private static final File DEF_JAVA_HOME = new File(System.getenv("JAVA_HOME"));
	private static final String DEF_VM_ARGS = "";
	
	private File jarFile, javaHome;
	private String vmArgs, updateUrl;
	
	public LaunchConfiguration(String jarFile, String javaHome, String vmArgs, String updateUrl) {
		this.jarFile = jarFile != null ? new File(jarFile) : DEF_JAR_FILE;
		this.javaHome = javaHome != null ? new File(javaHome) : DEF_JAVA_HOME;
		this.vmArgs = vmArgs != null ? vmArgs : DEF_VM_ARGS;
		this.updateUrl = updateUrl;
	}
	
	public File getJarFile() {
		return jarFile;
	}
	
	public File getJavaHome() {
		return javaHome;
	}
	
	public String getVmArgs() {
		return vmArgs;
	}

	public String getUpdateUrl() {
		return updateUrl;
	}
	
}
