package models.transfer;

import java.util.UUID;

public class CampaignBriefTransfer {
	private UUID campaignId;
	private String brief;

	public CampaignBriefTransfer() {
		super();
	}
	public UUID getCampaignUuid() {
		return campaignId;
	}
	public void setCampaignUuid(UUID campaignId) {
		this.campaignId = campaignId;
	}
	public String getBrief() {
		return brief;
	}
	public void setBrief(String brief) {
		this.brief = brief;
	}
}
