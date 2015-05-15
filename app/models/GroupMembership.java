package models;

import enums.MembershipStatus;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;

@Entity
public class GroupMembership extends Membership{

    private Boolean groupFollower;
    private Boolean groupInvitation;

    @OneToMany
    private WorkingGroup workingGroup;

    public GroupMembership(User creator, Date creation, Date removal, String lang, Long membershipId, Date expiration, MembershipStatus status, User target, Role role, Boolean groupFollower, Boolean groupInvitation, WorkingGroup workingGroup) {
        super(creator, creation, removal, lang, membershipId, expiration, status, target, role);
        this.groupFollower = groupFollower;
        this.groupInvitation = groupInvitation;
        this.workingGroup = workingGroup;
    }

    public WorkingGroup getWorkingGroup() {
        return workingGroup;
    }

    public void setWorkingGroup(WorkingGroup workingGroup) {
        this.workingGroup = workingGroup;
    }

    public Boolean getGroupFollower() {
        return groupFollower;
    }

    public void setGroupFollower(Boolean groupFollower) {
        this.groupFollower = groupFollower;
    }

    public Boolean getGroupInvitation() {
        return groupInvitation;
    }

    public void setGroupInvitation(Boolean groupInvitation) {
        this.groupInvitation = groupInvitation;
    }
}
