package es.hefame.keyvault.dao.mongodb;

import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.conversions.Bson;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.PersonDAO;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class MongoDbPersonDAO implements PersonDAO {

	/*
	 * { _id: "alejandro_ac" domain: "hefame.es" }
	 */
	@Override
	public Person getByFQDN(String fqdn) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("person");

		String personName = Domain.getPersonNameFromFQDN(fqdn);
		String domainId = Domain.getDomainIdFromFQDN(fqdn);

		Bson filter = and(eq("_id", personName), eq("domain", domainId));

		Document doc = col.find(filter).first();

		if (doc == null) {
			return null;
		}

		return new Person(doc.getString("_id"), doc.getString("domain"));
	}

	@Override
	public List<Person> getByDomain(Domain d) throws HException {
		if (d != null)
			return this.getByDomainId(d.getIdentifier());
		else
			return new LinkedList<>();
	}

	@Override
	public List<Person> getByDomainId(String d) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("person");
		List<Person> people = new LinkedList<>();
		col.find(eq("domain", d)).map(doc -> new Person(doc.getString("_id"), doc.getString("domain"))).into(people);
		return people;
	}

	@Override
	public List<Person> getList() throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("person");
		List<Person> people = new LinkedList<>();
		col.find().map(doc -> new Person(doc.getString("_id"), doc.getString("domain"))).into(people);
		return people;
	}

	@Override
	public boolean insert(Person person) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("person");

		Document doc = new Document();

		doc.put("_id", person.getName());
		doc.put("domain", person.getDomainId());

		try {
			col.insertOne(doc);
			return true;
		} catch (MongoException e) {
			throw new HException(e.getMessage(), e);
		}

	}

	@Override
	public boolean update(Person person) throws HException {
		// Person es inmutable
		return false;
	}

	@Override
	public boolean delete(Person person) throws HException {
		if (person == null)
			return false;
		return this.delete(person.getIdentifier());
	}

	@Override
	public boolean delete(String fqdn) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("person");
		String personName = Domain.getPersonNameFromFQDN(fqdn);
		String domainId = Domain.getDomainIdFromFQDN(fqdn);

		Bson filter = and(eq("_id", personName), eq("domain", domainId));
		try {
			col.deleteOne(filter);
			return true;
		} catch (MongoException e) {
			throw new HException(e.getMessage(), e);
		}
	}

}
