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

import com.googlecode.htmleasy.RedirectException;

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
	
	public int handleCreate(User currentUser) {		
		
		parseForm();
		
		Session session = HibernateUtil.getTransactionSession();
		
		// Create deadline prototype
		Deadline deadline = new Deadline();
		deadline.setTitle(title);
		deadline.setOwner(currentUser);
		deadline.setMessage(message);
		deadline.setURL(url);
		
		// Format and set date
		String datetime = date;
		datetime += " " + hour + ":" + minute;
		
		Calendar cal = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		try {
			cal.setTime(sdf.parse(datetime));
		} catch (Exception e) {
			log.error("e.getMessage()" +  ": error parsing date");
		}
		
		//check its not in the past
		if(cal.getTime().before(today.getTime())){
			throw new RedirectException("/app/#signapp/deadlines/error/4");
		}
		
		deadline.setDatetime(cal);
		
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
			Set<User> groupUsers;
			String[] groupIds = groups.split(",");
			System.out.println("Ok so far");
			for(int i=0;i<groupIds.length;i++){
				// Get group users
				groupUsers = Group.getGroup(Integer.parseInt(groupIds[i])).getUsers();
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
	
	public int handleUpdate(User currentUser, int id) {		
		
		parseForm();
		
		Session session = HibernateUtil.getTransactionSession();
		
		// Get the deadline to edit
		Deadline deadline = Deadline.getDeadline(id);
	  	
		// Check the owner is current user
		if(!deadline.getOwner().equals(currentUser)){
			throw new RedirectException("/app/#signapp/deadlines");
		}
		
		// Set new values
		deadline.setTitle(title);
		deadline.setMessage(message);	
		deadline.setMessage(url);	
		
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
			Set<User> groupUsers;
			String[] groupIds = groups.split(",");
			for(int i=0;i<groupIds.length;i++){
				// Get group users
			  	groupUsers = Group.getGroup(Integer.parseInt(groupIds[i])).getUsers();
			  	for(User u : groupUsers){
					// Add user to deadline users set
					deadlineUsers.add(u);
			  	}
			}	
		}
		
		deadline.setUsers(deadlineUsers);
		
		session.update(deadline);
		
		return deadline.getId();	
	}
	
	public void parseForm() {
				
		// Check for empty fields
		if(title==null||title.equals("")){ this.title = "Untitled Deadline"; }
		if(date==null||date.equals("")){ this.date = "01/01/1991"; }
		if(hour==null||hour.equals("")){ this.hour = "0"; }
		if(minute==null||minute.equals("")){ this.minute = "0"; }
		if(message==null||message.equals("")){ this.message = "No description"; }		
		if(url==null||url.equals("")){ this.url = "none"; }	
		if(users==null||users.equals("")){ this.users = ""; }		
		if(groups==null||groups.equals("")){ this.groups = ""; }	
				
	}
	
}
