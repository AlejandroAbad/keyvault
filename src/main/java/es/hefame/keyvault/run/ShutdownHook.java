package es.hefame.keyvault.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.http.server.HttpService;


public class ShutdownHook extends Thread
{
	private static Logger	log		= LogManager.getLogger();
	HttpService				server	= null;

	public ShutdownHook(HttpService server)
	{
		this.server = server;
	}

	@Override
	public void run()
	{
		Thread.currentThread().setName("SHT-ShutdownHook");
		log.traceEntry();
		log.info("Received Shutdown signal");

		log.info("Stopping HTTP engine");
		if (server != null) server.stop();

		log.info("HTTP(s) server stopped");
		log.info("\n---- API stopped ----\n");
		log.traceExit();
	}

}
