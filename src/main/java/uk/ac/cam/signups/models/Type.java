package uk.ac.cam.signups.models;

import com.google.common.collect.ImmutableMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.Column;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

import uk.ac.cam.signups.util.HibernateUtil;

@Entity
@Table(name="TYPES")
public class Type implements Mappable {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	private int id;
	
	private String name;

	@ManyToOne
	@JoinColumn(name="EVENT_ID")
	private Event event;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private Set<Row> rows = new HashSet<Row>(0);
	
	public Type() {}
	public Type(String name) {this.name = name;}
	public Type(int id, 
							String name, 
							Event event, 
							Set<Row> rows) {
		this.id = id;
		this.name = name;
		this.event = event;
		this.rows = rows;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	
	public Set<Row> getRows() {return this.rows;}
	public void setRows(Set<Row> rows) {this.rows = rows;}
	
	public Event getEvent() { return this.event; }
	public void setEvent(Event event) { this.event = event; }
	
	public Map<String, ?> toMap() {
		return ImmutableMap.of("name", name);
	}
	
	@SuppressWarnings("unchecked")
  public List<Type> findSimilar(String name, User user, String mode) {
		Session session = HibernateUtil.getTransactionSession();
		List<Type> types = null; 
		if(mode.equals("local")) {
			Query similars = session.createQuery("SELECT DISTINCT name FROM Type as type WHERE type.event.owner = :user AND lower(type.name) like :name");
			types = (List<Type>) similars.setParameter("user", user).setParameter("name", name.toLowerCase()).list();
		} else if(mode.equals("global")){
			Query similars = session.createQuery("SELECT DISTINCT name FROM Type as type WHERE lower(type.name) like :name");
			types = (List<Type>) similars.setParameter("name", name.toLowerCase()).list();
		}
		return types;
	}
}