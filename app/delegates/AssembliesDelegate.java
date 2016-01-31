package delegates;

import java.util.ArrayList;
import java.util.List;

import models.Assembly;
import models.MembershipInvitation;
import models.ResourceSpace;
import models.User;
import models.transfer.AssemblySummaryTransfer;
import models.transfer.AssemblyTransfer;
import models.transfer.InvitationTransfer;
import models.transfer.LinkedAssemblyTransfer;

import org.dozer.DozerBeanMapper;

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
			User creator) {

		Assembly newAssembly = mapper.map(newAssemblyTransfer, Assembly.class);

		newAssembly.setCreator(creator);
		if (newAssembly.getLang() == null)
			newAssembly.setLang(creator.getLanguage());

		newAssembly.setDefaultValues();

		Logger.info("Creating assembly");
		Logger.debug("=> " + newAssembly.toString());
		Assembly.create(newAssembly);

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

		// Create and send invitations
		List<InvitationTransfer> invitations = newAssemblyTransfer
				.getInvitations();
		if (invitations != null) {
			for (InvitationTransfer invitation : invitations) {
				MembershipInvitation.create(invitation, creator, newAssembly);
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
