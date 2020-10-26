package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import es.hefame.keyvault.util.exception.NoSuchAcronymException;

/*
 * KeyFactory
 */
public enum MimeType {

	PKCS12("application/x-pkcs12", "p12"), CERT("application/x-pem-file", "crt"),
	CA_CERT("application/x-pem-file", "crt"), KEY("application/x-pem-file", "key"),
	CSR("application/x-pem-file", "csr");
	/* CRL; */

	private static Map<String, MimeType> aliases;
	static {
		aliases = new HashMap<>();

		aliases.put("pfx", PKCS12);
		aliases.put("p12", PKCS12);
		aliases.put("pkcs12", PKCS12);

		aliases.put("cert", CERT);
		aliases.put("crt", CERT);
		aliases.put("cer", CERT);
		aliases.put("public", CERT);

		aliases.put("ca", CA_CERT);
		aliases.put("chain", CA_CERT);

		aliases.put("key", KEY);
		aliases.put("pkcs8", KEY);
		aliases.put("p8", KEY);

		aliases.put("csr", CSR);
		aliases.put("p10", CSR);

		// aliases.put("application/pkcs10", CSR);
		// aliases.put("application/pkix-crl", CRL);
	}

	public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
	public static final String END_CERT = "-----END CERTIFICATE-----";
	public static final String BEGIN_PRIVATE = "-----BEGIN RSA PRIVATE KEY-----";
	public static final String END_PRIVATE = "-----END RSA PRIVATE KEY-----";
	public static final String BEGIN_CSR = "-----BEGIN NEW CERTIFICATE REQUEST-----";
	public static final String END_CSR = "-----END NEW CERTIFICATE REQUEST-----";

	public static final String LINE_SEPARATOR = "\r\n";

	public final String mimeType;
	public final String fileExtension;

	private MimeType(String mimeType, String fileExtension) {
		this.mimeType = mimeType;
		this.fileExtension = fileExtension;
	}

	public static MimeType build(String incoming) throws NoSuchAcronymException {
		if (incoming == null) {
			throw new NoSuchAcronymException(MimeType.class);
		}

		MimeType algo = aliases.get(incoming.toLowerCase());

		if (algo != null) {
			return algo;
		} else {
			throw new NoSuchAcronymException(MimeType.class, incoming);
		}
	}

}
