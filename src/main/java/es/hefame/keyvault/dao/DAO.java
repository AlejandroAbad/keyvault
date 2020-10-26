package es.hefame.keyvault.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;

public class DAO {

	private static Logger logger = LogManager.getLogger();

	private DAO() {

	}

	private static String daoProvider = "Testing";
	private static final String BASE_PACKAGE = "es.hefame.keyvault.dao";

	private static DomainDAO domainDAO = null;
	private static PersonDAO personDAO = null;
	private static KeypairDAO keypairDAO = null;

	public static void setProvider(String daoProvider) throws HException {
		logger.debug("Estableciendo el proveedor de datos a {}", daoProvider);
		DAO.daoProvider = daoProvider;
		DAO.setDomainDAO();
		DAO.setPersonDAO();
		DAO.setKeypairDAO();
	}

	public static DomainDAO domain() {
		return DAO.domainDAO;
	}

	public static PersonDAO person() {
		return DAO.personDAO;
	}

	public static KeypairDAO keypair() {
		return DAO.keypairDAO;
	}

	@SuppressWarnings("unchecked")
	private static DomainDAO setDomainDAO() throws HException {
		String className = BASE_PACKAGE + "." + daoProvider.toLowerCase() + "." + daoProvider + "DomainDAO";
		Class<? extends DomainDAO> castingClass;
		try {
			castingClass = (Class<? extends DomainDAO>) Class.forName(className);
			Constructor<? extends DomainDAO> constructor = castingClass.getConstructor();
			DAO.domainDAO = constructor.newInstance();
			return DAO.domainDAO;
		} catch (InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | ClassNotFoundException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static PersonDAO setPersonDAO() throws HException {
		String className = BASE_PACKAGE + "." + daoProvider.toLowerCase() + "." + daoProvider + "PersonDAO";
		Class<? extends PersonDAO> castingClass;
		try {
			castingClass = (Class<? extends PersonDAO>) Class.forName(className);
			Constructor<? extends PersonDAO> constructor = castingClass.getConstructor();
			DAO.personDAO = constructor.newInstance();
			return DAO.personDAO;
		} catch (InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | ClassNotFoundException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static KeypairDAO setKeypairDAO() throws HException {
		String className = BASE_PACKAGE + "." + daoProvider.toLowerCase() + "." + daoProvider + "KeypairDAO";
		Class<? extends KeypairDAO> castingClass;
		try {
			castingClass = (Class<? extends KeypairDAO>) Class.forName(className);
			Constructor<? extends KeypairDAO> constructor = castingClass.getConstructor();
			DAO.keypairDAO = constructor.newInstance();
			return DAO.keypairDAO;
		} catch (InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | ClassNotFoundException e) {
			throw new HException(e.getMessage(), e);
		}
	}

}
