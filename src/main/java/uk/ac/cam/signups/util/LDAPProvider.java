package uk.ac.cam.signups.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDAPProvider {

	public static String getData(String crsid, String type) {
		Logger log = LoggerFactory.getLogger(LDAPProvider.class);
		
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://ldap.lookup.cam.ac.uk:389");

		Attributes a = null;
		try {
			DirContext ctx = new InitialDirContext(env);
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			SearchResult searchResult = ctx.search(
					"ou=people,o=University of Cambridge,dc=cam,dc=ac,dc=uk",
					"(uid=" + crsid + ")", controls).next();
			a = searchResult.getAttributes();
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
		
		try {
        	return a.get(type).get().toString();
        } catch (NamingException e) {
			log.error(e.getMessage());
			return null;
		}
		
	}
}
