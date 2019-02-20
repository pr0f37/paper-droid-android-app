package instapaper.api;

public class InstaMessage {
	protected String _type;
	InstaMessage(String type){
		_type = type;
	}
	
	public void setType(String type) {
		_type = type;
	}
	
	public String getType(String type) {
		return _type;
	}
}
