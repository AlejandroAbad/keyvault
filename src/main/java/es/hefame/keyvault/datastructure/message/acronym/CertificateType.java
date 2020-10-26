package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import es.hefame.keyvault.util.exception.NoSuchAcronymException;

public enum CertificateType {
	X509("X.509");

	private static Map<String, CertificateType> aliases;
	static {
		aliases = new HashMap<>();

		aliases.put("x.509", X509);
		aliases.put("x509", X509);

	}

	public final String certificateFactoryName;

	private CertificateType(String certificateFactoryName) {
		this.certificateFactoryName = certificateFactoryName;
	}

	public static CertificateType build(String incoming) throws NoSuchAcronymException {
		if (incoming == null) {
			throw new NoSuchAcronymException(CertificateType.class);
		}

		CertificateType algo = aliases.get(incoming.toLowerCase());

		if (algo != null) {
			return algo;
		} else {
			throw new NoSuchAcronymException(CertificateType.class, incoming);
		}
	}

}
