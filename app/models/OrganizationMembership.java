package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.MembershipStatus;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@DiscriminatorValue("ORG")
public class OrganizationMembership extends Membership{

    @JsonIgnore
    @ManyToOne
    private Organization organization;

    public static Model.Finder<Long, OrganizationMembership> find = new Model.Finder<Long, OrganizationMembership>(
            Long.class, OrganizationMembership.class);

    public static OrganizationMembership read(Long organizationMembershipId) {
        return find.ref(organizationMembershipId);
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
