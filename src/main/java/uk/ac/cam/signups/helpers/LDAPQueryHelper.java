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

/**
 * @author      Holly Priest <hp343@cam.ac.uk>
 * @version     1                
 * A class providing all static methods to make queries to LDAP
 * Provides defaults to return in case of error or if nothing is found
 * 
 */
public class LDAPQueryHelper {
	
	private static Logger log = LoggerFactory.getLogger(LDAPQueryHelper.class);

	/**
	 * Check if user with given crsid exists
	 * @return String displayName
	 */
	public static String checkCRSID(String crsid){
			log.debug("Checking user with " + crsid + " exists in LDAP");
			String uid = LDAPProvider.uniqueQuery("uid", crsid, "uid", "people");	
			if(uid==null){
				log.debug("No registedName found for user " + crsid);
				return null;
			}
		return uid;
	}
	
	/**
	 * Get users display name
	 * @return String displayName
	 */
	public static String getRegisteredName(String crsid){
			log.debug("Retrieving display name for " + crsid + " from LDAP");
			String registeredName = LDAPProvider.uniqueQuery("uid", crsid, "cn", "people");	
			if(registeredName==null){
				log.debug("No registedName found for user " + crsid);
				return "Annonymous";
			}
		return registeredName;
	}
	
	/**
	 * Get users surname
	 * @return String surname
	 */
	public static String getSurname(String crsid){
			log.debug("Retrieving surname for " + crsid + " from LDAP");
			String surname = LDAPProvider.uniqueQuery("uid", crsid, "sn", "people");	

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
			String email = LDAPProvider.uniqueQuery("uid", crsid, "mail", "people");	

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
			List<String> statusList = LDAPProvider.multipleQuery("uid", crsid, "misAffiliation", "people");
			if(statusList==null){ System.out.println("no misAffiliation");}
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
			String photo;
			log.debug("Retrieving photo for " + crsid + " from LDAP");
			// Set default as "none" to convert to no photo image in soy
			List<String> photoList = LDAPProvider.multipleQuery("uid", crsid, "jpegPhoto", "people");
			if(photoList==null||photoList.isEmpty()){
				photo="none";
			} else {
				photo = photoList.get(0);
			}
			
		return photo;
	}
	
	/**
	 * Gets a list of Institutions associated with user
	 * @return String institution (first institution found)
	 */
	public static String getInstitution(String crsid){
			String institution;
			log.debug("Retrieving institution for " + crsid + " from LDAP");
			//Default is no institution
			List<String> inList = LDAPProvider.multipleQuery("uid", crsid, "ou", "people");
			if(inList==null||inList.isEmpty()){
				institution="no institutions";
			} else {
				institution = inList.get(0);
			}
		return institution;
	}
	
	/**
	 * Gets the name of a group
	 * @return String group name
	 */
	public static String getGroupName(String id){
			log.debug("Retrieving name for group " + id + " from LDAP");
			String group = LDAPProvider.uniqueQuery("groupID", id, "groupTitle", "groups");	
	
			if(group==null){
				log.debug("No group found for id " + id);
				return "No group";
			}
		return group;
	}
	
	/**
	 * Gets a member CRSIDs of a group
	 * @return List<String> members
	 */
	public static List<String> getGroupMembers(String id){
			List<String> groupMembers = new ArrayList<String>();
			log.debug("Retrieving members of group id " + id + " from LDAP");
			//Default is no institution
			groupMembers = LDAPProvider.multipleQuery("groupID", id, "uid", "groups");

		return groupMembers;
	}
	
	/**
	 * Partial query of users by CRSID
	 * @return ImmutableMap userData
	 */
	public static List queryCRSID(String x){
		ArrayList<ImmutableMap<String, ?>> crsidMatches = new ArrayList<ImmutableMap<String, ?>>();
		log.debug("Retrieving crsids matching " + x + " from LDAP");
		crsidMatches = (ArrayList<ImmutableMap<String, ?>>) LDAPProvider.partialUserQuery(x, "uid");
	return crsidMatches;
	}
	
	/**
	 * Partial query of groups by group name
	 * @return ImmutableMap userData
	 */
	public static List queryGroups(String x){
		ArrayList<ImmutableMap<String, ?>> groupMatches = new ArrayList<ImmutableMap<String, ?>>();
		log.debug("Retrieving groups matching " + x + " from LDAP");
		groupMatches = (ArrayList<ImmutableMap<String, ?>>) LDAPProvider.partialGroupQuery(x, "groupTitle");
		return groupMatches;
	}
	
	/**
	 * Queries and builds a soy-ready immutable map of all data related to user
	 * @return ImmutableMap userData
	 */
	public static ImmutableMap<String, ?> getAll(String crsid){
		// Create a map of all the users data using builder
		log.debug("Getting all data for user " + crsid);
		ImmutableMap<String, ?> userData = new ImmutableMap.Builder<String, String>()
				.put("crsid", crsid)
				.put("fullname", getRegisteredName(crsid))
				.put("surname", getSurname(crsid))
				.put("email", getEmail(crsid))
				.put("status", getStatus(crsid))
				.put("photo", getPhoto(crsid))
				.put("institution", getInstitution(crsid))
				.build();
		return userData;
	}
	

	
}
