package uk.ac.cam.signups.forms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.FormParam;

import org.hibernate.Query;
import org.hibernate.Session;

import com.googlecode.htmleasy.RedirectException;

import uk.ac.cam.signups.helpers.LDAPQueryHelper;
import uk.ac.cam.signups.models.Deadline;
import uk.ac.cam.signups.models.Group;
import uk.ac.cam.signups.models.Row;
import uk.ac.cam.signups.models.Slot;
import uk.ac.cam.signups.models.Type;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;

public class GroupForm {
	@FormParam("title") String title;
	@FormParam("users[]") String users;
	@FormParam("import_id") String import_id;
	
	public int handle(User currentUser) {		
		
		parseForm();
		
		Session session = HibernateUtil.getTransactionSession();
		
		// Create group prototype
		Group group = new Group();
		group.setTitle(title);

		// Set owner of the user to current user
		group.setOwner(currentUser);

		
		// Create set of users for group
		Set<User> groupMembers = new HashSet<User>();
		if(!users.equals("")){
			User user;		
			String[] crsids = users.split(",");
			for(int i=0;i<crsids.length;i++){
				// Register user (adds user to database if they don't exist
				user = User.registerUser(crsids[i]);
				// Add to set of users
				groupMembers.add(user);
			}		
		}
		
		group.setUsers(groupMembers);
		
		session.save(group);
		
		// Add this group to the group members groups
		for(User u : groupMembers){
			Set<Group> subscriptions = u.getSubscriptions();
			subscriptions.add(group);
			session.update(u);
		}
		
		return group.getId();
				
	}

	public int handleUpdate(User currentUser, int id) {		
		Session session = HibernateUtil.getTransactionSession();
		
		// Get the group to edit
		Group group = Group.getGroup(id);
	  	
		// Check the owner is current user
		if(!group.getOwner().equals(currentUser)){
			throw new RedirectException("/app/#signapp/deadlines");
		}
		
		// Set new values
		group.setTitle(title);
		
		// Create new set of users for group
		Set<User> groupMembers = new HashSet<User>();
		if(!users.equals("")){
			User user;		
			String[] crsids = users.split(",");
			for(int i=0;i<crsids.length;i++){
				// Register user (adds user to database if they don't exist
				user = User.registerUser(crsids[i]);
				// Add to set of users
				groupMembers.add(user);
			}		
		}
		
		group.setUsers(groupMembers);
		
		session.update(group);
		
		return group.getId();
				
	}
	
	public int handleImport(User currentUser) {	
		
		parseForm();
		
		if(import_id==null||import_id.equals("")){
			throw new RedirectException("/app/#signapp/groups");
		}
		
		Session session = HibernateUtil.getTransactionSession();
		
		// Get group info from LDAP
		String title = LDAPQueryHelper.getGroupName(import_id);
		List<String> members = LDAPQueryHelper.getGroupMembers(import_id);
		
		// Create group prototype
		Group group = new Group();
		group.setTitle(title);

		// Set owner of the user to current user
		group.setOwner(currentUser);
		
		// Create set of users for group
		User user;
		Set<User> groupMembers = new HashSet<User>();
		for(String m : members){
			// Register user (adds user to database if they don't exist
			user = User.registerUser(m);
			// Add to set of users
			groupMembers.add(user);
		}	
		
		group.setUsers(groupMembers);
		
		session.save(group);
		
		// Add this group to the group members groups
		for(User u : groupMembers){
			Set<Group> subscriptions = u.getSubscriptions();
			subscriptions.add(group);
			session.update(u);
		}
		
		return group.getId();
				
	}

	public void parseForm() {
		
		// Check for empty fields
		if(title==null||title.equals("")){ this.title = "Untitled Group"; }
		if(users==null||users.equals("")){ this.users = ""; }
				
	}
	
}
