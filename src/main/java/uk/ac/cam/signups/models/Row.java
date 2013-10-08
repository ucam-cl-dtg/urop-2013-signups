package uk.ac.cam.signups.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.NotificationApiWrapper;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
import uk.ac.cam.signups.util.Util;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name = "ROWS")
public class Row implements Mappable, Comparable<Row> {

	@Transient
	private Logger logger = LoggerFactory.getLogger(Row.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logIdSeq")
	@SequenceGenerator(name = "logIdSeq", sequenceName = "LOG_SEQ", allocationSize = 1)
	private int id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "ROW_DATE")
	private Date time;

	@ManyToOne
	@JoinColumn(name = "EVENT_ID")
	private Event event;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "row")
	@OrderBy("id")
	private Set<Slot> slots = new TreeSet<Slot>();

	@ManyToOne
	@JoinColumn(name = "TYPE_ID")
	private Type type;

	public Row() {
	}

	public Row(Event event) {
		this.event = event;
	}

	public Row(Date time, Event event) {
		this.time = time;
		this.event = event;
	}

	public Row(int id, Date time, Set<Slot> slots, Event event, Type type) {
		this.id = id;
		this.time = time;
		this.slots.addAll(slots);
		this.event = event;
		this.type = type;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getTime() {
		return this.time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Set<Slot> getSlots() {
		return this.slots;
	}

	public void addSlots(Set<Slot> slots) {
		this.slots.addAll(slots);
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Map<String, ?> toMap() {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		if (getEvent().getSheetType().equals("datetime")) {
			SimpleDateFormat comparativeFormatter = new SimpleDateFormat(
					"yyyy MM dd HH mm");
			String comparativeString = comparativeFormatter.format(this.time);
			ImmutableMap.Builder<String, Object> dateBuilder = new ImmutableMap.Builder<String, Object>();
			dateBuilder = dateBuilder.put("year",
					comparativeString.substring(0, 4));
			dateBuilder = dateBuilder.put("month",
					comparativeString.substring(5, 7));
			dateBuilder = dateBuilder.put("day",
					comparativeString.substring(8, 10));
			dateBuilder = dateBuilder.put("hour",
					comparativeString.substring(11, 13));
			dateBuilder = dateBuilder.put("minute",
					comparativeString.substring(14, 16));
			dateBuilder = dateBuilder.put("comparativeString",
					comparativeString);

			// Date display
			SimpleDateFormat formatter = new SimpleDateFormat(
					"EEEE, d MMMM 'at' kk:mm");
			String dateString = formatter.format(time);
			builder = builder.put("dateDisplay", dateString);

			builder = builder.put("date", dateBuilder.build());
		}

		builder = builder.put("slots", Util.getImmutableCollection(slots));
		if (type != null) {
			builder = builder.put("type", type.toMap());
		} else {
			builder = builder.put("type", "no-type");
		}

		builder.put("eventSummary", ImmutableMap.of("obfuscatedId",
				event.getObfuscatedId(), "title", event.getTitle(),
				"expiryDate", event.getExpiryDateMap()));
		return builder.build();
	}

	public int compareTo(Row row) {
		if (row.time != null) {
			return this.time.compareTo(row.time);
		} else {
			if (this.id > row.getId()) {
				return 1;
			} else if (this.id == row.getId()) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	public void destroy(NotificationApiWrapper apiWrapper) {
		for (Slot slot : this.getSlots())
			slot.destroy(apiWrapper);

		Session session = HibernateUtil.getInstance().getSession();
		session.delete(this);
	}

	public boolean isEmpty() {
		for (Slot slot : getSlots()) {
			if (slot.getOwner() != null)
				return false;
		}
		return true;
	}
}