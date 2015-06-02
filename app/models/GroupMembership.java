package models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("GROUP")
public class GroupMembership extends Membership{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3737906484702711675L;
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
