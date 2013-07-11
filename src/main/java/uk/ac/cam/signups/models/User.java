package uk.ac.cam.signups.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="USERS")
public class User {
	@Id
	private String crsid;
	
	@ManyToMany(mappedBy = "users")
	private Set<Deadline> deadlines = new HashSet<Deadline>(0);

	@OneToMany(mappedBy = "owner")
	private Set<Event> events = new HashSet<Event>(0);

	@OneToMany(mappedBy = "users")
	private Set<HistoryItem> historyItems = new HashSet<HistoryItem>(0);

	@ManyToMany(mappedBy = "users")
	private Set<Group> groups = new HashSet<Group>(0);

	@OneToMany(mappedBy = "owner")
	private Set<Slot> slots = new HashSet<Slot>(0);
	
	public User() {}
	public User(String crsid, 
							Set<Deadline> deadlines, 
							Set<Event> events, 
							Set<HistoryItem> historyItems, 
							Set<Group> groups,
							Set<Slot> slots) {
		this.crsid = crsid;
		this.historyItems = historyItems;
		this.events = events;
		this.deadlines = deadlines;
		this.groups = groups;
		this.slots = slots;
	}
	
	public String getCrsid() {return crsid;}
	public void setCrsid(String crsid) {this.crsid = crsid;}
	
	public Set<Deadline> getDeadlines() { return deadlines; }
	public void setDeadlines(Set<Deadline> deadlines) { this.deadlines = deadlines; }
	
	public Set<Event> getEvents() { return events; }
	public void setEvents(Set<Event> events) { this.events = events; }
	
	public Set<Slot> getSlots() { return slots; }
	public void setSlots(Set<Slot> slots) { this.slots = slots; }
	
	public Set<HistoryItem> getHistoryItems() { return this.historyItems; }
	public void setHistoryItems(Set<HistoryItem> historyItems) { this.historyItems = historyItems; }
	
	public Set<Group> getGroups() { return this.groups; }
	public void setGroups(Set<Group> groups) { this.groups = groups; }
}