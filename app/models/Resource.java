package models;

import com.avaje.ebean.annotation.Index;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonView;
import enums.ResourceTypes;
import io.swagger.annotations.ApiModel;
import models.location.Location;
import models.misc.Views;
import utils.services.EtherpadWrapper;

import javax.persistence.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="Resource", description="Resource represents external resources, accessible through an URL")
public class Resource extends AppCivistBaseModel {
	@Id @GeneratedValue
	private Long resourceId;
	@Index
	@JsonView(Views.Public.class)
	private UUID uuid = UUID.randomUUID();
	@JsonView(Views.Public.class)
	private URL url;
	@Transient
	@JsonView(Views.Public.class)
	private String urlAsString;
	private User creator;
	@JsonView(Views.Public.class)
	private Location location;
	@JsonView(Views.Public.class)
	@Enumerated(EnumType.STRING)
	private ResourceTypes resourceType;
	@JsonView(Views.Public.class)
	private String name;
	@JsonView(Views.Public.class)
	private String title;
	@JsonView(Views.Public.class)
	private String description;
	/*
	 * Fields specific to each type
	 */
	
	/*
	 * Fields specific to type PAD
	 */
	@JsonIgnore
	private String padId;
    @JsonView(Views.Public.class)
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

	private boolean confirmed;

	private String resourceAuthKey;

	private boolean isTemplate = false;

	@JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "resources", cascade = CascadeType.ALL)
	private List<ResourceSpace> containingSpaces;
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

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getResourceAuthKey() {
		return this.resourceAuthKey;
	}

	public void setResourceAuthKey(String key) {
		this.resourceAuthKey = key;
	}

	public boolean getIsTemplate() {
		return this.isTemplate;
	}

	public void setIsTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}


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
	
	public void createHtmlPad(String etherpadServerUrl, String etherpadApiKey, String text) throws MalformedURLException, UnsupportedEncodingException {
		EtherpadWrapper eth = new EtherpadWrapper(etherpadServerUrl,etherpadApiKey);
		eth.createPad(this.padId);
		eth.setHTML(this.padId, text);
		this.setReadOnlyPadId(eth.getReadOnlyId(this.padId));
		this.setUrl(new URL(eth.getReadOnlyUrl(this.padId)));
	}
	
	public void createTextPad(String etherpadServerUrl, String etherpadApiKey, String text) throws MalformedURLException {
		EtherpadWrapper eth = new EtherpadWrapper(etherpadServerUrl,etherpadApiKey);
		eth.createPad(this.padId);
		eth.setText(this.padId, text);
		this.setReadOnlyPadId(eth.getReadOnlyId(this.padId));
		this.setUrl(new URL(eth.getReadOnlyUrl(this.padId)));
		
	}

	public void createReadablePad(String etherpadServerUrl, String etherpadApiKey, String text) throws MalformedURLException, UnsupportedEncodingException {
		EtherpadWrapper eth = new EtherpadWrapper(etherpadServerUrl,etherpadApiKey);
		eth.createPad(this.padId);
		eth.setHTML(this.padId, text);
		this.setUrl(new URL(eth.getEditUrl(this.padId)));
	}

    public static List<Resource> findByResourceType(ResourceTypes contributionTemplate) {
		return find.where().eq("resourceType",contributionTemplate.toString()).eq("confirmed", true).findList();
    }

	public static void deleteUnconfirmedContributionTemplates(ResourceTypes contributionTemplate) {
		List<Resource> resources = find.where().eq("resourceType",contributionTemplate.toString()).eq("confirmed", false).findList();
		Date today = new Date();
		for (Resource r: resources) {
			// adding 172800 seconds
			if (r.getCreation().getTime() < (today.getTime() - 172800l))
				find.ref(r.getResourceId()).delete();
		}
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

    public static Long getIdByUUID(UUID uuid) {
		Resource r = find.where().eq("uuid",uuid.toString()).findUnique();
		return r == null ? null : r.getResourceId();
	}
}
