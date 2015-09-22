//package models;
//
//import javax.persistence.AttributeOverride;
//import javax.persistence.CascadeType;
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.ManyToMany;
//import javax.persistence.OneToOne;
//
//@Entity
////@DiscriminatorValue("PROPOSAL")
////@AttributeOverride(name="type", column=@Column(name="type"))
//public class ContributionProposal extends Contribution {
//	
//	@OneToOne(cascade=CascadeType.ALL)
//	private ResourcePad proposalPad;
//
//	@OneToOne(cascade=CascadeType.ALL)
//	private ResourcePad proposalTemplate;
//	
//	@ManyToMany
//	private WorkingGroup groupAuthor;
//	
////	@ManyToMany
////	private List<Contribution> inspirations = new ArrayList<Contribution>();
//
//	public ResourcePad getProposalPad() {
//		return proposalPad;
//	}
//
//	public void setProposalPad(ResourcePad proposalPad) {
//		this.proposalPad = proposalPad;
//	}
//
//	public ResourcePad getProposalTemplate() {
//		return proposalTemplate;
//	}
//
//	public void setProposalTemplate(ResourcePad proposalTemplate) {
//		this.proposalTemplate = proposalTemplate;
//	}
//
//	public WorkingGroup getGroupAuthor() {
//		return groupAuthor;
//	}
//
//	public void setGroupAuthor(WorkingGroup groupAuthor) {
//		this.groupAuthor = groupAuthor;
//	}
//	
//}
