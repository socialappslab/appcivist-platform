package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import play.Logger;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ContributionPublishHistory extends AppCivistBaseModel {


    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;
    @JsonIgnore
    private Long contributionId;
    @JsonIgnore
    private Long resourceId;
    private Integer revision;

    public static Finder<Long, ContributionPublishHistory> find = new Finder<>(
            ContributionPublishHistory.class);

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContributionId() {
        return contributionId;
    }

    public void setContributionId(Long contributionId) {
        this.contributionId = contributionId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public static List<ContributionPublishHistory> getContributionsPublishHistory(Contribution c){
        if(c.getExtendedTextPad() == null){
            return null;
        }
        return find.where().eq("contributionId", c.getContributionId())
                .eq("resourceId", c.getExtendedTextPad().getResourceId()).findList();
    }
}
