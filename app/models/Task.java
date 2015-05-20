package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.TaskStatus;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Task extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long taskId;
    private String title;
    private String description;
    private Date dueDate;
    private User responsible;
    private TaskStatus status;
    private String places;

    public Task(User creator, Date creation, Date removal, String lang, Long taskId, String title, String description, Date dueDate, User responsible, TaskStatus status, String places) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.responsible = responsible;
        this.status = status;
        this.places = places;
    }

    public static Model.Finder<Long, Task> find = new Model.Finder<Long, Task>(
            Long.class, Task.class);

    public static Task read(Long taskId) {
        return find.ref(taskId);
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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public User getResponsible() {
        return responsible;
    }

    public void setResponsible(User responsible) {
        this.responsible = responsible;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getPlaces() {
        return places;
    }

    public void setPlaces(String places) {
        this.places = places;
    }


}
