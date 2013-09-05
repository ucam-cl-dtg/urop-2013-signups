package uk.ac.cam.signups.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import uk.ac.cam.signups.models.Mappable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
	public static List<Map<String, ?>> getImmutableCollection(Iterable<? extends Mappable> raws) {
		List<Map<String,?>> immutalizedCollection= new ArrayList<Map<String, ?>>();

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
			if (element.getId() == id)
				return element;
		}
		
		return null;
	}
	
	public static <T, K> ImmutableMap<T, List<K>> multimapToImmutableMap(ArrayListMultimap<T, K> mm) {
		ImmutableMap.Builder<T, List<K>> builder = ImmutableMap.builder();
				
		for(T k : mm.keySet()) 
			builder.put(k, ImmutableList.copyOf(mm.get(k)));
		return builder.build();
	}
	
	public static String join(Iterable<String> strs, String delimeter) {
		String joined = "";

		for(String str: strs)
			joined += (str + delimeter);
		
		return joined.substring(0, joined.length() - delimeter.length());
	}
	
	public static Calendar datepickerParser(String dateString) throws ParseException {
		SimpleDateFormat calendarFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		calendarFormatter.setLenient(false);
		Date formattedDate = calendarFormatter.parse(dateString);
		Calendar returnCal = new GregorianCalendar();
		returnCal.setTime(formattedDate);
		return returnCal;
	}
}
