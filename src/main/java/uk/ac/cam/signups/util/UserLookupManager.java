package uk.ac.cam.signups.util;

import com.google.common.collect.ImmutableMap;

/**
 * @author      Holly Priest <hp343@cam.ac.uk>
 * @version     1                
 * A class to contain all data that may need to be looked up for a user.
 * Created once per session for a single crsid.
 * Stores the data after the first lookup to avoid looking up the same data
 * through LDAP multiple times.
 */
public class UserLookupManager {
	private static UserLookupManager u;
	
	// Data fields
	private String crsid;
	private String fullName;
	private String surname;
	private String email;
	private String status;
	
	private UserLookupManager(String crsid){
		this.crsid = crsid;
	}
	
	public String getFullName(){
		if(fullName==null){
			return LDAPProvider.getData(crsid, "cn");	
		} 
		return fullName;
	}
	
	public String getSurname(){
		if(surname==null){
			return LDAPProvider.getData(crsid, "an");	
		} 
		return surname;
	}
	
	public String getEmail(){
		if(email==null){
			return LDAPProvider.getData(crsid, "mail");	
		} 
		return email;
	}

	public String getStatus(){
		if(status==null){
			return LDAPProvider.getData(crsid, "status");	
		} 
		return status;
	}
	
	public ImmutableMap<String, ?> getAll(){
		// Create a map of all the users data using builder
		ImmutableMap<String, ?> userData = new ImmutableMap.Builder<String, String>()
				.put("crsid", crsid)
				.put("fullname", getFullName())
				.put("surname", getSurname())
				.put("email", getEmail())
				.put("status", getStatus())
				.build();
		return userData;
	}

	// Create instance of UserLookupManager if it doesn't exist
	public static UserLookupManager getUserLookupManager(String crsid){
		if(u==null){
			u = new UserLookupManager(crsid);
		} 
		return u;
	}
}
