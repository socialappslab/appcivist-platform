package models;

import io.swagger.annotations.ApiModel;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.NotificationEventName;
import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="NotificationEventSignal", description="A notification event signal is a single notification that is signaled (i.e., sent) to users who have subscribed to the event name in the resource space")
public class NotificationEventSignal extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long id;
	private UUID uuid;
	
	// Information about the space where the event happened
	private UUID origin;
	@Enumerated(EnumType.STRING)
	private ResourceSpaceTypes originType;
	@Enumerated(EnumType.STRING)
	private NotificationEventName eventName;
	private String originName;
	private String title; // TODO: needed?
	private String text; // TODO: needed?

	// Information about the resource related to this event
	private String resourceType;
	private UUID resourceUUID;
	private String resourceTitle;
	private String resourceText;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date notificationDate = new Date();
	private String associatedUser;
	private Boolean signaled = false;

	public static Finder<Long, NotificationEventSignal> find = new Finder<>(
			NotificationEventSignal.class);

	public NotificationEventSignal() {
		super();
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Long getId () {
		return this.id;
	}
	
	public void setId(Long id) {
		this.id = id; 
	}
	
	public UUID getOrigin() {
		return origin;
	}

	public void setOrigin(UUID origin) {
		this.origin = origin;
	}

	public ResourceSpaceTypes getOriginType() {
		return originType;
	}

	public void setOriginType(ResourceSpaceTypes originType) {
		this.originType = originType;
	}

	public NotificationEventName getEventName() {
		return eventName;
	}

	public void setEventName(NotificationEventName eventName) {
		this.eventName = eventName;
	}

	public String getOriginName() {
		return originName;
	}

	public void setOriginName(String originName) {
		this.originName = originName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public UUID getResourceUUID() {
		return resourceUUID;
	}

	public void setResourceUUID(UUID resourceUUID) {
		this.resourceUUID = resourceUUID;
	}

	public String getResourceTitle() {
		return resourceTitle;
	}

	public void setResourceTitle(String resourceTitle) {
		this.resourceTitle = resourceTitle;
	}

	public String getResourceText() {
		return resourceText;
	}

	public void setResourceText(String resourceText) {
		this.resourceText = resourceText;
	}

	public Date getNotificationDate() {
		return notificationDate;
	}

	public void setNotificationDate(Date date) {
		this.notificationDate = date;
	}

	public String getAssociatedUser() {
		return associatedUser;
	}

	public void setAssociatedUser(String associatedUser) {
		this.associatedUser = associatedUser;
	}

	public Boolean getSignaled() {
		return signaled;
	}

	public void setSignaled(Boolean signaled) {
		this.signaled = signaled;
	}

	public static NotificationEventSignal read(Long id) {
		return find.ref(id);
	}

	public static List<NotificationEventSignal> findAll() {
		return find.all();
	}

	public static NotificationEventSignal create(NotificationEventSignal notification) {
		notification.save();
		notification.refresh();
		return notification;
	}

	public static NotificationEventSignal createObject(NotificationEventSignal notification) {
		notification.save();
		return notification;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}
}
