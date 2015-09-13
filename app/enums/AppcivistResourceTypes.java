package enums;

public enum AppcivistResourceTypes {
	CONTRIBUTION_IDEA,    	// Contributions are resources that are introduced in the system by single users
	CONTRIBUTION_QUESTION,  
	CONTRIBUTION_COMMENT, 
	CONTRIBUTION_ISSUE, 
	CONTRIBUTION_PROPOSAL, 				// Proposals are created and edited by Working Groups
	CONTRIBUTION_DISCUSSION, 			// Discussions are created and edited by Assemblies or Working Groups
	ELECTION, 				// Discussions are created and edited by Assemblies 
	BALLOT, 				// Ballots are created by Assemblies 
	VOTE, 					// Votes are created by single Users
	PROPOSAL_TEMPLATE,		// Templates are created and edited by Assemblies
	EXTERNAL,				// External resources can be anything that has a URL
	ASSEMBLY, 
	CAMPAIGN,
	CAMPAIGN_PHASE,
	WORKING_GROUP,
	
	PICTURE, 
	VIDEO, 
	PAD,
	TEXT, 
	WEBPAGE, 
	FILE
	
}
