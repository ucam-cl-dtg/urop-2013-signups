package uk.ac.cam.signups.models;

import com.google.common.collect.ImmutableMap;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
import uk.ac.cam.cl.dtg.ldap.LDAPQueryManager;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.ImmutableMappableExhaustedPair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "USERS")
public class User {
	@Transient
	private Logger logger = LoggerFactory.getLogger(User.class);

	@Id
	private String crsid;

	@OneToMany(mappedBy = "owner")
	private List<Event> events = new ArrayList<Event>();

	@OneToMany(mappedBy = "owner")
	private Set<Slot> slots = new HashSet<Slot>(0);

	public User() {
	}

	public User(String crsid) {
		this.crsid = crsid;
	}

	public String getName() {
		try {
	    return LDAPQueryManager.getUser(crsid).getcName();
    } catch (LDAPObjectNotFoundException e) {
    	return "John Doe";
    }
	}

	public String getCrsid() {
		return crsid;
	}

	public void setCrsid(String crsid) {
		this.crsid = crsid;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void addEvents(List<Event> events) {
		this.events.addAll(events);
	}

	@SuppressWarnings("unchecked")
	public ImmutableMappableExhaustedPair<Row> getRowsSignedUp(int page,
	    String mode) {
		// Process page into offset
		int offset = page * 10;

		// Query rows that the user has signed up
		Session session = HibernateUtil.getTransactionSession();
		Calendar now = new GregorianCalendar();
		Criteria q = session.createCriteria(Row.class)
		    .createAlias("slots", "slots").createAlias("event", "event")
		    .add(Restrictions.eq("slots.owner", this));
		if (mode.equals("contemporary")) {
			q = q.add(Restrictions.ge("calendar", now)).addOrder(
			    Order.asc("calendar"));
		} else if (mode.equals("archive")) {
			q = q.add(Restrictions.lt("calendar", now)).addOrder(
			    Order.desc("calendar"));
		} else if (mode.equals("no-time")) {
			q = q.add(Restrictions.eq("event.sheetType", "manual")).addOrder(
			    Order.desc("id"));
		}

		// Check if the row list is exhausted
		List<Row> rows = (List<Row>) q.setMaxResults(10).setFirstResult(offset)
		    .list();

		Boolean exhausted = false;
		if (rows.size() % 10 != 0) {
			exhausted = true;
		} else if (q.setFirstResult(offset + 10).setMaxResults(1).list().size() == 0) {
			exhausted = true;
		}

		return new ImmutableMappableExhaustedPair<Row>(rows, exhausted);
	}

	@SuppressWarnings("unchecked")
	public ImmutableMappableExhaustedPair<Event> getMyEvents(int page) {
		// Process page into offset
		int offset = page * 10;

		// Query events the user has created
		Session session = HibernateUtil.getTransactionSession();
		Query q = session
		    .createQuery(
		        "from Event as event where event.owner = :user order by id desc")
		    .setParameter("user", this).setFirstResult(offset).setMaxResults(10);

		// Check if the events list is exhausted
		Boolean exhausted = false;
		if (session.createQuery("from Event as event where event.owner = :user")
		    .setParameter("user", this).setFirstResult(offset + 10)
		    .setMaxResults(1).list().isEmpty())
			exhausted = true;

		return new ImmutableMappableExhaustedPair<Event>((List<Event>) q.list(),
		    exhausted);
	}

	public Set<Slot> getSlots() {
		return slots;
	}

	public void addSlots(Set<Slot> slots) {
		this.slots.addAll(slots);
	}

	// Register user from CRSID
	public static User registerUser(String crsid) {
		// Add user to database if necessary

		// Begin hibernate session
		Session session = HibernateUtil.getTransactionSession();

		// Does the user already exist?
		Query userQuery = session.createQuery("from User where id = :id")
		    .setParameter("id", crsid);
		User user = (User) userQuery.uniqueResult();

		// If no, check if they exist in LDAP and create them if so
		if (user == null) {
			try {
				LDAPQueryManager.getUser(crsid);
			} catch (LDAPObjectNotFoundException e) {
				return null;
			}
			
			User newUser = new User(crsid);
			session.save(newUser);
			return newUser;
		}

		return user;
	}

	// equals
	@Override
	public boolean equals(Object object) {
		// check for self-comparison
		if (this == object)
			return true;

		// check that the object is a user
		if (!(object instanceof User))
			return false;

		// compare crsids
		return (((User) object).getCrsid().equals(this.crsid));
	}

	public Map<String, ?> toMap() {
		return ImmutableMap.of("crsid", crsid, "name", getName());
	}
}