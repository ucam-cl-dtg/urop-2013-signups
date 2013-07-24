package uk.ac.cam.signups.models;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

import uk.ac.cam.signups.helpers.LDAPQueryHelper;
import uk.ac.cam.signups.util.HibernateSessionRequestFilter;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.UserLookupManager;
import uk.ac.cam.signups.util.Util;

import com.google.common.collect.ImmutableMap;

@Entity
@Table(name="GROUPS")
public class Group implements Mappable {

    @Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	private int id;

	@FormParam("title") private String title;

	@ManyToMany
	@JoinTable(name="GROUPS_USERS",
						joinColumns = {@JoinColumn(name = "GROUP_ID")},
						inverseJoinColumns = {@JoinColumn(name = "USER_CRSID")})
	private Set<User> users = new HashSet<User>(0);
	
	@ManyToOne
	@JoinColumn(name="USER_CRSID")
	private User owner;
	
	public Group() { }
	
	public Group(int id, 
				String title, 
				Set<User> users,
				User owner) {
		this.id = id;
		this.title = title;
		this.users = users;
		this.owner = owner;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public String getTitle() { return this.title; }
	public void setTitle(String title) { this.title = title; }
	
	public Set<User> getUsers() { return this.users; }
	public void setUsers(Set<User> users) { this.users = users; }
	
	public User getOwner() { return this.owner; }
	public void setOwner(User owner) { this.owner = owner; }

	// Queries
	public static Group getGroup(int id){
		
		Session session = HibernateUtil.getTransactionSession();
		
		Query getGroup = session.createQuery("from Group where id = :id").setParameter("id", id);
	  	Group group = (Group) getGroup.uniqueResult();	
	  	return group;
	}
	
	public static void deleteGroup(int id){
		
		Session session = HibernateUtil.getTransactionSession();
		
		Query getGroup = session.createQuery("from Group where id = :id").setParameter("id", id);
	  	Group group = (Group) getGroup.uniqueResult();
	  	session.delete(group);
	}
	
	// Map builder
	@Override
	public Map<String, ?> toMap() {
		ImmutableMap.Builder<String, Object> builder = new ImmutableMap.Builder<String, Object>()
			.put("id",id)
			.put("name",title)
			.put("owner",owner.toMap());
		
		// Get group users
		HashSet<ImmutableMap<String,?>> groupUsers = new HashSet<ImmutableMap<String,?>>();
		String crsid;
		for(User u : users){
			// Get users crsid
			crsid = u.getCrsid();
			// Get users display name from LDAP
			String name = LDAPQueryHelper.getRegisteredName(crsid);
			groupUsers.add(ImmutableMap.of("crsid",crsid, "name", name));
		}
		
		builder = builder.put("users", groupUsers);

		return builder.build();
		
	}
}
