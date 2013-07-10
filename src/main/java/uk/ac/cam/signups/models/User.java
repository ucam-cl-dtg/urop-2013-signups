package uk.ac.cam.signups.models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="USERS")
public class User {
	@Id
	private String crsid;
	
	private List<Deadline> deadlines;
	
	public User() {}
	
	public User(String crsid) {
		this.crsid = crsid;
	}
	
	public String getCrsid() {return crsid;}
	public void setCrsid(String crsid) {this.crsid = crsid;}
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "DEADLINES_STUDENTS", 
						joinColumns = { @JoinColumn(name = "USER_CRSID")},
						inverseJoinColumns = {@JoinColumn(name = "DEADLINE_ID")})
	public List<Deadline> getDeadlines() {
		return deadlines;
	}
	
	public void setDeadlines(List<Deadline> deadlines) {
		this.deadlines = deadlines;
	}
}