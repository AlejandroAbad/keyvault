package es.hefame.keyvault.datastructure.model.domain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.hefame.hcore.HException;
import es.hefame.hcore.JsonEncodable;

import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.hcore.http.exchange.IHttpRequest;

public abstract class Domain implements JsonEncodable {
	private static Logger logger = LogManager.getLogger();

	public static final String DOMAIN_ID = "auth_domain_id";
	public static final String DOMAIN_TYPE = "auth_domain_type";
	public static final String USER_ID = "user_id";
	public static final String USER_NAME = "user_name";
	public static final String USER_GROUPS = "user_groups";

	private String identifier;
	JSONObject connectionData;

	protected Domain(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public abstract String getDomainType();

	protected void setConnectionData(JSONObject connectionDataJSON) {
		this.connectionData = connectionDataJSON;
	}

	public String getConnectionData() {
		return this.connectionData.toJSONString();
	}

	public String generateFQDN(Person signer) {
		StringBuilder sb = new StringBuilder();
		sb.append(signer.getName()).append('@').append(this.getIdentifier());
		return sb.toString();
	}

	public abstract boolean authenticate(String username, String password, IHttpRequest request) throws HException;

	public String toString() {
		return this.jsonEncode().toJSONString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Domain other = (Domain) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equalsIgnoreCase(other.identifier))
			return false;
		return true;
	}

	public static Domain createDomain(String id, String type, String connectionData) throws HException {
		logger.trace("Extrayendo datos de conexion del dominio desde el string");
		try {
			JSONParser parser = new JSONParser();
			Object o = parser.parse(connectionData);
			JSONObject jsonObject = (JSONObject) o;
			return Domain.createDomain(id, type, jsonObject);
		} catch (ParseException e) {
			logger.catching(e);
			throw new HException("El formato de los datos de conexion del dominio no cumplen el standard JSON", e);
		}
	}

	public static Domain createDomain(String id, String type, JSONObject connectionData) throws HException {
		switch (type.toLowerCase()) {
			case "local":
				return new LocalDomain(id, connectionData);
			case "ldap":
				return new LdapDomain(id, connectionData);
			case "sap":
				return new SapDomain(id, connectionData);
			default:
				throw new HException("El tipo de dominio no existe");
		}
	}

	public static String getDomainIdFromFQDN(String fqdn) {
		if (fqdn == null) {
			return null;
		}
		String[] chunks = fqdn.split("@");
		if (chunks.length > 1)
			return chunks[1];
		return null;
	}

	public static String getPersonNameFromFQDN(String fqdn) {
		if (fqdn == null) {
			return null;
		}
		String[] chunks = fqdn.split("@");
		if (chunks.length > 0)
			return chunks[0];
		return null;
	}

	public static String generateFQDN(String personName, String domainId) {
		StringBuilder sb = new StringBuilder();
		sb.append(personName).append('@').append(domainId);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final JSONObject jsonEncode() {
		JSONObject root = new JSONObject();
		root.put("id", this.getIdentifier());
		root.put("type", this.getDomainType());
		root.put("connection_data", this.jsonEncodeConnectionData());
		return root;
	}

	public abstract JSONObject jsonEncodeConnectionData();

}
