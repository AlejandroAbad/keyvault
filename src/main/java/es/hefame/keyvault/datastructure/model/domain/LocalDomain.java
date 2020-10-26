package es.hefame.keyvault.datastructure.model.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.hcore.http.exchange.IHttpRequest;
import es.hefame.keyvault.dao.mariadb.MariaDbConnection;

public class LocalDomain extends Domain {
	private static Logger L = LogManager.getLogger();

	private String auth_table_name;
	private String user_col_name;
	private String pass_col_name;

	private String auth_query;

	public LocalDomain(String identifier, JSONObject connection_data) throws HttpException {
		super(identifier);
		if (connection_data == null) {
			throw new HttpException(400, "No se especifican datos de conexion al dominio");
		}

		this.setConnectionData(connection_data);

		this.auth_table_name = (String) connection_data.get("auth_table_name");
		this.user_col_name = (String) connection_data.get("user_col_name");
		this.pass_col_name = (String) connection_data.get("pass_col_name");

		if (auth_table_name == null) {
			throw new HttpException(400, "El campo 'auth_table_name' debe especificarse");
		}
		if (user_col_name == null) {
			throw new HttpException(400, "El campo 'user_col_name' debe especificarse");
		}
		if (pass_col_name == null) {
			throw new HttpException(400, "El campo 'pass_col_name' debe especificarse");
		}

		this.auth_query = "SELECT " + this.pass_col_name + " FROM " + this.auth_table_name + " WHERE "
				+ this.user_col_name + " = ?";
		this.setConnectionData(jsonEncodeConnectionData());
	}

	public LocalDomain(String identifier, String auth_table_name, String user_col_name, String pass_col_name) {
		super(identifier);

		this.auth_table_name = auth_table_name;
		this.user_col_name = user_col_name;
		this.pass_col_name = pass_col_name;

		this.auth_query = "SELECT " + this.pass_col_name + " FROM " + this.auth_table_name + " WHERE "
				+ this.user_col_name + " = ?";
		this.setConnectionData(jsonEncodeConnectionData());
	}

	@Override
	public String getDomainType() {
		return "local";
	}

	@Override
	public boolean authenticate(String user_fqdn, String password, IHttpRequest t) throws HException {

		t.setInternalValue(DOMAIN_ID, this.getIdentifier());
		t.setInternalValue(DOMAIN_TYPE, this.getDomainType());

		String person_name = Domain.getPersonNameFromFQDN(user_fqdn);
		t.setInternalValue(USER_NAME, person_name);
		t.setInternalValue(USER_ID, user_fqdn);

		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			conn = MariaDbConnection.getConnection();

			L.trace("Ejecutando [{}] para el usuario [{}]", auth_query, person_name);

			t.setInternalValue(USER_NAME, person_name);
			t.setInternalValue(USER_ID, user_fqdn);

			st = conn.prepareStatement(this.auth_query);
			st.setString(1, person_name);
			rs = st.executeQuery();

			if (rs.next()) {
				String stored_passwd = rs.getString(this.pass_col_name);
				return password.equals(stored_passwd);
			} else {
				return false;
			}

		} catch (SQLException e) {
			throw new HException("Error durante la operacion con la base de datos", e);
		} finally {
			MariaDbConnection.clearResources(st, rs, conn);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncodeConnectionData() {
		JSONObject root = new JSONObject();
		root.put("auth_table_name", this.auth_table_name);
		root.put("user_col_name", this.user_col_name);
		root.put("pass_col_name", this.pass_col_name);
		return root;
	}

}
