package es.hefame.keyvault.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.http.server.HttpService;

public class ShutdownHook extends Thread {
	private static Logger logger = LogManager.getLogger();
	HttpService server = null;

	public ShutdownHook(HttpService server) {
		this.server = server;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("SHT-ShutdownHook");
		logger.info("Deteniendo servidor HTTP");
		if (server != null)
			server.stop();
	}

}
