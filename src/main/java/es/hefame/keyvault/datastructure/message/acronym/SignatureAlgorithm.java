package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import es.hefame.keyvault.util.exception.NoSuchAcronymException;

/*
 * KeyFactory
 */
public enum SignatureAlgorithm {

	SHA224WITHDSA(0), SHA256WITHDSA(1), SHA384WITHDSA(2), SHA512WITHDSA(3),

	SHA224WITHECDSA(4), SHA256WITHECDSA(5), SHA384WITHECDSA(6), SHA512WITHECDSA(7), RIPEMD160WITHECDSA(8),

	MD2WITHRSA(9), MD4WITHRSA(10), MD5WITHRSA(11), SHA128WITHRSA(12), SHA224WITHRSA(13), SHA256WITHRSA(14),
	SHA384WITHRSA(15), SHA512WITHRSA(16), RIPEMD128WITHRSA(17), RIPEMD160WITHRSA(18), RIPEMD256WITHRSA(19),

	GOST3411WITHECGOST3410(20);

	private static Map<String, SignatureAlgorithm> aliases;
	static {
		aliases = new HashMap<>();

		aliases.put("sha128withdsa", SHA224WITHDSA);
		aliases.put("sha224withdsa", SHA224WITHDSA);
		aliases.put("sha256withdsa", SHA256WITHDSA);
		aliases.put("sha384withdsa", SHA384WITHDSA);
		aliases.put("sha512withdsa", SHA512WITHDSA);

		aliases.put("sha128withecdsa", SHA224WITHECDSA);
		aliases.put("sha224withecdsa", SHA224WITHECDSA);
		aliases.put("sha256withecdsa", SHA256WITHECDSA);
		aliases.put("sha384withecdsa", SHA384WITHECDSA);
		aliases.put("sha512withecdsa", SHA512WITHECDSA);

		aliases.put("ripemd160withecdsa", RIPEMD160WITHECDSA);

		aliases.put("md2withrsa", MD2WITHRSA);
		aliases.put("md4withrsa", MD4WITHRSA);
		aliases.put("md5withrsa", MD5WITHRSA);
		aliases.put("sha128withrsa", SHA128WITHRSA);
		aliases.put("sha224withrsa", SHA224WITHRSA);
		aliases.put("sha256withrsa", SHA256WITHRSA);
		aliases.put("sha384withrsa", SHA384WITHRSA);
		aliases.put("sha512withrsa", SHA512WITHRSA);
		aliases.put("ripemd128withrsa", RIPEMD128WITHRSA);
		aliases.put("ripemd160withrsa", RIPEMD160WITHRSA);
		aliases.put("ripemd256withrsa", RIPEMD256WITHRSA);

		aliases.put("gost3411withecgost3410", GOST3411WITHECGOST3410);
	}

	public final int code;

	private SignatureAlgorithm(int code) {
		this.code = code;
	}

	public static SignatureAlgorithm build(HashAlgorithm hash_algo, KeyAlgorithm key_algo)
			throws NoSuchAcronymException {
		if (hash_algo == null) {
			throw new NoSuchAcronymException(SignatureAlgorithm.class);
		}
		if (key_algo == null) {
			throw new NoSuchAcronymException(SignatureAlgorithm.class);
		}

		String name = hash_algo.name() + "with" + key_algo.name();
		name = name.toLowerCase();

		SignatureAlgorithm algo = aliases.get(name.toLowerCase());

		if (algo != null) {
			return algo;
		} else {
			throw new NoSuchAcronymException(SignatureAlgorithm.class, name);
		}

	}

}
