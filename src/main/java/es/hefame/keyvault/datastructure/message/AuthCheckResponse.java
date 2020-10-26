package es.hefame.keyvault.datastructure.message;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import es.hefame.hcore.JsonEncodable;

public class AuthCheckResponse implements JsonEncodable {
	private String userId = null;
	private boolean authenticated = false;
	private String displayName = null;

	public AuthCheckResponse(String userId, boolean authenticated, String displayName) {
		this.userId = userId;
		this.authenticated = authenticated;
		this.displayName = displayName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONAware jsonEncode() {
		JSONObject root = new JSONObject();
		root.put("authenticated", authenticated);
		if (userId != null)
			root.put("user_id", userId);
		if (displayName != null)
			root.put("display_name", displayName);
		return root;

	}

}
