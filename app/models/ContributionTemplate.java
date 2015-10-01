package models;

import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
public class ContributionTemplate extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long id;
	private UUID uuid;
	@Transient
	private String uuidAsString;

	@OneToMany(cascade = CascadeType.ALL)
	private ContributionTemplateSection templateSection;

	public ContributionTemplate(ContributionTemplateSection templateSection) {
		super();
		this.templateSection = templateSection;
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

	public ContributionTemplateSection getTemplateSection() {
		return templateSection;
	}

	public void setTemplateSection(ContributionTemplateSection templateSection) {
		this.templateSection = templateSection;
	}
}
