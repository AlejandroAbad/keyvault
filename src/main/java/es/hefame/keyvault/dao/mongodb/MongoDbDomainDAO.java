package es.hefame.keyvault.dao.mongodb;

import java.util.LinkedList;
import java.util.List;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import static com.mongodb.client.model.Filters.*;

import es.hefame.hcore.HException;
import es.hefame.keyvault.dao.DomainDAO;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class MongoDbDomainDAO implements DomainDAO {

	@Override
	public Domain getById(String id) throws HException {
		MongoCollection<Document> col = MongoDbConnection.getCollection("domain");

		Document doc = col.find(eq("_id", id)).first();

		if (doc == null) {
			return null;
		}

		String domainId = doc.getString("_id");
		String domainType = doc.getString("type");
		String domainConnectionData = doc.getString("connectionData");
		return Domain.createDomain(domainId, domainType, domainConnectionData);
	}

	@Override
	public List<Domain> getList() throws HException {

		MongoCollection<Document> col = MongoDbConnection.getCollection("domain");
		List<Domain> domainList = new LinkedList<>();

		col.find().map(doc -> {
			String domainId = doc.getString("_id");
			String domainType = doc.getString("type");
			String domainConnectionData = doc.getString("connectionData");
			try {
				return Domain.createDomain(domainId, domainType, domainConnectionData);
			} catch (HException e) {
				return null;
			}
		}).into(domainList);

		return domainList;
	}


	@Override
	public boolean insert(Domain domain) throws HException {

		MongoCollection<Document> col = MongoDbConnection.getCollection("domain");

		Document doc = new Document();
		doc.put("_id", domain.getIdentifier());
		doc.put("type", domain.getDomainType());
		doc.put("connectionData", domain.getConnectionData());

		try {
			col.insertOne(doc);
			return true;
		} catch (MongoException e) {
			throw new HException(e.getMessage(), e);
		}

	}

	@Override
	public boolean update(Domain domain) throws HException {

		MongoCollection<Document> col = MongoDbConnection.getCollection("domain");

		Document doc = new Document();
		doc.put("type", domain.getDomainType());
		doc.put("connectionData", domain.getConnectionData());

		try {
			col.updateOne(eq("_id", domain.getIdentifier()), doc);
			return true;
		} catch (MongoException e) {
			throw new HException(e.getMessage(), e);
		}

	}

	@Override
	public boolean delete(String domainId) throws HException {
		
		MongoCollection<Document> col = MongoDbConnection.getCollection("domain");
		try {
			col.deleteOne(eq("_id", domainId));
			return true;
		} catch (MongoException e) {
			throw new HException(e.getMessage(), e);
		}
	}

	@Override
	public boolean delete(Domain domain) throws HException {
		if (domain == null)
			return false;
		return this.delete(domain.getIdentifier());

	}

}
