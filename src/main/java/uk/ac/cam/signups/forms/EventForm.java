package uk.ac.cam.signups.forms;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.models.Event;
import uk.ac.cam.signups.models.Row;
import uk.ac.cam.signups.models.Slot;
import uk.ac.cam.signups.models.Type;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.FormParam;

public class EventForm {
	@FormParam("location")
	String location;
	@FormParam("room")
	String room;
	@FormParam("title")
	String title;
	@FormParam("types")
	String typeNames;
	@FormParam("n_of_columns")
	int nOfColumns;
	@FormParam("n_of_rows")
	int nOfRows;
	@FormParam("sheet_type")
	String sheetType;
	@FormParam("available_dates[]")
	String[] availableDates;
	@FormParam("available_hours[]")
	String[] availableHours;
	@FormParam("available_minutes[]")
	String[] availableMinutes;

	ArrayListMultimap<String, String> errors;

	Logger log = LoggerFactory.getLogger(EventForm.class);

	public Event handle(User currentUser) {
		Session session = HibernateUtil.getTransactionSession();
		// Create event prototype
		Event event = new Event();
		event.setLocation(location);
		event.setRoom(room);
		event.setTitle(title);
		event.setSheetType(sheetType);

		// Set owner of the user to current user
		event.setOwner(currentUser);
		session.save(event);

		// Set types
		Type type = null;
		String[] types = typeNames.split(",");
		for (String stype : types) {
			type = new Type(stype);
			type.setEvent(event);
			session.save(type);
		}

		// Create rows and associated slots
		Row row;
		if (sheetType.equals("manual")) {
			for (int i = 0; i < nOfRows; i++) {
				row = new Row(event);
				if (types.length == 1)
					row.setType(type);
				session.save(row);
				Slot slot;
				for (int j = 0; j < nOfColumns; j++) {
					slot = new Slot(row);
					session.save(slot);
				}
			}
		} else if (sheetType.equals("datetime")) {
			Calendar cal;
			Set<Calendar> duplicateCalContainer = new HashSet<Calendar>(); // To keep
			                                                               // track of
			                                                               // added
			                                                               // dates to
			                                                               // avoid
			                                                               // duplicates
			MAIN_LOOP: for (int i = 0; i < availableDates.length; i++) {
				// Create calendar object and parse parameters
				String[] splitDate = availableDates[i].split("/");
				int year = Integer.parseInt(splitDate[2]);
				int month = Integer.parseInt(splitDate[1]) - 1;
				int day = Integer.parseInt(splitDate[0]);
				cal = new GregorianCalendar(year, month, day,
				    Integer.parseInt(availableHours[i] != null ? availableHours[i]
				        : "0"),
				    Integer.parseInt(availableMinutes[i] != null ? availableMinutes[i]
				        : "0"));

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
				for (int j = 0; j < nOfColumns; j++) {
					slot = new Slot(row);
					session.save(slot);
				}
			}
		}

		return event;
	}

	public ArrayListMultimap<String, String> validate() {
		errors = ArrayListMultimap.create();

		// Title
		if (title.equals("") || title == null) {
			errors.put("title", "Title field cannot be empty.");
		} else if (title.length() > 90) {
			errors.put("title", "Title length cannot be more than 90 characters.");
		}

		// Types
		if (typeNames.equals("") || typeNames == null) {
			errors.put("eventType", "At least one event type is needed.");
		} else {
			String[] types = typeNames.split(",");

			if (types.length > 20) {
				errors.put("eventType", "You cannot set more than 20 events.");
			}

			for (String type : types) {
				if (type.length() > 40) {
					errors.put("eventType",
					    "No event type can be more than 40 characters.");
					break;
				}
			}
		}

		// Location and room
		if (!(location.equals("") || location == null) && location.length() > 90) {
			errors
			    .put("location", "Location name cannot be more than 90 characters.");
		}

		if (!(location.equals("") || location == null) && room.length() > 90) {
			errors.put("room", "Room name cannot be more than 90 characters.");
		}

		// Number of columns
		if (nOfColumns < 1) {
			errors.put("columns", "Group size cannot be less than 1.");
		} else if (nOfColumns > 50) {
			errors.put("columns", "Group size cannot be more more than 50");
		}

		if (sheetType == null
		    || !(sheetType.equals("datetime") || sheetType.equals("manual"))) {
			errors.put("sheetType", "Sheet type should be selected.");
		} else {

			// Number of rows (MANUAL sheet type)
			if (sheetType.equals("manual")) {
				if (nOfRows < 1) {
					errors.put("manualRows", "Number of rows canot be less than 1.");
				} else if (nOfRows > 200) {
					errors.put("manualRows", "Number of rows cannot be more than 200.");
				}
			}

			// Number of rows (DATETIME sheet type)
			if (sheetType.equals("datetime")) {
				if (!((availableDates.length == availableHours.length) && (availableHours.length == availableMinutes.length))) {
					errors.put("datetimeRows",
					    "Number of dates, hours and minutes do not match.");
				}

				for (String availableDate : availableDates) {
					if (availableDate == "") {
						errors.put("datetime", "No date can be empty.");
						break;
					} else if (!availableDate.matches("\\d\\d\\/\\d\\d\\/\\d\\d\\d\\d")) {
						errors.put("datetime",
						    "Date field shoud be in the form of dd/mm/yy.");
						break;
					}
				}

				if (availableDates.length < 1) {
					errors.put("datetime",
					    "Number of time slots cannot be less than 200.");
				} else if (availableDates.length > 200) {
					errors.put("datetime",
					    "Number of time slots cannot be more than 200.");
				}
			}

			if (!errors.containsKey("datetime")) {
				Calendar currentTime = new GregorianCalendar();
				Calendar timeAtHand;
				for (int i = 0; i < availableDates.length; i++) {
					String[] date = availableDates[i].split("/");
					timeAtHand = new GregorianCalendar(Integer.parseInt(date[0]),
					    Integer.parseInt(date[1]), Integer.parseInt(date[2]),
					    Integer.parseInt(availableHours[i]),
					    Integer.parseInt(availableMinutes[i]));
					if (timeAtHand.compareTo(currentTime) > 0) {
						errors.put("datetime", "You cannot add a date that is in the past.");
						break;
					}
				}
			}
		}

		return errors;
	}

	public ImmutableMap<String, ?> toMap() {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder.put("location", location);
		builder.put("room", room == null ? "" : room);
		builder.put("title", title);
		builder.put("types", typeNames);
		builder.put("columns", nOfColumns);
		builder.put("manualRows", nOfRows);
		builder.put("sheetType", sheetType == null ? "" : sheetType);

		List<Map<String, String>> datetimes = new ArrayList<Map<String, String>>();
		for (int i = 0; i < availableDates.length; i++) {
			datetimes.add(ImmutableMap.of("date", availableDates[i], "hour",
			    availableHours[i], "minute", availableMinutes[i]));
		}

		builder.put("datetimes", datetimes);

		return builder.build();
	}
}
