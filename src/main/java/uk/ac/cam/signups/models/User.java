package uk.ac.cam.signups.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;

import uk.ac.cam.signups.helpers.LDAPQueryHelper;
import uk.ac.cam.signups.util.HibernateUtil;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name="USERS")
public class User {
	@Id
	private String crsid;
	
	@ManyToMany(mappedBy = "users")
	private Set<Deadline> deadlines = new HashSet<Deadline>();

	@OneToMany(mappedBy = "owner")
	private Set<Event> events = new HashSet<Event>(0);

	@ManyToMany(mappedBy = "users")
	private Set<Group> subscriptions = new HashSet<Group>(0);
	
	@OneToMany(mappedBy = "owner")
	private Set<Group> groups = new HashSet<Group>(0);

	@OneToMany(mappedBy = "owner")
	private Set<Slot> slots = new HashSet<Slot>(0);
	
	public User() {}
	public User(String crsid) {
		this.crsid = crsid;
	}
	
	public String getCrsid() {return crsid;}
	public void setCrsid(String crsid) {this.crsid = crsid;}
	
	public Set<Deadline> getDeadlines() { return deadlines; }
	public void addDeadlines(Set<Deadline> deadlines) { this.deadlines.addAll(deadlines); }
	
	public Set<Event> getEvents() { return events; }
	public void addEvents(Set<Event> events) { this.events.addAll(events); }
	
	public Set<Slot> getSlots() { return slots; }
	public void addSlots(Set<Slot> slots) { this.slots.addAll(slots); }
	
	public Set<Group> getGroups() { return this.groups; }
	public void addGroups(Set<Group> groups) { this.groups.addAll(groups); }

	public Set<Group> getSubscriptions() { return this.subscriptions; }
	public void addSubscriptions(Set<Group> subscriptions) { this.subscriptions.addAll(subscriptions); }
	
	// Register user from CRSID
	public static User registerUser(String crsid){
		// Add user to database if necessary

		// Begin hibernate session
		Session session = HibernateUtil.getTransactionSession();
		
		// Does the user already exist?
		Query userQuery = session.createQuery("from User where id = :id").setParameter("id", crsid);
	  	User user = (User) userQuery.uniqueResult();
	  	
	  	// If no, check if they exist in LDAP and create them if so
	  	if(user==null){
	  		if(LDAPQueryHelper.checkCRSID(crsid)==null){
	  			return null;
	  		}
	  		User newUser = new User(crsid);
	  		session.save(newUser);
	  		return newUser;
	  	}

		
		return user;
	}
	
	// Maps
	// Get users groups as a map
	public Set<Map<String, ?>> getGroupsMap() {
		HashSet<Map<String, ?>> userGroups = new HashSet<Map<String, ?>>();
		
		if(groups==null){
			return new HashSet<Map<String, ?>>();
		}
		
		for(Group g : groups)  {
			userGroups.add(g.toMap());
		}
		return userGroups;
	}
	
	// Get users deadlines as a map
	public List<Map<String, ?>> getUserDeadlinesMap() {
		List<Map<String, ?>> userDeadlines = new ArrayList<Map<String, ?>>();
		
		if(deadlines==null){
			return new ArrayList<Map<String, ?>>();
		}
		
		//Sort the deadlines
		SortedSet<Deadline> sortedDeadlines = new TreeSet<Deadline>();
		for(Deadline d : deadlines){
			sortedDeadlines.add(d);
		}	
		
		// Get deadlines as a map of all parameters
		for(Deadline d : sortedDeadlines)  {
			userDeadlines.add(d.toMap());
		}
		return userDeadlines;
		
	}
	public List<Map<String, ?>> getUserCreatedDeadlinesMap() {
		List<Map<String, ?>> userDeadlines = new ArrayList<Map<String, ?>>();
		
		// Query deadlines where this user is the owner
		Session session = HibernateUtil.getTransactionSession();
		Query getDeadlines = session.createQuery("from Deadline where owner = :owner").setParameter("owner", this);
	  	List<Deadline> createdDeadlines = (List<Deadline>) getDeadlines.list();			
	 
		if(createdDeadlines==null){
			return new ArrayList<Map<String, ?>>();
		}
		
		// Get deadlines as a map of all parameters
		for(Deadline d : createdDeadlines)  {
			userDeadlines.add(d.toMap());
		}
		return userDeadlines;
	}
	
	// equals
	@Override
	public boolean equals(Object object){
		//check for self-comparison
		if(this == object) return true;
		
		//check that the object is a user
		if(!(object instanceof User)) return false;
		
		//compare crsids
		return (((User) object).getCrsid().equals(this.crsid));
	}
	
	public Map<String, ?> toMap() {
		return ImmutableMap.of("crsid", crsid);
	}
}