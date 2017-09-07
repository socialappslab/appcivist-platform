package models;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

/**
 * Created by ggaona on 02/9/17.
 */

@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotificationEventSignalUser extends AppCivistBaseModel {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne()
    @Column(name = "user_id")
    private User user;

    private Boolean read = false;

    @ManyToOne()
    @Column(name = "notification_event_signal_id")
    private NotificationEventSignal signal;

    public NotificationEventSignalUser(User user, NotificationEventSignal notificationEvent) {
        this.user = user;
        this.signal = notificationEvent;
        this.read = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificationEventSignal getSignal() {
        return signal;
    }

    public void setSignal(NotificationEventSignal signal) {
        this.signal = signal;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

}