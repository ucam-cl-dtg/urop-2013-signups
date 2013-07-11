package uk.ac.cam.signups.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.util.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static Logger log = LoggerFactory.getLogger(UserLookupManager.class);
	
	/**
	 * Fields to cache user data once looked up
	 */
	private String crsid;
	private String displayName;
	private String surname;
	private String email;
	private String status;
	private String photo;
	private String institution;
	
	private UserLookupManager(String crsid){
		this.crsid = crsid;
	}
	
	/**
	 * Get users display name
	 * @return String displayName
	 */
	public String getDisplayName(){
		if(displayName==null){
			log.debug("Retrieving display name from LDAP");
			displayName = LDAPProvider.getUniqueResult(crsid, "displayName");	
		} 
		return displayName;
	}
	
	/**
	 * Get users surname
	 * @return String surname
	 */
	public String getSurname(){
		if(surname==null){
			log.debug("Retrieving surname from LDAP");
			surname = LDAPProvider.getUniqueResult(crsid, "sn");	
		} 
		return surname;
	}
	

	/**
	 * Get users email
	 * @return String email
	 */
	public String getEmail(){
		if(email==null){
			log.debug("Retrieving email from LDAP");
			email = LDAPProvider.getUniqueResult(crsid, "mail");	
		} 
		return email;
	}

	
	/**
	 * Gets a list of misAffiliations associated with user
	 * If 'staff' misAffiliations user is present sets status as staff, otherwise student
	 * @return String status
	 */
	public String getStatus(){
		if(status==null){
			log.debug("Retrieving misAffiliation from LDAP");
			// Default status is student
			status = "student";
			List<String> statusList = LDAPProvider.getStringListResult(crsid, "misAffiliation");
			for(String s : statusList){
				// If the user has staff misAffiliation, set staff status
				if(s.toString().equals("staff")){
					log.debug("staff misAffiliation detected for user");
					status = "staff";
				}
			}
		} 
		return status;
	}
	
	/**
	 * Gets photo as an encoded base 64 jpeg
	 * To display in soy template, use  <img src="data:image/jpeg;base64,{$user.photo}" /> or similar
	 * @return String photo
	 */
	public String getPhoto(){
		if(photo==null){
			Log.debug("Retrieving photo from LDAP");
			
			// Set default as "none" to convert to no photo image in soy
			photo = "none";

			// See if there is a photo on LDAP
			NamingEnumeration<?> photoEnum = LDAPProvider.getEnumListResult(crsid, "jpegPhoto");
			// Convert to byte arrays
			List<byte[]> photoList = new ArrayList<byte[]>();
			try {
				while(photoEnum.hasMore()){
					photoList.add((byte[])photoEnum.next());		
				}
				byte[] p = (byte[])photoList.get(0);
				photo = new String(Base64.encodeBase64(p));
			} catch (NamingException e){
				log.error(e.getMessage());
			} catch (NullPointerException e){
				log.debug("User has no photo");
			}
		} 
		return photo;
	}
	
	/**
	 * Gets a list of misAffiliations associated with user
	 * If 'staff' misAffiliations user is present sets status as staff, otherwise student
	 * @return String status
	 */
	public String getInstitution(){
		if(institution==null){
			log.debug("Retrieving institution from LDAP");
			institution = "none";
			List<String> inList = LDAPProvider.getStringListResult(crsid, "ou");
			if(inList!=null&&!inList.isEmpty()){
				institution = inList.get(0);
			}
		} 
		return institution;
	}
	
	/**
	 * Builds a soy-ready immutable map of all data related to user
	 * @return ImmutableMap userData
	 */
	public ImmutableMap<String, ?> getAll(){
		// Create a map of all the users data using builder
		Log.debug("Building map of user data");
		ImmutableMap<String, ?> userData = new ImmutableMap.Builder<String, String>()
				.put("crsid", crsid)
				.put("fullname", getDisplayName())
				.put("surname", getSurname())
				.put("email", getEmail())
				.put("status", getStatus())
				.put("photo", getPhoto())
				.put("institution", getInstitution())
				.build();
		return userData;
	}
	
	/**
	 * Creates a UserLookupManager object if it does not already exist and
	 * returns it, otherwise just returns the existing singleton instance
	 * @param crsid
	 * @returns UserLookupManager u
	 *
	 */
	// Create instance of UserLookupManager if it doesn't exist
	public static UserLookupManager getUserLookupManager(String crsid){
		Log.info("UserLookupManager created for " + crsid);
		if(u==null){
			u = new UserLookupManager(crsid);
		} 
		return u;
	}
}
