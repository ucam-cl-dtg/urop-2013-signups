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
import javax.persistence.OrderBy;


@Entity
@Table(name="EVENTS")
public class Event implements Mappable {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;
	
	private String location;
	private String title;
	private String sheetType;
	
	@ManyToOne
	@JoinColumn(name = "USER_CRSID")
	private User owner;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
	@OrderBy("id")
	private Set<Row> rows = new TreeSet<Row>();
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
	@OrderBy("id")
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

	public String getSheetType() { return this.sheetType; }
	public void setSheetType(String sheetType) { this.sheetType = sheetType; }
	
	public User getOwner() { return this.owner; }
	public void setOwner(User owner) { this.owner = owner; }
	
	public Set<Row> getRows() { return this.rows; }
	public void addRows(Set<Row> rows) { this.rows.addAll(rows); }
	
	public Set<Type> getTypes() { return this.types; }
	public void addTypes(Set<Type> types) { this.types.addAll(types); }
	
	public Map<String, ?> toMap() {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>();
		builder = builder.put("id",id);
		builder = builder.put("title",title);
		builder = builder.put("location",location);
		builder = builder.put("sheetType",sheetType);
		builder = builder.put("owner",owner.toMap());
		builder = builder.put("types", Util.getImmutableCollection(types));
		
		if (sheetType.equals("datetime")) {
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
			builder = builder.put("rows", new ArrayList<ImmutableMap<String,?>>());
		} else if (sheetType.equals("manual")) {
			builder = builder.put("dates", new ArrayList<ImmutableMap<String,?>>());
			builder = builder.put("rows", Util.getImmutableCollection(rows));
		}

		return builder.build();
	}
}