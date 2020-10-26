package es.hefame.keyvault.datastructure.message.domain;

import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;

import es.hefame.hcore.JsonEncodable;
import es.hefame.keyvault.datastructure.model.domain.Domain;

public class ListDomainMessage implements JsonEncodable {

	public List<Domain> domain_list = new LinkedList<Domain>();

	public ListDomainMessage(List<Domain> domain_list) {
		this.domain_list = domain_list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray jsonEncode() {
		JSONArray root = new JSONArray();
		for (Domain d : domain_list) {
			/*
			 * JSONObject domain_object = new JSONObject(); domain_object.put("id",
			 * d.get_identifier()); domain_object.put("type", d.get_domain_type());
			 * domain_object.put("uri", "https://cpd25.hefame.es/rest/domain/" +
			 * Converter.encode_uri(d.get_identifier())); root.add(domain_object);
			 */
			root.add(d.jsonEncode());
		}
		return root;
	}

}
