package uk.ac.cam.signups.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="GROUPS")
public class Group {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	private int id;

	private String title;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name="GROUPS_USERS",
						joinColumns = {@JoinColumn(name = "GROUP_ID")},
						inverseJoinColumns = {@JoinColumn(name = "USER_CRSID")})
	private Set<User> users = new HashSet<User>(0);
	
	public Group(int id, 
							String title, 
							Set<User> users) {
		this.id = id;
		this.title = title;
		this.users = users;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public String getTitle() { return this.title; }
	public void setTitle(String title) { this.title = title; }
	
	public Set<User> getUsers() { return this.users; }
	public void setUsers(Set<User> users) { this.users = users; }
}
