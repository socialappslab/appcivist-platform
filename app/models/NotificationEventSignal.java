package models;

import com.avaje.ebean.*;
import com.avaje.ebean.annotation.DbJsonB;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.*;
import io.swagger.annotations.ApiModel;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
	@JsonIgnore
	private List<NotificationEventSignalUser> notificationsEventsSignalsUsers = new ArrayList<>();

	@Column(name = "rich_text")
	private String richText;

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

	public String getRichText() {
		return richText;
	}

	public void setRichText(String richText) {
		this.richText = richText;
	}

	public static NotificationEventSignal read(Long id) {
		return find.ref(id);
	}

	public static List<NotificationEventSignal> findAll() {
		return find.all();
	}

	public static List<NotificationEventSignal> findByOriginUuid(String spaceId) {
		ExpressionList<NotificationEventSignal> q = find.where();
		q.eq("data->>'origin'", spaceId)
				.eq("signalType", SubscriptionTypes.NEWSLETTER.name())
				.orderBy("creation desc");
		return q.findPagedList(0, 1).getList();
	}

	public static List<NotificationEventSignal> findLatestByOriginUuid(String spaceId, Integer days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, - days);
		ExpressionList<NotificationEventSignal> q = find.where();
		q.eq("data->>'origin'", spaceId)
				.eq("signalType", SubscriptionTypes.REGULAR.name())
				.ge("creation", calendar.getTime())
				.orderBy("creation desc");
		return q.findList();
	}

	public static List<NotificationEventSignal> findLastNtByOriginUuid(String originId, String signalType, Integer number) {
		return findLastNtByOriginUuidWithFilteredEvents(originId,signalType,number,null);
	}


	public static List<NotificationEventSignal> findLastNtByOriginUuidWithFilteredEvents
            (String originId, String signalType, Integer number, List<String> filteredEvents) {
		ExpressionList<NotificationEventSignal> q = null;
		String rawQ =   " select s.id, s.creation, s.last_update, s.lang, s.removal, s.removed, " +
                        " s.space_type, s.signal_type, s.event_id, s.text, s.title, s.data, s.rich_text " +
                        " from notification_event_signal s " +
                        " order by s.creation desc";

        RawSql rawSql = RawSqlBuilder.parse(rawQ).create();
        q = find.setRawSql(rawSql).where();
        q.eq("s.signal_type",signalType);
        q.eq("s.data->>'origin'", originId);
        q.setMaxRows(number);

        for(String filter : filteredEvents) {
            q.not(Expr.like("s.event_id", filter));
        }

		return q.findList();
	}

	public static List<NotificationEventSignal> findLastNtByOriginUuidAndFilters(String spaceId, Integer number) {
		ExpressionList<NotificationEventSignal> q = find.where();
		q.eq("data->>'origin'", spaceId)
				.eq("signalType", SubscriptionTypes.REGULAR.name())
				.orderBy("creation desc")
				.setMaxRows(number);
		return q.findList();
	}

	public static List<NotificationEventSignal> findLatestIdeasByOriginUuid(String spaceId, Integer days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, - days);
		ExpressionList<NotificationEventSignal> q = find.where();
		q.or(com.avaje.ebean.Expr.eq("eventId", NotificationEventName.UPDATED_CONTRIBUTION_IDEA),
				com.avaje.ebean.Expr.eq("eventId", NotificationEventName.UPDATED_CONTRIBUTION_PROPOSAL));
		q.eq("data->>'origin'", spaceId)
				.eq("signalType", SubscriptionTypes.REGULAR.name())
				.ge("creation", calendar.getTime())
				.orderBy("creation desc");
		return q.findList();
	}

	public static List<NotificationEventSignal> findLatesWGtBySpaceUuid(String spaceId, Integer days) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, - days);
		ExpressionList<NotificationEventSignal> q = find.where();
		q.eq("data->>'origin'", spaceId)
				.eq("signalType", SubscriptionTypes.REGULAR.name())
				.eq("spaceType", ResourceSpaceTypes.WORKING_GROUP.name())
				.ge("creation", calendar.getTime())
				.orderBy("creation desc");
		return q.findList();
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
