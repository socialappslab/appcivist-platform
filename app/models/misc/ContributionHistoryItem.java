package models.misc;

import java.util.ArrayList;
import java.util.List;

public class ContributionHistoryItem {

    private List<String> internalChanges = new ArrayList<>();
    private List<ContributionHistoryExternalChange> externalChanges = new ArrayList<>();
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
