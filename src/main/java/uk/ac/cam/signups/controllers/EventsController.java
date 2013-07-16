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
import javax.ws.rs.FormParam;

import org.hibernate.Session;
import org.jboss.resteasy.annotations.Form;

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
	public void createEvent(@Form Event event,
													@FormParam("types[]") String[] types,
													@FormParam("n_of_columns") int nOfColumns,
													@FormParam("n_of_rows") int nOfRows,
													@FormParam("row_type") String rowType,
													@FormParam("available_dates[]") String[] availableDates,
													@FormParam("available_hours[]") String[] availableHours,
													@FormParam("available_minutes[]") String[] availableMinutes) {

		// Set owner of the user to current user
		currentUser = initialiseUser();
		event.setOwner(currentUser);
		
		// Create rows and associated slots
		Set<Row> rows = new HashSet<Row>();
		Row row;
		if (rowType == "manual") {
			for(int i = 0; i < nOfRows; i++) {
				row = new Row();
				Set<Slot> slots = new HashSet<Slot>(0);
				for(int j = 0; j < nOfColumns; j++)
					slots.add(new Slot());
				row.setSlots(slots);
				rows.add(row);
			}
		} else if (rowType == "datetime") {
			Calendar cal;
			for(int i = 0; i > availableDates.length; i++) {
				String[] splitDate = availableDates[i].split("/");
				int year = Integer.parseInt(splitDate[2]);
				int month = Integer.parseInt(splitDate[1]);
				int day = Integer.parseInt(splitDate[0]);
				cal = new GregorianCalendar(year, month, day, Integer.parseInt(availableHours[i]), Integer.parseInt(availableMinutes[i]));
				row = new Row(cal);
				Set<Slot> slots = new HashSet<Slot>(0);
				for(int j = 0; j < nOfColumns; j++)
					slots.add(new Slot());
				row.setSlots(slots);
				rows.add(row);
			}		
		}
		event.setRows(rows);
		
		// Set types and create them if non-existent
		Type type;
		for(String stype: types) {
		}
		
		throw new RedirectException("/events/" + event.getId());
	}
	
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