package instapaper.api;

public class BookmarkInstaMessage extends InstaMessage {
	private int bookmark_id;
	private String url;
	private String title;
	private String description;
	private int time;
	private int starred;
	private String private_source;
	private String hash;
	private float progress;
	private int progress_timestamp;
	
	public BookmarkInstaMessage(int bookmark_id, String url,	String title,
			String description, int time,  int starred, String private_source, String hash, int progress, int progress_timestamp){
		super("bookmark");
		this.bookmark_id = bookmark_id; 
		this.url = url;
		this.title = title;
		this.description = description;
		this.time = time;
		this.starred = starred;
		this.private_source = private_source;
		this.hash = hash;
		this.progress = progress;
		this.progress_timestamp = progress_timestamp;
	}

	public int getBookmarkId() {
		return bookmark_id;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public int getTime() {
		return time;
	}

	public int getStarred() {
		return starred;
	}

	public String getPrivateSource() {
		return private_source;
	}

	public String getHash() {
		return hash;
	}

	public float getProgress() {
		return progress;
	}

	public int getProgressTimestamp() {
		return progress_timestamp;
	}
	
	
}
