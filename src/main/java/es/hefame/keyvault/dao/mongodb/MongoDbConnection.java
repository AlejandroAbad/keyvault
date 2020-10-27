package es.hefame.keyvault.dao.mongodb;

import java.io.Closeable;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import es.hefame.keyvault.config.Conf;

public class MongoDbConnection {

	private static Logger logger = LogManager.getLogger();

	private static final String DB_CONNECTION_URI = Conf.getString("dao.provider.MongoDb.uri");
	private static final String DB_CONNECTION_DB = Conf.getString("dao.provider.MongoDb.db");

	private MongoDbConnection() {

	}


	private static MongoClient mongoClient;
	private static MongoDatabase database;

	private static MongoDatabase getConnection() {
		if (database == null) {
			clearResources(mongoClient);
			mongoClient = new MongoClient(new MongoClientURI(DB_CONNECTION_URI));
			database = mongoClient.getDatabase(DB_CONNECTION_DB);
			logger.info("Establecida conexión con la base de datos {}", DB_CONNECTION_DB);
		}

		return database;
	}

	public static MongoCollection<Document> getCollection(String collectionName) {
		return getConnection().getCollection(collectionName);
	}

	public static <T> MongoCollection<T> getCollection(String collectionName, Class<T> className) {
		return getConnection().getCollection(collectionName, className);
	}

	public static void clearResources(Closeable... res) {
		for (Closeable r : res) {
			if (r != null) {
				try {
					r.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
