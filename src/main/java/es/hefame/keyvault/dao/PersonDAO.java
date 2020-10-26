package es.hefame.keyvault.dao;

import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;

;

public interface PersonDAO
{
	public Person getByFQDN(String fqdn) throws HException;

	public List<Person> getByDomain(Domain d) throws HException;

	public List<Person> getByDomainId(String d) throws HException;

	public List<Person> getList() throws HException;

	public boolean insert(Person person) throws HException;

	public boolean update(Person person) throws HException;

	public boolean delete(Person person) throws HException;

	public boolean delete(String personId) throws HException;
}
