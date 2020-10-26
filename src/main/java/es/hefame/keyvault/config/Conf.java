package es.hefame.keyvault.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Conf {

	private Conf() {

	}

	private static Logger logger = LogManager.getLogger();
	private static final Properties CONFIG;

	static {
		File configFile = new File(System.getProperty("config", "./config.properties"));
		CONFIG = new Properties();

		try {
			try (FileInputStream fis = new FileInputStream(configFile)) {
				CONFIG.load(fis);
			}
		} catch (IOException e) {
			logger.warn("No se pudo leer el fichero de configuración {}", configFile.getAbsolutePath());
		}

	}

	public static String get(String key, String def) {
		if (Conf.CONFIG == null)
			return def;
		return Conf.CONFIG.getProperty(key, def);
	}

	public static Integer get(String key, Integer def) {
		String value = Conf.CONFIG.getProperty(key);
		if (value == null)
			return def;

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public static String getString(String key) {
		return Conf.get(key, (String) null);
	}

	public static Integer getInteger(String key) {
		return Conf.get(key, (Integer) null);
	}

}
