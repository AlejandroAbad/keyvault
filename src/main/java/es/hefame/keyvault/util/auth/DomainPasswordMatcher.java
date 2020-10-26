package es.hefame.keyvault.util.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.authentication.rfc7235.rfc7617.BasicPasswordMatcher;
import es.hefame.hcore.http.exchange.IHttpRequest;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class DomainPasswordMatcher implements BasicPasswordMatcher {

	private static Logger log = LogManager.getLogger();

	@Override
	public boolean matchPassword(String realm, String username, String password, IHttpRequest request) {
		log.debug("Comprobando la contraseña del usuario [{}]", username);
		String domainName = Domain.getDomainIdFromFQDN(username);
		log.debug("El nombre de dominio del usuario es [{}]", domainName);

		if (domainName == null) {
			log.info("el usuario no tiene un dominio asociado");
			return false;
		}

		try {
			Domain domain = DAO.domain().get_by_id(domainName);
			log.debug("El dominio del usuario es [{}]", domain);
			if (domain == null) {
				log.info("No se encuentra el dominio [{}]", domainName);
				return false;
			}

			return domain.authenticate(username, password, request);

		} catch (HException e) {
			log.error("Ocurrio un error al autenticar al usuario en el dominio");
			log.catching(e);
		}

		return false;
	}

}
