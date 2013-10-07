package uk.ac.cam.signups.exceptions;

import uk.ac.cam.signups.models.User;

public class AuthorizationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String crsid;

	public AuthorizationException() {
		super("User cannot be authenticated.");
	}

	public AuthorizationException(String msg) {
		super(msg);
	}

	public AuthorizationException(User u) {
		super();
		this.crsid = u.getCrsid();
	}

	public AuthorizationException(String msg, User u) {
		super(msg);
		this.crsid = u.getCrsid();
	}

	public String getCrsid() {
		return this.crsid;
	}
}