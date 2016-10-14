package models;

import io.swagger.annotations.ApiModel;

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

import models.transfer.InvitationTransfer;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import providers.MyUsernamePasswordAuthProvider;

import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ManagementTypes;
import enums.MembershipStatus;
import enums.MembershipTypes;
import enums.MyRoles;
import exceptions.MembershipCreationException;

// TODO: replace membership invitations by direcltly using the model of membership, allowing the user in membership to be NULL
@Entity
@JsonInclude(Include.NON_EMPTY)
@Where(clause="removed=false")
@ApiModel(value="MembershipInvitation", description="Invitation to become a member of a Working Group or an Assembly")
public class MembershipInvitation extends AppCivistBaseModel {
	@Id @GeneratedValue private Long id;
	private String email;
	private Long userId;
	@Enumerated(EnumType.STRING) 
	private MembershipStatus status;
	@ManyToOne @JsonIgnore 
	private User creator;
	@OneToOne(mappedBy = "targetInvitation", cascade = CascadeType.ALL)
	@JsonManagedReference
	private TokenAction token;
	private Long targetId; // Id of Assembly or Working Group related to the invitation
	@Enumerated(EnumType.STRING) 
	private MembershipTypes targetType;
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<SecurityRole> roles = new ArrayList<SecurityRole>();
	@Transient 
	private Assembly targetAssembly;
	@Transient 
	private WorkingGroup targetGroup;
	
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
		if(targetId!=null && this.getTargetType().equals(MembershipTypes.ASSEMBLY)) {
			Assembly a = Assembly.read(targetId);
			return a;
		}
		return null;
	}

	public void setTargetAssembly(Assembly targetAssembly) {
		this.targetAssembly = targetAssembly;
	}

	public WorkingGroup getTargetGroup() {
		if(targetId!=null && this.getTargetType().equals(MembershipTypes.GROUP)) {
			WorkingGroup g = WorkingGroup.read(targetId);
			return g;
		}
		return null;
	}

	public void setTargetGroup(WorkingGroup targetGroup) {
		this.targetGroup = targetGroup;
	}
	
	/* DB Queries */
	
	public static MembershipInvitation read(Long id) {
		return find.ref(id);
	}
	
	public static MembershipInvitation create(MembershipInvitation mi) {
		mi.save();
		mi.refresh();
		return mi;
	}

	/**
	 * Create and send an email invitation to an assembly or a group (with the ID already set in the invitation form)
	 * 
	 * @param invitation the details of the invitation
	 * @param creator the user who is creating the invitation 
	 * @return the invitation that was just created, or null if something went wrong
	 * @throws MembershipCreationException 
	 */
	public static MembershipInvitation create(InvitationTransfer invitation, User creator) throws MembershipCreationException {
		Long targetId = invitation.getTargetId();
		MembershipTypes invitationType = invitation.getTargetType().equals("ASSEMBLY") ? MembershipTypes.ASSEMBLY : MembershipTypes.GROUP;
		String invitationBody = invitation.getInvitationEmail();
		if (targetId!=null) {
			return create(invitation, creator, targetId, invitationBody, invitationType);
		} else {
			throw new MembershipCreationException("Id of assembly or group is null");
		}
	}
	
	/**
	 * Create and send an email invitation to join a Working Group
	 * 
	 * @param invitation the details of the invitation
	 * @param creator the user who is creating the invitation 
	 * @param targetGroup the target Working Group
	 * @return the invitation that was just created, or null if something went wrong
	 * @throws MembershipCreationException 
	 */
	public static MembershipInvitation create(InvitationTransfer invitation, User creator, WorkingGroup targetGroup) throws MembershipCreationException {
		Long targetId = targetGroup != null ? targetGroup.getGroupId() : null;
		String invitationBody = targetGroup != null ? targetGroup.getInvitationEmail() :  Play.application().configuration().getString("appcivist.invitations.defaultBody");
		if (targetId!=null) {
			return create(invitation, creator, targetId, invitationBody, MembershipTypes.GROUP);
		} else {
			throw new MembershipCreationException("Id of assembly or group is null");
		}
	}
	
	/**
	 * Create and send an email invitation to join an Assembly
	 * 
	 * @param invitation the details of the invitation
	 * @param creator the user who is creating the invitation 
	 * @param targetGroup the target Working Group
	 * @return the invitation that was just created, or null if something went wrong
	 * @throws MembershipCreationException 
	 */
	public static MembershipInvitation create(InvitationTransfer invitation, User creator, Assembly targetAssembly) throws MembershipCreationException {
		Long targetId = targetAssembly != null ? targetAssembly.getAssemblyId() : null;
		String invitationBody = targetAssembly != null ? targetAssembly.getInvitationEmail() :  Play.application().configuration().getString("appcivist.invitations.defaultBody");
		
		if (targetId!=null) {
			return create(invitation, creator, targetId, invitationBody, MembershipTypes.ASSEMBLY);
		} else {
			throw new MembershipCreationException("Id of assembly or group is null");
		}
	}
	
	/**
	 * Create an invitation to a target assembly or group by ID
	 * 
	 * @param invitation
	 * @param creator
	 * @param targetId
	 * @param invitationBody
	 * @return
	 * @throws MembershipCreationException 
	 */
	public static MembershipInvitation create(InvitationTransfer invitation, User creator, Long targetId, String invitationBody, MembershipTypes targetType) throws MembershipCreationException {
		MembershipInvitation membershipInvitation = new MembershipInvitation();
		membershipInvitation.setCreator(creator);
		membershipInvitation.setEmail(invitation.getEmail());
		
		// Check if there is a user already associated with the email
		User invitedUser = User.findByEmail(invitation.getEmail());
		if(invitedUser!=null) {
			membershipInvitation.setUserId(invitedUser.getUserId());
		}
		
		membershipInvitation.setLang(creator.getLanguage());
		membershipInvitation.setStatus(MembershipStatus.INVITED);
		membershipInvitation.setTargetType(targetType);
		membershipInvitation.setTargetId(targetId);

		if(!membershipInvitation.existsAlready()) {
			List<SecurityRole> roles = new ArrayList<>();
			roles.add(SecurityRole.findByName(MyRoles.MEMBER.getName()));
	
			if (invitation.getCoordinator())
				roles.add(SecurityRole.findByName(MyRoles.COORDINATOR
						.getName()));
			if (invitation.getModerator())
				roles.add(SecurityRole.findByName(MyRoles.MODERATOR
						.getName()));
	
			membershipInvitation.setRoles(roles);
			membershipInvitation = MembershipInvitation.create(membershipInvitation);
		
			final String token = UUID.randomUUID().toString();
			TokenAction ta = TokenAction.create(TokenAction.Type.MEMBERSHIP_INVITATION, token, membershipInvitation);
			ta.refresh();
			
			// Preparing and sending the Invitation Email
			membershipInvitation.sendInvitationEmail(invitationBody, token);		
			return membershipInvitation;
		} else {
			throw new MembershipCreationException("Membership already exists");
		}
	}

	private boolean existsAlready() {
		// checking if the user is already a member
		Boolean isMember = Membership.checkIfExistsByEmailAndId(this.email, this.targetId, this.targetType);
		// checking if the user has already been invited
		Boolean invited = find.where()
			.eq("targetId", this.targetId)
			.eq("email", this.email)
			.eq("status",MembershipStatus.INVITED)
			.findList().size() > 0;
			
		return isMember || invited;
	}

	public void sendInvitationEmail(String invitationBody, String token) {
		String baseInvitationUrl = Play.application().configuration().getString("appcivist.invitations.baseUrl");
		String invitationUrl = baseInvitationUrl + "/invitation/"+token;
		String invitationEmailText = invitationBody+"\n\n\n"+Messages.get("membership.invitation.email.link")+": "+invitationUrl;
		String invitationEmailHTML = invitationBody+"<br><br>"+"<a href='"+invitationUrl+"'>"+Messages.get("membership.invitation.email.link")+"</a>";
		Logger.info("Sending group invitation to: "+this.getEmail());
		Logger.info("Invitation email: "+invitationEmailText);
		MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
		String emailSubject = Messages.get("membership.invitation.email.subject", this.getTargetType()+"("+this.getTargetId()+")");
		provider.sendInvitationByEmail(this, invitationEmailText, invitationEmailHTML, emailSubject);		
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

	public static void acceptAndCreateMembershipByToken(MembershipInvitation mi, User user) throws MembershipCreationException {
		mi.setStatus(MembershipStatus.ACCEPTED);
		mi.setUserId(user.getUserId());

		if (mi.getTargetType().equals(MembershipTypes.ASSEMBLY)) {
			MembershipAssembly ma = new MembershipAssembly();
			Assembly a = Assembly.read(mi.getTargetId());
			ma.setAssembly(a);
			ma.setCreator(mi.getCreator());
			ma.setStatus(MembershipStatus.ACCEPTED);
			//ma.setRoles(mi.getRoles());
			ma.setTargetUuid(a.getUuid());
			ma.setUser(user);
			ma = (MembershipAssembly) MembershipAssembly.create(ma);
			for (SecurityRole role : mi.getRoles()) {
				MembershipRole mr = new MembershipRole(ma.getMembershipId(),role.getRoleId());
				mr.save();
			}
		} else {
			MembershipGroup mg = new MembershipGroup();
			WorkingGroup g = WorkingGroup.read(mi.getTargetId());
			mg.setWorkingGroup(g);
			mg.setCreator(mi.getCreator());
			mg.setStatus(MembershipStatus.ACCEPTED);
			//TODO fix the cascade of membership to roles
			//mg.setRoles(mi.getRoles());
			mg.setTargetUuid(g.getUuid());
			mg.setUser(user);
			mg = (MembershipGroup) MembershipGroup.create(mg);
			
			for (SecurityRole role : mi.getRoles()) {
				MembershipRole mr = new MembershipRole(mg.getMembershipId(),role.getRoleId());
				mr.save();
			}
			// Create membership to assemblies in which the working group is listed
			List<Long> wgAssembliesIds = g.getAssemblies();
			for (Long assemblyId : wgAssembliesIds) {
				MembershipAssembly ma = new MembershipAssembly();
				Assembly a = Assembly.read(assemblyId);
				ma.setAssembly(a);
				ma.setCreator(mi.getCreator());
				ma.setStatus(MembershipStatus.ACCEPTED);
				
				// setup roles for the user in the assembly
				List<SecurityRole> roles = new ArrayList<SecurityRole>();
				// add the MEMBER role always
				roles.add(SecurityRole.findByName("MEMBER"));
				ManagementTypes assemblyMgtType = a.getProfile().getManagementType();
				// add COORDINATOR and MODERATOR if assembly is COORDINATED_AND_MODERATED
				if (assemblyMgtType.equals(ManagementTypes.COORDINATED_AND_MODERATED)) {
					roles.add(SecurityRole.findByName("COORDINATOR"));
				    roles.add(SecurityRole.findByName("MODERATOR"));
				} else if (assemblyMgtType.equals(ManagementTypes.COORDINATED)) {
					// add COORDINATOR only if assembly is COORDINATED
					roles.add(SecurityRole.findByName("COORDINATOR"));
				} else {
					// add MODERATOR only if assembly is MODERATED
					roles.add(SecurityRole.findByName("MODERATOR"));
				}
				
				//ma.setRoles(roles);
				ma.setTargetUuid(a.getUuid());
				ma.setUser(user);
				if (!ma.alreadyExists()) {
					ma = (MembershipAssembly) MembershipAssembly.create(ma);
					for (SecurityRole role : roles) {
						MembershipRole mr = new MembershipRole(ma.getMembershipId(),role.getRoleId());
						mr.save();
					}	
				}
			}
		}

		// Update Invitation record
		mi.update();		
		mi.refresh();
	}
	
	public static void rejectAndUpdateInvitationByToken(MembershipInvitation mi, User user) {
		mi.setStatus(MembershipStatus.REJECTED);
		mi.setUserId(user.getUserId());
		mi.update();		
		mi.refresh();		
	}

	public static MembershipInvitation findByUserIdTargetIdAndType(Long uid,
			Long aid, MembershipTypes type) {
		return find.where()
				.eq("userId", uid)
				.eq("targetId", aid)
				.eq("targetType", type)
				.findUnique();
	}
}
