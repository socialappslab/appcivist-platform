package models;

import io.swagger.annotations.ApiModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name="vote")
@ApiModel(value="BallotVote", description="Individual vote in casted ballot")
public class BallotVote extends Model {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="votes_id_seq")
	@Index
    private Long id;
    private Long candidateId;
    private Long ballotPaperId;
    private String value;
    /**
     * SCORE -> 0, 
     * RANKING -> 1 , 
     * PREFERENCE (YES, NO, ABSTAIN, BLOCK) -> 2
     */
    private Integer valueType;
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
    public static Finder<Long, BallotVote> find = new Finder<>(BallotVote.class);

    /*
	 * Basic Data operations
	 */
    public static BallotVote read(Long id) {
        return find.ref(id);
    }

    public static List<BallotVote> findAll() {
        return find.all();
    }

    public static BallotVote create(BallotVote theme) {
        theme.save();
        theme.refresh();
        return theme;
    }

    public static BallotVote createObject(BallotVote theme) {
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

	public Long getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(Long candidateId) {
		this.candidateId = candidateId;
	}

	public Long getBallotPaperId() {
		return ballotPaperId;
	}

	public void setBallotPaperId(Long ballotPaperId) {
		this.ballotPaperId = ballotPaperId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getValueType() {
		return valueType;
	}

	public void setValueType(Integer valueType) {
		this.valueType = valueType;
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

	public static List<BallotVote> findVotesForCandidate(Long ballotId, Long candidateId) {
		return find.where().eq("ballotId",ballotId).eq("candidateId",candidateId).findList();
	}
}
