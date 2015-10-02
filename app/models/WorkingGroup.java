package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ContributionTypes;
import enums.ManagementTypes;
import enums.ResourceSpaceTypes;
import enums.SupportedMembershipRegistration;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class WorkingGroup extends AppCivistBaseModel {
	@Id
	@GeneratedValue
    private Long groupId;
	private UUID uuid = UUID.randomUUID();
    private String name;
    private String text;
    private Boolean listed = true;
    @Enumerated(EnumType.STRING)
    private SupportedMembershipRegistration supportedMembership = SupportedMembershipRegistration.INVITATION_AND_REQUEST;
    @Enumerated(EnumType.STRING)
    private ManagementTypes managementType = ManagementTypes.COORDINATED_AND_MODERATED;
    private User creator;

    /**
 	 * The group resource space contains its configurations, themes, associated campaigns
 	 */
 	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
 	@JsonIgnore
    private ResourceSpace resources = new ResourceSpace(ResourceSpaceTypes.WORKING_GROUP);
    
 	@JsonIgnore
 	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace forum = new ResourceSpace(ResourceSpaceTypes.WORKING_GROUP);
 	
 // TODO: check if it works
 	@JsonIgnore
 	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "workingGroups")
 	private List<ResourceSpace> containingSpaces;
 	
 	/* Transient direct access to entities in the resources resource space */
 	@Transient
 	private List<Theme> themes;
 	@Transient
 	private List<Config> configs;
 	@Transient
 	private List<Contribution> forumPosts;
 	@Transient
 	private List<Contribution> brainstormingContributions;
 	@Transient
 	private List<Contribution> proposals;
 	
	@Transient
	private List<Theme> existingThemes;
	@Transient
	private List<Contribution> existingContributions;
 	
	public static Finder<Long, WorkingGroup> find = new Finder<>(WorkingGroup.class);

    public WorkingGroup() {
		super();
		this.uuid = UUID.randomUUID();
		this.resources = new ResourceSpace(ResourceSpaceTypes.WORKING_GROUP);
	}

	public static WorkingGroup read(Long workingGroupId) {
        return find.ref(workingGroupId);
    }

    public static WorkingGroup readInAssembly(Long aid, Long workingGroupId) {
        return find.where().eq("assemblies.assembly_assembly_id",aid).eq("groupId", workingGroupId).findUnique();    
    }
    
    public static List<WorkingGroup> findAll() {
        return find.all();
    }
    
    public static List<WorkingGroup> findByAssembly(Long aid) {
        return find.where().eq("assemblies.assembly_assembly_id",aid).findList();
    }

    public static Integer numberByName(String name){
        ExpressionList<WorkingGroup> wGroups = find.where().eq("name", name);
        return wGroups.findList().size();
    }

    public static WorkingGroup create(WorkingGroup workingGroup) {
        workingGroup.save();
        workingGroup.refresh();
        return workingGroup;
    }

    public static WorkingGroup createObject(WorkingGroup workingGroup) {
        workingGroup.save();
        return workingGroup;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

    public User getCreator() {
        return creator;

    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

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

    public ResourceSpace getResources() {
        return resources;
    }

    public ResourceSpace getForum() {
		return forum;
	}

	public void setForum(ResourceSpace forum) {
		this.forum = forum;
	}

	public void setResources(ResourceSpace resources) {
		this.resources = resources;
	}

	public void setResourceSpace(ResourceSpace resources) {
        this.resources = resources;
    }
	
	public List<ResourceSpace> getContainingSpaces() {
		return containingSpaces;
	}

	public void setContainingSpaces(List<ResourceSpace> containingSpaces) {
		this.containingSpaces = containingSpaces;
	}

    public Boolean getListed() {
        return listed;
    }

    public void setListed(Boolean isPublic) {
        this.listed = isPublic;
    }

    public ManagementTypes getManagementType() {
        return managementType;
    }

    public void setManagementType(ManagementTypes membershipRole) {
        this.managementType = membershipRole;
    }

	public SupportedMembershipRegistration getSupportedMembership() {
		return supportedMembership;
	}

	public void setSupportedMembership(
			SupportedMembershipRegistration supportedMembership) {
		this.supportedMembership = supportedMembership;
	}

	
	public List<Theme> getThemes() {
		return resources.getThemes();
	}

	public void setThemes(List<Theme> themes) {
		this.resources.setThemes(themes);
	}

	public List<Config> getConfigs() {
		return resources.getConfigs();
	}

	public void setConfigs(List<Config> configs) {
		this.resources.setConfigs(configs);
	}

	public List<Contribution> getForumPosts() {
		// TODO: use only resources return resources.getContributionsFilteredByType(ContributionTypes.FORUM_POST);
		return forum.getContributions();
	}

	public void setForumPosts(List<Contribution> forumPosts) {
		this.forum.setContributions(forumPosts);
	}

	public List<Contribution> getBrainstormingContributions() {
		return resources.getContributionsFilteredByType(ContributionTypes.BRAINSTORMING);
	}

	public void setBrainstormingContributions(
			List<Contribution> brainstormingContributions) {
		this.resources.getContributions().addAll(brainstormingContributions);
	}

	public List<Contribution> getProposals() {
		return resources.getContributionsFilteredByType(ContributionTypes.PROPOSAL);
	}

	public void setProposals(List<Contribution> proposals) {
		this.resources.getContributions().addAll(proposals);
	}
	
	@JsonIgnore
	public List<Theme> getExistingThemes() {
		return existingThemes;
	}

	public void setExistingThemes(List<Theme> existingThemes) {
		this.existingThemes = existingThemes;
	}	
	
	@JsonIgnore
	public List<Contribution> getExistingContributions() {
		return existingContributions;
	}

	public void setExistingContributions(List<Contribution> existingContributions) {
		this.existingContributions = existingContributions;
	}
	
	public List<ResourceSpace> getContainingSpacesFilteredByType(ResourceSpaceTypes type) {
		return containingSpaces.stream()
				.filter(p -> p.getType() == type)
				.collect(Collectors.toList());
	}
	
	public List<Campaign> getWorkingGroupCampaigns(String status) {
		List<ResourceSpace> campaignSpaces = getContainingSpacesFilteredByType(ResourceSpaceTypes.CAMPAIGN);
		List<Campaign> campaigns = new ArrayList<>();
		for (ResourceSpace rs : campaignSpaces) {
			campaigns.addAll(rs.getCampaignsFilteredByStatus(status));
		}
		return campaigns;
	}
	
}
