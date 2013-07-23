package uk.ac.cam.signups.controllers;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;
import org.jboss.resteasy.annotations.Form;

import java.util.List;
import java.util.Map;

import uk.ac.cam.signups.forms.EventForm;
import uk.ac.cam.signups.forms.FillSlot;
import uk.ac.cam.signups.models.*;
import uk.ac.cam.signups.util.HibernateUtil;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;

@Path("/signapp/events")
public class EventsController extends ApplicationController {
	
	
	// New
	@GET @Path("/new") @Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> newEvent() { 
		return ImmutableMap.of();
	}
	
	@POST @Path("/queryTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImmutableMap<String, ?>> generateTypeSuggestions(String q) {
		q = q.substring(2);
		
		return Type.findSimilar(q, initialiseUser(), "global");
	}
	
	// Create
	@POST @Path("/")
	public void createEvent(@Form EventForm eventForm) {
		int id = eventForm.handle(initialiseUser());
		
		throw new RedirectException("/app/#signapp/events/" + id);
	}
	
	// Show
	@GET @Path("/{id}") @Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> showEvent(@PathParam("id") int id){
		Session session = HibernateUtil.getTransactionSession();
		Event event = (Event) session.createQuery("FROM Event WHERE id = :id").setParameter("id", id).uniqueResult();
		
		return event.toMap();
	}
	
	// Fill Slot
	@POST @Path("/{id}/fill_slot")
	public void fillSlot(@PathParam("id") int id, @Form FillSlot fillSlot) {
		fillSlot.handle(id);

		throw new RedirectException("/events/" + id);
	}
	
	// Delete
	@DELETE @Path("/{id}")
	public void deleteEvent(@PathParam("id") int id) {
		
		throw new RedirectException("/");
	}
}