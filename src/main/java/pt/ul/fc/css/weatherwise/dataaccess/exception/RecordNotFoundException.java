package pt.ul.fc.css.weatherwise.dataaccess.exception;

public class RecordNotFoundException extends PersistenceException {

	private static final long serialVersionUID = 4484081634671467186L;

	public RecordNotFoundException(String message) {
		super (message);
	}

	public RecordNotFoundException(String message, Exception e) {
		super (message, e);
	}
}