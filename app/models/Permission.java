package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.PermissionTypes;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Permission extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long permitId;
    private PermissionTypes permit;

    @JsonIgnore
    @ManyToOne
    private List<Role> roles = new ArrayList<Role>();

    public Permission(User creator, Date creation, Date removal, String lang, Long permitId, PermissionTypes permit) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.permitId = permitId;
        this.permit = permit;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
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

    public PermissionTypes getPermit() {
        return permit;
    }

    public void setPermit(PermissionTypes permit) {
        this.permit = permit;
    }
}
