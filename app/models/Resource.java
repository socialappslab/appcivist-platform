package models;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import utils.services.EtherpadWrapper;
import models.location.Location;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResourceTypes;

@Entity
@JsonInclude(Include.NON_EMPTY)
public class Resource extends AppCivistBaseModel {
	@Id @GeneratedValue
	private Long resourceId;
	@Index
	private UUID uuid = UUID.randomUUID();
	private URL url;
	@Transient
	private String urlAsString;
	private User creator;
	private Location location;
	@Enumerated(EnumType.STRING)
	private ResourceTypes resourceType;

	private String name;
	/*
	 * Fields specific to each type
	 */
	
	/*
	 * Fields specific to type PAD
	 */
	@JsonIgnore
	private String padId;
	private String readOnlyPadId;
	private UUID resourceSpaceWithServerConfigs;

	/*
	 * Fields specific to type PICTURE
	 */
	@JsonIgnore
	private URL urlLarge; 
	@JsonIgnore
	private URL urlMedium;
	@JsonIgnore
	private URL urlThumbnail;
	
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
	
	public Resource(User creator, URL url, URL large, URL medium, URL thumbnail) {
		super();
		this.creator = creator;
		this.url = url;
		this.urlLarge = large; 
		this.urlMedium = medium; 
		this.urlThumbnail = thumbnail;
		if(url==null)
			if (urlLarge!=null) this.setUrl(urlLarge);
			else if (urlMedium!=null) this.setUrl(urlMedium);
			else if (urlThumbnail!=null) this.setUrl(urlThumbnail);
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

	public void setResourceType(ResourceTypes resourceType) {
		this.resourceType = resourceType;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPadId() {
		return padId;
	}

	public void setPadId(String padId) {
		this.padId = padId;
	}

	public String getReadOnlyPadId() {
		return readOnlyPadId;
	}

	public void setReadOnlyPadId(String padId) {
		this.readOnlyPadId = padId;
	}
	
	// TODO @Transient getPadContent => GET using Etherpad Client
	
	public UUID getResourceSpaceWithServerConfigs() {
		return resourceSpaceWithServerConfigs;
	}
	public void setResourceSpaceWithServerConfigs(
			UUID resourceSpaceWithServerConfigs) {
		this.resourceSpaceWithServerConfigs = resourceSpaceWithServerConfigs;
	}
	public URL getUrlLarge() {
		return urlLarge;
	}

	public void setUrlLarge(URL urlLarge) {
		this.urlLarge = urlLarge;
		if (this.getUrl()==null)
			this.setUrl(urlLarge);
	}

	public URL getUrlMedium() {
		return urlMedium;
	}

	public void setUrlMedium(URL urlMedium) {
		this.urlMedium = urlMedium;
		if (this.getUrl()==null && this.urlLarge==null)
			this.setUrl(urlMedium);
	}

	public URL getUrlThumbnail() {
		return urlThumbnail;
	}

	public void setUrlThumbnail(URL urlThumbnail) {
		this.urlThumbnail = urlThumbnail;
		if (this.getUrl()==null && this.urlLarge==null && this.urlMedium==null)
			this.setUrl(urlThumbnail);
	}	
	
	@Transient
	public String getUrlLargeString() {
		return urlLarge!=null ? urlLarge.toString() : null;
	}

	@Transient
	public void setUrlLargeString(String urlLargeString) throws MalformedURLException {
		if (urlLargeString!=null)
			setUrlLarge(new URL(urlLargeString));
	}

	@Transient
	public String getUrlMediumString() {
		return urlMedium!=null ? urlMedium.toString() : null;
	}

	@Transient
	public void setUrlMediumString(String urlMediumString) throws MalformedURLException {
		if (urlMediumString!=null)
			setUrlMedium( new URL(urlMediumString));
	}

	@Transient
	public String getUrlThumbnailString() {
		return urlThumbnail!=null ? urlThumbnail.toString() : null;
	}

	@Transient
	public void setUrlThumbnailString(String urlThumbnailString) throws MalformedURLException {
		if (urlThumbnailString!=null)
			setUrlThumbnail(new URL(urlThumbnailString));
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
	
	public String getUrlAsString() {
		if (url!=null)
			return this.url.toString();
		return null;
	}
	
	public void setUrlAsString(String urlAsString) throws MalformedURLException {
		this.url = new URL(urlAsString);
	}
	
	public void createPad(String etherpadServerUrl, String etherpadApiKey, String text) throws MalformedURLException {
		EtherpadWrapper eth = new EtherpadWrapper(etherpadServerUrl,etherpadApiKey);
		eth.createPad(this.padId);
		eth.setHTML(this.padId, text);
		this.setUrl(new URL(eth.getReadOnlyUrl(this.padId)));
	}

    public static List<Resource> findByResourceType(ResourceTypes contributionTemplate) {
    	System.out.println("----------------- resourceType " + contributionTemplate.toString());
		return find.where().eq("resourceType",contributionTemplate.toString()).findList();
    }
}
