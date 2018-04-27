package models;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.misc.Views;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "campaign_participation")
@ApiModel(value="Campaign Participation", description="Relation between an user and a campaign")
public class CampaignParticipation extends Model {

    @Id
    @GeneratedValue
    private Long campaignParticipationId;


    @ManyToOne(fetch= FetchType.EAGER, cascade= CascadeType.ALL)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private User user;

    @ManyToOne(fetch= FetchType.EAGER, cascade= CascadeType.ALL)
    @JsonIgnore
    private Campaign campaign;


    @JsonView(Views.Public.class)
    @Column(name = "user_consent")
    private Boolean userConsent;

    @JsonView(Views.Public.class)
    @Column(name = "user_provided_consent")
    private Boolean userProvidedConsent;

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
    @ApiModelProperty(name="creation_date", value="Date in which this resource was created", notes="By default set to NOW")
    @JsonView(Views.Public.class)
    @Column(name = "creation_date")
    private Date creationDate = new Date(); // by Default, the creation is NOW


    public static Model.Finder<Long, CampaignParticipation> find = new Model.Finder<>(CampaignParticipation.class);

    public static List<CampaignParticipation> getByCampaign(Campaign campaign) {
        return find.where().eq("campaign", campaign).findList();
    }

    public static List<CampaignParticipation> getByCampaignAndUser(Campaign campaign, User user) {
        return find.where().eq("campaign", campaign).eq("user", user).findList();
    }


    public static CampaignParticipation getByCampaignAndUserIds(Long cid, Long uid) {
        return find.where().eq("campaign.campaignId", cid).eq("user.userId", uid).findUnique();
    }

    public static CampaignParticipation createIfNotExist(User user, Campaign campaign) {
        if(find.where().eq("user", user).eq("campaign", campaign).findList().isEmpty()) {
            CampaignParticipation campaignParticipation = new CampaignParticipation();
            campaignParticipation.setCampaign(campaign);
            campaignParticipation.setUser(user);
            campaignParticipation.setUserConsent(false);
            campaignParticipation.setUserProvidedConsent(false);
            campaignParticipation.setCreationDate(new Date());
            campaignParticipation.save();
        }

        return getByCampaignAndUser(campaign, user).get(0);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public Boolean getUserConsent() {
        return userConsent;
    }

    public void setUserConsent(Boolean userConsent) {
        this.userConsent = userConsent;
    }

    public Boolean getUserProvidedConsent() {
        return userProvidedConsent;
    }

    public void setUserProvidedConsent(Boolean userProvidedConsent) {
        this.userProvidedConsent = userProvidedConsent;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCampaignParticipationId() {
        return campaignParticipationId;
    }

    public void setCampaignParticipationId(Long campaignParticipationId) {
        this.campaignParticipationId = campaignParticipationId;
    }
}
