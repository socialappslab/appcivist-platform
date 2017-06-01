package models;

import io.swagger.annotations.ApiModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@ApiModel(value="BallotConfiguration", description="key,value configurations for a voting ballot")
public class BallotConfiguration extends Model {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ballot_configurations_id_seq")
	@Index
    private Long id;
	private Long ballotId;
	private String key;
	private String value;
	private Integer position;
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
    public static Finder<Long, BallotConfiguration> find = new Finder<>(BallotConfiguration.class);

    /*
	 * Basic Data operations
	 */
    public static BallotConfiguration read(Long id) {
        return find.ref(id);
    }

    public static List<BallotConfiguration> findAll() {
        return find.all();
    }

    public static BallotConfiguration create(BallotConfiguration theme) {
        theme.save();
        theme.refresh();
        return theme;
    }

    public static BallotConfiguration createObject(BallotConfiguration theme) {
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
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
