package ar.edu.itba.pod.legajo47126.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile {
	// name of the config file
	private final String confFileName;
	
	// loaded properties
	private Properties configFile;
	
	public ConfigFile(String FileName) throws IOException {
		if (FileName == null || FileName.length() == 0) {
			throw new IllegalArgumentException("Invalid Config File Path");
		}
		
		this.confFileName = FileName;
		this.configFile = new Properties();
		
		try {
			this.configFile.load(new FileInputStream((FileName)));
		} catch (IOException e) {
			throw new IOException("There was an error opening the configuraton file");
		}
	}
		
	public String getConfFileName() {
		return confFileName;
	}
	
	public String[] getPropertiesList(String key, String delimeter) {
		String[] values = null;
		String val;
		
		val = this.configFile.getProperty(key);
		
		if (val != null && val.length() > 0) {
			values = val.split(delimeter);
		}
		
		return values;
	}
	
	public String getProperty(String key) {
		return this.configFile.getProperty(key);
	}
	
	public String getProperty(String key, String defaultValue) {
		String property;
		
		if((property = this.configFile.getProperty(key)) == null)
			return defaultValue;
		
		return property;
	}
	
	public int getProperty(String key, int defaultValue) {
		String property;
		
		if((property = this.configFile.getProperty(key)) == null)
			return defaultValue;
		
		return Integer.valueOf(property);
	}
	
	public long getProperty(String key, long defaultValue) {
		String property;
		
		if((property = this.configFile.getProperty(key)) == null)
			return defaultValue;
		
		return Long.valueOf(property);
	}
	
	public double getProperty(String key, double defaultValue){
		String property;
		
		if((property = this.configFile.getProperty(key)) == null)
			return defaultValue;
		
		return Double.valueOf(property);
	}

}
