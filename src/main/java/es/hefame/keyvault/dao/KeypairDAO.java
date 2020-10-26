package es.hefame.keyvault.dao;

import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;

public interface KeypairDAO
{
	public Keypair get_by_id(String id) throws HException;

	public List<Keypair> get_list() throws HException;

	public List<Keypair> get_owned_by(Person owner) throws HException;

	public List<Keypair> get_owned_by_person_id(String owner) throws HException;

	public boolean insert(Keypair new_keypair) throws HException;

	public boolean update(Keypair modified_keypair) throws HException;

	public boolean delete(Keypair keypair) throws HException;

	public boolean delete(String keypair_id) throws HException;

}
