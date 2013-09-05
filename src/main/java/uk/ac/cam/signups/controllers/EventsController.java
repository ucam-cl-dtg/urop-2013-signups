package uk.ac.cam.signups.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.UidGenerator;

import org.jboss.resteasy.annotations.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
import uk.ac.cam.signups.exceptions.AuthorizationException;
import uk.ac.cam.signups.exceptions.NotADosException;
import uk.ac.cam.signups.forms.EventForm;
import uk.ac.cam.signups.forms.FillSlot;
import uk.ac.cam.signups.models.Dos;
import uk.ac.cam.signups.models.Event;
import uk.ac.cam.signups.models.Row;
import uk.ac.cam.signups.models.Slot;
import uk.ac.cam.signups.models.Type;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.ImmutableMappableExhaustedPair;
import uk.ac.cam.signups.util.Util;

import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("/events")
public class EventsController extends ApplicationController {

	private Logger logger = LoggerFactory.getLogger(EventsController.class);

	/*
	 * CRUD and few other actions
	 */

	// Index
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> indexEvent() {
		Map<String, ?> signedUp = generateAssociatedRows(0, "contemporary");
		Map<String, ?> created = generateMyEvents(0);
		Map<String, ?> archived = generateAssociatedRows(0, "archive");
		Map<String, ?> noTime = generateAssociatedRows(0, "no-time");

		return ImmutableMap.of("eventsSignedUp", signedUp,
		    "eventsCreated", created, "eventsArchived", archived,
		    "eventsNoTime", noTime);
	}

	// Walker Zone
	@GET
	@Path("/dos")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> enterWalkerZone(@QueryParam("partial") String partial) {
		return queryPupils(0, partial);
	}
	
	// Walker Zone query for more students
	@GET
	@Path("/queryPupils")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> queryPupils(@QueryParam("page") int page, @QueryParam("partial") String partial) {
		User currentUser = initialiseUser();
		try {
			Dos currentDos = currentUser.getDos(getDashboardApiWrapper());

			ImmutableMappableExhaustedPair<User> pupils = null;
	
			if(partial == null) {
				pupils = currentDos.getPupils(page);
			} else {
				pupils = currentDos.getPupils(page, partial);
			}
			return ImmutableMap.of("pupils",
			    Util.getImmutableCollection(pupils.getMappableIterable()), "exhausted",
			    pupils.getExhausted());
		} catch(NotADosException e) {
			return ImmutableMap.of("error", e.getMessage());
		}
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

		if (errors.isEmpty()) {
			Event event = eventForm.handle(initialiseUser(), getNotificationApiWrapper());
			return ImmutableMap.of("redirectTo", "events/" + event.getObfuscatedId());
		} else {
			ImmutableMap<String, List<String>> actualErrors = Util
		    .multimapToImmutableMap(errors);
			return ImmutableMap.of("data", eventForm.toMap(), "errors", actualErrors);
		}
	}
	
	// Delete
	@DELETE
	@Path("/{obfuscatedId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, String> deleteEvent(@PathParam("obfuscatedId") String obfuscatedId) {
		Event event = Event.findById(obfuscatedId);

		try {
			authenticate(event.getOwner());
		} catch(AuthorizationException e) {
			logger.warn(e.getCrsid() + " tried to delete an event without owning it.");
			return ImmutableMap.of("error", "You cannot delete an event that you do not own.");
		}

		event.destroy(getNotificationApiWrapper());
			
		return ImmutableMap.of("redirectTo", "events");
	}

	// Fill Slot
	@POST
	@Path("/{obfuscatedId}/fillSlots")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> fillSlot(@Form FillSlot fillSlot, @PathParam("obfuscatedId") String obfuscatedId) {
		List<String> errors = fillSlot.validate();

		if (errors.isEmpty()) {
			fillSlot.handle(getNotificationApiWrapper(), initialiseUser());
			return ImmutableMap.of("redirectTo", "events/" + obfuscatedId);
		}	else {
			logger.error("Errorful area");
			return showEvent(obfuscatedId, errors);
		}
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
		    Util.getImmutableCollection(events.getMappableIterable()), "exhausted",
		    events.getExhausted());
	}

	// Query more rows created user is associated with
	public Map<String, ?> generateAssociatedRows(int page, String mode) {
		ImmutableMappableExhaustedPair<Row> rows = initialiseUser()
		    .getRowsSignedUp(page, mode);
		return ImmutableMap.of("data",
		    Util.getImmutableCollection(rows.getMappableIterable()), "exhausted",
		    rows.getExhausted());
	}

	// Query rows for individual's rows through DoS interface
	@GET
	@Path("/queryIndividualsEvents")
	public Map<String, ?> generateIndividualsRows(@QueryParam("page") int page,
	    @QueryParam("crsid") String crsid) {
		try {
			User cUser = initialiseUser();
			User u = initialiseSpecifiedUser(crsid);
			Dos currentDos = cUser.getDos(getDashboardApiWrapper());
			boolean isHisPupil = currentDos.isMyPupil(u);
			if (isHisPupil) {
				ImmutableMappableExhaustedPair<Row> rows = u.getRowsSignedUp(page,
				    "dos");
				return ImmutableMap.of("data",
				    Util.getImmutableCollection(rows.getMappableIterable()), "exhausted",
				    rows.getExhausted());
			} else {
				return ImmutableMap.of("error", "Not his pupil");
			}
		} catch (NotADosException e) {
			return ImmutableMap.of("error", e.getMessage());
		}
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
	public List<HashMap<String, String>> queryCRSID(@FormParam("q") String q) {
		// Perform LDAP search
		List<HashMap<String, String>> matches;
		try {
			matches = LDAPPartialQuery.partialUserByCrsid(q);
		} catch (LDAPObjectNotFoundException e) {
			return new ArrayList<HashMap<String, String>>();
		}

		return matches;
	}
	
	// Query CRSIDs of pupils
	@GET
	@Path("/queryPupilsCRSIDs")
	@Produces(MediaType.APPLICATION_JSON)
	public List<HashMap<String, String>> queryCRSIDsForDos(@QueryParam("q") String q) {
		User u = initialiseUser();
		try {
      return u.getDos(getDashboardApiWrapper()).getPupilCRSIDs(q);
    } catch (NotADosException e) {
    	return new ArrayList<HashMap<String, String>>();
    }
	}
	
	// Query for history of an event
	@GET
	@Path("/queryEventHistory")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> queryEventHistory(@QueryParam("id") String id, @QueryParam("page") int page) {
		Event event = Event.findById(id);
		ImmutableMappableExhaustedPair<uk.ac.cam.signups.models.Notification> nots = event.getNotifications(getNotificationApiWrapper(), page);
		
		return ImmutableMap.of(
				"list", Util.getImmutableCollection(nots.getMappableIterable()),
				"exhausted", nots.getExhausted());
	}

	//Show
	@GET
	@Path("/{obfuscatedId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, ?> showEvent(@PathParam("obfuscatedId") String obfuscatedId, @QueryParam("coolHack") List<String> errors) {
		Event event = Event.findById(obfuscatedId);
		
		if(errors == null) {
			errors = new ArrayList<String>();
		}

		return ImmutableMap.of(
				"isOwner", initialiseUser().equals(event.getOwner()),
				"data", event.toMap(), 
				"errors", errors,
				"notifications", queryEventHistory(obfuscatedId, 0)
				);
	}
	
	// Calendar
	@GET
	@Path("/calendar")
	@Produces("text/calendar")
	public Object getCalendar() throws SocketException, URISyntaxException {
		User cu = initialiseUser();

		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//OTT//OTT 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		// Handle events you have created
		List<Event> events = cu.getDatetimeEvents();
		for(Event event: events) {
			for(Row row: event.getRows()) {
				if (row.isEmpty())
					continue;
				
				java.util.Calendar cal = (java.util.Calendar) row.getCalendar().clone();
				Date start = cal.getTime();
				cal.add(java.util.Calendar.HOUR, 1);
				Date end = cal.getTime();
				Dur dur = new Dur(start,end);
				String subject = "OTTER: " + event.getTitle() 
													+ (row.getType() != null ? " (" + row.getType().getName() + ")" : "");
				UidGenerator ug = new UidGenerator("1");
				
				DateTime eventStart = new DateTime(start);
				VEvent ev = new VEvent(eventStart, dur, subject);
				
				Set<Slot> slots = row.getSlots();
				List<String> supervisees = new ArrayList<String>();
				for (Slot slot: slots) {
					User slotOwner = slot.getOwner();
					if (slotOwner != null)
						supervisees.add(slotOwner.getNameCrsid());
				}
				
				ev.getProperties().add(new Description("Event group: " + Util.join(supervisees, ", ")));
				ev.getProperties().add(ug.generateUid());
				calendar.getComponents().add(ev);
			}
		}
		
		// Handle events that you have joined
		List<Row> rows = cu.getRowsWithDatetimeSignedUp();
		for(Row row: rows) {
			java.util.Calendar cal = (java.util.Calendar) row.getCalendar().clone();
			Date start = cal.getTime();
			cal.add(java.util.Calendar.HOUR, 1);
			Date end = cal.getTime();
			Dur dur = new Dur(start,end);
			String subject = "OTTER: " + row.getEvent().getTitle() 
												+ (row.getType() != null ? " (" + row.getType().getName() + ")" : "");
			String hostMail = row.getEvent().getOwner().getCrsid() + "@cam.ac.uk";
			UidGenerator ug = new UidGenerator("1");
			
			DateTime eventStart = new DateTime(start);
			VEvent ev = new VEvent(eventStart, dur, subject);
			ev.getProperties().add(new Organizer("MAILTO:" + hostMail));
			ev.getProperties().add(ug.generateUid());
			
			Set<Slot> slots = row.getSlots();
			List<String> supervisees = new ArrayList<String>();
			for (Slot slot: slots) {
				User slotOwner = slot.getOwner();
				if (slotOwner != null)
					supervisees.add(slotOwner.getNameCrsid());
			}
			
			ev.getProperties().add(new Description("Event group: " + Util.join(supervisees, ", ")));
			calendar.getComponents().add(ev);	
		}
		
		if (rows.isEmpty() && events.isEmpty())
			return Response.status(401).build();
		
		ResponseBuilder builder = Response.ok(calendar.toString());
		builder.header("Content-Disposition",
				"attachment; filename=events_for_" + cu.getCrsid()
						+ ".ics");
		return builder.build();
	}
}