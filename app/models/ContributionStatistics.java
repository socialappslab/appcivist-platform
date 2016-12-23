package models;

import io.swagger.annotations.ApiModel;


@ApiModel(value="ContributionStatistics", description="Summarizes basic statistics on a contribution")
public class ContributionStatistics extends AppCivistBaseModel {

	private Long ups;
	private Long downs;
	private Long favs;
	private Long flags;
	private Long points;
	private Long contributionId;
	private Integer averageBenefit;
	private Integer averageNeed;
	private Integer averageFeasibility;
	private Integer eligibilityTrue;
	private Integer eligibilityFalse;


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

	public Integer getAverageBenefit() {
		this.averageBenefit = ContributionFeedback.getAverageBenefitForContribution(this.contributionId);
		return this.averageBenefit;
	}

	public Integer getAverageNeed() {
		this.averageNeed = ContributionFeedback.getAverageNeedForContribution(this.contributionId);
		return this.averageNeed;
	}

	public Integer getAverageFeasibility() {
		this.averageFeasibility = ContributionFeedback.getAverageFeasibilityForContribution(this.contributionId);
		return this.averageFeasibility;
	}

	public Integer getEligibilityTrue() {
		this.eligibilityTrue = ContributionFeedback.getElegibilityCountForContribution(this.contributionId, true);
		return this.eligibilityTrue;
	}

	public Integer getEligibilityFalse() {
		this.eligibilityFalse = ContributionFeedback.getElegibilityCountForContribution(this.contributionId, false);
		return this.eligibilityFalse;
	}
}
