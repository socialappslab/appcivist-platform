package models;

import com.avaje.ebean.annotation.DbJsonB;
import com.fasterxml.jackson.annotation.JsonBackReference;
import enums.SpaceTypes;
import enums.SubscriptionTypes;
import io.swagger.annotations.ApiModel;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.NotificationEventName;
import enums.ResourceSpaceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="NotificationEventSignal", description="A notification event signal is a single notification that is signaled (i.e., sent) to users who have subscribed to the event name in the resource space")
public class NotificationEventSignal extends AppCivistBaseModel {

	/*{
    [Other fields liek origin, originType, etc.],
		"spaceType" : "CAMPAIGN",
			"spaceId": "[UUID of the CAMPAIGN resourceSpace",
			"signalType": "[REGULAR|NEWSLETTER]",
			"eventId": "NEW_CONTRIBUTION_IDEA",
			"title" : "notification.new.contribution.idea.in.campaign",
			"text" : "notification.description.general.resource_new",
			"data" : "{"creation":"2017-08-27 17:11 PM GMT","lastUpdate":"2017-08-27 17:11 PM GMT","lang":"en","removed":false,"origin":"e1998630-4079-11e5-a151-feff819cdc9f","originType":"CAMPAIGN","eventName":"NEW_CONTRIBUTION_IDEA","originName":"Belleville - Paris PB 2016","title":"[AppCivist] New IDEA in Belleville - Paris PB 2016","resourceType":"IDEA","resourceUUID":"bd08f16f-ab4a-427e-b3aa-22823cb698bb","resourceTitle":"Testing idea","resourceText":"<p>this is my idea</p>","notificationDate":"2017-08-27 17:11 PM GMT","associatedUser":"Cristhian Parra","signaled":false}"
	}*/

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private ResourceSpaceTypes spaceType;

	@Enumerated(EnumType.STRING)
	private SubscriptionTypes signalType = SubscriptionTypes.REGULAR;

	@Enumerated(EnumType.STRING)
	private NotificationEventName eventId;

	private String title;
	private String text;

	@DbJsonB
	@Column(name = "data")
	@JsonInclude(Include.NON_EMPTY)
	private Map<String, Object> data;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="signal", fetch=FetchType.LAZY)
	private List<NotificationEventSignalUser> notificationsEventsSignalsUsers = new ArrayList<>();

	// Information about the space where the event happened
	/*private UUID origin;
	@Enumerated(EnumType.STRING)
	private ResourceSpaceTypes originType;
	@Enumerated(EnumType.STRING)
	private NotificationEventName eventName;
	private String originName;


	// Information about the resource related to this event
	private String resourceType;
	private UUID resourceUUID;
	private String resourceTitle;
	private String resourceText;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
	private Date notificationDate = new Date();
	private String associatedUser;
	private Boolean signaled = false;*/


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ResourceSpaceTypes getSpaceType() {
		return spaceType;
	}

	public void setSpaceType(ResourceSpaceTypes spaceType) {
		this.spaceType = spaceType;
	}

	public SubscriptionTypes getSignalType() {
		return signalType;
	}

	public void setSignalType(SubscriptionTypes signalType) {
		this.signalType = signalType;
	}

	public NotificationEventName getEventId() {
		return eventId;
	}

	public void setEventId(NotificationEventName eventId) {
		this.eventId = eventId;
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

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public static Finder<Long, NotificationEventSignal> find = new Finder<>(
			NotificationEventSignal.class);

	public NotificationEventSignal() {
		super();
	}
	public List<NotificationEventSignalUser> getNotificationsEventsSignalsUsers() {
		return notificationsEventsSignalsUsers;
	}

	public void setNotificationsEventsSignalsUsers(List<NotificationEventSignalUser> notificationsEventsSignalsUsers) {
		this.notificationsEventsSignalsUsers = notificationsEventsSignalsUsers;
	}

	public void addNotificationEventSignalUser(NotificationEventSignalUser signal){
		this.notificationsEventsSignalsUsers.add(signal);
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
