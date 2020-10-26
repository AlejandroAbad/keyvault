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
	private String domain_id;

	public Person(String fqdn) {
		this.name = Domain.getPersonNameFromFQDN(fqdn);
		this.domain_id = Domain.getDomainIdFromFQDN(fqdn);
	}

	public Person(String name, String domain) {
		this.name = name;
		this.domain_id = domain;
	}

	public String get_name() {
		return name;
	}

	public String get_identifier() {
		DomainDAO domain_datasource = DAO.domain();
		try {
			Domain person_domain = domain_datasource.get_by_id(domain_id);
			return person_domain.generateFQDN(this);
		} catch (HException e) {
			return this.name + '@' + this.domain_id;
		}

	}

	public String get_domain_id() {
		return domain_id;
	}

	public Domain get_domain() throws HException {
		return DAO.domain().get_by_id(domain_id);
	}

	public List<Keypair> get_owning_keypairs() throws HException {
		KeypairDAO keypair_datasource = DAO.keypair();
		return keypair_datasource.get_owned_by(this);
	}

	public void add_owning_keypair(Keypair keypair) throws HException {
		KeypairDAO keypair_datasource = DAO.keypair();
		keypair_datasource.insert(keypair);
	}

	public void delete_owning_keypair(Keypair keypair) throws HException {
		KeypairDAO keypair_datasource = DAO.keypair();
		keypair_datasource.delete(keypair);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append(" [");
		sb.append("\n\tName: ").append(this.get_name());
		sb.append("\n\tDomain ID: ").append(this.get_domain_id());
		sb.append("\n\tIdentifier: ").append(this.get_identifier());

		sb.append("\n\tOwning key pairs: {\n\t\t");
		try {
			for (Keypair owned_keypair : this.get_owning_keypairs()) {
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
		root.put("id", this.get_identifier());
		root.put("name", this.get_name());
		root.put("domain", this.get_domain_id());
		return root;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((domain_id == null) ? 0 : domain_id.hashCode());
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
		if (domain_id == null) {
			if (other.domain_id != null)
				return false;
		} else if (!domain_id.equals(other.domain_id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
