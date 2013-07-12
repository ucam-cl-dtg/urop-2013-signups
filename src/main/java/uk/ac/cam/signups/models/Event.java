package uk.ac.cam.signups.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="EVENTS")
public class Event {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;
	
	private String location;
	private String title;
	
	@ManyToOne
	@JoinColumn(name = "USER_CRSID")
	private User owner;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
	private Set<Row> rows = new HashSet<Row>(0);
	
	@ManyToMany(cascade = CascadeType.ALL, mappedBy = "events")
	private Set<Type> types = new HashSet<Type>(0);

	@OneToMany(mappedBy = "event")
	private Set<HistoryItem> historyItems = new HashSet<HistoryItem>(0);
	
	public Event() {}
	
	public Event(int id, 
							String location,
							String title,
							User owner, 
							Set<Row> rows, 
							Set<Type> types, 
							Set<HistoryItem> historyItems){
		this.id = id;
		this.location = location;
		this.owner = owner;
		this.types = types;
		this.rows = rows;
		this.historyItems = historyItems;
		this.title = title;
	}
	
	public int getId() {return id;}
	public void setId(int id) {this.id = id;}

	public String getLocation() {return location;}
	public void setLocation(String location) {this.location = location;}

	public String getTitle() { return this.title; }
	public void setTitle(String title) { this.title = title; }
	
	public User getOwner() { return this.owner; }
	public void setOwner(User owner) { this.owner = owner; }
	
	public Set<Row> getRows() { return this.rows; }
	public void setRows(Set<Row> rows) { this.rows = rows; }
	
	public Set<HistoryItem> getHistoryItems() { return this.historyItems; }
	public void setHistoryItems(Set<HistoryItem> historyItems) { this.historyItems = historyItems; }
	
	public Set<Type> getTypes() { return this.types; }
	public void setTypes(Set<Type> types) { this.types = types; }
}