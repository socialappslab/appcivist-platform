package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import play.db.ebean.Model;
import enums.MeetingStatus;

@Entity
public class Meeting extends AppCivistBaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2788111418126466766L;
	
	@Id
	@GeneratedValue
	private Long meetingId;
	private Date date;
	private String topic;
	private String place;
	private MeetingStatus status;
	private List<String> attendees = new ArrayList<String>();
	private URL doodle;
	private URL hangout;
	private User creator;

	public Meeting(User creator, 
			Date date, String topic, String place,
			MeetingStatus status, List<String> attendees, URL doodle,
			URL hangout) {
		this.creator = creator;
		this.date = date;
		this.topic = topic;
		this.place = place;
		this.status = status;
		this.attendees = attendees;
		this.doodle = doodle;
		this.hangout = hangout;
	}

	public static Model.Finder<Long, Meeting> find = new Model.Finder<Long, Meeting>(
			Long.class, Meeting.class);

	public static Meeting read(Long meetingId) {
		return find.ref(meetingId);
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
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
