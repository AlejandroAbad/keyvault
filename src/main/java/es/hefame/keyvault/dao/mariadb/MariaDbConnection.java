package es.hefame.keyvault.dao.mariadb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MariaDbConnection
{
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
			connection = DriverManager.getConnection("jdbc:mariadb://db.hefame.es:3306/db", "user", "123123");
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
