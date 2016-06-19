package models.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContributionTemplateTransfer {
	private Long id;
	private UUID uuid;
	private List<ContributionTemplateSectionTransfer> templateSections = new ArrayList<>(); 
	
	public ContributionTemplateTransfer() {
		super();
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

	public List<ContributionTemplateSectionTransfer> getTemplateSections() {
		return templateSections;
	}

	public void setTemplateSections(
			List<ContributionTemplateSectionTransfer> templateSections) {
		this.templateSections = templateSections;
	}

}
