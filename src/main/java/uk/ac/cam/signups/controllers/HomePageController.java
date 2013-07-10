package uk.ac.cam.signups.controllers;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;

//Import models
import uk.ac.cam.signups.models.*;

//Import the following for logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Import for hibernate requests
import uk.ac.cam.signups.util.HibernateSessionRequestFilter;

import org.hibernate.Query;
import org.hibernate.Session;

//Import for cam lookup requests
import uk.ac.cam.signups.util.LDAPProvider;

//Import the following for raven AND hibernate
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;


@Path("/")
public class HomePageController {
	// Database session
	private Session session;
	
	// Raven current request
	@Context
	HttpServletRequest request;
	
	// Create the logger
	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
	
	// Index
	@GET @Path("/") @ViewWith("/soy/home_page.index")
	public Map indexHomePage() {
		ImmutableMap<String, ?> user = getUserDetails();
		return ImmutableMap.of("user", user);
	}
	
	// DOS Index
	@GET @Path("/DoS") @ViewWith("/soy/home_page.dos")
	public Map dosHomePage() {
		return ImmutableMap.of();
	}
	
	// Admin Index
	@GET @Path("/admin") @ViewWith("/soy/home_page.admin")
	public Map adminHomePage() {
		return ImmutableMap.of();
	}
	
	// Method to get details of current user. Possibly add to user model as a static method?
	public ImmutableMap<String, ?> getUserDetails() {
		// This will extract the CRSID of the current user and return it:
		String crsid = (String) request.getSession().getAttribute("RavenRemoteUser");
		// This will get the user's name from LDAP
		String name = LDAPProvider.getData(crsid, "cn");
		
		// Begin hibernate session
		session = HibernateSessionRequestFilter.openSession(request);
		session.beginTransaction();
		// Does the user already exist?
		Query userQuery = session.createQuery("from USERS where id = :id").setParameter("id", crsid);
	  	User user = (User) userQuery.uniqueResult();
	  	
	  	// If no, create them
	  	if(user==null){
	  		log.debug("User " + crsid + "does not exist");
	  		User newUser = new User(crsid);
	  		session.save(newUser);
	  		log.info("User " + crsid + " added to USERS table");
	  	}
	  	// Close hibernate session
		session.getTransaction().commit();
		session.close();
		
		// Return a map of all the users data
		return ImmutableMap.of("crsid", crsid, "name", name);
	}
}