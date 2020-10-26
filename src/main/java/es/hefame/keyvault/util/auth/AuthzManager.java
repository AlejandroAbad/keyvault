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
	private static Logger L = LogManager.getLogger();

	public static void check(boolean condition) throws HttpException {
		check(condition, "No estas autorizado");
	}

	public static void check(boolean condition, String error_message) throws HttpException {
		if (!condition)
			throw new HttpException(403, error_message);
	}

	public static boolean isPerson(Person input, String personId) {
		if (personId == null || input == null) {
			return false;
		}
		return isPerson(input.get_identifier(), personId);
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
		return person.get_name().equalsIgnoreCase(input);
	}

	public static boolean isPerson(Person input, Person person) {
		if (person == null || input == null) {
			return false;
		}
		return person.equals(input);
	}

	public static boolean isPerson(String input, Person... person_list) {
		for (Person person : person_list) {
			if (isPerson(input, person)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isPerson(Person input, Person... person_list) {
		for (Person person : person_list) {
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
			return domain.equals(input.get_domain());
		} catch (HException e) {
			L.catching(e);
			return false;
		}
	}

	public static boolean inDomain(String input, Domain... domain_list) {
		for (Domain domain : domain_list) {
			if (inDomain(input, domain)) {
				return true;
			}
		}
		return false;
	}

	public static boolean inDomain(Person input, Domain... domain_list) {
		for (Domain domain : domain_list) {
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
		return keypair.getOwnerId().equalsIgnoreCase(input.get_identifier());
	}

	public static boolean in_local_domain(String input) {
		try {
			return inDomain(input, DAO.domain().get_by_id(LOCAL_DOMAIN_NAME));
		} catch (HException e) {
			L.catching(e);
			return false;
		}
	}

	public static boolean in_local_domain(Person input) {
		try {
			return inDomain(input, DAO.domain().get_by_id(LOCAL_DOMAIN_NAME));
		} catch (HException e) {
			L.catching(e);
			return false;
		}
	}

}
