package es.hefame.keyvault.dao.mongodb;

import es.hefame.hcore.HException;
import es.hefame.hcore.converter.ByteArrayConverter;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.DomainDAO;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.dao.PersonDAO;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;
import es.hefame.keyvault.datastructure.model.domain.LdapDomain;
import es.hefame.keyvault.datastructure.model.domain.SapDomain;

public class Populate {

	public static void main(String[] args) throws HException {
		DAO.setProvider("MongoDb");
		DomainDAO domainDAO = DAO.domain();
		PersonDAO personDAO = DAO.person();
		KeypairDAO keypairDAO = DAO.keypair();

		//Domain l = new LocalDomain("LOCAL", "auth_local", "person_name", "password");
		Domain h = new LdapDomain("hefame.es", "http://ad1.hefame.es", "dc=hefame,dc=es", "(&(objectclass=user)(|(sAMAccountName={%u})(uid={%u})))");
		Domain s = new SapDomain("P01", "P01", "PeCeroUno");
		//domainDAO.insert(l);
		domainDAO.insert(h);
		domainDAO.insert(s);

		//Person admin = new Person("admin", "local");
		Person alejandro = new Person("alejandro_ac", "hefame.es");
		Person juan = new Person("juan_sr", "hefame.es");
		Person cpdAix = new Person("cpd_aix", "P01");
		//personDAO.insert(admin);
		personDAO.insert(alejandro);
		personDAO.insert(juan);
		personDAO.insert(cpdAix);

		Keypair solano = new Keypair("CADUCADO", ByteArrayConverter.fromBase64(
				"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"),
				alejandro.getIdentifier(), "colchonero".toCharArray());
		keypairDAO.insert(solano);

	}

}
