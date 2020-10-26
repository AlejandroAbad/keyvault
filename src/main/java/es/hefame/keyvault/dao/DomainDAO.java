package es.hefame.keyvault.dao;

import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public interface DomainDAO
{
	public Domain getById(String id) throws HException;

	public List<Domain> getList() throws HException;

	public boolean insert(Domain domain) throws HException;

	public boolean update(Domain domain) throws HException;

	public boolean delete(Domain domain) throws HException;

	public boolean delete(String domain) throws HException;

}
