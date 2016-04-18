package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class ContributionStatistics extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long contributionStatisticsId;
	private Long ups;
	private Long downs;
	private Long favs;
	private Long views;
	private Long replies;
	private Long flags;
	private Long shares;

	@Transient
	private Long yes;
	@Transient
	private Long no;
	@Transient
	private Long abstain;
	@Transient
	private Long block;
	@Transient
	private Long score;
	@Transient
	private Long rank;
	@Transient
	private Boolean blocked = false;

	@OneToOne(mappedBy = "stats")
	@JsonBackReference
	private Contribution contribution;

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, ContributionStatistics> find = new Finder<>(
			ContributionStatistics.class);

	public ContributionStatistics() {
		super();
		this.ups = new Long(0);
		this.downs = new Long(0);
		this.favs = new Long(0);
		this.views = new Long(0);
		this.replies = new Long(0);
		this.flags = new Long(0);
		this.shares = new Long(0);
	}

	/*
	 * Getters and Setters
	 */

	public Long getContributionStatisticsId() {
		return contributionStatisticsId;
	}

	public void setContributionStatisticsId(Long contributionStatisticsId) {
		this.contributionStatisticsId = contributionStatisticsId;
	}

	public Long getUps() {
		return ups;
	}

	public void setUps(Long ups) {
		this.ups = ups;
	}

	public Long getDowns() {
		return downs;
	}

	public void setDowns(Long downs) {
		this.downs = downs;
	}

	public Long getFavs() {
		return favs;
	}

	public void setFavs(Long favs) {
		this.favs = favs;
	}

	public Long getViews() {
		return views;
	}

	public void setViews(Long views) {
		this.views = views;
	}

	public Long getReplies() {
		return replies;
	}

	public void setReplies(Long replies) {
		this.replies = replies;
	}

	public Long getFlags() {
		return flags;
	}

	public void setFlags(Long flags) {
		this.flags = flags;
	}

	public Long getShares() {
		return shares;
	}

	public void setShares(Long shares) {
		this.shares = shares;
	}

	public Long getYes() {
		return yes;
	}

	public void setYes(Long yes) {
		this.yes = yes;
	}

	public Long getNo() {
		return no;
	}

	public void setNo(Long no) {
		this.no = no;
	}

	public Long getAbstain() {
		return abstain;
	}

	public void setAbstain(Long abstain) {
		this.abstain = abstain;
	}

	public Long getBlock() {
		return block;
	}

	public void setBlock(Long block) {
		this.block = block;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public Long getRank() {
		return rank;
	}

	public void setRank(Long rank) {
		this.rank = rank;
	}

	public Boolean getBlocked() {
		return blocked;
	}

	public void setBlocked(Boolean blocked) {
		this.blocked = blocked;
	}

	public Contribution getContribution() {
		return contribution;
	}

	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}
	
	public Long getPoints() {
		return this.getUps() - this.getDowns();
	}

	/*
	 * Basic Data operations
	 */

	public static ContributionStatistics read(Long id) {
		return find.ref(id);
	}

	public static List<ContributionStatistics> findAll() {
		return find.all();
	}

	public static ContributionStatistics create(ContributionStatistics object) {
		object.save();
		object.refresh();
		return object;
	}

	public static ContributionStatistics createObject(
			ContributionStatistics object) {
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
