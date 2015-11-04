package delegates;

import java.util.ArrayList;
import java.util.List;

import models.Assembly;
import models.ResourceSpace;
import models.User;
import models.transfer.AssemblySummaryTransfer;
import models.transfer.AssemblyTransfer;
import models.transfer.InvitationTransfer;
import models.transfer.LinkedAssemblyTransfer;

import org.dozer.DozerBeanMapper;

import play.Logger;
import play.Play;
import play.libs.Json;
import providers.MyUsernamePasswordAuthProvider;

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
	public static List<Assembly> findAssemblies(String query, String filter, Boolean onlyListed) {
		List<Assembly> a = null;
		if (filter.equals("featured")) {
			a = Assembly.findFeaturedAssemblies(query, onlyListed);
		} else if (filter.equals("random")) { 
			a = Assembly.findRandomAssemblies(query, onlyListed);
//		TODO: } else if (filter.equals("nearby")) { return Assembly.findNearbyAssemblies();
//		TODO: } else if (filter.equals("summary")) { return Assembly.findAssembliesSummaries(query);
		} else {
			if (query!=null && !query.isEmpty()) return Assembly.findBySimilarName(query, onlyListed);
			else return Assembly.findAll(onlyListed);
		}
		
		return a;
	}
		
	/**
	 * Return queries based on a query and a filter
	 * 
	 */
	public static List<AssemblySummaryTransfer> findAssembliesPublic(String query, String filter) {
		List<Assembly> a = findAssemblies(query, filter, false);
		List<AssemblySummaryTransfer> result = new ArrayList<AssemblySummaryTransfer>();
		for (Assembly assembly : a) {
			AssemblySummaryTransfer destObject =  mapper.map(assembly, AssemblySummaryTransfer.class);
			result.add(destObject);
		}
		return result;
	}

	public static AssemblyTransfer create(AssemblyTransfer newAssemblyTransfer,
			User creator) {
		
		Assembly newAssembly =  mapper.map(newAssemblyTransfer, Assembly.class);

		newAssembly.setCreator(creator);
		if (newAssembly.getLang() == null)
			newAssembly.setLang(creator.getLanguage());
		newAssembly.setDefaultValues();

		// TODO: find a way to do this with @PrePersist and @PostPersist JPA
		// operations
		// List<Theme> existingThemes = newAssembly.extractExistingThemes();
		Logger.info("Creating assembly");
		Logger.debug("=> " + newAssembly.toString());
		Assembly.create(newAssembly);

		// Add List of Followed Assemblies
		ResourceSpace rs = newAssembly.getResources();
		List<LinkedAssemblyTransfer> linked = newAssemblyTransfer.getLinkedAssemblies();
		for (LinkedAssemblyTransfer linkedAssemblyTransfer : linked) {
			Assembly a = Assembly.read(linkedAssemblyTransfer.getAssemblyId());
			rs.addAssembly(a);
		}
		
		rs.update();
		
		// Send invitations
		List<InvitationTransfer> invitations = newAssemblyTransfer.getInvitations();
		for (InvitationTransfer invitation : invitations) {
			MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
			provider.sendInvitationByEmail(invitation, "ASSEMBLY", newAssembly.getAssemblyId());
		}
		
		Logger.info("Assembly created!");
		newAssembly.refresh();
		AssemblyTransfer created = mapper.map(newAssembly, AssemblyTransfer.class);
		return created;
	}

}
