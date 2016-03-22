package io.github.phantamanta44.tiabot.boot.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import io.github.phantamanta44.tiabot.boot.Bootstrapper;

public class IniConfig {

	private final File configFile;
	private final Map<String, String> configKeys = new HashMap<>();
	
	public IniConfig(String filename) {
		this(new File(filename));
	}
	
	public IniConfig(File file) {
		configFile = file;
	}
	
	public void read() throws FileNotFoundException, IOException {
		configKeys.clear();
		try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("#"))
					continue;
				String[] parts = line.split("=", 2);
				if (parts.length < 2) {
					Bootstrapper.logger.warn("Invalid config line:\n\t%s", line);
					continue;
				}
				configKeys.put(parts[0], parts[1]);
			}
		}
	}
	
	public String getPath() {
		return configFile.getPath();
	}
	
	public String get(String key) {
		return configKeys.get(key);
	}
	
	public boolean getBoolean(String key) {
		String val = get(key);
		if (val == null)
			return false;
		return val.equalsIgnoreCase("true");
	}
	
	public int getInt(String key) {
		String val = get(key);
		if (val == null)
			return 0;
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
	
	public float getFloat(String key) {
		String val = get(key);
		if (val == null)
			return 0F;
		try {
			return Float.parseFloat(val);
		} catch (NumberFormatException ex) {
			return 0F;
		}
	}
	
	public Set<Entry<String, String>> entrySet() {
		return configKeys.entrySet();
	}
	
	public void forEach(BiConsumer<String, String> func) {
		configKeys.forEach(func);
	}
	
	public Stream<Entry<String, String>> stream() {
		return configKeys.entrySet().stream();
	}
	
}
