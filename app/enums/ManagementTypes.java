package enums;

public enum ManagementTypes {
    OPEN, 			// any member can invite/accept members, 
    				// create working groups and move contributions 
    				// from one group to the other
    
    COORDINATED, 	// only coordinators can invite/accept members, create 
    				// working groups and move contributions from one group to 
    				// the other. Coordinators can further manage and configure 
    				// campaigns and manage the basic information of the Assembly 
    				// (its categories and info)

    DEMOCRATIC		//  role of members can be suggested by other members and 
    				// then subject to a vote
}
