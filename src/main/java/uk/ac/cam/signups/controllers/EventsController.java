package uk.ac.cam.signups.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;

import org.hibernate.Session;
import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
import uk.ac.cam.signups.forms.EventForm;
import uk.ac.cam.signups.forms.FillSlot;
import uk.ac.cam.signups.models.Event;
import uk.ac.cam.signups.models.Row;
import uk.ac.cam.signups.models.Type;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.ImmutableMappableExhaustedPair;
import uk.ac.cam.signups.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/signapp/events")
public class EventsController extends ApplicationController {

	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(EventsController.class);

	/*
	 * CRUD and few other actions
	 */

	// Index
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> indexEvent() {
		ImmutableMappableExhaustedPair<Row> eventsSignedUp = initialiseUser()
		    .getRowsSignedUp(0, "contemporary");
		ImmutableMappableExhaustedPair<Row> eventsArchived = initialiseUser()
		    .getRowsSignedUp(0, "archive");
		ImmutableMappableExhaustedPair<Row> eventsNoTime = initialiseUser()
		    .getRowsSignedUp(0, "no-time");
		ImmutableMappableExhaustedPair<Event> eventsCreated = initialiseUser()
		    .getMyEvents(0);

		Map<String, ?> immutableSignedUp = ImmutableMap.of("data",
		    Util.getImmutableCollection(eventsSignedUp.getMappableList()),
		    "exhausted", eventsSignedUp.getExhausted());

		Map<String, ?> immutableCreated = ImmutableMap.of("data",
		    Util.getImmutableCollection(eventsCreated.getMappableList()),
		    "exhausted", eventsCreated.getExhausted());

		Map<String, ?> immutableArchived = ImmutableMap.of("data",
		    Util.getImmutableCollection(eventsArchived.getMappableList()),
		    "exhausted", eventsArchived.getExhausted());

		Map<String, ?> immutableNoTime = ImmutableMap.of("data",
		    Util.getImmutableCollection(eventsNoTime.getMappableList()),
		    "exhausted", eventsNoTime.getExhausted());

		return ImmutableMap.of("eventsSignedUp", immutableSignedUp,
		    "eventsCreated", immutableCreated, "eventsArchived", immutableArchived,
		    "eventsNoTime", immutableNoTime);
	}

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
		ImmutableMap<String, List<String>> actualErrors = Util
		    .multimapToImmutableMap(errors);

		if (errors.isEmpty()) {
			Event event = eventForm.handle(initialiseUser());
			return ImmutableMap.of("redirectTo", "signapp/events/" + event.getId());
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

		return ImmutableMap.of("data", event.toMap(), "errors",
		    ArrayListMultimap.create());
	}

	// Fill Slot
	@POST
	@Path("/{id}/fill_slots")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> fillSlot(@PathParam("id") int id,
	    @Form FillSlot fillSlot) {
		fillSlot.handle(id);

		return ImmutableMap.of("redirectTo", "signapp/events/" + id);
	}

	/*
	 * Queries for various suggestions and completions
	 */

	// Query delegator
	@GET
	@Path("/queryEvents")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> generateEvents(@QueryParam("page") int page,
	    @QueryParam("mode") String mode) {
		if (mode.equals("created")) {
			return generateMyEvents(page);
		} else if (mode.equals("archive") || mode.equals("no-time")
		    || mode.equals("contemporary")) {
			return generateAssociatedRows(page, mode);
		}

		return null;
	}

	// Query more events created inside events
	public Map<String, ?> generateMyEvents(int page) {
		ImmutableMappableExhaustedPair<Event> events = initialiseUser()
		    .getMyEvents(page);
		return ImmutableMap.of("data",
		    Util.getImmutableCollection(events.getMappableList()), "exhausted",
		    events.getExhausted());
	}

	// Query more rows created user is associated with
	public Map<String, ?> generateAssociatedRows(int page, String mode) {
		ImmutableMappableExhaustedPair<Row> rows = initialiseUser()
		    .getRowsSignedUp(page, mode);
		return ImmutableMap.of("data",
		    Util.getImmutableCollection(rows.getMappableList()), "exhausted",
		    rows.getExhausted());
	}

	// Query types
	@POST
	@Path("/queryTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImmutableMap<String, ?>> generateTypeSuggestions(
	    @FormParam("q") String q) {
		return Type.findSimilar(q, initialiseUser(), "global");
	}

	// Query rooms
	@GET
	@Path("/queryRooms")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ImmutableMap<String, String>> generateRoomSuggestions(
	    @QueryParam("qroom") String qroom,
	    @QueryParam("qbuilding") String qbuilding) {
		return Event.suggestRooms(qbuilding, qroom);
	}

	// Find users
	@POST
	@Path("/queryCRSID")
	@Produces(MediaType.APPLICATION_JSON)
	public List<HashMap<String, String>> queryCRSId(@FormParam("q") String q) {
		// Perform LDAP search
    List<HashMap<String, String>> matches;
    try {
	    matches = LDAPPartialQuery.partialUserByCrsid(q);
    } catch (LDAPObjectNotFoundException e) {
    	return new ArrayList<HashMap<String,String>>();
    }

		return matches;
	}
}