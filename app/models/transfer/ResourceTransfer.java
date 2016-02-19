package models.transfer;

import java.net.URL;
import java.util.UUID;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import enums.ResourceTypes;

public class ResourceTransfer {
	private Long resourceId;
	private UUID uuid;
	private URL url;
	private UserTransfer creator;
	private LocationTransfer location;
	@Enumerated(EnumType.STRING)
	private ResourceTypes resourceType;
	private String name;
	private String padId;
	private String readOnlyPadId;
	private UUID resourceSpaceWithServerConfigs;
	private URL urlLarge; 
	private URL urlMedium;
	private URL urlThumbnail;
	
	public ResourceTransfer() {
		super();
	}

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

	public void setUrl(URL url) {
		this.url = url;
	}

	public UserTransfer getCreator() {
		return creator;
	}

	public void setCreator(UserTransfer creator) {
		this.creator = creator;
	}

	public LocationTransfer getLocation() {
		return location;
	}

	public void setLocation(LocationTransfer location) {
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

	public void setReadOnlyPadId(String readOnlyPadId) {
		this.readOnlyPadId = readOnlyPadId;
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
	}

	public URL getUrlMedium() {
		return urlMedium;
	}

	public void setUrlMedium(URL urlMedium) {
		this.urlMedium = urlMedium;
	}

	public URL getUrlThumbnail() {
		return urlThumbnail;
	}

	public void setUrlThumbnail(URL urlThumbnail) {
		this.urlThumbnail = urlThumbnail;
	}

}
