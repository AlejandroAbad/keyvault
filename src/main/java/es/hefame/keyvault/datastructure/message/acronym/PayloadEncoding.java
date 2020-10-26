package es.hefame.keyvault.datastructure.message.acronym;

import java.util.HashMap;
import java.util.Map;

import es.hefame.hcore.converter.ByteArrayConverter;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.util.exception.NoSuchAcronymException;

public enum PayloadEncoding {
	ASCII(0), BASE64(1);

	private static Map<String, PayloadEncoding> aliases;
	static {
		aliases = new HashMap<>();

		aliases.put("none", ASCII);
		aliases.put("text", ASCII);
		aliases.put("ascii", ASCII);
		aliases.put("ascii-7", ASCII);
		aliases.put("ascii-8", ASCII);
		aliases.put("iso-8859-1", ASCII);
		aliases.put("iso-8859-15", ASCII);
		aliases.put("plain", ASCII);

		aliases.put("base64", BASE64);
	}

	public final int code;

	private PayloadEncoding(int code) {
		this.code = code;
	}

	public static PayloadEncoding build(String incoming) throws NoSuchAcronymException {
		if (incoming == null) {
			return ASCII;
		}

		PayloadEncoding algo = aliases.get(incoming.toLowerCase());

		if (algo != null) {
			return algo;
		} else {
			throw new NoSuchAcronymException("payload_encoding", incoming);
		}
	}

	public byte[] decode(String rawPayload) throws HttpException {
		switch (this) {
			case ASCII:
				return rawPayload.getBytes();

			case BASE64:
				try {
					return ByteArrayConverter.fromBase64(rawPayload);
				} catch (Exception e) {
					throw new HttpException(400, "Cannot decode payload", e);
				}
			default:
				throw new HttpException(400, "Payload encoding not supported");

		}

	}

}
