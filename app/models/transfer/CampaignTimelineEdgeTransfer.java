package models.transfer;

import models.Component;

public class CampaignTimelineEdgeTransfer {
	private Long edgeId;
	private Component fromComponent;
	private Long fromComponentId;
	private Component toComponent;
	private Long toComponentId;

	public CampaignTimelineEdgeTransfer() {
		super();
	}

	public Long getEdgeId() {
		return edgeId;
	}

	public void setEdgeId(Long edgeId) {
		this.edgeId = edgeId;
	}

	public Component getFromComponent() {
		return fromComponent;
	}

	public void setFromComponent(Component fromComponent) {
		this.fromComponent = fromComponent;
	}

	public Long getFromComponentId() {
		return fromComponentId;
	}

	public void setFromComponentId(Long fromComponentId) {
		this.fromComponentId = fromComponentId;
	}

	public Component getToComponent() {
		return toComponent;
	}

	public void setToComponent(Component toComponent) {
		this.toComponent = toComponent;
	}

	public Long getToComponentId() {
		return toComponentId;
	}

	public void setToComponentId(Long toComponentId) {
		this.toComponentId = toComponentId;
	}
}
