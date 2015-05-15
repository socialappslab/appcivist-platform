package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.MembershipStatus;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@DiscriminatorValue("ORG")
public class OrganizationMembership extends Membership{

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @JsonIgnore
    @ManyToOne
    private Organization organization;
}
