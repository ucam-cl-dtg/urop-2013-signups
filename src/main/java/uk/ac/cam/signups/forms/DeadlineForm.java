package uk.ac.cam.signups.forms;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.FormParam;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.signups.models.Deadline;
import uk.ac.cam.signups.models.Group;
import uk.ac.cam.signups.models.User;
import uk.ac.cam.signups.util.HibernateUtil;
import uk.ac.cam.signups.util.LDAPProvider;

public class DeadlineForm {
	@FormParam("title") String title;
	@FormParam("date") String date;
	@FormParam("hour") String hour;
	@FormParam("minute") String minute;
	@FormParam("message") String message;
	@FormParam("url") String url;
	@FormParam("users[]") String users;
	@FormParam("groups[]") String groups;
	
	//Logger
	private static Logger log = LoggerFactory.getLogger(DeadlineForm.class);
	
	public int handle(User currentUser) {		
		
		System.out.println("Date: " + date);
		System.out.println("Hour: " + hour);
		System.out.println("Minute: " + minute);
		
		Session session = HibernateUtil.getTransactionSession();
		
		// Create deadline prototype
		Deadline deadline = new Deadline();
		deadline.setTitle(title);
		
		// Set defaults for optional fields
		if(message==null){
			deadline.setMessage("No description");
		} else {
			deadline.setMessage(message);			
		}
		if(url==null){
			deadline.setURL("none");
		} else {
			deadline.setURL(url);			
		}

		// Set owner of the user to current user
		deadline.setOwner(currentUser);
		
		// Format and set date
		String datetime = date;
		datetime += " " + hour + ":" + minute;
		System.out.println("Datetime: " + datetime);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		try {
			cal.setTime(sdf.parse(datetime));
		} catch (Exception e) {
			log.error("e.getMessage()" +  ": error parsing date");
		}
		deadline.setDatetime(cal);
		
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
		
		deadline.setUsers(deadlineUsers);
		
		session.save(deadline);
		
		return deadline.getId();
				
	}
}
