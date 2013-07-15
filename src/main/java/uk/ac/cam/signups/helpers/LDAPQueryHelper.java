package uk.ac.cam.signups.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.util.LDAPProvider;
import uk.ac.cam.signups.util.UserLookupManager;

public class LDAPQueryHelper {

	private static Logger log = LoggerFactory.getLogger(LDAPQueryHelper.class);
	
	public String getDisplayName(String crsid){
			log.debug("Retrieving display name for user " + crsid + "from LDAP");
		return LDAPProvider.getUniqueResult(crsid, "displayName");
	}
	
	public String getSurname(String crsid){
		log.debug("Retrieving display name for user " + crsid + "from LDAP");
	return LDAPProvider.getUniqueResult(crsid, "displayName");	
	}
	
}
