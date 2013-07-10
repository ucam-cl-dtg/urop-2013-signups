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

	/*
	 * The request will automatically be given the value of the current request
	 * when using the @Context tag. You need the request for getting a hibernate
	 * session and also for getting the current user, which is passed as a session
	 * attribute (the session being contained in the request).
	 */
	@Context
	HttpServletRequest request;

	/* 
	 * You could also declare the following in a method locally; you might as well
	 * have one logger for the entire class. Make sure it is static if you do.  
	 */
//	private static Logger log = LoggerFactory.getLogger(MainController.class);
	
	
	@GET
	@Path("/raven/")
	@ViewWith("/soy/raven.index")
	public Map<String, ?> demo() {
		
		// Get user's name and CRSID
		ImmutableMap<String, ?> user = getUserDetails();

		return ImmutableMap.of("user", user);
	}

	public ImmutableMap<String, ?> getUserDetails() {
		// This will extract the CRSID of the current user and return it:
		String crsid = (String) request.getSession().getAttribute("RavenRemoteUser");
		// This will get the user's name from LDAP
		String name = LDAPProvider.getData(crsid, "cn");
		
		// Return a map of all the users data
		return ImmutableMap.of("crsid", crsid, "name", name);
	}

}