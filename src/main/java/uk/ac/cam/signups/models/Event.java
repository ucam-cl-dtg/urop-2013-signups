package uk.ac.cam.signups.models;

import com.google.common.collect.ImmutableMap;

import org.hibernate.annotations.GenericGenerator;

import uk.ac.cam.signups.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;


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
		this.types.addAll(types);
		this.rows.addAll(rows);
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
	public void addRows(Set<Row> rows) { this.rows.addAll(rows); }
	
	public Set<Type> getTypes() { return this.types; }
	public void addTypes(Set<Type> types) { this.types.addAll(types); }
	
	public Map<String, ?> toMap() {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder = builder.put("title",title);
		builder = builder.put("location",location);
		builder = builder.put("owner",owner.toMap());
		builder = builder.put("types", Util.getImmutableCollection(types));
		
		// Make row hierarchy with dates
		SortedMap<String, Set<Row>> temp = new TreeMap<String, Set<Row>>();
		SortedSet<Row> rowContainer;
		for(Row row: rows) {
			Calendar cal = row.getCalendar();
			String key = "" + cal.get(Calendar.YEAR) + ":" + cal.get(Calendar.MONTH) + ":" + cal.get(Calendar.DAY_OF_MONTH);
			if (temp.containsKey(key)) {
				temp.get(key).add(row);
			} else {
				rowContainer = new TreeSet<Row>();
				rowContainer.add(row);
				temp.put(key, rowContainer);
			}
		}
		
		List<ImmutableMap<String, ?>> dates = new ArrayList<ImmutableMap<String, ?>>();
		for(String key: temp.keySet()) {
			// Parse date nicely
			String[] dateArray = key.split(":");
			int year = Integer.parseInt(dateArray[0]);
			int month = Integer.parseInt(dateArray[1]);
			int day = Integer.parseInt(dateArray[2]);
			Calendar cal = new GregorianCalendar(year, month, day);
			SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d");
			String date = formatter.format(cal.getTime());

			dates.add(ImmutableMap.of("date", date, "rows", Util.getImmutableCollection(temp.get(key))));
		}
		
		builder = builder.put("dates", dates);

		return builder.build();
	}
}