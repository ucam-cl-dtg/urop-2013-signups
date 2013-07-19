package uk.ac.cam.signups.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class LDAPProvider {

	//Logger
	private static Logger log = LoggerFactory.getLogger(LDAPProvider.class);
	
	// Query LDAP people
	public static Attributes queryPeople(String crsid, String type) {
		
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
		
		return a;
	}
	
	//TODO: query LDAP groups and institutions
	
	// Get a single result
	public static String getUniqueResult(String crsid, String type) {
		
		Attributes a = queryPeople(crsid, type);
		
		//If no match in search
		if(a==null){ return null;}
		
		try {
        	return a.get(type).get().toString();
        } catch (NamingException e) {
			log.error(e.getMessage());
			return null;
		} 
		
	}
	
	//Get a list of results as strings
	public static List<String> getStringListResult(String crsid, String type){
		
		Attributes a = queryPeople(crsid, type);
		
		try {
			List<String> listResults = new ArrayList<String>();
			
			//Initialise enumResults
			NamingEnumeration enumResults;
			
			//If no results return an empty list
			try {
			 enumResults = a.get(type).getAll();
			} catch (NullPointerException e){
				log.debug("User has no photo");
				return null;
			}
			
			// Convert enumeration type results to string
				while(enumResults.hasMore()){
				listResults.add(enumResults.next().toString());
				
				return listResults;
				}
					
        } catch (NamingException e) {
			log.error(e.getMessage());
			return null;
		}
		return new ArrayList<String>();
	}
	
	// Get LDAP data in unchanged enumeration type form
	public static NamingEnumeration getEnumListResult(String crsid, String type){
		
		Attributes a = queryPeople(crsid, type);
		
		try {
			try {
			 return a.get(type).getAll();
			} catch (NullPointerException e){
				log.debug("No data of type " + " for this user");
				return null;
			}	
					
        } catch (NamingException e) {
			log.error(e.getMessage());
			return null;
		}
	}	
	
	public static List testPartialQuery(String x){
		
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://ldap.lookup.cam.ac.uk:389");

		NamingEnumeration<SearchResult> enumResults;
		
		Attributes a = null;
		try {
			DirContext ctx = new InitialDirContext(env);
			SearchControls controls = new SearchControls();
			controls.setReturningAttributes(new String[]{"uid"});
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			enumResults = ctx.search(
					"ou=people,o=University of Cambridge,dc=cam,dc=ac,dc=uk",
					"(uid=" + x + "*)", controls);
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
		
		try {
			List<String> listResults = new ArrayList<String>(0);
			
			// Convert enumeration type results to string
				while(enumResults.hasMore()){
					listResults.add(enumResults.next().getAttributes().get("uid").get().toString());
				}
				
			return (ArrayList<String>) listResults;
					
        } catch (NamingException e) {
			log.error(e.getMessage());
			return null;
		} 
		
	}
	
	public static List partialUserSearch(String x){
		
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://ldap.lookup.cam.ac.uk:389");

		NamingEnumeration<SearchResult> enumResults;
		
		Attributes a = null;
		try {
			DirContext ctx = new InitialDirContext(env);
			SearchControls controls = new SearchControls();
			controls.setReturningAttributes(new String[]{"uid", "displayName"});
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			enumResults = ctx.search(
					"ou=people,o=University of Cambridge,dc=cam,dc=ac,dc=uk",
					"(uid=" + x + "*)", controls);
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		}
		
		try {
			ArrayList<ImmutableMap<String,?>> userMatches = new ArrayList<ImmutableMap<String,?>>();			
			
			// Convert enumeration type results to string
				while(enumResults.hasMore()){
					Attributes result = enumResults.next().getAttributes();
					userMatches.add(ImmutableMap.of("crsid", result.get("uid").get().toString(), "name", result.get("displayName").get().toString()));
				}
				
			return userMatches;
					
        } catch (NamingException e) {
			log.error(e.getMessage());
			return null;
		} 
		
	}
	
}
