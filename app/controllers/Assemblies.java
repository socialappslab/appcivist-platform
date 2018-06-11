package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feth.play.module.pa.PlayAuthenticate;
import delegates.AssembliesDelegate;
import delegates.MembershipsDelegate;
import delegates.NotificationsDelegate;
import delegates.ResourcesDelegate;
import enums.*;
import exceptions.ConfigurationException;
import exceptions.MembershipCreationException;
import http.Headers;
import io.swagger.annotations.*;
import models.*;
import models.misc.Views;
import models.transfer.*;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.*;
import play.twirl.api.Content;
import providers.LdapAuthProvider;
import providers.MyLoginUsernamePasswordAuthUser;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.LogActions;
import utils.Pair;

import javax.persistence.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static play.data.Form.form;

@Api(value = "01 assembly: Assembly Making", position=1, description = "Assembly Making endpoints: creating assemblies, listing assemblies and inviting people to join")
@With(Headers.class)
public class Assemblies extends Controller {

	public static final Form<Assembly> ASSEMBLY_FORM = form(Assembly.class);
	public static final Form<AssemblyTransfer> ASSEMBLY_TRANSFER_FORM = form(AssemblyTransfer.class);
	public static final Form<MembershipTransfer> MEMBERSHIP_FORM = form(MembershipTransfer.class);
	public static final Form<MembershipCollectionTransfer> INVITEES_FORM = form(MembershipCollectionTransfer.class);
	public static final Form<AssemblyProfile> PROFILE_FORM = form(AssemblyProfile.class);
	public static final Form<Organization> ORGANIZATION_FORM = form(Organization.class);

	/**
	 * GET       /api/assembly/listed
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
	 * GET       /api/assembly
	 * Return the full list of assemblies
	 * 
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Assembly.class, responseContainer = "List", produces = "application/json", value = "Get list of assemblies based on query")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result findAssemblies(	
			@ApiParam(name = "query", value = "Search query string (keywords in title)") String query, 
			@ApiParam(name = "filter", value = "Special filters. 'summary' returns only summarized info of assemblies, 'featured' returns a list of marks featured assemblies and 'nearby' limits the query to assemblies that are nearby of the user location, ", allowableValues = "featured,nearby,summary,random") String filter,
			@ApiParam(name = "shortname", value = "Search by shortname") String shortname) {
		List<Assembly> a = null;
		if(shortname != null && !shortname.equals("")){
			 Assembly assem= Assembly.findByShortName(shortname);
			 if (assem !=null) {
				 a = new ArrayList<Assembly>();
				 a.add(assem);
			 }
		}else {
			 a = AssembliesDelegate.findAssemblies(query, filter, true);
		}
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
			// 3. If there was a shortname, said something about the shortname
			if (shortname != null && !shortname.isEmpty() && errorMsg.isEmpty())
				errorMsg = "No assemblies with a shortname = '"
						+ shortname + "'";
			return notFound(Json.toJson(TransferResponseStatus.noDataMessage(
					errorMsg, "")));
		}
	}

	/**
	 * GET       /api/assembly/:aid/linked
	 *
	 * @param aid
	 * @return
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
	 * GET       /api/assembly/:aid/public
	 *
	 * @param aid
	 * @return
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

    @ApiOperation(httpMethod = "PUT", response = AssemblyTransfer.class, produces = "application/json", value = "Create assembly membership")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({ @Group(GlobalData.USER_ROLE) })
    public static Result createNewAssemblyMembership(@ApiParam(name = "id", value = "Assembly ID") Long id) {

        try {
            return ok(Json.toJson(AssembliesDelegate.createMembership(id)));
        } catch (MembershipCreationException e) {
            e.printStackTrace();
            return internalServerError(Json.toJson(TransferResponseStatus.errorMessage(
                    Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
                            e.getMessage()), "")));

        } finally {
			Ebean.endTransaction();
		}
    }
    @ApiOperation(httpMethod = "PUT", response = CampaignTransfer.class, produces = "application/json", value = "Change assembly status to PUBLISHED", notes = "Only for COORDINATORS.")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)

    public static Result publish(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        try {
            Assembly assembly = Assembly.read(aid);
            if (assembly == null) {
				return notFound(Json.toJson(TransferResponseStatus.noDataMessage("Assembly with id = '"+aid+"' does not exists", "")));
            }
            return ok(Json.toJson(AssembliesDelegate.publish(aid)));
        } catch (Exception e) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
                    "There was an internal error: " + e.getMessage()));
            e.printStackTrace();
            return internalServerError(Json.toJson(responseBody));
        }
    }



    @ApiOperation(httpMethod = "PUT", response = AssemblyTransfer.class, produces = "application/json", value = "Create assembly resources")
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Assembly Object", value = "Body of Assembly in JSON", required = true, dataType = "models.Assembly", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({ @Group(GlobalData.USER_ROLE) })
    public static Result createAssemblyResources(@ApiParam(name = "id", value = "Assembly ID") Long id) {
        final Form<AssemblyTransfer> newAssemblyForm = ASSEMBLY_TRANSFER_FORM.bindFromRequest();
        // Check for errors in received data
        if (newAssemblyForm.hasErrors()) {
            return badRequest(Json.toJson(TransferResponseStatus.badMessage(
                    Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
                            newAssemblyForm.errorsAsJson()), newAssemblyForm
                            .errorsAsJson().toString())));
        } else {
            AssemblyTransfer newAssembly = newAssemblyForm.get();
            newAssembly.setAssemblyId(id);
            return ok(Json.toJson(AssembliesDelegate.createResources(newAssembly)));

        }
    }

	/**
	 * POST      /api/assembly
	 *
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = AssemblyTransfer.class, produces = "application/json", value = "Create a new assembly", notes="The templates will be used to import all the resources from a list of existing assembly to the new")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Assembly Object", value = "Body of Assembly in JSON", required = true, dataType = "models.Assembly", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Restrict({ @Group(GlobalData.USER_ROLE) })
	public static Result createAssembly(@ApiParam(name="templates", value="List of assembly ids (separated by comma) to use as template for the current assembly") String templates,
										@ApiParam(name="invitations", value="Send invitations if true") String invitations) {
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
					AssemblyTransfer created = AssembliesDelegate.create(newAssembly, creator, templates, null, invitations);
					Ebean.commitTransaction();
					try {
						NotificationsDelegate.createNotificationEventsByType(
								ResourceSpaceTypes.ASSEMBLY.toString(), created.getUuid());
					} catch (ConfigurationException e) {
						Logger.error("Configuration error when creating events for notifications: " + LogActions.exceptionStackTraceToString(e));
					} catch (Exception e) {
						Logger.error("Error when creating events for notifications: " + LogActions.exceptionStackTraceToString(e));						
					}
					return ok(Json.toJson(created));
				} catch (Exception e) {
					Logger.error(e.getStackTrace().toString());
					e.printStackTrace();
					return internalServerError(Json.toJson(TransferResponseStatus.errorMessage(
							Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
									e.getMessage()), "")));
				} finally {
					Ebean.endTransaction();
				}
			} else {
				return internalServerError(Json
						.toJson(TransferResponseStatus.errorMessage(
								"An assembly with the same title already exists: "
										+ "'" + newAssembly.getName() + "'", "")));
			}
		}
	}

	/**
	 * POST      /api/assembly/:id/assembly
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = AssemblyTransfer.class, produces = "application/json", value = "Create a new assembly in a principal assembly")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Errors in the form", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Assembly Object", value = "Body of Assembly in JSON", required = true, dataType = "models.Assembly", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result createAssemblyInAssembly(
			@ApiParam(name="id", value="Id of the principal assembly under which to create the new") Long id, 
			@ApiParam(name="templates", value="List of assembly ids (separated by comma) to use as template for the current assembly") String templates,
			@ApiParam(name="invitations", value="Send invitations if true") String invitations) {
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
			// Sub-Assemblies can have repeated names because they will not have a domain name associated 
			// Assembly a = Assembly.findByName(newAssembly.getName());
			
			// Read principal assembly
			Assembly a = Assembly.read(id);
			if (a!=null && a.getPrincipalAssembly()) {
				Ebean.beginTransaction();
				try {
					AssemblyTransfer created = AssembliesDelegate.create(newAssembly, creator, templates, a, invitations);
					Ebean.commitTransaction();
					try {
						NotificationsDelegate.createNotificationEventsByType(
								ResourceSpaceTypes.ASSEMBLY.toString(), created.getUuid());
					} catch (ConfigurationException e) {
						Logger.error("Configuration error when creating events for notifications: " + LogActions.exceptionStackTraceToString(e));
					} catch (Exception e) {
						Logger.error("Error when creating events for notifications: " + LogActions.exceptionStackTraceToString(e));						
					}
					return ok(Json.toJson(created));
				} catch (Exception e) {
					Logger.error(LogActions.exceptionStackTraceToString(e));
					return internalServerError(Json.toJson(TransferResponseStatus.errorMessage(
							Messages.get(GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
									e.getMessage()), "")));
				} finally {
					Ebean.endTransaction();
				}
			} else {
				return badRequest(Json.toJson("Assembly " + id + " is not a principal assembly"));
			}
		}
	}

	/**
	 * GET       /api/assembly/:id
	 *
	 * @param id
	 * @return
	 */
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

	/**
	 * GET       /api/assembly/name/:shortname
	 *
	 * @param shortname
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Read Assembly by Shortname", notes="Only for MEMBERS of the assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	public static Result findAssemblyByShortname(@ApiParam(name = "shortname", value = "Assembly Shortname") String shortname) {
		Assembly a = Assembly.findByShortName(shortname);
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String result = mapper.writerWithView(Views.Public.class)
					.writeValueAsString(a);

			Content ret = new Content() {
				@Override public String body() { return result; }
				@Override public String contentType() { return "application/json"; }
			};

			return a != null ? Results.ok(ret) : notFound(Json.toJson(
					new TransferResponseStatus(ResponseStatus.NODATA,
					"No assembly with Shortname = " + shortname)));
		}catch(Exception e){
			return badRequest(Json.toJson(Json
					.toJson(new TransferResponseStatus("Error processing request"))));
		}
	}

	/**
	 * GET       /api/assembly/name/:shortname
	 *
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", value = "Read Assembly by its Universal ID")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	public static Result findAssemblyByUUID(@ApiParam(name = "uuid", value = "Assembly UUID") String uuid) {
		Assembly a = Assembly.readByUUID(UUID.fromString(uuid));
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
			String result = mapper.writerWithView(Views.Public.class)
					.writeValueAsString(a);
			
			Content ret = new Content() {
				@Override public String body() { return result; }
				@Override public String contentType() { return "application/json"; }
			};

			return a != null ? Results.ok(ret) : notFound(Json.toJson(
					new TransferResponseStatus(ResponseStatus.NODATA,
					"No assembly with Shortname = " + uuid)));
		}catch(Exception e){
			return badRequest(Json.toJson(Json
					.toJson(new TransferResponseStatus("Error processing request"))));
		}
	}

	/**
	 * DELETE    /api/assembly/:id
	 *
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete Assembly by ID", notes = "Only for COORDINATORS")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result deleteAssembly(@ApiParam(name = "id", value = "Assembly ID") Long id) {
		Assembly.delete(id);
		return ok();
	}

	/**
	 * PUT       /api/assembly/:id
	 *
	 * @param id
	 * @return
	 */
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
			Ebean.beginTransaction();
			
			Assembly newAssembly = null;
			TransferResponseStatus responseBody = new TransferResponseStatus();
			try {
				Assembly updatedAssembly = newAssemblyForm.get();
				newAssembly = Assembly.readAndUpdate(updatedAssembly,id);

				AssemblyProfile profile = newAssembly.getProfile();
				AssemblyProfile profileDB = AssemblyProfile.findByAssembly(newAssembly.getUuid());
				profile.setAssemblyProfileId(profileDB.getAssemblyProfileId());
				profile.update();
				// TODO: return URL of the new group
				Logger.info("Updating assembly");
				Logger.debug("=> " + newAssemblyForm.toString());
				
				newAssembly.update();

				NotificationsDelegate.createNotificationEventsByType(
						ResourceSpaceTypes.ASSEMBLY.toString(), newAssembly.getUuid());
				Ebean.commitTransaction();

			} catch (Exception e) {
				Logger.error("Error updating assembly: "+LogActions.exceptionStackTraceToString(e));
				responseBody.setStatusMessage(Messages.get(
						GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
						newAssembly.getName()));
				return internalServerError(Json.toJson(responseBody));
			} finally {
				Ebean.endTransaction();
			}

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
	 * POST      /api/assembly/:id/membership/:type
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
			Result aRet =  Memberships.createMemberShip(requestor, "assembly", newMembership,id);

			F.Promise.promise(() -> {

				Boolean coordinator = false;
				for (SecurityRole role : requestor.getRoles()) {
					if (role.getName().equals("COORDINATOR")) {
						coordinator = true;
					}
				}
				Assembly a = Assembly.read(id);
				if (!coordinator) {
					HashMap<String, Boolean> ignoredEvents = new HashMap<String, Boolean>();
					ignoredEvents.put(EventKeys.NEW_CONTRIBUTION_FEEDBACK, true);
					ignoredEvents.put(EventKeys.NEW_CONTRIBUTION_FEEDBACK_FLAG, true);
					ignoredEvents.put(EventKeys.UPDATED_ASSEMBLY_CONFIGS, true);
					ignoredEvents.put(EventKeys.UPDATED_WORKING_GROUP_CONFIGS, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_IDEA, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_PROPOSAL, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_DISCUSSION, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_COMMENT, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_NOTE, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_FORUM_POST, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_FEEDBACK, true);
					ignoredEvents.put(EventKeys.UPDATED_CONTRIBUTION_HISTORY, true);
					ignoredEvents.put(EventKeys.MEMBER_JOINED, true);
					ignoredEvents.put(EventKeys.DELETED_CONTRIBUTION, true);
					ignoredEvents.put(EventKeys.MODERATED_CONTRIBUTION, true);
					if (Subscription.findByUserIdAndSpaceId(requestor, a.getResourcesResourceSpaceUUID().toString()).isEmpty()) {
						Subscription sub = new Subscription();
						sub.setUserId(requestor.getUuid().toString());
						sub.setSpaceType(ResourceSpaceTypes.ASSEMBLY);
						sub.setSpaceId(a.getResourcesResourceSpaceUUID().toString());
						sub.setSubscriptionType(SubscriptionTypes.REGULAR);
						sub.setIgnoredEvents(ignoredEvents);
						sub.insert();
						try {
							NotificationsDelegate.subscribeToEvent(sub);
						} catch (ConfigurationException e) {
							Logger.error("Error notification the subscription creation", e);
							e.printStackTrace();
						}
					}
					try {
						List<Campaign> campaigns = a.getResources().getCampaignsFilteredByStatus("ongoing");
						for (Campaign c : campaigns) {
							if (!Subscription.findByUserIdAndSpaceId(requestor, c.getResources().getUuid().toString()).isEmpty()) {
								continue;
							}
							Config config = Config.findByUser(requestor.getUuid(), "notifications.preference." +
									"contributed-contributions.auto-subscription");
							if (config != null
									&& config.getValue().equals("true")) {
								Subscription sub = new Subscription();
								sub.setUserId(requestor.getUuid().toString());
								sub.setSpaceType(ResourceSpaceTypes.CAMPAIGN);
								sub.setSpaceId(c.getResourceSpaceUUID());
								sub.setSubscriptionType(SubscriptionTypes.NEWSLETTER);
								HashMap<String, Boolean> services = new HashMap<>();
								services.put("facebook-messenger", true);
								sub.setDisabledServices(services);
								sub.insert();
								try {
									NotificationsDelegate.subscribeToEvent(sub);
								} catch (ConfigurationException e) {
									Logger.error("Error notification the subscription creation", e);
									e.printStackTrace();
								}
							}
							Subscription sub = new Subscription();
							sub.setUserId(requestor.getUuid().toString());
							sub.setSpaceType(ResourceSpaceTypes.CAMPAIGN);
							sub.setSpaceId(c.getResourceSpaceUUID());
							sub.setSubscriptionType(SubscriptionTypes.REGULAR);
							sub.setIgnoredEvents(ignoredEvents);
							HashMap<String, Boolean> services = new HashMap<>();
							services.put("facebook-messenger", true);
							services.put("email", true);
							sub.setDisabledServices(services);
							sub.insert();
							try {
								NotificationsDelegate.subscribeToEvent(sub);
							} catch (ConfigurationException e) {
								Logger.error("Error notification the subscription creation", e);
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						Logger.error("Error in subscription creation", e);
						e.printStackTrace();
					}
				}
				return Optional.ofNullable(null);
			});
			return aRet;
		}
	}

	/**
	 * GET       /api/assembly/:id/membership/:status
	 *
	 * @param id
	 * @param status
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Membership.class, responseContainer="List", produces = "application/json", value = "Get Assembly Memberships by ID and status", notes = "Only for MEMBERS of the assembly")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "No assembly found", response = TransferResponseStatus.class) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result listMembershipsWithStatus(
			@ApiParam(name = "id", value = "Assembly ID") Long id,
			@ApiParam(name = "status", value = "Status of membership invitation or request") String status,
			@ApiParam(name = "ldap", value = "Include LDAP users") String ldap,
			@ApiParam(name = "ldapsearch", value = "Search string to apply to potential members from external LDAP server") String ldapsearch,
			@ApiParam(name = "query", value = "Name query on memberships") String query) {

		Map<String, List> aRet = new HashMap<>();
		List<Membership> m = MembershipAssembly.findByAssemblyIdStatusAndNameQuery(id,status, query);
		aRet.put("members", m);

		if(ldap.equals("true")) {
			Assembly assembly = Assembly.read(id);
			try {
				aRet.put("ldap", LdapAuthProvider.getMemberLdapUsers(assembly, ldapsearch));
			} catch (Exception e) {
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage("Error: "+e.getMessage());
				return internalServerError(Json.toJson(responseBody));
			}
		}

		if (!aRet.get("members").isEmpty() || (ldap.equals("true") && !aRet.get("ldap").isEmpty()))
			return ok(Json.toJson(aRet));
		return notFound(Json.toJson(new TransferResponseStatus(
				"No memberships with status '" + status + "' in Assembly '"
						+ id + "'")));
	}

	/**
	 * GET       /api/assembly/:aid/user/:uid
	 *
	 * @param aid
	 * @param userId
	 * @return
	 */
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

	/**
	 * POST      /api/assembly/:id/invitations
	 *
	 * @param id
	 * @return
	 */
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
					TransferResponseStatus responseBody = new TransferResponseStatus();
					responseBody.setStatusMessage(Messages.get(
							GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR,
							"Error: "+e.getMessage()));
					return internalServerError(Json.toJson(responseBody));
				} finally {
					Ebean.endTransaction();
				}
			}
			return ok(Json.toJson(results));
		}
	}

	/**
	 * PUT       /api/assembly/:uuid/profile
	 *
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod="PUT", response = AssemblyProfile.class, produces = "application/json", value = "Update the profile of the Assembly")
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

	/**
	 * GET       /api/assembly/:uuid/profile
	 *
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod="GET", response = AssemblyProfile.class, produces = "application/json", value = "Read the profile of an Assembly")
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

	/**
	 * GET       /api/assembly/:uuid/theme
	 *
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod="GET", response = Theme.class, responseContainer = "List", produces = "application/json", value = "Get themes of an assembly by its UUID")
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
	 * PUT       /api/assembly/:aid/contribution/template/:rid
	 * Confirms a Resource CAMPAIGN_TEMPLATE
	 * @param aid
	 * @param rid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = Resource.class, value = "Confirm Contribution Template")
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
	 * GET       /api/assembly/:aid/contribution/template
	 * Get list of available campaign templates in the assembly
	 *
	 * @param aid
	 * @return
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
	 * DELETE    /api/assembly/:aid/contribution/template/:rid
	 * Delete campaign by ID
	 * @param aid
	 * @param resourceId
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete contribution template from assembly", notes="Only for COORDINATOS of assembly")
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

	/**
	 * GET       /api/assembly/public/:uuid
	 *
	 * @param uuid
	 * @return
	 */
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

	// TODO: move the following endpoints to the Resource Space Controller
	
	/**
	 * PUT      /api/space/:sid/organization
	 *
	 * @param sid
	 * @return
	 */
	@ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Update organizations in a resource space")
	@ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "Organization object", value = "Body of Organization in JSON", required = true, dataType = "models.Organization", paramType = "body"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	public static Result updateOrganizationsInSpace(
			@ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
		final Form<Organization> updatedForm = ORGANIZATION_FORM.bindFromRequest();
		if (updatedForm.hasErrors()) {
			TransferResponseStatus responseBody = new TransferResponseStatus();
			responseBody.setStatusMessage(Messages.get("Organization data error",
					updatedForm.errorsAsJson()));
			return badRequest(Json.toJson(responseBody));
		} else {
			User author = User.findByAuthUserIdentity(PlayAuthenticate
					.getUser(session()));
			ResourceSpace rs = ResourceSpace.read(sid);
			if(rs==null)
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"No Resource Space found with id: " + sid)));
			try {
				Organization organization = updatedForm.get();
				if(organization.getOrganizationId()!=null){
					organization = organization.read(organization.getOrganizationId());
				}
				rs.getOrganizations().add(organization);
				rs.update();
			} catch (Exception e) {
				return internalServerError(Json
						.toJson(new TransferResponseStatus(
								ResponseStatus.SERVERERROR,
								"Error when assigning Organizations to Resource Space: " + e.toString())));
			}
			return ok("OK");
		}
	}

	/**
	 * GET      /api/space/:sid/organization
	 *
	 * @param sid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Organization.class, responseContainer = "List", produces = "application/json", value = "Get organizations in a resource space")
	@ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	@SubjectPresent
	public static Result findOrganizationsInSpace(
			@ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
		ResourceSpace rs = ResourceSpace.read(sid);
		if(rs==null)
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"No Resource Space found with id: " + sid)));
		return ok(Json.toJson(rs.getOrganizations()));
	}
	
	/**
	 * GET      /api/space/:uuid/organization
	 *
	 * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Organization.class, responseContainer = "List", produces = "application/json", value = "Get organizations in a resource space by the UUID of the space")
	@ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
	public static Result findOrganizationsInSpaceByUUID(
			@ApiParam(name = "uuid", value = "Resource Space UUID") UUID uuid) {
		ResourceSpace rs = ResourceSpace.readByUUID(uuid);
		if(rs==null)
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"No Resource Space found with id: " + uuid)));
		return ok(Json.toJson(rs.getOrganizations()));
	}

	/**
	 * DELETE      /api/space/:sid/organization/:id
	 *
	 * @param sid
	 * @param id
	 * @return
	 */
	@ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete a organization from resource space")
	@ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	public static Result deleteOrganizationFromSpace(
			@ApiParam(name = "sid", value = "Resource Space ID") Long sid,
			@ApiParam(name = "id", value = "Organization ID") Long id) {
		User author = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		ResourceSpace rs = ResourceSpace.read(sid);
		if(rs==null)
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"No Resource Space found with id: " + sid)));
		Organization org = Organization.read(id);
		if(org==null)
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"No Organization found with id: " + sid)));
		rs.getOrganizations().remove(org);
		try {
			rs.update();
		} catch (Exception e) {
			return internalServerError(Json
					.toJson(new TransferResponseStatus(
							ResponseStatus.SERVERERROR,
							"Error when deleting Organization from Resource Space: " + e.toString())));
		}
		return ok("OK");
	}

	private static BufferedReader br;
	private static Random random = new Random();
	private static SecureRandom secureRandom = new SecureRandom();

	private static String[] seedPasswords = { "sip", "hop", "vouch", "value", "walk",
			"act", "stray", "call", "curl", "stay", "yell", "tack",
			"sing", "yawn", "open", "read", "edit",
			"spell", "fix", "love", "knit", "like",
			"praise", "hide", "clip" };

	/**
	 * POST      /assembly/:aid/member
	 *
	 * @param aid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = String.class, produces = "application/json", value = "Upload users to assembly")
	@ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result uploadAssemblyUsers(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
												@ApiParam(name = "send_invitations", value = "Send invitations if true") String sendInvitations) {

		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
		User sessionUser = User.findByAuthUserIdentity(PlayAuthenticate
				.getUser(session()));
		Assembly assembly = null;
		ObjectMapper om = new ObjectMapper();
		ArrayNode an = om.createArrayNode();

		if (uploadFilePart != null) {
			try {
				assembly = Assembly.read(aid);
				// Read CSV with list of users
				br = new BufferedReader(new FileReader(uploadFilePart.getFile()));
				String cvsSplitBy = ",";
				String line = "";
				int  lineNumber = 0;
				while ((line = br.readLine()) != null) {
                    if (lineNumber==0) {
                        lineNumber++; // ignore header
                    } else {
                        String[] cell = line.split(cvsSplitBy);
                        if (!line.contains("WebKitFormBoundary") && cell.length == 5) {
                            User u = User.findByEmail(cell[0]);
                            // Create account if not exists
                            if (u == null) {
                                u = createNewAssemblyUser(cell, om, an);
                            }

                            // If send_invitations==TRUE, send an invitation email to the corresponding email.
                            // Else create membership
                            if (sendInvitations.equals("true")) {
                                if (!Membership.checkIfExistsByEmailAndId(u.getEmail(), assembly.getAssemblyId(), MembershipTypes.ASSEMBLY)) {
                                    //CREATE invitation and send mail
                                    InvitationTransfer invitation = createAndSendInvitation(u, sessionUser, assembly, "ASSEMBLY", cell[4]);
                                }
                            } else {
                                createMembership(u, sessionUser, assembly, "ASSEMBLY", cell[4]);
                            }
                        }
                    }
				}
			} catch(EntityNotFoundException e){
				e.printStackTrace();
                Logger.error(e.getLocalizedMessage());
                return internalServerError("The assembly doesn't exist");
			} catch(Exception e){
				e.printStackTrace();
                Logger.error(e.getLocalizedMessage());
                return internalServerError("Error reading the CSV file");
			}
		}

		return ok(Json.toJson(an));
	}


	/**
	 * GET      /assembly/:aid/id?type=[campaign, group, contribution, resource, theme]&uuid=resource_uuid getIdOfResourceInAssembly
	 *
	 * @param aid
     * @param type
     * @param uuid
	 * @return
	 */
	@ApiOperation(httpMethod = "GET", response = Long.class, produces = "application/json", value = "Get numerical ID of reource in an assembly")
	@ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	@Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result getIdOfResourceInAssembly(
			@ApiParam(name = "aid", value = "Assembly ID") Long aid,
			@ApiParam(name = "type", value = "Type of entity") String type,
			@ApiParam(name = "uuid", value = "UUID of entity") String uuid) {

		Long id = new Long(0);

		switch (type.toLowerCase()) {
			case "assembly":
				id = Assembly.getIdByUUID(UUID.fromString(uuid));
				break;
			case "campaign":
				id = Campaign.getIdByUUID(UUID.fromString(uuid));
				break;
			case "contribution":
				id = Contribution.getIdByUUID(UUID.fromString(uuid));
				break;
			case "group":
				id = WorkingGroup.getIdByUUID(UUID.fromString(uuid));
				break;
			case "resource":
				id = Resource.getIdByUUID(UUID.fromString(uuid));
				break;
			default:
				break;
		}

		if (id == 0) {
			TransferResponseStatus r = new TransferResponseStatus();
			r.setStatusMessage("Entity with that UUID does not exist under this assembly");
			r.setResponseStatus(ResponseStatus.NODATA);
			return notFound(Json.toJson(r));
		}

        TransferResponseStatus r = new TransferResponseStatus();
        r.setStatusMessage("Found ID");
        r.setResponseStatus(ResponseStatus.OK);
        r.setNewResourceId(id);
		return ok(Json.toJson(r));
	}


	/**
	 * POST      /assembly/:aid/campaign/:cid/group/:gid/member
	 *
	 * @param aid
	 * @return
	 */
	@ApiOperation(httpMethod = "POST", response = String.class, produces = "application/json", value = "Upload users to assembly")
	@ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
	@ApiImplicitParams({
			@ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
			@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
	@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
	public static Result uploadGroupUsers(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
													 @ApiParam(name = "cid", value = "Campaign ID") Long cid,
													 @ApiParam(name = "gid", value = "Working Group ID") Long gid,
													 @ApiParam(name = "send_invitations", value = "Send invitations if true") String sendInvitations) {

        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
        User sessionUser = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        WorkingGroup wg = null;
        Assembly assembly = null;
        ObjectMapper om = new ObjectMapper();
        ArrayNode an = om.createArrayNode();
        if (uploadFilePart != null) {
            try {
                Ebean.beginTransaction();
                wg = WorkingGroup.read(gid);
                assembly = Assembly.read(aid);
                // Read CSV with list of users
                br = new BufferedReader(new FileReader(uploadFilePart.getFile()));
                String cvsSplitBy = ",";
                String line = "";
                int lineNumber = 0;
                while ((line = br.readLine()) != null) {
                    if (lineNumber==0) {
                        lineNumber++; // ignore header
                    } else {
                        String[] cell = line.split(cvsSplitBy);
                        if (!line.contains("WebKitFormBoundary") && cell.length == 5) {
                            User u = User.findByEmail(cell[0]);
                            // Create account if not exists
                            if (u == null) {
                                u = createNewAssemblyUser(cell, om, an);
                            }

                            // If send_invitations==TRUE, send an invitation email to the corresponding email.
                            // Else create membership
                            if (sendInvitations.equals("true")) {
                                if (!Membership.checkIfExistsByEmailAndId(u.getEmail(), assembly.getAssemblyId(), MembershipTypes.ASSEMBLY)) {
                                    //CREATE invitation and send mail
                                    InvitationTransfer invitation = createAndSendInvitation(u, sessionUser, wg, "GROUP", cell[4]);
                                }
                            } else {
                                // Create membership (with the group gid)
                                createMembership(u, sessionUser, wg, "GROUP", cell[4]);
                                // Also with the assembly aid
                                createMembership(u, sessionUser, assembly, "ASSEMBLY", "MEMBER");
                            }
                        }
                    }
                }
                Ebean.commitTransaction();
            } catch (EntityNotFoundException e) {
                e.printStackTrace();
                Logger.error(e.getLocalizedMessage());
                return internalServerError("The working group doesn't exist");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.error(e.getLocalizedMessage());
                return internalServerError("Error reading the CSV file");
            } finally {
				Ebean.endTransaction();
			}
        }
		return ok(Json.toJson(an));
	}

	private static User createNewAssemblyUser(String[] cell, ObjectMapper om, ArrayNode an) {
        User u = new User();
        u.setEmail(cell[0]);
        u.setName(cell[1] + " " + cell[2]);
        u.setUsername(u.getEmail()); // email
        u.setLanguage(cell[3]);
        // For users without account: generate pass (see the test to generate passwords) and return this in the response

        Integer passIndex = random.nextInt(25);
//        String pass = seedPasswords[passIndex] + new BigInteger(20, secureRandom).toString(32);
        String pass = "@ppcivist";
        MyLoginUsernamePasswordAuthUser authUser =
                new MyLoginUsernamePasswordAuthUser(pass, u.getEmail());
        // Hash a password for the first time
        String hashed = authUser.getHashedPassword();
        u.save();

        LinkedAccount la = new LinkedAccount();
        la.setUser(u);
        la.setProviderUserId(hashed);
        la.setProviderKey("password");
        la.save();

        UserProfile up = new UserProfile(null, null, cell[1], null, cell[2],
                null, null);
        up.setUser(u);
        up.save();

        ObjectNode on = om.createObjectNode();
        on.put("username", u.getEmail());
        on.put("password", pass);
        an.add(on);
        u.refresh();
        return u;
    }

    private static InvitationTransfer createAndSendInvitation(User u, User creator, AppCivistBaseModel target, String targetType, String role ) throws MembershipCreationException {
        //invitation created and send mail
        InvitationTransfer invitation = new InvitationTransfer();
        invitation.setEmail(u.getEmail());
        invitation.setInvitationEmail(u.getEmail()); // change
        invitation.setTargetType(targetType);
        Assembly a = null;
        WorkingGroup wg = null;

        if (role.toUpperCase().equals("COORDINATOR")) {
            invitation.setCoordinator(true);
            invitation.setModerator(false);
        } else if (role.toUpperCase().equals("MODERATOR")) {
            invitation.setCoordinator(false);
            invitation.setModerator(true);
        } else {
            invitation.setCoordinator(false);
            invitation.setModerator(false);
        }
        if (targetType.equals("ASSEMBLY")) {
            a = ((Assembly) target);
            invitation.setTargetId(a.getAssemblyId());
            MembershipInvitation.create(invitation, creator, a);
        } else {
            wg = ((WorkingGroup) target);
            invitation.setTargetId(wg.getGroupId());
            MembershipInvitation.create(invitation, creator, wg);
        }
        return invitation;
    }

    private static Membership createMembership(User u, User creator, AppCivistBaseModel target, String targetType, String role) {
	    List<SecurityRole> roles = new ArrayList<>();
        roles.add(SecurityRole.findByName("MEMBER")); // Member role
        if (role.equals("COORDINATOR")) {
            roles.add(SecurityRole.findByName("COORDINATOR"));
        } else if (role.equals("MODERATOR")) {
            roles.add(SecurityRole.findByName("COORDINATOR"));
        }
        if (targetType.equals("ASSEMBLY")) {
            Assembly a = ((Assembly) target);
            MembershipAssembly ma = new MembershipAssembly();
            ma.setUser(u);
            ma.setCreator(creator);
            ma.setMembershipType(targetType.toUpperCase());
            ma.setAssembly(a);
            ma.setStatus(MembershipStatus.ACCEPTED);
            ma.setRoles(roles);
            ma.save();
            return ma;
        } else {
            WorkingGroup wg = ((WorkingGroup) target);
            MembershipGroup mG = new MembershipGroup();
            mG.setUser(u);
            mG.setCreator(creator);
            mG.setMembershipType(targetType.toUpperCase());
            mG.setWorkingGroup(wg);
            mG.setStatus(MembershipStatus.ACCEPTED);
            mG.save();
            return mG;
        }
    }


}
