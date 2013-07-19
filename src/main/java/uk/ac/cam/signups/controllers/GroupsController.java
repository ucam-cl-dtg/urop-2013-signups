package uk.ac.cam.signups.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.forms.GroupForm;
import uk.ac.cam.signups.models.Group;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.LDAPProvider;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;

@Path("signapp/groups")
public class GroupsController extends ApplicationController {
		// Create the logger
		private static Logger log = LoggerFactory.getLogger(GroupsController.class);
		
		// Get current user from raven session
		private User user;
		
		// Index
		@GET @Path("/") 
		@Produces(MediaType.APPLICATION_JSON)
		public Map indexGroups() {
			// Initialise user
			user = initialiseUser();

			return ImmutableMap.of("crsid", user.getCrsid(), "groups", user.getGroupsMap());
		}
		
		// Create
		@POST @Path("/") 
		public void createGroup(@Form GroupForm groupForm) throws Exception {
			// Initialise user
			user = initialiseUser();

			int id= groupForm.handle(user);

			
			throw new RedirectException("/app/#signapp/groups");
		}
		
		// Find users
		@POST @Path("/queryCRSID")
		@Produces(MediaType.APPLICATION_JSON)
		public List queryCRSId(String q) {
			
			//Remove q= prefix
			String x = q.substring(2);
			System.out.println(x);
			
			// Perform LDAP search
			ArrayList<ImmutableMap<String,?>> matches = (ArrayList<ImmutableMap<String, ?>>) LDAPProvider.partialUserSearch(x);;
			
			return matches;
		}
		
		//Edit
		@GET @Path("/{id}/edit") //@ViewWith("/soy/groups.edit")
		@Produces(MediaType.APPLICATION_JSON)
		public Map editGroup(@PathParam("id") int id) {
			
			// Get the group to edit
			Session session = HibernateUtil.getTransactionSession();
			Query editGroup = session.createQuery("from Group where id = :id").setParameter("id", id);
		  	Group group = (Group) editGroup.uniqueResult();	
		  	
		  	// If group not found
		  	if(group==null){
		  		throw new RedirectException("/app/#signapp/groups");
		  	}
		  	
			// Check that the current user owns the group, otherwise throw a redirect exception
		  	
			// Create group map method in group model later
			return ImmutableMap.of("id", group.getId(), "name", group.getTitle(), "users", group.getUsersMap());
		}
		
		// Update
		@POST @Path("/{id}/update")
		public void updateGroup(@PathParam("id") int id,
				@Form GroupForm groupForm) {
			
			// Check that current user is the owner
			
			// Update the group
			
			
			throw new RedirectException("/app/#signapp/groups/"+id+"/edit");
		}
		
		//Destroy 
		@DELETE @Path("/{id}")
		public void deleteGroup(@PathParam("id") int id) {
			
			// Delete the group object
			Session session = HibernateUtil.getTransactionSession();
			Query groupQuery = session.createQuery("from Group where id = :id");
			groupQuery.setParameter("id", id);
			Group group = (Group)groupQuery.uniqueResult();
			session.delete(group);

			throw new RedirectException("/app/#signapp/groups");
		}
		
}