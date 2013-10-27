package uk.ac.cam.signups.controllers;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import uk.ac.cam.cl.dtg.teaching.api.FormattedDate;
import uk.ac.cam.signups.models.Event;
import uk.ac.cam.signups.models.Row;
import uk.ac.cam.signups.models.Slot;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.Util;

public class SummaryIndex {

	public static class IndexedDay {
		private FormattedDate day;
		private List<IndexedEvent> events;

		public IndexedDay(Date date) {
			this.day = new FormattedDate(date);
			this.events = new LinkedList<IndexedEvent>();
		}

		public void sortIndexedEvents() {
			Collections.sort(events, new Comparator<IndexedEvent>() {
				@Override
				public int compare(IndexedEvent o1, IndexedEvent o2) {
					return o1.bookingStartTime.compareTo(o2.bookingStartTime);
				}				
			});
		}

		public FormattedDate getDay() {
			return day;
		}

		public List<IndexedEvent> getEvents() {
			return events;
		}
		
		public void add(IndexedEvent indexedEvent) {
			this.events.add(indexedEvent);
		}
		
		
	}
	
	public static class IndexedEvent {
		private String obfuscatedId;
		private String title;
		private FormattedDate bookingStartTime;
		private SortedSet<IndexedRow> rows;
		private boolean empty;
		
		public IndexedEvent(Event e) {
			this.obfuscatedId = e.getObfuscatedId();
			this.title = e.getTitle();
			this.bookingStartTime = new FormattedDate(e.getExpiryDate());
			this.rows = new TreeSet<IndexedRow>();
			this.empty = true;
		}
		
		public void add(IndexedRow r) {
			rows.add(r);
			if (r.rowTime != null && r.rowTime.compareTo(bookingStartTime) < 1) {
				bookingStartTime = r.rowTime;
			}
			if (!r.isEmpty()) {
				empty = false;
			}
		}

		public boolean isEmpty() {
			return empty;
		}
		
		public String getObfuscatedId() {
			return obfuscatedId;
		}

		public String getTitle() {
			return title;
		}

		public FormattedDate getBookingStartTime() {
			return bookingStartTime;
		}

		public SortedSet<IndexedRow> getRows() {
			return rows;
		}
		
		
	}

	public static class IndexedRow implements Comparable<IndexedRow> {
		private FormattedDate rowTime;
		private List<String> bookings;
		private int rowID;
		private boolean empty;
		
		public IndexedRow(Row row) {
			Date rowTimeValue = row.getTime();
			this.rowTime = rowTimeValue != null ? new FormattedDate(rowTimeValue) : null;
			this.bookings = new LinkedList<String>();
			this.rowID = row.getId();
			this.empty = true;
			for(Slot s : row.getSlots()) {
				User owner = s.getOwner();
				if (owner != null) {
					bookings.add(owner.getCrsid());
					empty = false;
				}
				else {
					// add a null for a placeholder so we know the slot exists and is empty
					bookings.add(null);
				}
			}
		}

		public FormattedDate getRowTime() {
			return rowTime;
		}

		public List<String> getBookings() {
			return bookings;
		}
		
		public boolean isEmpty() {
			return empty;
		}

		@Override
		public int compareTo(IndexedRow o) {
			long thisTime = rowTime == null ? 0 : rowTime.getTimeMillis();
			long thatTime = o.rowTime == null ? 0 : rowTime.getTimeMillis();
			
			int result = new Long(thisTime).compareTo(thatTime);
			if (result == 0) return new Integer(rowID).compareTo(o.rowID);
			return result;
		}
		
		
	}

	private Map<Date, IndexedDay> days = new TreeMap<Date, IndexedDay>(Collections.reverseOrder());

	private Map<Date, Map<String, IndexedEvent>> eventIndex = new HashMap<Date, Map<String, IndexedEvent>>();

	/**
	 * Build a list of days. For each day we have the list of events which have
	 * a booking in that day. And for each event we have the actual bookings.
	 */
	public SummaryIndex() {
	}

	public Collection<IndexedDay> getResult() {
		for(IndexedDay indexedDay : days.values()) {
			indexedDay.sortIndexedEvents();
		}
		return days.values();
	}
	
	public void addAll(Collection<Row> rows) {
		for(Row r : rows) {
			addRow(r);
		}
	}
	
	public void addRow(Row row) {
		Date rowDay = getDay(row);
		IndexedEvent indexedEvent = addEvent(row.getEvent(),rowDay);
		indexedEvent.add(new IndexedRow(row));
	}
	
	public IndexedEvent addEvent(Event event, Date day) {		
		IndexedDay indexedDay = days.get(day);
		if (indexedDay == null) {
			indexedDay = new IndexedDay(day);
			days.put(day, indexedDay);
			eventIndex.put(day, new HashMap<String, IndexedEvent>());
		}
		
		IndexedEvent indexedEvent = eventIndex.get(day).get(
				event.getObfuscatedId());
		if (indexedEvent == null) {
			indexedEvent = new IndexedEvent(event);
			eventIndex.get(day).put(event.getObfuscatedId(),indexedEvent);
			indexedDay.add(indexedEvent);
		}
		return indexedEvent;
	}
	
	private Date getDay(Row row) {
		Date rowTime = row.getTime();
		if (rowTime == null)
			rowTime = row.getEvent().getExpiryDate();
		Date rowDay = Util.convertToDay(rowTime);
		return rowDay;
	}

}
