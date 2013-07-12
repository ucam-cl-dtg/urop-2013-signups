package uk.ac.cam.signups.controllers;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.Form;

import uk.ac.cam.signups.models.Event;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;

@Path("/events")
public class EventsController extends ApplicationController {
	
	// New
	@GET @Path("/new") @ViewWith("/soy/events.new")
	public Map<String, Object> newEvent() { 
		return ImmutableMap.of();
	}
	
	// Create
	/*
	@POST @Path("/")
	public void createEvent(@Form Event event) {
		
		throw new RedirectException("/events/" + event.getId());
	}
	*/
	
	// Show
	@GET @Path("/{id}") @ViewWith("/soy/events.show")
	public Map<String, Object> showEvent(@PathParam("id") int id){
		return ImmutableMap.of();
	}
	
	// Fill Slot
	@POST @Path("/{id}/fill_slot")
	public void fillSlot(@PathParam("id") int id) {
		
		throw new RedirectException("/events/" + id);
	}
	
	// Delete
	@DELETE @Path("/{id}")
	public void deleteEvent(@PathParam("id") int id) {
		
		throw new RedirectException("/");
	}
}