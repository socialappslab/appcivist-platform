package models;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

import enums.ResourceTypes;
import models.Location.Geo;
import play.db.ebean.Model;

@Entity
public class Resource extends AppCivistBaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2501353074091631217L;
	@Id
	@Column(name = "resource_id")
	private Long resourceId;
	private ResourceTypes type;
	private String externalResourceType;
	private URL url;
	private User creator;

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "RELATED_RESOURCES", joinColumns = { @JoinColumn(name = "source", referencedColumnName = "resource_id") }, inverseJoinColumns = { @JoinColumn(name = "target", referencedColumnName = "resource_id") })
	private List<Resource> resources = new ArrayList<Resource>();

	@OneToOne
	private Geo location;
/*
	@OneToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "resource_contribution", 
		joinColumns = 
			@JoinColumn(name = "resource_id", referencedColumnName = "resource_id"),
		inverseJoinColumns = 
			@JoinColumn(name = "contribution_id", referencedColumnName = "contribution_id")
	)*/
//	private Contribution contribution;
	
	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Model.Finder<Long, Resource> find = new Model.Finder<Long, Resource>(
			Long.class, Resource.class);

	public Resource(User creator, ResourceTypes type, URL externalURL,
			List<Resource> resources) {
		this.creator = creator;
		this.type = type;
		this.url = externalURL;
		this.resources = resources;
	}

	/*
	 * Getters and Setters
	 */

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public ResourceTypes getType() {
		return type;
	}

	public void setType(ResourceTypes type) {
		this.type = type;
	}

	/**
	 * The external resource type is a string property reserved to further
	 * explained what exactly the resource is (a google doc, spreadsheet, etc.)
	 * 
	 * @return
	 */
	public String getExternalResourceType() {
		return externalResourceType;
	}

	public void setExternalResourceType(String externalResourceType) {
		this.externalResourceType = externalResourceType;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL externalURL) {
		this.url = externalURL;
	}

	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public Geo getLocation() {
		return location;
	}

	public void setLocation(Geo location) {
		this.location = location;
	}

	/*
	 * Basic Data operations
	 */
	public static Resource read(Long resourceId) {
		return find.ref(resourceId);
	}

	public static List<Resource> findAll() {
		return find.all();
	}

	public static Resource create(Resource resource) {
		resource.save();
		resource.refresh();
		return resource;
	}

	public static Resource createObject(Resource resource) {
		resource.save();
		return resource;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}
}
