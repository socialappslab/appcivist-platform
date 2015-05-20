package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.MembershipStatus;
import play.db.ebean.Model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@DiscriminatorValue("GROUP")
public class GroupMembership extends Membership{


    public WorkingGroup getWorkingGroup() {
        return workingGroup;
    }

    public void setWorkingGroup(WorkingGroup WorkingGroup) {
        this.workingGroup = WorkingGroup;
    }

    @JsonIgnore
    @ManyToOne
    private WorkingGroup workingGroup;

    public static Model.Finder<Long, GroupMembership> find = new Model.Finder<Long, GroupMembership>(
            Long.class, GroupMembership.class);

    public static GroupMembership read(Long groupMembershipId) {
        return find.ref(groupMembershipId);
    }
}
