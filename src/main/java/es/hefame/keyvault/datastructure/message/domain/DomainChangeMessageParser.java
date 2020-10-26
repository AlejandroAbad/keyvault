package es.hefame.keyvault.datastructure.message.domain;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class DomainChangeMessageParser {
	private Domain domain;

	public DomainChangeMessageParser(byte[] request) throws HttpException {
		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new String(request));
			JSONObject jsonObject = (JSONObject) obj;

			String domain_id = (String) jsonObject.get("id");
			String type = (String) jsonObject.get("type");
			JSONObject connection_data = (JSONObject) jsonObject.get("connection_data");

			if (domain_id == null) {
				throw new HttpException(400, "El campo 'id' es obligatorio");
			}
			if (type == null) {
				throw new HttpException(400, "El campo 'type' es obligatorio");
			}

			this.domain = Domain.createDomain(domain_id, type, connection_data);
		} catch (ParseException e) {
			throw new HttpException(400, "El JSON no esta bin formado", e);
		} catch (HException e) {
			throw new HttpException(400, "Los datos del dominio no son correctos", e);
		}
	}

	public DomainChangeMessageParser(String domain_id, byte[] request) throws HttpException {
		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new String(request));
			JSONObject jsonObject = (JSONObject) obj;

			String type = (String) jsonObject.get("type");
			JSONObject connection_data = (JSONObject) jsonObject.get("connection_data");

			if (type == null) {
				throw new HttpException(400, "El campo 'type' es obligatorio");
			}

			this.domain = Domain.createDomain(domain_id, type, connection_data);

		} catch (ParseException e) {
			throw new HttpException(400, "El JSON no esta bin formado", e);
		} catch (HException e) {
			throw new HttpException(400, "Los datos del dominio no son correctos", e);
		}
	}

	public Domain get_domain() {
		return this.domain;
	}
}
