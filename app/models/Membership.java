package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.MembershipStatus;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="MEMBERSHIP_TYPE")
public abstract class Membership extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4939869903730586228L;
	
    @Id
    @GeneratedValue
    private Long membershipId;
    private Date expiration;
    private MembershipStatus status;
    private User creator;

    @JsonIgnore
    @ManyToOne
    private User user;

    @JsonIgnore
    @ManyToMany(cascade = CascadeType.ALL)
    private List<Role> roles = new ArrayList<Role>();

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

    public User getUser() {
        return user;
    }

    public void setUser(User User) {
        this.user = User;
    }
    
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
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

    public List<Role> getRole() {
        return roles;
    }

    public void setRole(List<Role> roles) {
        this.roles = roles;
    }
}
