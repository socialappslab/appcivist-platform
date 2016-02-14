package delegates;

import java.util.List;

import models.Assembly;
import models.Campaign;
import models.ResourceSpace;
import models.User;
import models.transfer.CampaignTransfer;

import org.dozer.DozerBeanMapper;

import play.Logger;
import play.Play;

public class CampaignDelegate {

	public static DozerBeanMapper mapper;
	static {
		List<String> mappingFiles = 
				Play.application().configuration()
					.getStringList("appcivist.dozer.mappingFiles");
		mapper = new DozerBeanMapper(mappingFiles);
	}
	
	public static CampaignTransfer create(CampaignTransfer newCampaignTransfer, User campaignCreator, Long aid) {
		
		Campaign newCampaign =  mapper.map(newCampaignTransfer, Campaign.class);
		
		if (newCampaign.getLang() == null) 
			newCampaign.setLang(campaignCreator.getLanguage());
		Logger.info("Creating new campaign");

		
		// Adding the new campaign to the Assembly Resource Space
		Campaign.create(newCampaign);
		ResourceSpace assemblyResources = Assembly.read(aid).getResources();
		assemblyResources.addCampaign(newCampaign);
		assemblyResources.update();
		newCampaign.refresh();
		
		newCampaignTransfer = mapper.map(newCampaign, CampaignTransfer.class);
		return newCampaignTransfer;
	}

}
