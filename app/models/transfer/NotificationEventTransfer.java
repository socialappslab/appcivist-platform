package models.transfer;

import io.swagger.annotations.ApiModel;

@ApiModel(value="NotificationEventTransfer", description="Basic Notification Events")
public class NotificationEventTransfer {
	private String eventId;
	private String title;
	
	public NotificationEventTransfer() {
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
}
