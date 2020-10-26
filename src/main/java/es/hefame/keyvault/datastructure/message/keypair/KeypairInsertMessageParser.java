package es.hefame.keyvault.datastructure.message.keypair;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import es.hefame.hcore.HException;
import es.hefame.hcore.converter.ByteArrayConverter;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.datastructure.message.acronym.KeypairFormat;
import es.hefame.keyvault.datastructure.model.Keypair;

public class KeypairInsertMessageParser {
	private static Logger L = LogManager.getLogger();
	private Keypair keypair;

	public KeypairInsertMessageParser(byte[] request) throws HException {
		JSONParser parser = new JSONParser();

		try {
			Object obj = parser.parse(new String(request));
			JSONObject jsonObject = (JSONObject) obj;

			String id = (String) jsonObject.get("id");
			String owner = (String) jsonObject.get("owner");
			KeypairFormat format = KeypairFormat.build((String) jsonObject.get("keypair_format"));

			if (id == null) {
				throw new HttpException(400, "'id' field is mandatory");
			}
			if (owner == null) {
				throw new HttpException(400, "'owner' field is mandatory");
			}
			if (format == null) {
				throw new HttpException(400, "'keypair_format' field is mandatory");
			}

			switch (format) {
				case PKCS12:
					id = id.trim();
					String payload = (String) jsonObject.get("payload");
					String passphrase = (String) jsonObject.get("passphrase");
					if (payload == null) {
						throw new HttpException(400, "'payload' field is mandatory if format is PKCS#12");
					}

					if (passphrase == null) {
						passphrase = "";
					}

					L.trace("Base 64: [{}]", payload);

					byte[] pkcs12_data = ByteArrayConverter.fromBase64(payload);

					L.trace("Tamano decodificado: [{}]", payload);

					// byte[] pkcs12_data = new BASE64Decoder().decodeBuffer(payload);

					this.keypair = new Keypair(id, pkcs12_data, owner, passphrase.toCharArray());
					break;
				default:
					throw new HttpException(400, "Format value is invalid");
			}
		} catch (HException | ParseException e) {
			throw new HttpException(400, e.getMessage(), e);
		}
	}

	public Keypair get_keypair() {
		return this.keypair;
	}

}
