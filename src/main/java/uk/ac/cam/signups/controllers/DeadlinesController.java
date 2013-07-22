package uk.ac.cam.signups.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	// Index 
	@GET @Path("/") //@ViewWith("deadlines.index")
	@Produces(MediaType.APPLICATION_JSON)
	public Map indexDeadlines() {
		// Get current user
		currentUser = initialiseUser();
		

		return ImmutableMap.of("crsid", currentUser.getCrsid(), "deadlines", currentUser.get);
	}

	// Find groups for AJAX
	@POST @Path("/queryGroup")
	@Produces(MediaType.APPLICATION_JSON)
	public List queryCRSId(String q) {
		currentUser = initialiseUser();
		String crsid = currentUser.getCrsid();
		
		//Remove q= prefix
		String x = q.substring(2);
		
		//List of group matches
		ArrayList<ImmutableMap<String,?>> matches = new ArrayList<ImmutableMap<String, ?>>();
		
		//Get matching group names.. O(n) each time.. is this too slow? maybe define the .equals and hashCode method?
		for(Group g : currentUser.getGroups()){
			if(g.getTitle().contains(x)){
				matches.add(ImmutableMap.of("group_id", g.getId(), "group_name", g.getTitle()));
			}
		}
		
		return matches;
	}
	
	// Create
	@POST @Path("/") 
	public void createGroup(@Form DeadlineForm deadlineForm) throws Exception {
		currentUser = initialiseUser();
		
		int id = deadlineForm.handle(currentUser);
		
		throw new RedirectException("/app/#signapp/deadlines");
	}
	

//	// Edit
//	@GET @Path("/{id}/edit") @ViewWith("/soy/deadlines.edit")
//	public Map editDeadline(@PathParam("id") int deadlineId) {
//		
//		return ImmutableMap.of();
//	}
//	
//	// Update
//	@PUT @Path("/{id}")
//	public void updateDeadline(@PathParam("id") int deadlineId,
//			@Form Deadline deadline) {
//		
//	}
//	
//	// Delete
//	@DELETE @PathParam("/{id}")
//	public void deleteDeadline(@PathParam("id") int deadlineId) {
//		
//		throw new RedirectException("/events/" + id + "/deadlines");
//	}
}