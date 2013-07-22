package uk.ac.cam.signups.forms;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.FormParam;

import org.hibernate.Session;

import uk.ac.cam.signups.models.Deadline;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;

public class DeadlineForm {
	@FormParam("title") String title;
	@FormParam("datetime") String datetime;
	@FormParam("message") String message;
	@FormParam("users[]") String users;
	@FormParam("groups[]") String groups;
	
	public int handle(User currentUser) {		
		
		System.out.println("Deadline name: " + title);
		System.out.println("Deadline date: " + datetime);
		System.out.println("Deadline message: " + message);
		System.out.println("Deadline users: " + users);
		System.out.println("Deadline groups: " + groups);
		
//		Session session = HibernateUtil.getTransactionSession();
//		
//		// Create deadline prototype
//		Deadline deadline = new Deadline();
//		deadline.setTitle(title);
//		deadline.setDatetime(datetime);
//		deadline.setMessage(message);
//
//		// Set owner of the user to current user
//		deadline.setOwner(currentUser);
//
//		
//		// Create set of users for deadline from users field
//		User user;
//		Set<User> deadlineUsers = new HashSet<User>();
//		String[] crsids = users.split(",");
//		for(int i=0;i<crsids.length;i++){
//			// Register user (adds user to database if they don't exist
////			user = User.registerUser(crsids[i]);
//			// Add to set of users
////			deadlineUsers.add(user);
//		}		
//		// Create set of users for deadline from groups field
//		Group group;
//		String[] groupIds = groups.split(",");
//		for(int i=0;i<groupIds.length;i++){
//			// Get group users
//			group = 
//			// Register user (adds user to database if they don't exist
////			user = User.registerUser(crsids[i]);
//			// Add to set of users
////			deadlineUsers.add(user);
//		}	
////		
////		deadline.setUsers(deadlineUsers);
////		
////		session.save(deadline);
////		
////		// Add this group to the group members groups
////		for(User u : groupMembers){
////			Set<Group> subscriptions = u.getSubscriptions();
////			subscriptions.add(group);
////			session.update(u);
////		}
////		
////		return deadline.getId();
		
		return 0;
				
	}
}
