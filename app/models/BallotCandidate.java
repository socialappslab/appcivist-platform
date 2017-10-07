package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import enums.BallotCandidateTypes;
import io.swagger.annotations.ApiModel;

import java.util.*;

import javax.persistence.*;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="candidate")
@ApiModel(value="BallotCandidate", description="The pointer to a candidate in a a ballot paper. Represents a resource in AppCivist through its UUID")
public class BallotCandidate extends Model {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="candidates_id_seq")
	@Index
    private Long id;
	private Long ballotId;
    private UUID uuid = UUID.randomUUID();
    /**
	 * EXTERNAL = 0
	 * ASSEMBLY = 1
	 * CONTRIBUTION = 2
	 * CAMPAIGN = 3
	 * USER = 4
	 * GROUP = 5
     */
	@Enumerated(EnumType.ORDINAL)
    private BallotCandidateTypes candidateType;
	private UUID candidateUuid;
	@Transient
	private String candidateUuidAsString;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date createdAt = Calendar.getInstance().getTime();	
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date updatedAt = Calendar.getInstance().getTime();	
	private Boolean removed = false;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date removedAt;
	
    /**
	 * The find property is an static property that facilitates database query creation
	 */
    public static Finder<Long, BallotCandidate> find = new Finder<>(BallotCandidate.class);

    /*
	 * Basic Data operations
	 */
    public static BallotCandidate read(Long id) {
        return find.ref(id);
    }

    public static List<BallotCandidate> findAll() {
        return find.all();
    }

    public static BallotCandidate create(BallotCandidate theme) {
        theme.save();
        theme.refresh();
        return theme;
    }

    public static BallotCandidate createObject(BallotCandidate theme) {
        theme.save();
        return theme;
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static void update(Long id) {
        find.ref(id).update();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBallotId() {
		return ballotId;
	}

	public void setBallotId(Long ballotId) {
		this.ballotId = ballotId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public BallotCandidateTypes getCandidateType() {
		return candidateType;
	}

	public void setCandidateType(BallotCandidateTypes candidateType) {
		this.candidateType = candidateType;
	}

	public UUID getCandidateUuid() {
		return candidateUuid;
	}

	public void setCandidateUuid(UUID candidateUuid) {
		this.candidateUuid = candidateUuid;
	}

	public void setCandidateUuidAsString(String uuid) {
		this.candidateUuidAsString = uuid;
		this.candidateUuid = UUID.fromString(uuid);
	}

	public String getCandidateUuidAsString() {
		return candidateUuidAsString;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Boolean getRemoved() {
		return removed;
	}

	public void setRemoved(Boolean removed) {
		this.removed = removed;
	}

	public Date getRemovedAt() {
		return removedAt;
	}

	public void setRemovedAt(Date removedAt) {
		this.removedAt = removedAt;
	}

	public static BallotCandidate findByCandidateUuid(Long ballotId, UUID uuid) {
		return find.where()
				.eq("ballotId",ballotId)
				.eq("candidateUuid",uuid).findUnique();
	}

	@JsonIgnore
	public Contribution getContribution(){
		return Contribution.find.where().eq("uuid", this.candidateUuid).findUnique();
	}

	@Transient
	public Map<String,String> getContributionSummary() {
		Contribution c = Contribution.find.where().eq("uuid", this.candidateUuid).findUnique();
		Map<String, String> cSummary = new HashMap<>();
		String title = c!=null ? c.getTitle() : null;
		Long id = c!=null ? c.getContributionId() : null;
		cSummary.put("title",title);
		cSummary.put("id",id+"");
		return cSummary;
	}
}
