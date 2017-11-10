package delegates;

import java.util.ArrayList;
import java.util.List;

import enums.AssemblyStatus;
import models.*;
import models.location.Location;
import models.transfer.*;

import org.dozer.DozerBeanMapper;

import exceptions.MembershipCreationException;
import play.Logger;
import play.Play;

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
		newAssembly.setStatus(AssemblyStatus.PUBLISHED);
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
