package es.hefame.keyvault.datastructure.message.person;

import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;

import es.hefame.hcore.JsonEncodable;
import es.hefame.keyvault.datastructure.model.Person;

public class ListPersonMessage implements JsonEncodable
{

	private List<Person> people = new LinkedList<>();

	public ListPersonMessage(List<Person> people)
	{
		this.people = people;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray jsonEncode()
	{
		JSONArray root = new JSONArray();
		for (Person d : people)
		{
			/*
			 * JSONObject domain_object = new JSONObject();
			 * domain_object.put("id", d.get_identifier());
			 * domain_object.put("uri", "https://cpd25.hefame.es/rest/person/" + Converter.encode_uri(d.get_identifier()));
			 * root.add(domain_object);
			 */
			root.add(d.jsonEncode());
		}
		return root;
	}

	public List<Person> getPeople() {
		return this.people;
	}

}
