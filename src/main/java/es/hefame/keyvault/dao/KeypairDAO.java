package es.hefame.keyvault.dao;

import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;

public interface KeypairDAO
{
	public Keypair getById(String id) throws HException;

	public List<Keypair> getList() throws HException;

	public List<Keypair> getOwnedBy(Person owner) throws HException;

	public List<Keypair> getOwnedByPersonId(String owner) throws HException;

	public boolean insert(Keypair newKeypair) throws HException;

	public boolean update(Keypair modifiedKeypair) throws HException;

	public boolean delete(Keypair keypair) throws HException;

	public boolean delete(String keypairId) throws HException;

}
