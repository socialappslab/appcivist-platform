package controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.Campaign;
import models.ComponentInstance;
import models.ComponentInstanceMilestone;
import models.Contribution;
import models.Membership;
import models.MembershipAssembly;
import models.ResourceSpace;
import models.User;
import models.WorkingGroup;
import models.transfer.TransferResponseStatus;
import models.transfer.UpdateTransfer;
import be.objectify.deadbolt.java.actions.Dynamic;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import enums.AppcivistNotificationTypes;
import enums.AppcivistResourceTypes;
import play.*;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;
import security.SecurityModelConstants;
import views.html.*;
import http.Headers;

/**
 * Temporal controller to manage a mockup version of the notification server bus
 * TODO: replace this controller (or adapt) to use a notification queue like RabbitMQ
 * 
 * @author cdparra
 *
 */
@Api(value="/notification")
@With(Headers.class)
@SuppressWarnings("unused")
public class Notifications extends Controller {

	final private static String NOTIFICATION_TITLE_ASSEMBLY_UPDATE = "notification.title.assembly.update";
	final private static String NOTIFICATION_TITLE_GROUP_UPDATE  = "notification.title.group.update";
	final private static String NOTIFICATION_TITLE_CONTRIBUTION_UPDATE  = "notification.title.contribution.update";
	final private static String NOTIFICATION_TITLE_CAMPAIGN_UPDATE  = "notification.title.campaign.update";
	final private static String NOTIFICATION_TITLE_MILESTONE_UPDATE  = "notification.title.campaign.update.milestone";
	final private static String NOTIFICATION_TITLE_MESSAGE_NEW = "notification.title.message.new";
	final private static String NOTIFICATION_TITLE_MESSAGE_REPLY = "notification.title.message.reply";
	final private static String NOTIFICATION_TITLE_MESSAGE_GROUP_NEW = "notification.title.message.new.group";
	final private static String NOTIFICATION_TITLE_MESSAGE_GROUP_REPLY = "notification.title.message.reply.group";
	final private static String NOTIFICATION_TITLE_MESSAGE_ASSEMBLY_NEW = "notification.title.message.new.assembly";
	final private static String NOTIFICATION_TITLE_MESSAGE_ASSEMBLY_REPLY = "notification.title.message.reply.assembly";
	
	final private static String NOTIFICATION_DESCRIPTION_ASSEMBLY_FORUM_CONTRIBUTION = "notification.description.assembly.forum.contribution";
	final private static String NOTIFICATION_DESCRIPTION_GROUP_FORUM_CONTRIBUTION = "notification.description.group.forum.contribution";
	final private static String NOTIFICATION_DESCRIPTION_CONTRIBUTION_COMMENT = "notification.description.contribution.comment";
	final private static String NOTIFICATION_DESCRIPTION_CAMPAIGN_CONTRIBUTION = "notification.description.campaign.contribution";
	final private static String NOTIFICATION_DESCRIPTION_UPCOMING_MILESTONE = "notification.description.campaign.upcoming.milestone";
	
	/**
	 * userInbox is the method called by the route GET /user/{uuid}/inbox
	 * it returns a list of TransferUpdate containing the latest news from User's assemblies, groups, and contributions
	 * @param userUUID
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = UpdateTransfer.class, responseContainer="List", produces = "application/json", value = "Update user information", notes = "Updates user information")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "User not found", response=TransferResponseStatus.class) })
	@ApiImplicitParams({
		//@ApiImplicitParam(name="user", value="user", dataType="String", defaultValue="user", paramType = "path"),
		@ApiImplicitParam(name="uuid", value="User's UUID", dataType="Long", paramType="path"),
		@ApiImplicitParam(name="SESSION_KEY", value="User's session authentication key", dataType="String", paramType="header")
	})
	@Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
	public static Result userInbox(UUID userUUID) {
		// 0. Obtain User
		User u = User.findByUUID(userUUID);
		// 1. Get a list of Assemblies to which the User belongs
		List<Membership> myAssemblyMemberships = Membership.findByUser(u, "ASSEMBLY");
		// 2. Get a list of Working Groups to which the User belongs
		List<Membership> myGroupMemberships = Membership.findByUser(u, "GROUP");
		// 3. Get a list of Contributions by the user 
		List<Contribution> myContribs = Contribution.readByCreator(u);
		
		List<UpdateTransfer> updates = new ArrayList<UpdateTransfer>();

		// 4. Process AssemblyMemberships to get
		// 4.1. New Assembly Forum Posts
		// 4.2. Current Ongoing Campaigns Upcoming Milestones
		// 4.3. Current Ongoing Campaigns Latest Contribution
		updates = processMyAssemblies(u, updates, myAssemblyMemberships);
		
		// 5. Process GroupMemberships to get
		// 5.1. New Group Forum Posts
		// 5.2. New comments related to Group Contributions
		//TODO: updates = processMyGroups(u, updates, myGroupMemberships);
		
		// 6. Process Contributions to get comments on them
		//TODO: updates = processMyContributions(u, updates, myContribs);
		if (!updates.isEmpty()) return ok(Json.toJson(updates));
		else
			return notFound(Json.toJson(new TransferResponseStatus("No updates")));
	}

	private static List<UpdateTransfer> processMyAssemblies(User u,
			List<UpdateTransfer> updates, List<Membership> myAssemblyMemberships) {

		for (Membership membership : myAssemblyMemberships) {
			Assembly a = ((MembershipAssembly) membership).getAssembly();
			
			// 4.1. New Assembly Forum Posts
			ResourceSpace aForum = a.getForum();
			List<Contribution> posts = null;
			Contribution latestForumPost = null;
			if (aForum !=null) posts = aForum.getContributions();
			if (posts != null && !posts.isEmpty()) latestForumPost = posts.get(posts.size()-1);
			if (latestForumPost != null)
				updates.add(UpdateTransfer.getInstance(
						AppcivistNotificationTypes.ASSEMBLY_UPDATE,
						AppcivistResourceTypes.CONTRIBUTION_COMMENT,
						AppcivistResourceTypes.ASSEMBLY,
						NOTIFICATION_TITLE_ASSEMBLY_UPDATE,
						NOTIFICATION_DESCRIPTION_ASSEMBLY_FORUM_CONTRIBUTION, u
								.getName(), u.getLanguage(), a.getAssemblyId(),
						a.getUuid(), a.getName(), latestForumPost
								.getContributionId(),
						latestForumPost.getUuid(), latestForumPost.getTitle(),
						latestForumPost.getText(), latestForumPost.getAuthors().get(0)
								.getName(), latestForumPost.getCreation()));
			
			
			// 4.2. Current Ongoing Campaigns Upcoming Milestones
			ResourceSpace resources = a.getResources();
			List<Campaign> campaigns = null;
			if (resources !=null) campaigns = resources.getCampaigns();
			if (campaigns != null && !campaigns.isEmpty()) {
				for (Campaign c : campaigns) {
					List<ComponentInstance> components = c.getResources().getComponents();
					if (components != null && !components.isEmpty()) {
						for (ComponentInstance p : components) {
							Calendar today = Calendar.getInstance();
							if (p.getEndDate() !=null && p.getEndDate().after(today.getTime())) {
								// 4.2. Current Ongoing Campaigns Upcoming Milestones
								List<ComponentInstanceMilestone> milestones = p.getMilestones();
								if (milestones!=null && !milestones.isEmpty()) {
									for (ComponentInstanceMilestone m : milestones) {
										Date mStart = m.getStart();
										Calendar cal = Calendar.getInstance();
										cal.setTime(mStart); // Now use today date.
										cal.add(Calendar.DATE, m.getDays());
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
										if(cal.getTime().after(today.getTime())) 
											updates.add(UpdateTransfer.getInstance(
													AppcivistNotificationTypes.UPCOMING_MILESTONE,
													AppcivistResourceTypes.CAMPAIGN_COMPONENT,
													AppcivistResourceTypes.CAMPAIGN,
													NOTIFICATION_TITLE_MILESTONE_UPDATE,
													NOTIFICATION_DESCRIPTION_UPCOMING_MILESTONE,
													u.getName(),
													u.getLanguage(),
													c.getCampaignId(),
													c.getUuid(),
													c.getTitle(),
													m.getComponentInstanceMilestoneId(),
													m.getUuid(), m.getTitle(),
													sdf.format(cal.getTime()),
													a.getName(), m
															.getCreation()));
									}
								}
							}

							// TODO: 4.3. Current Ongoing Campaigns Latest Contribution
						}
					}
					
				}
			}
		}		
		return updates;
	}

	private static List<UpdateTransfer> processMyGroups(User u,
			List<UpdateTransfer> updates, List<Membership> myGroupMemberships) {
		for (Membership membership : myGroupMemberships) {
			WorkingGroup g = membership.getTargetGroup();
			
			// 4.1. New Group Forum Posts
			// TODO
//			ResourceSpace gForum = g.getResources().get;
//			List<Contribution> posts = null;
//			Contribution latestForumPost = null;
//			if (gForum !=null) posts = gForum.getContributions();
//			if (posts != null && !posts.isEmpty()) latestForumPost = posts.get(0);
//			if (latestForumPost != null)
//				updates.add(TransferUpdate.getInstance(
//						AppcivistNotificationTypes.ASSEMBLY_UPDATE,
//						AppcivistResourceTypes.CONTRIBUTION_COMMENT,
//						AppcivistResourceTypes.ASSEMBLY,
//						NOTIFICATION_TITLE_ASSEMBLY_UPDATE,
//						NOTIFICATION_DESCRIPTION_ASSEMBLY_FORUM_CONTRIBUTION, u
//								.getName(), u.getLanguage(), g.getAssemblyId(),
//						g.getUuid(), g.getName(), latestForumPost
//								.getContributionId(),
//						latestForumPost.getUuid(), latestForumPost.getTitle(),
//						latestForumPost.getText(), latestForumPost.getAuthor()
//								.getName(), latestForumPost.getCreation()));
		}
		
		return updates;
	}

	private static List<UpdateTransfer> processMyContributions(User u,
			List<UpdateTransfer> updates, List<Contribution> myContribs) {
		// TODO
		
		return null;
	}


}
