package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalDataConfigKeys {

	public static final Map<String, String> CONFIG_DEFAULTS= new HashMap<String, String>();
	public static final  List<GlobalDataConfig> configList = new ArrayList<>();

	public static final String INSTANCE = "INSTANCE";
	public static final String GENERAL = "GENERAL";
	public static final String ASSEMBLY = "ASSEMBLY";
	public static final String CAMPAIGN = "CAMPAIGN";
	public static final String COMPONENT = "COMPONENT";
	public static final String WORKING_GROUP = "WORKING GROUP";

	// AppCivist Instance Configuration Keys	
	public static final String APPCIVIST_INSTANCE_API_KEY = "appcivist.instance.api-key"; //:** a generated secret code that can be use by apps to connect to the API and access information of //assemblies, campaigns, working groups and contributions on this instance. 
	public static final String APPCIVIST_INSTANCE_ASSEMBLY_NETWORK = "appcivist.instance.assembly-network"; 	//:** if TRUE, it allows COORDINATORS of the principal assembly to create new and manage other assemblies //under the principal one. 
	public static final String APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_CAMPAIGN_LIMIT = "appcivist.instance.assembly-network-campaign-limit"; 		//:** number of ongoing Campaign (per assembly) that can be created in the network.
	public static final String APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_LIMIT  = "appcivist.instance.assembly-network-limit"; //:** number of Assemblies that can be created in the network.
	public static final String APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_WORKING_GROUP_LIMIT = "appcivist.instance.assembly-network-working-group-limit";	//:** number of Working Groups (per assembly and campaign) that can be created in the network.
	public static final String APPCIVIST_INSTANCE_ETHERPAD_API_KEY = "appcivist.instance.etherpad-api-key"; 	//:** (only for COORDINATORS) configures the Etherpad API key in case the coordinators decide to use //their own etherpad server instance
	public static final String APPCIVIST_INSTANCE_ETHERPAD_BASE_URL = "appcivist.instance.etherpad-base-url"; 	//:** Base URL of the Etherpad server in use by this instance
	public static final String APPCIVIST_INSTANCE_INSTANCE_DOMAIN = "appcivist.instance.domain"; 	//:** domain registered to the instance (when Signing in, the server will determine the principal //assembly associated to the user by reading the domain from where the request is coming from)
	public static final String APPCIVIST_INSTANCE_INSTANCE_THEME = "appcivist.instance.theme";	//:** a URL that points to a CSS files that will overwrite the color scheme of the site for this assembly

	// AppCivist General Configuration Keys, applicable to any resource space
	public static final String APPCIVIST_REQUIRE_GROUP_AUTHORSHIP = "appcivist.campaign.require-group-authorship"; // Default is FALSE, if TRUE, proposals must be related to a WG on creation
	public static final String APPCIVIST_CREATE_GROUP_ON_NEW_PROPOSALS = "appcivist.campaign.create-group-new-proposals"; // Default is FALSE, if TRUE, proposals WG are automatically created if the proposal does not have one

	// Assembly Level Configuration Keys
	public static final String APPCIVIST_ASSEMBLY_DISABLE_NEW_MEMBERSHIPS = "appcivist.assembly.disable-new-memberships"; // only coordinators can add new members to the assembly, by uploading users for them.   
	public static final String APPCIVIST_ASSEMBLY_ENABLE_FORUM = "appcivist.assembly.enable-forum"; // if TRUE, an assembly page will be the home page after signing in (this assembly page has not been designed yet)
	public static final String APPCIVIST_ASSEMBLY_ENABLE_MODERATOR_ROLE = "appcivist.assembly.enable-moderator-role"; // if TRUE, members can also have the role MODERATOR, which allows a member to delete content (comments, proposals, ideas, etc.)
	public static final String APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM = "appcivist.assembly.has-registration-form"; // if TRUE, to join the assembly, users must fill in a registration form
	public static final String APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM_ID = "appcivist.assembly.has-registration-form-id"; // an ID that represents an internal registration form use 
	public static final String APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM_URL = "appcivist.assembly.has-registration-form-url"; // a URL to an external registration form
	public static final String APPCIVIST_ASSEMBLY_MEMBERSHIP_INVITATION_BY_MEMBERS = "appcivist.assembly.membership-invitation-by-members"; // if TRUE, regular members can invite other users to join, otherwise only COORDINATORS have this right
	public static final String APPCIVIST_ASSEMBLY_MEMBERSHIP_TYPE = "appcivist.assembly.membership-type"; // REGISTRATION (other users ask to join), INVITATION (members invite other users) or INVITATION_AND_REGISTRATION. 
	public static final String APPCIVIST_ASSEMBLY_ENABLE_SOCIAL_IDEATION = "appcivist.assembly.enable-social-ideation"; // FALSE DEFAULT
	public static final String APPCIVIST_ASSEMBLY_AUTO_MEMBERSHIP_WORKING_GROUPS = "appcivist.assembly.auto-membership-working-groups"; // Name of groups separated by comma
	public static final String APPCIVIST_ASSEMBLY_AUTO_MEMBERSHIP_WORKING_GROUPS_ALL = "appcivist.assembly.auto-membership-working-groups-all"; // Default FALSE
	public static final String APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_SERVER = "appcivist.assembly.authentication.ldap.server";
	public static final String APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_PORT = "appcivist.assembly.authentication.ldap.port";
	public static final String APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_DN = "appcivist.assembly.authentication.ldap.dn";
	public static final String APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_DN = "appcivist.assembly.authentication.ldap.admin.dn";
	public static final String APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_PASS = "appcivist.assembly.authentication.ldap.admin.pass";
	public static final String APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_MAIL = "appcivist.assembly.authentication.ldap.admin.mail";
	public static final String APPCIVIST_ASSEMBLY_AUTHENTICATION_FORGOT_URL = "appcivist.assembly.authentication.forgot-url";
	public static final String APPCIVIST_ASSEMBLY_AUTHENTICATION_SINGUP_URL = "appcivist.assembly.authentication.signup-url";
	public static final String APPCIVIST_ASSEMBLY_AUTHENTICATION_REGISTRATION_TITLE = "appcivist.assembly.authentication.registration-title";
	public static final String APPCIVIST_ASSEMBLY_AUTHENTICATION_USERNAME_PLACEHOLDER = "appcivist.assembly.authentication.username-placeholder";
	public static final String APPCIVIST_ASSEMBLY_AUTHENTICATION_PASSWORD_PLACEHOLDER = "appcivist.assembly.authentication.password-placeholder";
	public static final String APPCIVIST_ASSEMBLY_AUTHENTICATION_SINGUP_TITLE = "appcivist.assembly.authentication.signup-title";




	// Campaign Level Configuration Keys
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPE_PRINCIPAL = "appcivist.campaign.contribution-type-principal"; // what type of contribution is the displayed at the center of the page if a timeline with components is disabled (e.g., these campaigns is centered around PROPOSALS or IDEAS)    
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPES = "appcivist.campaign.contribution-types"; // and array that contains the list of contribution TYPES collected in this campaign (IDEA, PROPOSAL, NOTE, etc.)  
	public static final String APPCIVIST_CAMPAIGN_DISABLE_DISCUSSIONS = "appcivist.campaign.disable-discussions"; // if TRUE, the comment sections in the campaign dashboard is disabled 
	public static final String APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD = "appcivist.campaign.disable-etherpad"; // if TRUE, proposals do not have an etherpad extended text attached, only a description field. 
	public static final String APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK = "appcivist.campaign.disable-extended-feedback"; // if TRUE, extended feedback is disabled 
	public static final String APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK_PUBLIC = "appcivist.campaign.disable-extended-feedback-public"; // if TRUE, extended feedback is disabled in the public site
	public static final String APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_NEW_CONTRIBUTIONS  = "appcivist.campaign.disable-new-contributions"; // if TRUE, contributions can only be imported by COORDINATORS, members will not be able to create ideas or proposals
	public static final String APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING = "appcivist.campaign.disable-informal-voting"; // if TRUE, up and down votes are disabled
	public static final String APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING_PUBLIC = "appcivist.campaign.disable-informal-voting-public"; // if TRUE, up and down votes are disabled in the public site
	public static final String APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_DISCUSSIONS = "appcivist.campaign.disable-public-discussions"; // if TRUE, the comment sections is hidden in public site
	public static final String APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_SITE = "appcivist.campaign.disable-public-site"; // if TRUE, the campaign does not have a public site
	public static final String APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD = "appcivist.campaign.extended-feedback-password"; // if TRUE, extended feedback is disabled 
	public static final String APPCIVIST_CAMPAIGN_HIDE_TIMELINE = "appcivist.campaign.hide-timeline"; // if TRUappcivist.assembly.disable-new-membershipsE, the timeline of the campaign is hidden and the campaign has only one stage
	public static final String APPCIVIST_CAMPAIGN_SHOW_ASSEMBLY_LOGO = "appcivist.campaign.show-assembly-logo"; // if TRUE, show the assembly logo in the campaign dashboard
	public static final String APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS = "appcivist.campaign.proposal-default-status"; // NEW or PUBLISHED
	public static final String APPCIVIST_CAMPAIGN_FEEDBACK_HIDDEN_FIELDS = "appcivist.campaign.feedback.hidden-fields"; // EMPTY DEFAULT
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_HIDDEN_FIELDS = "appcivist.campaign.contribution.hidden-fields"; // EMPTY DEFAULT
	public static final String APPCIVIST_CAMPAIGN_ENABLE_IDEAS_DURING_PROPOSALS = "appcivist.campaign.enable-ideas-during-proposals"; // FALSE DEFAULT, if TRUE, ideas can be also added during a PROPOSALS phase
	public static final String APPCIVIST_CAMPAIGN_OPEN_IDEA_SECTION_DEFAULT= "appcivist.campaign.open-idea-section-default"; // FALSE DEFAULT
	public static final String APPCIVIST_CAMPAIGN_ALLOW_EMERGENT_THEMES = "appcivsit.campaign.allow-emergent-themes"; // TRUE DEFAULT
	public static final String APPCIVIST_CAMPAIGN_ALLOW_ANONYMOUS_IDEAS = "appcivist.campaign.allow-anonymous-ideas"; // FALSE DEFAULT
	public static final String APPCIVIST_CAMPAIGN_NEWSLETTER_FRECUENCY = "appcivist.campaign.newsletter-frecuency";
	public static final String APPCIVIST_CAMPAIGN_FORCE_COLLABORATIVE_EDITOR = "appcivist.force-collaborative-editor"; // PEERDOC or ETHERPAD, will force propsoal creation process to already create the proposal document in the selected service
	public static final String APPCIVIST_CAMPAIGN_DISABLE_PROPOSAL_DISCUSSIONS = "appcivist.campaign.disable-proposal-discussions"; // if TRUE, the comment sections in the proposal page is disabled
	public static final String APPCIVIST_CAMPAIGN_FAQ_URL = "appcivist.campaign.faq-url";
	public static final String APPCIVIST_CAMPAIGN_THEMES_NUMBER_LIMIT = "appcivist.campaign.themes-number-limit";
	public static final String APPCIVIST_CAMPAIGN_THEMES_EXTENDED_DESCRIPTION_URL = "appcivist.campaign.themes.extended-description-url";
	public static final String APPCIVIST_CAMPAIGN_RESEARCH_CONSENT_TEXT = "appcivist.campaign.research-consent-text";
	public static final String APPCIVIST_CAMPAIGN_RESEARCH_CONSENT_TEXT_APPROVE = "appcivist.campaign.research-consent-text-approve";
	public static final String APPCIVIST_CAMPAIGN_RESEARCH_CONSENT_TEXT_REJECT = "appcivist.campaign.research-consent-text-reject";
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_DEFAULT_TITLE = "appcivist.campaign.contribution.default-title";
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_DEFAULT_DESCRIPTION = "appcivist.campaign.contribution.default-description";
	public static final String APPCIVIST_CAMPAIGN_AUTHORSHIP_INVITATION_EMAIL_TEMPLATE = "appcivist.campaign.authorship-invitation-email-template";
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_PUBLIC_DRAFT_STATUS_REQ = "appcivist.campaign.contribution.public_draft.status-requirements";
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_PUBLIC_STATUS_REQ = "appcivist.campaign.contribution.published.status-requirements";

	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_FORKED_PUBLIC_DRAFT_STATUS_REQ = "appcivist.campaign.contribution.forked_public_draft.status-requirements";
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_FORKED_PUBLISHED_STATUS_REQ = "appcivist.campaign.contribution.forked_published.status-requirements";
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_MERGED_PUBLIC_DRAFT_STATUS_REQ = "appcivist.campaign.contribution.merged_public_draft.status-requirements";
	public static final String APPCIVIST_CAMPAIGN_CONTRIBUTION_MERGED_PUBLISHED_STATUS_REQ = "appcivist.campaign.contribution.merged_public_draft.status-requirements";


	public static final String APPCIVIST_CAMPAIGN_ACCESSIBILITY_URL = "appcivist.campaign.accessibility.url"; //link to a URL explaining accessibility issues in connection with an campaign
	public static final String APPCIVIST_CAMPAIGN_KEYWORDS_LIMIT ="appcivist.campaign.keywords.limit";

	//Add the configuration to the default templates for creating campaigns
	
	// Component Level Configuration Keys
	public static final String APPCIVIST_COMPONENT_CONTRIBUTION_TYPE_PRINCIPAL = "appcivist.component.contribution-type-principal"; // what type of contribution is the displayed at the center of the page when this component in the timeline is current (e.g., in Idea Collection, the main contribution type will be IDEAS)
	
	// Working Group Configuration Keys
	public static final String APPCIVIST_WG_DISABLE_PUBLIC_SITE = "appcivist.wg.disable-public-site"; // if TRUE, the working group is not available in the public site
	public static final String APPCIVIST_WG_ENABLE_MODERATOR_ROLE = "appcivist.wg.enable-moderator-role"; // if TRUE, members can also have the role MODERATOR, which allows a member to delete content (comments, proposals, ideas, etc.)
	public static final String APPCIVIST_WG_HAS_REGISTRATION_FORM = "appcivist.wg.has-registration-form"; // if TRUE, to join the assembly, users must fill in a registration form
	public static final String APPCIVIST_WG_HAS_REGISTRATION_FORM_ID = "appcivist.wg.has-registration-form-id"; // an ID that represents an internal registration form use 
	public static final String APPCIVIST_WG_HAS_REGISTRATION_FORM_URL = "appcivist.wg.has-registration-form-url"; // a URL to an external registration form
	public static final String APPCIVIST_WG_MEMBERSHIP_INVITATION_BY_MEMBERS = "appcivist.wg.membership-invitation-by-members"; // if TRUE, regular members can invite other users to join, otherwise only COORDINATORS have this right
	public static final String APPCIVIST_WG_MEMBERSHIP_TYPE = "appcivist.wg.membership-type"; // REGISTRATION (other users ask to join), INVITATION (members invite other users) or INVITATION_AND_REGISTRATION. 
	public static final String APPCIVIST_WG_ALLOW_EMERGENT_THEMES = "appcivsit.wg.allow-emergent-themes"; // TRUE DEFAULT

	static {
		// AppCivist Instance Configuration Default Values		
		CONFIG_DEFAULTS.put(APPCIVIST_INSTANCE_ASSEMBLY_NETWORK, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_CAMPAIGN_LIMIT, "1");
		CONFIG_DEFAULTS.put(APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_LIMIT, "10");
		CONFIG_DEFAULTS.put(APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_WORKING_GROUP_LIMIT, "10");
		CONFIG_DEFAULTS.put(APPCIVIST_INSTANCE_ETHERPAD_API_KEY, "http://localhost:9001");
		CONFIG_DEFAULTS.put(APPCIVIST_INSTANCE_ETHERPAD_BASE_URL, "3dca6eb8e4f2b5ea5216a88e49d7a63c09da0d7c793d0734b708058e0a687a19");
		
		// Assembly Configuration Default Values
		CONFIG_DEFAULTS.put(APPCIVIST_ASSEMBLY_DISABLE_NEW_MEMBERSHIPS, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_ASSEMBLY_ENABLE_FORUM, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_ASSEMBLY_ENABLE_MODERATOR_ROLE, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_ASSEMBLY_MEMBERSHIP_INVITATION_BY_MEMBERS, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_ASSEMBLY_MEMBERSHIP_TYPE, "INVITATION_AND_REGISTRATION");
		CONFIG_DEFAULTS.put(APPCIVIST_ASSEMBLY_ENABLE_SOCIAL_IDEATION, "FALSE");
		
		// Campaign Configuration Default Values
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPE_PRINCIPAL, "PROPOSAL");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPES, "IDEA, PROPOSAL, COMMENT");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_DISCUSSIONS, "TRUE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK_PUBLIC, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_NEW_CONTRIBUTIONS , "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING_PUBLIC, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_DISCUSSIONS, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_SITE, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD, "a44@c1v1st");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_HIDE_TIMELINE, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_SHOW_ASSEMBLY_LOGO, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS, "NEW");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_FEEDBACK_HIDDEN_FIELDS, "");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_CONTRIBUTION_HIDDEN_FIELDS, "");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_ENABLE_IDEAS_DURING_PROPOSALS, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_OPEN_IDEA_SECTION_DEFAULT, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_ALLOW_EMERGENT_THEMES, "TRUE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_ALLOW_ANONYMOUS_IDEAS, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_THEMES_NUMBER_LIMIT, "0");
		
		// Component Level Configuration Default Values
		CONFIG_DEFAULTS.put(APPCIVIST_COMPONENT_CONTRIBUTION_TYPE_PRINCIPAL,"PROPOSAL"); 
		
		// Working Group Level Configuration Default Values
		CONFIG_DEFAULTS.put(APPCIVIST_WG_DISABLE_PUBLIC_SITE, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_WG_ENABLE_MODERATOR_ROLE, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_WG_HAS_REGISTRATION_FORM, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_WG_MEMBERSHIP_INVITATION_BY_MEMBERS, "FALSE");
		CONFIG_DEFAULTS.put(APPCIVIST_WG_MEMBERSHIP_TYPE, "INVITATION_AND_REGISTRATION");
		CONFIG_DEFAULTS.put(APPCIVIST_WG_ALLOW_EMERGENT_THEMES, "TRUE");

		CONFIG_DEFAULTS.put(APPCIVIST_CAMPAIGN_NEWSLETTER_FRECUENCY, "7");

	};

	public static class GlobalDataConfig {
		private String target;
		private String key;
		private String description;

		 GlobalDataConfig(String target, String key, String description) {
			this.target = target;
			this.key = key;
			this.description = description;
		}

		public String getTarget() {
			return target;
		}

		public void setTarget(String target) {
			this.target = target;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	static {

		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_API_KEY,
				"a generated secret code that can be use by apps to connect to the " +
						"API and access information of assemblies, campaigns, working groups and " +
						"contributions on this instance."));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_ASSEMBLY_NETWORK,
				"if TRUE, it allows COORDINATORS of the principal assembly to create new and manage other assemblies " +
						"under the principal one."));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_CAMPAIGN_LIMIT,
				"if TRUE, it allows COORDINATORS of the principal assembly to create new and manage other assemblies " +
						"under the principal one."));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_LIMIT,
				"number of Assemblies that can be created in the network."));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_ASSEMBLY_NETWORK_WORKING_GROUP_LIMIT,
				"number of Working Groups (per assembly and campaign) that can be created in the network."));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_ETHERPAD_API_KEY,
				"(only for COORDINATORS) configures the Etherpad API key in case the coordinators decide to use //their own etherpad server instance"));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_ETHERPAD_BASE_URL,
				"Base URL of the Etherpad server in use by this instance"));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_INSTANCE_DOMAIN,
				"domain registered to the instance (when Signing in, the server will determine the principal //assembly associated to the user by reading the domain from where the request is coming from)"))
		;
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_INSTANCE_THEME,
				"a URL that points to a CSS files that will overwrite the color scheme of the site for this assembly"));
		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_INSTANCE_THEME,
				"a URL that points to a CSS files that will overwrite the color scheme of the site for this assembly"));

		configList.add(new GlobalDataConfig(INSTANCE, APPCIVIST_INSTANCE_INSTANCE_THEME,
				"a URL that points to a CSS files that will overwrite the color scheme of the site for this assembly"));

		configList.add(new GlobalDataConfig(GENERAL, APPCIVIST_REQUIRE_GROUP_AUTHORSHIP,
				"Default is FALSE, if TRUE, proposals must be related to a WG on creation"));
		configList.add(new GlobalDataConfig(GENERAL, APPCIVIST_CREATE_GROUP_ON_NEW_PROPOSALS,
				"Default is FALSE, if TRUE, proposals WG are automatically created if the proposal does not have one"));

		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_DISABLE_NEW_MEMBERSHIPS,
				"only coordinators can add new members to the assembly, by uploading users for them. "));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_ENABLE_FORUM,
				"if TRUE, an assembly page will be the home page after signing in (this assembly page has not been designed yet)"));
		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_ENABLE_MODERATOR_ROLE,
				"if TRUE, members can also have the role MODERATOR, which allows a member to delete content (comments, proposals, ideas, etc.)"));
		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM,
				"if TRUE, to join the assembly, users must fill in a registration form"));
		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM_ID,
				"an ID that represents an internal registration form use "));

		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM_URL,
				"a URL to an external registration form"));

		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_MEMBERSHIP_INVITATION_BY_MEMBERS,
				"if TRUE, regular members can invite other users to join, otherwise only COORDINATORS have this right"));
		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_MEMBERSHIP_TYPE,
				"REGISTRATION (other users ask to join), INVITATION (members invite other users) or INVITATION_AND_REGISTRATION. "));
		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_ENABLE_SOCIAL_IDEATION,
				"FALSE DEFAULT"));
		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_AUTO_MEMBERSHIP_WORKING_GROUPS,
				"Name of groups separated by comma"));
		configList.add(new GlobalDataConfig(ASSEMBLY, APPCIVIST_ASSEMBLY_AUTO_MEMBERSHIP_WORKING_GROUPS_ALL,
				"Default FALSE"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_SERVER,
				"The url to the LDAP server where authentication will be made"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_PORT,
				"The port to the LDAP server where authentication will be made"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_DN,
				"The domain to the LDAP server where authentication will be made"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_DN,
				"The admin domain to the LDAP server for search users"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_PASS,
				"The admin password to the LDAP server for search users"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_LDAP_AUTHENTICATION_ADMIN_MAIL,
				"The admin mail to the LDAP server for search users"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_AUTHENTICATION_FORGOT_URL,
				"The url to go when a user forgot his password"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_AUTHENTICATION_SINGUP_URL,
				"The assembly url to go for sing up a new user"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_AUTHENTICATION_REGISTRATION_TITLE,
				"The assembly registration title to use"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_AUTHENTICATION_USERNAME_PLACEHOLDER,
				"The assembly username place holder to use"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_AUTHENTICATION_PASSWORD_PLACEHOLDER,
				"The assembly password place holder to use"));
		configList.add(new GlobalDataConfig(ASSEMBLY,  APPCIVIST_ASSEMBLY_AUTHENTICATION_SINGUP_TITLE,
				"The assembly sign up title to use"));


		configList.add(new GlobalDataConfig(COMPONENT,  APPCIVIST_COMPONENT_CONTRIBUTION_TYPE_PRINCIPAL,
				"what type of contribution is the displayed at the center of the page when this component in the timeline is current (e.g., in Idea Collection, the main contribution type will be IDEAS)"));

		configList.add(new GlobalDataConfig(WORKING_GROUP,  APPCIVIST_WG_DISABLE_PUBLIC_SITE,
				" if TRUE, the working group is not available in the public site"));

		configList.add(new GlobalDataConfig(WORKING_GROUP,  APPCIVIST_WG_ENABLE_MODERATOR_ROLE,
				"if TRUE, members can also have the role MODERATOR, which allows a member to delete content (comments, proposals, ideas, etc.)"));

		configList.add(new GlobalDataConfig(WORKING_GROUP,  APPCIVIST_WG_HAS_REGISTRATION_FORM,
				"if TRUE, to join the assembly, users must fill in a registration form"));

		configList.add(new GlobalDataConfig(WORKING_GROUP,  APPCIVIST_WG_HAS_REGISTRATION_FORM_ID,
				"an ID that represents an internal registration form use "));

		configList.add(new GlobalDataConfig(WORKING_GROUP,  APPCIVIST_WG_HAS_REGISTRATION_FORM_URL,
				"a URL to an external registration form"));
		configList.add(new GlobalDataConfig(WORKING_GROUP, APPCIVIST_WG_MEMBERSHIP_INVITATION_BY_MEMBERS,
				"if TRUE, regular members can invite other users to join, otherwise only COORDINATORS have this right"));
		configList.add(new GlobalDataConfig(WORKING_GROUP, APPCIVIST_WG_MEMBERSHIP_TYPE,
				"REGISTRATION (other users ask to join), INVITATION (members invite other users) or INVITATION_AND_REGISTRATION. "));
		configList.add(new GlobalDataConfig(WORKING_GROUP, APPCIVIST_WG_ALLOW_EMERGENT_THEMES,
				"TRUE DEFAULT"));

		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPE_PRINCIPAL,
				"what type of contribution is the displayed at the center of the page if a timeline with components is disabled (e.g., these campaigns is centered around PROPOSALS or IDEAS)    "));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPES,
				"and array that contains the list of contribution TYPES collected in this campaign (IDEA, PROPOSAL, NOTE, etc.)  "));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_DISCUSSIONS,
				"if TRUE, the comment sections in the campaign dashboard is disabled "));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD,
				"if TRUE, proposals do not have an etherpad extended text attached, only a description field. "));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK,
				"if TRUE, extended feedback is disabled "));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK_PUBLIC,
				"if TRUE, extended feedback is disabled in the public site"));
		configList.add(new GlobalDataConfig(CAMPAIGN,  APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_NEW_CONTRIBUTIONS,
				"if TRUE, contributions can only be imported by COORDINATORS, members will not be able to create ideas or proposals"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING,
				"if TRUE, up and down votes are disabled"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING_PUBLIC,
				"if TRUE, up and down votes are disabled in the public site"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_DISCUSSIONS,
				"if TRUE, the comment sections is hidden in public site"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_SITE,
				"if TRUE, the campaign does not have a public site"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD,
				"if TRUE, extended feedback is disabled "));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_HIDE_TIMELINE,
				"if TRUE, the timeline of the campaign is hidden and the campaign has only one stage"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_SHOW_ASSEMBLY_LOGO,
				"if TRUE, show the assembly logo in the campaign dashboard"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS,
				"NEW or PUBLISHED"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_FEEDBACK_HIDDEN_FIELDS,
				"EMPTY DEFAULT"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_ENABLE_IDEAS_DURING_PROPOSALS,
				"FALSE DEFAULT, if TRUE, ideas can be also added during a PROPOSALS phase"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_OPEN_IDEA_SECTION_DEFAULT,
				"FALSE DEFAULT"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_ALLOW_EMERGENT_THEMES,
				"TRUE DEFAULT"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_ALLOW_ANONYMOUS_IDEAS,
				"FALSE DEFAULT"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_NEWSLETTER_FRECUENCY,
				"Frecuency in days for the newsletter to be send"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_FORCE_COLLABORATIVE_EDITOR,
				"PEERDOC or ETHERPAD, will force propsoal creation process to already create the proposal document in the selected service"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_DISABLE_PROPOSAL_DISCUSSIONS,
				"if TRUE, the comment sections in the proposal page is disabled"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_FAQ_URL,
				"URL for the FAQ Page of the campaign"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_THEMES_NUMBER_LIMIT,
				"Limit of the number of themes in the campaign"));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_THEMES_EXTENDED_DESCRIPTION_URL,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_RESEARCH_CONSENT_TEXT,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_RESEARCH_CONSENT_TEXT_APPROVE,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_RESEARCH_CONSENT_TEXT_REJECT,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_CONTRIBUTION_DEFAULT_TITLE,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_CONTRIBUTION_DEFAULT_DESCRIPTION,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_AUTHORSHIP_INVITATION_EMAIL_TEMPLATE,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_CONTRIBUTION_PUBLIC_DRAFT_STATUS_REQ,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_CONTRIBUTION_PUBLIC_STATUS_REQ,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, APPCIVIST_CAMPAIGN_ACCESSIBILITY_URL,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN,  APPCIVIST_CAMPAIGN_KEYWORDS_LIMIT,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, 	APPCIVIST_CAMPAIGN_CONTRIBUTION_FORKED_PUBLIC_DRAFT_STATUS_REQ,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, 	APPCIVIST_CAMPAIGN_CONTRIBUTION_FORKED_PUBLISHED_STATUS_REQ,
				""));
		configList.add(new GlobalDataConfig(CAMPAIGN, 	APPCIVIST_CAMPAIGN_CONTRIBUTION_MERGED_PUBLIC_DRAFT_STATUS_REQ,
				""));
	}

}
