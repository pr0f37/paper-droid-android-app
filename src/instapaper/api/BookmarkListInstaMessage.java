package instapaper.api;

import java.util.LinkedList;

import android.text.TextUtils;

import com.google.gson.Gson;

public class BookmarkListInstaMessage extends InstaMessage {
	private CredentialsInstaMessage credentialsMsg;
	private LinkedList<BookmarkInstaMessage> bookmarkMessagesList;
	public BookmarkListInstaMessage() {
		super("BookmarkList");
		bookmarkMessagesList = new LinkedList<BookmarkInstaMessage>();
	}
	
	public BookmarkListInstaMessage(CredentialsInstaMessage credMsg) {
		super("BookmarkList");
		bookmarkMessagesList = new LinkedList<BookmarkInstaMessage>();
		credentialsMsg = credMsg;
	}
	
	public BookmarkListInstaMessage(String msg){
		super("BookmarkList");
		bookmarkMessagesList = new LinkedList<BookmarkInstaMessage>();
		msg = msg.substring(1, msg.length()-1);
		String[] objects = TextUtils.split(msg, "\\},\\{");
		for (int i = 0; i < objects.length; i++)
			objects[i] = "{" + objects[i] + "}";
		
		credentialsMsg = new Gson().fromJson(objects[1], CredentialsInstaMessage.class);
		for (int i = 2; i < objects.length; i++) 
			bookmarkMessagesList.add(new Gson().fromJson(objects[i], BookmarkInstaMessage.class));
	}
	
	public void addBookmarkMessageToList(BookmarkInstaMessage bookmark) {
		bookmarkMessagesList.add(bookmark);
	}

	public CredentialsInstaMessage getCredentialsMessage() {
		return credentialsMsg;
	}

	public LinkedList<BookmarkInstaMessage> getBookmarkMessagesList() {
		return bookmarkMessagesList;
	}
	
}
