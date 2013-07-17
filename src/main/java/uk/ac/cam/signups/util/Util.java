package uk.ac.cam.signups.util;

import uk.ac.cam.signups.models.Mappable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Util {
	
	public static Set<Map<String, ?>> getImmutableCollection(Set<? extends Mappable> raws) {
		Set<Map<String,?>> immutalizedCollection= new HashSet<Map<String, ?>>(0);
		for(Mappable raw: raws)
			immutalizedCollection.add(raw.toMap());
		return immutalizedCollection;
	}
}
