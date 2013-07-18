package uk.ac.cam.signups.forms;

import org.hibernate.Session;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import uk.ac.cam.signups.models.*;
import uk.ac.cam.signups.util.HibernateUtil;

import javax.ws.rs.FormParam;

public class EventForm {
	@FormParam("location") String location;
	@FormParam("title") String title;
	@FormParam("types[]") String[] typeNames;
	@FormParam("n_of_columns") int nOfColumns;
	@FormParam("n_of_rows") int nOfRows;
	@FormParam("row_type") String rowType;
	@FormParam("available_dates[]") String[] availableDates;
	@FormParam("available_hours[]") String[] availableHours;
	@FormParam("available_minutes[]") String[] availableMinutes;
	
	public int handle(User currentUser) {		
		Session session = HibernateUtil.getTransaction();
		// Create event prototype
		Event event = new Event();
		event.setLocation(location);
		event.setTitle(title);

		// Set owner of the user to current user
		event.setOwner(currentUser);
		session.save(event);
		
		// Create rows and associated slots
		Row row;
		if (rowType.equals("manual")) {
			for(int i = 0; i < nOfRows; i++) {
				row = new Row(event);
				session.save(row);
				Slot slot;
				for(int j = 0; j < nOfColumns; j++) {
					slot = new Slot(row);
					session.save(slot);
				}
			}
		} else if (rowType.equals("datetime")) {
			Calendar cal;
			for(int i = 0; i < availableDates.length; i++) {
				String[] splitDate = availableDates[i].split("/");
				int year = Integer.parseInt(splitDate[2]);
				int month = Integer.parseInt(splitDate[1]);
				int day = Integer.parseInt(splitDate[0]);
				cal = new GregorianCalendar(year, month, day, Integer.parseInt(availableHours[i]), Integer.parseInt(availableMinutes[i]));
				row = new Row(cal, event);
				session.save(row);
				Slot slot;
				for(int j = 0; j < nOfColumns; j++) {
					slot = new Slot(row);
					session.save(slot);
				}
			}		
		}
		
		// Set types and create them if non-existent
		Set<Type> types = new HashSet<Type>(0);
		Type type;
		for(String stype: typeNames) {
			type = new Type(stype);
			session.save(type);
			types.add(type);
		}
		event.addTypes(types);
		
		session.getTransaction().commit();
		
		return event.getId();
	}
}