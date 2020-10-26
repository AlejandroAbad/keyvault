package es.hefame.keyvault.dao;

import es.hefame.hcore.HException;

public class DAO {
	private static String dao_provider = "Testing";
	private static final String BASE_PACKAGE = "es.hefame.keyvault.dao";

	private static DomainDAO domain_provider = null;
	private static PersonDAO person_provider = null;
	private static KeypairDAO keypair_provider = null;

	public static void set_provider(String dao_provider) throws HException {
		DAO.dao_provider = dao_provider;
		DAO.set_domain_dao();
		DAO.set_person_dao();
		DAO.set_keypair_dao();
	}

	public static DomainDAO domain() {
		return DAO.domain_provider;
	}

	public static PersonDAO person() {
		return DAO.person_provider;
	}

	public static KeypairDAO keypair() {
		return DAO.keypair_provider;
	}

	@SuppressWarnings("unchecked")
	private static DomainDAO set_domain_dao() throws HException {
		String class_name = BASE_PACKAGE + "." + dao_provider.toLowerCase() + "." + dao_provider + "DomainDAO";
		Class<? extends DomainDAO> casting_class;
		try {
			casting_class = (Class<? extends DomainDAO>) Class.forName(class_name);
			DAO.domain_provider = (DomainDAO) casting_class.newInstance();
			return DAO.domain_provider;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static PersonDAO set_person_dao() throws HException {
		String class_name = BASE_PACKAGE + "." + dao_provider.toLowerCase() + "." + dao_provider + "PersonDAO";
		Class<? extends PersonDAO> casting_class;
		try {
			casting_class = (Class<? extends PersonDAO>) Class.forName(class_name);
			DAO.person_provider = (PersonDAO) casting_class.newInstance();
			return DAO.person_provider;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static KeypairDAO set_keypair_dao() throws HException {
		String class_name = BASE_PACKAGE + "." + dao_provider.toLowerCase() + "." + dao_provider + "KeypairDAO";
		Class<? extends KeypairDAO> casting_class;
		try {
			casting_class = (Class<? extends KeypairDAO>) Class.forName(class_name);
			DAO.keypair_provider = (KeypairDAO) casting_class.newInstance();
			return DAO.keypair_provider;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new HException(e.getMessage(), e);
		}
	}

}
