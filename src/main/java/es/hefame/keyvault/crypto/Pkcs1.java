package es.hefame.keyvault.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.HashAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.KeyAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.SignatureAlgorithm;

public class Pkcs1 {

	private Pkcs1() {
	}

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static byte[] sign(byte[] data, Key pk, HashAlgorithm hashAlgorithm) throws HttpException {

		SignatureAlgorithm signAlgorithm = SignatureAlgorithm.build(hashAlgorithm,
				KeyAlgorithm.build(pk.getAlgorithm()));
		try {
			Signature signMachine = Signature.getInstance(signAlgorithm.name(), "BC");
			signMachine.initSign((PrivateKey) pk, new SecureRandom());
			signMachine.update(data);
			return signMachine.sign();
		} catch (NoSuchAlgorithmException e) {
			throw new HttpException(501, "Sign algorithm not implemented", e);
		} catch (ClassCastException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
			throw new HttpException(500, "Error generating signature", e);
		}

	}
}
