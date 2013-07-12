package uk.ac.cam.signups.controllers;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;

//Import models
import uk.ac.cam.signups.models.*;

//Import the following for logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class HomePageController extends ApplicationController{
	
	// Create the logger
	private static Logger log = LoggerFactory.getLogger(HomePageController.class);
	
	private User user;
	
	// Index
	@GET @Path("/") @ViewWith("/soy/home_page.index")
	public Map indexHomePage() {
		
		// Initialise user
		user = initialiseUser();
		
		// Get user details
		log.debug("Index GET: Getting user details");
		ImmutableMap<String, ?> user = ulm.getAll();
		
		// Return data for template
		return ImmutableMap.of("user", user);
	}
	
	// DOS Index
	@GET @Path("/DoS") @ViewWith("/soy/home_page.dos")
	public Map dosHomePage() {
		
		// Initialise user
		initialiseUser();
		
		// Does user have staff level access?
		if(!isStaff()){
			throw new RedirectException("/");
		}
		
		return ImmutableMap.of();
	}
	
	// Admin Index
	@GET @Path("/admin") @ViewWith("/soy/home_page.admin")
	public Map adminHomePage() {
		return ImmutableMap.of();
	}
	
	// Authenticate staff
	public boolean isStaff() {
		try {
			return (ulm.getStatus().equals("staff"));
		} catch(NullPointerException e) {
			log.error("User initialisation failed");
			return false;
		}
	}
}