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

			ImmutableMap<String, ?> errors = ImmutableMap.of("get", false, "auth", false, "noname", false, "noimport", false, "importsize", false);
			
			return ImmutableMap.of("crsid", currentUser.getCrsid(), "groups", currentUser.getGroupsMap(), "errors", errors);
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
		  	Map<String, ?> groupMap;
		  	
		  	// Error handling.. not great but works. can't use redirects because of template errors
		  	if(group==null){
		  		groupMap = ImmutableMap.of("id", -1, "name", "Group not found", "owner", currentUser.toMap(), "users", new HashSet<ImmutableMap<String,?>>());
		  	} else if(!group.getOwner().equals(currentUser)) {
		  		groupMap = ImmutableMap.of("id", -2, "name", "Group not found", "owner", currentUser.toMap(), "users", new HashSet<ImmutableMap<String,?>>());		  		
		  	} else {
		  		groupMap = group.toMap();
		  	}
		  	
			return groupMap;
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
		
		// Errors
		@GET @Path("/error/{type}") 
		@Produces(MediaType.APPLICATION_JSON)
		public Map groupErrors(@PathParam("type") int error){
			
			currentUser = initialiseUser();
			
			ImmutableMap<String, ?> errors = ImmutableMap.of("get", (error==1), "auth", (error==2), "noname", (error==3), "noimport", (error==4), "importsize", (error==5));

			return ImmutableMap.of("crsid", currentUser.getCrsid(), "groups", currentUser.getGroupsMap(), "errors", errors);
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