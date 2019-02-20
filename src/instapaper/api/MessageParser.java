package instapaper.api;

import android.text.TextUtils;

import com.google.gson.Gson;

public class MessageParser {

	public static InstaMessage getToken(String msg) {
		if (msg.equalsIgnoreCase("Invalid xAuth credentials.") || msg.equalsIgnoreCase("Missing or invalid request token."))
			return new ErrorInstaMessage(1, "Invalid username/password");
		
		String[] resp = TextUtils.split(msg, "&");
		String token = (TextUtils.split(resp[0],"="))[1];
		String secret = (TextUtils.split(resp[1],"="))[1];
		return new TokenInstaMessage(token, secret);
	}
	
	public static InstaMessage getCredentials(String msg) {
		msg = msg.substring(1, msg.length()-1);
		if (isError(msg))
			return new Gson().fromJson(msg, ErrorInstaMessage.class);
		return new Gson().fromJson(msg, CredentialsInstaMessage.class);
	}
	
	public synchronized static InstaMessage getBookmark(String msg) {
		msg = msg.substring(1, msg.length()-1);
		if (isError(msg))
			return new Gson().fromJson(msg, ErrorInstaMessage.class);
		return new Gson().fromJson(msg, BookmarkInstaMessage.class);
	}
	
	
	public synchronized static InstaMessage getBookmarkList(String msg) {
		msg = msg.substring(1, msg.length()-1);
		if (isError(msg))
			return new Gson().fromJson(msg, ErrorInstaMessage.class);
//		msg = msg.substring(1, msg.length()-1);
//		String[] objects = TextUtils.split(msg, "\\},\\{");
//		for (int i = 0; i < objects.length; i++)
//			objects[i] = "{" + objects[i] + "}";
//		
//		BookmarkListInstaMessage bkmListMsg = new BookmarkListInstaMessage(new Gson().fromJson(objects[1], CredentialsInstaMessage.class));
//		for (int i = 2; i < objects.length; i++) {
//			BookmarkInstaMessage bkm = new Gson().fromJson(objects[i], BookmarkInstaMessage.class);
//			bkmListMsg.addBookmarkMessageToList(bkm);
//		}
		return new BookmarkListInstaMessage(msg);
	}
	
	public static InstaMessage getResult(String msg) {
		msg = msg.substring(1, msg.length()-1);
		if (isError(msg))
			return new Gson().fromJson(msg, ErrorInstaMessage.class);
		
		return new InstaMessage("success");
	}
	
	public static InstaMessage getBookmarkText(String msg) {
		msg = msg.substring(1, msg.length()-1);
		if (isError(msg))
			return new Gson().fromJson(msg, ErrorInstaMessage.class);
		return new BookmarkTextInstaMessage(msg);
	}
	
	public synchronized static InstaMessage getFoldersList(String msg) {
		msg = msg.substring(1, msg.length()-1);
		if (isError(msg))
			return new Gson().fromJson(msg, ErrorInstaMessage.class);
		return new FoldersListInstaMessage(msg);
	}
	
	public static InstaMessage getFolder(String msg) {
		msg = msg.substring(1, msg.length()-1);
		if (isError(msg))
			return new Gson().fromJson(msg, ErrorInstaMessage.class);
		return new Gson().fromJson(msg, FolderInstaMessage.class);
	}
	
	public static boolean isError(String msg) {
		if (msg.startsWith("{\"type\":\"error\""))	
				return true;
		return false;
	}
	
}
