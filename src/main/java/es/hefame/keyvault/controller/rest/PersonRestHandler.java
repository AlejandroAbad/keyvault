package es.hefame.keyvault.controller.rest;

import java.io.IOException;
import java.util.List;

import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.dao.KeypairDAO;
import es.hefame.keyvault.datastructure.message.keypair.KeypairListMessage;
import es.hefame.keyvault.datastructure.message.person.ListPersonMessage;
import es.hefame.keyvault.datastructure.message.person.PersonChangeMessageParser;
import es.hefame.keyvault.datastructure.model.Keypair;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.HttpException;
import es.hefame.hcore.http.exchange.HttpConnection;

public class PersonRestHandler extends HttpController
{
	@Override
	protected void get(HttpConnection t) throws IOException, HException
	{
		// String authenticated_user_id = (String) t.request.get_attribute("user_id");

		String personId = t.request.getURIField(2);
		String subcommand = t.request.getURIField(3);

		// GET /rest/person
		if (personId == null)
		{
			// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id));

			List<Person> people = DAO.person().getList();
			ListPersonMessage message = new ListPersonMessage(people);
			t.response.send(message, 200);
			return;
		}

		// GET /rest/person/<person_id>[/*]
		Person person = DAO.person().getByFQDN(personId);
		if (person == null)
		{
			// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id));
			t.response.send(new HttpException(404, "No se encuentra la persona solicitada"));
			return;
		}

		// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id) || AuthzManager.is_person(authenticated_user_id, person));
		// GET /rest/person/<person_id>
		if (subcommand == null)
		{
			t.response.send(person, 200);
			return;
		}

		switch (subcommand.toLowerCase())
		{
			case "keypairs":
				KeypairDAO keypairDAO = DAO.keypair();
				List<Keypair> keypairList = keypairDAO.getOwnedBy(person);
				KeypairListMessage peopleMessage = new KeypairListMessage(keypairList);
				t.response.send(peopleMessage, 200);
				return;
			default:
				t.response.send(person, 200);
				return;
		}

	}

	@Override
	protected void post(HttpConnection t) throws HException, IOException
	{
		// String authenticated_user_id = (String) t.request.get_attribute("user_id");
		// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id));

		PersonChangeMessageParser incoming = new PersonChangeMessageParser(t.request.getBodyAsByteArray());
		Person p = incoming.getPerson();
		DAO.person().insert(p);
		p = DAO.person().getByFQDN(p.getIdentifier());
		t.response.send(p, 201);
	}


	@Override
	protected void delete(HttpConnection t) throws HException, IOException
	{
		// String authenticated_user_id = (String) t.request.get_attribute("user_id");
		// AuthzManager.check(AuthzManager.in_local_domain(authenticated_user_id));

		String personId = t.request.getURIField(2);

		if (personId == null)
		{
			t.response.send(new HttpException(400, "No se especifica la persona a borrar"));
			return;
		}

		Person person = DAO.person().getByFQDN(personId);
		if (person == null)
		{
			t.response.send(new HttpException(404, "No se encuentra la persona especificada"));
			return;
		}

		DAO.person().delete(person);
		t.response.send(person, 200);
	}
}
