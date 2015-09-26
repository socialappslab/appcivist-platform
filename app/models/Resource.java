package models;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import enums.ResourceTypes;
import models.location.Location;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "RESOURCE_TYPE")
public class Resource extends AppCivistBaseModel {
	@Id
	@GeneratedValue
	@Column(name = "resource_id")
	private Long resourceId;
	private UUID uuid = UUID.randomUUID();
	private URL url;
	@Transient
	private String urlAsString;
	private User creator;
	private Location location;
	
	@Column(name = "RESOURCE_TYPE", insertable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private ResourceTypes resourceType;

	/**
	 * The find property is an static property that facilitates database query
	 * creation
	 */
	public static Finder<Long, Resource> find = new Finder<>(Resource.class);

	public Resource() {
		super();
	}
	public Resource(URL url) {
		super();
		this.url = url;
	}	
	public Resource(String url) throws MalformedURLException {
		super();
		this.url = new URL(url);
	}

	public Resource(User creator, URL url) {
		super();
		this.creator = creator;
		this.url = url;
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


	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL externalURL) {
		this.url = externalURL;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public ResourceTypes getResourceType() {
		return resourceType;
	}

//	public void setResourceType(ResourceTypes resourceType) {
//		this.resourceType = resourceType;
//	}
	
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
	
	public String getUrlAsString() {
		if (url!=null)
			return this.url.toString();
		return null;
	}
	public void setUrlAsString(String urlAsString) throws MalformedURLException {
		this.url = new URL(urlAsString);
	}
}
