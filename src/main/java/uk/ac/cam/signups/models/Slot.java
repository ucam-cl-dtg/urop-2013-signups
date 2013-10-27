package uk.ac.cam.signups.models;

import java.util.Date;
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
import uk.ac.cam.signups.util.Signups;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name = "SLOTS")
public class Slot implements Mappable {
	@Transient
	private Logger logger = LoggerFactory.getLogger(Slot.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logIdSeq")
	@SequenceGenerator(name = "logIdSeq", sequenceName = "LOG_SEQ", allocationSize = 1)
	private int id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROW_ID")
	private Row row;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_CRSID")
	private User owner;

	public Slot() {
	}

	public Slot(Row row) {
		this.row = row;
	}

	public Slot(int id, Row row, User owner) {
		this.id = id;
		this.row = row;
		this.owner = owner;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Row getRow() {
		return this.row;
	}

	public void setRow(Row row) {
		this.row = row;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	@Override
	public Map<String, ?> toMap(User currentUser) {
		ImmutableMap<String, ?> rOwner;
		if (owner != null) {
			rOwner = (ImmutableMap<String, ?>) owner.toMap(currentUser);
		} else {
			rOwner = ImmutableMap.of("crsid", "", "name", "");
		}
		return ImmutableMap.of("id", id, "owner", rOwner,"isupdateable",isUpdateable(currentUser));
	}

	public void destroy(NotificationApiWrapper apiWrapper) {
		if (getOwner() != null) {
			Row row = this.getRow();
			Event event = row.getEvent();
			String sheetType = event.getSheetType();
			Date currentTime = new Date();
			Date targetTime = sheetType.equals(Event.SHEETTYPE_DATETIME) ? row
					.getTime() : event.getExpiryDate();

			if (currentTime.compareTo(targetTime) < 0) {
				String message = event.getTitle() + " by "
						+ event.getOwner().getCrsid() + " has been canceled.";
				try {
					apiWrapper.createNotification(message, Signups.APPLICATION_NAME, "events",
							getOwner().getCrsid());
				} catch (NotificationException e) {
					logger.error("Notification could not be created.");
					logger.error(e.getMessage());
				}
			}
		}

		Session session = HibernateUtil.getInstance().getSession();
		session.delete(this);
	}

	public boolean isUpdateable(User currentUser) {
		
		Event event = getRow().getEvent();
		User eventOwner = event.getOwner();
		User slotOwner = this.getOwner();
		
		// if the currentUser owns the event they can do what they like
		if (currentUser.equals(eventOwner))
			return true;
	
		// if the event is not in freeform mode then this slot is only editable
		// if its not booked already or if its booked by the acting user
		if (!event.isFreeformEditable()) {
			if (slotOwner != null && !slotOwner.equals(currentUser))
				return false;
		}
	
		Date currentTime = new Date();
	
		// if the event has closed then don't allow edits
		if (currentTime.after(event.getExpiryDate()))
			return false;
	
		// if the slot time has passed then don't allow edits
		if (Event.SHEETTYPE_DATETIME.equals(event.getSheetType())) {
			if (currentTime.after(getRow().getTime()))
				return false;
		}
		return true;
	}
}
