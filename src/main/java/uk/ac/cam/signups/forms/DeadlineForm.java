package uk.ac.cam.signups.forms;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.FormParam;

import org.hibernate.Query;
import org.hibernate.Session;

import uk.ac.cam.signups.models.Deadline;
import uk.ac.cam.signups.models.Group;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;

public class DeadlineForm {
	@FormParam("title") String title;
	@FormParam("datetime") String datetime;
	@FormParam("message") String message;
	@FormParam("users[]") String users;
	@FormParam("groups[]") String groups;
	
	public int handle(User currentUser) {		
		
		Session session = HibernateUtil.getTransactionSession();
		
		// Create deadline prototype
		Deadline deadline = new Deadline();
		deadline.setTitle(title);
		deadline.setDatetime(Calendar.getInstance());
		deadline.setMessage(message);

		// Set owner of the user to current user
		deadline.setOwner(currentUser);

		
		// Create set of users
		Set<User> deadlineUsers = new HashSet<User>();
		
		// Add users from users field
		if(!users.equals("")){
			User user;
			String[] crsids = users.split(",");
			for(int i=0;i<crsids.length;i++){
				// Register user (adds user to database if they don't exist
				user = User.registerUser(crsids[i]);
				// Add to set of users
				deadlineUsers.add(user);
			}		
		}
			
		// Add users from groups field
		if(!groups.equals("")){
			Group group;
			Set<User> groupUsers;
			String[] groupIds = groups.split(",");
			System.out.println("Ok so far");
			for(int i=0;i<groupIds.length;i++){
				// Get group users
				Query getGroup = session.createQuery("from Group where id = :id").setParameter("id", Integer.parseInt(groupIds[i]));
			  	group = (Group) getGroup.uniqueResult();	
			  	groupUsers = group.getUsers();
			  	for(User u : groupUsers){
					// Add user to deadline users set
					deadlineUsers.add(u);
			  	}
			}	
		}
		
		for(User u : deadlineUsers){
			System.out.print(u.getCrsid() + " ");
		}
		
		deadline.setUsers(deadlineUsers);
		
		session.save(deadline);
		
		return deadline.getId();
				
	}
}
