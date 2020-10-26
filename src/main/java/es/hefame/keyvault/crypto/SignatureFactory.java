package es.hefame.keyvault.crypto;

import java.security.cert.X509Certificate;
import java.util.List;

import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.HashAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.SignatureFormat;
import es.hefame.keyvault.datastructure.model.Keypair;

public class SignatureFactory {

	private SignatureFactory() {

	}

	public static byte[] sign_pkcs7(byte[] payload, Keypair keypair, HashAlgorithm hash_algorithm,
			boolean attach_payload, boolean with_chain) throws HttpException {
		List<X509Certificate> certs = null;
		if (with_chain) {
			certs = keypair.getCertificateChain();
		}
		return Pkcs7.sign(payload, keypair.getCertificate(), keypair.getPrivateKey(), hash_algorithm, certs,
				attach_payload);
	}

	public static byte[] sign_pkcs1(byte[] payload, Keypair keypair, HashAlgorithm hash_algorithm)
			throws HttpException {
		return Pkcs1.sign(payload, keypair.getPrivateKey(), hash_algorithm);
	}

	public static byte[] sign_pades(byte[] payload, Keypair keypair, HashAlgorithm hash_algorithm,
			SignatureFormat signature_format) throws HttpException {
		return PAdES.sign(payload, keypair, hash_algorithm, signature_format);
	}

	public static byte[] sign_xmldsig(byte[] payload, Keypair keypair, String node, HashAlgorithm hash_algorithm,
			SignatureFormat signature_format) throws HttpException {
		return XmlDSig.sign(payload, keypair, node, hash_algorithm, signature_format);
	}

}
