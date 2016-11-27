package models.transfer;

import io.swagger.annotations.ApiModel;

@ApiModel(value="NotificationSignalTransfer", description="Basic Notification Signals")
public class NotificationSignalTransfer {
	private String eventId;
	private String title;
	private String text;
	private String data; 
	
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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}	
}
