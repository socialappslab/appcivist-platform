package models.transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * Transfer model to receive membership creation objects
 * 
 * @author cdparra
 *
 */
public class MembershipCollectionTransfer {

	/**
	 * The id of the user to be added to the group or assembly
	 */
	private List<MembershipTransfer> memberships = new ArrayList<MembershipTransfer>();

	public MembershipCollectionTransfer() {
		super();
	}

	public List<MembershipTransfer> getMemberships() {
		return memberships;
	}

	public void setMemberships(List<MembershipTransfer> memberships) {
		this.memberships = memberships;
	}
}
