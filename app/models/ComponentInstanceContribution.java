package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import enums.CampaignPhaseContributionConnectionTypes;

@Entity
public class ComponentInstanceContribution extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long campaignPhaseContributionId;
	private CampaignPhaseContributionConnectionTypes type;
	private User creator;
	
	@ManyToOne(cascade=CascadeType.ALL)
	private Contribution contribution;

	@ManyToOne(cascade=CascadeType.ALL)
	private ComponentInstance phase;
	
	// Who is the group that moved or copied this contribution to its Campaign Phase
	@OneToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "phase_contribution_group", 
		joinColumns = 
			@JoinColumn(name = "group_id", referencedColumnName = "group_id"), 
		inverseJoinColumns = 
			@JoinColumn(name = "campaign_phase_contribution_id", referencedColumnName = "campaign_phase_contribution_id")
	)
	private WorkingGroup ownerGroup;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, ComponentInstanceContribution> find = new Finder<>(ComponentInstanceContribution.class);

	public ComponentInstanceContribution(CampaignPhaseContributionConnectionTypes type,
			Contribution source, ComponentInstance target) {
		super();
		this.type = type;
		this.contribution = source;
		this.phase = target;
	}

	public ComponentInstanceContribution() {
		super();
	}

	/*
	 * Getters and Setters
	 */
	public Long getCampaignPhaseContributionId() {
		return campaignPhaseContributionId;
	}

	public void setCampaignPhaseContributionId(Long campaignPhaseContributionId) {
		this.campaignPhaseContributionId = campaignPhaseContributionId;
	}

	public CampaignPhaseContributionConnectionTypes getType() {
		return type;
	}

	public void setType(CampaignPhaseContributionConnectionTypes type) {
		this.type = type;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Contribution getContribution() {
		return contribution;
	}

	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}

	public ComponentInstance getPhase() {
		return phase;
	}

	public void setPhase(ComponentInstance phase) {
		this.phase = phase;
	}


	/*
	 * Basic Data operations
	 */

	public WorkingGroup getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(WorkingGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	public static ComponentInstanceContribution read(Long id) {
		return find.ref(id);
	}

	public static List<ComponentInstanceContribution> findAll() {
		return find.all();
	}
	
	public static ComponentInstanceContribution create(ComponentInstanceContribution object) {
		object.save();
		object.refresh();
		return object;
	}

	public static ComponentInstanceContribution createObject(ComponentInstanceContribution object) {
		object.save();
		return object;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}
}
