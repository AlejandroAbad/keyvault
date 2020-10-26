package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import es.hefame.keyvault.util.exception.NoSuchAcronymException;

public enum KeypairFormat {

	PKCS12(), JKS(), PEM();

	private static Map<String, KeypairFormat> aliases;
	static {
		aliases = new HashMap<>();

		aliases.put("p12", PKCS12);
		aliases.put("pfx", PKCS12);
		aliases.put("pkcs12", PKCS12);
		aliases.put("pkcs-12", PKCS12);
		aliases.put("pkcs#12", PKCS12);

		aliases.put("jks", JKS);

		aliases.put("pem", PEM);
	}

	private KeypairFormat() {

	}

	public static KeypairFormat build(String incoming) throws NoSuchAcronymException {
		if (incoming == null) {
			throw new NoSuchAcronymException("keypair_format");
		}

		String modified = incoming.toLowerCase();

		KeypairFormat format = aliases.get(modified.toLowerCase());

		if (format != null) {
			return format;
		} else {
			throw new NoSuchAcronymException("keypair_format", incoming);
		}
	}

}
