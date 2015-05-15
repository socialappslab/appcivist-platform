package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.MeetingStatus;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Meeting extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long meetingId;
    private Date date;
    private String topic;
    private String place;
    private MeetingStatus status;
    private List<String> attendees =  new ArrayList<String>();
    private URL doodle;
    private URL hangout;

    public Meeting(User creator, Date creation, Date removal, String lang, Long meetingId, Date date, String topic, String place, MeetingStatus status, List<String> attendees, URL doodle, URL hangout) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.meetingId = meetingId;
        this.date = date;
        this.topic = topic;
        this.place = place;
        this.status = status;
        this.attendees = attendees;
        this.doodle = doodle;
        this.hangout = hangout;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public Date getRemoval() {
        return removal;
    }

    public void setRemoval(Date removal) {
        this.removal = removal;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Long getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public MeetingStatus getStatus() {
        return status;
    }

    public void setStatus(MeetingStatus status) {
        this.status = status;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public URL getDoodle() {
        return doodle;
    }

    public void setDoodle(URL doodle) {
        this.doodle = doodle;
    }

    public URL getHangout() {
        return hangout;
    }

    public void setHangout(URL hangout) {
        this.hangout = hangout;
    }

}
