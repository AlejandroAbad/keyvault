package es.hefame.keyvault.dao.mariadb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.keyvault.config.Conf;

public class MariaDbConnection
{

	private static Logger logger = LogManager.getLogger();
	
	private static final String DB_CONNECTION_URI = Conf.getString("dao.provider.MariaDb.uri");
	private static final String DB_CONNECTION_USER = Conf.getString("dao.provider.MariaDb.user");
	private static final String DB_CONNECTION_PASS = Conf.getString("dao.provider.MariaDb.pass");

	private MariaDbConnection () {

	}

	private static Connection connection = null;

	public static Connection getConnection() throws SQLException
	{
		if (connection == null || connection.isClosed())
		{
			try
			{
				Class.forName("org.mariadb.jdbc.Driver");
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}

			logger.info("Conectando a MariaDB {} con el usuario {}", DB_CONNECTION_URI, DB_CONNECTION_USER);
			connection = DriverManager.getConnection(DB_CONNECTION_URI, DB_CONNECTION_USER, DB_CONNECTION_PASS);
			connection.setAutoCommit(false);
		}
		return connection;
	}

	public static void clearResources(AutoCloseable... res)
	{
		for (AutoCloseable r : res)
		{
			if (r != null)
			{
				try
				{
					r.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void rollback(Connection conn)
	{
		try
		{
			if (conn != null)
			{
				conn.rollback();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

}
