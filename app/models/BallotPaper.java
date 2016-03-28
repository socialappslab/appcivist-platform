package models;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class BallotPaper extends Model {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ballot_papers_id_seq")
	@Index
    private Long id;
	private Long ballotId;
	private UUID uuid = UUID.randomUUID(); 
	private String signature;
	/**
	 * 0 -> DRAFT
	 * 1 -> FINISHED
	 */
	private Integer status;
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
    public static Finder<Long, BallotPaper> find = new Finder<>(BallotPaper.class);

    /*
	 * Basic Data operations
	 */
    public static BallotPaper read(Long id) {
        return find.ref(id);
    }

    public static List<BallotPaper> findAll() {
        return find.all();
    }

    public static BallotPaper create(BallotPaper theme) {
        theme.save();
        theme.refresh();
        return theme;
    }

    public static BallotPaper createObject(BallotPaper theme) {
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
	
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
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
}
