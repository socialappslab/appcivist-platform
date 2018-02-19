package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.DbJsonB;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import enums.ResourceSpaceTypes;
import enums.SubscriptionTypes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.transfer.NotificationSignalTransfer;

import javax.persistence.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value = "Subscription", description = "Subsription for Notifications Support")
public class Subscription extends Model {
    @Id
    @GeneratedValue
    @ApiModelProperty(hidden=true)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @JsonInclude(Include.NON_EMPTY)
    @Column(name = "space_id")
    private String spaceId;

    @JsonInclude(Include.NON_EMPTY)
    @Column(name = "space_type")
    @Enumerated(EnumType.STRING)
    private ResourceSpaceTypes spaceType;

    @JsonInclude(Include.NON_EMPTY)
    @Column(name = "subscription_type")
    @Enumerated(EnumType.STRING)
    private SubscriptionTypes subscriptionType;

    @Column(name = "newsletter_frecuency")
    private Integer newsletterFrecuency = 7;

    @DbJsonB
    @Column(name = "ignored_events")
    @JsonInclude(Include.NON_EMPTY)
    private Map<String, Boolean> ignoredEvents;

    @DbJsonB
    @JsonInclude(Include.NON_EMPTY)
    @Column(name = "disabled_services")
    private Map<String, Boolean> disabledServices;

    @Column(name = "default_service")
    private Integer defaultService = null;

    @Column(name = "default_identity")
    private String defaultIdentity = null;

    public static Finder<Long, Subscription> find = new Finder<>(Subscription.class);

    public Subscription() {
        super();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public ResourceSpaceTypes getSpaceType() {
        return spaceType;
    }

    public void setSpaceType(ResourceSpaceTypes spaceType) {
        this.spaceType = spaceType;
    }

    public SubscriptionTypes getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionTypes subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public Integer getNewsletterFrecuency() {
        return newsletterFrecuency;
    }

    public void setNewsletterFrecuency(Integer newsletterFrecuency) {
        this.newsletterFrecuency = newsletterFrecuency;
    }

    public Map<String, Boolean> getIgnoredEvents() {
        return ignoredEvents;
    }

    public void setIgnoredEvents(HashMap<String, Boolean> ignoredEvents) {
        this.ignoredEvents = ignoredEvents;
    }

    public Map<String, Boolean> getDisabledServices() {
        return disabledServices;
    }

    public void setDisabledServices(HashMap<String, Boolean> disabledServices) {
        this.disabledServices = disabledServices;
    }

    public Integer getDefaultService() {
        return defaultService;
    }

    public void setDefaultService(Integer defaultService) {
        this.defaultService = defaultService;
    }

    public String getDefaultIdentity() {
        return defaultIdentity;
    }

    public void setDefaultIdentity(String defaultIdentity) {
        this.defaultIdentity = defaultIdentity;
    }

    public static List<Subscription> findByUserId(User u) {
        com.avaje.ebean.Query<Subscription> q = find.where().eq("user.userId",u.getUuidAsString()).query();
        List<Subscription> membs = q.findList();
        return membs;
    }
  
    public static List<Subscription> findContributionSubscriptionsByUserId(User u) {
        com.avaje.ebean.Query<Subscription> q = find.where().eq("user.userId",u.getUuidAsString())
                .eq("spaceType", ResourceSpaceTypes.CONTRIBUTION).query();
        List<Subscription> membs = q.findList();
        return membs;
    }

    public static List<Subscription> findByUserIdAndSpaceId(User u, String rsUUID) {
        return findSubscriptionByUserIdAndSpaceId(u.getUuidAsString(),rsUUID);
    }

    public static List<Subscription> findByUserIdAndSpaceIdAndType(User u, String rsUUID, String type) {
        return findSubscriptionByUserIdAndSpaceIdAndType(u.getUuidAsString(), rsUUID, type);
    }


    public static Boolean existByUserIdAndSpaceId(User u, ResourceSpace resourceSpace) {
        List<Subscription> membs = findSubscriptionByUserIdAndSpaceId(u.getUuidAsString(),resourceSpace.getUuidAsString());
        return !membs.isEmpty();
    }

    public static List<Subscription> findSubscriptionByUserIdAndSpaceId (String userUUID, String resourceSpaceUUID) {
        return find.where()
                .eq("userId",userUUID)
                .eq("spaceId",resourceSpaceUUID)
                .query()
                .findList();
    }


    public static List<Subscription> findSubscriptionByUserIdAndSpaceIdAndType (String userUUID, String resourceSpaceUUID, String type) {
        return find.where()
                .eq("userId",userUUID)
                .eq("spaceId",resourceSpaceUUID)
                .eq("subscriptionType",type)
                .query()
                .findList();
    }

    public static List<Subscription> findSubscriptionBySpaceId (String resourceSpaceUUID) {
        return find.where()
                .eq("spaceId",resourceSpaceUUID)
                .query()
                .findList();
    }

    public static Subscription findSubscriptionBySpaceIdAndId (String resourceSpaceUUID, Long id) {
        return find.where()
                .eq("spaceId",resourceSpaceUUID)
                .eq("id",id)
                .query()
                .findUnique();
    }

    public static Subscription findSubscriptionBySpaceIdAndIdentifier (String resourceSpaceUUID, String defaultIdentity) {
        return find.where()
                .eq("spaceId",resourceSpaceUUID)
                .eq("defaultIdentity",defaultIdentity)
                .query()
                .findUnique();
    }

    public static List<Subscription> findBySignal(NotificationSignalTransfer signal) {
        /*
            * subscription.spaceType === signal.spaceType
    * subscription.spaceId === signal.spaceId
    * subscription.subscriptionType === signal.signalType
    * subscription.ignoredEventsList[signal.eventName] === null OR false
         */
        com.avaje.ebean.Query<Subscription> q = find.where()
                .eq("spaceType",signal.getSpaceType())
                .eq("spaceId", signal.getSpaceId())
                .eq("subscriptionType",signal.getSignalType())
                .query();
        List<Subscription> membs = q.findList();
        return membs;
    }

    public static List<Subscription> findBySubscriptionAndSpaceType(SubscriptionTypes type, ResourceSpaceTypes space1,
                                                                    ResourceSpaceTypes space2) {
        com.avaje.ebean.Query<Subscription> q = find.where()
                        .eq("subscriptionType", type)
                        .or(
                            com.avaje.ebean.Expr.eq("spaceType", space1),
                            com.avaje.ebean.Expr.eq("spaceType", space2)
                        )
                        .query();
        return q.findList();
    }

    public static Subscription update(Subscription newSubscription, Subscription old) {
        old.setDefaultService(newSubscription.getDefaultService());
        old.setDefaultIdentity(newSubscription.getDefaultIdentity());
        old.setDisabledServices((HashMap<String, Boolean>) newSubscription.getDisabledServices());
        old.setIgnoredEvents((HashMap<String, Boolean>) newSubscription.getIgnoredEvents());
        old.setNewsletterFrecuency(newSubscription.getNewsletterFrecuency());
        old.setSubscriptionType(newSubscription.getSubscriptionType());
        old.update();
        old.refresh();
        return old;
    }
}
