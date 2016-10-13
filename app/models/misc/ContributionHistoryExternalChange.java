package models.misc;


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

    private String externalRef;
    private Long externalRefId;
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
