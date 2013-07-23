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
		// Get current user
		currentUser = initialiseUser();
		
		String crsid;
		List<ImmutableMap<String, ?>> deadlines;
		List<ImmutableMap<String, ?>> cdeadlines;
		
		try {
			crsid = currentUser.getCrsid();
			deadlines = currentUser.getUserDeadlinesMap();
			cdeadlines = currentUser.getUserCreatedDeadlinesMap();
		} catch (Exception e) {
			log.error("Error getting deadlines: "+  e.getMessage());
			throw new RedirectException("/app/#signapp/deadlines/error");
		}
		
		return ImmutableMap.of("crsid", crsid, "deadlines", deadlines, "cdeadlines", cdeadlines);
	}
	
	// Create
	@POST @Path("/") 
	public void createGroup(@Form DeadlineForm deadlineForm) throws Exception {
		currentUser = initialiseUser();
		
		int id = deadlineForm.handle(currentUser);
		
		throw new RedirectException("/app/#signapp/deadlines");
	}
	

	// Edit
	@GET @Path("/{id}/edit") //@ViewWith("/soy/deadlines.edit")
	@Produces(MediaType.APPLICATION_JSON)
	public Map editDeadline(@PathParam("id") int id) {
		currentUser = initialiseUser();
		
		// Get the deadline to edit
		Session session = HibernateUtil.getTransactionSession();
		Query queryDeadline = session.createQuery("from Deadline where id = :id").setParameter("id", id);
	  	Deadline deadline = (Deadline) queryDeadline.uniqueResult();	
	  	
	  	// If deadline not found
	  	if(deadline==null){
	  		throw new RedirectException("/app/#signapp/deadlines");
	  	}
	  	
		// Check that the current user owns the deadline, otherwise throw a redirect exception
	  	if(deadline.getOwner()!=currentUser){
	  		throw new RedirectException("/app/#signapp/deadlines");
	  	}
	  	
		return deadline.getDeadlineMap();		
	}
	
//	// Update
//	@PUT @Path("/{id}")
//	public void updateDeadline(@PathParam("id") int deadlineId,
//			@Form Deadline deadline) {
//		
//	}
//	
	
	// Delete
	@DELETE @Path("/{id}")
	public void deleteDeadline(@PathParam("id") int id) {
		
		// Delete the group object
		Session session = HibernateUtil.getTransactionSession();
		Query deadlineQuery = session.createQuery("from Deadline where id = :id");
		deadlineQuery.setParameter("id", id);
		Deadline deadline = (Deadline)deadlineQuery.uniqueResult();
		session.delete(deadline);
		log.info("Deadline id: " + id + "deleted");
		
		throw new RedirectException("/app/#signapp/deadlines");
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
		
		//Get matching group names.. O(n) each time.. is this too slow? maybe define the .equals and hashCode method?
		for(Group g : currentUser.getGroups()){
			if(g.getTitle().contains(x)){
				matches.add(ImmutableMap.of("group_id", g.getId(), "group_name", g.getTitle()));
			}
		}
		
		return matches;
	}
}