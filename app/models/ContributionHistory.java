package models;

import com.avaje.ebean.annotation.Index;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import enums.ContributionTypes;
import models.misc.ContributionHistoryAssociationChange;
import models.misc.ContributionHistoryExternalChange;
import models.misc.ContributionHistoryItem;
import play.Logger;
import play.data.validation.Constraints;
import io.swagger.annotations.ApiModel;

import javax.persistence.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@ApiModel(value="ContributionHistory", description="History of changes and events related to a contribution")
public class ContributionHistory extends AppCivistBaseModel {

    public enum Types {

        CREATION("CREATION"),
        INTERNAL_CHANGE("INTERNAL_CHANGE"),
        EXTERNAL_CHANGE("EXTERNAL_CHANGE"),
        ASSOCIATION("ASSOCIATION");

        private String type;

        Types(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    @Id
    @GeneratedValue
    private Long contributionHistoryId;
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "contributionHistories", cascade = CascadeType.PERSIST)
    private List<ResourceSpace> relatedResourceSpaces;
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
    @Column(name = "moderation_comment", columnDefinition = "text")
    private String moderationComment;
    private String budget;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm a z")
    private Date actionDueDate;
    private Boolean actionDone = false;
    private String action;
    private String assessmentSummary;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "contribution_history_appcivist_user")
    @Where(clause = "${ta}.active=true")
    @JsonIgnoreProperties({"providers", "roles", "permissions", "sessionKey", "identifier"})
    private List<User> authors = new ArrayList<User>();

    @Transient
    ContributionHistoryItem changes;

    public static Finder<Long, ContributionHistory> find = new Finder<>(
            ContributionHistory.class);

    public Long getContributionHistoryId() {
        return contributionHistoryId;
    }

    public void setContributionHistoryId(Long contributionHistoryId) {
        this.contributionHistoryId = contributionHistoryId;
    }

    public String getModerationComment() {
        return moderationComment;
    }

    public void setModerationComment(String moderationComment) {
        this.moderationComment = moderationComment;
    }

    public ContributionHistoryItem getChanges() {
        return changes;
    }

    public void setChanges(ContributionHistoryItem changes) {
        this.changes = changes;
    }

    public List<ResourceSpace> getRelatedResourceSpaces() {
        if (this.relatedResourceSpaces == null) {
            this.relatedResourceSpaces = new ArrayList<>();
        }
        return relatedResourceSpaces;
    }

    public void setRelatedResourceSpaces(List<ResourceSpace> relatedResourceSpaces) {
        this.relatedResourceSpaces = relatedResourceSpaces;
    }

    public List<User> getAuthors() {
        if (this.authors == null) {
            this.authors = new ArrayList<>();
        }
        return authors;
    }

    public void setAuthors(List<User> authors) {
        this.authors = authors;
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

        boolean newRecord = contribution.getContributionId() == null;
        if (!newRecord) {
            contribution = Contribution.read(contribution.getContributionId());
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Logger.info("To be converted to history: " + objectMapper.writeValueAsString(contribution));
        } catch (Exception e) {
            Logger.warn("Error showing historic input");
        }

        ContributionHistory contributionHistory = new ContributionHistory();
        for (Field field : contribution.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Field fieldInHistoric = ContributionHistory.class.getDeclaredField(field.getName());
                fieldInHistoric.setAccessible(true);
                if (field.getName().toLowerCase().contains("ebean") || field.isAnnotationPresent(ManyToMany.class)
                        || field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToMany.class)
                        || field.isAnnotationPresent(OneToOne.class)) {
                    continue;
                }
                fieldInHistoric.set(contributionHistory, field.get(contribution));
            } catch (Exception e) {
            }
        }

        if (contribution.getContainingSpaces() != null) {
            contributionHistory.getRelatedResourceSpaces().addAll(contribution.getContainingSpaces());
        }
        if (contribution.getAuthors() != null) {
            contributionHistory.getAuthors().addAll(contribution.getAuthors());
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Logger.info("Historic obtained: " + objectMapper.writeValueAsString(contributionHistory));
        } catch (Exception e) {
            System.out.println("Error showing historic output");
        }

        contributionHistory.save();
    }

    public static List<ContributionHistory> getContributionsHistory(Long contributionId) throws Exception {
        List<ContributionHistory> histories = find.where().eq("contributionId", contributionId).eq("removed", false).orderBy("contributionHistoryId").findList();
        ContributionHistory previousHistory = null;
        for (ContributionHistory history : histories) {
            history.initChangeset(previousHistory);
            previousHistory = history;
        }
        return histories;
    }

    private void initChangeset(ContributionHistory previous) throws Exception {
        this.changes = new ContributionHistoryItem();
        if (previous == null) {
            return;
        }

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            //If they are both null, skip the processing
            if (field.get(this) == null && field.get(previous) == null) {
                continue;
            }
            //First, we initialize the internal changes by comparing against the previous history
            if (!(field.getName().toLowerCase().contains("ebean") || field.isAnnotationPresent(ManyToMany.class) ||
                    field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(Transient.class) ||
                    field.isAnnotationPresent(Id.class))) {
                if (field.get(this) == null) {
                    changes.getInternalChanges().add(field.getName());
                    continue;
                }
                if (!field.get(this).equals(field.get(previous))) {
                    changes.getInternalChanges().add(field.getName());
                }
            }

            //Processing external changes
            if ((field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(ManyToOne.class) ||
                    field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(OneToOne.class)) &&
                    !field.isAnnotationPresent(Transient.class) && !field.getName().equals("relatedResourceSpaces")) {

                Class relationClass = null;
                boolean isList;
                try {
                    relationClass = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    isList = true;
                } catch (Exception e) {
                    relationClass = field.getType();
                    isList = false;
                }

                //If the current external is null, then it was deleted
                if (field.get(this) == null) {
                    ContributionHistoryExternalChange externalChange = new ContributionHistoryExternalChange();
                    externalChange.setExternalRef(field.getName());
                    externalChange.setChangeType(ContributionHistoryExternalChange.ExternalChangeType.DELETED);
                    changes.getExternalChanges().add(externalChange);
                    continue;
                }
                //If the previous external is null, then it was added
                else if (field.get(previous) == null) {
                    ContributionHistoryExternalChange externalChange = new ContributionHistoryExternalChange();
                    externalChange.setExternalRef(field.getName());
                    externalChange.setChangeType(ContributionHistoryExternalChange.ExternalChangeType.ADDED);
                    changes.getExternalChanges().add(externalChange);
                    continue;
                }

                //If neither of them is null, we'll have to do further verification
                if (isList) {
                    List currentList = (List) field.get(this);
                    List previousList = (List) field.get(previous);
                    for(Object element : currentList){
                        if(!previousList.stream().filter(o -> getEntityId(o).equals(getEntityId(element))).
                                findFirst().isPresent()){
                            ContributionHistoryExternalChange externalChange = new ContributionHistoryExternalChange();
                            externalChange.setExternalRef(field.getName());
                            externalChange.setExternalRefId(getEntityId(element));
                            externalChange.setChangeType(ContributionHistoryExternalChange.ExternalChangeType.ADDED);
                            changes.getExternalChanges().add(externalChange);
                        }
                    }
                    for(Object element : previousList){
                        if(!currentList.stream().filter(o -> getEntityId(o).equals(getEntityId(element))).
                                findFirst().isPresent()){
                            ContributionHistoryExternalChange externalChange = new ContributionHistoryExternalChange();
                            externalChange.setExternalRef(field.getName());
                            externalChange.setExternalRefId(getEntityId(element));
                            externalChange.setChangeType(ContributionHistoryExternalChange.ExternalChangeType.DELETED);
                            changes.getExternalChanges().add(externalChange);
                        }
                    }

                } else {

                    ContributionHistoryExternalChange externalChange = new ContributionHistoryExternalChange();
                    externalChange.setExternalRef(field.getName());
                    externalChange.setChangeType(ContributionHistoryExternalChange.ExternalChangeType.MODIFIED);
                    changes.getExternalChanges().add(externalChange);
                }
            }

            if(field.getName().equals("relatedResourceSpaces")){
                List<ResourceSpace> currentList = this.getRelatedResourceSpaces();
                List<ResourceSpace> previousList = previous.getRelatedResourceSpaces();
                for(ResourceSpace element : currentList){
                    if(!previousList.stream().filter(o -> o.getResourceSpaceId().equals(element.getResourceSpaceId())).
                            findFirst().isPresent()){
                        ContributionHistoryAssociationChange assoChange = new ContributionHistoryAssociationChange();
                        assoChange.setResourceSpaceId(element.getResourceSpaceId());
                        assoChange.setType(element.getType());
                        assoChange.setChangeType(ContributionHistoryAssociationChange.AssociationChangeType.ADDED);
                        changes.getAssociationChanges().add(assoChange);
                    }
                }
                for(ResourceSpace element : previousList){
                    if(!currentList.stream().filter(o -> o.getResourceSpaceId().equals(element.getResourceSpaceId())).
                            findFirst().isPresent()){
                        ContributionHistoryAssociationChange assoChange = new ContributionHistoryAssociationChange();
                        assoChange.setResourceSpaceId(element.getResourceSpaceId());
                        assoChange.setType(element.getType());
                        assoChange.setChangeType(ContributionHistoryAssociationChange.AssociationChangeType.DELETED);
                        changes.getAssociationChanges().add(assoChange);
                    }
                }
            }

        }

        //Now, lets process the resourcespace
    }

    private Long getEntityId(Object entity){
        Long id = null;
        try {
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Id.class)) {
                    id = (Long) field.get(entity);
                    break;
                }
            }
        }catch (Exception e){
            id = null;
        }
        return id;
    }
}
