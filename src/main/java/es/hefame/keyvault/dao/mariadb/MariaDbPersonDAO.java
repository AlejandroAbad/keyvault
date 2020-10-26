package es.hefame.keyvault.dao.mariadb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.PersonDAO;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class MariaDbPersonDAO implements PersonDAO
{
	//private static Logger L = LogManager.getLogger();

	@Override
	public Person getByFQDN(String fqdn) throws HException
	{
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			conn = MariaDbConnection.getConnection();

			String selectSQL = "SELECT name, domain_id FROM person WHERE name = ? AND domain_id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, Domain.getPersonNameFromFQDN(fqdn));
			st.setString(2, Domain.getDomainIdFromFQDN(fqdn));
			rs = st.executeQuery();

			if (rs.next())
			{
				String person_name = rs.getString("name");
				String domain_id = rs.getString("domain_id");
				return new Person(person_name, domain_id);
			}

		}
		catch (SQLException e)
		{
			throw new HException("Error during database operation", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, rs, conn);
		}

		return null;
	}

	@Override
	public List<Person> getByDomain(Domain d) throws HException
	{
		if (d != null) return this.getByDomainId(d.getIdentifier());
		else return new LinkedList<Person>();
	}

	@Override
	public List<Person> getByDomainId(String d) throws HException
	{
		List<Person> people = new LinkedList<Person>();
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			conn = MariaDbConnection.getConnection();

			String selectSQL = "SELECT name, domain_id FROM person WHERE domain_id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, d);
			rs = st.executeQuery();

			while (rs.next())
			{
				String person_name = rs.getString("name");
				String domain_id = rs.getString("domain_id");

				people.add(new Person(person_name, domain_id));
			}
		}
		catch (SQLException e)
		{
			throw new HException("Error during database operation", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, rs, conn);
		}

		return people;
	}

	@Override
	public List<Person> getList() throws HException
	{
		List<Person> people = new LinkedList<Person>();
		Connection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		try
		{
			conn = MariaDbConnection.getConnection();

			String selectSQL = "SELECT name, domain_id FROM person";
			st = conn.prepareStatement(selectSQL);
			rs = st.executeQuery();

			while (rs.next())
			{
				String person_name = rs.getString("name");
				String domain_id = rs.getString("domain_id");
				people.add(new Person(person_name, domain_id));
			}
		}
		catch (SQLException e)
		{
			throw new HException("Error during database operation", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, rs, conn);
		}

		return people;
	}

	@Override
	public boolean insert(Person person) throws HException
	{
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "INSERT INTO person (name, domain_id) VALUES (?, ?)";
			st = conn.prepareStatement(selectSQL);

			st.setString(1, person.getName());
			st.setString(2, person.getDomainId());
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
			throw new HException("Error during database operation", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}

	@Override
	public boolean update(Person person) throws HException
	{
		// Person is inmutable at the moment
		return false;
	}

	@Override
	public boolean delete(Person person) throws HException
	{
		if (person == null) return false;
		return this.delete(person.getIdentifier());
	}

	@Override
	public boolean delete(String person_id) throws HException
	{
		Connection conn = null;
		PreparedStatement st = null;
		try
		{
			conn = MariaDbConnection.getConnection();
			String selectSQL = "DELETE FROM person WHERE name = ? AND domain_id = ?";
			st = conn.prepareStatement(selectSQL);
			st.setString(1, Domain.getPersonNameFromFQDN(person_id));
			st.setString(2, Domain.getDomainIdFromFQDN(person_id));
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
			throw new HException("Error during database operation", e);
		}
		finally
		{
			MariaDbConnection.clearResources(st, conn);
		}
	}

}
