package delegates;

import java.util.List;

import models.User;
import models.transfer.CampaignTransfer;

import org.dozer.DozerBeanMapper;

import play.Play;

public class CampaignDelegate {

	public static DozerBeanMapper mapper;
	static {
		List<String> mappingFiles = 
				Play.application().configuration()
					.getStringList("appcivist.dozer.mappingFiles");
		mapper = new DozerBeanMapper(mappingFiles);
	}
	
	public static CampaignTransfer create(CampaignTransfer newCampaignTransfer, User creator) {
//		
//		Assembly newAssembly =  mapper.map(newAssemblyTransfer, Assembly.class);
//		
//		newAssembly.setCreator(creator);
//		if (newAssembly.getLang() == null)
//			newAssembly.setLang(creator.getLanguage());
//		
//		newAssembly.setDefaultValues();
//
//		Logger.info("Creating assembly");
//		Logger.debug("=> " + newAssembly.toString());
//		Assembly.create(newAssembly);
//
//		// Add List of Followed Assemblies
//		ResourceSpace rs = newAssembly.getResources();
//		List<LinkedAssemblyTransfer> linked = newAssemblyTransfer.getLinkedAssemblies();
//		for (LinkedAssemblyTransfer linkedAssemblyTransfer : linked) {
//			Assembly a = Assembly.read(linkedAssemblyTransfer.getAssemblyId());
//			rs.addAssembly(a);
//		}
//		
//		rs.update();
//		
//		// Send invitations
//		List<InvitationTransfer> invitations = newAssemblyTransfer.getInvitations();
//		for (InvitationTransfer invitation : invitations) {
//			MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
//			provider.sendInvitationByEmail(invitation, "ASSEMBLY", newAssembly.getAssemblyId());
//		}
//		
//		Logger.info("Assembly created!");
//		newAssembly.refresh();
//		AssemblyTransfer created = mapper.map(newAssembly, AssemblyTransfer.class);
		return null;
	}

}
