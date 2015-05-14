package models;

import enums.PermissionTypes;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Permission extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long permitId;
    private String resName;
    private PermissionTypes permit;

    public Permission(User creator, Date creation, Date removal, String lang, Long permitId, String resName, PermissionTypes permit) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.permitId = permitId;
        this.resName = resName;
        this.permit = permit;
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

    public Long getPermitId() {
        return permitId;
    }

    public void setPermitId(Long permitId) {
        this.permitId = permitId;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public PermissionTypes getPermit() {
        return permit;
    }

    public void setPermit(PermissionTypes permit) {
        this.permit = permit;
    }
}
