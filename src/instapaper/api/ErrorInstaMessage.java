package instapaper.api;

public class ErrorInstaMessage extends InstaMessage {
	private int _errorCode;
	private String _message;
	public ErrorInstaMessage(int errorCode, String message) {
		super("error");
		_errorCode = errorCode;
		_message = message;
	}
	
	public int getErrorCode() {
		return _errorCode;
	}
	
	public void setErrorCode(int errorCode) {
		_errorCode = errorCode;
	}
	
	public String getMessage() {
		return _message;
	}
	
	public void setMessage(String message) {
		_message = message;
	}
}
