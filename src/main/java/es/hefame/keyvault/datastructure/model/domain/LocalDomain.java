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

	private String authTableName;
	private String userColName;
	private String passColName;

	private String authQuery;

	public LocalDomain(String identifier, JSONObject connectionData) throws HttpException {
		super(identifier);
		if (connectionData == null) {
			throw new HttpException(400, "No se especifican datos de conexion al dominio");
		}

		this.setConnectionData(connectionData);

		this.authTableName = (String) connectionData.get("auth_table_name");
		this.userColName = (String) connectionData.get("user_col_name");
		this.passColName = (String) connectionData.get("pass_col_name");

		if (authTableName == null) {
			throw new HttpException(400, "El campo 'auth_table_name' debe especificarse");
		}
		if (userColName == null) {
			throw new HttpException(400, "El campo 'user_col_name' debe especificarse");
		}
		if (passColName == null) {
			throw new HttpException(400, "El campo 'pass_col_name' debe especificarse");
		}

		this.authQuery = "SELECT " + this.passColName + " FROM " + this.authTableName + " WHERE "
				+ this.userColName + " = ?";
		this.setConnectionData(jsonEncodeConnectionData());
	}

	public LocalDomain(String identifier, String authTableName, String userColName, String passColName) {
		super(identifier);

		this.authTableName = authTableName;
		this.userColName = userColName;
		this.passColName = passColName;

		this.authQuery = "SELECT " + this.passColName + " FROM " + this.authTableName + " WHERE "
				+ this.userColName + " = ?";
		this.setConnectionData(jsonEncodeConnectionData());
	}

	@Override
	public String getDomainType() {
		return "local";
	}

	@Override
	public boolean authenticate(String userFQDN, String password, IHttpRequest t) throws HException {

		t.setInternalValue(DOMAIN_ID, this.getIdentifier());
		t.setInternalValue(DOMAIN_TYPE, this.getDomainType());

		String personName = Domain.getPersonNameFromFQDN(userFQDN);
		t.setInternalValue(USER_NAME, personName);
		t.setInternalValue(USER_ID, userFQDN);

		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			conn = MariaDbConnection.getConnection();

			L.trace("Ejecutando [{}] para el usuario [{}]", authQuery, personName);

			t.setInternalValue(USER_NAME, personName);
			t.setInternalValue(USER_ID, userFQDN);

			st = conn.prepareStatement(this.authQuery);
			st.setString(1, personName);
			rs = st.executeQuery();

			if (rs.next()) {
				String storedPasswd = rs.getString(this.passColName);
				return password.equals(storedPasswd);
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
		root.put("auth_table_name", this.authTableName);
		root.put("user_col_name", this.userColName);
		root.put("pass_col_name", this.passColName);
		return root;
	}

}
