package instapaper.api;

public class FolderInstaMessage extends InstaMessage {
	private int folder_id;
	private String title;
	private int sync_to_mobile;
	private int position;
	public FolderInstaMessage(int folder_id, String title, int sync_to_mobile, int position){
		super("folder");
		this.folder_id = folder_id;
		this.title = title;
		this.sync_to_mobile = sync_to_mobile;
		this.position = position;
	}
	public int getFolder_id() {
		return folder_id;
	}
	public String getTitle() {
		return title;
	}
	public int getSync_to_mobile() {
		return sync_to_mobile;
	}
	public int getPosition() {
		return position;
	}
	
}
