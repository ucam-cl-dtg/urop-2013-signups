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
		System.exit(-1);
		// Create event prototype
		Event event = new Event();
		event.setLocation(location);
		event.setTitle(title);

		// Set owner of the user to current user
		event.setOwner(currentUser);
		
		// Create rows and associated slots
		Set<Row> rows = new HashSet<Row>();
		Row row;
		if (rowType.equals("manual")) {
			for(int i = 0; i < nOfRows; i++) {
				row = new Row();
				Set<Slot> slots = new HashSet<Slot>(0);
				Slot slot;
				for(int j = 0; j < nOfColumns; j++) {
					slot = new Slot();
					session.save(slot);
					slots.add(slot);
				}
				row.setSlots(slots);
				session.save(row);
				rows.add(row);
			}
		} else if (rowType.equals("datetime")) {
			Calendar cal;
			for(int i = 0; i > availableDates.length; i++) {
				String[] splitDate = availableDates[i].split("/");
				int year = Integer.parseInt(splitDate[2]);
				int month = Integer.parseInt(splitDate[1]);
				int day = Integer.parseInt(splitDate[0]);
				cal = new GregorianCalendar(year, month, day, Integer.parseInt(availableHours[i]), Integer.parseInt(availableMinutes[i]));
				row = new Row(cal);
				Set<Slot> slots = new HashSet<Slot>(0);
				Slot slot;
				for(int j = 0; j < nOfColumns; j++) {
					slot = new Slot();
					session.save(slot);
					slots.add(slot);
				}
				row.setSlots(slots);
				session.save(row);
				rows.add(row);
			}		
		}
		event.setRows(rows);
		
		// Set types and create them if non-existent
		Set<Type> types = new HashSet<Type>(0);
		Type type;
		for(String stype: typeNames) {
			type = new Type(stype);
			session.save(type);
			types.add(type);
		}
		event.setTypes(types);
		session.save(event);
		
		session.getTransaction().commit();
		
		return event.getId();
	}
}
