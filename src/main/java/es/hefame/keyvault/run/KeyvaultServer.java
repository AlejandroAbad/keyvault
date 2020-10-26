package es.hefame.keyvault.run;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.authentication.rfc7235.rfc7617.BasicAuthenticator;
import es.hefame.hcore.http.server.HttpService;
import es.hefame.keyvault.controller.rest.AuthCheckRestHandler;
import es.hefame.keyvault.controller.rest.DomainRestHandler;
import es.hefame.keyvault.controller.rest.KeypairRestHandler;
import es.hefame.keyvault.controller.rest.PersonRestHandler;
import es.hefame.keyvault.controller.rest.SignRestHandler;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.util.auth.DomainPasswordMatcher;


public class KeyvaultServer
{
	private static Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws Exception
	{

		logger.info("Arrancando servicio KeyVault");
		DAO.set_provider("MariaDb");

		try
		{
			// C.load();

			int port = 8443;// C.agent.port;
			int maxConnections = 10;

			Map<String, HttpController> routes = new HashMap<>();
			// routes.put("/test", new jhefame.hfe.controller.QuickTestHandler());
			routes.put("/rest/domain", new DomainRestHandler());
			routes.put("/rest/person", new PersonRestHandler());
			routes.put("/rest/keypair", new KeypairRestHandler());
			routes.put("/rest/sign", new SignRestHandler());
			routes.put("/rest/authcheck", new AuthCheckRestHandler());

			HttpService server = new HttpService(port, maxConnections, routes);
			HttpController.setDefaultAuthenticator(new BasicAuthenticator("HefameKeyVault", new DomainPasswordMatcher()));

			ShutdownHook shutdownHook = new ShutdownHook(server);
			// L.trace("Setting up ShutdownHook [{}]", shutdown_hook.getClass().getName());
			Runtime.getRuntime().addShutdownHook(shutdownHook);

			server.start();
		}
		catch (Exception e)
		{
			logger.fatal("Aborting execution with exit code {}", 2);
			System.exit(2);
		}

	}

}
