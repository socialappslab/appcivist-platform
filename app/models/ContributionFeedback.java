package models;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import enums.ContributionFeedbackStatus;
import enums.ContributionFeedbackTypes;
import enums.ContributionStatus;
import io.swagger.annotations.ApiModel;

import javax.persistence.*;
import java.util.List;

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

	private Integer benefit;
	private Integer need;
	private Integer feasibility;
	private Boolean elegibility;
	@Column(name = "textual_feedback")
	private String textualFeedback;
	@Enumerated(EnumType.ORDINAL)
	private ContributionFeedbackTypes type;
	@Enumerated(EnumType.ORDINAL)
	private ContributionFeedbackStatus status;
	@Column(name = "working_group_id")
	private Long workingGroupId;
	@Column(name = "official_group_feedback")
	private Boolean officialGroupFeedback;
	private Boolean archived = false;

	public static Finder<Long, ContributionFeedback> find = new Finder<>(ContributionFeedback.class);

	public ContributionFeedback() {
		super();
	}

	/*
	 * Getters and Setters
	 */

	/**
	 * Finds all the archived feedbacks that are previous states of this feedback
	 * @return
	 */
	@JsonIgnore
	public List<ContributionFeedback> getContributionFeedbackHistory(){
		return find.where().eq("contributionId", this.contributionId).eq("workingGroupId", workingGroupId).
				eq("userId", this.userId).eq("status", this.status == null ? null : this.status.ordinal()).
				eq("type", this.type == null ? null : this.type.ordinal()).eq("archived", true).
				orderBy("creation").findList();
	}

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

	public Integer getBenefit() {
		return benefit;
	}

	public void setBenefit(Integer benefit) {
		this.benefit = benefit;
	}

	public Integer getNeed() {
		return need;
	}

	public void setNeed(Integer need) {
		this.need = need;
	}

	public Integer getFeasibility() {
		return feasibility;
	}

	public void setFeasibility(Integer feasibility) {
		this.feasibility = feasibility;
	}

	public Boolean getElegibility() {
		return elegibility;
	}

	public void setElegibility(Boolean elegibility) {
		this.elegibility = elegibility;
	}

	public String getTextualFeedback() {
		return textualFeedback;
	}

	public void setTextualFeedback(String textualFeedback) {
		this.textualFeedback = textualFeedback;
	}

	public ContributionFeedbackTypes getType() {
		return type;
	}

	public void setType(ContributionFeedbackTypes type) {
		this.type = type;
	}

	public ContributionFeedbackStatus getStatus() {
		return status;
	}

	public void setStatus(ContributionFeedbackStatus status) {
		this.status = status;
	}

	public Long getWorkingGroupId() {
		return workingGroupId;
	}

	public void setWorkingGroupId(Long workingGroupId) {
		this.workingGroupId = workingGroupId;
	}

	public Boolean getOfficialGroupFeedback() {
		return officialGroupFeedback;
	}

	public void setOfficialGroupFeedback(Boolean officialGroupFeedback) {
		this.officialGroupFeedback = officialGroupFeedback;
	}

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
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
		return find.where().eq("contributionId", contributionId).eq("archived", false).eq("up", true).findRowCount();
	}

	public static long getUpsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contributionId", contributionId).
				eq("archived", false).eq("up", true).
				eq("type", ContributionFeedbackTypes.WORKING_GROUP).findRowCount();
	}
	
	public static long getDownsForContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("archived", false).eq("down", true).findRowCount();
	}

	public static long getDownsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contributionId", contributionId).
				eq("archived", false).eq("down", true).
				eq("type", ContributionFeedbackTypes.WORKING_GROUP).findRowCount();
	}
	
	public static long getFavsForContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("archived", false).eq("fav", true).findRowCount();
	}

	public static long getFavsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contributionId", contributionId).
				eq("archived", false).eq("fav", true).
				eq("type", ContributionFeedbackTypes.WORKING_GROUP).findRowCount();
	}
	
	public static long getFlagsForContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("archived", false).eq("flag", true).findRowCount();
	}

	public static long getFlagsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contributionId", contributionId).
				eq("archived", false).eq("flag", true).
				eq("type", ContributionFeedbackTypes.WORKING_GROUP).findRowCount();
	}
	
	public static long getPointsForContribution(Long contributionId) {
		return getUpsForContribution(contributionId)-getDownsForContribution(contributionId);
	}

	public static long getPointsForGroup(Long workingGroupId, Long contributionId) {
		return getUpsForGroup(workingGroupId, contributionId)-getDownsForGroup(workingGroupId, contributionId);
	}

	public static ContributionFeedback findByContributionAndUserId(Long cid, Long userId) {
		return find.where().eq("contributionId", cid).eq("archived", false).eq("userId", userId).findUnique();
	}

	public static Integer getAverageBenefitForContribution(Long contributionId) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, sum(t0.benefit) / count(contribution_id) as benefit from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();
		List<ContributionFeedback> feedbacks =  where.eq("t0.contribution_id", contributionId).eq("t0.archived", false).findList();
		return feedbacks != null && !feedbacks.isEmpty() && feedbacks.get(0).getBenefit() != null ? feedbacks.get(0).getBenefit() : 0;
	}

	public static Integer getAverageBenefitForGroup(Long workingGroupId, Long contributionId) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, sum(t0.benefit) / count(contribution_id) as benefit from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();
		List<ContributionFeedback> feedbacks = where.eq("t0.working_group_id", workingGroupId).eq("t0.contribution_id", contributionId).eq("t0.archived", false).findList();
		return feedbacks != null && !feedbacks.isEmpty()  && feedbacks.get(0).getBenefit() != null ? feedbacks.get(0).getBenefit() : 0;
	}

	public static Integer getAverageNeedForContribution(Long contributionId) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, sum(t0.need) / count(contribution_id) as need from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();
		List<ContributionFeedback> feedbacks =  where.eq("t0.contribution_id", contributionId).eq("t0.archived", false).findList();
		return feedbacks != null && !feedbacks.isEmpty() && feedbacks.get(0).getNeed() != null ? feedbacks.get(0).getNeed() : 0;
	}

	public static Integer getAverageNeedForGroup(Long workingGroupId, Long contributionId) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, sum(t0.need) / count(contribution_id) as need from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();
		List<ContributionFeedback> feedbacks =  where.eq("t0.working_group_id", workingGroupId).eq("t0.contribution_id", contributionId).eq("t0.archived", false).findList();
		return feedbacks != null && !feedbacks.isEmpty() && feedbacks.get(0).getNeed() != null ? feedbacks.get(0).getNeed() : 0;
	}

	public static Integer getAverageFeasibilityForContribution(Long contributionId) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, sum(t0.feasibility) / count(contribution_id) as feasibility from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();
		List<ContributionFeedback> feedbacks =  where.eq("t0.contribution_id", contributionId).eq("t0.archived", false).findList();
		return feedbacks != null && !feedbacks.isEmpty() && feedbacks.get(0).getFeasibility() != null ? feedbacks.get(0).getFeasibility() : 0;
	}

	public static Integer getAverageFeasibilityForGroup(Long workingGroupId, Long contributionId) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, sum(t0.feasibility) / count(contribution_id) as feasibility from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();
		List<ContributionFeedback> feedbacks =  where.eq("t0.working_group_id", workingGroupId).eq("t0.contribution_id", contributionId).eq("t0.archived", false).findList();
		return feedbacks != null && !feedbacks.isEmpty() && feedbacks.get(0).getFeasibility() != null ? feedbacks.get(0).getFeasibility() : 0;
	}

	public static Integer getElegibilityCountForContribution(Long contributionId, boolean elegibility) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, count(elegibility) as benefit from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();

		List<ContributionFeedback> feedbacks = where.eq("t0.contribution_id", contributionId).eq("t0.archived", false).eq("elegibility", elegibility).
				findList();
		return feedbacks != null && !feedbacks.isEmpty() && feedbacks.get(0).getBenefit() != null ? feedbacks.get(0).getBenefit() : 0;
	}

	public static Integer getElegibilityCountForGroup(Long workingGroupId, Long contributionId, boolean elegibility) {

		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select contribution_id as id, count(elegibility) as benefit from contribution_feedback t0 " +
				" group by contribution_id";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();

		List<ContributionFeedback> feedbacks = where.eq("t0.working_group_id", workingGroupId).
				eq("t0.contribution_id", contributionId).eq("t0.archived", false).eq("elegibility", elegibility).
				findList();
		return feedbacks != null && !feedbacks.isEmpty() && feedbacks.get(0).getBenefit() != null ? feedbacks.get(0).getBenefit() : 0;
	}

	/**
	 * Returns the non archived Contribution feedback found based on the arguments
	 * @param cid
	 * @param userId
	 * @param workingGroupId
	 * @param type
	 * @param status
	 * @return
	 */
	public static List<ContributionFeedback> findPreviousContributionFeedback(Long cid, Long userId, Long workingGroupId,
												ContributionFeedbackTypes type, ContributionFeedbackStatus status ){
		return find.where().eq("contributionId", cid).eq("workingGroupId", workingGroupId).
				eq("userId", userId).eq("type", type == null ? null : type.ordinal()).
				eq("status", status == null ? null : status.ordinal()).eq("archived", false).findList();

	}

	public static List<ContributionFeedback> getFeedbacksByContribution(Long contributionId) {
		return find.where().eq("contributionId", contributionId).eq("archived", false).findList();
	}

	public static List<ContributionFeedback> getPrivateFeedbacksByContributionTypeAndWGroup(Long contributionId, Long groupId, String type) {
		ExpressionList<ContributionFeedback> where = find.where().eq("contributionId", contributionId)
				.eq("archived", false)
				//.eq("status", ContributionFeedbackStatus.PRIVATE)
				.eq("workingGroupId", groupId);
		if (type != null)
			where.eq("type", ContributionFeedbackTypes.valueOf(type));
		return where.findList();
	}

	public static List<ContributionFeedback> getPrivateFeedbacksByContributionType(Long contributionId, Long userId, String type) {
		ExpressionList<ContributionFeedback> where = find.where().eq("contributionId", contributionId)
				.eq("archived", false)
				//.eq("status", ContributionFeedbackStatus.PRIVATE)
				.eq("userId", userId)
				.isNull("workingGroupId");
		if (type != null)
			where.eq("type", ContributionFeedbackTypes.valueOf(type));
		return where.findList();
	}

	public static List<ContributionFeedback> getPublicFeedbacksByContributionType(Long contributionId, String type) {
		ExpressionList<ContributionFeedback> where = find.where().eq("contributionId", contributionId)
				.eq("archived", false)
				.eq("status", ContributionFeedbackStatus.PUBLIC);
		if (type != null)
			where.eq("type", ContributionFeedbackTypes.valueOf(type));
		return where.findList();
	}
}

