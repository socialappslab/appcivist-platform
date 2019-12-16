package models;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModel;
import models.misc.Views;


@ApiModel(value="ContributionStatistics", description="Summarizes basic statistics on a contribution")
public class ContributionStatistics extends AppCivistBaseModel {

	public static final String STATISTICS_GROUP = "STATISTICS_GROUP";
	public static final String STATISTICS_CONTRIBUTION = "STATISTICS_CONTRIBUTION";

	@JsonView(Views.Public.class)
	private Long ups;
	@JsonView(Views.Public.class)
	private Long downs;
	@JsonView(Views.Public.class)
	private Long favs;
	@JsonView(Views.Public.class)
	private Long flags;
	@JsonView(Views.Public.class)
	private Long points;
	private Long contributionId;
	private Long groupId;
	@JsonView(Views.Public.class)
	private Double averageBenefit;
	@JsonView(Views.Public.class)
	private Double averageNeed;
	@JsonView(Views.Public.class)
	private Double averageFeasibility;
	@JsonView(Views.Public.class)
	private Integer eligibilityTrue;
	@JsonView(Views.Public.class)
	private Integer eligibilityFalse;
	@JsonView(Views.Public.class)
	private String type;


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
		this.type = STATISTICS_CONTRIBUTION;
	}

	public ContributionStatistics(Long groupId, Long contributionId) {
		super();
		this.contributionId = contributionId;
		this.groupId = groupId;
		this.type = STATISTICS_GROUP;

	}

	/*
	 * Getters and Setters
	 */

	public Long getUps() {
		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.ups = ContributionFeedback.getUpsForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.ups = ContributionFeedback.getUpsForGroup(this.groupId, this.contributionId);
		}

		return this.ups;
	}

	public Long getDowns() {

		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.downs = ContributionFeedback.getDownsForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.downs = ContributionFeedback.getDownsForGroup(this.groupId, this.contributionId);
		}
		return this.downs;
	}

	public Long getFavs() {
		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.favs = ContributionFeedback.getFavsForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.favs = ContributionFeedback.getFavsForGroup(this.groupId, this.contributionId);
		}
		return this.favs;
	}

	public Long getFlags() {

		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.flags = ContributionFeedback.getFlagsForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.flags = ContributionFeedback.getFlagsForGroup(this.groupId, this.contributionId);
		}

		return this.flags;
	}

	public Long getPoints() {

		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.points = ContributionFeedback.getPointsForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.points = ContributionFeedback.getPointsForGroup(this.groupId, this.contributionId);
		}
		return this.points;
	}

	public Double getAverageBenefit() {

		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.averageBenefit = ContributionFeedback.getAverageBenefitForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.averageBenefit = ContributionFeedback.getAverageBenefitForGroup(this.groupId, this.contributionId);
		}
		return this.averageBenefit;
	}

	public Double getAverageNeed() {

		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.averageNeed = ContributionFeedback.getAverageNeedForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.averageNeed = ContributionFeedback.getAverageNeedForGroup(this.groupId, this.contributionId);
		}

		return this.averageNeed;
	}

	public Double getAverageFeasibility() {

		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.averageFeasibility = ContributionFeedback.getAverageFeasibilityForContribution(this.contributionId);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.averageFeasibility = ContributionFeedback.getAverageFeasibilityForGroup(this.groupId, this.contributionId);
		}
		return this.averageFeasibility;
	}

	public Integer getEligibilityTrue() {

		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.eligibilityTrue = ContributionFeedback.getElegibilityCountForContribution(this.contributionId, true);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.eligibilityTrue = ContributionFeedback.getElegibilityCountForGroup(this.groupId, this.contributionId, true);
		}
		return this.eligibilityTrue;
	}

	public Integer getEligibilityFalse() {


		if(type.equals(STATISTICS_CONTRIBUTION)){
			this.eligibilityFalse = ContributionFeedback.getElegibilityCountForContribution(this.contributionId, false);
		}
		if(type.equals(STATISTICS_GROUP)){
			this.eligibilityFalse = ContributionFeedback.getElegibilityCountForGroup(this.groupId, this.contributionId, false);
		}
		return this.eligibilityFalse;
	}
}
