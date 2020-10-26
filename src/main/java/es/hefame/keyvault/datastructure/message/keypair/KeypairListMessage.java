package es.hefame.keyvault.datastructure.message.keypair;

import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;

import es.hefame.hcore.JsonEncodable;
import es.hefame.keyvault.datastructure.model.Keypair;

public class KeypairListMessage implements JsonEncodable
{

	private List<Keypair> keypairList = new LinkedList<>();

	public KeypairListMessage(List<Keypair> keypair_list)
	{
		this.keypairList = keypair_list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONArray jsonEncode()
	{
		JSONArray root = new JSONArray();
		for (Keypair p : keypairList)
		{

			// JSONObject domain_object = new JSONObject();
			// domain_object.put("id", p.get_identifier());
			// domain_object.put("subject", p.get_certificate().getSubjectDN().toString());
			// domain_object.put("uri", "https://cpd25.hefame.es/rest/keypair/" + C.strings.encodeUri(p.get_identifier()));
			root.add(p.jsonEncode());
		}
		return root;
	}

	public List<Keypair> getKeypairList() {
		return this.keypairList;
	}

}
