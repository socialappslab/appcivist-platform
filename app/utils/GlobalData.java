package utils;

import enums.CampaignTemplatesEnum;

public class GlobalData {
	public static final String APPCIVIST_BASE_URL = "/api";
	public static final String APPCIVIST_ASSEMBLY_BASE_URL = APPCIVIST_BASE_URL+"/assembly";
	public static final String APPCIVIST_ASSEMBLY_DEFAULT_ICON = "/assets/images/justicia-140.png"; 
	public static final String APPCIVIST_ASSEMBLY_DEFAULT_COVER = "/assets/images/covers/2015_GoldenGateFromPointBonita-1920.JPG";
	public static final String APPCIVIST_WG_DEFAULT_COLOR = "#0D493B"; // AppCivist Dark Green
	/**
	 * Internationalization Messages Bindings and config keyes
	 */
	public static final String DEFAULT_LANGUAGE = "en-US";
	
	// Assemblies
	public static final String ASSEMBLY_CREATE_MSG_SUCCESS = "assemblies.creation.success";
	public static final String ASSEMBLY_CREATE_MSG_ERROR = "assemblies.creation.error";
    public static final String ASSEMBLY_BASE_PATH = "/assembly";

    // Working Groups
	public static final String GROUP_CREATE_MSG_SUCCESS = "groups.creation.success";
	public static final String GROUP_CREATE_MSG_ERROR = "groups.creation.error";
	public static final String GROUP_BASE_PATH = "/group";

	// Working Groups Membership
	public static final String MEMBERSHIP_INVITATION_CREATE_MSG_SUCCESS = "membership.invitation.creation.success";
	public static final String MEMBERSHIP_INVITATION_CREATE_MSG_ERROR = "membership.invitation.creation.error";
	public static final String MEMBERSHIP_INVITATION_CREATE_MSG_UNAUTHORIZED = "membership.invitation.creation.unauthorized";
	public static final String MEMBERSHIP_INVITATION_BASE_PATH = "/membership";
	
	// Role
	public static final String ROLE_CREATE_MSG_SUCCESS = "roles.creation.success";
	public static final String ROLE_CREATE_MSG_ERROR = "roles.creation.error";
	public static final String ROLE_BASE_PATH = "/role";

	// Config
	public static final String CONFIG_CREATE_MSG_SUCCESS = "configs.creation.success";
	public static final String CONFIG_CREATE_MSG_ERROR = "configs.creation.error";
	public static final String CONFIG_BASE_PATH = "/config";

	// Campaign
	public static final String CAMPAIGN_CREATE_MSG_SUCCESS = "campaign.creation.success";
	public static final String CAMPAIGN_CREATE_MSG_ERROR = "campaign.creation.error";
	public static final String CAMPAIGN_BASE_PATH = "/campaign";
	public static final CampaignTemplatesEnum DEFAULT_CAMPAIGN_TYPE = CampaignTemplatesEnum.PARTICIPATORY_BUDGETING;
	public static final String CONFIG_CAMPAIGN_INCLUDE_PUBLISHED_PROPOSAL = "campaign.include.all.published.proposals";
	public static final String CONFIG_CAMPAIGN_CONTRIBUTION_TYPES = "appcivist.campaign.contribution-types";

	// Campaign Components
	public static final String CAMPAIGN_PHASE_CREATE_MSG_SUCCESS = "campaign.phase.creation.success";
	public static final String CAMPAIGN_PHASE_CREATE_MSG_ERROR = "campaign.phase.creation.error";
	public static final String CAMPAIGN_PHASE_BASE_PATH = "/phase";
	public static final String CONFIG_COMPONENT_IDEAS_ENABLE_MULTIPLE_AUTHORS= "component.ideas.enable-multiple-authors";
	public static final String CONFIG_COMPONENT_IDEAS_ENABLE_ATTACHMENTS = "component.ideas.enable-attachments";
	public static final String CONFIG_COMPONENT_IDEAS_CONTRIBUTION_LIMIT = "component.ideas.contribution-limit";
	public static final String CONFIG_COMPONENT_PROPOSALS_DISABLE_COLLABORATIVE_EDITOR = "component.proposals.disable-collaborative-editor";
	public static final String CONFIG_COMPONENT_PROPOSALS_ENABLE_MULTIPLE_AUTHORS = "component.proposals.enable-multiple-authors";
	public static final String CONFIG_COMPONENT_DELIBERATION_ENABLE_TECHNICAL_ASSESMENT = "component.deliberation.enable-technical-assessment";
	public static final String CONFIG_COMPONENT_DELIBERATION_WHO_DELIBERATES = "component.deliberation.who-deliberates";
	public static final String CONFIG_COMPONENT_DELIBERATION_WHO_DELIBERATES_JURY = "component.deliberation.who-deliberates-jury";
	public static final String CONFIG_COMPONENT_DELIBERATION_WHO_DELIBERATES_JURY_PERCENTAGE = "component.deliberation.who-deliberates-jury-percentage";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM = "component.voting.system";
	public static final String CONFIG_COMPONENT_VOTING_BALLOT_PASSWORD = "component.voting.ballot.password";
	public static final String CONFIG_COMPONENT_VOTING_BALLOT_ENTITY_TYPE = "component.voting.ballot.entity.type";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_DISTRIBUTED_POINTS = "component.voting.system-distributed-points";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_BLOCK_THRESHOLD = "component.voting.system.plurality.block.threshold";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_TYPE = "component.voting.system.plurality.type";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_VOTES = "component.voting.system.plurality.votes";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_QUORUM = "component.voting.system.quorum";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_QUORUM_ENABLE = "component.voting.system.quorum.enable";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_RANGE_MAX_SCORE = "component.voting.system-range-max-score";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_RANGE_MIN_SCORE = "component.voting.system-range-min-score";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_RANKED_NUMBER_PROPOSALS = "component.voting.system-ranked-number-proposals";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_WINNERS = "component.voting.system.winners";
	public static final String CONFIG_COMPONENT_VOTING_SYSTEM_WINNERS_FIXED_NUMBER = "component.voting.system.winners.fixed.number";

	// Contribution
	public static final String CONTRIBUTION_CREATE_MSG_SUCCESS = "contribution.creation.success";
	public static final String CONTRIBUTION_CREATE_MSG_ERROR = "contribution.creation.error";
	public static final String CONTRIBUTION_UPDATE_MSG_ERROR = "contribution.update.error";
	public static final String CONTRIBUTION_THEME_CREATE_MSG_SUCCESS = "contribution.theme.creation.success";
	public static final String CONTRIBUTION_THEME_CREATE_MSG_ERROR = "contribution.theme.creation.error";
	public static final String CONTRIBUTION_BASE_PATH = "/contribution";

    // User Management Variables
	public static final String USER_ROLE = "USER";
	public static final String ADMIN_ROLE = "ADMIN";
	public static final String COORDINATOR_ROLE = "COORDINATOR";
	public static final String MEMBER_ROLE = "MEMBER";
	public static final String FOLLOWER_ROLE = "FOLLOWER";
	
	// External Services Constants
	public static final String GRAVATAR_BASE_URL = "http://www.gravatar.com/avatar/";
	public static final String ETHERPAD_BASE_URL = "http://localhost:9001/";

	//Notifications
	public static final String MISSING_RESOURCE_SPACE_TYPE = "resourceSpace.type";
	public static final String CONFIG_NOTIFICATION_DEFAULT_SERVER_BASE = "appcivist.services.notification.default.serverBaseUrl";
	public static final String CONFIG_NOTIFICATION_DEFAULT_API_KEY = "appcivist.services.notification.default.apiKey";
	public static final String MISSING_CONFIGURATION = "appcivist.configuration.missing";

	// General
	public static final String CONFIG_ETHERPAD_SERVER = "component.proposal-making.etherpad-server";
	public static final String CONFIG_ETHERPAD_API_KEY = "component.proposal-making.etherpad-api-key";
	public static final String CONFIG_APPCIVIST_ETHERPAD_SERVER = "appcivist.services.etherpad.default.serverBaseUrl";
	public static final String CONFIG_APPCIVIST_ETHERPAD_API_KEY = "appcivist.services.etherpad.default.apiKey";
	public static final String CONFIG_APPCIVIST_PRODUCTION_ETHERPAD_SERVER = "appcivist.services.etherpad.appcivist.serverBaseUrl";
	public static final String CONFIG_APPCIVIST_PRODUCTION_ETHERPAD_API_KEY = "appcivist.services.etherpad.appcivist.apiKey";
	public static final String CONFIG_APPCIVIST_TEST_ETHERPAD_SERVER = "appcivist.services.etherpad.appcivist-test.serverBaseUrl";
	public static final String CONFIG_APPCIVIST_TEST_ETHERPAD_API_KEY = "appcivist.services.etherpad.appcivist-test.apiKey";
	public static final String CONFIG_APPCIVIST_ETHERPAD= "appcivist.services.etherpad";
	public static final String CONFIG_USER_ACTIONS_LOGGING = "appcivist.logging";
	public static final String CONFIG_RECAPTCHA_SERVER_URL = "appcivist.services.recaptcha.serverURL";
	public static final String CONFIG_RECAPTCHA_SECRET = "appcivist.services.recaptcha.secret";
	public static final String CONFIG_URI_ENTITY_MANAGER = "appcivist.services.entityManager.serverURL";


	public static final String CONFIG_FORGOT_PASSWORD_URL_BASE = "appcivist.services.password.forgotBaseUrl";
	public static final String FORGOT_PASSWORD_DEFAULT_URL_BASE = "/user/password/reset/";

	public static final Integer DEFAULT_PAGE_SIZE = 16;

	public static final String GEOCODING_SERVICE = "appcivist.services.geocoding.service";
}
