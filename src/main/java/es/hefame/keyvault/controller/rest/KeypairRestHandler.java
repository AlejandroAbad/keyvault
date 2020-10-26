package es.hefame.keyvault.controller.rest;

import java.io.IOException;
import java.util.List;

import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpException;
import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.datastructure.message.acronym.MimeType;
import es.hefame.keyvault.datastructure.message.keypair.KeypairInsertMessageParser;
import es.hefame.keyvault.datastructure.message.keypair.KeypairListMessage;
import es.hefame.keyvault.datastructure.message.keypair.KeypairUpdateMessageParser;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.domain.Domain;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.exchange.FormEncodedQuery;
import es.hefame.hcore.http.exchange.HttpConnection;

public class KeypairRestHandler extends HttpController {

	/**
	 * Obtiene información de pares de claves almacenadas en la BBDD. Si no se
	 * especifica el nombre del par de claves, se devuelve la lista de pares de
	 * claves del usuario solicitante. Si se especifica el nombre del par de claves,
	 * por defecto se devuelve únicamente la información del par indicado en formato
	 * JSON. Se pueden solicitar en otros formatos, indicando en el parámetro URI
	 * 'format' el formato deseado. Se admiten los alias definidos en el enumerado
	 * 'MimeType'
	 */
	@Override
	protected void get(HttpConnection t) throws IOException, HException {

		String keypair_id = t.request.getURIField(2);

		if (keypair_id == null) {
			String authenticatedUserID = t.request.getInternalValue(Domain.USER_ID, String.class);
			if (authenticatedUserID == null) {
				t.response.send(new HttpException(403, "Lista de certificados no disponible"));
				return;
			}

			// Esto debe listar las claves del usuario autenticado
			KeypairDAO keypairDAO = DAO.keypair();
			List<Keypair> keys = keypairDAO.get_owned_by_person_id(authenticatedUserID);
			KeypairListMessage message = new KeypairListMessage(keys);
			t.response.send(message, 200);
		} else {
			KeypairDAO keypairDAO = DAO.keypair();

			Keypair keypair = keypairDAO.get_by_id(keypair_id);
			if (keypair != null) {
				// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id) ||
				// AuthzManager.is_person(authenticated_user_id, keypair.get_owner_id()));

				FormEncodedQuery query = t.request.getQueryString();
				String requestedType = query.getLast("format");

				if (requestedType != null) {
					MimeType mimeType = MimeType.build(requestedType);
					byte[] rawData = keypair.getAsMimeType(mimeType);
					t.response.setHeader("Content-Disposition", "attachment; filename=\"" + keypair.getFileName(mimeType) + "\"");
					t.response.send(rawData, 200, mimeType.mimeType);
					return;
				}

				// No especifica formato, se devuelve en JSON
				t.response.send(keypair, 200);
				return;

			} else {
				t.response.send(new HttpException(404, "No se encuentra el keypair solicitado"));
				return;
			}
		}
	}

	@Override
	protected void post(HttpConnection t) throws IOException, HException {
		byte[] request_body = t.request.getBodyAsByteArray();
		KeypairInsertMessageParser incoming_message = new KeypairInsertMessageParser(request_body);
		Keypair new_keypair = incoming_message.get_keypair();
		KeypairDAO keypair_datasource = DAO.keypair();
		System.out.println(keypair_datasource.insert(new_keypair));
		t.response.send(new_keypair, 200);
	}

	@Override
	protected void put(HttpConnection t) throws IOException, HException {
		// String authenticated_user_id = (String) t.request.get_attribute("user_id");
		// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id) ||
		// AuthzManager.is_person(authenticated_user_id, keypair.get_owner_id()));

		byte[] request_body = t.request.getBodyAsByteArray();
		KeypairUpdateMessageParser incoming_message = new KeypairUpdateMessageParser(request_body);
		Keypair modified_keypair = incoming_message.get_updated_keypair();

		Keypair original = DAO.keypair().get_by_id(modified_keypair.getIdentifier());

		if (original == null) {
			t.response.send(new HttpException(404, "No se encuentra el keypair especificado"));
			return;
		}

		// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id) ||
		// AuthzManager.is_person(authenticated_user_id, original.get_owner_id()));

		KeypairDAO keypair_datasource = DAO.keypair();
		System.out.println(keypair_datasource.update(modified_keypair));
		t.response.send(modified_keypair, 200);
	}

	@Override
	protected void delete(HttpConnection t) throws HException, IOException {
		// String authenticated_user_id = (String) t.request.get_attribute("user_id");

		String keypair_id = t.request.getURIField(2);

		if (keypair_id == null) {
			t.response.send(new HttpException(400, "No se ha especificado un keypair"));
			return;
		}

		Keypair keypair_to_delete = DAO.keypair().get_by_id(keypair_id);
		if (keypair_to_delete == null) {
			t.response.send(new HttpException(404, "No se encuentra el keypair especificado"));
			return;
		}

		// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id) ||
		// AuthzManager.is_person(authenticated_user_id,
		// keypair_to_delete.get_owner_id()));

		DAO.keypair().delete(keypair_to_delete);
		t.response.send(keypair_to_delete, 200);
	}

}
