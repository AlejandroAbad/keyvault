package es.hefame.keyvault.datastructure.message.person;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.model.Person;

public class PersonChangeMessageParser {
	private Person person;

	public PersonChangeMessageParser(byte[] request) throws HttpException {
		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new String(request));
			JSONObject jsonObject = (JSONObject) obj;

			String name = (String) jsonObject.get("name");
			String domain_id = (String) jsonObject.get("domain");

			if (name == null) {
				throw new HttpException(400, "'name' field is mandatory");
			}
			if (domain_id == null) {
				throw new HttpException(400, "'domain' field is mandatory");
			}

			this.person = new Person(name, domain_id);
		} catch (ParseException e) {
			throw new HttpException(400, "JSON is bad formed", e);
		}
	}

	public Person get_person() {
		return this.person;
	}
}
