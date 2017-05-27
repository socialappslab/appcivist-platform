package models;

import io.swagger.annotations.ApiModel;

import javax.persistence.*; // you can also import one by one
import java.util.*;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
// extending from AppCivistBaseModel adds some additional 
// attributes common to all entities in the system 
@Entity
public class Event extends AppCivistBaseModel {
    
    @Id
    private Long eventId;
    @Column
    private String eventTitle;
    @Column
    private String eventDescription;
    @Column
    private Date eventStart_datetime;
    @Column
    private Date eventEnd_datetime;
    @Column
    private String eventLocation;
    @Column
    private String event_url;
    @Column
    private String eventAssociated_milestone;

    // other attributes
    // we can also use jackson annotations (import 
    // com.fasterxml.jackson.annotation) to manage what will be 
    // automatically rendered in json or not using annotations 
    // like JsonView, JsonIgnore, JsonManagedReference or 
    // JsonBackReference

    // Always add a Finder attribute, to facilitate the 
    // implementation of queries

    //constructor
    public UserProfile(Long eventId, UUID uuid, String eventTitle, String eventDescription, Date eventStart_datetime,
                       Date eventEnd_datetime, String eventLocation, String event_url, String eventAssociated_milestone) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventDescription = eventDescription;
        this.eventStart_datetime = eventStart_datetime;
        this.eventEnd_datetime = eventEnd_datetime;
        this.eventLocation = eventLocation;
        this.event_url = event_url;
        this.eventAssociated_milestone = eventAssociated_milestone;
    }


    public static Finder<Long,Event> find = new Finder<>(Event.class);
    

    // accesor methods 
    //eventId
    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long id) {
        eventId = id;
    }
    //eventTitle
    public String getTitle() {
        return eventTitle;
    }
    public void setTitle(String title) {
        eventTitle = title;
    }
    //eventDescription
    public String getDescription() {
        return eventDescription;
    }
    public void setDescription(String description) {
        eventDescription = description;
    }
    //start_datetime
    public Date getStartDateTime() {
        return eventStart_datetime;
    }
    public void setStartDateTime(Date start_datetime) {
        eventStart_datetime = start_datetime;
    }
    //end_datetime
    public Date getEndDateTime() {
        return eventEnd_datetime;
    }
    public void setEndDateTime(Date end_datetime) {
        eventEnd_datetime = end_datetime;
    }
    //location
    public String getLocation() {
        return eventLocation;
    }
    public void setLocation(String location) {
        eventLocation = location;
    }
    //url
    public String getUrl() {
        return event_url;
    }
    public void setUrl(String url) {
        event_url = url;
    }
    //url
    public String getAssociatedMilestone() {
        return eventAssociated_milestone;
    }
    public void setAssociatedMilestone(String associated_milestone) {
        eventAssociated_milestone = associated_milestone;
    }

    // basic CRUD persistence methods
    public static void create(Event e){
        e.save();
    }
    
    public static void delete(Long id){
        find.ref(id).delete(); // notice use of find attribute
    }
    
    public static Event read(Long id){
        return find.byId(id);
    }
    
    // other methods to implement queries around the entity Event
    // explore the possibilities of the find.where.* API to 
    // implement all sort of basic queries
    public static List<Event> eventBetweenDates(Date date1, Date date2) {
        return find.where().between("eventStart_datemetime", date1, date2).findList();
    } 

   /* public static List<Event> eventTitleEquals(String title) {
        return find.where().eq("eventTitle", title).findList();
    }*/
}    
 
