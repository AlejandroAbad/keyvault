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

	public static byte[] signPKCS7(byte[] payload, Keypair keypair, HashAlgorithm hashAlgorithm,
			boolean attachPayload, boolean withChain) throws HttpException {
		List<X509Certificate> certs = null;
		if (withChain) {
			certs = keypair.getCertificateChain();
		}
		return Pkcs7.sign(payload, keypair.getCertificate(), keypair.getPrivateKey(), hashAlgorithm, certs,
				attachPayload);
	}

	public static byte[] signPKCS1(byte[] payload, Keypair keypair, HashAlgorithm hashAlgorithm)
			throws HttpException {
		return Pkcs1.sign(payload, keypair.getPrivateKey(), hashAlgorithm);
	}

	public static byte[] signPAdES(byte[] payload, Keypair keypair, HashAlgorithm hashAlgorithm,
			SignatureFormat signatureFormat) throws HttpException {
		return PAdES.sign(payload, keypair, hashAlgorithm, signatureFormat);
	}

	public static byte[] signXmlDSig(byte[] payload, Keypair keypair, String node, HashAlgorithm hashAlgorithm,
			SignatureFormat signatureFormat) throws HttpException {
		return XmlDSig.sign(payload, keypair, node, hashAlgorithm, signatureFormat);
	}

}
