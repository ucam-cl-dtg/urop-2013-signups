package uk.ac.cam.signups.controllers;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.Form;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;

@Path("/events/{id}/deadlines")
public class DeadlinesController {
	@PathParam("id") int id;
	
	// Index 
	@GET @Path("/") @ViewWith("deadlines.index")
	public Map indexDeadlines() {
		return ImmutableMap.of();
	}
	
	// New
	@GET @Path("/new") @ViewWith("deadlines.new")
	public Map newDeadline() {
		return ImmutableMap.of()
	}
	
	// Create 
	@POST @Path("/") 
	public void createDeadline(@Form Deadline deadline) {
		
		throw new RedirectException("/" + deadline.getId());
	}
	
	// Edit
	@GET @Path("/{id}/edit") @ViewWith("/soy/deadlines.edit")
	public Map editDeadline(@PathParam("id") int deadlineId) {
		
		return ImmutableMap.of();
	}
	
	// Update
	@PUT @Path("/{id}")
	public void updateDeadline(@PathParam("id") int deadlineId,
			@Form Deadline deadline) {
		
	}
	
	// Delete
	@DELETE @PathParam("/{id}")
	public void deleteDeadline(@PathParam("id") int deadlineId) {
		
		throw new RedirectException("/events/" + id + "/deadlines");
	}
}