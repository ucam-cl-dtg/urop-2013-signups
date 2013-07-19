package uk.ac.cam.signups.util;

import uk.ac.cam.signups.models.Mappable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
	
	public static List<Map<String, ?>> getImmutableCollection(Set<? extends Mappable> raws) {
		List<Map<String,?>> immutalizedCollection= new ArrayList<Map<String, ?>>(0);
		for(Mappable raw: raws)
			immutalizedCollection.add(raw.toMap());
		return immutalizedCollection;
	}
}
