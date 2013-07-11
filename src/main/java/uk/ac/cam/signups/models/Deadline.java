package uk.ac.cam.signups.models;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="DEADLINES")
public class Deadline {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;

	private String title;
	private String message;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "DEADLINES_USERS", 
						joinColumns = { @JoinColumn(name = "DEADLINE_ID")},
						inverseJoinColumns = { @JoinColumn(name = "USER_CRSID")})
	private Set<User> users = new HashSet<User>(0);

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "deadline")
	private Set<Row> rows = new HashSet<Row>(0);
	
	public Deadline() {}
	public Deadline(int id, 
									String title, 
									String message, 
									Set<User> users, 
									Set<Row> rows) {
		this.id = id;
		this.title = title;
		this.message = message;
		this.rows = rows;
		this.users = users;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public String getTitle() { return this.title; }
	public void setTitle(String title) { this.title = title; }

	public String getMessage() { return this.message; }
	public void setMessage(String message) { this.message= message; }
	
	public Set<User> getUsers() { return this.users; }
	public void setUsers(Set<User> users) { this.users = users; }
	
	public Set<Row> getRows() { return this.rows; } 
	public void setRows(Set<Row> rows) { this.rows = rows; }
}
