package models;

import io.swagger.annotations.ApiModel;

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

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(content=Include.NON_NULL)
@ApiModel(value="ContributionTemplate", description="Template for a contribution")
public class ContributionTemplate extends AppCivistBaseModel {

	@Id
	@GeneratedValue
	private Long id;
	@Index
	private UUID uuid = UUID.randomUUID();
	@Transient
	private String uuidAsString;

	// TODO: implement as an ordered list
	@OneToMany(cascade = CascadeType.ALL)
	private List<ContributionTemplateSection> templateSections = new ArrayList<>();

	public static Finder<Long, ContributionTemplate> find = new Finder<>(ContributionTemplate.class);

	public static ContributionTemplate read(Long id) {
		return find.ref(id);
	}

	public ContributionTemplate(List<ContributionTemplateSection> templateSections) {
		super();
		this.templateSections = templateSections;
		this.uuid = UUID.randomUUID();
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
		return uuid != null ? uuid.toString() : "";
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
