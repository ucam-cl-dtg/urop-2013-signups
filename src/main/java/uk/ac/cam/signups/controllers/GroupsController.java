package uk.ac.cam.signups.controllers;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;

import org.hibernate.Query;
import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.models.Group;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateSessionRequestFilter;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;

@Path("/groups")
public class GroupsController extends ApplicationController {
		// Create the logger
		private static Logger log = LoggerFactory.getLogger(GroupsController.class);
		
		// Index
		@GET @Path("/") @ViewWith("/soy/groups.index")
		public Map indexGroups() {
			return ImmutableMap.of();
		}
		
		// New
		@GET @Path("/new") @ViewWith("/soy/groups.new")
		public Map newGroup() {
			return ImmutableMap.of();
		}
		
		// Create
		@POST @Path("/") 
		public void createGroup(@Form Group group) {
			
			throw new RedirectException("/groups");
		}
		
		// Edit
		@GET @Path("/{id}/edit") @ViewWith("/soy/groups.edit")
		public Map editGroup(@PathParam("id") int id) {
			return ImmutableMap.of();
		}
		
		// Update
		@PUT @Path("/{id}")
		public void updateGroup(@PathParam("id") int id,
				@Form Group group) {
			
			throw new RedirectException("/");
		}
		
		// Destroy 
		@DELETE @Path("/{id}")
		public void deleteGroup(@PathParam("id") int id) {
			
			throw new RedirectException("/");
		}
		
		// Query database for groups
		public Set<Group> getUserGroups(){
			log.debug("begin hibernate session");
			session = HibernateSessionRequestFilter.openSession(request);
			session.beginTransaction();	
			// Are there any groups for this user?
			Query userQuery = session.createQuery("from User where id = :id").setParameter("id", crsid);
		  	User user = (User) userQuery.uniqueResult();
		  	Set<Group> groups = user.getGroupsMap();
			
		  	// Close hibernate session
		  	log.debug("closing hibernate session");
			session.getTransaction().commit();
			session.close();	
			return groups;
		}
}