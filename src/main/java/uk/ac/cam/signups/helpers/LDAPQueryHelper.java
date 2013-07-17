package uk.ac.cam.signups.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.util.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import uk.ac.cam.signups.util.LDAPProvider;
import uk.ac.cam.signups.util.UserLookupManager;

public class LDAPQueryHelper {
	
	private static Logger log = LoggerFactory.getLogger(LDAPQueryHelper.class);
	
	/**
	 * Get users display name
	 * @return String displayName
	 */
	public static String getDisplayName(String crsid){
			log.debug("Retrieving display name for " + crsid + " from LDAP");
			String displayName = LDAPProvider.getUniqueResult(crsid, "displayName");	
			if(displayName==null){
				log.debug("No displayName found for user " + crsid);
				return "Annonymous";
			}
		return displayName;
	}
	
	/**
	 * Get users surname
	 * @return String surname
	 */
	public static String getSurname(String crsid){
			log.debug("Retrieving surname for " + crsid + " from LDAP");
			String surname = LDAPProvider.getUniqueResult(crsid, "sn");	
			if(surname==null){
				log.debug("No surname found for user " + crsid);
				return "Unknown";
			}
		return surname;
	}
	

	/**
	 * Get users email
	 * @return String email
	 */
	public static String getEmail(String crsid){
			log.debug("Retrieving email for " + crsid + " from LDAP");
			String email = LDAPProvider.getUniqueResult(crsid, "mail");	
			if(email==null){
				log.debug("No email found for user " + crsid);
				return "No email";
			}
		return email;
	}

	
	/**
	 * Gets a list of misAffiliations associated with user
	 * If 'staff' misAffiliations user is present sets status as staff, otherwise student
	 * @return String status
	 */
	public static String getStatus(String crsid){
			log.debug("Retrieving misAffiliation for " + crsid + " from LDAP");
			// Default status is student
			String status = "student";
			List<String> statusList = LDAPProvider.getStringListResult(crsid, "misAffiliation");
			for(String s : statusList){
				// If the user has staff misAffiliation, set staff status
				if(s.toString().equals("staff")){
					log.debug("staff misAffiliation detected for user " + crsid);
					status = "staff";
				}
			}
		return status;
	}
	
	/**
	 * Gets photo as an encoded base 64 jpeg
	 * To display in soy template, use  <img src="data:image/jpeg;base64,{$user.photo}" /> or similar
	 * @return String photo
	 */
	public static String getPhoto(String crsid){
			Log.debug("Retrieving photo for " + crsid + " from LDAP");
			
			// Set default as "none" to convert to no photo image in soy
			String photo = "none";

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
				log.debug("User " + crsid + " has no photo");
			}
		return photo;
	}
	
	/**
	 * Gets a list of misAffiliations associated with user
	 * If 'staff' misAffiliations user is present sets status as staff, otherwise student
	 * @return String status
	 */
	public static String getInstitution(String crsid){
			log.debug("Retrieving institution for " + crsid + " from LDAP");
			//Default is no institution
			String institution = "none";
			List<String> inList = LDAPProvider.getStringListResult(crsid, "ou");
			if(inList!=null&&!inList.isEmpty()){
				institution = inList.get(0);
			}
		return institution;
	}
	
	/**
	 * Builds a soy-ready immutable map of all data related to user
	 * @return ImmutableMap userData
	 */
	public static ImmutableMap<String, ?> getAll(String crsid){
		// Create a map of all the users data using builder
		Log.debug("Getting all data for user " + crsid);
		ImmutableMap<String, ?> userData = new ImmutableMap.Builder<String, String>()
				.put("crsid", crsid)
				.put("fullname", getDisplayName(crsid))
				.put("surname", getSurname(crsid))
				.put("email", getEmail(crsid))
				.put("status", getStatus(crsid))
				.put("photo", getPhoto(crsid))
				.put("institution", getInstitution(crsid))
				.build();
		return userData;
	}
	
}
