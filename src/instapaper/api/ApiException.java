package instapaper.api;

public class ApiException extends Exception {

	private static final long serialVersionUID = 1L;
	private String _message;
	private int _code;
	public ApiException(String message, int code){
		super();
		_message = message;
		_code = code;
	}
	
	public ApiException(ErrorInstaMessage errorMessage){
		super();
		_message = errorMessage.getMessage();
		_code = errorMessage.getErrorCode();
	}

	public String getMessage() {
		return _message;
	}
	
	public int getCode() {
		return _code;
	}
	
}
