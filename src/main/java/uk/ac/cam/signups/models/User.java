package uk.ac.cam.signups.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;

import uk.ac.cam.signups.util.HibernateUtil;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name="USERS")
public class User {
	@Id
	private String crsid;
	
	@ManyToMany(mappedBy = "users")
	private Set<Deadline> deadlines = new HashSet<Deadline>(0);

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
	  	
	  	// If no, create them
	  	if(user==null){
	  		User newUser = new User(crsid);
	  		session.save(newUser);
	  		return newUser;
	  	}

		
		return user;
	}
	
	// Soy friendly get methods
	
	// Get users groups as a map
	public Set<ImmutableMap<String, ?>> getGroupsMap() {
		HashSet<ImmutableMap<String, ?>> userGroups = new HashSet<ImmutableMap<String, ?>>(0);
		
		if(groups==null){
			return new HashSet<ImmutableMap<String, ?>>(0);
		}
		
		for(Group g : groups)  {
			userGroups.add(ImmutableMap.of("id", g.getId(), "name", g.getTitle(), "users", g.getUsersMap(), "owner", g.getOwner().getCrsid()));
		}
		return userGroups;
	}
	
	// Get users deadlines as a map
	public Set<ImmutableMap<String, ?>> getDeadlinesMap() {
		HashSet<ImmutableMap<String, ?>> userDeadlines = new HashSet<ImmutableMap<String, ?>>(0);
		
		if(deadlines==null){
			return new HashSet<ImmutableMap<String, ?>>(0);
		}
		
		// Get deadlines as a map of all parameters
		for(Deadline d : deadlines)  {
			userDeadlines.add(ImmutableMap.of("id", d.getId(), "name", d.getTitle(), "message", d.getMessage(),"users", d.getUsersMap()));
		}
		return userDeadlines;
	}
	public Set<ImmutableMap<String, ?>> getCreatedDeadlinesMap() {
		HashSet<ImmutableMap<String, ?>> userDeadlines = new HashSet<ImmutableMap<String, ?>>(0);
		
		// Query deadlines where this user is the owner
		Session session = HibernateUtil.getTransactionSession();
		Query getDeadlines = session.createQuery("from Deadline where owner = :owner").setParameter("owner", this);
	  	List<Deadline> createdDeadlines = (List<Deadline>) getDeadlines.list();			
	 
		if(createdDeadlines==null){
			return new HashSet<ImmutableMap<String, ?>>(0);
		}
		
		// Get deadlines as a map of all parameters
		for(Deadline d : createdDeadlines)  {
			userDeadlines.add(ImmutableMap.of("id", d.getId(), "name", d.getTitle(), "message", d.getMessage(),"users", d.getUsersMap()));
		}
		return userDeadlines;
	}
	
	//what is this for??
	public Map<String, ?> toMap() {
		return ImmutableMap.of("crsid", crsid);
	}
}