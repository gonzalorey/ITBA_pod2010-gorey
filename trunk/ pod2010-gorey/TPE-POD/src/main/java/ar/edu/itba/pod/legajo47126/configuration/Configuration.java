package ar.edu.itba.pod.legajo47126.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class Configuration {
		private final String confFileName;
		private Properties configFile;
		
		public Configuration(String FileName) throws IOException {
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
		
		public String getProperty(String key) {
			String p;
			
			p = this.configFile.getProperty(key);
			return p;
			
		}
		
		public String[] getPropertyList(String key, String delimeter) {
			String[] values = null;
			String val;
			
			val = this.configFile.getProperty(key);
			
			if (val != null && val.length() > 0) {
				values = val.split(delimeter);
			}

			return values;
		}

		public String getConfFileName() {
			return confFileName;
		}
		
		public void rmUnsupportedCompTypes(List<String> ctypes) {
			String[] mytypes = getPropertyList("Content-Encoding", ",");			
			List<String> compressionTypes = new LinkedList<String>();
			
			if (mytypes != null && mytypes.length > 0) {
				
				for (String t : mytypes) {
					compressionTypes.add(t);
				}
				
				ctypes.retainAll(compressionTypes);
			} else {
				ctypes.clear();
			}
			
			return;
		}
}
