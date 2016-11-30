package models.transfer;

import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import play.data.validation.Constraints.Required;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.NotificationEventName;
import io.swagger.annotations.ApiModel;

@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="NotificationSubscriptionTransfer", description="Basic Notification Subscription")
public class NotificationSubscriptionTransfer {
	private String eventId;
	private String alertEndpoint;
	private String endpointType;
	@Required
	private UUID origin;
	@Required
	@Enumerated(EnumType.STRING)
	private NotificationEventName eventName;
	
	public NotificationSubscriptionTransfer() {
		super();
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getAlertEndpoint() {
		return alertEndpoint;
	}

	public void setAlertEndpoint(String alertEndpoint) {
		this.alertEndpoint = alertEndpoint;
	}

	public String getEndpointType() {
		return endpointType;
	}

	public void setEndpointType(String endpointType) {
		this.endpointType = endpointType;
	}

	public UUID getOrigin() {
		return origin;
	}

	public void setOrigin(UUID origin) {
		this.origin = origin;
	}

	public NotificationEventName getEventName() {
		return eventName;
	}

	public void setEventName(NotificationEventName eventName) {
		this.eventName = eventName;
	}

	public void setEventIdFromOriginAndEventName() {
		this.eventId = this.origin+"_"+this.eventName;
	}
}
