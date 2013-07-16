package uk.ac.cam.signups.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.Column;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

import uk.ac.cam.signups.util.HibernateUtil;

@Entity
@Table(name="TYPES")
public class Type {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	private int id;
	
	@Column(name="name", unique = true)
	private String name;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name="EVENTS_TYPES",
						joinColumns = {@JoinColumn(name = "TYPE_ID")},
						inverseJoinColumns = {@JoinColumn(name = "EVENT_ID")})
	private Set<Event> events = new HashSet<Event>(0);

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private Set<Row> rows = new HashSet<Row>(0);
	
	public Type() {}
	public Type(int id, 
							String name, 
							Set<Event> events, 
							Set<Row> rows) {
		this.id = id;
		this.name = name;
		this.events = events;
		this.rows = rows;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	
	public Set<Row> getRows() {return this.rows;}
	public void setRows(Set<Row> rows) {this.rows = rows;}
	
	public Set<Event> getEvents() { return this.events; }
	public void setEvents(Set<Event> events) { this.events = events; }
	
	public Set<Type> findSimilar(String param, User user) {
		Session session = HibernateUtil.getTransaction();
		Query similars = session.createQuery("SELECT DISTINCT name FROM Type as type WHERE type.event.owner = :user AND lower(type.name) like :name");
		Set<Type> types = similars.setParameter();
		session.getTransaction().commit();
	}
	}
}