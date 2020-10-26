package es.hefame.keyvault.datastructure.message;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;

public class AuthCheckResponse implements JsonEncodable {
	private String user_id = null;
	private boolean authenticated = false;
	private String display_name = null;

	public AuthCheckResponse(String user_id, boolean authenticated, String display_name) {
		this.user_id = user_id;
		this.authenticated = authenticated;
		this.display_name = display_name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode() {
		JSONObject root = new JSONObject();
		root.put("authenticated", authenticated);
		if (user_id != null)
			root.put("user_id", user_id);
		if (display_name != null)
			root.put("display_name", display_name);
		return root;

	}

}
