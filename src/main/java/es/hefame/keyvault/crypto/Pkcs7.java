package es.hefame.keyvault.crypto;

import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.HashAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.KeyAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.SignatureAlgorithm;


public class Pkcs7
{

	private Pkcs7()
	{
	}

	static
	{
		Security.addProvider(new BouncyCastleProvider());
	}

	public static byte[] sign(byte[] data, X509Certificate cert, Key key, HashAlgorithm hashAlgorithm, List<X509Certificate> caCerts, boolean attachPayload) throws HttpException
	{
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.build(hashAlgorithm, KeyAlgorithm.build(key.getAlgorithm()));

		try
		{
			PrivateKey privateKey = (PrivateKey) key;

			ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm.name()).setProvider("BC")
					.build(privateKey);
			DigestCalculatorProvider digestCalculator = new JcaDigestCalculatorProviderBuilder().setProvider("BC").build();
			JcaSignerInfoGeneratorBuilder signerInfoBuilder = new JcaSignerInfoGeneratorBuilder(digestCalculator);
			SignerInfoGenerator sig = signerInfoBuilder.build(contentSigner, cert);
			CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
			generator.addSignerInfoGenerator(sig);

			if (caCerts != null)
			{
				JcaCertStore certChainStore = new JcaCertStore(caCerts);
				generator.addCertificates(certChainStore);
			}

			CMSTypedData cmsdata = new CMSProcessableByteArray(data);
			CMSSignedData signeddata;

			signeddata = generator.generate(cmsdata, attachPayload);
			return signeddata.getEncoded();

		}
		catch (IllegalArgumentException e)
		{
			throw new HttpException(501, "Signature algorithm not implemented", e);
		}
		catch (IOException | OperatorCreationException | CertificateEncodingException | CMSException e)
		{
			throw new HttpException(500, "Unable to generate signature", e);
		}

	}

}
