package instapaper.api;

import java.util.LinkedList;

import com.google.gson.Gson;

import android.text.TextUtils;

public class FoldersListInstaMessage extends InstaMessage {
	LinkedList<FolderInstaMessage> folders;
	public FoldersListInstaMessage(String msg){
		super("folders list");
		folders = new LinkedList<FolderInstaMessage>();
		if(!msg.isEmpty()){
			msg = msg.substring(1, msg.length()-1);
			String[] foldersString = TextUtils.split(msg, "\\},\\{");
			for (int i = 0; i < foldersString.length; i++)
			{
				foldersString[i] = "{" + foldersString[i] + "}";
				folders.add(new Gson().fromJson(foldersString[i], FolderInstaMessage.class));
			}	
		}
		
	}
	public LinkedList<FolderInstaMessage> getFolders() {
		return folders;
	}
	
}
 