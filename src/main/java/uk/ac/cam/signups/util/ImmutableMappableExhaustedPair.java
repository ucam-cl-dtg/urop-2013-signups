package uk.ac.cam.signups.util;

import uk.ac.cam.signups.models.Mappable;

public class ImmutableMappableExhaustedPair<T extends Mappable> {
	private Iterable<T> mappableList;
	private Boolean exhausted;

	public ImmutableMappableExhaustedPair(Iterable<T> mappableList,
			Boolean exhausted) {
		this.mappableList = mappableList;
		this.exhausted = exhausted;
	}

	public Iterable<T> getMappableIterable() {
		return this.mappableList;
	}

	public Boolean getExhausted() {
		return this.exhausted;
	}
}