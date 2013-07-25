package uk.ac.cam.signups.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.forms.DeadlineForm;
import uk.ac.cam.signups.forms.GroupForm;
import uk.ac.cam.signups.helpers.LDAPQueryHelper;
import uk.ac.cam.signups.models.Deadline;
import uk.ac.cam.signups.models.Group;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.LDAPProvider;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;

@Path("/signapp/deadlines")
public class DeadlinesController extends ApplicationController {
	
	private User currentUser;
	
	//Logger
	private static Logger log = LoggerFactory.getLogger(DeadlinesController.class);
	
	// Index 
	@GET @Path("/") //@ViewWith("deadlines.index")
	@Produces(MediaType.APPLICATION_JSON)
	public Map indexDeadlines() {

		currentUser = initialiseUser();

		return ImmutableMap.of("crsid", currentUser.getCrsid(), "deadlines", currentUser.getUserDeadlinesMap(), "cdeadlines", currentUser.getUserCreatedDeadlinesMap());
	}
	
	// Create
	@POST @Path("/") 
	public void createGroup(@Form DeadlineForm deadlineForm) throws Exception {
		currentUser = initialiseUser();
		
		int id = deadlineForm.handleCreate(currentUser);
		
		throw new RedirectException("/app/#signapp/deadlines");
	}
	

	// Edit
	@GET @Path("/{id}/edit") //@ViewWith("/soy/deadlines.edit")
	@Produces(MediaType.APPLICATION_JSON)
	public Map editDeadline(@PathParam("id") int id) {
		
		currentUser = initialiseUser();
		
	  	Deadline deadline = Deadline.getDeadline(id);
	  	
	  	if(deadline==null){
	  		//throw new RedirectException("/app/#signapp/deadlines/error/1");
	  		return ImmutableMap.of("redirect", "signapp/deadlines/error/1");
	  	}
	  	if(!deadline.getOwner().equals(currentUser)){
	  		//throw new RedirectException("/app/#signapp/deadlines/error/2");
	  		return ImmutableMap.of("redirect", "signapp/deadlines/error/2");
	  	}
		return deadline.toMap();		
	}
	
	// Update
	@POST @Path("/{id}/edit")
	public void updateDeadline(@Form DeadlineForm deadlineForm, @PathParam("id") int id) {
		
		currentUser = initialiseUser();
		
		id = deadlineForm.handleUpdate(currentUser, id);
		
		throw new RedirectException("/app/#signapp/deadlines");
	}
	
	
	// Delete
	@DELETE @Path("/{id}")
	public void deleteDeadline(@PathParam("id") int id) {
		
		Deadline.deleteDeadline(id);
		
		throw new RedirectException("/app/#signapp/deadlines");
	}
	
	// Error
	@GET @Path("/error/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map deadlineError(@PathParam("type") int type){
		
		switch (type){
		case 1: // not found
			return ImmutableMap.of("errormsg", "Deadline not found, returning to deadlines page", "redirect", "/app/#signapp/deadlines");
		case 2: // auth
			return ImmutableMap.of("errormsg", "You are not authorised to edit this deadline, returning to deadlines page", "redirect", "/app/#signapp/deadlines");
		}
		
		return ImmutableMap.of("errormsg", "There was an error processing your request, returning to home page", "redirect", "/app/#signapp/deadlines");
		
	}
	
	// Find groups AJAX
	@POST @Path("/queryGroup")
	@Produces(MediaType.APPLICATION_JSON)
	public List queryCRSId(String q) {
		currentUser = initialiseUser();
		String crsid = currentUser.getCrsid();
		
		//Remove q= prefix
		String x = q.substring(2);
		
		//List of group matches
		ArrayList<ImmutableMap<String,?>> matches = new ArrayList<ImmutableMap<String, ?>>();
		
		//Get matching group names.. is this too slow? 
		for(Group g : currentUser.getGroups()){
			if(g.getTitle().contains(x)){
				matches.add(ImmutableMap.of("group_id", g.getId(), "group_name", g.getTitle()));
			}
		}
		
		return matches;
	}
}