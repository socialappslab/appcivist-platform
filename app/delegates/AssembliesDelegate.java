package delegates;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.MembershipInvitation;
import models.ResourceSpace;
import models.SecurityRole;
import models.TokenAction;
import models.User;
import models.transfer.AssemblySummaryTransfer;
import models.transfer.AssemblyTransfer;
import models.transfer.InvitationTransfer;
import models.transfer.LinkedAssemblyTransfer;

import org.dozer.DozerBeanMapper;

import play.Logger;
import play.Play;
import providers.MyUsernamePasswordAuthProvider;
import enums.MembershipStatus;
import enums.MembershipTypes;
import enums.MyRoles;

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
				MembershipInvitation membershipInvitation = new MembershipInvitation();
				membershipInvitation.setCreator(creator);
				membershipInvitation.setEmail(invitation.getEmail());
				membershipInvitation.setLang(creator.getLanguage());
				membershipInvitation.setStatus(MembershipStatus.INVITED);
				membershipInvitation.setTargetType(MembershipTypes.ASSEMBLY);
				membershipInvitation.setTargetId(newAssembly.getAssemblyId());

				List<SecurityRole> roles = new ArrayList<>();
				roles.add(SecurityRole.findByName(MyRoles.MEMBER.getName()));

				if (invitation.getCoordinator())
					roles.add(SecurityRole.findByName(MyRoles.COORDINATOR
							.getName()));
				if (invitation.getModerator())
					roles.add(SecurityRole.findByName(MyRoles.MODERATOR
							.getName()));

				membershipInvitation.setRoles(roles);
				final String token = UUID.randomUUID().toString();
				TokenAction ta = TokenAction.create(TokenAction.Type.MEMBERSHIP_INVITATION, token, null);
				membershipInvitation.setToken(ta);
				
				String baseInvitationUrl = Play.application().configuration().getString("appcivist.invitations.baseUrl");
				String invitationUrl = baseInvitationUrl + "invitation/"+token;
				String invitationEmail = newAssembly.getInvitationEmail()+"\n\n\n"+"Invitation Link: "+invitationUrl;
				membershipInvitation = MembershipInvitation.create(membershipInvitation);
				
				Logger.info("Sending assembly invitation to: "+membershipInvitation.getEmail());
				Logger.info("Invitation email: "+invitationEmail);
				MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
				provider.sendInvitationByEmail(membershipInvitation, invitationEmail, "AppCivist Invitation");
			}
		}
		AssemblyTransfer created = mapper.map(newAssembly, AssemblyTransfer.class);
		return created;
	}

}
