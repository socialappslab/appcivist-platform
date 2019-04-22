package enums;

public enum NotificationEventName {
	NEW_CAMPAIGN, NEW_WORKING_GROUP, NEW_VOTING_BALLOT, NEW_MILESTONE, 
	NEW_CONTRIBUTION_IDEA, NEW_CONTRIBUTION_PROPOSAL, NEW_CONTRIBUTION_DISCUSSION, 
	NEW_CONTRIBUTION_COMMENT, NEW_CONTRIBUTION_NOTE, NEW_CONTRIBUTION_FORUM_POST, NEW_CONTRIBUTION_FEEDBACK,
	NEW_CONTRIBUTION_ISSUE,
	// TODO: add ISSUE, ACTION_ITEM, ASSESSMENT:

	UPDATED_ASSEMBLY,UPDATED_CAMPAIGN, UPDATED_WORKING_GROUP, UPDATED_VOTING_BALLOT, UPDATED_MILESTONE,
	UPDATED_CONTRIBUTION_IDEA, UPDATED_CONTRIBUTION_PROPOSAL, UPDATED_CONTRIBUTION_DISCUSSION, 
	UPDATED_CONTRIBUTION_COMMENT, UPDATED_CONTRIBUTION_NOTE, UPDATED_CONTRIBUTION_FORUM_POST, 
	UPDATED_CONTRIBUTION_FEEDBACK, UPDATED_CONTRIBUTION_HISTORY,

	//peerdoc related
	UPDATED_CONTRIBUTION_COMMENT_RESOLVE,
	UPDATED_CONTRIBUTION_COMMENT_UNRESOLVE,
	//


	MILESTONE_PASSED, MILESTONE_UPCOMING,MILESTONE_UPCOMING_IN_A_WEEK, MILESTONE_UPCOMING_IN_A_DAY,

	BALLOT_UPCOMING_IN_A_DAY, BALLOT_UPCOMING_IN_A_WEEK,BALLOT_UPCOMING_IN_A_MONTH,
	BALLOT_ENDING_IN_A_DAY, BALLOT_ENDING_IN_A_WEEK,BALLOT_ENDING_IN_A_MONTH,

	//Configs
	UPDATED_ASSEMBLY_CONFIGS,UPDATED_CAMPAIGN_CONFIGS,UPDATED_WORKING_GROUP_CONFIGS,

	MEMBER_JOINED,DELETED_CONTRIBUTION,MODERATED_CONTRIBUTION,NEW_CONTRIBUTION_FEEDBACK_FLAG,
	 NEWSLETTER, NEW_CONTRIBUTION_FORK, NEW_CONTRIBUTION_MERGE, NEW_COORDINATOR_SIGNAL;

	public static boolean contains(String test) {

		for (NotificationEventName c : NotificationEventName.values()) {
			if (c.name().equals(test)) {
				return true;
			}
		}

		return false;
	}
}

