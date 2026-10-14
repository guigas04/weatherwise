package pt.ul.fc.css.weatherwise.business.exception;

public class ApplicationException extends Exception {

	private static final long serialVersionUID = -3416001628323171383L;

	public ApplicationException(String message) {
		super (message);
	}

	public ApplicationException(String message, Exception e) {
		super (message, e);
	}
}