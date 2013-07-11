package uk.ac.cam.signups.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateSessionRequestFilter;
import uk.ac.cam.signups.util.UserLookupManager;

public class ApplicationController {
	// Create the logger
	private static Logger log = LoggerFactory.getLogger(ApplicationController.class);
	
	// Database session
	protected Session session;
	
	// UserLookupManager for this user
	protected UserLookupManager ulm;
	
	// Raven current request
	@Context
	HttpServletRequest request;
	
	//UserLookupManager setup
	protected void initialiseUser(){
		// This will extract the CRSID of the current user and return it:
		log.debug("Getting crsid from raven");	
		String crsid = (String) request.getSession().getAttribute("RavenRemoteUser");
		// Create UserLookupManager for this user
		log.debug("Creating userLookupManager");	
		ulm = UserLookupManager.getUserLookupManager(crsid);
		// Add user to database if necessary
		log.debug("Checking if user is in database");
		registerUser(crsid);		
	}
	
	// Add to User class later
	protected void registerUser(String crsid) {
		// Begin hibernate session
		log.debug("begin hibernate session");
		session = HibernateSessionRequestFilter.openSession(request);
		session.beginTransaction();
		// Does the user already exist?
		Query userQuery = session.createQuery("from User where id = :id").setParameter("id", crsid);
	  	User user = (User) userQuery.uniqueResult();
	  	
	  	// If no, create them
	  	if(user==null){
	  		log.debug("User " + crsid + "does not exist");
	  		User newUser = new User(crsid, null, null, null, null, null);
	  		session.save(newUser);
	  		log.info("User " + crsid + " added to USERS table");
	  	}
	  	// Close hibernate session
	  	log.debug("closing hibernate session");
		session.getTransaction().commit();
		session.close();		
	}
}
