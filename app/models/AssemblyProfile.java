package models;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import utils.GlobalData;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ManagementTypes;
import enums.SupportedMembershipRegistration;

@Entity
@JsonInclude(Include.NON_NULL)
public class AssemblyProfile extends AppCivistBaseModel {
	@Id
	@GeneratedValue
//	@Column(name="assembly_profile_id")
	private Long assemblyProfileId;
	
	@OneToOne(mappedBy="profile")
	@JoinColumn(name="assembly_profile_id", unique= true, nullable=true, insertable=true, updatable=true)
	private Assembly assembly; 
	
	private String targetAudience;
	@Enumerated(EnumType.STRING)
	private SupportedMembershipRegistration supportedMembership = SupportedMembershipRegistration.INVITATION_AND_REQUEST; //   OPEN, INVITATION, REQUEST, INVITATION_AND_REQUEST
	@Enumerated(EnumType.STRING)
	private ManagementTypes managementType = ManagementTypes.OPEN; // assemblies are OPEN by default
	private String icon = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_ICON; // a small icon to represent the assembly
	private String cover = GlobalData.APPCIVIST_ASSEMBLY_DEFAULT_COVER;	// cover picture of the assembly, to appear on the top of its page
	
	private String primaryContactName;
	private String primaryContactPhone;
	private String primaryContactEmail;
	
	// TODO: 
	/**
	 * The find property is an static property that facilitates database query creation
	 */
	public static Finder<Long, AssemblyProfile> find = new Finder<>(AssemblyProfile.class);

	/**
	 * Empty constructor
	 */
	public AssemblyProfile() {
		super();
	}
	
	public AssemblyProfile(Assembly assembly, String targetAudience,
			SupportedMembershipRegistration supportedMembership,
			ManagementTypes managementType, String icon, String cover,
			String primaryContactName, String primaryContactPhone,
			String primaryContactEmail) {
		super();
		this.assembly = assembly;
		this.targetAudience = targetAudience;
		this.supportedMembership = supportedMembership;
		this.managementType = managementType;
		this.icon = icon;
		this.cover = cover;
		this.primaryContactName = primaryContactName;
		this.primaryContactPhone = primaryContactPhone;
		this.primaryContactEmail = primaryContactEmail;
	}

	public Long getAssemblyProfileId() {
		return assemblyProfileId;
	}

	public void setAssemblyProfileId(Long assemblyProfileId) {
		this.assemblyProfileId = assemblyProfileId;
	}

	public Assembly getAssembly() {
		return assembly;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}

	public String getTargetAudience() {
		return targetAudience;
	}

	public void setTargetAudience(String targetAudience) {
		this.targetAudience = targetAudience;
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

	public String getPrimaryContactName() {
		return primaryContactName;
	}

	public void setPrimaryContactName(String primaryContactName) {
		this.primaryContactName = primaryContactName;
	}

	public String getPrimaryContactPhone() {
		return primaryContactPhone;
	}

	public void setPrimaryContactPhone(String primaryContactPhone) {
		this.primaryContactPhone = primaryContactPhone;
	}

	public String getPrimaryContactEmail() {
		return primaryContactEmail;
	}

	public void setPrimaryContactEmail(String primaryContactEmail) {
		this.primaryContactEmail = primaryContactEmail;
	}
	
	/*
	 * Basic Data Queries
	 */

	public static void create(AssemblyProfile profile) {
		profile.save();
		profile.refresh();
	}

	public static AssemblyProfile read(Long profileId) {
		return find.ref(profileId);
	}

	public static AssemblyProfile createObject(AssemblyProfile profile) {
		profile.save();
		return profile;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static AssemblyProfile update(AssemblyProfile profile) {
		profile.update();
		profile.refresh();
		return profile;
	}

	public static List<AssemblyProfile> findBySimilarAudience(String query) {
		return find.where().ilike("targetAudience","%"+query+"%").findList();
	}

	public static AssemblyProfile findByAssembly(UUID uuid) {
		return find.where().eq("assembly.uuid", uuid).findUnique();
	}		
}
