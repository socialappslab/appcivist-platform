package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.MembershipStatus;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="MEMBERSHIP_TYPE")
public abstract class Membership extends Model{

    //Commons
    private User creator;
    private Date creation;
    private Date removal;
    private String lang;

    @Id
    private Long membershipId;
    private Date expiration;
    private MembershipStatus status;


    public User getUser() {
        return user;
    }

    public void setUser(User User) {
        this.user = User;
    }

    @JsonIgnore
    @ManyToOne
    private User user;


    public Role getRole() {
        return role;
    }

    public void setRole(Role Role) {
        this.role = Role;
    }

    @JsonIgnore
    @ManyToOne
    private Role role;

    public static Model.Finder<Long, Membership> find = new Model.Finder<Long, Membership>(
            Long.class, Membership.class);

    public static Membership read(Long membershipId) {
        return find.ref(membershipId);
    }

    public static List<Membership> findAll() {
        return find.all();
    }

    public static Membership create(Membership membership) {
        membership.save();
        membership.refresh();
        return membership;
    }

    public static Membership createObject(Membership membership) {
        membership.save();
        return membership;
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

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = status;
    }
}
