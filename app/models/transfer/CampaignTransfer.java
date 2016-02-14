package models.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CampaignTransfer {
	private Long campaignId;
	private String title;
	private String shortname;
	private String goal;	
	private String url;
	private UUID uuid;
	private Boolean listed = true;
	private List<ComponentTransfer> components = new ArrayList<>();
	private List<ConfigTransfer> configs = new ArrayList<>();
	private List<ThemeTransfer> themes = new ArrayList<>();
	private List<WorkingGroupTransfer> workingGroups = new ArrayList<>();
	private List<Long> assemblies = new ArrayList<>();
	private List<ComponentTransfer> existingComponents = new ArrayList<>();
	private List<ConfigTransfer> existingConfigs = new ArrayList<>();
	private List<ThemeTransfer> existingThemes = new ArrayList<>();
	private List<WorkingGroupTransfer> existingWorkingGroups = new ArrayList<>();
	private CampaignTemplateTransfer template;
	private CampaignTimelineEdgeTransfer timelineEdges;
	public CampaignTransfer() {
		super();
	}
	public Long getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(Long campaignId) {
		this.campaignId = campaignId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public String getGoal() {
		return goal;
	}
	public void setGoal(String goal) {
		this.goal = goal;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public UUID getUuid() {
		return uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	public Boolean getListed() {
		return listed;
	}
	public void setListed(Boolean listed) {
		this.listed = listed;
	}
	public List<ComponentTransfer> getComponents() {
		return components;
	}
	public void setComponents(List<ComponentTransfer> components) {
		this.components = components;
	}
	public List<ConfigTransfer> getConfigs() {
		return configs;
	}
	public void setConfigs(List<ConfigTransfer> configs) {
		this.configs = configs;
	}
	public List<ThemeTransfer> getThemes() {
		return themes;
	}
	public void setThemes(List<ThemeTransfer> themes) {
		this.themes = themes;
	}
	public List<WorkingGroupTransfer> getWorkingGroups() {
		return workingGroups;
	}
	public void setWorkingGroups(List<WorkingGroupTransfer> workingGroups) {
		this.workingGroups = workingGroups;
	}
	public List<Long> getAssemblies() {
		return assemblies;
	}
	public void setAssemblies(List<Long> assemblies) {
		this.assemblies = assemblies;
	}
	public List<ComponentTransfer> getExistingComponents() {
		return existingComponents;
	}
	public void setExistingComponents(List<ComponentTransfer> existingComponents) {
		this.existingComponents = existingComponents;
	}
	public List<ConfigTransfer> getExistingConfigs() {
		return existingConfigs;
	}
	public void setExistingConfigs(List<ConfigTransfer> existingConfigs) {
		this.existingConfigs = existingConfigs;
	}
	public List<ThemeTransfer> getExistingThemes() {
		return existingThemes;
	}
	public void setExistingThemes(List<ThemeTransfer> existingThemes) {
		this.existingThemes = existingThemes;
	}
	public List<WorkingGroupTransfer> getExistingWorkingGroups() {
		return existingWorkingGroups;
	}
	public void setExistingWorkingGroups(
			List<WorkingGroupTransfer> existingWorkingGroups) {
		this.existingWorkingGroups = existingWorkingGroups;
	}
	public CampaignTemplateTransfer getTemplate() {
		return template;
	}
	public void setTemplate(CampaignTemplateTransfer template) {
		this.template = template;
	}
	public CampaignTimelineEdgeTransfer getTimelineEdges() {
		return timelineEdges;
	}
	public void setTimelineEdges(CampaignTimelineEdgeTransfer timelineEdges) {
		this.timelineEdges = timelineEdges;
	}
}
