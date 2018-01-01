package delegates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import enums.CampaignStatus;
import enums.ConfigTargets;
import models.*;
import models.transfer.CampaignSummaryTransfer;
import models.transfer.CampaignTransfer;

import org.dozer.DozerBeanMapper;

import play.Logger;
import play.Play;
import utils.GlobalDataConfigKeys;

public class CampaignDelegate {

	public static DozerBeanMapper mapper;
	static {
		List<String> mappingFiles = 
				Play.application().configuration()
					.getStringList("appcivist.dozer.mappingFiles");
		mapper = new DozerBeanMapper(mappingFiles);
	}

	/**
	 * Create themes and default configs
	 * @param campaign
	 * @return
	 */
	public static CampaignTransfer createResources(CampaignTransfer campaign) {
        Campaign newCampaign =  mapper.map(campaign, Campaign.class);
		Campaign oldCampaign =  Campaign.read(newCampaign.getCampaignId());
        oldCampaign.setExistingThemes(newCampaign.getExistingThemes());
		List<Theme> existingThemes = oldCampaign.getExistingThemes();
		oldCampaign.setResources(newCampaign.getResources());
		ResourceSpace campaignResources = oldCampaign.getResources();
		// 5. Add existing themes to the resource space
		if (existingThemes != null && !existingThemes.isEmpty())
			campaignResources.getThemes().addAll(existingThemes);
		oldCampaign.setConfigs(getDefaultConfigs());
        oldCampaign.update();
        oldCampaign.refresh();

		return  mapper.map(newCampaign, CampaignTransfer.class);
	}

	public static CampaignTransfer publish(Long campaignId) {
		Campaign newCampaign =  Campaign.read(campaignId);
		newCampaign.setStatus(CampaignStatus.PUBLISHED);
		newCampaign.update();
		newCampaign.refresh();
		return  mapper.map(newCampaign, CampaignTransfer.class);
	}

	public static CampaignTransfer create(CampaignTransfer newCampaignTransfer, User campaignCreator, Long aid, String templateCampaignIds) {
		
		Campaign newCampaign =  mapper.map(newCampaignTransfer, Campaign.class);
		newCampaign.setCreator(campaignCreator);
		if (newCampaign.getLang() == null)
			newCampaign.setLang(campaignCreator.getLanguage());
		Logger.info("Creating new campaign");

		ResourceSpace assemblyResources = Assembly.read(aid).getResources();
		// Adding the new campaign to the Assembly Resource Space
		newCampaign.getAssemblies().add(aid);
		newCampaign.getContainingSpaces().add(assemblyResources);
		// By default, if no goal is stated, then the goal is the same as the title
		if (newCampaign.getGoal()==null) {
			newCampaign.setGoal(newCampaign.getTitle());
		}
		Campaign.create(newCampaign);
		newCampaign.refresh();
        newCampaign.setStatus(CampaignStatus.DRAFT);
		assemblyResources.addCampaign(newCampaign);
		assemblyResources.update();

		// If templates !=  null the assembly must be related with the templates
		if (templateCampaignIds != null && templateCampaignIds.compareTo("") != 0) {
			String[] templatesIDs = templateCampaignIds.split(",");
			for (String id: templatesIDs) {
				Campaign campaignTemplate = Campaign.read(Long.parseLong(id));
				List<Resource> campaignTemplateResources = campaignTemplate.getResources().getResources();
				List<Theme> campaignTemplateThemes = campaignTemplate.getResources().getThemes();
				List<Config> campaignTemplateConfigs = campaignTemplate.getResources().getConfigs();
				List<Component> campaignTemplateComponents = campaignTemplate.getResources().getComponents();
				if (newCampaign.getResources() == null) {
					newCampaign.setResources(new ResourceSpace());
				}
				if (newCampaign.getResources().getResources() == null) {
					newCampaign.getResources().setResources(new ArrayList<>());
				}
				if (newCampaign.getResources().getThemes() == null) {
					newCampaign.getResources().setThemes(new ArrayList<>());
				}
				if (newCampaign.getResources().getComponents() == null) {
					newCampaign.getResources().setComponents(new ArrayList<>());
				}
				if (newCampaign.getResources().getConfigs() == null) {
					newCampaign.getResources().setConfigs(new ArrayList<>());
				}
				newCampaign.getResources().getResources().addAll(campaignTemplateResources);
				newCampaign.getResources().getThemes().addAll(campaignTemplateThemes);
				newCampaign.getResources().getConfigs().addAll(campaignTemplateConfigs);
				newCampaign.getResources().getComponents().addAll(campaignTemplateComponents);
			}
			newCampaign.getResources().update();
		}

		newCampaignTransfer = mapper.map(newCampaign, CampaignTransfer.class);
		return newCampaignTransfer;
	}

	private static List<Config> getDefaultConfigs() {
		List<Config> aRet = new ArrayList<>();
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_NEWSLETTER_FRECUENCY,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_NEWSLETTER_FRECUENCY)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_ANONYMOUS_IDEAS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_ANONYMOUS_IDEAS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPE_PRINCIPAL,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPE_PRINCIPAL)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPES,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_TYPES)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_DISCUSSIONS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_DISCUSSIONS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK_PUBLIC,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_FEEDBACK_PUBLIC)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_NEW_CONTRIBUTIONS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_EXTENDED_NEW_CONTRIBUTIONS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING_PUBLIC,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_INFORMAL_VOTING_PUBLIC)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_DISCUSSIONS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_DISCUSSIONS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_SITE,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_PUBLIC_SITE)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_HIDE_TIMELINE,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_HIDE_TIMELINE)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_SHOW_ASSEMBLY_LOGO,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_SHOW_ASSEMBLY_LOGO)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_FEEDBACK_HIDDEN_FIELDS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_FEEDBACK_HIDDEN_FIELDS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_HIDDEN_FIELDS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_HIDDEN_FIELDS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ENABLE_IDEAS_DURING_PROPOSALS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ENABLE_IDEAS_DURING_PROPOSALS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_OPEN_IDEA_SECTION_DEFAULT,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_OPEN_IDEA_SECTION_DEFAULT)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_EMERGENT_THEMES,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_EMERGENT_THEMES)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_ANONYMOUS_IDEAS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_ANONYMOUS_IDEAS)));
		for (Config config: aRet) {
			config.setConfigTarget(ConfigTargets.CAMPAIGN);
		}
		return aRet;
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
