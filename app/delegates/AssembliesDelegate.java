package delegates;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.impl.AsExistingPropertyTypeSerializer;
import enums.AssemblyStatus;
import enums.CampaignStatus;
import enums.ConfigTargets;
import models.*;
import models.location.Location;
import models.transfer.*;

import org.dozer.DozerBeanMapper;

import exceptions.MembershipCreationException;
import play.Logger;
import play.Play;
import utils.GlobalDataConfigKeys;

public class AssembliesDelegate {

	public static DozerBeanMapper mapper;
	static {
		List<String> mappingFiles = Play.application().configuration()
				.getStringList("appcivist.dozer.mappingFiles");
		mapper = new DozerBeanMapper(mappingFiles);
	}

	/**
	 * Return queries based on a query and a filter
	 * 
	 */
	public static List<Assembly> findAssemblies(String query, String filter,
			Boolean onlyListed) {
		List<Assembly> a = null;
		if (filter.equals("featured")) {
			a = Assembly.findFeaturedAssemblies(query, onlyListed);
		} else if (filter.equals("random")) {
			a = Assembly.findRandomAssemblies(query, onlyListed);
			// TODO: } else if (filter.equals("nearby")) { return
			// Assembly.findNearbyAssemblies();
			// TODO: } else if (filter.equals("summary")) { return
			// Assembly.findAssembliesSummaries(query);
		} else {
			if (query != null && !query.isEmpty())
				return Assembly.findBySimilarName(query, onlyListed);
			else
				return Assembly.findAll(onlyListed);
		}

		return a;
	}

	/**
	 * Return queries based on a query and a filter
	 * 
	 */
	public static List<AssemblySummaryTransfer> findAssembliesPublic(
			String query, String filter) {
		List<Assembly> a = findAssemblies(query, filter, false);
		List<AssemblySummaryTransfer> result = new ArrayList<AssemblySummaryTransfer>();
		for (Assembly assembly : a) {
			AssemblySummaryTransfer destObject = mapper.map(assembly,
					AssemblySummaryTransfer.class);
			result.add(destObject);
		}
		return result;
	}
	public static AssemblyTransfer createMembership(Long id) throws MembershipCreationException {

		Assembly a = Assembly.read(id);
		Assembly.createMembership(a);
		a.update();
		a.refresh();
		return  mapper.map(a, AssemblyTransfer.class);
	}

	private static List<Config> getDefaultConfigs() {
		List<Config> aRet = new ArrayList<>();
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_AUTO_MEMBERSHIP_WORKING_GROUPS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_AUTO_MEMBERSHIP_WORKING_GROUPS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_DISABLE_NEW_MEMBERSHIPS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_DISABLE_NEW_MEMBERSHIPS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_ENABLE_FORUM,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_ENABLE_FORUM)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_ENABLE_MODERATOR_ROLE,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_ENABLE_MODERATOR_ROLE)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_HAS_REGISTRATION_FORM)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_MEMBERSHIP_INVITATION_BY_MEMBERS,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_MEMBERSHIP_INVITATION_BY_MEMBERS)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_MEMBERSHIP_TYPE,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_MEMBERSHIP_TYPE)));
		aRet.add(new Config(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_ENABLE_SOCIAL_IDEATION,
				GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_ASSEMBLY_ENABLE_SOCIAL_IDEATION)));
		for (Config config: aRet) {
			config.setConfigTarget(ConfigTargets.ASSEMBLY);
		}

		return aRet;
	}
	public static AssemblyTransfer publish(Long assemblyId) {
		Assembly newAssembly =  Assembly.read(assemblyId);
		newAssembly.setStatus(AssemblyStatus.PUBLISHED);
		newAssembly.update();
		newAssembly.refresh();
		return  mapper.map(newAssembly, AssemblyTransfer.class);
	}

	public static AssemblyTransfer createResources(AssemblyTransfer assemblyTransfer) {
		Assembly newAssembly = mapper.map(assemblyTransfer, Assembly.class);
		Assembly.createResources(newAssembly);
		Assembly old = Assembly.read(assemblyTransfer.getAssemblyId());
		List<Config> configs = getDefaultConfigs();
		List<Config> configsLoaded = new ArrayList<Config>();
		for (Config conf: configs) {
			Config.create(conf);
			configsLoaded.add(conf);
		}
		old.setConfigs(configsLoaded);
		old.update();
		old.refresh();
		return  mapper.map(old, AssemblyTransfer.class);

	}

	public static AssemblyTransfer create(AssemblyTransfer newAssemblyTransfer,
			User creator, String templates, Assembly principal, String invitations) throws MembershipCreationException {

		Location location = null;
		LocationTransfer locationTransfer = null;
		if (newAssemblyTransfer.getProfile() != null) {
			locationTransfer = newAssemblyTransfer.getProfile().getLocation();
			if (locationTransfer != null && locationTransfer.getLocationId() != null) {
				location = Location.find.where().eq("locationId", locationTransfer.getLocationId()).findUnique();
				if(location == null) {
					locationTransfer.setLocationId(null);
					newAssemblyTransfer.setLocation(locationTransfer);
				} else {
					newAssemblyTransfer.setLocation(null);
				}
			} else {
				newAssemblyTransfer.setLocation(locationTransfer);
			}
		}

		Assembly newAssembly = mapper.map(newAssemblyTransfer, Assembly.class);
		if (newAssemblyTransfer.getLocation() == null) {
			newAssembly.setLocation(null);
		}

		newAssembly.setCreator(creator);
		newAssembly.setStatus(AssemblyStatus.DRAFT);

		if (newAssembly.getLang() == null)
			newAssembly.setLang(creator.getLanguage());

		newAssembly.setDefaultValues();

		Logger.info("Creating assembly");
		Logger.debug("=> " + newAssembly.toString());

		// If templates !=  null the assembly must be related with the templates
		if (templates != null && templates.compareTo("") != 0) {
			String[] templatesIDs = templates.split(",");
			for (String id: templatesIDs) {
				Resource template = Resource.read(Long.parseLong(id));
				if (newAssembly.getResources() != null && newAssembly.getResources().getResources() != null) {
					newAssembly.getResources().getResources().add(template);
				} else {
					newAssembly.getResources().setResources(new ArrayList<Resource>());
					newAssembly.getResources().getResources().add(template);
				}

			}
		}
        newAssembly.save();
        newAssembly.refresh();
		Assembly.create(newAssembly);
		if(location != null) {
			Location location1 = mapper.map(locationTransfer, Location.class);
			newAssembly.setLocation(location1);
			newAssembly.update();
		}

		// Add List of Followed Assemblies
		ResourceSpace rs = newAssembly.getResources();
		List<LinkedAssemblyTransfer> linked = newAssemblyTransfer
				.getLinkedAssemblies();
		if (linked != null) {
			for (LinkedAssemblyTransfer linkedAssemblyTransfer : linked) {
				Assembly a = Assembly.read(linkedAssemblyTransfer
						.getAssemblyId());
				rs.addAssembly(a);
			}
		}
		rs.update();
		Logger.info("Assembly created!");

		newAssembly.refresh();
		newAssembly.setStatus(AssemblyStatus.DRAFT);
		newAssembly.update();
		newAssembly.refresh();
		// Adding new assembly to principal assembly if created under it
		if (principal!=null) {
			ResourceSpace rsprincipal = principal.getResources();
			rsprincipal.addAssembly(newAssembly);
			rsprincipal.update();
		}
		
		// Create and send invitations
		if(invitations!=null && invitations.equals("true")){
			List<InvitationTransfer> invitationsList = newAssemblyTransfer
					.getInvitations();
			if (invitations != null) {
				for (InvitationTransfer invitation : invitationsList) {
					MembershipInvitation.create(invitation, creator, newAssembly);
				}
			}
		}
		AssemblyTransfer created = mapper.map(newAssembly, AssemblyTransfer.class);
		return created;
	}

	
	public static AssemblySummaryTransfer readListedLinkedAssembly(Long aid, User requestor) {
		// 1. Read the assembly and check if it is listed
		Assembly a = Assembly.read(aid);
		if (a!=null && a.getListed()) {
			return mapper.map(a, AssemblySummaryTransfer.class);
		} else {
			// 2. if it is not listed, check if it is between the list of linked assemblies 
			//    linked to the user's assemblies
			//TODO: a = Assembly.readIfLinkedToUser(a,requestor);
			return null;
		}
	}

}
