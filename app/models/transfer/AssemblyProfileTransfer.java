package models.transfer;

import play.data.validation.Constraints.Required;
import utils.GlobalData;
import enums.ManagementTypes;
import enums.SupportedMembershipRegistration;

public class AssemblyProfileTransfer {
	private String targetAudience;
	@Required private SupportedMembershipRegistration supportedMembership = SupportedMembershipRegistration.INVITATION_AND_REQUEST; //   OPEN, INVITATION, REQUEST, INVITATION_AND_REQUEST
	@Required private ManagementTypes managementType = ManagementTypes.OPEN; // assemblies are OPEN by default
	private String icon = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_ICON; // a small icon to represent the assembly
	private String cover = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_COVER;	// cover picture of the assembly, to appear on the top of its page
	
	private String primaryContactName;
	private String primaryContactPhone;
	private String primaryContactEmail;
	private LocationTransfer location;
	public String getTargetAudience() {
		return targetAudience;
	}
	public void setTargetAudience(String targetAudience) {
		this.targetAudience = targetAudience;
	}
	public SupportedMembershipRegistration getSupportedMembership() {
		return supportedMembership;
	}
	public void setSupportedMembership(
			SupportedMembershipRegistration supportedMembership) {
		this.supportedMembership = supportedMembership;
	}
	public ManagementTypes getManagementType() {
		return managementType;
	}
	public void setManagementType(ManagementTypes managementType) {
		this.managementType = managementType;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public String getPrimaryContactName() {
		return primaryContactName;
	}
	public void setPrimaryContactName(String primaryContactName) {
		this.primaryContactName = primaryContactName;
	}
	public String getPrimaryContactPhone() {
		return primaryContactPhone;
	}
	public void setPrimaryContactPhone(String primaryContactPhone) {
		this.primaryContactPhone = primaryContactPhone;
	}
	public String getPrimaryContactEmail() {
		return primaryContactEmail;
	}
	public void setPrimaryContactEmail(String primaryContactEmail) {
		this.primaryContactEmail = primaryContactEmail;
	}
	public LocationTransfer getLocation() {
		return location;
	}
	public void setLocation(LocationTransfer location) {
		this.location = location;
	}
}
