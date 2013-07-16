package uk.ac.cam.signups.controllers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.hibernate.Session;
import org.jboss.resteasy.annotations.Form;

import uk.ac.cam.signups.forms.EventForm;
import uk.ac.cam.signups.models.*;
import uk.ac.cam.signups.util.HibernateUtil;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;
import com.googlecode.htmleasy.ViewWith;

@Path("/events")
public class EventsController extends ApplicationController {
	
	private User currentUser;
	
	// New
	@GET @Path("/new") @ViewWith("/soy/events.new")
	public Map<String, Object> newEvent() { 
		return ImmutableMap.of();
	}
	
	// Create
	@POST @Path("/")
	public void createEvent(@Form EventForm eventForm) {
		int id = eventForm.handle(initialiseUser());
		
		throw new RedirectException("/events/" + id);
	}
	
	// Show
	@GET @Path("/{id}") @ViewWith("/soy/events.show")
	public Map<String, Object> showEvent(@PathParam("id") int id){
		Session session = HibernateUtil.getTransaction();
		Event event = (Event) session.createQuery("FROM Event WHERE id = :id").setParameter("id", id).uniqueResult();
		session.getTransaction().commit();
		
		return event.toImmutableMap();
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