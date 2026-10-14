package pt.ul.fc.css.weatherwise.dataaccess.exception;

public class PersistenceException extends Exception {

	private static final long serialVersionUID = -3416001628323171383L;

	public PersistenceException(String message) {
		super (message);
	}

	public PersistenceException(String message, Exception e) {
		super (message, e);
	}
}