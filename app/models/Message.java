package models;

import enums.MessageType;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class Message extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long messageId;
    private String title;
    private String text;
    private MessageType type;

    @ManyToOne //Un usuario o una lista de usuarios?
    private User targetUser;

    @ManyToOne //Un workingGroup o una lista de workingGroups?
    private WorkingGroup targeWorkingGroup;

    @ManyToOne //Una assembly o una lista de assemblies?
    private Assembly assembly;

    @ManyToOne //Una organization o una lista de organizations?
    private Organization organization;

    public Message(User creator, Date creation, Date removal, String lang, Long messageId, String title, String text, MessageType type, User targetUser, WorkingGroup targeWorkingGroup, Assembly assembly, Organization organization) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.messageId = messageId;
        this.title = title;
        this.text = text;
        this.type = type;
        this.targetUser = targetUser;
        this.targeWorkingGroup = targeWorkingGroup;
        this.assembly = assembly;
        this.organization = organization;
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

    public WorkingGroup getTargeWorkingGroup() {
        return targeWorkingGroup;
    }

    public void setTargeWorkingGroup(WorkingGroup targeWorkingGroup) {
        this.targeWorkingGroup = targeWorkingGroup;
    }

    public Assembly getAssembly() {
        return assembly;
    }

    public void setAssembly(Assembly assembly) {
        this.assembly = assembly;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
