package controllers;

import static play.data.Form.form;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import http.Headers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import models.*;
import models.misc.Views;
import models.transfer.AssemblySummaryTransfer;
import models.transfer.AssemblyTransfer;
import models.transfer.MembershipCollectionTransfer;
import models.transfer.MembershipTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.Pair;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import delegates.AssembliesDelegate;
import delegates.MembershipsDelegate;
import delegates.ResourcesDelegate;
import enums.ResourceTypes;
import enums.ResponseStatus;
import exceptions.MembershipCreationException;

@Api(value = "01 assembly: Assembly Making", position=1, description = "Assembly Making endpoints: creating assemblies, listing assemblies and inviting people to join")
@With(Headers.class)
public class Assemblies extends Controller {

	public static final Form<Assembly> ASSEMBLY_FORM = form(Assembly.class);
	public static final Form<AssemblyTransfer> ASSEMBLY_TRANSFER_FORM = form(AssemblyTransfer.class);
	public static final Form<MembershipTransfer> MEMBERSHIP_FORM = form(MembershipTransfer.class);
	public static final Form<MembershipCollectionTransfer> INVITEES_FORM = form(MembershipCollectionTransfer.class);
	public static final Form<AssemblyProfile> PROFILE_FORM = form(AssemblyProfile.class);

	/**
	 * Return the full list of assemblies for non users
	 * 
	 */
	@ApiOperation(httpMethod = "GET", response = AssemblySummaryTransfer.class, responseContainer = "List", produces = "application/json", value = "Get list of assemblies based on query")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	public static Result findAssembliesPublic(
			@ApiParam(name = "query", value = "Search query string (keywords in title)") String query, 
			@ApiParam(name = "filter", value = "Special filters. 'summary' returns only summarized info of assemblies, 'featured' returns a list of marks featured assemblies and 'nearby' limits the query to assemblies that are nearby of the user location, ", allowableValues = "featured,nearby,summary,random") String filter) {
		List<AssemblySummaryTransfer> a = AssembliesDelegate
				.findAssembliesPublic(query, filter);
		if (a != null)
			return ok(Json.toJson(a));
		else {
			String errorMsg = "";
			// 1. Check if the filter is supported, if not ask errorMsg about
			// not being supported
			if (filter != null && !filter.isEmpty()) {
				if (!filter.equals("featured") || !filter.equals("random")
				// || !filter.equals("nearby")
				// || !filter.equals("summary")
				) {
					errorMsg = "Filter '" + filter + "' is not supported yet";
				}
			}

			// 2. If there was a query, said something about the query
			if (query != null && !query.isEmpty() && errorMsg.isEmpty())
				errorMsg = "No assemblies with a title resembling query = '"
						+ query + "'";
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					errorMsg, "")));
		}
	}

	/**
	 * Return the full list of assemblies
	 * 
	 * @return models.AssemblyCollection
	 */
	@ApiOperation(httpMethod = "GET", response = Assembly.class, responseContainer = "List", produces = "application/json", value = "Get list of assemblies based on query")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result findAssemblies(	
			@ApiParam(name = "query", value = "Search query string (keywords in title)") String query, 
			@ApiParam(name = "filter", value = "Special filters. 'summary' returns only summarized info of assemblies, 'featured' returns a list of marks featured assemblies and 'nearby' limits the query to assemblies that are nearby of the user location, ", allowableValues = "featured,nearby,summary,random") String filter) {
		
		List<Assembly> a = AssembliesDelegate.findAssemblies(query, filter, true);
		if (a != null)
			return ok(Json.toJson(a));
		else {
			String errorMsg = "";
			// 1. Check if the filter is supported, if not ask errorMsg about
			// not being supported
			if (filter != null && !filter.isEmpty()) {
				if (!filter.equals("featured") || !filter.equals("random")
				// || !filter.equals("nearby")
				// || !filter.equals("summary")
				) {
					errorMsg = "Filter '" + filter + "' is not supported yet";
				}
			}

			// 2. If there was a query, said something about the query
			if (query != null && !query.isEmpty() && errorMsg.isEmpty())
				errorMsg = "No assemblies with a title resembling query = '"
						+ query + "'";
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					errorMsg, "")));
		}
	}

	/**
	 * 
	 * @return models.AssemblyCollection
	 */
	@ApiOperation(httpMethod = "GET", response = Assembly.class, responseContainer = "List", produces = "application/json", value = "Get list of linked assemblies to a single assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result findAssembliesLinked(@ApiParam(name="aid", value="Assembly ID") Long aid) {
		Assembly a = Assembly.findById(aid);
		if (a != null && a.getResources() != null) {
			List<Assembly> linked = a.getFollowedAssemblies();
			if (linked!=null && !linked.isEmpty())
				return ok(Json.toJson(linked));
			else 
				return notFound(Json.toJson(TransferResponseStatus.noDataMessage("This assembly is not following any other assembly", "")));
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage("Assembly with id = '"+aid+"' does not exists", "")));
		}
	}
	
	/**
	 * 
	 * @return models.AssemblyCollection
	 */
	@ApiOperation(httpMethod = "GET", response = AssemblySummaryTransfer.class, produces = "application/json", value = "Get assembly profile if it is listed or if it is in the list of linked assemblies")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result getListedLinkedAssemblyProfile(@ApiParam(name="aid", value="Assembly ID") Long aid) {
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		AssemblySummaryTransfer assembly = AssembliesDelegate.readListedLinkedAssembly(aid, requestor);
		if (assembly != null) {
			return ok(Json.toJson(assembly));		
		} else {
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage("Assembly with id = '"+aid+"' is not available for this user", "")));
		}
	}
	
	@ApiOperation(response = AssemblyTransfer.class, produces = "application/json", value = "Create a new assembly", httpMethod="POST", notes="The templates will be used to import all the resources from a list of existing assembly to the new")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Assembly Object", value = "Body of Assembly in JSON", required = true, dataType = "models.Assembly", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result createAssembly(@ApiParam(name="templates", value="List of assembly ids (separated by comma) to use as template for the current assembly") String templates) {
		// Get the user record of the creator
		User creator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		final Form<AssemblyTransfer> newAssemblyForm = ASSEMBLY_TRANSFER_FORM.bindFromRequest();
		// Check for errors in received data
		if (newAssemblyForm.hasErrors()) {
			return badRequest(Json.toJson(TransferResponseStatus.badMessage(
					Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
							newAssemblyForm.errorsAsJson()), newAssemblyForm
							.errorsAsJson().toString())));
		} else {
			AssemblyTransfer newAssembly = newAssemblyForm.get();
			// Do not create assemblies with names that already exist
			Assembly a = Assembly.findByName(newAssembly.getName());

			if (a==null) {
				Ebean.beginTransaction();
				try {
					AssemblyTransfer created = AssembliesDelegate.create(newAssembly, creator, templates);
					Ebean.commitTransaction();
					return ok(Json.toJson(created));
				} catch (Exception e) {
					Logger.error(e.getStackTrace().toString());
					e.printStackTrace();
					Ebean.rollbackTransaction();
					return internalServerError(Json.toJson(TransferResponseStatus.errorMessage(
							Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
									e.getMessage()), "")));
				}
			} else {
				return internalServerError(Json
						.toJson(TransferResponseStatus.errorMessage(
								"An assembly with the same title already exists: "
										+ "'" + newAssembly.getName() + "'", "")));
			}
		}
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Read Assembly by ID", notes="Only for MEMBERS of the assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findAssembly(@ApiParam(name = "id", value = "Assembly ID") Long id) {
		Assembly a = Assembly.read(id);
		return a != null ? ok(Json.toJson(a)) : notFound(Json
				.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
						"No assembly with ID = " + id)));
	}

	@ApiOperation(httpMethod = "DELETE", response = Assembly.class, produces = "application/json", value = "Delete Assembly by ID", notes = "Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteAssembly(@ApiParam(name = "id", value = "Assembly ID") Long id) {
		Assembly.delete(id);
		return ok();
	}

	@ApiOperation(httpMethod = "PUT", response = Assembly.class, produces = "application/json", value = "Update Assembly by ID", notes = "Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Assembly Object", value = "Body of Assembly in JSON", required = true, dataType = "models.Assembly", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateAssembly(@ApiParam(name = "id", value = "Assembly ID") Long id) {
		// 1. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Assembly> newAssemblyForm = ASSEMBLY_FORM.bindFromRequest();

		if (newAssemblyForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
					newAssemblyForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			Assembly newAssembly = newAssemblyForm.get();

			TransferResponseStatus responseBody = new TransferResponseStatus();
			newAssembly.setAssemblyId(id);
			newAssembly.update();

			// TODO: return URL of the new group
			Logger.info("Updating assembly");
			Logger.debug("=> " + newAssemblyForm.toString());

			responseBody.setNewResourceId(newAssembly.getAssemblyId());
			responseBody.setStatusMessage(Messages.get(
					GlobalData.ASSEMBLY_CREATE_MSG_SUCCESS,
					newAssembly.getName()));
			responseBody.setNewResourceURL(GlobalData.ASSEMBLY_BASE_PATH + "/"
					+ newAssembly.getAssemblyId());

			return ok(Json.toJson(responseBody));
		}
	}

	/**
	 * Creates memberships in the assembly for a new user
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Membership.class, produces = "application/json", value = "Add membership to the assembly", notes = "Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Membership simplified object", value = "Membership's form in body", dataType = "models.transfer.MembershipTransfer", paramType = "body", required = true),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createAssemblyMembership(
			@ApiParam(name = "id", value = "Assembly ID") Long id, 
			@ApiParam(name = "type", value = "Type of membership", allowableValues = "INVITATION, REQUEST, SUBSCRIPTION") String type) {
		// 1. obtaining the user of the requestor
		User requestor = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<MembershipTransfer> newMembershipForm = MEMBERSHIP_FORM
				.bindFromRequest();

		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			MembershipTransfer newMembership = newMembershipForm.get();
			try {
				Ebean.beginTransaction();
				Result r = Memberships.createMembership(requestor, "assembly", id,
						type, newMembership.getUserId(), newMembership.getEmail(),
						newMembership.getDefaultRoleId(),
						newMembership.getDefaultRoleName());
				Ebean.commitTransaction();
				return r;
			} catch (MembershipCreationException e) {
				Ebean.rollbackTransaction();
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages.get(
						GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
						"Error: "+e.getMessage()));
				return internalServerError(Json.toJson(responseBody));
			}
		}
	}

	@ApiOperation(httpMethod = "GET", response = Membership.class, responseContainer="List", produces = "application/json", value = "Get Assembly Memberships by ID and status", notes = "Only for MEMBERS of the assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "status", value = "Status of membership invitation or request", allowableValues = "REQUESTED, INVITED, FOLLOWING, ALL", required = true, paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result listMembershipsWithStatus(Long id, String status) {
		List<Membership> m = MembershipAssembly.findByAssemblyIdAndStatus(id,status);
		if (m != null && !m.isEmpty())
			return ok(Json.toJson(m));
		return notFound(Json.toJson(new TransferResponseStatus(
				"No memberships with status '" + status + "' in Assembly '"
						+ id + "'")));
	}
	
	@ApiOperation(httpMethod = "GET", response = TransferResponseStatus.class, produces = "application/json", value = "Verify if user is member of an assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No membership in this group found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	//@SubjectPresent
	public static Result isUserMemberOfAssembly(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
			@ApiParam(name = "uid", value = "User id") Long userId) {
		Boolean result = MembershipAssembly.isUserMemberOfAssembly(userId,aid);
		if (result) return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK, 
					"User '" + userId + "' is a member of Assembly '"+ aid + "'")));
		else return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, 
				"User '" + userId + "' is not a member of Assembly '"+ aid + "'")));
	}
	
	// TODO: create a better model for the response in this request
	@ApiOperation(httpMethod = "POST", response = Pair.class, responseContainer="List", produces = "application/json", value = "Add membership to the assembly by listing AppCivist's users emails", notes = "Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Membership simplified object", value = "List of membership's form in the body including only the target's user email", dataType = "models.transfer.MembershipTransfer", paramType = "body", required = true),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result inviteNewMembers(@ApiParam(name = "id", value = "Assembly ID") Long id) {
		// 1. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<MembershipCollectionTransfer> newMembershipForm = INVITEES_FORM
				.bindFromRequest();
		
		if (newMembershipForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get(
					GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
					newMembershipForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			// 1. obtaining the user of the requestor
			User requestor = User.findByAuthUserIdentity(PlayAuthenticate
					.getUser(session()));
			List<Pair<Membership, TransferResponseStatus>> results = new ArrayList<Pair<Membership, TransferResponseStatus>>();
			MembershipCollectionTransfer collection = newMembershipForm.get();
			for (MembershipTransfer membership : collection.getMemberships()) {
				Pair<Membership, TransferResponseStatus> result;
				try {
					Ebean.beginTransaction();
					result = MembershipsDelegate
							.createMembership(requestor, "assembly", id,
									"INVITATION", null, membership.getEmail(),
									membership.getDefaultRoleId(),
									membership.getDefaultRoleName());
					Ebean.commitTransaction();
					results.add(result);
				} catch (MembershipCreationException e) {
					Ebean.rollbackTransaction();
					TransferResponseStatus responseBody = new TransferResponseStatus();
					responseBody.setStatusMessage(Messages.get(
							GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
							"Error: "+e.getMessage()));
					return internalServerError(Json.toJson(responseBody));
				}
			}
			return ok(Json.toJson(results));
		}
	}

	@ApiOperation(httpMethod="GET", response = AssemblyProfile.class, produces = "application/json", value = "Update the profile of the Assembly")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "AssemblyProfile object", value = "Body of AssemblyProfile in JSON", required = true, dataType = "models.AssemblyProfile", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateProfile(@ApiParam(name = "uuid", value = "Universal ID of the Assembly") UUID uuid) {
		final Form<AssemblyProfile> updatedProfileForm = PROFILE_FORM.bindFromRequest();

		if (updatedProfileForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get("Profile data error",
					updatedProfileForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			AssemblyProfile ap = AssemblyProfile.findByAssembly(uuid);
			if (ap == null) {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(Messages
						.get("Assembly Profile for assembly '" + uuid
								+ "' does not exist"));
				return notFound(Json.toJson(responseBody));
			}

			AssemblyProfile apUpdate = updatedProfileForm.get();
			apUpdate.setAssemblyProfileId(ap.getAssemblyProfileId());
			Logger.info("Updating Assembly Profile of Assembly: "
					+ ap.getAssembly().getName());
			Logger.info("Updating Assembly Profile: "
					+ apUpdate.getTargetAudience());
			apUpdate = AssemblyProfile.update(apUpdate);
			return ok(Json.toJson(apUpdate));
		}
	}

	@ApiOperation(response = AssemblyProfile.class, produces = "application/json", value = "Read the profile of an Assembly")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOrListed", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result getAssemblyProfile(@ApiParam(name = "uuid", value = "Universal ID of Assembly") UUID uuid) {
		AssemblyProfile ap = AssemblyProfile.findByAssembly(uuid);
		if (ap == null) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages
					.get("Assembly Profile for assembly '" + uuid
							+ "' does not exist"));
			return notFound(Json.toJson(responseBody));
		}

		return ok(Json.toJson(ap));
	}

	@ApiOperation(response = Theme.class, responseContainer = "List", produces = "application/json", value = "Get themes of an assembly by its UUID")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@SubjectPresent
	public static Result getAssemblyThemes(@ApiParam(name = "uuid", value = "Universal ID of Assembly") UUID uuid) {
		Assembly a = Assembly.readByUUID(uuid);
		if (a == null) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage("Assembly '" + uuid
					+ "' does not exist");
			return notFound(Json.toJson(responseBody));
		} else {
			List<Theme> themes = a.getResources() == null ? null : a
					.getResources().getThemes();
			if (themes != null && !themes.isEmpty()) {
				return ok(Json.toJson(themes));
			} else {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage("The assembly'" + uuid
						+ "' has not Themes");
				return notFound(Json.toJson(responseBody));
			}
		}
	}
	
	/**
	 * POST /api/assembly/:aid/contribution/template
	 * Create a new Resource CAMPAIGN_TEMPLATE
	 * @param aid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = Resource.class, value = "Create a new Contribution Template in an assembly", notes="Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createContributionTemplateInAssembly(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid) {
		User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
		Resource res = ResourcesDelegate.createResource(campaignCreator, "", ResourceTypes.CONTRIBUTION_TEMPLATE, false, false);
		Assembly assembly = Assembly.read(aid);
		assembly.getResources().getResources().add(res);
		Assembly.update(assembly);
		if (res != null) {
			return ok(Json.toJson(res));
		} else {
			return internalServerError("The HTML text is malformed.");
		}

	}

	/**
	 * PUT /api/assembly/:aid/contribution/template/:rid
	 * Confirms a Resource CAMPAIGN_TEMPLATE
	 * @param rid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", value = "Confirm Contribution Template")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No contribution template found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result confirmContributionTemplateInAssembly(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid,
			@ApiParam(name = "Resource ID", value = "Contribution Template") Long rid) {
		Resource res = ResourcesDelegate.confirmResource(rid);
		return ok(Json.toJson(res));
	}

	/**
	 * GET /api/assembly/:aid/contribution/template Get list of available campaign templates in the assembly
	 *
	 * @return JSON array with the list of campaign templates
	 */
	@ApiOperation(httpMethod = "GET", response = URL.class, responseContainer = "List", produces = "application/json", value = "Get list of available contribution templates in assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No Contribution Template Found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({ @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"), })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findContributionTemplatesInAssembly(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid) {

		Assembly assembly = Assembly.read(aid);
		List<Resource> cts = assembly.getResources().getResources().stream().filter((r) ->
				ResourceTypes.CONTRIBUTION_TEMPLATE.equals(r.getResourceType())).collect(Collectors.toList());
		if (cts != null && !cts.isEmpty()) {
			List<URL> urls = cts.stream().map(sc -> sc.getUrl()).collect(Collectors.toList());
			return ok(Json.toJson(urls));
		} else {
			return notFound(Json.toJson(new TransferResponseStatus(
					"No contribution templates")));
		}
	}

	/**
	 * DELETE /api/assembly/:aid/campaign/template/:rid
	 * Delete campaign by ID
	 * @param aid
	 * @param resourceId
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = Campaign.class, produces = "application/json", value = "Delete contribution template from assembly", notes="Only for COORDINATOS of assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteContributionTemplateInAssembly(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid,
			@ApiParam(name = "rid", value = "Resource ID") Long resourceId) {
		Resource.delete(resourceId);
		return ok();
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Read assembly by Universal ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	public static Result findAssemblyByUUID(@ApiParam(name = "uuid", value = "Assembly Universal ID (UUID)") UUID uuid) {
		try{

			Assembly assembly = Assembly.readByUUID(uuid);
//			if(assembly == null){
//				return ok(Json
//						.toJson(new TransferResponseStatus("No assembly found")));
//			}

			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String result = mapper.writerWithView(Views.Public.class)
					.writeValueAsString(assembly);

			Content ret = new Content() {
				@Override public String body() { return result; }
				@Override public String contentType() { return "application/json"; }
			};

			return Results.ok(ret);
		}catch(Exception e){
			return badRequest(Json.toJson(Json
					.toJson(new TransferResponseStatus("Error processing request"))));
		}

	}
}
