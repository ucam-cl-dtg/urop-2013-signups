package uk.ac.cam.signups.controllers;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.Form;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.ViewWith;

@Path("/events")
public class EventsController {
	
	@GET @Path("/new") @ViewWith("/soy/events.new")
	public Map newEvent() { 
		return ImmutableMap.of();
	}
	
	@POST @Path("/")
	public void createEvent(@Form Event event) {
		
	}
	
	@GET @Path("/{id}") @ViewWith("/soy/events.show")
	public Map showEvent(@PathParam("id") int id){
		return ImmutableMap.of();
	}
}