package uk.ac.cam.signups.models;

import com.google.common.collect.ImmutableMap;

import uk.ac.cam.signups.util.Util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name="EVENTS")
public class Event implements Mappable {
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
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
	private Set<Type> types = new HashSet<Type>(0);

	public Event() {}
	
	public Event(int id, 
							String location,
							String title,
							User owner, 
							Set<Row> rows, 
							Set<Type> types) {
		this.id = id;
		this.location = location;
		this.owner = owner;
		this.types = types;
		this.rows = rows;
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
	
	public Set<Type> getTypes() { return this.types; }
	public void setTypes(Set<Type> types) { this.types = types; }
	
	public Map<String, ?> toMap() {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder = builder.put("title",title);
		builder = builder.put("location",location);
		builder = builder.put("owner",owner.toMap());
		Set<Map<String,?>> immutableRows = Util.getImmutableCollection(rows);
		//builder = builder.put("rows", Util.getImmutableCollection(rows));
		builder = builder.put("types", Util.getImmutableCollection(types));
		return builder.build();
	}
}