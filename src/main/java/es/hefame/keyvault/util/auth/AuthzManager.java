package es.hefame.keyvault.util.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class AuthzManager {
	private AuthzManager() {

	}

	public static final String LOCAL_DOMAIN_NAME = "local";
	private static Logger logger = LogManager.getLogger();

	public static void check(boolean condition) throws HttpException {
		check(condition, "No estas autorizado");
	}

	public static void check(boolean condition, String errorMessage) throws HttpException {
		if (!condition)
			throw new HttpException(403, errorMessage);
	}

	public static boolean isPerson(Person input, String personId) {
		if (personId == null || input == null) {
			return false;
		}
		return isPerson(input.getIdentifier(), personId);
	}

	public static boolean isPerson(String input, String personId) {
		if (personId == null || input == null) {
			return false;
		}
		return personId.equalsIgnoreCase(input);
	}

	public static boolean isPerson(String input, Person person) {
		if (person == null || input == null) {
			return false;
		}
		return person.getName().equalsIgnoreCase(input);
	}

	public static boolean isPerson(Person input, Person person) {
		if (person == null || input == null) {
			return false;
		}
		return person.equals(input);
	}

	public static boolean isPerson(String input, Person... personList) {
		for (Person person : personList) {
			if (isPerson(input, person)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPerson(Person input, Person... personList) {
		for (Person person : personList) {
			if (isPerson(input, person)) {
				return true;
			}
		}
		return false;
	}

	public static boolean inDomain(String input, Domain domain) {
		if (domain == null || input == null) {
			return false;
		}
		return domain.getIdentifier().equalsIgnoreCase(Domain.getDomainIdFromFQDN(input));
	}

	public static boolean inDomain(Person input, Domain domain) {
		if (domain == null || input == null) {
			return false;
		}
		try {
			return domain.equals(input.getDomain());
		} catch (HException e) {
			logger.catching(e);
			return false;
		}
	}

	public static boolean inDomain(String input, Domain... domainList) {
		for (Domain domain : domainList) {
			if (inDomain(input, domain)) {
				return true;
			}
		}
		return false;
	}

	public static boolean inDomain(Person input, Domain... domainList) {
		for (Domain domain : domainList) {
			if (inDomain(input, domain)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isKeyOwner(String input, Keypair keypair) {
		return keypair.getOwnerId().equalsIgnoreCase(input);
	}

	public static boolean isKeyOwner(Person input, Keypair keypair) {
		return keypair.getOwnerId().equalsIgnoreCase(input.getIdentifier());
	}

	public static boolean inLocalDomain(String input) {
		try {
			return inDomain(input, DAO.domain().getById(LOCAL_DOMAIN_NAME));
		} catch (HException e) {
			logger.catching(e);
			return false;
		}
	}

	public static boolean inLocalDomain(Person input) {
		try {
			return inDomain(input, DAO.domain().getById(LOCAL_DOMAIN_NAME));
		} catch (HException e) {
			logger.catching(e);
			return false;
		}
	}

}
