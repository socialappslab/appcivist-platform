package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import models.transfer.InvitationTransfer;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ContributionTypes;
import enums.ManagementTypes;
import enums.MembershipStatus;
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
	@Column(name="text", columnDefinition="text")
    private String text;
    private Boolean listed = true;
    private String majorityThreshold;
    private Boolean blockMajority;
    private User creator;
    
    @OneToOne(cascade=CascadeType.ALL)
	@JsonIgnoreProperties({ "workingGroupProfileId", "workingGroup" })
	@JsonInclude(Include.NON_EMPTY)
	private WorkingGroupProfile profile = new WorkingGroupProfile();

	@Column(name="invitationEmail", columnDefinition="text")
	private String invitationEmail;
	@Transient private List<InvitationTransfer> invitations;
    
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
 
	@ManyToMany(cascade = CascadeType.ALL, mappedBy="workingGroupAuthors")
	@JsonBackReference
	@Where(clause="${ta}.removed=false")
	private List<Contribution> proposals = new ArrayList<Contribution>();
 	
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private Long forumResourceSpaceId;
	@Transient
	@JsonInclude(Include.NON_EMPTY)
	private Long resourcesResourceSpaceId;
 	
 // TODO: check if it works
 	@JsonIgnore
 	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "workingGroups")
 	private List<ResourceSpace> containingSpaces;
 	
 	@JsonIgnore
 	@OneToMany(cascade = CascadeType.REMOVE, mappedBy="workingGroup",fetch=FetchType.LAZY)
 	private List<MembershipGroup> members; 
 	
	@Transient
	@JsonIgnore
	private List<Theme> existingThemes;
	@Transient
	@JsonIgnore
	private List<Contribution> existingContributions;
 	
	@Transient
	private List<Long> assemblies;
	@Transient
	private List<Long> campaigns;
	
	
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
		// 1. Check first for existing entities in ManyToMany relationships. Save them for later update
		List<Theme> existingThemes = workingGroup.getExistingThemes();
		
		// 2. Create the new working group
		workingGroup.save();

		// 3. Add existing entities in relationships to the manytomany resources then update
		ResourceSpace groupResources = workingGroup.getResources();
		if (existingThemes != null && !existingThemes.isEmpty())
			groupResources.getThemes().addAll(existingThemes);
		
		groupResources.update();
		
		// 4. Refresh the new campaign to get the newest version
		workingGroup.refresh();

		// 5. Add the creator as a members with roles MODERATOR, COORDINATOR and MEMBER
		MembershipGroup mg = new MembershipGroup();
		mg.setWorkingGroup(workingGroup);
		mg.setCreator(workingGroup.getCreator());
		mg.setUser(workingGroup.getCreator());
		mg.setStatus(MembershipStatus.ACCEPTED);
		mg.setLang(workingGroup.getLang());
	
		List<SecurityRole> roles = new ArrayList<SecurityRole>();
		roles.add(SecurityRole.findByName("MEMBER"));
		roles.add(SecurityRole.findByName("COORDINATOR"));
		roles.add(SecurityRole.findByName("MODERATOR"));
		mg.setRoles(roles);
		
		MembershipGroup.create(mg);
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

	public WorkingGroupProfile getProfile() {
		return profile;
	}

	public void setProfile(WorkingGroupProfile profile) {
		this.profile = profile;
	}

	public String getInvitationEmail() {
		return invitationEmail;
	}

	public void setInvitationEmail(String invitationEmail) {
		this.invitationEmail = invitationEmail;
	}

	public List<InvitationTransfer> getInvitations() {
		return invitations;
	}

	public void setInvitations(List<InvitationTransfer> invitations) {
		this.invitations = invitations;
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

	public ManagementTypes getManagementType() {
        return this.profile.getManagementType();
    }

    public void setManagementType(ManagementTypes membershipRole) {
        this.profile.setManagementType(membershipRole);;
    }

	public SupportedMembershipRegistration getSupportedMembership() {
		return this.profile.getSupportedMembership();
	}

	public void setSupportedMembership(
			SupportedMembershipRegistration supportedMembership) {
		this.profile.setSupportedMembership(supportedMembership);
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
	
	public Long getForumResourceSpaceId() {
		return forum !=null ? forum.getResourceSpaceId() : null;
	}

	public void setForumResourceSpaceId(Long id) {
		if(this.forum!=null && this.forum.getResourceSpaceId() == null)
			this.forum.setResourceSpaceId(id);
	}

	public Long getResourcesResourceSpaceId() {
		return resources !=null ? resources.getResourceSpaceId() : null;
	}
	
	public void setResourcesResourceSpaceId(Long id) {
		if(this.resources!=null && this.resources.getResourceSpaceId() == null)
			this.resources.setResourceSpaceId(id);
	}
	
	public List<Contribution> getBrainstormingContributions() {
		return resources.getContributionsFilteredByType(ContributionTypes.BRAINSTORMING);
	}

	public void setBrainstormingContributions(
			List<Contribution> brainstormingContributions) {
		this.resources.getContributions().addAll(brainstormingContributions);
	}

	public List<Contribution> getProposals() {
//		return resources.getContributionsFilteredByType(ContributionTypes.PROPOSAL);
		return this.proposals;
	}

	public void setProposals(List<Contribution> proposals) {
//		this.resources.getContributions().addAll(proposals);
		this.proposals = proposals;
	}

	public List<MembershipGroup> getMembers() {
		return this.members;
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
	
	public List<Long> getAssemblies() {
		List <Long> assemblyIds = new ArrayList<>();
		List<ResourceSpace> spaces = this.containingSpaces.stream().filter(p -> p.getType() == ResourceSpaceTypes.ASSEMBLY).collect(Collectors.toList());
		
		for (ResourceSpace resourceSpace : spaces) {
			Assembly a = resourceSpace.getAssemblyResources();
			if(a!=null) {
				assemblyIds.add(a.getAssemblyId());
			}
		}
		return assemblyIds;
	}
	
	public List<Long> getCampaigns() {
		List <Long> campaignIds = new ArrayList<>();
		List<ResourceSpace> spaces = this.containingSpaces.stream().filter(p -> p.getType() == ResourceSpaceTypes.CAMPAIGN).collect(Collectors.toList());
		
		for (ResourceSpace resourceSpace : spaces) {
			Campaign a = resourceSpace.getCampaign();
			if(a!=null) {
				campaignIds.add(a.getCampaignId());
			}
		}
		return campaignIds;
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
