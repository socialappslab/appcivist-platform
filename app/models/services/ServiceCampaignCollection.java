package models.services;

import java.util.*;

import play.db.ebean.*;

public class ServiceCampaignCollection extends Model {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213623744477425962L;
	private List<ServiceCampaign> campaigns;

	public List<ServiceCampaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<ServiceCampaign> campaigns) {
		this.campaigns = campaigns;
	}	
	
	
}
