package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity(name="ballot_registration_fields")
public class BallotRegistrationField extends Model {
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="ballot_registration_fields_id_seq")
	@Index
    private Long id;
    private Long ballotId;
	private String name;
	@Column(name = "description", columnDefinition = "text")
	private String description;
	@Column(name = "expected_value", columnDefinition = "text")
	private String expectedValue;
	private Integer position;
	private Boolean removed = false;
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm a z")
	private Date removedAt;
	
	/**
	 * The find property is an static property that facilitates database query creation
	 */
    public static Finder<Long, BallotRegistrationField> find = new Finder<>(BallotRegistrationField.class);

    /*
	 * Basic Data operations
	 */
    public static BallotRegistrationField read(Long themeId) {
        return find.ref(themeId);
    }

    public static List<BallotRegistrationField> findAll() {
        return find.all();
    }

    public static BallotRegistrationField create(BallotRegistrationField theme) {
        theme.save();
        theme.refresh();
        return theme;
    }

    public static BallotRegistrationField createObject(BallotRegistrationField theme) {
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

	public void setBallotId(Long ballot_id) {
		this.ballotId = ballot_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExpectedValue() {
		return expectedValue;
	}

	public void setExpectedValue(String expectedValue) {
		this.expectedValue = expectedValue;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
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
