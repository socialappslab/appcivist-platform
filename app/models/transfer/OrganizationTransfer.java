package models.transfer;

import models.Resource;

import java.util.UUID;

public class OrganizationTransfer {
	private Long organizationId;
	private String title;
	private String description;
	private ResourceTransfer logo;

	public OrganizationTransfer() {
		super();
	}

	public OrganizationTransfer(Long id, String title, String description) {
		super();
		this.title = title;
		this.description = description;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public ResourceTransfer getLogo() {
		return logo;
	}

	public void setLogo(ResourceTransfer logo) {
		this.logo = logo;
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
}
