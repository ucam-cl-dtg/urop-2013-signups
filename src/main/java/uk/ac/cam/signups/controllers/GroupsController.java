package uk.ac.cam.signups.controllers;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.Form;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;

@Path("/groups")
public class GroupsController {
	
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
}