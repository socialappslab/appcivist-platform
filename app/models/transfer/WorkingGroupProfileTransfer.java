package models.transfer;

import enums.ManagementTypes;
import enums.SupportedMembershipRegistration;

public class WorkingGroupProfileTransfer {
	private Long workingGroupProfileId;
	private SupportedMembershipRegistration supportedMembership;
	private ManagementTypes managementType;
	private String icon;
	private String cover;
	public Long getWorkingGroupProfileId() {
		return workingGroupProfileId;
	}
	public void setWorkingGroupProfileId(Long workingGroupProfileId) {
		this.workingGroupProfileId = workingGroupProfileId;
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
	
}
