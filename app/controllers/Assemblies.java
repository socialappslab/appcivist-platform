package controllers;

import static play.data.Form.form;
import http.Headers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.Assembly;
import models.AssemblyProfile;
import models.Membership;
import models.MembershipAssembly;
import models.ResourceSpace;
import models.Theme;
import models.User;
import models.transfer.AssemblySummaryTransfer;
import models.transfer.MembershipCollectionTransfer;
import models.transfer.MembershipTransfer;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.Pair;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import delegates.AssembliesDelegate;
import delegates.MembershipsDelegate;
import enums.ResponseStatus;

@Api(value = "/assembly", description = "Assembly Making basic services: creating assemblies, listing assemblies, creating groups and invite people to join")
@With(Headers.class)
public class Assemblies extends Controller {

	public static final Form<Assembly> ASSEMBLY_FORM = form(Assembly.class);
	public static final Form<MembershipTransfer> MEMBERSHIP_FORM = form(MembershipTransfer.class);
	public static final Form<MembershipCollectionTransfer> INVITEES_FORM = form(MembershipCollectionTransfer.class);
	public static final Form<AssemblyProfile> PROFILE_FORM = form(AssemblyProfile.class);

	/**
	 * Return the full list of assemblies for non users
	 * 
	 */
	@ApiOperation(httpMethod = "GET", response = AssemblySummaryTransfer.class, responseContainer = "List", produces = "application/json", value = "Get list of assemblies based on query", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "query", value = "Search query string (keywords in title)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "filter", value = "Special filters. 'summary' returns only summarized info of assemblies, 'featured' returns a list of marks featured assemblies and 'nearby' limits the query to assemblies that are nearby of the user location, ", dataType = "String", paramType = "query", allowableValues = "featured,nearby,summary,random"), })
	public static Result findAssembliesPublic(String query, String filter) {
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
	@ApiOperation(httpMethod = "GET", response = Assembly.class, responseContainer = "List", produces = "application/json", value = "Get list of assemblies based on query", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "query", value = "Search query string (keywords in title)", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "filter", value = "Special filters. 'summary' returns only summarized info of assemblies, 'featured' returns a list of marks featured assemblies and 'nearby' limits the query to assemblies that are nearby of the user location, ", dataType = "String", paramType = "query", allowableValues = "featured,nearby,summary,random"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result findAssemblies(String query, String filter) {
		List<Assembly> a = AssembliesDelegate.findAssemblies(query, filter,
				false);
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

	@ApiOperation(response = Assembly.class, produces = "application/json", value = "Create a new assembly")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "assembly_form", value = "Body of Assembly in JSON", required = true, dataType = "models.Assembly", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result createAssembly() {
		// 1. obtaining the user of the requestor
		User creator = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));

		// 2. read the new group data from the body
		// another way of getting the body content => request().body().asJson()
		final Form<Assembly> newAssemblyForm = ASSEMBLY_FORM.bindFromRequest();

		if (newAssemblyForm.hasErrors()) {
			return badRequest(Json.toJson(TransferResponseStatus.badMessage(
					Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
							newAssemblyForm.errorsAsJson()), newAssemblyForm
							.errorsAsJson().toString())));
		} else {
			Assembly newAssembly = newAssemblyForm.get();
			// setting default values (TODO: maybe we should create a dedicated
			// method for this in each model)
			newAssembly.setCreator(creator);
			if (newAssembly.getLang() == null)
				newAssembly.setLang(creator.getLanguage());
			// TODO: check if assembly with same title exists
			// if not add it

			newAssembly.setDefaultValues();
			// Since JPA does not support saving ManyToMany associations with
			// elements that already exist, we remove those from the resources
			// space and then add them again through an update, once the
			// resource
			// space already exist
			// TODO: find a way to do this with @PrePersist and @PostPersist JPA
			// operations
			List<Theme> existingThemes = newAssembly.extractExistingThemes();
			Logger.info("Creating assembly");
			Logger.debug("=> " + newAssemblyForm.toString());
			newAssembly.save();
			if (!existingThemes.isEmpty()) {
				Logger.info("=> Adding Existing Themes");
				Logger.debug("=> " + existingThemes.toString());
				newAssembly.addThemes(existingThemes);
				newAssembly.updateResources();
			}

			// TODO: return URL of the new group
			Logger.info("Assembly created!");
			return ok(Json.toJson(newAssembly));
		}
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Get Assembly by ID", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result findAssembly(Long id) {
		Assembly a = Assembly.read(id);
		return a != null ? ok(Json.toJson(a)) : notFound(Json
				.toJson(new TransferResponseStatus(ResponseStatus.NODATA,
						"No assembly with ID = " + id)));
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Delete Assembly by ID", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteAssembly(Long id) {
		Assembly.delete(id);
		return ok();
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Update Assembly by ID", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "assembly_form", value = "Body of Assembly in JSON", required = true, dataType = "models.Assembly", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateAssembly(Long id) {
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
	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Add membership to the assembly", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "type", value = "Type of membership", allowableValues = "INVITATION, REQUEST, SUBSCRIPTION", required = true, paramType = "path"),
			@ApiImplicitParam(name = "membership_form", value = "membership's form in body", dataType = "models.transfer.MembershipTransfer", paramType = "body", required = true),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createAssemblyMembership(Long id, String type) {
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
			return Memberships.createMembership(requestor, "assembly", id,
					type, newMembership.getUserId(), newMembership.getEmail(),
					newMembership.getDefaultRoleId(),
					newMembership.getDefaultRoleName());
		}
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Get Assembly Memberships by ID and status", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "status", value = "Status of membership invitation or request", allowableValues = "REQUESTED, INVITED, FOLLOWING, ALL", required = true, paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result listMembershipsWithStatus(Long id, String status) {
		List<Membership> m = MembershipAssembly.findByAssemblyIdAndStatus(id,
				status);
		if (m != null && !m.isEmpty())
			return ok(Json.toJson(m));
		return notFound(Json.toJson(new TransferResponseStatus(
				"No memberships with status '" + status + "' in Assembly '"
						+ id + "'")));
	}

	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Add membership to the assembly by listing AppCivist's users emails", notes = "Get the full list of assemblies. Only availabe to ADMINS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "Assembly id", dataType = "Long", paramType = "path"),
			@ApiImplicitParam(name = "membership_form", value = "List of membership's form in the body including only the target's user email", dataType = "models.transfer.MembershipTransfer", paramType = "body", required = true),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result inviteNewMembers(Long id) {
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
				Pair<Membership, TransferResponseStatus> result = MembershipsDelegate
						.createMembership(requestor, "assembly", id,
								"INVITATION", null, membership.getEmail(),
								membership.getDefaultRoleId(),
								membership.getDefaultRoleName());
				results.add(result);
			}
			return ok(Json.toJson(results));
		}
	}

	@ApiOperation(response = AssemblyProfile.class, produces = "application/json", value = "Update the profile of the Assembly")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "assembly_profile_form", value = "Body of AssemblyProfile in JSON", required = true, dataType = "models.AssemblyProfile", paramType = "body"),
			@ApiImplicitParam(name = "uuid", value = "UUID of Assembly", required = true, dataType = "java.util.UUID", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result updateProfile(UUID uuid) {
		final Form<AssemblyProfile> updatedProfileForm = PROFILE_FORM
				.bindFromRequest();

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

	@ApiOperation(response = AssemblyProfile.class, produces = "application/json", value = "Get the profile of an Assembly")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "uuid", value = "UUID of Assembly", required = true, dataType = "java.util.UUID", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOrListed", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result getAssemblyProfile(UUID uuid) {
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
			@ApiImplicitParam(name = "uuid", value = "UUID of Assembly", required = true, dataType = "java.util.UUID", paramType = "path"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@SubjectPresent
	public static Result getAssemblyThemes(UUID uuid) {
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
}
