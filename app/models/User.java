package models;

import enums.MembershipStatus;
import io.swagger.annotations.ApiModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import models.TokenAction.Type;
import models.misc.Views;
import models.transfer.AssemblyTransfer;
import play.Play;
import play.db.ebean.Transactional;
import play.i18n.Lang;
import play.mvc.Http.Context;
import providers.ExistingGroupSignupIdentity;
import providers.GroupSignupIdentity;
import providers.InvitationSignupIdentity;
import providers.LanguageSignupIdentity;
import utils.GlobalData;
import utils.GlobalDataConfigKeys;
import utils.security.HashGenerationException;
import utils.security.HashGeneratorUtils;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Subject;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthUser;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.feth.play.module.pa.user.PicturedIdentity;

import controllers.Users;
import delegates.AssembliesDelegate;
import enums.MyRoles;
import exceptions.MembershipCreationException;
import exceptions.TokenNotValidException;

@Entity
@JsonInclude(Include.NON_EMPTY)
@Table(name="appcivist_user")
@Where(clause="active=true")
@ApiModel(value="User", description="Model representing each user in AppCivist")
public class User extends AppCivistBaseModel implements Subject {

	@Id
	@GeneratedValue
	private Long userId;
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;
	@Column(unique=true)
	private String email;
	@JsonView(Views.Public.class)
	private String name;
	@JsonView(Views.Public.class)
	@Column(unique=true)
	private String username;
	private String language = GlobalData.DEFAULT_LANGUAGE;	
	@Column(name = "email_verified")
	private Boolean emailVerified;
	@JsonView(Views.Public.class)
	@Column(name = "profile_pic")
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	@JsonIgnoreProperties({"creator", "resourceId", "location", "resourceType"})
	private Resource profilePic;
	@Column
	private boolean active = true;
	// TODO create a transfer model for user and place the session key only there
	@Transient
	private String sessionKey;

	/*
	 * Relationships
	 */
	@JsonIgnore
	@OneToMany(mappedBy="user", cascade = CascadeType.PERSIST)
	private List<LinkedAccount> linkedAccounts;

	@JsonIgnore
	@OneToMany(mappedBy = "targetUser", cascade = CascadeType.PERSIST)
	private List<TokenAction> tokenActions;

    @JsonIgnore
    @OneToMany(mappedBy="user", cascade = CascadeType.PERSIST)
    private List<Membership> memberships = new ArrayList<Membership>();

    //	@ManyToMany(cascade = CascadeType.ALL)

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "User_Security_Roles", 
		joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = true, insertable = true) }, 
		inverseJoinColumns = { @JoinColumn(name = "role_id", referencedColumnName = "role_id", updatable = true, insertable = true) }
	)
	private List<SecurityRole> roles = new ArrayList<SecurityRole>();
	
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "User_User_Permission", 
		joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "user_id", updatable = true, insertable = true) }, 
		inverseJoinColumns = { @JoinColumn(name = "permission_id", referencedColumnName = "permission_id", updatable = true, insertable = true) }
	)
    private List<UserPermission> permissions = new ArrayList<UserPermission>();
    
	@Transient
	private List<String> providers;
	/**
	 * Static finder property
	 */
	public static Finder<Long, User> find = new Finder<>(User.class);
	
    public User(){
		super();
	}
	

	/************************************************************************************************
	 * Getters & Setters
	 ************************************************************************************************/

	
	public Long getUserId() {
		return userId;
	}


	public void setUserId(Long userId) {
		this.userId = userId;
	}


	public UUID getUuid() {
		return uuid;
	}


	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}


	public String getUuidAsString() {
		return uuid.toString();
	}


	public void setUuidAsString(String uuidAsString) {
		this.uuidAsString = uuidAsString;
		this.uuid = UUID.fromString(uuidAsString);
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getLanguage() {
		return language;
	}


	public void setLanguage(String language) {
		this.language = language;
	}


	public Boolean getEmailVerified() {
		return emailVerified;
	}


	public Boolean isEmailVerified() {
		return emailVerified != null ? emailVerified : false;
	}


	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}


	public boolean isActive() {
		return active;
	}


	public void setActive(boolean active) {
		this.active = active;
	}


	public String getSessionKey() {
		return sessionKey;
	}


	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}


	public List<TokenAction> getTokenActions() {
		return tokenActions;
	}


	public void setTokenActions(List<TokenAction> tokenActions) {
		this.tokenActions = tokenActions;
	}


	public List<Membership> getMemberships() {
		return memberships;
	}


	public void setMemberships(List<Membership> memberships) {
		this.memberships = memberships;
	}


	public List<SecurityRole> getRoles() {
		return roles;
	}


	public void setRoles(List<SecurityRole> roles) {
		this.roles = roles;
	}


	public Resource getProfilePic() {
		return profilePic;
	}


	public void setProfilePic(Resource profilePic) {
		this.profilePic = profilePic;
	}

	
	@Override
	public List<? extends Permission> getPermissions() {
		return this.permissions;
	}
	
	public void setPermissions(List<UserPermission> permissions) {
		this.permissions = permissions;
	}

	public List<LinkedAccount> getLinkedAccounts() {
		return linkedAccounts;
	}

	public void setLinkedAccounts(List<LinkedAccount> linkedAccounts) {
		this.linkedAccounts = linkedAccounts;
	}


	@Override
	public String getIdentifier() {
		//return this.userId.toString();
		return this.username;
	}
		
	/************************************************************************************************
	 * Basic Persistence queries
	 ************************************************************************************************/
	
	public static List<User> findAll() {
		List<User> users = find.all();
		return users;
	}

	public static void create(User user) {
		user.save();
		user.refresh();
	}
	
	public static User read(Long userId) {
		return find.ref(userId);
	}

	public static User createObject(User user) {
		user.save();
		return user;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}
	
	/************************************************************************************************
	 * Additional persistence queries (some aliases of the previous)
	 ************************************************************************************************/
	public static User findByUserId(Long id) {
		return read(id);
	}

	public static User findByEmail(String email) {
		return findByEmailList(email).findUnique();
	}
	
	private static ExpressionList<User> findByEmailList(final String email) {
		return find.where().eq("email", email).eq("active",true);
	}

	public static User findByUsernamePasswordIdentity(
			UsernamePasswordAuthUser user) {
		return findByUsernamePasswordIdentityList(user).findUnique();
	}

	private static ExpressionList<User> findByUsernamePasswordIdentityList(
			final UsernamePasswordAuthUser identity) {
		return findByEmailList(identity.getEmail()).eq(
				"linkedAccounts.providerKey", identity.getProvider());
	}

	public static User findByProviderAndKey(String providerKey, String providerUserId) {
		return find.where().eq("linkedAccounts.providerKey", providerKey)
				.eq("linkedAccounts.providerUserId", providerUserId).eq("active",true).findUnique();
	}

	/************************************************************************************************
	 * Password and User Management operations
	 ************************************************************************************************/
	public void resetPassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUser(this, Type.PASSWORD_RESET);
	}
	
	public void changePassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
		if (a == null) {
			if (create) {
				a = LinkedAccount.create(authUser);
				a.setUser(this);
			} else {
				throw new RuntimeException(
						"Account not enabled for password usage");
			}
		}
		a.setProviderUserId(authUser.getHashedPassword());
		a.save();
	}
	
	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}

	@Transactional
	public static void verify(final User unverified) {
		// You might want to wrap this into a transaction
		// Model..em().getTransaction()
		User user = User.read(unverified.getUserId());
		user.setEmailVerified(true);
		user.save();
		// user.update(unverified.getId());
		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
	}
	
	/************************************************************************************************
	 * Authentication & Authorization
	 ************************************************************************************************/
	
	public static boolean existsByAuthUserIdentity(
			final AuthUserIdentity identity) {
		final ExpressionList<User> exp = findAuthUserByIdentity(identity);
		return exp.findRowCount() > 0;
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
		} else {
			return findAuthUserByIdentity(identity).findUnique();
		}
	}
	
	private static ExpressionList<User> findAuthUserByIdentity(
			final AuthUserIdentity identity) {
		return find.where()//.eq("active", true) // adding users soft deletion capabilities
				.eq("linkedAccounts.providerUserId", identity.getId())
				.eq("linkedAccounts.providerKey", identity.getProvider())
				.eq("active",true);
	}
	
	public void merge(final User otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		//otherUser.active = false;
		
		// delete merged user
		otherUser.delete();
		Ebean.save(Arrays.asList(new User[] { otherUser, this }));
	}

	public static User createFromAuthUser(final AuthUser authUser) throws HashGenerationException, MalformedURLException, TokenNotValidException, MembershipCreationException {
		/*
		 * 0. Zero step, create a new User instance
		 */
		User user = new User();

		/*
		 * 1. We start by already adding the role USER and a LINKEDACCOUNT to
		 * the user instance to be created
		 */
		user.roles = Collections.singletonList(SecurityRole.findByName(MyRoles.USER.toString()));
		user.linkedAccounts = Collections.singletonList(LinkedAccount.create(authUser));
		user.active = true;
		Long userId = null;

		/*
		 * 2. Second, we will try to see if the email is sent and find if the
		 * user is already part of the system
		 */
		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
		
			/*
			 * Remember, even when getting them from FB & Co., emails should be
			 * verified within the application as a security breach there might
			 * break your security as well!
			 */
			user.email = identity.getEmail();
			user.emailVerified = false;
			userId = User.findByEmail(identity.getEmail()) != null ? User
					.findByEmail(identity.getEmail()).getUserId() : null;
		}

		/*
		 * 4. If part of the signup form there is also a name, and the person
		 * bean does not have the name on it add this name as Firstname
		 */
		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (user.getName() == null || user.getName() == "") {
				user.setName(name);
			}
		}

		/*
		 * 5. If the picture URL is also set on the User form, add the picture
		 * to the user
		 */
		if (authUser instanceof PicturedIdentity) {
			final PicturedIdentity identity = (PicturedIdentity) authUser;
			final String picture = identity.getPicture();
			if (picture != null) {
				// TODO: generate large, medium and thumbnail version of the "picture" URL
				Resource profilePicResource = new Resource(user, new URL(picture), new URL(picture), new URL(picture), new URL(picture));				
				user.setProfilePic(profilePicResource);
			} else {
				Resource profilePicResource = getDefaultProfilePictureResource(user);
				user.setProfilePic(profilePicResource);
			}
		} else {
			Resource profilePicResource = getDefaultProfilePictureResource(user); 	
			user.setProfilePic(profilePicResource);
		}

		/*
		 * 6. always the email is going to be validated by google
		 */
		if (authUser instanceof GoogleAuthUser) {
			user.setEmailVerified(true);
		}

		/*
		 * 7. Generate the username
		 * TODO add username to the signup form
		 */
		user.setUsername(user.getEmail());
		
		/*
		 * 8. Set language of user
		 */
		// TODO get the default language from request	
		
		if (authUser instanceof LanguageSignupIdentity) {
			final LanguageSignupIdentity identity = (LanguageSignupIdentity) authUser;
			String lang = identity.getLanguage();
			String userLanguage = lang == null ? Play.application().configuration().getString("default.language") : identity.getLanguage();		
			user.setLanguage(userLanguage);

		}

		UserProfile userProfile = new UserProfile(user, null, "", "",  "", null, "");
		userProfile.save();

		/*
		 * 9. Create the new user
		 */
		if (userId != null)
			User.update(userId); //TODO CHECK THIS PART AGAIN
		else {
			user.save();
			user.refresh();
		}
		
		
		/*
		 * 10. If the new user is also creating an assembly, create the assembly
		 */
		if (authUser instanceof GroupSignupIdentity) {
			GroupSignupIdentity groupSignupUser = (GroupSignupIdentity) authUser;
			AssemblyTransfer newAssemblyTransfer = groupSignupUser.getNewAssembly();
			if (newAssemblyTransfer!=null) {
				// create the assembly with user as creator
				AssembliesDelegate.create(newAssemblyTransfer, user, null, null, "true");
			}
		}
		/*
		 * 11. If the new user have an existing assembly, create membership for the assembly
		 */
		if (authUser instanceof ExistingGroupSignupIdentity) {
			ExistingGroupSignupIdentity groupSignupUser = (ExistingGroupSignupIdentity) authUser;
			AssemblyTransfer newAssemblyTransfer = groupSignupUser.getExistingAssembly();
			if (newAssemblyTransfer!=null) {
				Assembly assembly = null;
				if(newAssemblyTransfer.getAssemblyId()!=null && newAssemblyTransfer.getAssemblyId()!=0){
					assembly = Assembly.read(newAssemblyTransfer.getAssemblyId());
				}else if(newAssemblyTransfer.getShortname()!=null && !newAssemblyTransfer.getShortname().equals("")){
					assembly = Assembly.findByShortName(newAssemblyTransfer.getShortname());
				}else if(newAssemblyTransfer.getUuid()!=null && !newAssemblyTransfer.getUuid().equals("")){
					assembly = Assembly.readByUUID(newAssemblyTransfer.getUuid());
				}
				if (assembly!=null){
					// Add the user as a members with roles MEMBER
					MembershipAssembly ma = new MembershipAssembly();
					ma.setAssembly(assembly);
					ma.setCreator(assembly.getCreator());
					ma.setUser(user);
					ma.setStatus(MembershipStatus.ACCEPTED);
					ma.setLang(user.getLang());
					List<SecurityRole> roles = new ArrayList<>();
					roles.add(SecurityRole.findByName("MEMBER"));
					ma.setRoles(roles);
					MembershipAssembly.create(ma);

					/*
					 * 12. If assembly is configured to join new members into specific Working Grops automatically
					 *     create group memberships
					 */

					Config c = assembly.getResources().getConfigByKey(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_AUTO_MEMBERSHIP_WORKING_GROUPS);
					if (c!=null) {
						String autoWgsList = c.getValue();
						List<String> wgNames = Arrays.asList(autoWgsList.split(","));

						for (String wgName: wgNames) {
							WorkingGroup wg = WorkingGroup.readByName(wgName.trim());
							if (wg!=null) {
								// Add the user as a members with roles MEMBER
								MembershipGroup mg = new MembershipGroup();
								mg.setWorkingGroup(wg);
								mg.setCreator(assembly.getCreator());
								mg.setUser(user);
								mg.setStatus(MembershipStatus.ACCEPTED);
								mg.setLang(user.getLang());
								roles = new ArrayList<>();
								roles.add(SecurityRole.findByName("MEMBER"));
								mg.setRoles(roles);
								MembershipGroup.create(mg);
							}
						}
					}
				}
			}
		}
		
		if (authUser instanceof InvitationSignupIdentity) {
			InvitationSignupIdentity invitationUser = (InvitationSignupIdentity) authUser;
			UUID token = invitationUser.getInvitationToken();
			
			if (token != null) {
				// accept invitation
				
				// 1. Verify the token
				MembershipInvitation mi = MembershipInvitation.findByToken(token);
				final TokenAction ta = Users.tokenIsValid(token.toString(),Type.MEMBERSHIP_INVITATION); 
				if (ta == null) {
					throw new TokenNotValidException();
				}
				ta.delete();
				
				// 2. Accept the invitation 
				//    and create the membership to the target assembly/group
				MembershipInvitation.acceptAndCreateMembershipByToken(mi, user);				
			}
		}

		
		return user;
	}

	private static String getDefaultProfilePictureURL(String email) throws HashGenerationException {
		String userMd5Hash = HashGeneratorUtils.generateMD5(email);
		String gravatarURL = GlobalData.GRAVATAR_BASE_URL+userMd5Hash+"?d=identicon";
		return gravatarURL;
	}
	
	private static Resource getDefaultProfilePictureResource(User user) throws HashGenerationException, MalformedURLException {
		String picture = getDefaultProfilePictureURL(user.getEmail());
		String large = picture+"&s=128";
		String medium = picture+"&s=64";
		String thumbnail = picture+"&s=32"; 
		Resource profilePicResource = new Resource(user, new URL(picture), new URL(large), new URL(medium), new URL(thumbnail));
		return profilePicResource;
	}
	public void addRole(SecurityRole role) {
		this.roles.add(role);	
	}
	
	@SuppressWarnings("unused")
	private static String generateUsername(String email) {
		String newUsername = email.split("@")[0];
		int count = models.User.usernameExists(newUsername);
		if (count > 0) {
			newUsername += (count++);
		}
		return newUsername;
	}

	private static int usernameExists(String newUsername) {
		return find.where().eq("active", true)
				.like("username", "%" + newUsername + "%").findList().size();
	}
	
	// TODO check play authenticate inherited code
	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(
				User.findByAuthUserIdentity(newUser));
	}
	
	public List<String> getProviders() {
		final Set<String> providerKeys = new HashSet<String>(
				linkedAccounts.size());
		for (final LinkedAccount acc : linkedAccounts) {
			providerKeys.add(acc.getProviderKey());
		}
		return new ArrayList<String>(providerKeys);
	}
	
	// TODO: Added to fix issue of null bean object
	public void setProviders(List<String> providers) {
		this.providers = providers;
	}
	
	public static void addLinkedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}

	/************************************************************************************************
	 * Other DB queries
	 ************************************************************************************************/
	
    public static User findByUserName(String userName)
    {
        return find.where()
                   .eq("username",
                       userName)
                   .findUnique();
    }

	public static List<User> findByName(String name)
	{
		return find.where()
				.eq("name",
						name)
				.findList();
	}


	public static User findByUUID(UUID uuid) {
		return find.where().eq("uuid", uuid).findUnique();
	}


	public void setSessionLanguage() {
		final String userLangCode = this.getLanguage();
   		final Lang lang = Lang.preferred(Context.current().request().acceptLanguages());
   		final String langCode = userLangCode !=null ? userLangCode : lang.code();   
   		Context.current().changeLang(langCode);
	}
}
