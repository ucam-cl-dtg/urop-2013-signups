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
	
	public static <T extends Mappable> Set<Integer> getIds(List<T> collection) {
		Set<Integer> ids = new HashSet<Integer>();
		for(T item : collection) {
			ids.add(item.getId());
		}
		
		return ids;
	}
	
	public static <T extends Mappable> T findById(List<T> collection, int id) {
		for(T element: collection) {
			if (element.getId() == id) {
				return element;
			}
		}
		
		return null;
	}
}
