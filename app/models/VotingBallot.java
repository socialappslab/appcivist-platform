//package models;
//
//import java.util.List;
//import java.util.UUID;
//
//import javax.persistence.CascadeType;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.OneToMany;
//import javax.persistence.OneToOne;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//
//@Entity
//public class VotingBallot extends AppCivistBaseModel {
//
//	@Id
//	@GeneratedValue
//	private Long votingBallotId;
//	private UUID uuid = UUID.randomUUID();
//	private String uuidAsString;
//	private String instructions;
//	private String notes;
//	private String system;
//	
//	@OneToMany(cascade=CascadeType.ALL)
//	List<>
//	
//	
//	/**
//	 * The find property is an static property that facilitates database query
//	 * creation
//	 */
//	public static Finder<Long, VotingBallot> find = new Finder<>(
//			VotingBallot.class);
//
//	public VotingBallot() {
//		super();
//		this.ups = new Long(0);
//		this.downs = new Long(0);
//		this.favs = new Long(0);
//		this.views = new Long(0);
//		this.replies = new Long(0);
//		this.flags = new Long(0);
//		this.shares = new Long(0);
//	}
//
//	/*
//	 * Getters and Setters
//	 */
//
//	public Long getContributionStatisticsId() {
//		return contributionStatisticsId;
//	}
//
//	public void setContributionStatisticsId(Long contributionStatisticsId) {
//		this.contributionStatisticsId = contributionStatisticsId;
//	}
//
//	public Long getUps() {
//		return ups;
//	}
//
//	public void setUps(Long ups) {
//		this.ups = ups;
//	}
//
//	public Long getDowns() {
//		return downs;
//	}
//
//	public void setDowns(Long downs) {
//		this.downs = downs;
//	}
//
//	public Long getFavs() {
//		return favs;
//	}
//
//	public void setFavs(Long favs) {
//		this.favs = favs;
//	}
//
//	public Long getViews() {
//		return views;
//	}
//
//	public void setViews(Long views) {
//		this.views = views;
//	}
//
//	public Long getReplies() {
//		return replies;
//	}
//
//	public void setReplies(Long replies) {
//		this.replies = replies;
//	}
//
//	public Long getFlags() {
//		return flags;
//	}
//
//	public void setFlags(Long flags) {
//		this.flags = flags;
//	}
//
//	public Long getShares() {
//		return shares;
//	}
//
//	public void setShares(Long shares) {
//		this.shares = shares;
//	}
//
//	public Contribution getContribution() {
//		return contribution;
//	}
//
//	public void setContribution(Contribution contribution) {
//		this.contribution = contribution;
//	}
//
//	/*
//	 * Basic Data operations
//	 */
//
//	public static VotingBallot read(Long id) {
//		return find.ref(id);
//	}
//
//	public static List<VotingBallot> findAll() {
//		return find.all();
//	}
//
//	public static VotingBallot create(VotingBallot object) {
//		object.save();
//		object.refresh();
//		return object;
//	}
//
//	public static VotingBallot createObject(
//			VotingBallot object) {
//		object.save();
//		return object;
//	}
//
//	public static void delete(Long id) {
//		find.ref(id).delete();
//	}
//
//	public static void update(Long id) {
//		find.ref(id).update();
//	}
//}
