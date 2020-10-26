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

public class SignRequestMessageParser {
	private static Logger L = LogManager.getLogger();

	private String cert_id;
	private byte[] payload;
	private PayloadEncoding payload_encoding;
	private SignatureFormat signature_format;
	private HashAlgorithm hash_algorithm;

	// Format-specific options
	// # PKCS-7
	private boolean append_ca_chain;
	private boolean attach_payload;

	// # XMLDSIG
	private String xmldsig_node_id;

	public SignRequestMessageParser(byte[] request) throws HException {
		JSONParser parser = new JSONParser();

		try {
			if (request.length == 0) {
				throw new HttpException(400, "No body data found");
			}
			Object obj = parser.parse(new String(request));
			JSONObject jsonObject = (JSONObject) obj;

			// Certificate ID
			this.cert_id = (String) jsonObject.get("cert_id");
			if (cert_id == null) {
				throw new HttpException(400, "No certificate ID was specified");
			}

			// Signature Type
			signature_format = SignatureFormat.build((String) jsonObject.get("signature_format"));

			// Payload encoding
			this.payload_encoding = PayloadEncoding.build((String) jsonObject.get("payload_encoding"));

			// Signature Algorithm
			hash_algorithm = HashAlgorithm.build((String) jsonObject.get("hash_algorithm"));

			// Payload
			String raw_payload = (String) jsonObject.get("payload");
			if (raw_payload == null) {
				throw new HttpException(400, "No payload was supplied");
			}
			this.payload = payload_encoding.decode(raw_payload);

			Object o;
			// PKCS#7
			// Attach payload ?
			this.attach_payload = true;
			o = jsonObject.get("attach_payload");
			if (o != null) {
				this.attach_payload = (Boolean) o;
			}

			// Append ca certs ?
			this.append_ca_chain = true;
			o = jsonObject.get("append_ca_chain");
			if (o != null) {
				this.append_ca_chain = (Boolean) o;
			}

			// XMLDSIG
			// Node ID
			this.xmldsig_node_id = "data";
			o = jsonObject.get("xmldsig_node_id");
			if (o != null) {
				this.xmldsig_node_id = o.toString();
			}

		} catch (ClassCastException | ParseException e) {
			L.error("Error al deserializar el JSON");
			L.catching(e);
			throw new HttpException(400, "Error al deserializar el JSON");
		}

	}

	public String get_cert_id() {
		return cert_id;
	}

	public byte[] get_payload() {
		return this.payload;
	}

	public SignatureFormat get_signature_format() {
		return signature_format;
	}

	public HashAlgorithm get_hash_algorithm() {
		return hash_algorithm;
	}

	public boolean append_chain() {
		return append_ca_chain;
	}

	public boolean attach_payload() {
		return attach_payload;
	}

	public String xmldsig_node_id() {
		return xmldsig_node_id;
	}

}
