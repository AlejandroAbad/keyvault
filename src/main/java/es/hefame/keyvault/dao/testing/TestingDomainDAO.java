package es.hefame.keyvault.dao.testing;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.DomainDAO;
import es.hefame.keyvault.dao.PersonDAO;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;
import es.hefame.keyvault.datastructure.model.domain.LdapDomain;
import es.hefame.keyvault.datastructure.model.domain.LocalDomain;

public class TestingDomainDAO implements DomainDAO
{
	private static Logger		L			= LogManager.getLogger();
	private static List<Domain>	domain_list	= new LinkedList<Domain>();

	static
	{
		try
		{
			domain_list.add(new LocalDomain("LOCAL", "auth_local", "person_name", "password"));
			domain_list.add(new LdapDomain("hefame.es", "http://ad1.hefame.es", "dc=hefame,dc=es", "(&(objectclass=user)(|(sAMAccountName={%u})(uid={%u})))"));
		}
		catch (HException e)
		{
			L.catching(e);
		}
	}

	@Override
	public Domain get_by_id(String id)
	{
		for (Domain d : domain_list)
		{
			if (d.getIdentifier().equalsIgnoreCase(id)) { return d; }
		}
		return null;
	}

	@Override
	public List<Domain> get_list()
	{
		return domain_list;
	}

	@Override
	public boolean insert(Domain domain) throws HException
	{
		if (domain == null) throw new HException("Cannot insert a null domain");

		if (this.get_by_id(domain.getIdentifier()) == null)
		{
			return domain_list.add(domain);
		}
		else
		{
			throw new HException("A domain with ID [ " + domain.getIdentifier() + " ] already exists in database");
		}
	}

	@Override
	public boolean update(Domain domain) throws HException
	{
		if (domain == null) throw new HException("Cannot update a null domain");
		int pos = domain_list.indexOf(domain);
		if (pos != -1)
		{
			domain_list.remove(pos);
			return domain_list.add(domain);
		}
		else
		{
			throw new HException("A domain with ID [ " + domain.getIdentifier() + " ] does not exists. No update viable");
		}
	}

	@Override
	public boolean delete(Domain domain) throws HException
	{
		// This simulates a DELETE in cascade for example in a SQL db
		PersonDAO person_datasource = DAO.person();
		List<Person> people_in_domain = person_datasource.get_by_domain(domain);
		for (Person p : people_in_domain)
		{
			person_datasource.delete(p);
		}

		return domain_list.remove(domain);
	}

	@Override
	public boolean delete(String domain_id) throws HException
	{
		Domain d = this.get_by_id(domain_id);
		return this.delete(d);
	}

}
