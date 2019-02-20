package instapaper.api;

public class BookmarkTextInstaMessage extends InstaMessage {
	private String messageBody;
	public BookmarkTextInstaMessage(String msg) {
		super("bookmark text");
		messageBody = msg;
	}
	
	public String getMessageBody() {
		return messageBody;
	}
}
