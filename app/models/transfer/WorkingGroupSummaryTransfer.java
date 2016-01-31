package models.transfer;

import java.util.List;
import java.util.UUID;

public class WorkingGroupSummaryTransfer {
	private Long groupId;
	private UUID uuid;
    private String name;
    private String text;
    private Boolean listed = true;
    private String majorityThreshold;
    private Boolean blockMajority;
	private WorkingGroupProfileTransfer profile;
	private List<ThemeTransfer> existingThemes;
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Boolean getListed() {
		return listed;
	}
	public void setListed(Boolean listed) {
		this.listed = listed;
	}
	public String getMajorityThreshold() {
		return majorityThreshold;
	}
	public void setMajorityThreshold(String majorityThreshold) {
		this.majorityThreshold = majorityThreshold;
	}
	public Boolean getBlockMajority() {
		return blockMajority;
	}
	public void setBlockMajority(Boolean blockMajority) {
		this.blockMajority = blockMajority;
	}
	public WorkingGroupProfileTransfer getProfile() {
		return profile;
	}
	public void setProfile(WorkingGroupProfileTransfer profile) {
		this.profile = profile;
	}
	public List<ThemeTransfer> getExistingThemes() {
		return existingThemes;
	}
	public void setExistingThemes(List<ThemeTransfer> existingThemes) {
		this.existingThemes = existingThemes;
	}
	
}
