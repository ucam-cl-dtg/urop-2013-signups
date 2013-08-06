package uk.ac.cam.signups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.UserLookupManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class ApplicationController {
	// Logger
	private static Logger log = LoggerFactory.getLogger(ApplicationController.class);
	
	// UserLookupManager for this user
	protected UserLookupManager ulm;
	
	// Raven session
	@Context
	HttpServletRequest sRequest;
	
	protected User initialiseUser() {
		
		// This will extract the CRSID of the current user and return it:
		log.debug("Getting crsid from raven");	
		log.error("Request is null? " + (sRequest == null));
		String crsid = (String) sRequest.getSession().getAttribute("RavenRemoteUser");
		
		// Create UserLookupManager for this user
		log.debug("Creating userLookupManager");	
		ulm = UserLookupManager.getUserLookupManager(crsid);
		
		// Register or return the user
		return User.registerUser(crsid);
	}
	
	// temporary for testing
	protected User initialiseSpecifiedUser(String crsid) {
		
		// Create UserLookupManager for this user
		log.debug("Creating userLookupManager");	
		ulm = UserLookupManager.getUserLookupManager(crsid);
		
		// Register or return the user
		return User.registerUser(crsid);
	}
}