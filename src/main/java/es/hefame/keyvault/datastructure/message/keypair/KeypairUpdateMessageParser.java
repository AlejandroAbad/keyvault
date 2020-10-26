package es.hefame.keyvault.datastructure.message.keypair;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import es.hefame.hcore.HException;
import es.hefame.hcore.converter.ByteArrayConverter;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.datastructure.message.acronym.KeypairFormat;
import es.hefame.keyvault.datastructure.model.Keypair;

public class KeypairUpdateMessageParser {
	private Keypair original_keypair;
	private Keypair updated_keypair;

	public KeypairUpdateMessageParser(byte[] request) throws HException {
		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new String(request));
			JSONObject jsonObject = (JSONObject) obj;

			String id = (String) jsonObject.get("id");
			if (id == null) {
				throw new HttpException(400, "'id' field is mandatory");
			}

			this.original_keypair = DAO.keypair().get_by_id(id);

			if (original_keypair == null) {
				throw new HttpException(404, "Key pair not found");
			}

			updated_keypair = original_keypair.clone();

			String owner = (String) jsonObject.get("owner");
			if (owner != null) {
				updated_keypair.setOwner(owner);
			}

			// May the request update the keypair?
			String keypair_format = (String) jsonObject.get("keypair_format");
			if (keypair_format != null) {
				KeypairFormat format = KeypairFormat.build(keypair_format);
				switch (format) {
					case PKCS12:
						String payload = (String) jsonObject.get("payload");
						String passphrase = (String) jsonObject.get("passphrase");
						if (payload == null) {
							throw new HttpException(400, "'payload' field is mandatory if format is " + format);
						}

						if (passphrase == null) {
							passphrase = "";
						}

						byte[] pkcs12_data = ByteArrayConverter.fromBase64(payload);
						this.updated_keypair.setKeypairFromPKCS12(pkcs12_data, passphrase.toCharArray());
						break;
					default:
						throw new HttpException(400, "Format value [ " + format + " ] is invalid");
				}
			}

		} catch (Exception e) {
			throw new HttpException(400, e.getMessage(), e);
		}
	}

	public Keypair get_original_keypair() {
		return this.original_keypair;
	}

	public Keypair get_updated_keypair() {
		return this.updated_keypair;
	}

}
