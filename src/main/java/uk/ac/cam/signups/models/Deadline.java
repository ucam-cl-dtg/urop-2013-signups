package uk.ac.cam.signups.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

import uk.ac.cam.signups.helpers.LDAPQueryHelper;
import uk.ac.cam.signups.util.HibernateUtil;

import com.google.common.collect.ImmutableMap;
import com.googlecode.htmleasy.RedirectException;

@Entity
@Table(name="DEADLINES")
public class Deadline implements Comparable<Deadline>, Mappable {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	private String title;
	private String message;
	private String url;
	private Calendar datetime;

	@ManyToMany
	@JoinTable(name = "DEADLINES_USERS", 
						joinColumns = { @JoinColumn(name = "DEADLINE_ID")},
						inverseJoinColumns = { @JoinColumn(name = "USER_CRSID")})
	private Set<User> users = new HashSet<User>(0);
	
	@ManyToOne
	@JoinColumn(name="USER_CRSID")
	private User owner;
	
	public Deadline() {}
	public Deadline(int id, 
									String title, 
									String message, 
									Set<User> users, 
									User owner) {
		this.id = id;
		this.title = title;
		this.message = message;
		this.users = users;
		this.owner = owner;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public String getTitle() { return this.title; }
	public void setTitle(String title) { this.title = title; }

	public String getMessage() { return this.message; }
	public void setMessage(String message) { this.message= message; }
	
	public String getURL() { return this.url; }
	public void setURL(String url) { this.url= url; }
	
	public Calendar getDatetime() { return this.datetime; }
	public void setDatetime(Calendar datetime) { this.datetime= datetime; }
	
	public User getOwner() { return this.owner; }
	public void setOwner(User owner) { this.owner= owner; }
	
	public Set<User> getUsers() { return this.users; }
	public void setUsers(Set<User> users) { this.users = users; }
	
	// Queries
	public static Deadline getDeadline(int id){
		
		Session session = HibernateUtil.getTransactionSession();
		
		Query getDeadline = session.createQuery("from Deadline where id = :id").setParameter("id", id);
	  	Deadline deadline = (Deadline) getDeadline.uniqueResult();	
	  	return deadline;
	}
	
	public static void deleteDeadline(int id){
		
		Session session = HibernateUtil.getTransactionSession();
		
		Query getDeadline = session.createQuery("from Deadline where id = :id").setParameter("id", id);
	  	Deadline deadline = (Deadline) getDeadline.uniqueResult();	
	  	session.delete(deadline);
	}
	
	// Map builder
	@Override
	public Map<String, ?> toMap() {
		
		ImmutableMap.Builder<String, Object> builder; ;
		
		try {
			builder = new ImmutableMap.Builder<String, Object>();
			builder =builder
				.put("id", id)
				.put("name", title)
				.put("message", message)
				.put("url", url)
				.put("owner", owner.toMap());
			
			HashSet<ImmutableMap<String,?>> deadlineUsers = new HashSet<ImmutableMap<String,?>>();
			String crsid;
			for(User u : users){
				// Get users crsid
				crsid = u.getCrsid();
				// Get users display name from LDAP
				String name = LDAPQueryHelper.getRegisteredName(crsid);
				deadlineUsers.add(ImmutableMap.of("crsid",crsid, "name", name));
			}		
			
			builder = builder
					.put("datetime", getDateMap())
					.put("users", deadlineUsers);
			
		} catch(NullPointerException e){
			builder  = new ImmutableMap.Builder<String, Object>();
			builder =builder
					.put("id", id)
					.put("name", "Error getting deadline")
					.put("message", "")
					.put("url", "")
					.put("owner", "")
					.put("datetime", getDateMap())
					.put("users", "");
			return builder.build();
		}
		return builder.build();
	}

	// Get formatted Date and time
	public ImmutableMap<String, ?> getDateMap(){
		ImmutableMap<String, ?> dateMap;
		
		SimpleDateFormat niceDateFormat = new SimpleDateFormat("EEEEE, dd MMMMM yyyy");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
		SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
		String niceDateString = niceDateFormat.format(datetime.getTime());
		String dateString = dateFormat.format(datetime.getTime());
		String hourString = hourFormat.format(datetime.getTime());
		String minuteString = minuteFormat.format(datetime.getTime());
		
		// Is the deadline imminent? (ie. is it very close to current date)
		Calendar tomorrow = Calendar.getInstance();
		tomorrow.add(Calendar.DAY_OF_YEAR, 1);
		boolean imminent = tomorrow.get(Calendar.YEAR) >= datetime.get(Calendar.YEAR) &&
							tomorrow.get(Calendar.DAY_OF_YEAR) >= datetime.get(Calendar.DAY_OF_YEAR);
		
		try {
			dateMap = ImmutableMap.of("nicedate", niceDateString, "date", dateString, "hour", hourString, "minute", minuteString, "imminent", imminent);
			return dateMap;
		} catch(NullPointerException e){
			return ImmutableMap.of("nicedate", "00/00/0000", "date", "00/00/0000", "hour", "00", "minute", "00");
		}
			
	}
	
	// Set deadline natural ordering
	public int compareTo(Deadline deadline) {
		return this.datetime.compareTo(deadline.datetime);
	}
	
}
