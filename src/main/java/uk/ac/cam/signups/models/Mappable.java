package uk.ac.cam.signups.models;

import java.util.Map;

public interface Mappable {
	public int getId();
	public Map<String, ?> toMap();
}
