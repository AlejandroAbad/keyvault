package es.hefame.keyvault.controller.rest;

import java.io.IOException;

import es.hefame.keyvault.datastructure.message.AuthCheckResponse;
import es.hefame.keyvault.datastructure.model.domain.Domain;
import es.hefame.keyvault.datastructure.model.domain.LdapDomain;
import es.hefame.hcore.HException;
import es.hefame.hcore.converter.StringConverter;
import es.hefame.hcore.http.HttpController;
import es.hefame.hcore.http.authentication.NullAuthenticator;
import es.hefame.hcore.http.exchange.HttpConnection;

public class AuthCheckRestHandler extends HttpController
{
	public AuthCheckRestHandler()
	{
		super();
		this.setAuthenticator(new NullAuthenticator());
	}

	@Override
	protected void get(HttpConnection t) throws IOException, HException
	{

		String userId = null;
		String displayName = null;
		boolean authenticated = false;

		if (HttpController.defaultAuthenticator != null)
		{
			authenticated = HttpController.defaultAuthenticator.authenticateRequest(t.request);
		}

		if (authenticated)
		{
			userId = t.request.getInternalValue(Domain.USER_ID, String.class);
			displayName = StringConverter.upperCaseFirst(Domain.getPersonNameFromFQDN(userId));

			switch (t.request.getInternalValue(Domain.DOMAIN_TYPE, String.class))
			{
				case "ldap":
					displayName = t.request.getInternalValue(LdapDomain.USER_LDAP_CN, String.class);
					break;
				default:
			}

		}

		AuthCheckResponse msg = new AuthCheckResponse(userId, authenticated, displayName);
		t.response.send(msg, 200);

	}

}
