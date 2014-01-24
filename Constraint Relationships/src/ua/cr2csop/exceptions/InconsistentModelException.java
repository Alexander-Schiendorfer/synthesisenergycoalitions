package ua.cr2csop.exceptions;

/**
 * Exception class used to indicate that
 * some model definitions have not been implemented
 * correctly
 * @author alexander
 *
 */
public class InconsistentModelException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1403980047734911849L;
	
	public InconsistentModelException(String message) {
		super(message);
	}
	
	public InconsistentModelException(String message, Throwable cause) {
		super(message, cause);
	}
}
