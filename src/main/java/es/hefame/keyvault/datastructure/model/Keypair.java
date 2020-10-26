package es.hefame.keyvault.datastructure.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.converter.ByteArrayConverter;
import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.MimeType;
import es.hefame.keyvault.datastructure.model.domain.Domain;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.X500Name;

public class Keypair implements JsonEncodable {

	private static Logger logger = LogManager.getLogger();

	private String identifier;
	private String ownerId;

	private X509Certificate cert;
	private Key key;
	private List<X509Certificate> certChain = new LinkedList<>();

	/**
	 * Instanciación a partir de un PKCS#12
	 * @param identifier
	 * @param pkcs12
	 * @param owner
	 * @param passphrase
	 * @throws HException
	*/
	public Keypair(String identifier, byte[] pkcs12, String owner, char[] passphrase) throws HException {
		this.identifier = identifier;
		this.ownerId = owner;
		this.parsePKCS12(pkcs12, passphrase);
	}


	public Keypair(String identifier, String owner, X509Certificate cert, Key key, List<X509Certificate> chain) {
		this.identifier = identifier;
		this.ownerId = owner;
		this.cert = cert;
		this.key = key;
		this.certChain = chain;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public X509Certificate getCertificate() {
		return cert;
	}

	public Key getPrivateKey() {
		return key;
	}

	public List<X509Certificate> getCertificateChain() {
		return certChain;
	}

	public X509Certificate[] getCertificateChainArray() {
		return certChain.toArray(new X509Certificate[0]);
	}

	public void setOwner(String ownerId) {
		this.ownerId = ownerId;
	}

	public void setKeypairFromPKCS12(byte[] pkcs12, char[] passphrase) throws HttpException {
		this.parsePKCS12(pkcs12, passphrase);
	}

	public void setKeypair(X509Certificate cert, Key key, List<X509Certificate> chain) {
		this.cert = cert;
		this.key = key;
		this.certChain = chain;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode() {
		JSONObject root = new JSONObject();
		root.put("id", this.getIdentifier());

		JSONObject ownerElement = new JSONObject();
		ownerElement.put("id", this.getOwnerId());
		ownerElement.put("name", Domain.getPersonNameFromFQDN(this.getOwnerId()));
		ownerElement.put("domain", Domain.getDomainIdFromFQDN(this.getOwnerId()));

		root.put("owner", ownerElement);

		{
			root.put("subject", this.cert.getSubjectDN().getName());
			root.put("issuer", this.cert.getIssuerDN().getName());
			root.put("not_after", this.cert.getNotAfter().getTime());
			root.put("not_before", this.cert.getNotBefore().getTime());
			root.put("serial_number", String.valueOf(this.cert.getSerialNumber()));
			root.put("certificate_type", this.cert.getType());
			root.put("privatekey_algorithm", this.key.getAlgorithm());
			try {
				root.put("x509", ByteArrayConverter.toBase64(this.cert.getEncoded()));
			} catch (CertificateEncodingException e) {
				logger.error("Error al convertir el certificado a X509");
				logger.catching(e);
			}

			JSONArray json_cert_chain = new JSONArray();
			for (X509Certificate chained_cert : this.certChain) {
				JSONObject json_chained_cert = new JSONObject();
				json_chained_cert.put("issuer", chained_cert.getIssuerDN().getName());
				json_chained_cert.put("subject", chained_cert.getSubjectDN().getName());
				json_cert_chain.add(json_chained_cert);
			}
			root.put("chain", json_cert_chain);
		}

		return root;
	}

	public byte[] getAsMimeType(MimeType type) throws HttpException {

		if (type != null) {
			switch (type) {
				case PKCS12:
					return getP12("colchonero".toCharArray());
				case CERT:
					return getCertPem();
				case CA_CERT:
					return getCaPem();
				case KEY:
					return getKeyPem();
				case CSR:
					return getCSR();
			}
		}

		return this.jsonEncode().toJSONString().getBytes();
	}

	public String getFileName(MimeType type) {
		return this.identifier.toLowerCase() + '.' + type.fileExtension;
	}

	private byte[] getCertPem() throws HttpException {
		try {
			Base64.Encoder encoder = Base64.getMimeEncoder(64, MimeType.LINE_SEPARATOR.getBytes());
			StringBuilder sb = new StringBuilder();
			sb.append(MimeType.BEGIN_CERT).append(MimeType.LINE_SEPARATOR);
			sb.append(new String(encoder.encode(cert.getEncoded())));
			sb.append(MimeType.LINE_SEPARATOR).append(MimeType.END_CERT);
			return sb.toString().getBytes();
		} catch (Exception e) {
			throw new HttpException(500, "Error while generating CER file", e);
		}
	}

	private byte[] getCaPem() throws HttpException {
		try {
			Base64.Encoder encoder = Base64.getMimeEncoder(64, MimeType.LINE_SEPARATOR.getBytes());
			StringBuilder sb = new StringBuilder();

			for (X509Certificate certInChain : this.certChain) {
				sb.append(MimeType.BEGIN_CERT).append(MimeType.LINE_SEPARATOR);
				sb.append(new String(encoder.encode(certInChain.getEncoded())));
				sb.append(MimeType.LINE_SEPARATOR).append(MimeType.END_CERT).append(MimeType.LINE_SEPARATOR);
			}

			return sb.toString().getBytes();
		} catch (Exception e) {
			throw new HttpException(500, "Error while generating CA file", e);
		}
	}

	private byte[] getKeyPem() throws HttpException {
		try {
			Base64.Encoder encoder = Base64.getMimeEncoder(64, MimeType.LINE_SEPARATOR.getBytes());
			StringBuilder sb = new StringBuilder();
			sb.append(MimeType.BEGIN_PRIVATE).append(MimeType.LINE_SEPARATOR);
			sb.append(new String(encoder.encode(key.getEncoded())));
			sb.append(MimeType.LINE_SEPARATOR).append(MimeType.END_PRIVATE);
			return sb.toString().getBytes();
		} catch (Exception e) {
			throw new HttpException(500, "Error while generating KEY file", e);
		}
	}

	private byte[] getP12(char[] password) throws HttpException {
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			try {
				KeyStore ks = KeyStore.getInstance("PKCS12");
				ks.load(null, null);
				ks.setKeyEntry(this.identifier, key, "".toCharArray(), this.certChain.toArray(new X509Certificate[0]));
				ks.store(outStream, password);
				return outStream.toByteArray();
			} catch (Exception e) {
				throw new HttpException(500, "Error while generating P12 file", e);
			}
		} catch (IOException e) {
			throw new HttpException(500, "Error while generating P12 file", e);
		}
	}

	private byte[] getCSR() throws HttpException {
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			try (PrintStream ps = new PrintStream(outStream)) {

				try {
					X500Name name = new X500Name(cert.getSubjectX500Principal().getEncoded());
					Signature sig = Signature.getInstance(cert.getSigAlgName());
					sig.initSign((PrivateKey) key);
					PKCS10 pkcs10 = new PKCS10(cert.getPublicKey());
					pkcs10.encodeAndSign(name, sig);
					pkcs10.print(ps);
					
					return outStream.toByteArray();
				} catch (Exception e) {
					throw new HttpException(500, "Error while generating P12 file", e);
				}
			}
		} catch (IOException e) {
			throw new HttpException(500, "Error while generating P12 file", e);
		}
	}

	private void parsePKCS12(byte[] pkcs12, char[] passphrase) throws HttpException {
		try {
			KeyStore p12 = KeyStore.getInstance("pkcs12");
			p12.load(new ByteArrayInputStream(pkcs12), passphrase);
			this.certChain = new LinkedList<>();

			Enumeration<String> e = p12.aliases();
			if (e.hasMoreElements()) {
				String alias = e.nextElement();

				if (p12.isKeyEntry(alias)) {
					Certificate[] caChain = p12.getCertificateChain(alias);
					this.cert = (X509Certificate) p12.getCertificate(alias);
					this.key = p12.getKey(alias, passphrase);

					for (Certificate ce : caChain) {
						this.certChain.add((X509Certificate) ce);
					}
				} else {
					throw new HttpException(400, "The alias does not have private key data");
				}
			} else {
				throw new HttpException(400, "No aliases in the PKCS#12 chain");
			}
		} catch (ClassCastException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException
				| CertificateException | IOException e) {
			throw new HttpException(400, e.getMessage(), e);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append(" [");
		sb.append("\n\tIdentifier: ").append(this.getIdentifier());
		sb.append("\n]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Keypair other = (Keypair) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	@Override
	public Keypair clone() {
		List<X509Certificate> newCertChain = new LinkedList<>();
		for (X509Certificate chained_cert : this.certChain) {
			newCertChain.add(chained_cert);
		}

		return new Keypair(identifier, ownerId, cert, key, newCertChain);
	}
}
