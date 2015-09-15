package models.transfer;

/**
 * Transfer model to receive membership creation objects
 * 
 * @author cdparra
 *
 */
public class MembershipTransfer {

    /**
     * The id of the user to be added to the group or assembly
     */
    private Long userId;
    private Long groupId;
    private Long assemblyId;
    private String email;
    private String type;
    private String targetCollection;
    private Long defaultRoleId; 
    private String defaultRoleName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTargetCollection() {
		return targetCollection;
	}

	public void setTargetCollection(String targetCollection) {
		this.targetCollection = targetCollection;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Long getAssemblyId() {
		return assemblyId;
	}

	public void setAssemblyId(Long assemblyId) {
		this.assemblyId = assemblyId;
	}

	public Long getDefaultRoleId() {
		return defaultRoleId;
	}

	public void setDefaultRoleId(Long defaultRoleId) {
		this.defaultRoleId = defaultRoleId;
	}

	public String getDefaultRoleName() {
		return defaultRoleName;
	}

	public void setDefaultRoleName(String defaultRoleName) {
		this.defaultRoleName = defaultRoleName;
	}
}
