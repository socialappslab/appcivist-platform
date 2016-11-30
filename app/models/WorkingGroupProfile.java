package models;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.misc.Views;
import utils.GlobalData;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ManagementTypes;
import enums.SupportedMembershipRegistration;

@Entity
@JsonInclude(Include.NON_NULL)
@ApiModel(value="WorkingGroupProfile", description="Profile of Working Groups")
public class WorkingGroupProfile extends AppCivistBaseModel {
	@Id @GeneratedValue
	private Long workingGroupProfileId;
	@OneToOne(mappedBy="profile")
	@JoinColumn(name="working_group_profile_id", unique= true, nullable=true, insertable=true, updatable=true)
	private WorkingGroup workingGroup;
	@JsonView(Views.Public.class)
	@Enumerated(EnumType.STRING)
	private SupportedMembershipRegistration supportedMembership = SupportedMembershipRegistration.INVITATION_AND_REQUEST;
	@JsonView(Views.Public.class)
	@Enumerated(EnumType.STRING)
	private ManagementTypes managementType = ManagementTypes.COORDINATED_AND_MODERATED;
	@JsonView(Views.Public.class)
	private String icon = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_ICON; // a small icon to represent the assembly
	@JsonView(Views.Public.class)
	private String cover = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_COVER;	// cover picture of the assembly, to appear on the top of its page
	
	// TODO: 
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, WorkingGroupProfile> find = new Finder<>(WorkingGroupProfile.class);

	/**
	 * Empty constructor
	 */
	public WorkingGroupProfile() {
		super();
	}

	public Long getWorkingGroupProfileId() {
		return workingGroupProfileId;
	}

	public void setWorkingGroupProfileId(Long assemblyProfileId) {
		this.workingGroupProfileId = assemblyProfileId;
	}

	public WorkingGroup getWorkingGroup() {
		return workingGroup;
	}

	public void setWorkingGroup(WorkingGroup wg) {
		this.workingGroup = wg;
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
	
	/*
	 * Basic Data Queries
	 */

	public static void create(WorkingGroupProfile profile) {
		profile.save();
		profile.refresh();
	}

	public static WorkingGroupProfile read(Long profileId) {
		return find.ref(profileId);
	}

	public static WorkingGroupProfile createObject(WorkingGroupProfile profile) {
		profile.save();
		return profile;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static WorkingGroupProfile update(WorkingGroupProfile profile) {
		profile.update();
		profile.refresh();
		return profile;
	}

	public static List<WorkingGroupProfile> findBySimilarAudience(String query) {
		return find.where().ilike("targetAudience","%"+query+"%").findList();
	}

	public static WorkingGroupProfile findByAssembly(UUID uuid) {
		return find.where().eq("assembly.uuid", uuid).findUnique();
	}		
}
