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
import uk.ac.cam.signups.helpers.LDAPQueryHelper;
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
		private User currentUser;
		
		// Index
		@GET @Path("/") 
		@Produces(MediaType.APPLICATION_JSON)
		public Map indexGroups() {

			currentUser = initialiseUser();

			return ImmutableMap.of("crsid", currentUser.getCrsid(), "groups", currentUser.getGroupsMap());
		}
		
		// Create
		@POST @Path("/") 
		public void createGroup(@Form GroupForm groupForm) throws Exception {
			
			currentUser = initialiseUser();

			int id= groupForm.handle(currentUser);
			
			throw new RedirectException("/app/#signapp/groups");
		}
		
		// Import from LDAP
		@POST @Path("/import") 
		public void importGroup(@Form GroupForm groupForm) throws Exception {
			
			currentUser = initialiseUser();

			int id= groupForm.handleImport(currentUser);
			
			throw new RedirectException("/app/#signapp/groups");
		}
		
		//Edit
		@GET @Path("/{id}/edit") //@ViewWith("/soy/groups.edit")
		@Produces(MediaType.APPLICATION_JSON)
		public Map editGroup(@PathParam("id") int id) {
			
			currentUser = initialiseUser();
			
		  	Group group = Group.getGroup(id);
		  	
		  	// If group not found
		  	if(group==null){
		  		throw new RedirectException("/app/#signapp/groups");
		  	}
		  	
			return group.toMap();
		}
		
		// Update
		@POST @Path("/{id}/update")
		public void updateGroup(@Form GroupForm groupForm, @PathParam("id") int id) {	
			
			currentUser = initialiseUser();
						
			id = groupForm.handleUpdate(currentUser, id);

			throw new RedirectException("/app/#signapp/groups");
		}
		
		//Destroy 
		@DELETE @Path("/{id}")
		public void deleteGroup(@PathParam("id") int id) {
			
			Group.deleteGroup(id);

			throw new RedirectException("/app/#signapp/groups");
		}
		
		//Error
		@GET @Path("/error/{type}")
		@Produces(MediaType.APPLICATION_JSON)
		public void groupError(String type){
			
		}
		
		// Find users
		@POST @Path("/queryCRSID")
		@Produces(MediaType.APPLICATION_JSON)
		public List queryCRSId(String q) {
			
			//Remove q= prefix
			String x = q.substring(2);
			
			// Perform LDAP search
			ArrayList<ImmutableMap<String,?>> matches = (ArrayList<ImmutableMap<String, ?>>) LDAPQueryHelper.queryCRSID(x);
			
			return matches;
		}
		
		// Find groups from LDAP
		@POST @Path("/queryGroup")
		@Produces(MediaType.APPLICATION_JSON)
		public List queryGroup(String q) {
			
			//Remove q= prefix
			String x = q.substring(2);
			
			// Perform LDAP search
			ArrayList<ImmutableMap<String,?>> matches = (ArrayList<ImmutableMap<String, ?>>) LDAPQueryHelper.queryGroups(x);
			
			return matches;
		}	
}