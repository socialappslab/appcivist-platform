package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;

	// TODO: implemente as an ordered list
	@OneToMany(cascade = CascadeType.ALL)
	private List<ContributionTemplateSection> templateSections = new ArrayList<>();

	public ContributionTemplate(List<ContributionTemplateSection> templateSections) {
		super();
		this.templateSections = templateSections;
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

	public List<ContributionTemplateSection> getTemplateSections() {
		Collections.sort(templateSections, new ContributionTemplateSection());
		return templateSections;
	}

	public void setTemplateSections(List<ContributionTemplateSection> templateSections) {
		this.templateSections = templateSections;
	}
}
