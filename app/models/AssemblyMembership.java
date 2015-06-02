package models;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@DiscriminatorValue("ASSEMBLY")
public class AssemblyMembership extends Membership{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6654162992798204503L;
	@JsonIgnore
    @ManyToOne
    private Assembly organization;

    public Assembly getOrganization() {
        return organization;
    }

    public void setOrganization(Assembly organization) {
        this.organization = organization;
    }
}
