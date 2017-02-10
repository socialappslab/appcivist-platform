package models.misc;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

public class ContributionHistoryItem {

	@JsonView(Views.Public.class)
    private List<String> internalChanges = new ArrayList<>();
    @JsonView(Views.Public.class)
    private List<ContributionHistoryExternalChange> externalChanges = new ArrayList<>();
    @JsonView(Views.Public.class)
    private List<ContributionHistoryAssociationChange> associationChanges = new ArrayList<>();

    public List<String> getInternalChanges() {
        return internalChanges;
    }

    public void setInternalChanges(List<String> internalChanges) {
        this.internalChanges = internalChanges;
    }

    public List<ContributionHistoryExternalChange> getExternalChanges() {
        return externalChanges;
    }

    public void setExternalChanges(List<ContributionHistoryExternalChange> externalChanges) {
        this.externalChanges = externalChanges;
    }

    public List<ContributionHistoryAssociationChange> getAssociationChanges() {
        return associationChanges;
    }

    public void setAssociationChanges(List<ContributionHistoryAssociationChange> associationChanges) {
        this.associationChanges = associationChanges;
    }
}
