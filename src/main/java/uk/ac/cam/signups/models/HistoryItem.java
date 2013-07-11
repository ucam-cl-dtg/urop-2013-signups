package uk.ac.cam.signups.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.CascadeType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="HISTORY_ITEMS")
public class HistoryItem {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	private int id;
	
	private String message;

	@ManyToOne
	@JoinColumn(name = "EVENT_ID")
	private Event event;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "HISTORY_ITEMS_EVENTS",
						joinColumns = { @JoinColumn(name="HISTORY_ITEM_ID")},
						inverseJoinColumns = {@JoinColumn(name="USER_CRSID")})
	private Set<User> users = new HashSet<User>(0);

	public HistoryItem() {}
	public HistoryItem(int id, 
										String message, 
										Event event, 
										Set<User> users) {
		this.id = id;
		this.message = message;
		this.event = event;
		this.users = users;
	}
	
	public int getInt() { return this.id; }
	public void setInt(int id) { this.id = id; }
	
	public String getMessage() { return this.message; }
	public void setMessage(String message) { this.message = message; }
	
	public Set<User> getUsers() { return this.users; }
	public void setUsers(Set<User> users) { this.users = users; }
	
	public Event getEvent() { return this.event; }
	public void setEvent(Event event) { this.event = event; }
}