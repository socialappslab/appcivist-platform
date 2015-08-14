package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import enums.MessageType;

@Entity
public class Message extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long messageId;
	private String title;
	private String text;
	private MessageType type;
	private User creator;
	
	@ManyToOne
	private User targetUser;

	@ManyToOne
	private WorkingGroup targetWorkingGroup;

	@ManyToOne
	private Assembly targetAssembly;


	public Message(User creator, String title, String text, MessageType type,
			User targetUser, WorkingGroup targeWorkingGroup, Assembly assembly) {
		this.creator = creator;
		this.title = title;
		this.text = text;
		this.type = type;
		this.targetUser = targetUser;
		this.targetWorkingGroup = targeWorkingGroup;
		this.targetAssembly = assembly;
	}

	public Message() {
		super();
	}

	public static Finder<Long, Message> find = new Finder<>(Message.class);

	public static Message read(Long messageId) {
		return find.ref(messageId);
	}

	public static List<Message> findAll() {
		return find.all();
	}

	public static Message create(Message message) {
		message.save();
		message.refresh();
		return message;
	}

	public static Message createObject(Message message) {
		message.save();
		return message;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
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

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public User getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(User targetUser) {
		this.targetUser = targetUser;
	}

	public WorkingGroup getTargetWorkingGroup() {
		return targetWorkingGroup;
	}

	public void setTargetWorkingGroup(WorkingGroup targeWorkingGroup) {
		this.targetWorkingGroup = targeWorkingGroup;
	}

	public Assembly getTargetAssembly() {
		return targetAssembly;
	}

	public void setTargetAssembly(Assembly assembly) {
		this.targetAssembly = assembly;
	}
}
