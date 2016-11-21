package models;

import enums.ContributionFeedbackTypes;
import io.swagger.annotations.ApiModel;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="ContributionFeedback", description="Feedback associated to a contribution")
public class ContributionFeedback extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long id;
	private Boolean up = false;
	private Boolean down = false;
	private Boolean fav = false;
	private Boolean flag = false;
	// TODO: Add Priority Matrix score fields: benefit, need, feasibility, elegibility, text feedback
	private Long contributionId;
	private Long userId;
	// TODO: Add a way of making feedback private or limited to a working group
	// TODO: Add a way of making feedback 
	private ContributionFeedbackTypes type;

	public static Finder<Long, ContributionFeedback> find = new Finder<>(ContributionFeedback.class);

	public ContributionFeedback() {
		super();
	}

	/*
	 * Getters and Setters
	 */

	public Long getId() {
		return id;
	}

	public void setId(Long contributionStatisticsId) {
		this.id = contributionStatisticsId;
	}

	public Boolean getUp() {
		return up;
	}

	public void setUp(Boolean up) {
		this.up = up;
	}

	public Boolean getDown() {
		return down;
	}

	public void setDown(Boolean down) {
		this.down = down;
	}

	public Boolean getFav() {
		return fav;
	}

	public void setFav(Boolean fav) {
		this.fav = fav;
	}

	public Boolean getFlag() {
		return flag;
	}

	public void setFlag(Boolean flag) {
		this.flag = flag;
	}

	public Long getContributionId() {
		return contributionId;
	}

	public void setContributionId(Long contribution) {
		this.contributionId = contribution;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long user) {
		this.userId = user;
	}


	public ContributionFeedbackTypes getType() {
		return type;
	}

	public void setType(ContributionFeedbackTypes type) {
		this.type = type;
	}

	/*
         * Basic Data operations
         */
	public static ContributionFeedback read(Long id) {
		return find.ref(id);
	}

	public static List<ContributionFeedback> findAll() {
		return find.all();
	}

	public static ContributionFeedback create(ContributionFeedback object) {
		object.save();
		object.refresh();
		return object;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public static long getUpsForContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("up", true).findRowCount();
	}
	
	public static long getDownsForContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("down", true).findRowCount();
	}
	
	public static long getFavsForContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("fav", true).findRowCount();
	}
	
	public static long getFlagsForContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("flag", true).findRowCount();
	}
	
	public static long getPointsForContribution(Long contributionId) {
		return getUpsForContribution(contributionId)-getDownsForContribution(contributionId);
	}

	public static ContributionFeedback findByContributionAndUserId(Long cid, Long userId) {
		return find.where().eq("contributionId", cid).eq("userId", userId).findUnique();
	}
}
