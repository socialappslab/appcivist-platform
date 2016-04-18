package models;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="candidate")
public class BallotCandidate extends Model {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="candidates_id_seq")
	@Index
    private Long id;
	private Long ballotId;
    private UUID uuid = UUID.randomUUID();
    /**
     * 0 -> EXTERNAL
     * 1 -> ASSEMBLY
     */
    private Integer candidateType;
	private UUID contributionUuid;
	@Transient
	private String contributionUuidAsString;
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

	public Integer getCandidateType() {
		return candidateType;
	}

	public void setCandidateType(Integer candidateType) {
		this.candidateType = candidateType;
	}

	public UUID getContributionUuid() {
		return contributionUuid;
	}

	public void setContributionUuid(UUID contributionUuid) {
		this.contributionUuid = contributionUuid;
	}

	public void setContributionUuidAsString(String uuid) {
		this.contributionUuidAsString = uuid;
		this.contributionUuid = UUID.fromString(uuid);
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

	public static BallotCandidate findByContributionUuid(Long ballotId, UUID uuid) {
		return find.where()
				.eq("ballotId",ballotId)
				.eq("contributionUuid",uuid).findUnique();
	}
}
