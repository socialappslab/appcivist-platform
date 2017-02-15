package models.misc;

import com.fasterxml.jackson.annotation.JsonView;


public class ContributionHistoryExternalChange {

    public enum ExternalChangeType {

        ADDED("Added"), DELETED("Deleted"), MODIFIED("Modified");
        private String type;

        ExternalChangeType(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }

    @JsonView(Views.Public.class)
    private String externalRef;
    @JsonView(Views.Public.class)
    private Long externalRefId;
    @JsonView(Views.Public.class)
    private ExternalChangeType changeType;

    public String getExternalRef() {
        return externalRef;
    }

    public void setExternalRef(String externalRef) {
        this.externalRef = externalRef;
    }

    public Long getExternalRefId() {
        return externalRefId;
    }

    public void setExternalRefId(Long externalRefId) {
        this.externalRefId = externalRefId;
    }

    public ExternalChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ExternalChangeType changeType) {
        this.changeType = changeType;
    }
}
