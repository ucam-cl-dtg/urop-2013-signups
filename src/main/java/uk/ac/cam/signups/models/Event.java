package uk.ac.cam.signups.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.teaching.api.ItemNotFoundException;
import uk.ac.cam.cl.dtg.teaching.api.Mapper;
import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.Notification;
import uk.ac.cam.cl.dtg.teaching.api.NotificationApi.NotificationApiWrapper;
import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;
import uk.ac.cam.signups.util.ImmutableMappableExhaustedPair;
import uk.ac.cam.signups.util.Signups;
import uk.ac.cam.signups.util.Util;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name = "EVENTS")
public class Event implements Mappable {

	public static final String SHEETTYPE_MANUAL = "manual";

	public static final String SHEETTYPE_DATETIME = "datetime";

	@Transient
	private Logger logger = LoggerFactory.getLogger(Event.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logIdSeq")
	@SequenceGenerator(name = "logIdSeq", sequenceName = "LOG_SEQ", allocationSize = 1)
	private int id;

	private String location;
	private String room;
	private String title;

	/**
	 * Indicates whether we have times attached to slots or just slots on their
	 * own.
	 */
	private String sheetType;
	private Date expiryDate;

	@Index(name = "obfuscatedIdIndex")
	private String obfuscatedId;

	@Column(name = "dos_visibility", nullable = false, columnDefinition = "boolean default true")
	private boolean dosVisibility;

	@Column(name = "freeform_editable", nullable = false, columnDefinition = "boolean default true")
	private boolean freeformEditable;

	public boolean isFreeformEditable() {
		return freeformEditable;
	}

	public void setFreeformEditable(boolean freeformEditable) {
		this.freeformEditable = freeformEditable;
	}

	@ManyToOne
	@JoinColumn(name = "USER_CRSID")
	private User owner;

	private String description;

	/**
	 * The slots which make up the event
	 */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
	@Sort(type = SortType.NATURAL)
	@OrderBy("time")
	private SortedSet<Row> rows;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
	@OrderBy("id")
	private Set<Type> types;

	public Event() {
	}

	public Event(int id, String location, String title, User owner,
			Set<Row> rows, Set<Type> types) {
		this.id = id;
		this.location = location;
		this.owner = owner;
		this.types.addAll(types);
		this.rows.addAll(rows);
		this.title = title;
	}

	public ImmutableMappableExhaustedPair<uk.ac.cam.signups.models.Notification> getNotifications(
			NotificationApiWrapper getApiWrapper, int page) {
		Set<Notification> notificationsSet = getApiWrapper
				.getNotificationsWithForeignId(page * 10, 10, Signups.APPLICATION_NAME,
						this.getOwner().getCrsid(), "signups-" + this.getId())
				.getNotifications();
		SortedSet<uk.ac.cam.signups.models.Notification> notificationsList = new TreeSet<uk.ac.cam.signups.models.Notification>();
		for (Notification notification : notificationsSet) {
			notificationsList.add(new uk.ac.cam.signups.models.Notification(
					notification.getId(), notification.getMessage(),
					notification.getTimestamp()));
		}

		boolean exhausted = false;

		if (notificationsSet.size() % 10 != 0) {
			exhausted = true;
		} else if (getApiWrapper
				.getNotificationsWithForeignId(page * 10 + 10, 1, Signups.APPLICATION_NAME,
						this.getOwner().getCrsid(), "signups-" + this.getId())
				.getNotifications().size() < 1) {
			exhausted = true;
		}

		return new ImmutableMappableExhaustedPair<uk.ac.cam.signups.models.Notification>(
				notificationsList, exhausted);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getObfuscatedId() {
		return obfuscatedId;
	}

	public void setObfuscatedId(String obfuscatedId) {
		this.obfuscatedId = obfuscatedId;
	}

	public boolean getDosVisibility() {
		return dosVisibility;
	}

	public void setDosVisibility(boolean dosVisibility) {
		this.dosVisibility = dosVisibility;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSheetType() {
		return this.sheetType;
	}

	public void setSheetType(String sheetType) {
		this.sheetType = sheetType;
	}

	public Date getExpiryDate() {
		if (this.expiryDate == null) {
			if (SHEETTYPE_DATETIME.equals(this.sheetType)) {
				// offset the expiryDate by one ms to make sure any logic tests
				// for booking the last slot will still pass
				this.expiryDate = new Date(
						this.rows.last().getTime().getTime() + 1);
			} else {
				// this can only happen if the database is inconsistent
				final long oneHundredDays = 1000L*60*60*24*100;
				this.expiryDate = new Date(System.currentTimeMillis() + oneHundredDays);
			}
		}
		return this.expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public SortedSet<Row> getRows() {
		return this.rows;
	}

	public void addRows(SortedSet<Row> rows) {
		this.rows.addAll(rows);
	}

	public Set<Type> getTypes() {
		return this.types;
	}

	public void addTypes(Set<Type> types) {
		this.types.addAll(types);
	}
	

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("unchecked")
	public static List<ImmutableMap<String, String>> suggestRooms(
			String qbuilding, String qroom) {
		Session session = HibernateUtil.getInstance().getSession();
		Query q = session
				.createQuery("select distinct room from Event as event where lower(event.location) like :building and lower(event.room) like :room");
		List<String> suggestions = (List<String>) q
				.setParameter("building", "%" + qbuilding.toLowerCase() + "%")
				.setParameter("room", "%" + qroom.toLowerCase() + "%")
				.setMaxResults(10).list();
		List<ImmutableMap<String, String>> suggestionsMap = new ArrayList<ImmutableMap<String, String>>();
		for (String suggestion : suggestions)
			suggestionsMap.add(ImmutableMap.of("room", suggestion));

		return suggestionsMap;
	}

	@Override
	public Map<String, ?> toMap(User currentUser) {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder = builder.put("obfuscatedId", obfuscatedId);
		builder = builder.put("title", title);
		builder = builder.put("location", location);
		builder = builder.put("description", description == null ? "" : description);
		builder = builder.put("room", room == null ? "" : room);
		builder = builder.put("sheetType", sheetType);
		builder = builder.put("owner", owner.toMap(currentUser));
		builder = builder.put("types", Util.getImmutableCollection(types,currentUser));
		builder = builder.put("lastRow", rows.last().toMap(currentUser));
		builder = builder.put("allowFreeform",freeformEditable || currentUser.equals(owner));

		// Expiry date generator (pretty print and comparative)
		builder = builder.put("expiryDate", (Map<String, ?>) Mapper.map(getExpiryDate()));

		if (sheetType.equals(SHEETTYPE_DATETIME)) {
			// Map dates to sets of slots on that date
			SortedMap<Date, SortedSet<Row>> dateMap = new TreeMap<Date, SortedSet<Row>>();

			for (Row row : rows) {
				Date day = Util.convertToDay(row.getTime());
				SortedSet<Row> rowContainer = dateMap.get(day);
				if (rowContainer == null) {
					rowContainer = new TreeSet<Row>();
					dateMap.put(day, rowContainer);
				}
				rowContainer.add(row);
			}

			List<ImmutableMap<String, ?>> dates = new ArrayList<ImmutableMap<String, ?>>();
			for (Entry<Date, SortedSet<Row>> entry : dateMap.entrySet()) {
				Date key = entry.getKey();
				SortedSet<Row> value = entry.getValue();

				dates.add(ImmutableMap.of("date", Mapper.map(key), "rows",
						Util.getImmutableCollection(value, currentUser)));
			}

			builder = builder.put("dates", dates);
			builder = builder.put("rows",
					new ArrayList<ImmutableMap<String, ?>>());

		} else if (sheetType.equals(SHEETTYPE_MANUAL)) {
			builder = builder.put("dates",
					new ArrayList<ImmutableMap<String, ?>>());
			List<Map<String, ?>> immutableRows = Util
					.getImmutableCollection(rows,currentUser);
			builder = builder.put("rows", immutableRows);
		}

		Map<String, ?> eventMap = builder.build();
		return eventMap;
	}

	public static Event findById(String obfuscatedId)
			throws ItemNotFoundException {
		Session session = HibernateUtil.getInstance().getSession();
		Event event = (Event) session.createCriteria(Event.class)
				.add(Restrictions.eq("obfuscatedId", obfuscatedId))
				.uniqueResult();

		if (event == null) {
			throw new ItemNotFoundException("Failed to find event with ID "
					+ obfuscatedId);
		}

		return event;
	}

	public void destroy(NotificationApiWrapper apiWrapper) {
		for (Row row : this.getRows())
			row.destroy(apiWrapper);

		for (Type type : this.getTypes())
			type.destroy();

		Session session = HibernateUtil.getInstance().getSession();
		session.delete(this);
	}

	/**
	 * An event is active if the current time is before the deadline and there
	 * are some slots which are not in the past
	 */
	public boolean isActive() {
		Date currentTime = new Date();
		if (currentTime.after(getExpiryDate()))	return false;
		if (SHEETTYPE_DATETIME.equals(sheetType) && currentTime.after(rows.last().getTime())) return false;
		return true;
	}
}