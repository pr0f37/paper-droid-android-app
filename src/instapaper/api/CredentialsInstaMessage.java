package instapaper.api;

public class CredentialsInstaMessage extends InstaMessage {
	private int user_id;
	private String username;
	private int subscription_is_active;
	public CredentialsInstaMessage(int user_id, String username) {
		super("user");
		this.user_id = user_id;
		this.username = username;
	}
	
	public int getUserId() {
		return user_id;
	}
	
	public String getUserName() {
		return username;
	}
	
	public int getSubscriptionStatus() {
		return subscription_is_active;
	}
}
