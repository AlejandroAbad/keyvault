package es.hefame.keyvault.datastructure.model;

import java.util.List;

import org.json.simple.JSONObject;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.DomainDAO;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class Person implements es.hefame.hcore.JsonEncodable {
	private String name;
	private String domainId;

	public Person(String fqdn) {
		this.name = Domain.getPersonNameFromFQDN(fqdn);
		this.domainId = Domain.getDomainIdFromFQDN(fqdn);
	}

	public Person(String name, String domain) {
		this.name = name;
		this.domainId = domain;
	}

	public String getName() {
		return name;
	}

	public String getIdentifier() {
		DomainDAO domainDAO = DAO.domain();
		try {
			Domain personDomain = domainDAO.getById(domainId);
			return personDomain.generateFQDN(this);
		} catch (HException e) {
			return this.name + '@' + this.domainId;
		}

	}

	public String getDomainId() {
		return domainId;
	}

	public Domain getDomain() throws HException {
		return DAO.domain().getById(domainId);
	}

	public List<Keypair> getOwningKeypairs() throws HException {
		KeypairDAO keypairDAO = DAO.keypair();
		return keypairDAO.getOwnedBy(this);
	}

	public void addOwningKeypair(Keypair keypair) throws HException {
		KeypairDAO keypairDAO = DAO.keypair();
		keypairDAO.insert(keypair);
	}

	public void deleteOwningKeypair(Keypair keypair) throws HException {
		KeypairDAO keypairDAO = DAO.keypair();
		keypairDAO.delete(keypair);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append(" [");
		sb.append("\n\tName: ").append(this.getName());
		sb.append("\n\tDomain ID: ").append(this.getDomainId());
		sb.append("\n\tIdentifier: ").append(this.getIdentifier());

		sb.append("\n\tOwning key pairs: {\n\t\t");
		try {
			for (Keypair owned_keypair : this.getOwningKeypairs()) {
				sb.append(owned_keypair.toString().replace("\n", "\n\t\t"));
			}
		} catch (HException e) {
			sb.append("\n\t\t").append("*EXCEPTION* ").append(e.getMessage());
		}
		sb.append("\n\t}");
		sb.append("\n]");
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject jsonEncode() {
		JSONObject root = new JSONObject();
		root.put("id", this.getIdentifier());
		root.put("name", this.getName());
		root.put("domain", this.getDomainId());
		return root;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domainId == null) ? 0 : domainId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (domainId == null) {
			if (other.domainId != null)
				return false;
		} else if (!domainId.equals(other.domainId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
