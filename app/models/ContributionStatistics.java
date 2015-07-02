package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
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
	
	@OneToOne(mappedBy="stats")
	private Contribution contribution;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, ContributionStatistics> find = new Finder<Long, ContributionStatistics>(
			Long.class, ContributionStatistics.class);
	
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

	public Contribution getContribution() {
		return contribution;
	}

	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
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

	public static ContributionStatistics createObject(ContributionStatistics object) {
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
