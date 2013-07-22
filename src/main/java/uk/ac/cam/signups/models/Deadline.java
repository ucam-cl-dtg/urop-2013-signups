package uk.ac.cam.signups.models;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.GenericGenerator;

import uk.ac.cam.signups.helpers.LDAPQueryHelper;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name="DEADLINES")
public class Deadline {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	private String title;
	private String message;
	private String url;
	private Calendar datetime;

	@ManyToMany(cascade = CascadeType.ALL)
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
	
	public Calendar getDatetime() { return this.datetime; }
	public void setDatetime(Calendar datetime) { this.datetime= datetime; }
	
	public User getOwner() { return this.owner; }
	public void setOwner(User owner) { this.owner= owner; }
	
	public Set<User> getUsers() { return this.users; }
	public void setUsers(Set<User> users) { this.users = users; }
	
	// Soy friendly get methods
	// Get formatted Date and time
//	public ImmutableMap<String, ?> getCalendarMap(){
//		SimpleDateFormat dateFormat = new SimpleDateFormat("u-F")
//		return ImmutableMap.of("day", datetime.get(DAY_OF_WEEK))
//	}
	
	// Get users as a map
	public HashSet getUsersMap() {
		HashSet<ImmutableMap<String,?>> deadlineUsers = new HashSet<ImmutableMap<String,?>>();
		String crsid;
		for(User u : users){
			// Get users crsid
			crsid = u.getCrsid();
			// Get users display name from LDAP
			String name = LDAPQueryHelper.getDisplayName(crsid);
			deadlineUsers.add(ImmutableMap.of("crsid",crsid, "name", name));
		}
		return deadlineUsers;
	}
}
