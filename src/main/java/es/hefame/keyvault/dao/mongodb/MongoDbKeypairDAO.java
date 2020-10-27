package es.hefame.keyvault.dao.mongodb;

import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.mongodb.Block;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.Binary;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.datastructure.message.acronym.CertificateType;
import es.hefame.keyvault.datastructure.message.acronym.KeyAlgorithm;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.util.exception.NoSuchAcronymException;

/*
	KEY
	{
		_id: "uid"
		owner: "userFQDN"
		key: {
			algorithm: "RSA"
			data: Blob
		},
		certificate: {
			type: "X509"
			data: Blob
		}
		caChain: [ {type: "X509", data: Blob}, {type: "X509", data: Blob}, ... ]
	}
*/

public class MongoDbKeypairDAO implements KeypairDAO {

	private static Logger logger = LogManager.getLogger();

	private Keypair convertBsonToKeypair(Document doc)
			throws NoSuchAcronymException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {

		String id = doc.getString("_id");
		String owner = doc.getString("owner");

		// Extraccion de la clave privada
		PrivateKey privateKey = convertBsonToPrivateKey(doc.get("key", Document.class));
		X509Certificate certificate = convertBsonToCertificate(doc.get("certificate", Document.class));

		List<Document> caChainCertsDocs = doc.getList("caChain", Document.class);
		List<X509Certificate> caChainCerts = new LinkedList<>();

		for (Document chainedCertDoc : caChainCertsDocs) {
			caChainCerts.add(convertBsonToCertificate(chainedCertDoc));
		}

		return new Keypair(id, owner, certificate, privateKey, caChainCerts);
	}

	private PrivateKey convertBsonToPrivateKey(Document doc)
			throws NoSuchAcronymException, NoSuchAlgorithmException, InvalidKeySpecException {

		KeyAlgorithm keyAlgorithm = KeyAlgorithm.build(doc.getString("algorithm"));
		Binary keyData = doc.get("data", Binary.class);

		// Building the PrivateKey
		KeyFactory kf = KeyFactory.getInstance(keyAlgorithm.name());
		PKCS8EncodedKeySpec keysp = new PKCS8EncodedKeySpec(keyData.getData());
		return kf.generatePrivate(keysp);
	}

	private X509Certificate convertBsonToCertificate(Document doc) throws NoSuchAcronymException, CertificateException {
		CertificateType certType = CertificateType.build(doc.getString("type"));
		Binary certData = doc.get("data", Binary.class);

		// Building the Certificate
		CertificateFactory certificateFactory = CertificateFactory.getInstance(certType.certificateFactoryName);

		return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certData.getData()));
	}

	private Document convertKeypairToBson(Keypair k) throws CertificateEncodingException {

		Document root = new Document();
		root.put("_id", k.getIdentifier());
		root.put("owner", k.getOwnerId());
		root.put("key", convertKeyToBson(k.getPrivateKey()));
		root.put("certificate", convertCertificateToBson(k.getCertificate()));

		List<Document> certChainDocuments = new LinkedList<>();
		k.getCertificateChain().forEach(cert -> {
			try {
				certChainDocuments.add(convertCertificateToBson(cert));
			} catch (CertificateEncodingException e) {
				logger.catching(e);
			}
		});
		root.put("caChain", certChainDocuments);
		return root;
	}

	private Document convertKeyToBson(Key k) {
		Document key = new Document();
		key.put("algorithm", k.getAlgorithm());
		key.put("data", new Binary(k.getEncoded()));
		return key;
	}

	private Document convertCertificateToBson(X509Certificate cert) throws CertificateEncodingException {
		Document crt = new Document();

		crt.put("type", cert.getType());
		crt.put("data", new Binary(cert.getEncoded()));

		return crt;
	}

	@Override
	public Keypair getById(String pairId) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("keypair");
		Document doc = col.find(eq("_id", pairId)).first();
		if (doc == null)
			return null;
		try {
			return convertBsonToKeypair(doc);
		} catch (Exception e) {
			logger.catching(e);
			throw new HException(e.getMessage(), e);
		}
	}

	@Override
	public List<Keypair> getList() throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("keypair");
		List<Keypair> keypairs = new LinkedList<>();

		col.find().forEach(new Consumer<Document>() {
			@Override
			public void accept(Document doc) {
				try {
					Keypair keypair = convertBsonToKeypair(doc);
					keypairs.add(keypair);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		});

		return keypairs;
	}

	@Override
	public List<Keypair> getOwnedBy(Person owner) throws HException {
		return this.getOwnedByPersonId(owner.getIdentifier());
	}

	@Override
	public List<Keypair> getOwnedByPersonId(String ownerId) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("keypair");
		List<Keypair> keypairs = new LinkedList<>();
		col.find(eq("owner", ownerId)).forEach(new Consumer<Document>() {
			@Override
			public void accept(Document doc) {
				try {
					Keypair keypair = convertBsonToKeypair(doc);
					keypairs.add(keypair);
				} catch (Exception e) {
					logger.catching(e);
				}
			}
		});
		return keypairs;
	}

	@Override
	public boolean insert(Keypair newKeypair) throws HException {

		MongoCollection<Document> col = MongoDbConnection.getCollection("keypair");

		try {
			Document doc = convertKeypairToBson(newKeypair);
			col.insertOne(doc);
			return true;
		} catch (MongoException | CertificateEncodingException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	@Override
	public boolean update(Keypair modifiedKeypair) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("keypair");

		try {
			Document doc = convertKeypairToBson(modifiedKeypair);
			doc.remove("_id");
			col.updateOne(eq("_id", modifiedKeypair.getIdentifier()), doc);
			return true;
		} catch (MongoException | CertificateEncodingException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	@Override
	public boolean delete(String keypairId) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("keypair");

		try {
			col.deleteOne(eq("_id", keypairId));
			return true;
		} catch (MongoException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	public boolean delete(Keypair keypair) throws HException {
		if (keypair == null)
			return false;
		return this.delete(keypair.getIdentifier());
	}

}
