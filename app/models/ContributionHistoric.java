package models;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import enums.ContributionTypes;
import models.location.Location;
import play.data.validation.Constraints;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import play.Logger;

@Entity
public class ContributionHistoric extends AppCivistBaseModel {

    @Id
    @GeneratedValue
    private Long rev;

    private Long contributionId;
    @Index
    private UUID uuid = UUID.randomUUID();
    @Constraints.Required
    private String title;
    @Constraints.Required
    @Column(name = "text", columnDefinition = "text")
    private String text;
    @Enumerated(EnumType.STRING)
    @Constraints.Required
    private ContributionTypes type;
    @JsonIgnore
    @Index
    @Column(name = "text_index", columnDefinition = "text")
    private String textIndex;
    private String budget;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    private Date actionDueDate;
    private Boolean actionDone = false;
    private String action;
    private String assessmentSummary;

    public static Finder<Long, ContributionHistoric> find = new Finder<>(
            ContributionHistoric.class);

    public Long getRev() {
        return rev;
    }

    public void setRev(Long rev) {
        this.rev = rev;
    }

    public Long getContributionId() {
        return contributionId;
    }

    public void setContributionId(Long contributionId) {
        this.contributionId = contributionId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ContributionTypes getType() {
        return type;
    }

    public void setType(ContributionTypes type) {
        this.type = type;
    }

    public String getTextIndex() {
        return textIndex;
    }

    public void setTextIndex(String textIndex) {
        this.textIndex = textIndex;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public Date getActionDueDate() {
        return actionDueDate;
    }

    public void setActionDueDate(Date actionDueDate) {
        this.actionDueDate = actionDueDate;
    }

    public Boolean getActionDone() {
        return actionDone;
    }

    public void setActionDone(Boolean actionDone) {
        this.actionDone = actionDone;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAssessmentSummary() {
        return assessmentSummary;
    }

    public void setAssessmentSummary(String assessmentSummary) {
        this.assessmentSummary = assessmentSummary;
    }

    public static void createHistoricFromContribution(Contribution contribution) {
        contribution = Contribution.read(contribution.getContributionId());
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Logger.info("To be converted to historic: " + objectMapper.writeValueAsString(contribution));
        }catch (Exception e){
            Logger.warn("Error showing historic input");
        }
        ContributionHistoric contributionHistoric = new ContributionHistoric();
        for (Field field : contribution.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Field fieldInHistoric = ContributionHistoric.class.getDeclaredField(field.getName());
                fieldInHistoric.setAccessible(true);
                if(field.getName().toLowerCase().contains("ebean")){
                    continue;
                }
                fieldInHistoric.set(contributionHistoric, field.get(contribution));
            } catch (Exception e) {
            }
        }
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Logger.info("Historic obtained: " + objectMapper.writeValueAsString(contributionHistoric));
        }catch (Exception e){
            System.out.println("Error showing historic output");
        }

        contributionHistoric.save();
    }

    public static List<ContributionHistoric> getContributionsHistory(Long contributionId){
        return find.where().eq("contributionId", contributionId).eq("removed",false).orderBy("rev").findList();
    }
}
