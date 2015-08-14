package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Formula;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.ManagementTypes;
import enums.ResourceSpaceTypes;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class WorkingGroup extends AppCivistBaseModel {
	@Id
	@GeneratedValue
    private Long groupId;
	private UUID uuid = UUID.randomUUID();
    private String name;
    private String text;
    private Boolean isPublic = true;
    private Boolean acceptRequests = true;
    @Enumerated(EnumType.STRING)
    private ManagementTypes managementType = ManagementTypes.OPEN;
    private User creator;
    
    @Formula(select="select c from config c where c.targetUuid=${ta}.uuid")
	private List<Config> workingGroupConfigs = new ArrayList<Config>();

    
    @ManyToMany(cascade=CascadeType.ALL)
//    @JoinTable(name="working_groups_assembly",
//    	joinColumns = { 
//        	@JoinColumn(name = "assembly", referencedColumnName = "assembly_id")
//    	}, 
//    	inverseJoinColumns = { 
//    		@JoinColumn(name = "group", referencedColumnName = "group_id")
//    })
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    @OneToOne(fetch=FetchType.LAZY,cascade = CascadeType.ALL)
    private ResourceSpace resources;
    
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

    public List<Config> getWorkingGroupConfigs() {
		return workingGroupConfigs;
	}

	public void setWorkingGroupConfigs(List<Config> workingGroupConfigs) {
		this.workingGroupConfigs = workingGroupConfigs;
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

    public List<Assembly> getAssemblies() {
        return assemblies;
    }

    public void setAssemblies(List<Assembly> assemblies) {
        this.assemblies = assemblies;
    }

    public ResourceSpace getResources() {
        return resources;
    }

    public void setResourceSpace(ResourceSpace resources) {
        this.resources = resources;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getAcceptRequests() {
        return acceptRequests;
    }

    public void setAcceptRequests(Boolean acceptRequests) {
        this.acceptRequests = acceptRequests;
    }

    public ManagementTypes getManagementType() {
        return managementType;
    }

    public void setManagementType(ManagementTypes membershipRole) {
        this.managementType = membershipRole;
    }
}
