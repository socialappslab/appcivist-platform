package models;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import models.misc.Views;
import play.data.validation.Constraints.Required;


import javax.persistence.*;
import java.util.*;


@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="Organization", description="Model reprensenting Organizations within an Assembly")
public class Organization extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	private Long organizationId;
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@JsonView(Views.Public.class)
	@Required
	private String title;
	@Required
	@JsonView(Views.Public.class)
	private String description;
	@Required
	@JsonView(Views.Public.class)
	@OneToOne(cascade = CascadeType.ALL)
	@JsonIgnoreProperties({"creator", "resourceId", "location", "resourceType"})
	private Resource logo;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "organizations")
	private List<ResourceSpace> containingSpaces;


	public static Finder<Long, Organization> find = new Finder<>(Organization.class);

	public Organization() {
		super();
		this.uuid = UUID.randomUUID();
	}

	public Organization(String title, String description, Resource logo) {
		this.uuid = UUID.randomUUID();
		this.title = title;
		this.description = description;
		this.logo = logo;
	}

	public static Organization read(Long organizationId) {
		return find.ref(organizationId);
	}


	public static List<Organization> findAll() {
		return find.all();
	}


	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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

	public Resource getLogo() {
		return logo;
	}

	public void setLogo(Resource logo) {
		this.logo = logo;
	}

	public List<ResourceSpace> getContainingSpaces() {
		return containingSpaces;
	}

	public void setContainingSpaces(List<ResourceSpace> containingSpaces) {
		this.containingSpaces = containingSpaces;
	}

	/*
     * Basic Data operations
     */

	public static Organization create(Organization organization) {
		organization.save();
		organization.refresh();
		return organization;
	}

	public static Organization createObject(Organization organization) {
		organization.save();
		return organization;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}



}
