package uk.ac.cam.signups.controllers;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;

import org.hibernate.Session;
import org.jboss.resteasy.annotations.Form;

import uk.ac.cam.signups.forms.EventForm;
import uk.ac.cam.signups.forms.FillSlot;
import uk.ac.cam.signups.helpers.LDAPQueryHelper;
import uk.ac.cam.signups.models.Event;
import uk.ac.cam.signups.models.Type;
import uk.ac.cam.signups.util.HibernateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/signapp/events")
public class EventsController extends ApplicationController {

	// New
	@GET
	@Path("/new")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> newEvent() {
		return ImmutableMap.of();
	}

	// Query types
	@POST
	@Path("/queryTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImmutableMap<String, ?>> generateTypeSuggestions(String q) {
		return Type.findSimilar(q.substring(2), initialiseUser(), "global");
	}

	// Create
	@POST
	@Path("/")
	public void createEvent(@Form EventForm eventForm) {
		int id = eventForm.handle(initialiseUser());

		throw new RedirectException("/app/#signapp/events/" + id);
	}

	// Show
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> showEvent(@PathParam("id") int id) {
		Session session = HibernateUtil.getTransactionSession();
		Event event = (Event) session.createQuery("FROM Event WHERE id = :id")
		    .setParameter("id", id).uniqueResult();

		return event.toMap();
	}

	// Find users
	@POST
	@Path("/queryCRSID")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImmutableMap<String, String>> queryCRSId(String q) {
		// Perform LDAP search
		@SuppressWarnings("unchecked")
    ArrayList<ImmutableMap<String, String>> matches = (ArrayList<ImmutableMap<String, String>>) LDAPQueryHelper.queryCRSID(q.substring(2));

		return matches;
	}

	// Fill Slot
	@POST
	@Path("/{id}/fill_slots")
	public void fillSlot(@PathParam("id") int id, @Form FillSlot fillSlot) {
		fillSlot.handle(id);

		throw new RedirectException("/app/#signapp/events/" + id);
	}

	// Delete
	@DELETE
	@Path("/{id}")
	public void deleteEvent(@PathParam("id") int id) {

		throw new RedirectException("/");
	}
}