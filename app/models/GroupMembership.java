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

    @JsonIgnore
    @ManyToOne
    private WorkingGroup workingGroup;

    public WorkingGroup getWorkingGroup() {
        return workingGroup;
    }

    public void setWorkingGroup(WorkingGroup WorkingGroup) {
        this.workingGroup = WorkingGroup;
    }

}