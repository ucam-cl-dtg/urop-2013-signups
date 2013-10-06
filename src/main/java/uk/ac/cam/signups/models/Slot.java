package uk.ac.cam.signups.models;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.NotificationApiWrapper;
import uk.ac.cam.cl.dtg.teaching.api.NotificationException;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name="SLOTS")
public class Slot implements Mappable {
	@Transient
	private Logger logger = LoggerFactory.getLogger(Slot.class);
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="logIdSeq") 
	@SequenceGenerator(name="logIdSeq",sequenceName="LOG_SEQ", allocationSize=1)
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ROW_ID")
	private Row row;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="USER_CRSID")
	private User owner;
	
	public Slot() {}
	public Slot(Row row) {
		this.row = row;
	}
	public Slot(int id, Row row, User owner) {
		this.id = id;
		this.row = row;
		this.owner = owner;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public Row getRow() { return this.row; }
	public void setRow(Row row) { this.row = row; }
	
	public User getOwner() { return this.owner; }
	public void setOwner(User owner) { this.owner = owner; }

  public Map<String, ?> toMap() {
  	ImmutableMap<String, ?> rOwner;
  	if (owner != null) {
  		rOwner = (ImmutableMap<String, ?>) owner.toMap();
  	} else {
  		rOwner = ImmutableMap.of("crsid", "", "name", "");
  	}
	  return ImmutableMap.of("id",id,"owner", rOwner);
  }
  
  public void destroy(NotificationApiWrapper apiWrapper) {
  	if (getOwner() != null) {
	  	Row row = this.getRow();
	  	Event event = row.getEvent();
	  	String sheetType = event.getSheetType();
	  	boolean sendNotification = false;
	  	Calendar currentTime = new GregorianCalendar();
	  	
	  	if (sheetType.equals("datetime")) {
	  		if (currentTime.compareTo(row.getCalendar()) < 0)
	  			sendNotification = true;
	  	} else if (sheetType.equals("manual")){
	  		if (currentTime.compareTo(event.getExpiryDate()) < 0)
	  			sendNotification = true;
	  	}
	  	
	  	if (sendNotification) {
	  		String message = event.getTitle() + " by " + event.getOwner().getCrsid() + " has been canceled.";
		  	try {
		      apiWrapper.createNotification(message, "signapp", "events", getOwner().getCrsid());
	      } catch (NotificationException e) {
	      	logger.error("Notification could not be created.");
	      	logger.error(e.getMessage());
	      }
	  	}
	  }
  	
  	Session session = HibernateUtil.getInstance().getSession();
  	session.delete(this);
  }
}
