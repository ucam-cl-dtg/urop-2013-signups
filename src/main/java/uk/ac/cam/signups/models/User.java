package uk.ac.cam.signups.models;

import com.google.common.collect.ImmutableMap;

import org.hibernate.Query;
import org.hibernate.Session;

import uk.ac.cam.signups.helpers.LDAPQueryHelper;
import uk.ac.cam.signups.util.HibernateUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="USERS")
public class User {
	@Id
	private String crsid;

	@OneToMany(mappedBy = "owner")
	private Set<Event> events = new HashSet<Event>(0);

	@OneToMany(mappedBy = "owner")
	private Set<Slot> slots = new HashSet<Slot>(0);
	
	public User() {}
	public User(String crsid) {
		this.crsid = crsid;
	}
	
	public String getName() {
		return LDAPQueryHelper.getRegisteredName(crsid);
	}
	
	public String getCrsid() {return crsid;}
	public void setCrsid(String crsid) {this.crsid = crsid;}
	
	public Set<Event> getEvents() { return events; }
	public void addEvents(Set<Event> events) { this.events.addAll(events); }
	
	public Set<Slot> getSlots() { return slots; }
	public void addSlots(Set<Slot> slots) { this.slots.addAll(slots); }
	
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
		return ImmutableMap.of("crsid", crsid, "name", getName());
	}
}