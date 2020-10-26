package es.hefame.keyvault.datastructure.message;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;

public class InfoMessage implements JsonEncodable {
	private String message = null;
	private Integer code = null;

	public InfoMessage(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public InfoMessage(String message) {
		this.message = message;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode() {
		JSONObject root = new JSONObject();
		if (code != null) {
			root.put("code", code.intValue());
		}
		root.put("message", message);
		return root;

	}

}
