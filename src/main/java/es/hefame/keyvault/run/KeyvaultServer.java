package es.hefame.keyvault.run;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.authentication.rfc7235.rfc7617.BasicAuthenticator;
import es.hefame.hcore.http.server.HttpService;
import es.hefame.keyvault.config.Conf;
import es.hefame.keyvault.controller.rest.AuthCheckRestHandler;
import es.hefame.keyvault.controller.rest.DomainRestHandler;
import es.hefame.keyvault.controller.rest.KeypairRestHandler;
import es.hefame.keyvault.controller.rest.PersonRestHandler;
import es.hefame.keyvault.controller.rest.SignRestHandler;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.util.auth.DomainPasswordMatcher;

public class KeyvaultServer {
	private static Logger logger = LogManager.getLogger();

	public static void main(String[] args) throws Exception {

		DAO.setProvider(Conf.get("dao.provider", "Testing"));

		try {
			int port = Conf.get("http.port", 8080);
			int maxConnections = Conf.get("http.maxConnections", 10);

			Map<String, HttpController> routes = new HashMap<>();
			// routes.put("/test", new jhefame.hfe.controller.QuickTestHandler());
			routes.put("/rest/domain", new DomainRestHandler());
			routes.put("/rest/person", new PersonRestHandler());
			routes.put("/rest/keypair", new KeypairRestHandler());
			routes.put("/rest/sign", new SignRestHandler());
			routes.put("/rest/authcheck", new AuthCheckRestHandler());

			HttpService server = new HttpService(port, maxConnections, routes);
			HttpController
					.setDefaultAuthenticator(new BasicAuthenticator("HefameKeyVault", new DomainPasswordMatcher()));

			ShutdownHook shutdownHook = new ShutdownHook(server);
			Runtime.getRuntime().addShutdownHook(shutdownHook);

			logger.info("Arrancando servidor HTTP en el puerto {}", port);
			server.start();
		} catch (Exception e) {
			logger.fatal("Abortando la ejecución del servidor debido a una excepción");
			logger.catching(Level.FATAL, e);
			System.exit(2);
		}

	}

}
