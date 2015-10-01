package models;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

@Entity
@DiscriminatorValue("PROPOSAL")
public class ContributionProposal extends Contribution {
	
	@OneToOne(cascade=CascadeType.ALL)
	private ResourcePad proposalPad;
	public ResourcePad getProposalPad() {
		return proposalPad;
	}

	public void setProposalPad(ResourcePad proposalPad) {
		this.proposalPad = proposalPad;
	}	
}
