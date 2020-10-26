package es.hefame.keyvault.controller.rest;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import es.hefame.keyvault.dao.DAO;
import es.hefame.keyvault.datastructure.message.domain.DomainChangeMessageParser;
import es.hefame.keyvault.datastructure.message.domain.ListDomainMessage;
import es.hefame.keyvault.datastructure.message.person.ListPersonMessage;
import es.hefame.keyvault.datastructure.model.Person;
import es.hefame.keyvault.datastructure.model.domain.Domain;
import es.hefame.hcore.HException;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.HttpException;
import es.hefame.hcore.http.exchange.HttpConnection;

public class DomainRestHandler extends HttpController
{

	private static Logger log = LogManager.getLogger();

	@Override
	protected void get(HttpConnection t) throws IOException, HException
	{
		// String user_id = (String) t.request.get_attribute("user_id");
		// AuthzManager.check(AuthzManager.in_local_domain(user_id));

		String domainId = t.request.getURIField(2);
		String subcommand = t.request.getURIField(3);

		// GET /rest/domain
		if (domainId == null)
		{
			List<Domain> domainList = DAO.domain().getList();
			ListDomainMessage message = new ListDomainMessage(domainList);
			t.response.send(message, 200);
			return;
		}

		// GET /rest/domain/<domain_id>[/*]
		Domain domain = DAO.domain().getById(domainId);
		if (domain == null)
		{
			t.response.send(new HttpException(404, "No se encuenta el dominio con el ID especificado"));
			return;
		}

		// GET /rest/domain/<domain_id>
		if (subcommand == null)
		{
			t.response.send(domain, 200);
			return;
		}

		switch (subcommand.toLowerCase())
		{
			// GET /rest/domain/<domain_id>/members
			case "members":
				log.info("Peticion para obtener los miembros del dominio [{}]", domain);
				List<Person> members = DAO.person().getByDomain(domain);
				ListPersonMessage peopleMessage = new ListPersonMessage(members);
				t.response.send(peopleMessage, 200);
				return;
			// GET /rest/domain/<domain_id>/*
			default:
				t.response.send(domain, 200);
				return;
		}

	}

	@Override
	protected void post(HttpConnection t) throws IOException, HException
	{
		// String user_id = (String) t.request.get_attribute("user_id");
		// AuthzManager.check(AuthzManager.in_local_domain(user_id));

		DomainChangeMessageParser incoming = new DomainChangeMessageParser(t.request.getBodyAsByteArray());
		Domain d = incoming.getDomain();
		DAO.domain().insert(d);
		d = DAO.domain().getById(d.getIdentifier());
		t.response.send(d, 201);

	}

	@Override
	protected void put(HttpConnection t) throws IOException, HException
	{
		// String user_id = (String) t.request.get_attribute("user_id");
		// AuthzManager.check(AuthzManager.in_local_domain(user_id));

		String domainId = t.request.getURIField(2);
		if (domainId == null)
		{
			t.response.send(new HttpException(400, "No se ha especificado un dominio"));
			return;
		}

		Domain original = DAO.domain().getById(domainId);
		if (original == null)
		{
			t.response.send(new HttpException(400, "No se encuentra el dominio especificado"));
			return;
		}

		DomainChangeMessageParser incoming = new DomainChangeMessageParser(domainId, t.request.getBodyAsByteArray());
		Domain d = incoming.getDomain();
		DAO.domain().update(d);
		d = DAO.domain().getById(d.getIdentifier());
		t.response.send(d, 200);
	}

	@Override
	protected void delete(HttpConnection t) throws IOException, HException
	{
		// String user_id = (String) t.request.get_attribute("user_id");
		// AuthzManager.check(AuthzManager.in_local_domain(user_id));

		String domainId = t.request.getURIField(2);

		if (domainId == null)
		{
			t.response.send(new HttpException(400, "No se ha especificado un dominio"));
			return;
		}

		Domain domain = DAO.domain().getById(domainId);
		if (domain == null)
		{
			t.response.send(new HttpException(404, "Dominio no encontrado"));
			return;
		}

		DAO.domain().delete(domain);
		t.response.send(domain, 200);
	}
}
