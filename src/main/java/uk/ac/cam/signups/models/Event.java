package uk.ac.cam.signups.models;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="EVENTS")
public class Event {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private String id;
	
	// private Set<Deadline> deadlines;
	
	public Event() {}
	
	public Event(int id) {
		this.id = id;
	}
	
	public String getId() {return id;}
	public void setId(int id) {this.id = id;}
	
	/*
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "DEADLINES_STUDENTS", 
						joinColumns = { @JoinColumn(name = "USER_CRSID")},
						inverseJoinColumns = {@JoinColumn(name = "DEADLINE_ID")})
	public Set<Deadline> getDeadlines() {
		return deadlines;
	}
	
	public void setDeadlines(Set<Deadline> deadlines) {
		this.deadlines = deadlines;
	}
	*/
}