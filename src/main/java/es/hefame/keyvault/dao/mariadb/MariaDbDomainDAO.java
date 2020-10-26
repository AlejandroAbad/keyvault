package es.hefame.keyvault.dao.mariadb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.DomainDAO;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class MariaDbDomainDAO implements DomainDAO
{

	@Override
	public Domain getById(String id) throws HException
	{
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "SELECT id, domain_type, connection_data FROM domain WHERE id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, id);
			rs = st.executeQuery();

			if (rs.next())
			{
				String domain_id = rs.getString("id");
				String domain_type = rs.getString("domain_type");
				String domain_connection_data = rs.getString("connection_data");
				return Domain.createDomain(domain_id, domain_type, domain_connection_data);
			}

		}
		catch (SQLException e)
		{
			throw new HException("Error al consultar la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, rs, conn);
		}
		return null;
	}

	@Override
	public List<Domain> getList() throws HException
	{
		List<Domain> domain_list = new LinkedList<Domain>();
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "SELECT id, domain_type, connection_data FROM domain";
			st = conn.prepareStatement(selectSQL);
			rs = st.executeQuery();

			while (rs.next())
			{
				String domain_id = rs.getString("id");
				String domain_type = rs.getString("domain_type");
				String domain_connection_data = rs.getString("connection_data");
				try
				{
					domain_list.add(Domain.createDomain(domain_id, domain_type, domain_connection_data));
				}
				catch (HException e)
				{

				}
			}
		}
		catch (SQLException e)
		{
			throw new HException("Error al consultar la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, rs, conn);
		}

		return domain_list;
	}

	@Override
	public boolean insert(Domain domain) throws HException
	{
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "INSERT INTO domain (id, connection_data, domain_type) VALUES (?, ?, ?)";
			st = conn.prepareStatement(selectSQL);

			st.setString(1, domain.getIdentifier());
			st.setString(2, domain.getConnectionData());
			st.setString(3, domain.getDomainType());
			int result = st.executeUpdate();

			if (result == 1)
			{
				conn.commit();
				return true;
			}
			else
			{
				conn.rollback();
				return false;
			}
		}
		catch (SQLException e)
		{
			MariaDbConnection.rollback(conn);
			throw new HException("Error al consultar la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean update(Domain domain) throws HException
	{
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "UPDATE domain SET connection_data = ?, domain_type = ? WHERE id = ?";
			st = conn.prepareStatement(selectSQL);

			st.setString(1, domain.getConnectionData());
			st.setString(2, domain.getDomainType());
			st.setString(3, domain.getIdentifier());
			int result = st.executeUpdate();

			if (result == 1)
			{
				conn.commit();
				return true;
			}
			else
			{
				conn.rollback();
				return false;
			}
		}
		catch (SQLException e)
		{
			MariaDbConnection.rollback(conn);
			throw new HException("Error al consultar la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean delete(String domain_id) throws HException
	{
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();

			String selectSQL = "DELETE FROM domain WHERE id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, domain_id);

			int result = st.executeUpdate();
			if (result == 1)
			{
				conn.commit();
				return true;
			}
			else
			{
				conn.rollback();
				return false;
			}
		}
		catch (SQLException e)
		{
			MariaDbConnection.rollback(conn);
			throw new HException("Error al consultar la base de datos", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean delete(Domain domain) throws HException
	{
		if (domain == null) return false;
		return this.delete(domain.getIdentifier());

	}

}
