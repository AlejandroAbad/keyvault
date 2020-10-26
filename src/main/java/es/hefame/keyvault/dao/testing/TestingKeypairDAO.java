package es.hefame.keyvault.dao.testing;

import java.util.LinkedList;
import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;

public class TestingKeypairDAO implements KeypairDAO
{
	private static List<Keypair> secure_storage = new LinkedList<Keypair>();

	@Override
	public Keypair get_by_id(String id)
	{
		for (Keypair s : secure_storage)
		{
			if (s.getIdentifier().equals(id)) { return s; }
		}
		return null;
	}

	@Override
	public List<Keypair> get_list()
	{
		return secure_storage;
	}

	@Override
	public List<Keypair> get_owned_by_person_id(String owner_id)
	{
		List<Keypair> result = new LinkedList<Keypair>();

		if (owner_id != null)
		{
			for (Keypair s : secure_storage)
			{
				if (s.getOwnerId().equals(owner_id))
				{
					result.add(s);
				}
			}
		}

		return result;
	}

	@Override
	public List<Keypair> get_owned_by(Person owner) throws HException
	{
		if (owner != null) { return this.get_owned_by_person_id(owner.get_identifier()); }
		return new LinkedList<Keypair>();
	}

	@Override
	public boolean insert(Keypair new_keypair) throws HException
	{
		if (new_keypair == null) throw new HException("Cannot insert a null yey pair");

		if (this.get_by_id(new_keypair.getIdentifier()) == null)
		{
			secure_storage.add(new_keypair);
		}
		else
		{
			throw new HException("A key pair with ID [ " + new_keypair.getIdentifier() + " ] already exists in database");
		}
		return true;
	}

	@Override
	public boolean update(Keypair modified_keypair) throws HException
	{
		if (modified_keypair == null) throw new HException("Cannot update a null key pair");
		int pos = secure_storage.indexOf(modified_keypair);

		if (pos != -1)
		{
			secure_storage.remove(pos);
			secure_storage.add(modified_keypair);
		}
		else
		{
			throw new HException("Key pair with ID [ " + modified_keypair.getIdentifier() + " ] does not exists. No update viable");
		}
		return true;
	}

	@Override
	public boolean delete(Keypair keypair)
	{
		return secure_storage.remove(keypair);
	}

	@Override
	public boolean delete(String keypair_id)
	{
		Keypair keypair = this.get_by_id(keypair_id);
		return secure_storage.remove(keypair);
	}

}
