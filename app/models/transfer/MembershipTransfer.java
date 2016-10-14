package models.transfer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * Transfer model to receive membership creation objects
 * 
 * @author cdparra
 *
 */
@ApiModel(
		value = "Membership", 
		description = "A Membership connects a user to an Assembly or a Working Group, assigning a role that is used for authorization purposes. Only Coordinators have the power to create and update memberships. Members can delete their own memberships."
)
public class MembershipTransfer {
    private Long userId;
    private Long groupId;
    private Long assemblyId;
    @ApiModelProperty(value="Email of the person for whom a membership will be created")
    private String email;
	@ApiModelProperty(value="Type identifies what type of membership depending on the target", allowableValues="ASSEMBLY, GROUP")
    private String type;

	@ApiModelProperty(value="targetCollection is the the ID of the Assembly or Working Group to which we are associating the user")
    private String targetCollection;
    private Long defaultRoleId; 
    private String defaultRoleName;
    
    @ApiModelProperty(value="status", allowableValues="REQUESTED, INVITED, ACCEPTED or REJECTED")
	private String status;

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
