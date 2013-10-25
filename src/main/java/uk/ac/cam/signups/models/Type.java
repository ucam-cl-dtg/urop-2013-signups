package uk.ac.cam.signups.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;

import uk.ac.cam.cl.dtg.teaching.hibernate.HibernateUtil;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name = "TYPES")
public class Type implements Mappable {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "logIdSeq")
	@SequenceGenerator(name = "logIdSeq", sequenceName = "LOG_SEQ", allocationSize = 1)
	private int id;

	private String name;

	@ManyToOne
	@JoinColumn(name = "EVENT_ID")
	private Event event;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "type")
	private Set<Row> rows = new HashSet<Row>(0);

	public Type() {
	}

	public Type(String name) {
		this.name = name;
	}

	public Type(int id, String name, Event event, Set<Row> rows) {
		this.id = id;
		this.name = name;
		this.event = event;
		this.rows = rows;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Row> getRows() {
		return this.rows;
	}

	public void setRows(Set<Row> rows) {
		this.rows = rows;
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	@Override
	public Map<String, ?> toMap(User currentUser) {
		return ImmutableMap.of("id", id, "name", name);
	}

	@SuppressWarnings("unchecked")
	public static List<ImmutableMap<String, ?>> findSimilar(String name,
			User user, String mode) {
		Session session = HibernateUtil.getInstance().getSession();
		List<String> types = null;
		if (mode.equals("local")) {
			Query similars = session
					.createQuery("SELECT DISTINCT name FROM Type as type WHERE type.event.owner = :user AND lower(type.name) like :name");
			types = (List<String>) similars.setParameter("user", user)
					.setParameter("name", name.toLowerCase() + "%").list();
		} else if (mode.equals("global")) {
			Query similars = session
					.createQuery("SELECT DISTINCT name FROM Type as type WHERE lower(type.name) like :name");
			types = (List<String>) similars.setParameter("name",
					name.toLowerCase() + "%").list();
		}

		List<ImmutableMap<String, ?>> immutableTypes = new ArrayList<ImmutableMap<String, ?>>();
		for (String typeName : types)
			immutableTypes.add(ImmutableMap.of("name", typeName));
		if (!immutableTypes.contains(name))
			immutableTypes.add(ImmutableMap.of("name", name));

		return immutableTypes;
	}

	public void destroy() {
		Session session = HibernateUtil.getInstance().getSession();
		session.delete(this);
	}
}