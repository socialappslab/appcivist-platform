package models.misc;

import com.fasterxml.jackson.annotation.JsonView;

import enums.ResourceSpaceTypes;

public class ContributionHistoryAssociationChange {

    public enum AssociationChangeType {

        ADDED("Added"), DELETED("Deleted");
        private String type;

        AssociationChangeType(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }

    @JsonView(Views.Public.class)
    private Long resourceSpaceId;
    @JsonView(Views.Public.class)
    private ResourceSpaceTypes type;
    @JsonView(Views.Public.class)
    private AssociationChangeType changeType;

    public Long getResourceSpaceId() {
        return resourceSpaceId;
    }

    public void setResourceSpaceId(Long resourceSpaceId) {
        this.resourceSpaceId = resourceSpaceId;
    }

    public ResourceSpaceTypes getType() {
        return type;
    }

    public void setType(ResourceSpaceTypes type) {
        this.type = type;
    }

    public AssociationChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(AssociationChangeType changeType) {
        this.changeType = changeType;
    }
}
