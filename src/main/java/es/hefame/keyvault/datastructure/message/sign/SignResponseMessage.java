package es.hefame.keyvault.datastructure.message.sign;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;
import es.hefame.hcore.converter.ByteArrayConverter;
import es.hefame.keyvault.datastructure.message.acronym.SignatureFormat;

public class SignResponseMessage implements JsonEncodable {
	private String signedMessage;
	private SignatureFormat signatureFormat;
	private String signatureId;

	public SignResponseMessage(byte[] signedMessage, SignatureFormat signatureFormat, String signatureId,
			boolean base64) {
		super();

		if (base64) {
			this.signedMessage = ByteArrayConverter.toBase64(signedMessage).replace("\r\n", "");
		} else {
			this.signedMessage = new String(signedMessage);
		}

		this.signatureFormat = signatureFormat;
		this.signatureId = signatureId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode() {
		JSONObject root = new JSONObject();
		root.put("signed_message", this.signedMessage);
		root.put("signature_format", this.signatureFormat.display_name);
		root.put("signature_id", this.signatureId);
		return root;
	}

}
