package es.hefame.keyvault.datastructure.message.sign;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.HashAlgorithm;
import es.hefame.keyvault.datastructure.message.acronym.PayloadEncoding;
import es.hefame.keyvault.datastructure.message.acronym.SignatureFormat;

// TODO: Esta clase debe poder extenderse para aceptar opciones especificas a cada metodo de firma.
public class SignRequestMessageParser {
	private static Logger logger = LogManager.getLogger();

	private String certId;
	private byte[] payload;
	private PayloadEncoding payloadEncoding;
	private SignatureFormat signatureFormat;
	private HashAlgorithm hashAlgorithm;

	// Format-specific options
	// # PKCS-7
	private boolean appendCAChain;
	private boolean attachPayload;

	// # XMLDSIG
	private String xmldsigNodeId;

	public SignRequestMessageParser(byte[] request) throws HException {
		JSONParser parser = new JSONParser();

		try {
			if (request.length == 0) {
				throw new HttpException(400, "No body data found");
			}
			Object obj = parser.parse(new String(request));
			JSONObject jsonObject = (JSONObject) obj;

			// Certificate ID
			this.certId = (String) jsonObject.get("cert_id");
			if (certId == null) {
				throw new HttpException(400, "No certificate ID was specified");
			}

			// Signature Type
			signatureFormat = SignatureFormat.build((String) jsonObject.get("signature_format"));

			// Payload encoding
			this.payloadEncoding = PayloadEncoding.build((String) jsonObject.get("payload_encoding"));

			// Signature Algorithm
			hashAlgorithm = HashAlgorithm.build((String) jsonObject.get("hash_algorithm"));

			// Payload
			String rawPayload = (String) jsonObject.get("payload");
			if (rawPayload == null) {
				throw new HttpException(400, "No payload was supplied");
			}
			this.payload = payloadEncoding.decode(rawPayload);

			Object o;
			// PKCS#7
			// Attach payload ?
			this.attachPayload = true;
			o = jsonObject.get("attach_payload");
			if (o != null) {
				this.attachPayload = (Boolean) o;
			}

			// Append ca certs ?
			this.appendCAChain = true;
			o = jsonObject.get("append_ca_chain");
			if (o != null) {
				this.appendCAChain = (Boolean) o;
			}

			// XMLDSIG
			// Node ID
			this.xmldsigNodeId = "data";
			o = jsonObject.get("xmldsig_node_id");
			if (o != null) {
				this.xmldsigNodeId = o.toString();
			}

		} catch (ClassCastException | ParseException e) {
			logger.error("Error al deserializar el JSON");
			logger.catching(e);
			throw new HttpException(400, "Error al deserializar el JSON");
		}

	}

	public String getCertId() {
		return certId;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public SignatureFormat getSignatureFormat() {
		return signatureFormat;
	}

	public HashAlgorithm getHashAlgorithm() {
		return hashAlgorithm;
	}

	public boolean appendChain() {
		return appendCAChain;
	}

	public boolean attachPayload() {
		return attachPayload;
	}

	public String xmldsigNodeId() {
		return xmldsigNodeId;
	}

}
