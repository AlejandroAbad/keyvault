package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import es.hefame.keyvault.util.exception.NoSuchAcronymException;

public enum HashAlgorithm {

	MD2(), MD4(), MD5(),

	RIPEMD128(), RIPEMD160(), RIPEMD256(), RIPEMD320(),

	SHA128("SHA-1"), SHA224("SHA-224"), SHA256("SHA-256"), SHA384("SHA-384"), SHA512("SHA-512"),

	GOST3411(), TIGER(), WHIRLPOOL();

	public static final HashAlgorithm DEFAULT = SHA256;
	private static Map<String, HashAlgorithm> aliases;
	static {
		aliases = new HashMap<>();

		aliases.put("md2", MD2);
		aliases.put("md4", MD4);
		aliases.put("md5", MD5);

		aliases.put("sha", SHA128);
		aliases.put("sha-1", SHA128);
		aliases.put("sha-128", SHA128);
		aliases.put("sha-224", SHA224);
		aliases.put("sha-256", SHA256);
		aliases.put("sha-384", SHA384);
		aliases.put("sha-512", SHA512);

		aliases.put("sha1", SHA128);
		aliases.put("sha128", SHA128);
		aliases.put("sha256", SHA256);
		aliases.put("sha224", SHA224);
		aliases.put("sha384", SHA384);
		aliases.put("sha512", SHA512);

		aliases.put("gost", GOST3411);
		aliases.put("gost3411", GOST3411);
		aliases.put("gost-3411", GOST3411);
		aliases.put("tiger", TIGER);
		aliases.put("whirlpool", WHIRLPOOL);

		aliases.put("ripemd128", RIPEMD128);
		aliases.put("ripemd160", RIPEMD160);
		aliases.put("ripemd256", RIPEMD256);
		aliases.put("ripemd320", RIPEMD320);
	}

	public final String itextpdfName;

	private HashAlgorithm(String itextpdfName) {
		if (itextpdfName == null || itextpdfName.trim().length() == 0)
			this.itextpdfName = this.name().trim();
		else
			this.itextpdfName = itextpdfName;
	}

	private HashAlgorithm() {
		this(null);
	}

	public static HashAlgorithm build(String incoming) throws NoSuchAcronymException {
		if (incoming == null) {
			return SHA256;
		}

		HashAlgorithm algo = aliases.get(incoming.toLowerCase());

		if (algo != null) {
			return algo;
		} else {
			throw new NoSuchAcronymException("hash_algorithm", incoming);
		}
	}

}
