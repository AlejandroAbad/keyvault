package es.hefame.keyvault.datastructure.model.domain;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import es.hefame.hcore.http.HttpException;
import es.hefame.hcore.http.exchange.IHttpRequest;
import es.hefame.keyvault.datastructure.model.Person;

public class LdapDomain extends Domain {

	private static Logger L = LogManager.getLogger();

	public static final String USER_LDAP_CN = "USER_LDAP_CN";
	public static final String USER_LDAP_UID = "USER_LDAP_UID";

	private String ldap_uri;
	private String search_branch;
	private String search_filter;

	public LdapDomain(String identifier, JSONObject connection_data) throws HttpException {
		super(identifier);
		if (connection_data == null) {
			throw new HttpException(400, "No se especifican datos de conexion al dominio");
		}

		this.ldap_uri = (String) connection_data.get("ldap_uri");
		this.search_branch = (String) connection_data.get("search_branch");
		this.search_filter = (String) connection_data.get("search_filter");

		if (ldap_uri == null) {
			throw new HttpException(400, "El campo 'ldap_uri' debe especificarse");
		}
		if (search_branch == null) {
			throw new HttpException(400, "El campo 'search_branch' debe especificarse");
		}
		if (search_filter == null) {
			throw new HttpException(400, "El campo 'search_filter' debe especificarse");
		}
		this.setConnectionData(jsonEncodeConnectionData());
	}

	public LdapDomain(String identifier, String ldapURI, String searchBranch, String searchFilter)
			throws HttpException {
		super(identifier);
		if (ldapURI == null) {
			throw new HttpException(400, "'ldap_uri' cannot be null");
		}
		if (searchBranch == null) {
			throw new HttpException(400, "'search_branch' cannot be null");
		}
		if (searchFilter == null) {
			throw new HttpException(400, "'search_filter' cannot be null");
		}

		this.ldap_uri = ldapURI;
		this.search_branch = searchBranch;
		this.search_filter = searchFilter;
		this.setConnectionData(jsonEncodeConnectionData());
	}

	@Override
	public String getDomainType() {
		return "ldap";
	}

	public String getLdapURI() {
		return ldap_uri;
	}

	public String getSearchBranch() {
		return search_branch;
	}

	public String getSearchFilter() {
		return search_filter;
	}

	@Override
	public String generateFQDN(Person signer) {
		StringBuilder sb = new StringBuilder();
		sb.append(signer.getName().toLowerCase()).append('@').append(this.getIdentifier());
		return sb.toString();
	}

	@Override
	public boolean authenticate(String userFQDN, String password, IHttpRequest t) throws HttpException {

		t.setInternalValue(DOMAIN_ID, this.getIdentifier());
		t.setInternalValue(DOMAIN_TYPE, this.getDomainType());

		String personName = Domain.getPersonNameFromFQDN(userFQDN);
		t.setInternalValue(USER_NAME, personName);
		t.setInternalValue(USER_ID, userFQDN);

		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, this.ldap_uri);
		env.put(Context.SECURITY_PRINCIPAL, userFQDN);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.REFERRAL, "follow");

		// Create the initial context
		try {
			DirContext ctx = new InitialDirContext(env);

			SearchControls ctls = new SearchControls();
			String[] attrIDs = { "cn", "uid", "memberOf" };
			ctls.setReturningAttributes(attrIDs);
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String parsedFilter = this.search_filter.replace("{%u}", personName);

			L.debug("Autenticando al usuario [{}] contra el LDAP [{}] en la rama [{}] con filtro [{}]", personName,	this.ldap_uri, this.search_branch, parsedFilter);
			NamingEnumeration<?> answer = ctx.search(this.search_branch, parsedFilter, ctls);

			if (answer.hasMore()) {
				SearchResult rslt = (SearchResult) answer.next();
				Attributes attrs = rslt.getAttributes();

				String cn = attrs.get("cn").get().toString();
				String uid = attrs.get("uid").get().toString();

				L.info("Estableciendo datos del usuario CN=[{}], UID=[{}]", cn, uid);
				t.setInternalValue(USER_LDAP_CN, cn);
				t.setInternalValue(USER_LDAP_UID, uid);

				List<String> userGroups = new LinkedList<>();
				NamingEnumeration<?> groups = attrs.get("memberOf").getAll();
				while (groups.hasMore()) {
					String s = groups.next().toString();
					userGroups.add(s);
				}

				L.debug("Estableciendo grupos del usuario: {}", userGroups);
				t.setInternalValue(USER_GROUPS, userGroups);
				return true;
			} else {
				return false;
			}


		} catch (NamingException e) {
			L.error("Fallo al autenticar al usuario contra el servidor LDAP");
			L.catching(e);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject jsonEncodeConnectionData() {
		JSONObject root = new JSONObject();
		root.put("ldap_uri", this.getLdapURI());
		root.put("search_branch", this.getSearchBranch());
		root.put("search_filter", this.getSearchFilter());
		return root;
	}

}
