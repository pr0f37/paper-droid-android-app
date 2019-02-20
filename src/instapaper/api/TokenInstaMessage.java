package instapaper.api;

public class TokenInstaMessage extends InstaMessage {
	String _token;
	String _secret;
	public TokenInstaMessage(String token, String secret){
		super("token");
		_token = token;
		_secret = secret;
	}
	
	
	public String getToken() {
		return _token;
	}
	
	public String getSecret() {
		return _secret;
	}
}
