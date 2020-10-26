package es.hefame.keyvault.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.HashAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.SignatureFormat;
import es.hefame.keyvault.datastructure.model.Keypair;

import com.sun.org.apache.xml.internal.security.utils.XMLUtils;

public class XmlDSig {
	private XmlDSig() {

	}

	private static Logger logger = LogManager.getLogger();

	public static byte[] sign(byte[] payload, Keypair keypair, String nodeId, HashAlgorithm hashAlgorithm,
			SignatureFormat signatureFormat) throws HttpException {
		if (nodeId.charAt(0) != '#') {
			nodeId = '#' + nodeId;
		}

		if (hashAlgorithm != null && signatureFormat != null) {
			// Esto lo pongo pa que no de porculo el ESLint
		}

		XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
		Reference ref;

		try {

			ref = fac.newReference(nodeId, fac.newDigestMethod(DigestMethod.SHA1, null), null, null, null);

			// Create the SignedInfo.
			SignedInfo si = fac.newSignedInfo(
					fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
					fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null), Collections.singletonList(ref));

			KeyInfoFactory kif = fac.getKeyInfoFactory();
			List<X509Certificate> x509Content = new ArrayList<>();
			x509Content.add(keypair.getCertificate());
			X509Data xd = kif.newX509Data(x509Content);
			KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

			// Instantiate the document to be signed.
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Document docSignatura;

			docSignatura = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(payload));
			Element root = (Element) docSignatura.getFirstChild();

			if (root == null)
				throw new HttpException(400, "El XML no es valido");

			if (root.getElementsByTagName("Data").item(0) == null)
				throw new HttpException(400, "No se encuentra el elemento SignedData.Data en la estructura XML");

			((Element) root.getElementsByTagName("Data").item(0)).setIdAttribute("id", true);

			DOMSignContext dsc = new DOMSignContext(keypair.getPrivateKey(), root);
			XMLSignature signature = fac.newXMLSignature(si, ki);
			signature.sign(dsc);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XMLUtils.outputDOM(docSignatura.getDocumentElement(), bos);

			return bos.toByteArray();

		} catch (Exception ex) {
			logger.error("Ocurrio un error al realizar la firma");
			logger.catching(ex);

			throw new HttpException(400, ex.getMessage());
		}

	}
}
