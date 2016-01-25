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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.MembershipStatus;
import enums.MembershipTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
@Where(clause="removed=false")
public class MembershipInvitation extends AppCivistBaseModel {
	@Id @GeneratedValue private Long id;
	private String email;
	private Long userId;
	@Enumerated(EnumType.STRING) private MembershipStatus status;
	@ManyToOne @JsonIgnore private User creator;
	@OneToOne private TokenAction token;
	private Long targetId; // Id of Assembly or Working Group related to the invitation
	@Enumerated(EnumType.STRING) private MembershipTypes targetType;
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<SecurityRole> roles = new ArrayList<SecurityRole>();
	@Transient private Assembly targetAssembly;
	@Transient private WorkingGroup targetGroup;
	
	public static Finder<Long, MembershipInvitation> find = new Finder<>(MembershipInvitation.class);

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public MembershipStatus getStatus() {
		return status;
	}

	public void setStatus(MembershipStatus status) {
		this.status = status;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public TokenAction getToken() {
		return token;
	}

	public void setToken(TokenAction token) {
		this.token = token;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public MembershipTypes getTargetType() {
		return targetType;
	}

	public void setTargetType(MembershipTypes targetType) {
		this.targetType = targetType;
	}

	public List<SecurityRole> getRoles() {
		return roles;
	}

	public void setRoles(List<SecurityRole> roles) {
		this.roles = roles;
	}

	public Assembly getTargetAssembly() {
		return this.targetType.equals(MembershipTypes.ASSEMBLY) ? Assembly.read(targetId) : null;
	}

	public void setTargetAssembly(Assembly targetAssembly) {
		this.targetAssembly = targetAssembly;
	}

	public WorkingGroup getTargetGroup() {
		return this.targetType.equals(MembershipTypes.GROUP) ? WorkingGroup.read(targetId) : null;
	}

	public void setTargetGroup(WorkingGroup targetGroup) {
		this.targetGroup = targetGroup;
	}
	
	/* DB Queries */
	
	public static MembershipInvitation create(MembershipInvitation mi) {
		mi.save();
		mi.refresh();
		return mi;
	}

	public static List<MembershipInvitation> findByTargetId(Long targetId) {	
		return find.where().eq("targetId",targetId).findList();
	}
	public static List<MembershipInvitation> findByTargetIdAndStatus(Long targetId, String status) {	
		return find.where()
				.eq("targetId", targetId)
				.eq("status", status.toUpperCase())
				.findList();
	}

	public static MembershipInvitation findByToken(UUID token) {
		return find.where()
				.eq("token.token", token.toString())
				.findUnique();
	}
}
