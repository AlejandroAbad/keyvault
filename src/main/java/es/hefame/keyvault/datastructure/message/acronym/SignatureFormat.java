package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;

import es.hefame.keyvault.util.exception.NoSuchAcronymException;

public enum SignatureFormat {

	PKCS1("PKCS#1"), PKCS7("PKCS#7"), PADES_CMS("PAdES-CMS", CryptoStandard.CMS),
	PADES_BES("PAdES-BES", CryptoStandard.CADES), XMLDSIG("xmldsig");

	private static Logger L = LogManager.getLogger();
	private static Map<String, SignatureFormat> aliases;
	static {
		aliases = new HashMap<String, SignatureFormat>();

		aliases.put("pkcs1", PKCS1);
		aliases.put("pkcs-1", PKCS1);
		aliases.put("pkcs#1", PKCS1);
		aliases.put("cms", PKCS7);
		aliases.put("pkcs7", PKCS7);
		aliases.put("pkcs-7", PKCS7);
		aliases.put("pkcs#7", PKCS7);
		aliases.put("pades", PADES_CMS);
		aliases.put("pades-cms", PADES_CMS);
		aliases.put("pades-cms", PADES_CMS);
		aliases.put("pades-bes", PADES_BES);
		aliases.put("xmldsig", XMLDSIG);
	}

	public final String display_name;
	public final CryptoStandard itextpdf_cryptostandard;

	private SignatureFormat(String display_name, CryptoStandard itextpdf_cryptostandard) {
		this.display_name = display_name;
		this.itextpdf_cryptostandard = itextpdf_cryptostandard;
	}

	private SignatureFormat(String display_name) {
		this(display_name, null);
	}

	public static SignatureFormat build(String incoming) throws NoSuchAcronymException {
		if (incoming == null) {
			throw new NoSuchAcronymException("signature_format");
		}

		String modified = incoming.toLowerCase();

		SignatureFormat format = aliases.get(modified.toLowerCase());

		if (format != null) {
			L.debug("Se resuelve el SignatureFormat como [{}]", () -> format.name());
			return format;
		} else {
			throw new NoSuchAcronymException("signature_format", incoming);

		}
	}

}
