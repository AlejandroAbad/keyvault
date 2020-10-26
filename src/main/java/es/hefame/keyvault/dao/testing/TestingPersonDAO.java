package es.hefame.keyvault.dao.testing;

import java.util.LinkedList;
import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.dao.PersonDAO;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class TestingPersonDAO implements PersonDAO
{
	private static List<Person> people = new LinkedList<Person>();

	static
	{
		people.add(new Person("Alejandro_AC1", "hefame.es"));
		people.add(new Person("Alejandro_AC2", "hefame.es"));
		people.add(new Person("Alejandro_AC3", "hefame.es"));
		people.add(new Person("root", "LOCAL"));
		people.add(new Person("Administrador", "LOCAL"));

	}

	@Override
	public Person get_by_fqdn(String fqdn) throws HException
	{
		for (Person s : people)
		{
			if (s.get_identifier().equals(fqdn)) { return s; }
		}
		return null;
	}

	@Override
	public List<Person> get_by_domain(Domain d)
	{
		if (d != null) { return this.get_by_domain_id(d.getIdentifier()); }
		return new LinkedList<Person>();
	}

	public List<Person> get_by_domain_id(String domain_id)
	{
		List<Person> result = new LinkedList<Person>();

		if (domain_id != null)
		{
			for (Person s : people)
			{
				if (s.get_domain_id().equals(domain_id))
				{
					result.add(s);
				}
			}
		}
		return result;
	}

	@Override
	public List<Person> get_list()
	{
		return people;
	}

	@Override
	public boolean insert(Person person) throws HException
	{
		if (person == null) throw new HException("Cannot insert a null person");

		if (this.get_by_fqdn(person.get_identifier()) == null)
		{
			return people.add(person);
		}
		else
		{
			throw new HException("Someone with FQDN [ " + person.get_identifier() + " ] already exists in database");
		}
	}

	@Override
	public boolean update(Person person) throws HException
	{
		if (person == null) throw new HException("Cannot update a null person");
		int pos = people.indexOf(person);
		if (pos != -1)
		{
			people.remove(pos);
			return people.add(person);
		}
		else
		{
			throw new HException("Person with ID [ " + person.get_identifier() + " ] does not exists. No update viable");
		}

	}

	@Override
	public boolean delete(Person person) throws HException
	{
		KeypairDAO keypair_datasource;
		keypair_datasource = DAO.keypair();

		List<Keypair> owned_keypair = keypair_datasource.get_owned_by(person);
		for (Keypair p : owned_keypair)
		{
			keypair_datasource.delete(p);
		}

		return people.remove(person);
	}

	public boolean delete(String person_id) throws HException
	{
		Person p = this.get_by_fqdn(person_id);
		return this.delete(p);
	}

}
