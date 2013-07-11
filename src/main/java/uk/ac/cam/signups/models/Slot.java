package uk.ac.cam.signups.models;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="SLOTS")
public class Slot {
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy="increment")
	private int id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ROW_ID")
	private Row row;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="USER_CRSID")
	private User owner;
	
	public Slot() {}
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
}
