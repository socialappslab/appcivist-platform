package models;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnore;

import play.db.ebean.Model;

import javax.persistence.*;

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

    @JsonIgnore
    @ManyToMany(mappedBy="roles")
    private List<Membership> memberships  = new ArrayList<Membership>();

    @JsonIgnore
    @ManyToMany(mappedBy="roles")
    private List<User> users = new ArrayList<User>();

    public Role(User creator, Date creation, Date removal, String lang, Long roleId, String name, Permission permits, /*List<Membership> memberships,*/ List<WorkingGroup> workingGroups, List<User> users) {
        this.creator = creator;
        this.creation = creation;
        this.removal = removal;
        this.lang = lang;
        this.roleId = roleId;
        this.name = name;
        this.permits = permits;
        this.users = users;
    }

    public static Model.Finder<Long, Role> find = new Model.Finder<Long, Role>(
            Long.class, Role.class);

    public static Role read(Long roleId) {
        return find.ref(roleId);
    }
    
    public static Integer readByTitle(String name){
    	ExpressionList<Role> roles = find.where().eq("name", name);
    	return roles.findList().size();
    }

    public static List<Role> findAll() {
        return find.all();
    }

    public static Role create(Role role) {
        role.save();
        role.refresh();
        return role;
    }

    public static Role createObject(Role role) {
        role.save();
        return role;
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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Membership> getMemberships() {
        return memberships;
    }

    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }
}
