package models;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.ApiModel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.*;

import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ContributionTypes;
import enums.ContributionFeedbackStatus;
import enums.ContributionFeedbackTypes;
import models.misc.Views;
import org.omg.CORBA.OBJ_ADAPTER;

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
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="contribution_id")
	private Contribution contribution;

	private Long userId;
	// TODO: Add a way of making feedback private or limited to a working group

	private Integer benefit;
	private Integer need;
	private Integer feasibility;
	private Boolean elegibility;
	@Column(name = "textual_feedback")
	private String textualFeedback;
	@Enumerated(EnumType.STRING)
	private ContributionFeedbackTypes type;
	@Enumerated(EnumType.STRING)
	private ContributionFeedbackStatus status;
	@Column(name = "working_group_id")
	private Long workingGroupId;
	@Column(name = "official_group_feedback")
	private Boolean officialGroupFeedback = false;
	private Boolean archived = false;

	@JsonView(Views.Public.class)
	@ManyToOne(cascade = CascadeType.ALL)
	private NonMemberAuthor nonMemberAuthor;

	@Transient
	private String password;

	@Transient
	private UUID workingGroupUuid;

	@Transient
	@Enumerated(EnumType.STRING)
	private ContributionTypes parentType;

	@Transient
	private Map<String, Object> user;

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
		return find.where().eq("contribution.contributionId", this.contribution.getContributionId()).eq("workingGroupId", workingGroupId).
				eq("userId", this.userId).eq("status", this.status == null ? null : this.status).
				eq("type", this.type == null ? null : this.type).eq("archived", true).
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
		return this.contribution.getContributionId();
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
	
	public ContributionTypes getParentType() {
		parentType = this.contribution.getType();
		return parentType;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public UUID getWorkingGroupUuid() {
		return workingGroupUuid;
	}

	public void setWorkingGroupUuid(UUID uuid) {
		this.workingGroupUuid = uuid;
	}

	
	public Contribution getContribution() {
		return contribution;
	}

	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}

	public NonMemberAuthor getNonMemberAuthor() {
		return nonMemberAuthor;
	}

	public void setNonMemberAuthor(NonMemberAuthor nonMemberAuthor) {
		this.nonMemberAuthor = nonMemberAuthor;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public Map<String, Object> getUser() {
		return user;
	}

	public void setUser(Map<String, Object> user) {
		this.user = user;
	}

	public static long getUpsForContribution(Long contributionId) {
		return find.where().eq("contribution.contributionId", contributionId).eq("archived", false).eq("up", true).findRowCount();
	}

	public static long getUpsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contribution.contributionId", contributionId).
				eq("archived", false).eq("up", true).
				eq("type", ContributionFeedbackTypes.WORKING_GROUP).findRowCount();
	}
	
	public static long getDownsForContribution(Long contributionId) {
		return find.where().eq("contribution.contributionId", contributionId).eq("archived", false).eq("down", true).findRowCount();
	}

	public static long getDownsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contribution.contributionId", contributionId).
				eq("archived", false).eq("down", true).
				eq("type", ContributionFeedbackTypes.WORKING_GROUP).findRowCount();
	}
	
	public static long getFavsForContribution(Long contributionId) {
		return find.where().eq("contribution.contributionId", contributionId).eq("archived", false).eq("fav", true).findRowCount();
	}

	public static long getFavsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contribution.contributionId", contributionId).
				eq("archived", false).eq("fav", true).
				eq("type", ContributionFeedbackTypes.WORKING_GROUP).findRowCount();
	}
	
	public static long getFlagsForContribution(Long contributionId) {
		return find.where().eq("contribution.contributionId", contributionId).eq("archived", false).eq("flag", true).findRowCount();
	}

	public static long getFlagsForGroup(Long workingGroupId, Long contributionId) {
		return find.where().eq("workingGroupId", workingGroupId).eq("contribution.contributionId", contributionId).
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
		List<ContributionFeedback> feedbacks =  find.where().eq("contribution.contributionId", cid)
				.eq("archived", false).eq("userId", userId).findList();
		if(feedbacks != null && !feedbacks.isEmpty()) {
			return feedbacks.get(0);
		} else {
			return null;
		}
	}

	public static Double getAverageTypeForContribution(Long contributionId, String type, Long groupId) {
		ExpressionList<ContributionFeedback> where;
		String rawQuery = "select id as id, t0." + type + " as " + type + " from contribution_feedback t0 ";
		RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
		where = find.setRawSql(rawSql).where();
		if(groupId != null) {
			where.eq("t0.working_group_id", groupId);
		}
		where.eq("status", ContributionFeedbackStatus.PUBLIC);
		List<ContributionFeedback> feedbacks =  where.eq("t0.contribution_id", contributionId).eq("t0.archived", false)
				.isNotNull("t0." + type).findList();
		double total = 0;
		double count = 0;
		for(ContributionFeedback feedback: feedbacks) {
			if (type.equals("benefit")) {
				total = total + Double.valueOf(feedback.getBenefit());
				count = count + 1;
			} else if(type.equals("need")) {
				total = total + Double.valueOf(feedback.getNeed());
				count = count + 1;
			} else {
				total = total + Double.valueOf(feedback.getFeasibility());
				count = count + 1;
			}
		}
		return !feedbacks.isEmpty() ? total/count : 0.0;
	}
	public static Double getAverageBenefitForContribution(Long contributionId) {
		return getAverageTypeForContribution(contributionId, "benefit", null);
	}

	public static Double getAverageBenefitForGroup(Long workingGroupId, Long contributionId) {
		return getAverageTypeForContribution(contributionId, "benefit", workingGroupId);
	}

	public static Double getAverageNeedForContribution(Long contributionId) {

		return getAverageTypeForContribution(contributionId, "need", null);
	}

	public static Double getAverageNeedForGroup(Long workingGroupId, Long contributionId) {
		return getAverageTypeForContribution(contributionId, "need", workingGroupId);
	}

	public static Double getAverageFeasibilityForContribution(Long contributionId) {
		return getAverageTypeForContribution(contributionId, "feasibility", null);
	}

	public static Double getAverageFeasibilityForGroup(Long workingGroupId, Long contributionId) {

		return getAverageTypeForContribution(contributionId, "feasibility", workingGroupId);

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
												ContributionFeedbackTypes type, ContributionFeedbackStatus status, NonMemberAuthor nonMemberAuthor ){
		return find.where().eq("contribution.contributionId", cid).eq("workingGroupId", workingGroupId).
				eq("userId", userId).eq("type", type == null ? null : type).
				eq("status", status == null ? null : status).eq("archived", false).
				eq("nonMemberAuthor",nonMemberAuthor == null ? null : nonMemberAuthor).findList();

	}

	public static List<ContributionFeedback> getFeedbacksByContribution(Long contributionId) {
		return find.where().eq("contribution.contributionId", contributionId).eq("archived", false).findList();
	}

	public static List<ContributionFeedback> getPrivateFeedbacksByContributionTypeAndWGroup(Long contributionId, Long groupId, String type) {
		ExpressionList<ContributionFeedback> where = find.where().eq("contribution.contributionId", contributionId)
				.eq("archived", false);
				//.eq("status", ContributionFeedbackStatus.PRIVATE)
	//			.eq("workingGroupId", groupId);
		if (type != null)
			where.eq("type", ContributionFeedbackTypes.valueOf(type));
		return where.findList();
	}

	public static List<ContributionFeedback> getPrivateFeedbacksByContributionType(Long contributionId, Long userId, String type) {
		ExpressionList<ContributionFeedback> where = find.where().eq("contribution.contributionId", contributionId)
				.eq("archived", false)
				//.eq("status", ContributionFeedbackStatus.PRIVATE)
				.isNull("workingGroupId");
		if (userId != null)
			where.eq("userId", userId);
		if (type != null)
			where.eq("type", ContributionFeedbackTypes.valueOf(type));
		return where.findList();
	}

	public static List<ContributionFeedback> getPublicFeedbacksByContributionType(Long contributionId, String type) {
		ExpressionList<ContributionFeedback> where = find.where().eq("contribution.contributionId", contributionId)
				.eq("archived", false)
				.eq("status", ContributionFeedbackStatus.PUBLIC);
		if (type != null)
			where.eq("type", ContributionFeedbackTypes.valueOf(type));
		return where.findList();
	}
}

