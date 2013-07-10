package uk.ac.cam.signups.controllers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

// Import the following for logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import for hibernate requests
import uk.ac.cam.signups.util.HibernateSessionRequestFilter;

//Import for cam lookup requests
import uk.ac.cam.signups.util.LDAPProvider;

import org.hibernate.Session;

// Import the following for raven AND hibernate
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;


@Path("/")
public class RavenTestController {
	private Session session;
	
	//Current request
	@Context
	HttpServletRequest request;

	// Create the logger
	private static Logger log = LoggerFactory.getLogger(RavenTestController.class);
	
	
	@GET
	@Path("/raven/")
	@ViewWith("/soy/raven.index")
	public Map<String, ?> demo() {
		
		// Get user's name and CRSID
		ImmutableMap<String, ?> user = getUserDetails();
		log.info("User: "  + user.get("name") + " " + user.get("crsid"));
		
		return ImmutableMap.of("user", user);
	}

	public ImmutableMap<String, ?> getUserDetails() {
		// This will extract the CRSID of the current user and return it:
		String crsid = (String) request.getSession().getAttribute("RavenRemoteUser");
		// This will get the user's name from LDAP
		String name = LDAPProvider.getData(crsid, "cn");
		
		// Get a session from hibernate using the current request
		session = HibernateSessionRequestFilter.openSession(request);
		session.beginTransaction();
		session.getTransaction().commit();
		session.close();
		
		// Return a map of all the users data
		return ImmutableMap.of("crsid", crsid, "name", name);
	}

}