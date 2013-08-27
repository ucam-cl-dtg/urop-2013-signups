package uk.ac.cam.signups.models;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

public class Notification implements Mappable, Comparable<Notification> {
	private String message;
	private Calendar timestamp;

	private Logger logger = LoggerFactory.getLogger(Notification.class);
	
	public Notification(String message, String timestamp) {
		this.message = message;
		
		SimpleDateFormat formatter = new SimpleDateFormat("E M dd hh:mm:ss zzz yyyy");
		Calendar dummyCal = new GregorianCalendar();
		try {
	    dummyCal.setTime(formatter.parse(timestamp));
    } catch (ParseException e) {
    	logger.error("Could not properly parse your notification time.");
    	logger.error(e.getMessage());
    }
		this.timestamp = dummyCal;
	}
	
	@Override
  public int compareTo(Notification not) {
		return timestamp.compareTo(not.timestamp);
  }

	@Override
  public int getId() {
	  throw new UnsupportedOperationException();
  }

	@Override
  public Map<String, ?> toMap() {
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d kk:mm");

		return ImmutableMap.of(
				"message", this.message,
				"timestamp", formatter.format(this.timestamp.getTime()));
  }
}