package uk.ac.cam.signups.controllers;

import com.google.common.collect.ArrayListMultimap;
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
import uk.ac.cam.signups.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/signapp/events")
public class EventsController extends ApplicationController {
	
	/*
	 * CRUD and few other actions
	 */
	
	/*
	// Index
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, ?>> indexEvent() {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		List<ImmutableMap<String, ?>> eventsISignedUp = new ArrayList<ImmutableMap<String, ?>>();
		
	}
	*/

	// New
	@GET
	@Path("/new")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> newEvent() {
		return ImmutableMap.of("data", "undefined", "errors", "undefined");
	}

	// Create
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> createEvent(@Form EventForm eventForm) {
		ArrayListMultimap<String, String> errors = eventForm.validate();
		ImmutableMap<String, List<String>> actualErrors = Util.multimapToImmutableMap(errors);

		if (errors.isEmpty()) {
			Event event = eventForm.handle(initialiseUser());
			return ImmutableMap.of("redirectTo","signapp/events/" + event.getId());
		} else {
			return ImmutableMap.of("data", eventForm.toMap(), "errors", actualErrors);
		}
	}

	// Show
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> showEvent(@PathParam("id") int id) {
		Session session = HibernateUtil.getTransactionSession();
		Event event = (Event) session.createQuery("FROM Event WHERE id = :id")
		    .setParameter("id", id).uniqueResult();

		return ImmutableMap.of("data", event.toMap(), "errors", ArrayListMultimap.create());
	}
	
	//Fill Slot
	@POST
	@Path("/{id}/fill_slots")
	public void fillSlot(@PathParam("id") int id, @Form FillSlot fillSlot) {
		fillSlot.handle(id);

		throw new RedirectException("/app/#signapp/events/" + id);
	}
	
	/*
	 * Queries for various suggestions and completions
	 */
	
	// Query types
	@POST
	@Path("/queryTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImmutableMap<String, ?>> generateTypeSuggestions(String q) {
		return Type.findSimilar(q.substring(2), initialiseUser(), "global");
	}
	
	// Query rooms
	@GET
	@Path("/queryRooms")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImmutableMap<String, String>> generateRoomSuggestions(@QueryParam("qroom") String qroom, @QueryParam("qbuilding") String qbuilding) {
		return Event.suggestRooms(qbuilding, qroom);
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
}