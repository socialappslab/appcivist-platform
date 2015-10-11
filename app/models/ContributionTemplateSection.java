package models;

import java.util.Comparator;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class ContributionTemplateSection extends AppCivistBaseModel implements Comparator<ContributionTemplateSection> {
	@Id
	@GeneratedValue
	private Long id;
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;

	private String title;
	private String description;
	private int length;
	private int position;

	public ContributionTemplateSection() {
		super();
	}
	
	public ContributionTemplateSection(String title, String description,
			int length, int order) {
		super();
		this.title = title;
		this.description = description;
		this.length = length;
		this.position = order;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public String getUuidAsString() {
		return uuid.toString();
	}

	public void setUuidAsString(String uuidAsString) {
		this.uuid = UUID.fromString(uuidAsString);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int order) {
		this.position = order;
	}
	@Override
	public int compare(ContributionTemplateSection o1, ContributionTemplateSection o2) {
		return o1.getPosition() - o2.getPosition();
	}
}
