package uk.ac.cam.signups.models;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="SLOTS")
public class Slot implements Mappable {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="logIdSeq") 
	@SequenceGenerator(name="logIdSeq",sequenceName="LOG_SEQ", allocationSize=1)
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ROW_ID")
	private Row row;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="USER_CRSID")
	private User owner;
	
	public Slot() {}
	public Slot(Row row) {
		this.row = row;
	}
	public Slot(int id, Row row, User owner) {
		this.id = id;
		this.row = row;
		this.owner = owner;
	}
	
	public int getId() { return this.id; }
	public void setId(int id) { this.id = id; }
	
	public Row getRow() { return this.row; }
	public void setRow(Row row) { this.row = row; }
	
	public User getOwner() { return this.owner; }
	public void setOwner(User owner) { this.owner = owner; }

  public Map<String, ?> toMap() {
  	ImmutableMap<String, ?> rOwner;
  	if (owner != null) {
  		rOwner = (ImmutableMap<String, ?>) owner.toMap();
  	} else {
  		rOwner = ImmutableMap.of("crsid", "", "name", "");
  	}
	  return ImmutableMap.of("id",id,"owner", rOwner);
  }
}
