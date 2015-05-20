package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import play.db.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Role extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long roleId;
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy="roles")
    private Permission permits;


    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }

    @JsonIgnore
    @OneToMany(mappedBy="role", cascade = CascadeType.ALL)
    private List<Membership> memberships  = new ArrayList<Membership>();;

/*
    @OneToMany(cascade = CascadeType.ALL, mappedBy="role")
    private List<Membership> memberships = new ArrayList<Membership>();*/

    @OneToMany(cascade = CascadeType.ALL, mappedBy="role")
    private List<WorkingGroup> workingGroups = new ArrayList<WorkingGroup>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy="role")
    private List<User> users = new ArrayList<User>();

    public Role(User creator, Date creation, Date removal, String lang, Long roleId, String name, Permission permits, /*List<Membership> memberships,*/ List<WorkingGroup> workingGroups, List<User> users) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.roleId = roleId;
        this.name = name;
        this.permits = permits;
        //this.memberships = memberships;
        this.workingGroups = workingGroups;
        this.users = users;
    }

    public static Model.Finder<Long, Role> find = new Model.Finder<Long, Role>(
            Long.class, Role.class);

    public static Role read(Long roleId) {
        return find.ref(roleId);
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

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Permission getPermits() {
        return permits;
    }

    public void setPermits(Permission permits) {
        this.permits = permits;
    }
/*
    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }
*/
    public List<WorkingGroup> getWorkingGroups() {
        return workingGroups;
    }

    public void setWorkingGroups(List<WorkingGroup> workingGroups) {
        this.workingGroups = workingGroups;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
