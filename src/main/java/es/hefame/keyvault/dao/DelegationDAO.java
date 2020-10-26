package es.hefame.keyvault.dao;

import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.datastructure.model.Delegation;

public interface DelegationDAO {

	public List<Delegation> getPersonDelegations(String personId) throws HException;
	
	public List<Delegation> getKeypairDelegations(String keypairId) throws HException;

	public boolean insert(Delegation delegation) throws HException;

	public boolean update(Delegation delegation) throws HException;

	public boolean delete(Delegation delegation) throws HException;

}
