package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ManagementTypes;
import enums.MembershipCreationTypes;
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
    private ManagementTypes managementType = ManagementTypes.OPEN;
    private User creator;
    
//    @Formula(select="select c from config c where c.targetUuid=${ta}.uuid")
//	private List<Config> workingGroupConfigs = new ArrayList<Config>();
   
    @ManyToMany(cascade=CascadeType.ALL)
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    /**
 	 * The group resource space contains its configurations, themes, associated campaigns
 	 */
 	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
 	@JsonIgnoreProperties({ "uuid" })
    private ResourceSpace resources = new ResourceSpace(ResourceSpaceTypes.WORKING_GROUP);
    
 	@OneToOne(fetch = FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonInclude(Include.NON_EMPTY)
	private ResourceSpace forum = new ResourceSpace(ResourceSpaceTypes.WORKING_GROUP);
 	
 // TODO: check if it works
 	@JsonIgnore
 	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "workingGroups")
 	private List<ResourceSpace> targetSpaces;

	// TODO: think about how to make Assemblies, Groups, Users, Contributions, and Proposals; 
	// TODO: all be connected in a P2P architecture. 
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

//    public List<Config> getWorkingGroupConfigs() {
//    	if (workingGroupConfigs == null) {
//    		workingGroupConfigs = new ArrayList<Config>();
//    	}
//		return workingGroupConfigs;
//	}
//
//   public void setWorkingGroupConfigs(List<Config> workingGroupConfigs) {
//	   if (workingGroupConfigs == null)
//		   workingGroupConfigs = new ArrayList<Config>();
//	   this.workingGroupConfigs = workingGroupConfigs;
//	}

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

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
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
	
	public List<ResourceSpace> getTargetSpaces() {
		return targetSpaces;
	}

	public void setTargetSpaces(List<ResourceSpace> targetSpaces) {
		this.targetSpaces = targetSpaces;
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
}
