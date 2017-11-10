package delegates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import enums.CampaignStatus;
import models.*;
import models.transfer.CampaignSummaryTransfer;
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
	
	public static CampaignTransfer create(CampaignTransfer newCampaignTransfer, User campaignCreator, Long aid, String templates) {
		
		Campaign newCampaign =  mapper.map(newCampaignTransfer, Campaign.class);
		
		if (newCampaign.getLang() == null) 
			newCampaign.setLang(campaignCreator.getLanguage());
		Logger.info("Creating new campaign");

		// If templates !=  null the assembly must be related with the templates
		if (templates != null && templates.compareTo("") != 0) {
			String[] templatesIDs = templates.split(",");
			for (String id: templatesIDs) {
				Resource template = Resource.read(Long.parseLong(id));
				if (newCampaign.getResources() != null && newCampaign.getResources().getResources() != null) {
					newCampaign.getResources().getResources().add(template);
				} else {
					newCampaign.getResources().setResources(new ArrayList<Resource>());
					newCampaign.getResources().getResources().add(template);
				}

			}
		}
		ResourceSpace assemblyResources = Assembly.read(aid).getResources();


		// Adding the new campaign to the Assembly Resource Space
		newCampaign.getAssemblies().add(aid);
		newCampaign.getContainingSpaces().add(assemblyResources);
		Campaign.create(newCampaign);


		assemblyResources.addCampaign(newCampaign);
		assemblyResources.update();
		newCampaign.setStatus(CampaignStatus.PUBLISHED);
		newCampaign.update();
		newCampaign.refresh();
		
		newCampaignTransfer = mapper.map(newCampaign, CampaignTransfer.class);
		return newCampaignTransfer;
	}

	public static Campaign getCampaignSummary(UUID campaignUUID) {
		Campaign campaign = Campaign.readByUUID(campaignUUID);
		return campaign;
	}

	public static List<CampaignSummaryTransfer> findByBindingBallot(UUID uuid) {
		List<Campaign> campaigns = Campaign.findByCurrentBallotUUID(uuid);
		List<CampaignSummaryTransfer> campaignSummaries = new ArrayList<CampaignSummaryTransfer>();
		for (Campaign campaign : campaigns) {
			CampaignSummaryTransfer campaignSummary = mapper.map(campaign, CampaignSummaryTransfer.class);	
			campaignSummaries.add(campaignSummary);
		}
		return campaignSummaries;
	}
}
