package es.hefame.keyvault.datastructure.model.domain;

import org.json.simple.JSONObject;

import es.hefame.hcore.http.HttpException;
import es.hefame.hcore.http.authentication.Authenticator;
import es.hefame.hcore.http.exchange.IHttpRequest;
import es.hefame.keyvault.datastructure.model.Person;

public class SapDomain extends Domain {

	private String systemName;
	private String apikey;

	public SapDomain(String identifier, String systemName, String apikey) {
		super(identifier);
		this.systemName = systemName;
		this.apikey = apikey;

		this.setConnectionData(jsonEncodeConnectionData());
	}

	public SapDomain(String id, JSONObject connectionData) throws HttpException {
		super(id);
		if (connectionData == null) {
			throw new HttpException(400, "No se especifican datos de conexion al dominio");
		}

		this.setConnectionData(connectionData);

		this.systemName = (String) connectionData.get("system_name");
		this.apikey = (String) connectionData.get("apikey");
	}

	@Override
	public String getDomainType() {
		return "sap";
	}

	public String getSystemName() {
		return systemName;
	}

	public String getApiKey() {
		return apikey;
	}

	@Override
	public String generateFQDN(Person signer) {
		StringBuilder sb = new StringBuilder();
		sb.append(signer.getName().toLowerCase()).append('@').append(this.getIdentifier());
		return sb.toString();
	}

	@Override
	public boolean authenticate(String signer, String password, IHttpRequest t) throws HttpException {
		t.setInternalValue(DOMAIN_ID, this.getIdentifier());
		t.setInternalValue(DOMAIN_TYPE, this.getDomainType());

		if (this.getApiKey().equals(password)) {
			return true;
		}

		HttpException exception = new HttpException(401,
				"No se autoriza al usuario porque no dispone de una clave válida.");
		Authenticator.addAuthHeader("Content-Type", "application/json", t);
		Authenticator.setAuthResponse(exception, t);

		throw exception;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncodeConnectionData() {
		JSONObject root = new JSONObject();
		root.put("system_name", this.getSystemName());
		root.put("apikey", this.getApiKey());
		return root;
	}

}
