package models;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.NotificationEventName;
import enums.ResourceSpaceTypes;

//TODO: persist notifications for history @Entity(name = "notification_event")
@JsonInclude(Include.NON_EMPTY)
public class NotificationEvent extends AppCivistBaseModel {
	// TODO: persist notifications for history @Id
	// TODO: persist notifications for history @GeneratedValue
	// TODO: persist notifications for history private Long id;
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
	private Date date = new Date();
	private String associatedUser;

	public static Finder<Long, NotificationEvent> find = new Finder<>(
			NotificationEvent.class);

	public NotificationEvent() {
		super();
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getAssociatedUser() {
		return associatedUser;
	}

	public void setAssociatedUser(String associatedUser) {
		this.associatedUser = associatedUser;
	}

	public static NotificationEvent read(Long id) {
		return find.ref(id);
	}

	public static List<NotificationEvent> findAll() {
		return find.all();
	}

	public static NotificationEvent create(NotificationEvent notification) {
		notification.save();
		notification.refresh();
		return notification;
	}

	public static NotificationEvent createObject(NotificationEvent notification) {
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
