package models;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import enums.MembershipRoles;
import play.db.ebean.Model;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class WorkingGroup extends AppCivistBaseModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6809971288859856328L;
	
	@Id
	@GeneratedValue
    private Long groupId;
    private String name;
    private String text;
    private Boolean isPublic = true;
    private Boolean acceptRequests = true;
    private MembershipRoles membershipRole = MembershipRoles.MEMBER;
    private User creator;

    private Long testField;
    
    @ManyToMany(cascade=CascadeType.ALL)
    private List<Assembly> assemblies = new ArrayList<Assembly>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Resource> resources = new ArrayList<Resource>();
    
	@OneToMany(mappedBy="workingGroup", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<Config> workingGroupConfigs = new ArrayList<Config>();

	// TODO: think about how to make Assemblies, Groups, Users, Contributions, and Proposals; 
	// TODO: all be connected in a P2P architecture. 
	public static Model.Finder<Long, WorkingGroup> find = new Model.Finder<Long, WorkingGroup>(
            Long.class, WorkingGroup.class);

    public static WorkingGroup read(Long workingGroupId) {
        return find.ref(workingGroupId);
    }

    public static List<WorkingGroup> findAll() {
        return find.all();
    }

    public static Integer readByTitle(String name){
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

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
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

    public MembershipRoles getMembershipRole() {
        return membershipRole;
    }

    public void setMembershipRole(MembershipRoles membershipRole) {
        this.membershipRole = membershipRole;
    }

	public List<Config> getWorkingGroupConfigs() {
		return workingGroupConfigs;
	}

	public void setWorkingGroupConfigs(List<Config> workingGroupConfigs) {
		this.workingGroupConfigs = workingGroupConfigs;
	}
}
