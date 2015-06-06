package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import play.db.ebean.Model;
import enums.TaskStatus;

@Entity
public class Task extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6574200018754738463L;
	// Commons

	@Id
	@GeneratedValue
	private Long taskId;
	private String title;
	private String description;
	private Date dueDate;
	private User responsible;
	private TaskStatus status;
	private String places;
	private User creator;

	public Task(User creator, String title, String description, Date dueDate,
			User responsible, TaskStatus status, String places) {
		this.creator = creator;
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
