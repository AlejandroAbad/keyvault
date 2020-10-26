package es.hefame.keyvault.dao;

import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;

;

public interface PersonDAO
{
	public Person get_by_fqdn(String fqdn) throws HException;

	public List<Person> get_by_domain(Domain d) throws HException;

	public List<Person> get_by_domain_id(String d) throws HException;

	public List<Person> get_list() throws HException;

	public boolean insert(Person person) throws HException;

	public boolean update(Person person) throws HException;

	public boolean delete(Person person) throws HException;

	public boolean delete(String person_id) throws HException;
}
