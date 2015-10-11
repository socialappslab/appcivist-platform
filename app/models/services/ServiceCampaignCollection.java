package models.services;

import java.util.*;
import com.avaje.ebean.Model;

public class ServiceCampaignCollection extends Model {
	private List<ServiceCampaign> campaigns;

	public List<ServiceCampaign> getCampaigns() {
		return campaigns;
	}

	public void setCampaigns(List<ServiceCampaign> campaigns) {
		this.campaigns = campaigns;
	}	
	
	
}
