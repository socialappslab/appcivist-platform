package models.transfer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import play.i18n.Messages;
import enums.AppcivistNotificationTypes;
import enums.AppcivistResourceTypes;
import models.NotificationEventSignal;

@JsonInclude(Include.NON_EMPTY)
public class UpdateTransfer {
	private AppcivistNotificationTypes type;
	private AppcivistResourceTypes resourceType;
	private AppcivistResourceTypes containerType;
	private String title;
	private String text;
	private String resourceSummary;
	private Long resourceId;
	private UUID resourceUUID;
	private Long containerId;
	private UUID containerUUID;
	private Date date;
	private String lang;
	private String relativeUrl;
	private Map<String, Object> resource = new HashMap<>();
	private Map<String, Object> container = new HashMap<>();

	public UpdateTransfer() {
		super();
	}

	public UpdateTransfer(AppcivistNotificationTypes type,
			AppcivistResourceTypes resourceType, AppcivistResourceTypes containerType, String title, String text,
			String resourceSummary, Long resourceId, UUID resourceUUID, Long containerId, UUID containerUUID,  
			Date date, String lang, String rel) {
		super();
		this.type = type;
		this.resourceType = resourceType;
		this.setContainerType(containerType);
		this.title = title;
		this.text = text;
		this.resourceSummary = resourceSummary;
		this.resourceId = resourceId;
		this.resourceUUID = resourceUUID;
		this.setContainerId(containerId);
		this.setContainerUUID(containerUUID); 
		this.date = date;
		this.lang = lang;
		this.relativeUrl = rel;
	}

	public AppcivistNotificationTypes getType() {
		return type;
	}
	public void setType(AppcivistNotificationTypes type) {
		this.type = type;
	}
	public AppcivistResourceTypes getResourceType() {
		return resourceType;
	}
	public void setResourceType(AppcivistResourceTypes resourceType) {
		this.resourceType = resourceType;
	}
	public AppcivistResourceTypes getContainerType() {
		return containerType;
	}

	public void setContainerType(AppcivistResourceTypes containerType) {
		this.containerType = containerType;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getResourceSummary() {
		return resourceSummary;
	}
	public void setResourceSummary(String resourceSummary) {
		this.resourceSummary = resourceSummary;
	}
	public Long getResourceId() {
		return resourceId;
	}
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}
	public UUID getResourceUUID() {
		return resourceUUID;
	}
	public void setResourceUUID(UUID resourceUUID) {
		this.resourceUUID = resourceUUID;
	}
	public Long getContainerId() {
		return containerId;
	}

	public void setContainerId(Long containerId) {
		this.containerId = containerId;
	}

	public UUID getContainerUUID() {
		return containerUUID;
	}

	public void setContainerUUID(UUID containerUUID) {
		this.containerUUID = containerUUID;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getRelativeUrl() {
		return relativeUrl;
	}

	public void setRelativeUrl(String relativeUrli) {
		this.relativeUrl = relativeUrli;
	}

	public static UpdateTransfer getInstance(
			final AppcivistNotificationTypes updateType, 
			final AppcivistResourceTypes resourceType, 
			final AppcivistResourceTypes containerType,
			final String titleMessageKey,
			final String descriptionMessageKey,
			final String userName,
			final String userLang,
			final Long containerId, 
			final UUID containerUUID,
			final String containerName,
			final Long resourceId, 
			final UUID resourceUUID,
			final String resourceTitle,
			final String resourceText, 
			final String resourceAuthor,
			final Date resourceCreationDate) {

		String title = Messages.get(userLang, titleMessageKey, "");
		String desc = "";
		if(updateType.equals(AppcivistNotificationTypes.UPCOMING_MILESTONE)) 
			desc = Messages.get(userLang, descriptionMessageKey, resourceAuthor, containerName);
		else 
			desc = Messages.get(userLang, descriptionMessageKey, resourceTitle, resourceText, containerName);
		
		String text = resourceTitle+"/n"+resourceText;
		int length = text.length();
		if (length>256) length=256;
		return new UpdateTransfer(
				updateType, 
				resourceType, 
				containerType, 
				title, 
				desc,
				(resourceTitle+". "+resourceText).substring(0, length-1),
				resourceId, resourceUUID, 
				containerId, containerUUID,
				resourceCreationDate, userLang, "");
				
	}

	public Map<String, Object> getResource() {
		return resource;
	}

	public void setResource(Map<String, Object> resource) {
		this.resource = resource;
	}

	public void setResourceProperty(String key, Object value) {
		this.resource.put(key, value);
	}
	
	public Map<String, Object> getContainer() {
		return container;
	}

	public void setContainer(Map<String, Object> container) {
		this.container = container;
	}
	
	public void setContainerProperty(String key, Object value) {
		this.container.put(key, value);
	}

}
