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

import uk.ac.cam.signups.util.UserLookupManager;

//Import the following for raven AND hibernate
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;


@Path("/")
public class HomePageController {
	// Database session
	private Session session;
	
	// UserLookupManager for this user
	private UserLookupManager ulm;
	
	// Raven current request
	@Context
	HttpServletRequest request;
	
	// Create the logger
	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
	
	// Index
	@GET @Path("/") @ViewWith("/soy/home_page.index")
	public Map indexHomePage() {
		// Get user details
		ImmutableMap<String, ?> user = getUserDetails();
		
		// Return data for template
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
	
	// Method to get details of current user. 
	public ImmutableMap<String, ?> getUserDetails() {
		
		// This will extract the CRSID of the current user and return it:
		String crsid = (String) request.getSession().getAttribute("RavenRemoteUser");
		// Create UserLookupManager for this user
		ulm = UserLookupManager.getUserLookupManager(crsid);
		// Add user to database if necessary
		registerUser(crsid);
		
		// Return all data of current user
		return ulm.getAll();
	}
	
	// Add to User class later
	public void registerUser(String crsid) {
		// Begin hibernate session
		session = HibernateSessionRequestFilter.openSession(request);
		session.beginTransaction();
		// Does the user already exist?
		Query userQuery = session.createQuery("from User where id = :id").setParameter("id", crsid);
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
	}
}