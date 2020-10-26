package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import es.hefame.keyvault.util.exception.NoSuchAcronymException;


public enum KeyAlgorithm {
	X509(0), DSA(1), DH(2), EC(3), ECMQV(4), ECDSA(5), ECDH(6), ECDHC(7), RSA(8), GOST3410(9), ECGOST3410(10),
	ELGAMAL(11);

	private static Map<String, KeyAlgorithm> aliases;
	static {
		aliases = new HashMap<>();

		aliases.put("rsa", RSA);
		aliases.put("dsa", DSA);
		aliases.put("ec", EC);
		aliases.put("ecmqv", ECMQV);
		aliases.put("ecdsa", ECDSA);
		aliases.put("ecdh", ECDH);
		aliases.put("ecdhc", ECDHC);
		aliases.put("elgamal", ELGAMAL);

		aliases.put("x.509", X509);
		aliases.put("x509", X509);

		aliases.put("dh", DH);
		aliases.put("diffiehellman", DH);
		aliases.put("diffie-hellman", DH);

		aliases.put("gost3410", GOST3410);
		aliases.put("gost-3410", GOST3410);
		aliases.put("gost-3410-94", GOST3410);

		aliases.put("ecgost3410", ECGOST3410);
		aliases.put("ecgost-3410", ECGOST3410);
		aliases.put("gost-3410-2001", ECGOST3410);
	}

	public final int code;

	private KeyAlgorithm(int code) {
		this.code = code;
	}

	public static KeyAlgorithm build(String incoming) throws NoSuchAcronymException {
		if (incoming == null) {
			throw new NoSuchAcronymException(KeyAlgorithm.class);
		}

		KeyAlgorithm algo = aliases.get(incoming.toLowerCase());

		if (algo != null) {
			return algo;
		} else {
			throw new NoSuchAcronymException(KeyAlgorithm.class, incoming);
		}
	}

}
