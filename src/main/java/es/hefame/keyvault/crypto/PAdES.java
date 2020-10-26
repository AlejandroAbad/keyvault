package es.hefame.keyvault.crypto;

import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.Security;


import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.HashAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.SignatureFormat;
import es.hefame.keyvault.datastructure.model.Keypair;

public class PAdES
{
	private PAdES()
	{
		// http://developers.itextpdf.com/examples/security-itext5/digital-signatures-white-paper
	}

	static
	{
		Security.addProvider(new BouncyCastleProvider());
	}

	public static byte[] sign(byte[] payload, Keypair keypair, HashAlgorithm hashAlgorithm, SignatureFormat signatureFormat) throws HttpException
	{
		try
		{
			PdfReader reader = new PdfReader(payload);
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			PdfSignatureAppearance appearance = PdfStamper.createSignature(reader, os, '\0').getSignatureAppearance();
			// appearance.setCertificationLevel(PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED);
			// appearance.setCertificate(keypair.get_certificate());
			// appearance.setReason("Una buena razon");
			// appearance.setLocation("La conchinchina");
			// appearance.setContact(keypair.get_certificate().getSubjectDN().getName());
			// appearance.setImage(Image.getInstance("C:\\Users\\alejandro_ac\\Desktop\\pichote.png"));
			// appearance.setSignDate(new GregorianCalendar(2016, 11, 8, 15, 15, 15));
			// appearance.setLayer2Text(appearance.getContact() + "\nEn " + appearance.getLocation() + ", " + appearance.getSignDate().toInstant().toString());
			// appearance.setLayer4Text("LAyer4TEXT");
			// appearance.setVisibleSignature(new Rectangle(20, 20, 200, 120), 1, "sig");

			TSAClient tsaClient = new TSAClientBouncyCastle("https://freetsa.org/tsr");

			ExternalSignature es = new PrivateKeySignature((PrivateKey) keypair.getPrivateKey(), hashAlgorithm.itextpdfName, "BC");
			ExternalDigest digest = new BouncyCastleDigest();

			MakeSignature.signDetached(appearance, digest, es, keypair.getCertificateChainArray(), null, null, tsaClient, 0, signatureFormat.itextpdf_cryptostandard);

			return os.toByteArray();

		}
		catch (Exception e)
		{
			throw new HttpException(500, "Error generating signature", e);
		}

	}
}
