package uk.ac.cam.signups.exceptions;

public class NotADosException extends Exception {

	/**
	 * 
	 */
  private static final long serialVersionUID = 1L;

  public NotADosException() {super("Not a Director of Studes.");}
	public NotADosException(String e) {
		super(e);
	}
}