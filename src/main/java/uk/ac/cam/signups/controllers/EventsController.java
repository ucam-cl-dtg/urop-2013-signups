package uk.ac.cam.signups.controllers;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;

@Path("/events")
public class EventsController {
	
	@GET @Path("/new") @ViewWith("/soy/events.new")
	public Map newEvent() { 
		return ImmutableMap.of();
	}
	
}