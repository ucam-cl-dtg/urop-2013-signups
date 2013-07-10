package uk.ac.cam.signups.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.ws.rs.FormParam;

@Entity
@Table(name="USERS")
public class User {
	private String crsid;
	
	public User() {}
	
	public User(String crsid) {
		this.crsid = crsid;
	}
	
	public String getCrsid() {return crsid;}
	public void setCrsid(String crsid) {this.crsid = crsid;}
}