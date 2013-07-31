package uk.ac.cam.signups.forms;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import uk.ac.cam.signups.models.*;
import uk.ac.cam.signups.util.HibernateUtil;

import javax.ws.rs.FormParam;

public class EventForm {
	@FormParam("location") String location;
	@FormParam("room") String room;
	@FormParam("title") String title;
	@FormParam("types") String typeNames;
	@FormParam("n_of_columns") int nOfColumns;
	@FormParam("n_of_rows") int nOfRows;
	@FormParam("row_type") String rowType;
	@FormParam("available_dates[]") String[] availableDates;
	@FormParam("available_hours[]") String[] availableHours;
	@FormParam("available_minutes[]") String[] availableMinutes;
	
	Logger log = LoggerFactory.getLogger(EventForm.class);
	
	public int handle(User currentUser) {		
		Session session = HibernateUtil.getTransactionSession();
		// Create event prototype
		Event event = new Event();
		event.setLocation(location);
		event.setRoom(room);
		event.setTitle(title);
		event.setSheetType(rowType);

		// Set owner of the user to current user
		event.setOwner(currentUser);
		session.save(event);

		// Set types
		Type type = null;
		String[] types = typeNames.split(",");
		for(String stype: types){
			type = new Type(stype);
			type.setEvent(event);
			session.save(type);
		}
		
		// Create rows and associated slots
		Row row;
		if (rowType.equals("manual")) {
			for(int i = 0; i < nOfRows; i++) {
				row = new Row(event);
				if (types.length == 1)
					row.setType(type);
				session.save(row);
				Slot slot;
				for(int j = 0; j < nOfColumns; j++) {
					slot = new Slot(row);
					session.save(slot);
				}
			}
		} else if (rowType.equals("datetime")) {
			Calendar cal;
			Set<Calendar> duplicateCalContainer = new HashSet<Calendar>(); // To keep track of added dates to avoid duplicates
			MAIN_LOOP:
			for(int i = 0; i < availableDates.length; i++) {
				// Create calendar object and parse parameters
				String[] splitDate = availableDates[i].split("/");
				int year = Integer.parseInt(splitDate[2]);
				int month = Integer.parseInt(splitDate[1]) - 1;
				int day = Integer.parseInt(splitDate[0]);
				cal = new GregorianCalendar(year, month, day, Integer.parseInt(availableHours[i]), Integer.parseInt(availableMinutes[i]));
				
				// Skip duplicates
				if (duplicateCalContainer.contains(cal))
					continue MAIN_LOOP;
				
				duplicateCalContainer.add(cal);
				
				row = new Row(cal, event);
				
				// Set type for rows if there is only one type for the event
				if (types.length == 1)
					row.setType(type);
				session.save(row);
				
				// Create slots
				Slot slot;
				for(int j = 0; j < nOfColumns; j++) {
					slot = new Slot(row);
					session.save(slot);
				}
			}		
		}
		
		return event.getId();
	}
}