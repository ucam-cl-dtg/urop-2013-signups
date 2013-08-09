package uk.ac.cam.signups.util;

import uk.ac.cam.signups.models.Mappable;

import java.util.List;

public class ImmutableMappableExhaustedPair<T extends Mappable> {
	private List<T> mappableList;
	private Boolean exhausted;
	
	public ImmutableMappableExhaustedPair(List<T> mappableList, Boolean exhausted) {
		this.mappableList = mappableList;
		this.exhausted = exhausted;
	}
	
	public List<T> getMappableList() { return this.mappableList; }
	public Boolean getExhausted() { return this.exhausted; }
}