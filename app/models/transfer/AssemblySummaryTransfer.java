package models.transfer;

import java.util.List;
import java.util.UUID;

public class AssemblySummaryTransfer {
	private Long assemblyId;
	private UUID uuid;
	private String shortname;
	private String name;
	private AssemblyProfileTransfer profile;
	private List<ThemeTransfer> themes;
	
	public Long getAssemblyId() {
		return assemblyId;
	}
	public void setAssemblyId(Long assemblyId) {
		this.assemblyId = assemblyId;
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID id) {
		this.uuid = id;
	}
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public String getName() {
		return name;
	}
	public void setName(String n) {
		this.name = n;
	}
	public AssemblyProfileTransfer getProfile() {
		return profile;
	}
	public void setProfile(AssemblyProfileTransfer profile) {
		this.profile = profile;
	}
	public List<ThemeTransfer> getThemes() {
		return themes;
	}
	public void setThemes(List<ThemeTransfer> themes) {
		this.themes = themes;
	}
}
