package models;


public class ContributionStatistics extends AppCivistBaseModel {

	private Long ups;
	private Long downs;
	private Long favs;
	private Long flags;
	private Long points;
	private Long contributionId;

	public ContributionStatistics() {
		super();
		this.ups = new Long(0);
		this.downs = new Long(0);
		this.favs = new Long(0);
		this.flags = new Long(0);
	}

	public ContributionStatistics(Long contributionId) {
		super();
		this.contributionId = contributionId;
	}

	/*
	 * Getters and Setters
	 */

	public Long getUps() {
		this.ups = ContributionFeedback.getUpsForContribution(this.contributionId);
		return this.ups;
	}

	public Long getDowns() {
		this.downs = ContributionFeedback.getDownsForContribution(this.contributionId);
		return this.downs;
	}

	public Long getFavs() {
		this.favs = ContributionFeedback.getFavsForContribution(this.contributionId);
		return this.favs;
	}

	public Long getFlags() {
		this.flags = ContributionFeedback.getFlagsForContribution(this.contributionId);
		return this.flags;
	}

	public Long getPoints() {
		this.points = ContributionFeedback.getPointsForContribution(this.contributionId);
		return this.points;
	}
}
