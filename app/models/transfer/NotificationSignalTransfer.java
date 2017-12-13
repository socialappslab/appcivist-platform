package models.transfer;

import io.swagger.annotations.ApiModel;
import java.util.Map;

@ApiModel(value="NotificationSignalTransfer", description="Basic Notification Signals")
public class NotificationSignalTransfer {
	private String eventId;
	private String title;
	private String text;
	private Map<String,Object> data;

	private String spaceId;
	private String spaceType;
	private String signalType;


	public String getSpaceType() {
		return spaceType;
	}

	public void setSpaceType(String spaceType) {
		this.spaceType = spaceType;
	}

	public String getSignalType() {
		return signalType;
	}

	public void setSignalType(String signalType) {
		this.signalType = signalType;
	}



	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}


	public NotificationSignalTransfer() {
		super();
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String eventTitle) {
		this.title = eventTitle;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Map<String,Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}	
}
