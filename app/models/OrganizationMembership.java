package models;

import enums.MembershipStatus;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class OrganizationMembership extends Membership{

    private Boolean follower;
    private Boolean invitation;

    @ManyToMany(mappedBy = "organizationMemberships")
    private List<OrganizationMembership> organizationMemberships = new ArrayList<OrganizationMembership>();

    public OrganizationMembership(User creator, Date creation, Date removal, String lang, Long membershipId, Date expiration, MembershipStatus status, User target, Role role, Boolean follower, Boolean invitation, List<OrganizationMembership> organizationMemberships) {
        super(creator, creation, removal, lang, membershipId, expiration, status, target, role);
        this.follower = follower;
        this.invitation = invitation;
        this.organizationMemberships = organizationMemberships;
    }

    public List<OrganizationMembership> getOrganizationMemberships() {
        return organizationMemberships;
    }

    public void setOrganizationMemberships(List<OrganizationMembership> organizationMemberships) {
        this.organizationMemberships = organizationMemberships;
    }

    public Boolean getFollower() {
        return follower;
    }

    public void setFollower(Boolean follower) {
        this.follower = follower;
    }

    public Boolean getInvitation() {
        return invitation;
    }

    public void setInvitation(Boolean invitation) {
        this.invitation = invitation;
    }
}
