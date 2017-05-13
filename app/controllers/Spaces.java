package controllers;


import static play.data.Form.form;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import delegates.ContributionsDelegate;
import delegates.NotificationsDelegate;
import enums.*;
import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import models.*;
import models.misc.ThemeStats;
import models.misc.Views;
import models.transfer.BallotTransfer;
import models.transfer.TransferResponseStatus;
import org.apache.commons.lang3.RandomUtils;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feth.play.module.pa.PlayAuthenticate;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.LogActions;

@Api(value = "09 space: resource space management", description = "Resource space management")
@With(Headers.class)
public class Spaces extends Controller {

    public static final Form<CustomFieldDefinition> CUSTOM_FIELD_DEFINITION_FORM = form(CustomFieldDefinition.class);

    public static final Form<CustomFieldValue> CUSTOM_FIELD_VALUE_FORM = form(CustomFieldValue.class);

    public static final Form<BallotTransfer> BALLOT_TRANSFER_FORM = form(BallotTransfer.class);

    public static final Form<Ballot> BALLOT_FORM = form(Ballot.class);

    public static final Form<ResourceSpace> RESOURCE_SPACE_FORM = form(ResourceSpace.class);

    public static final Form<Config> CONFIG_FORM = form(Config.class);

    public static final Form<ComponentMilestone> COMPONENT_MILESTONE_FORM = form(ComponentMilestone.class);

    public static final Form<Component> COMPONENT_FORM = form(Component.class);

    public static final Form<Campaign> CAMPAIGN_FORM = form(Campaign.class);

    public static final Form<Assembly> ASSEMBLY_FORM = form(Assembly.class);

    public static final Form<WorkingGroup> WORKING_GROUP_FORM = form(WorkingGroup.class);

    public static final Form<Theme> THEME_FORM = form(Theme.class);

    public static final Form<Resource> RESOURCE_FORM = form(Resource.class);

    public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);

    @ApiOperation(produces="application/json", value="Simple search of resource space", httpMethod="GET")
    public static Result getSpace(Long sid) {
        ResourceSpace rs = ResourceSpace.read(sid);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("type", rs.getType().toString());

        String name = "";
        Long id = null;
        if (rs.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
            name = rs.getAssemblyResources().getName();
            id = rs.getAssemblyResources().getAssemblyId();
        } else if (rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
            name = rs.getWorkingGroupResources().getName();
            id = rs.getWorkingGroupResources().getGroupId();
        } else if (rs.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
            name = rs.getCampaign().getTitle();
            id = rs.getCampaign().getCampaignId();
        }
        json.put("name", name);
        json.put("id", id);

        return Results.ok(json);
    }

    @ApiOperation(produces="application/json", value="Simple search of resource space", httpMethod="GET")
    public static Result getPublicSpace(UUID uuid) {
        try{
            ResourceSpace rs = ResourceSpace.readByUUID(uuid);

            String name = "";
            if (rs.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
                name = rs.getAssemblyResources().getName();
            } else if (rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
                name = rs.getWorkingGroupResources().getName();
            } else if (rs.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                name = rs.getCampaign().getTitle();
            }
            rs.setName(name);

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(rs);

            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };

            return Results.ok(ret);

        }catch(Exception e){
            Logger.error("Error processing space's public view", e);
            return internalServerError(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }

    }

    /**
     * GET       /api/space/:sid/field
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CustomFieldDefinition.class, produces = "application/json", responseContainer = "List", value = "List of custom field definition in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result findSpaceFields(@ApiParam(name = "sid", value = "Space ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<CustomFieldDefinition> customFieldDefinitions;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            customFieldDefinitions = resourceSpace.getCustomFieldDefinitions();
        }
        return ok(Json.toJson(customFieldDefinitions));
    }


    /**
     * GET       /api/space/:sid/commentcount
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ObjectNode.class, produces = "application/json", responseContainer = "List", value = "Comment count of a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    // @ApiImplicitParams({
    //         @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result getCommentCount(@ApiParam(name = "sid", value = "Space ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        Integer commentCount = 0;
        List<Contribution> discussionList = resourceSpace.getContributionsFilteredByType(ContributionTypes.DISCUSSION);
        List<Contribution> commentList = resourceSpace.getContributionsFilteredByType(ContributionTypes.COMMENT);
        commentCount = discussionList.size() + commentList.size();
        ObjectNode result = Json.newObject();
        result.put("resourceId", sid);
        result.put("counter", commentCount);
        return ok(result);
    }
    
    /**
     * GET       /api/space/:uuid/commentcount
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ObjectNode.class, produces = "application/json", responseContainer = "List", value = "Comment count of a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    // @ApiImplicitParams({
    //         @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result getCommentCountPublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        Integer commentCount = 0;
        List<Contribution> discussionList = resourceSpace.getContributionsFilteredByType(ContributionTypes.DISCUSSION);
        List<Contribution> commentList = resourceSpace.getContributionsFilteredByType(ContributionTypes.COMMENT);
        commentCount = discussionList.size() + commentList.size();
        ObjectNode result = Json.newObject();
        result.put("resourceUUID", uuid.toString());
        result.put("counter", commentCount);
        return ok(result);
    }

    /**
     * POST       /api/space/:sid/field
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = CustomFieldDefinition.class, produces = "application/json", value = "Create a custom field definition in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CustomFieldDefinition object", value = "Body of CustomFieldDefinition in JSON", required = true, dataType = "models.CustomFieldDefinition", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result createSpaceFields(@ApiParam(name = "sid", value = "Space ID") Long sid) {
        final Form<CustomFieldDefinition> newCustomFieldDefinitionForm = CUSTOM_FIELD_DEFINITION_FORM
                .bindFromRequest();
        User creator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            if (newCustomFieldDefinitionForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "The form has the following error: "+
                                newCustomFieldDefinitionForm.errorsAsJson()));
                return badRequest(Json.toJson(responseBody));
            } else {
                CustomFieldDefinition newCustomFieldDefinition = newCustomFieldDefinitionForm.get();
                newCustomFieldDefinition.setContextUserId(creator.getUserId());
                newCustomFieldDefinition = CustomFieldDefinition.create(newCustomFieldDefinition);
                resourceSpace.getCustomFieldDefinitions().add(newCustomFieldDefinition);
                resourceSpace.update();
                return ok(Json.toJson(newCustomFieldDefinition));
            }
        }
    }

    /**
     * DELETE       /api/space/:sid/field/:cfid
     *
     * @param sid
     * @param cfid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = CustomFieldDefinition.class, produces = "application/json", responseContainer = "List", value = "Delete a custom field definition from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceFields(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                           @ApiParam(name = "cfid", value = "Custom field definition ID") Long cfid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<CustomFieldDefinition> customFieldDefinitions;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            CustomFieldDefinition customFieldDefinition = CustomFieldDefinition.read(cfid);
            if (customFieldDefinition == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No custom field definition found with id "+cfid)));
            }
            resourceSpace.getCustomFieldDefinitions().remove(customFieldDefinition);
            resourceSpace.update();
            resourceSpace.refresh();
            customFieldDefinition.softRemove();
            customFieldDefinitions = resourceSpace.getCustomFieldDefinitions();
        }
        return ok(Json.toJson(customFieldDefinitions));
    }

    /**
     * PUT       /api/space/:sid/field/:cfid
     *
     * @param sid
     * @param cfid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = CustomFieldDefinition.class, produces = "application/json", value = "Update a custom field definition in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CustomFieldDefinition object", value = "Body of CustomFieldDefinition in JSON", required = true, dataType = "models.CustomFieldDefinition", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result updateSpaceFields(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                           @ApiParam(name = "cfid", value = "Custom field definition ID") Long cfid) {
        final Form<CustomFieldDefinition> newCustomFieldDefinitionForm = CUSTOM_FIELD_DEFINITION_FORM
                .bindFromRequest();
        User creator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            if (newCustomFieldDefinitionForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "The form has the following error: "+
                                newCustomFieldDefinitionForm.errorsAsJson()));
                return badRequest(Json.toJson(responseBody));
            } else {
                CustomFieldDefinition customFieldDefinition = CustomFieldDefinition.read(cfid);
                if (customFieldDefinition == null) {
                    return notFound(Json
                            .toJson(new TransferResponseStatus("No custom field definition found with id "+cfid)));
                }
                CustomFieldDefinition newCustomFieldDefinition = newCustomFieldDefinitionForm.get();
                newCustomFieldDefinition.setCustomFieldDefinitionId(customFieldDefinition.getCustomFieldDefinitionId());
                newCustomFieldDefinition.setContextUserId(creator.getUserId());
                newCustomFieldDefinition.update();
                resourceSpace.getCustomFieldDefinitions().add(newCustomFieldDefinition);
                resourceSpace.update();
                return ok(Json.toJson(newCustomFieldDefinition));
            }
        }
    }

    /**
     * GET       /api/space/:sid/fieldvalue
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CustomFieldValue.class, produces = "application/json", responseContainer = "List", value = "List of custom field value in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result findSpaceFieldsValue(@ApiParam(name = "sid", value = "Space ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            List<CustomFieldValue> customFieldValues = resourceSpace.getCustomFieldValues();
            return ok(Json.toJson(customFieldValues));   
        }
    }

    /**
     * GET       /api/space/:uuid/fieldvalue/public
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CustomFieldValue.class, produces = "application/json", responseContainer = "List", value = "List of custom field value in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceFieldsValuePublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            List<CustomFieldValue> customFieldValues = resourceSpace.getCustomFieldValues();
            return ok(Json.toJson(customFieldValues));
        }
    }

    /**
     * POST       /api/space/:sid/fieldvalue
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = CustomFieldValue.class, produces = "application/json", value = "Create a custom field value in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CustomFieldValue object", value = "Body of CustomFieldValue in JSON", required = true, dataType = "models.CustomFieldValue", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result createSpaceFieldsValue(@ApiParam(name = "sid", value = "Space ID") Long sid) {
        final Form<CustomFieldValue> newCustomFieldValueForm = CUSTOM_FIELD_VALUE_FORM
                .bindFromRequest();
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            if (newCustomFieldValueForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "The form has the following error: "+
                                newCustomFieldValueForm.errorsAsJson()));
                return badRequest(Json.toJson(responseBody));
			} else {
				CustomFieldValue newCustomFieldValue = newCustomFieldValueForm.get();

				Long definitionId = newCustomFieldValue
						.getCustomFieldDefinition() != null ? 
								newCustomFieldValue.getCustomFieldDefinition().getCustomFieldDefinitionId() : null;
				if (definitionId != null) {
					newCustomFieldValue.setCustomFieldDefinition(CustomFieldDefinition.read(definitionId));
					newCustomFieldValue = CustomFieldValue.create(newCustomFieldValue);
	               Logger.info("Adding custom field value to resource space: "+sid);
	               if (resourceSpace.getCustomFieldValues() == null) {
	                   Logger.info("Creating array of custom field value in resource space: "+sid);
	            	   resourceSpace.setCustomFieldValues(new ArrayList<>());
	               } 
	               resourceSpace.getCustomFieldValues().add(newCustomFieldValue);
	               resourceSpace.update();
				} else {
					TransferResponseStatus responseBody = new TransferResponseStatus();
					responseBody.setStatusMessage(Messages
							.get("The value does not specify a defition"));
					return badRequest(Json.toJson(responseBody));
				}

				return ok(Json.toJson(newCustomFieldValue));
			}
		}
    }
    
    
   /** 
    * POST       /api/space/:sid/fieldvalue
    *
    * @param sid
    * @return
    */
   @ApiOperation(httpMethod = "POST", response = CustomFieldValue.class, produces = "application/json", value = "Create a list of custom field values in a resource space")
   @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
   @ApiImplicitParams({
           @ApiImplicitParam(name = "List of CustomFieldValue objects within a ResourceSpace", value = "Body of ResourceSpace with CustomFieldValues in JSON", required = true, dataType = "models.ResourceSpace", paramType = "body"),
           @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
   public static Result createSpaceFieldValues(@ApiParam(name = "sid", value = "Space ID") Long sid) {
       final Form<ResourceSpace> newCustomFieldValuesForm = RESOURCE_SPACE_FORM.bindFromRequest();
       ResourceSpace resourceSpace = ResourceSpace.read(sid);
       if (resourceSpace == null) {
           return notFound(Json
                   .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
       } else {
           if (newCustomFieldValuesForm.hasErrors()) {
               TransferResponseStatus responseBody = new TransferResponseStatus();
               responseBody.setStatusMessage(Messages.get("The form has the following error: "+newCustomFieldValuesForm.errorsAsJson()));
               return badRequest(Json.toJson(responseBody));
           } else {
        	   ResourceSpace newCustomFieldValuesSpace = newCustomFieldValuesForm.get(); 
               List<CustomFieldValue> newCustomFieldValues = newCustomFieldValuesSpace.getCustomFieldValues();
               for (CustomFieldValue customFieldValue : newCustomFieldValues) {
            	   // create the value only if appropriate target UUID and field value definition ID are included
            	   Long definitionId = customFieldValue.getCustomFieldDefinition() != null ? customFieldValue.getCustomFieldDefinition().getCustomFieldDefinitionId() : null;
            	   if (definitionId != null) {
            		   CustomFieldDefinition def = CustomFieldDefinition.read(definitionId);
            		   customFieldValue.setCustomFieldDefinition(def);
            		   customFieldValue = CustomFieldValue.create(customFieldValue);
            	   } else {
                       TransferResponseStatus responseBody = new TransferResponseStatus();
                       responseBody.setStatusMessage(Messages.get("The value does not specify a defition"));
                       return badRequest(Json.toJson(responseBody));
            	   }
               }
               Logger.info("Adding custom field values to resource space: "+sid);
               if (resourceSpace.getCustomFieldValues() == null) {
                   Logger.info("Creating array of custom field values in resource space: "+sid);
            	   resourceSpace.setCustomFieldValues(new ArrayList<>());
               } 
               resourceSpace.getCustomFieldValues().addAll(newCustomFieldValues);
               resourceSpace.update();
               return ok(Json.toJson(newCustomFieldValues));
           }
       }
   }
   
   
   @ApiOperation(httpMethod = "PUT", response = CustomFieldValue.class, produces = "application/json", value = "Update a list of custom field values in a resource space")
   @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
   @ApiImplicitParams({
           @ApiImplicitParam(name = "List of CustomFieldValue objects within a ResourceSpace", value = "Body of ResourceSpace with CustomFieldValues in JSON", required = true, dataType = "models.ResourceSpace", paramType = "body"),
           @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
   public static Result updateSpaceFieldValues(@ApiParam(name = "sid", value = "Space ID") Long sid) {
       final Form<ResourceSpace> newCustomFieldValuesForm = RESOURCE_SPACE_FORM.bindFromRequest();
       ResourceSpace resourceSpace = ResourceSpace.read(sid);
       if (resourceSpace == null) {
           return notFound(Json
                   .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
       } else {
           if (newCustomFieldValuesForm.hasErrors()) {
               TransferResponseStatus responseBody = new TransferResponseStatus();
               responseBody.setStatusMessage(Messages.get("The form has the following error: "+newCustomFieldValuesForm.errorsAsJson()));
               return badRequest(Json.toJson(responseBody));
           } else {
        	   ResourceSpace newCustomFieldValuesSpace = newCustomFieldValuesForm.get(); 
               List<CustomFieldValue> newCustomFieldValues = newCustomFieldValuesSpace.getCustomFieldValues();
               Boolean customValuesAreNew = false;
               for (CustomFieldValue customFieldValue : newCustomFieldValues) {
            	   if (customFieldValue.getCustomFieldValueId() == null) {
                	   customFieldValue = CustomFieldValue.create(customFieldValue);
                	   customValuesAreNew = true;
            	   } else {
                	   customFieldValue = CustomFieldValue.update(customFieldValue);
            	   }            	   
               }
               
               if (customValuesAreNew) {
                   Logger.info("Adding custom field values to resource space: "+sid);
                   if (resourceSpace.getCustomFieldValues() == null) {
                       Logger.info("Creating array of custom field values in resource space: "+sid);
                	   resourceSpace.setCustomFieldValues(new ArrayList<>());
                   } 
                   resourceSpace.getCustomFieldValues().addAll(newCustomFieldValues);
                   resourceSpace.update();
               }
               return ok(Json.toJson(newCustomFieldValues));
           }
       }
   }
   

    /**
     * DELETE       /api/space/:sid/fieldvalue/:cfid
     *
     * @param sid
     * @param cfid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = CustomFieldValue.class, produces = "application/json", responseContainer = "List", value = "Delete a custom field value from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceFieldsValue(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                           @ApiParam(name = "cfid", value = "Custom field value ID") Long cfid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<CustomFieldValue> customFieldValues = new ArrayList<CustomFieldValue>();
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            CustomFieldValue customFieldValue = CustomFieldValue.read(cfid);
            if (customFieldValue == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No custom field value found with id "+cfid)));
            }
            resourceSpace.getCustomFieldDefinitions().remove(customFieldValue.getCustomFieldDefinition());
            resourceSpace.update();
            resourceSpace.refresh();
            customFieldValue.softRemove();
            return ok(Json.toJson(customFieldValues));            
        }
    }

    /**
     * PUT       /api/space/:sid/fieldvalue/:cfid
     *
     * @param sid
     * @param cfid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = CustomFieldValue.class, produces = "application/json", value = "Update a custom field value in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "CustomFieldValue object", value = "Body of CustomFieldValue in JSON", required = true, dataType = "models.CustomFieldValue", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result updateSpaceFieldsValue(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                           @ApiParam(name = "cfid", value = "Custom field value ID") Long cfid) {
        final Form<CustomFieldValue> newCustomFieldValueForm = CUSTOM_FIELD_VALUE_FORM
                .bindFromRequest();
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            if (newCustomFieldValueForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "The form has the following error: "+
                                newCustomFieldValueForm.errorsAsJson()));
                return badRequest(Json.toJson(responseBody));
            } else {
                CustomFieldValue customFieldValue = CustomFieldValue.read(cfid);
                if (customFieldValue == null) {
                    return notFound(Json
                            .toJson(new TransferResponseStatus("No custom field value found with id "+cfid)));
                }
                CustomFieldValue newCustomFieldValue = newCustomFieldValueForm.get();
                newCustomFieldValue.setCustomFieldValueId(customFieldValue.getCustomFieldValueId());
                newCustomFieldValue.setCustomFieldDefinition(customFieldValue.getCustomFieldDefinition());
                newCustomFieldValue.update();
                return ok(Json.toJson(newCustomFieldValue));
            }
        }
    }

    /**
     * PUT       /api/space/:sid/ballot
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Ballot.class, produces = "application/json", value = "Update a ballot in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Ballot object", value = "Body of Ballot in JSON", required = true, dataType = "models.Ballot", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result updateSpaceBallot(@ApiParam(name = "sid", value = "Space ID") Long sid) {

        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            final Form<Ballot> newBallotForm = BALLOT_FORM
                    .bindFromRequest();
            if (newBallotForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "The form has the following error: "+
                                newBallotForm.errorsAsJson()));
                return badRequest(Json.toJson(responseBody));
            } else {
                Ballot ballot = newBallotForm.get();
                if (ballot.getId()==null) {
                    TransferResponseStatus responseBody = new TransferResponseStatus();
                    responseBody.setStatusMessage("The id of the ballot is not found in request");
                    return badRequest(Json.toJson(responseBody));
                }
                ballot.update();
                F.Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(
                            resourceSpace.getType(),
                            NotificationEventName.UPDATED_VOTING_BALLOT,
                            resourceSpace,
                            resourceSpace);
                });
                return ok(Json.toJson(ballot));
            }
        }
    }

    /**
     * GET       /api/space/:sid/ballot
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Ballot.class, produces = "application/json", responseContainer = "List", value = "List of ballots in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @SubjectPresent
    public static Result findSpaceBallot(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                         @ApiParam(name = "status", value = "Status of ballots", allowableValues = "active,archived", defaultValue = "") String status,
                                         @ApiParam(name = "starts_at", value = "String Date with format ddMMyyyy") String starts_at,
                                         @ApiParam(name = "ends_at", value = "String Date with format ddMMyyyy") String ends_at) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            BallotStatus mappedType = null;
            Date startsAt = null;
            Date endsAt = null;
            if (status != null && !status.isEmpty()) {
                try{
                    mappedType = BallotStatus.valueOf(status.toUpperCase());
                }catch (Exception e) {
                    return notFound(Json.toJson(new TransferResponseStatus(
                            "Format status error " + e.getMessage())));
                }
            }
            DateFormat df = new SimpleDateFormat("ddMMyyyy");
            if (starts_at != null && !starts_at.isEmpty()) {
                try {
                    startsAt = df.parse(starts_at);
                } catch (ParseException e) {
                    return notFound(Json.toJson(new TransferResponseStatus(
                        "Format date error " + e.getMessage())));
                }
            }
            if (ends_at != null && !ends_at.isEmpty()) {
                try {
                    endsAt = df.parse(ends_at);
                } catch (ParseException e) {
                    return notFound(Json.toJson(new TransferResponseStatus(
                            "Format date error " + e.getMessage())));
                }
            }
            List<Ballot> resourceSpaceBallots = resourceSpace.getBallotsFilteredByStatusDate(mappedType,startsAt,endsAt);
            return ok(Json.toJson(resourceSpaceBallots));
        }
    }

    /**
     * GET       /api/space/:sid/ballot/:bid
     *
     * @param sid
     * @param bid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Ballot.class, produces = "application/json", value = "Ballot by id in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @SubjectPresent
    public static Result findSpaceBallotById(@ApiParam(name = "sid", value = "Space ID") Long sid,@ApiParam(name = "bid", value = "Ballot ID") Long bid) {
        ResourceSpace resourceSpace = ResourceSpace.findByBallot(sid,bid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No ballot found with id "+bid+ " in space with id "+sid)));
        } else {
            Ballot resourceSpaceBallot = Ballot.read(bid);
            return ok(Json.toJson(resourceSpaceBallot));
        }
    }

    /**
     * GET       /api/public/space/:uuid/ballot
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Ballot.class, produces = "application/json", responseContainer = "List", value = "List of ballots in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceBallotPublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid,
                                         @ApiParam(name = "status", value = "Status of ballots", allowableValues = "active,archived", defaultValue = "") String status,
                                         @ApiParam(name = "starts_at", value = "String Date with format ddMMyyyy") String starts_at,
                                         @ApiParam(name = "ends_at", value = "String Date with format ddMMyyyy") String ends_at) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            BallotStatus mappedType = null;
            Date startsAt = null;
            Date endsAt = null;
            if (status != null && !status.isEmpty()) {
                try{
                    mappedType = BallotStatus.valueOf(status.toUpperCase());
                }catch (Exception e) {
                    return notFound(Json.toJson(new TransferResponseStatus(
                            "Format status error " + e.getMessage())));
                }
            }
            DateFormat df = new SimpleDateFormat("ddMMyyyy");
            if (starts_at != null && !starts_at.isEmpty()) {
                try {
                    startsAt = df.parse(starts_at);
                } catch (ParseException e) {
                    return notFound(Json.toJson(new TransferResponseStatus(
                            "Format date error " + e.getMessage())));
                }
            }
            if (ends_at != null && !ends_at.isEmpty()) {
                try {
                    endsAt = df.parse(ends_at);
                } catch (ParseException e) {
                    return notFound(Json.toJson(new TransferResponseStatus(
                            "Format date error " + e.getMessage())));
                }
            }
            String result;
            try {
                List<Ballot> resourceSpaceBallots = resourceSpace.getBallotsFilteredByStatusDate(mappedType,startsAt,endsAt);
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resourceSpaceBallots);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No ballots with this space uuid")));
            }
            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };
            return ok(ret);
        }
    }


    /**
     * POST       /api/space/:sid/ballot
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Ballot.class, produces = "application/json", value = "Create a ballot in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "BallotTransfer object", value = "Body of BallotTransfer in JSON", required = true, dataType = "models.transfer.BallotTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result createSpaceBallot(@ApiParam(name = "sid", value = "Space ID") Long sid) {

        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            final Form<BallotTransfer> newBallotForm = BALLOT_TRANSFER_FORM
                    .bindFromRequest();
            if (newBallotForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "The form has the following error: "+
                                newBallotForm.errorsAsJson()));
                return badRequest(Json.toJson(responseBody));
            } else {
                BallotTransfer ballotTransfer = newBallotForm.get();
                UUID consensus = resourceSpace.getConsensusBallot();
                Ballot currentBallot = Ballot.findByUUID(consensus);
                List<BallotCandidate> newCandidates = new ArrayList<>();

                //Creates new candidates
                if (ballotTransfer.getCandidateType() == null || ballotTransfer.getBallotCandidates() == null || ballotTransfer.getBallotCandidates().isEmpty()) {
                    //Include all contributions of type PROPOSAL with status PUBLISHED
                    List<Contribution> contributionList = Contribution.findContributionsInSpaceByTypeStatus(resourceSpace.getResourceSpaceId(), ContributionTypes.PROPOSAL,ContributionStatus.PUBLISHED);
                    for (Contribution c:contributionList
                         ) {
                        BallotCandidate ballotCandidate = new BallotCandidate();
                        ballotCandidate.setCandidateUuid(c.getUuid());
                    }
                    ballotTransfer.setCandidateType(BallotCandidateTypes.CONTRIBUTION);
                } else {
                    newCandidates = ballotTransfer.getBallotCandidates();
                }
                Ballot newBallot = Ballot.createConsensusBallotForResourceSpace(resourceSpace, ballotTransfer.getBallotConfigs());

                for (BallotCandidate candidate : newCandidates) {
                    BallotCandidate contributionAssociatedCandidate = new BallotCandidate();
                    contributionAssociatedCandidate.setBallotId(newBallot.getId());
                    contributionAssociatedCandidate.setCandidateType(ballotTransfer.getCandidateType());
                    contributionAssociatedCandidate.setCandidateUuid(candidate.getCandidateUuid());
                    contributionAssociatedCandidate.save();
                }
                //Send the current ballot to a historic
                if(currentBallot != null){
                    resourceSpace.getBallotHistories().add(currentBallot);
                    resourceSpace.save();
                    //Archive previous ballot
                    currentBallot.setStatus(BallotStatus.ARCHIVED);
                    currentBallot.update();
                }

                F.Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(resourceSpace.getType(), NotificationEventName.NEW_VOTING_BALLOT, resourceSpace, resourceSpace);
                });

                return ok(Json.toJson(newBallot));
            }
        }
    }

    /**
     * PUT /api/space/:sid/config/:uuid
     *
     * @param sid
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Config.class, produces = "application/json", value = "Update a config description by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No config found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Config object", value = "Config in json", dataType = "models.Config", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceCampaignConfig(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "uuid", value = "Config UUID") UUID uuid) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Config> newConfigForm = CONFIG_FORM.bindFromRequest();
        if (newConfigForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Error updating config");
            return badRequest(Json.toJson(responseBody));
        } else {
            Config updatedConfig = newConfigForm.get();
            ResourceSpace resourceSpace = ResourceSpace.read(sid);
            if (resourceSpace == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
            }
            updatedConfig.setUuid(uuid);
            updatedConfig.update();
            resourceSpace.getConfigs().add(updatedConfig);
            resourceSpace.update();
            Logger.info("Updating Config");
            return ok(Json.toJson(updatedConfig));
        }
    }

    /**
     * PUT /api/space/:sid/milestone/:mid
     *
     * @param sid
     * @param mid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = ComponentMilestone.class, produces = "application/json", value = "Update a ComponentMilestone description by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No ComponentMilestone found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ComponentMilestone object", value = "ComponentMilestone in json", dataType = "models.ComponentMilestone", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceComponentMilestone(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "mid", value = "ComponentMilestone ID") Long mid) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<ComponentMilestone> newComponentMilestoneForm = COMPONENT_MILESTONE_FORM.bindFromRequest();
        if (newComponentMilestoneForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Error updating ComponentMilestone");
            return badRequest(Json.toJson(responseBody));
        } else {
            ComponentMilestone updatedComponentMilestone = newComponentMilestoneForm.get();
            ResourceSpace resourceSpace = ResourceSpace.read(sid);
            if (resourceSpace == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
            }
            updatedComponentMilestone.setComponentMilestoneId(mid);
            updatedComponentMilestone.update();
            resourceSpace.getComponent().getMilestones().add(updatedComponentMilestone);
            resourceSpace.update();
            Logger.info("Updating ComponentMilestone");
            return ok(Json.toJson(updatedComponentMilestone));
        }
    }

    /**
     * PUT /api/space/:sid/component/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Component.class, produces = "application/json", value = "Update a Component description by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Component found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Component object", value = "Component in json", dataType = "models.Component", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceCampaignComponent(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "cid", value = "Component ID") Long cid) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Component> newComponentForm = COMPONENT_FORM.bindFromRequest();
        if (newComponentForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Error updating Component");
            return badRequest(Json.toJson(responseBody));
        } else {
            Component updatedComponent = newComponentForm.get();
            ResourceSpace resourceSpace = ResourceSpace.read(sid);
            if (resourceSpace == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
            }
            updatedComponent.setComponentId(cid);
            updatedComponent.update();
            resourceSpace.getComponents().add(updatedComponent);
            resourceSpace.update();
            Logger.info("Updating Component");
            return ok(Json.toJson(updatedComponent));
        }
    }

    /**
     * GET /api/space/:sid/component
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, responseContainer = "List", value = "Lists resource space's components")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceComponent(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<Component> componentList;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            componentList = resourceSpace.getComponents();
            return ok(Json.toJson(componentList));
        }
    }

    /**
     * GET /api/space/:sid/component/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, value = "Component in a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceComponentById(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                                @ApiParam(name = "cid", value = "Component ID") Long cid) {
        ResourceSpace resourceSpace = ResourceSpace.findByComponent(sid, cid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            Component component = Component.read(cid);
            return ok(Json.toJson(component));
        }
    }

    /**
     * GET       /api/public/space/:uuid/component
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, produces = "application/json", responseContainer = "List", value = "List of components in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceComponentPublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            String result;
            try {
                List<Component> resourceSpaceComponents = resourceSpace.getComponents();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resourceSpaceComponents);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No components with this space uuid")));
            }
            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };
            return ok(ret);
        }
    }

    /**
     * GET /api/space/:sid/milestone
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ComponentMilestone.class, responseContainer = "List", value = "Lists resource space's component milestones")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceMilestone(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<ComponentMilestone> componentList;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            componentList = resourceSpace.getMilestones();
            return ok(Json.toJson(componentList));
        }
    }

    /**
     * GET /api/space/:sid/milestone/:mid
     *
     * @param sid
     * @param mid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ComponentMilestone.class, value = "Component Milestone in a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceMilestoneById(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                                @ApiParam(name = "mid", value = "Component Milestone ID") Long mid) {
        ResourceSpace resourceSpace = ResourceSpace.findByMilestone(sid, mid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            ComponentMilestone component = ComponentMilestone.read(mid);
            return ok(Json.toJson(component));
        }
    }

    /**
     * GET       /api/public/space/:uuid/milestone
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ComponentMilestone.class, produces = "application/json", responseContainer = "List", value = "List of milestones in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceMilestonePublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            String result;
            try {
                List<ComponentMilestone> resourceSpaceComponents = resourceSpace.getMilestones();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resourceSpaceComponents);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No milestones with this space uuid")));
            }
            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };
            return ok(ret);
        }
    }

    /**
     * GET /api/space/:sid/assembly
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Assembly.class, responseContainer = "List", value = "Lists resource space's assemblies")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceAssembly(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<Assembly> assemblyList;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            assemblyList = resourceSpace.getAssemblies();
            return ok(Json.toJson(assemblyList));
        }
    }

    /**
     * GET /api/space/:sid/assembly/:aid
     *
     * @param sid
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Assembly.class, value = "Assembly in a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceAssemblyById(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                                @ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        ResourceSpace resourceSpace = ResourceSpace.findByAssembly(sid, aid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            Assembly assembly = Assembly.read(aid);
            return ok(Json.toJson(assembly));
        }
    }

    /**
     * GET       /api/public/space/:uuid/assembly
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Assembly.class, produces = "application/json", responseContainer = "List", value = "List of assemblies in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceAssemblyPublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            String result;
            try {
                List<Assembly> resourceSpAssemblies = resourceSpace.getAssemblies();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resourceSpAssemblies);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No assemblies with this space uuid")));
            }
            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };
            return ok(ret);
        }
    }

    /**
     * GET /api/space/:sid/campaign
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", value = "Lists resource space's Campaigns")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceCampaign(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<Campaign> campaignList;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            campaignList = resourceSpace.getCampaigns();
            return ok(Json.toJson(campaignList));
        }
    }

    /**
     * GET /api/space/:sid/campaign/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, value = "Campaign in a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceCampaignById(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                               @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
        ResourceSpace resourceSpace = ResourceSpace.findByCampaign(sid, cid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            Campaign campaign = Campaign.read(cid);
            return ok(Json.toJson(campaign));
        }
    }

    /**
     * GET       /api/public/space/:uuid/campaign
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", responseContainer = "List", value = "List of campaigns in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceCampaignPublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            String result;
            try {
                List<Campaign> resourceCampaigns = resourceSpace.getCampaigns();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resourceCampaigns);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No campaigns with this space uuid")));
            }
            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };
            return ok(ret);
        }
    }

    /**
     * GET /api/space/:sid/theme
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Theme.class, responseContainer = "List", value = "Lists resource space's themes")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceTheme(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<Theme> themeList;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            themeList = resourceSpace.getThemes();
            return ok(Json.toJson(themeList));
        }
    }

    /**
     * GET /api/space/:sid/theme/:tid
     *
     * @param sid
     * @param tid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Theme.class, value = "Theme in a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceThemeById(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                               @ApiParam(name = "tid", value = "Theme ID") Long tid) {
        ResourceSpace resourceSpace = ResourceSpace.findByTheme(sid, tid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            Theme theme = Theme.read(tid);
            return ok(Json.toJson(theme));
        }
    }

    /**
     * GET       /api/public/space/:uuid/theme
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Theme.class, produces = "application/json", responseContainer = "List", value = "List of themes in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceThemePublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            String result;
            try {
                List<Theme> resourceSpaceThemes = resourceSpace.getThemes();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resourceSpaceThemes);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No themes with this space uuid")));
            }
            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };
            return ok(ret);
        }
    }

    /**
     * GET /api/space/:sid/group
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer = "List", value = "Lists resource space's groups")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceGroup(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                        @ApiParam(name="is_topic", value="Filter by topic default true") String isTopic) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<WorkingGroup> workingGroupList;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            if (isTopic.equals("true") || isTopic.equals("TRUE")){
                workingGroupList = resourceSpace.getGroupsFilteredByTopic();
            }else{
                workingGroupList = resourceSpace.getWorkingGroups();
            }
            return ok(Json.toJson(workingGroupList));
        }
    }

    /**
     * GET /api/space/:sid/group/:gid
     *
     * @param sid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, value = "WorkingGroup in a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceGroupById(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                            @ApiParam(name = "gid", value = "WorkingGroup ID") Long gid) {
        ResourceSpace resourceSpace = ResourceSpace.findByGroup(sid, gid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            WorkingGroup workingGroup = WorkingGroup.read(gid);
            return ok(Json.toJson(workingGroup));
        }
    }

    /**
     * GET       /api/public/space/:uuid/group
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, produces = "application/json", responseContainer = "List", value = "List of groups in a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class) })
    public static Result findSpaceGroupPublic(@ApiParam(name = "uuid", value = "Space UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            String result;
            try {
                List<WorkingGroup> resourceSpaceGroups = resourceSpace.getWorkingGroups();
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resourceSpaceGroups);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No groups with this space uuid")));
            }
            Content ret = new Content() {
                @Override
                public String body() {
                    return result;
                }

                @Override
                public String contentType() {
                    return "application/json";
                }
            };
            return ok(ret);
        }
    }

    /**
     * PUT /api/space/:sid/contribution/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, produces = "application/json", value = "Update a Contribution description by its ID and the space ID", notes = "Only for Members")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Contribution in json", dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceContribution(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid) {
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Error updating Contribution");
            return badRequest(Json.toJson(responseBody));
        } else {
            ResourceSpace resourceSpace = ResourceSpace.read(sid);
            if (resourceSpace == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
            }
            Ebean.beginTransaction();
            try {
                Contribution newContribution = newContributionForm.get();
                newContribution.setContributionId(cid);
                newContribution.setContextUserId(author.getUserId());
                if(!ResourceSpace.isMemberResourceSpace(author,resourceSpace,newContribution)){
                    return unauthorized(Json.toJson(new TransferResponseStatus(
                            ResponseStatus.UNAUTHORIZED,
                            "User unauthorized")));
                }
                Contribution updatedContribution = Contribution.readAndUpdate(newContribution, cid, author.getUserId());
                F.Promise.promise(() -> {
                    return NotificationsDelegate
                            .updatedContributionInResourceSpace(resourceSpace,
                                    updatedContribution);
                });

                Ebean.commitTransaction();
                return ok(Json.toJson(updatedContribution));
            } catch (Exception e) {
                Ebean.endTransaction();
                e.printStackTrace();
                Logger.error("Error while updating contribution => ",
                        LogActions.exceptionStackTraceToString(e));
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(e.getMessage());
                return Controller
                        .internalServerError(Json.toJson(responseBody));
            }
        }
    }

    /**
     * PUT /api/space/:sid/assembly/:aid
     *
     * @param sid
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Assembly.class, produces = "application/json", value = "Update a Assembly by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Assembly found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Assembly object", value = "Assembly in json", dataType = "models.Assembly", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceAssembly(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with sid "+sid)));
        }
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
            return unauthorized(Json.toJson(new TransferResponseStatus(
                    ResponseStatus.UNAUTHORIZED,
                    "User unauthorized")));
        }
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
                newAssembly = Assembly.readAndUpdate(updatedAssembly,aid);

                AssemblyProfile profile = newAssembly.getProfile();
                AssemblyProfile profileDB = AssemblyProfile.findByAssembly(newAssembly.getUuid());
                profile.setAssemblyProfileId(profileDB.getAssemblyProfileId());
                profile.update();
                // TODO: return URL of the new group
                Logger.info("Updating assembly");
                Logger.debug("=> " + newAssemblyForm.toString());

                newAssembly.update();
                resourceSpace.getAssemblies().add(newAssembly);
                resourceSpace.update();
                NotificationsDelegate.createNotificationEventsByType(
                        ResourceSpaceTypes.ASSEMBLY.toString(), newAssembly.getUuid());
            } catch (Exception e) {
                Ebean.rollbackTransaction();
                Logger.error("Error updating assembly: "+LogActions.exceptionStackTraceToString(e));
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.ASSEMBLY_CREATE_MSG_ERROR,
                        newAssembly.getName()));
                return internalServerError(Json.toJson(responseBody));
            }
            Ebean.commitTransaction();

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
     * PUT /api/space/:sid/group/:gid
     *
     * @param sid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = WorkingGroup.class, produces = "application/json", value = "Update a Working Group by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No WorkingGroup found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "WorkingGroup object", value = "WorkingGroup in json", dataType = "models.WorkingGroup", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceGroup(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "gid", value = "WorkingGroup ID") Long gid) {
        final Form<WorkingGroup> newWorkingGroupForm = WORKING_GROUP_FORM
                .bindFromRequest();
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with sid "+sid)));
        }
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
            return unauthorized(Json.toJson(new TransferResponseStatus(
                    ResponseStatus.UNAUTHORIZED,
                    "User unauthorized")));
        }
        if (newWorkingGroupForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.GROUP_CREATE_MSG_ERROR,
                    newWorkingGroupForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            WorkingGroup newWorkingGroup = newWorkingGroupForm.get();

            TransferResponseStatus responseBody = new TransferResponseStatus();
            newWorkingGroup.setGroupId(gid);
            List<Theme> themes = newWorkingGroup.getThemes();
            List<Theme> themesLoaded = new ArrayList<Theme>();
            for (Theme theme: themes) {
                Theme themeRead = Theme.read(theme.getThemeId());
                themesLoaded.add(themeRead);
            }
            newWorkingGroup.setThemes(themesLoaded);
            newWorkingGroup.update();
            resourceSpace.getWorkingGroups().add(newWorkingGroup);
            resourceSpace.update();
            // TODO: return URL of the new group
            Logger.info("Updating working group");
            Logger.debug("=> " + newWorkingGroupForm.toString());

            responseBody.setNewResourceId(newWorkingGroup.getGroupId());
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.GROUP_CREATE_MSG_SUCCESS,
                    newWorkingGroup.getName()/*
                                             * ,
                                             * groupCreator.getIdentifier()
                                             */));
            responseBody.setNewResourceURL(GlobalData.GROUP_BASE_PATH + "/"
                    + newWorkingGroup.getGroupId());

            F.Promise.promise(() -> {
                return NotificationsDelegate.signalNotification(
                        ResourceSpaceTypes.WORKING_GROUP,
                        NotificationEventName.UPDATED_WORKING_GROUP,
                        resourceSpace, newWorkingGroup);
            });

            return ok(Json.toJson(responseBody));
        }
    }

    /**
     * PUT /api/space/:sid/campaign/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update a Campaign by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Campaign object", value = "Campaign in json", dataType = "models.Campaign", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceCampaign(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
        final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with sid "+sid)));
        }
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
            return unauthorized(Json.toJson(new TransferResponseStatus(
                    ResponseStatus.UNAUTHORIZED,
                    "User unauthorized")));
        }
        if (newCampaignForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
                    newCampaignForm.errorsAsJson()));
            Logger.info("Error updating campaign");
            Logger.debug("=> " + newCampaignForm.errorsAsJson());
            return badRequest(Json.toJson(responseBody));
        } else {
            try {
                Campaign campaignOld = Campaign.read(cid);
                Campaign updatedCampaign = newCampaignForm.get();
                updatedCampaign.setCampaignId(cid);
                updatedCampaign.setBallots(campaignOld.getBallots());
                updatedCampaign.setResources(campaignOld.getResources());
                updatedCampaign.setForum(campaignOld.getForum());
                updatedCampaign.setTimelineEdges(campaignOld.getTimelineEdges());
                updatedCampaign.setTemplate(campaignOld.getTemplate());
                List<Component> componentList = new ArrayList<Component>();
                for (Component component:updatedCampaign.getComponents()
                        ) {
                    component.update();
                    componentList.add(component);
                }
                updatedCampaign.setComponents(componentList);
                updatedCampaign.update();
                Logger.info("Updating campaign");
                Logger.debug("=> " + newCampaignForm.toString());
                resourceSpace.getCampaigns().add(updatedCampaign);
                resourceSpace.update();
                F.Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(ResourceSpaceTypes.ASSEMBLY, NotificationEventName.UPDATED_CAMPAIGN, resourceSpace, updatedCampaign);
                });

                return ok(Json.toJson(updatedCampaign));
            } catch (Exception e) {
                return badRequest(Json.toJson("Invalid fields"));
            }
        }
    }

    /**
     * PUT /api/space/:sid/resource/:rid
     *
     * @param sid
     * @param rid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Resource.class, produces = "application/json", value = "Update a Resource by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Resource found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Resource object", value = "Resource in json", dataType = "models.Resource", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceResource(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "rid", value = "Resource ID") Long rid) {
        final Form<Resource> newResourceForm = RESOURCE_FORM.bindFromRequest();
        if (newResourceForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Error updating Resource");
            return badRequest(Json.toJson(responseBody));
        } else {
            Resource updatedResource = newResourceForm.get();
            ResourceSpace resourceSpace = ResourceSpace.read(sid);
            if (resourceSpace == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
            }
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            updatedResource.setResourceId(rid);
            updatedResource.update();
            resourceSpace.getResources().add(updatedResource);
            resourceSpace.update();
            Logger.info("Updating Resource");
            return ok(Json.toJson(updatedResource));
        }
    }

    /**
     * PUT /api/space/:sid/theme/:tid
     *
     * @param sid
     * @param tid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Theme.class, produces = "application/json", value = "Update a Theme by its ID and the space ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Theme found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Theme object", value = "Theme in json", dataType = "models.Theme", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result updateSpaceTheme(
            @ApiParam(name = "sid", value = "Space ID") Long sid,
            @ApiParam(name = "tid", value = "Theme ID") Long tid) {
        final Form<Theme> newThemeForm = THEME_FORM.bindFromRequest();
        if (newThemeForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Error updating Theme");
            return badRequest(Json.toJson(responseBody));
        } else {
            Theme updatedTheme = newThemeForm.get();
            ResourceSpace resourceSpace = ResourceSpace.read(sid);
            if (resourceSpace == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
            }
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            updatedTheme.setThemeId(tid);
            updatedTheme.update();
            resourceSpace.getThemes().add(updatedTheme);
            resourceSpace.update();
            Logger.info("Updating Theme");
            return ok(Json.toJson(updatedTheme));
        }
    }

    /**
     * DELETE       /api/space/:sid/config/:uuid
     *
     * @param sid
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Config.class, produces = "application/json", responseContainer = "List", value = "Delete a Config from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Config found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceConfig(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                           @ApiParam(name = "uuid", value = "Config UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.findByConfig(sid, uuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            Config config = Config.read(uuid);
            if (config == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No config found with uuid "+uuid)));
            }

            resourceSpace.getConfigs().remove(config);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.CONFIG,null);
            return ok(Json.toJson(resourceSpace.getConfigs()));
        }
    }

    /**
     * DELETE       /api/space/:sid/ballot/:bid
     *
     * @param sid
     * @param bid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Ballot.class, produces = "application/json", responseContainer = "List", value = "Delete a Ballot from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Ballot found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceBallot(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                           @ApiParam(name = "bid", value = "Ballot ID") Long bid) {
        ResourceSpace resourceSpace = ResourceSpace.findByBallot(sid, bid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            Ballot ballot = Ballot.read(bid);
            if (ballot == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No Ballot found with id "+bid)));
            }
            resourceSpace.getBallots().remove(ballot);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.BALLOT,ballot.getId());
            return ok(Json.toJson(resourceSpace.getBallots()));
        }
    }

    /**
     * DELETE       /api/space/:sid/component/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Component.class, produces = "application/json", responseContainer = "List", value = "Delete a Component from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Component found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceComponent(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                           @ApiParam(name = "cid", value = "Component ID") Long cid) {
        ResourceSpace resourceSpace = ResourceSpace.findByComponent(sid, cid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            Component component = Component.read(cid);
            if (component == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No Component found with id "+cid)));
            }
            resourceSpace.getComponents().remove(component);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.COMPONENT,component.getComponentId());
            return ok(Json.toJson(resourceSpace.getComponents()));
        }
    }

    /**
     * DELETE       /api/space/:sid/milestone/:mid
     *
     * @param sid
     * @param mid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = ComponentMilestone.class, produces = "application/json", responseContainer = "List", value = "Delete a Component Milestone from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Component Milestone found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceMilestone(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                              @ApiParam(name = "mid", value = "Component Milestone ID") Long mid) {
        ResourceSpace resourceSpace = ResourceSpace.findByMilestone(sid, mid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            ComponentMilestone componentMilestone = ComponentMilestone.read(mid);
            if (componentMilestone == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No Component Milestone found with id "+mid)));
            }
            resourceSpace.getMilestones().remove(componentMilestone);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.MILESTONE,componentMilestone.getComponentMilestoneId());
            return ok(Json.toJson(resourceSpace.getMilestones()));
        }
    }

    /**
     * DELETE       /api/space/:sid/contribution/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, produces = "application/json", responseContainer = "List", value = "Delete a Contribution from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Contribution found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceContribution(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                              @ApiParam(name = "cid", value = "Contribution ID") Long cid) {
        ResourceSpace resourceSpace = ResourceSpace.findByContribution(sid, cid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            Contribution contribution = Contribution.read(cid);
            if (contribution == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No contribution found with id "+cid)));
            }
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isMemberResourceSpace(author,resourceSpace,contribution)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            resourceSpace.getContributions().remove(contribution);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.CONTRIBUTION,contribution.getContributionId());
            return ok(Json.toJson(resourceSpace.getContributions()));
        }
    }

    /**
     * DELETE       /api/space/:sid/assembly/:aid
     *
     * @param sid
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Assembly.class, produces = "application/json", responseContainer = "List", value = "Delete a Assembly from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Assembly found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceAssembly(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                                 @ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        ResourceSpace resourceSpace = ResourceSpace.findByAssembly(sid, aid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            Assembly assembly = Assembly.read(aid);
            if (assembly == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No Assembly found with id "+aid)));
            }
            resourceSpace.getAssemblies().remove(assembly);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.ASSEMBLY,assembly.getAssemblyId());
            return ok(Json.toJson(resourceSpace.getAssemblies()));
        }
    }

    /**
     * DELETE       /api/space/:sid/group/:gid
     *
     * @param sid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = WorkingGroup.class, produces = "application/json", responseContainer = "List", value = "Delete a WorkingGroup from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No WorkingGroup found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceGroup(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                             @ApiParam(name = "gid", value = "WorkingGroup ID") Long gid) {
        ResourceSpace resourceSpace = ResourceSpace.findByGroup(sid, gid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            WorkingGroup workingGroup = WorkingGroup.read(gid);
            if (workingGroup == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No workingGroup found with id "+gid)));
            }
            resourceSpace.getWorkingGroups().remove(workingGroup);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.GROUP,workingGroup.getGroupId());
            return ok(Json.toJson(resourceSpace.getWorkingGroups()));
        }
    }

    /**
     * DELETE       /api/space/:sid/campaign/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Campaign.class, produces = "application/json", responseContainer = "List", value = "Delete a Campaign from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Campaign found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceCampaign(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                             @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
        ResourceSpace resourceSpace = ResourceSpace.findByCampaign(sid, cid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            Campaign campaign = Campaign.read(cid);
            if (campaign == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No campaign found with id "+cid)));
            }
            resourceSpace.getCampaigns().remove(campaign);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.CAMPAIGN,campaign.getCampaignId());
            return ok(Json.toJson(resourceSpace.getCampaigns()));
        }
    }

    /**
     * DELETE       /api/space/:sid/theme/:tid
     *
     * @param sid
     * @param tid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Theme.class, produces = "application/json", responseContainer = "List", value = "Delete a Theme from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Theme found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceTheme(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                             @ApiParam(name = "tid", value = "Theme ID") Long tid) {
        ResourceSpace resourceSpace = ResourceSpace.findByTheme(sid, tid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            Theme theme = Theme.read(tid);
            if (theme == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No theme found with id "+tid)));
            }
            resourceSpace.getThemes().remove(theme);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.THEME,theme.getThemeId());
            return ok(Json.toJson(resourceSpace.getThemes()));
        }
    }

    /**
     * DELETE       /api/space/:sid/resource/:rid
     *
     * @param sid
     * @param rid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Resource.class, produces = "application/json", responseContainer = "List", value = "Delete a Resource from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Resource found", response = TransferResponseStatus.class) })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    public static Result deleteSpaceResource(@ApiParam(name = "sid", value = "Space ID") Long sid,
                                          @ApiParam(name = "rid", value = "Resource ID") Long rid) {
        ResourceSpace resourceSpace = ResourceSpace.findByResource(sid, rid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
            if(!ResourceSpace.isCoordinatorResourceSpace(author,resourceSpace)){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            Resource resource = Resource.read(rid);
            if (resource == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource found with id "+rid)));
            }
            resourceSpace.getResources().remove(resource);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.RESOURCE,resource.getResourceId());
            return ok(Json.toJson(resourceSpace.getResources()));
        }
    }


    /**
     * DELETE       /api/public/space/:suuid/contribution/:cuuid
     *
     * @param suuid
     * @param cuuid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, produces = "application/json", responseContainer = "List", value = "Delete a Contribution by UUID from a resource space")
    @ApiResponses(value = { @ApiResponse(code = 404, message = "No Contribution found", response = TransferResponseStatus.class) })
    public static Result deleteSpaceContributionPublic(@ApiParam(name = "suuid", value = "Space UUID") UUID suuid,
                                                 @ApiParam(name = "cuuid", value = "Contribution UUID") UUID cuuid) {
        ResourceSpace resourceSpace = ResourceSpace.findByContributionUuid(suuid, cuuid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+suuid)));
        } else {
            Contribution contribution = Contribution.readByUUID(cuuid);
            if (contribution == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No contribution found with uuid "+cuuid)));
            }
            if(!(ContributionTypes.COMMENT.equals(contribution.getType()) || ContributionTypes.DISCUSSION.equals(contribution.getType()))){
                return unauthorized(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.UNAUTHORIZED,
                        "User unauthorized")));
            }
            resourceSpace.getContributions().remove(contribution);
            resourceSpace.update();
            resourceSpace.refresh();
            ResourceSpaceAssociationHistory.createAssociationHistory(resourceSpace,ResourceSpaceAssociationTypes.CONTRIBUTION,contribution.getContributionId());
            return ok(Json.toJson(resourceSpace.getContributions()));
        }
    }

    /**
     * PUT /api/public/space/:suuid/contribution/:cuuid
     *
     * @param suuid
     * @param cuuid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, produces = "application/json", value = "Update a Contribution description by its UUID and the space UUID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Contribution in json", dataType = "models.Contribution", paramType = "body")})
    public static Result updateSpaceContributionPublic(
            @ApiParam(name = "suuid", value = "Space UUID") UUID suuid,
            @ApiParam(name = "cuuid", value = "Contribution UUID") UUID cuuid) {
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();
        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("Error updating Contribution");
            return badRequest(Json.toJson(responseBody));
        } else {
            ResourceSpace resourceSpace = ResourceSpace.readByUUID(suuid);
            if (resourceSpace == null) {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with uuid "+suuid)));
            }
            Ebean.beginTransaction();
            try {
                Contribution newContribution = newContributionForm.get();
                Contribution contribution = Contribution.readByUUID(cuuid);
                if (contribution == null) {
                    return notFound(Json
                            .toJson(new TransferResponseStatus("No contribution found with uuid "+cuuid)));
                }
                newContribution.setContributionId(contribution.getContributionId());
                if(!(ContributionTypes.COMMENT.equals(contribution.getType()) || ContributionTypes.DISCUSSION.equals(contribution.getType()))){
                    return unauthorized(Json.toJson(new TransferResponseStatus(
                            ResponseStatus.UNAUTHORIZED,
                            "User unauthorized")));
                }
                Contribution updatedContribution = Contribution.readAndUpdate(newContribution, contribution.getContributionId(), null);
                F.Promise.promise(() -> {
                    return NotificationsDelegate
                            .updatedContributionInResourceSpace(resourceSpace,
                                    updatedContribution);
                });

                Ebean.commitTransaction();
                return ok(Json.toJson(updatedContribution));
            } catch (Exception e) {
                Ebean.endTransaction();
                e.printStackTrace();
                Logger.error("Error while updating contribution => ",
                        LogActions.exceptionStackTraceToString(e));
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(e.getMessage());
                return Controller
                        .internalServerError(Json.toJson(responseBody));
            }
        }
    }
    /**
     * GET       /api/space/:sid/insights/themes
     *
     * @param sid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Theme.class, responseContainer = "List", produces = "application/json",
            value = "Get themes stats in a specific Resource Space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result exportSpaceThemeStats(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "type", value = "String", allowableValues = "OFFICIAL_PRE_DEFINED,EMERGENT,ALL") String type) {
        ResourceSpace rs = ResourceSpace.read(sid);
        if (rs == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        }
        List<Contribution> contributions = Contribution.findAllByContainingSpaceOrTypes(rs, ContributionTypes.PROPOSAL, ContributionTypes.IDEA);

        if (contributions == null) {
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No contributions for {resource space}: " + sid )));
        }
        ThemeTypes themeType = null;
        if (type != null && !type.isEmpty() && !type.equals("ALL")) {
            themeType = ThemeTypes.valueOf(type.toUpperCase());
        }
        HashMap<Long,ThemeStats> themesHash = new HashMap<Long,ThemeStats>();
        List<Contribution> contributionsFinal = new ArrayList<Contribution>();
        contributionsFinal.addAll(contributions);
        for (Contribution contribution: contributions) {
            contributionsFinal.addAll(Contribution.findAllByContainingSpaceOrTypes(contribution.getResourceSpace(), ContributionTypes.COMMENT, ContributionTypes.DISCUSSION));
        }
        for (Contribution contribution: contributionsFinal) {
            List<Theme> themeList = contribution.getThemes();
            for (Theme theme:themeList) {
                Theme themeLoaded = Theme.read(theme.getThemeId());
                if (themeType==null || themeLoaded.getType().equals(themeType)){
                    ThemeStats themeStats = new ThemeStats();
                    Integer proposals = themesHash.get(themeLoaded.getThemeId()) == null ? 0
                            : themesHash.get(themeLoaded.getThemeId()).getProposals();
                    Integer ideas = themesHash.get(themeLoaded.getThemeId()) == null ? 0
                            : themesHash.get(themeLoaded.getThemeId()).getIdeas();
                    Integer discussion = themesHash.get(themeLoaded.getThemeId()) == null ? 0
                            : themesHash.get(themeLoaded.getThemeId()).getDiscussion();
                    if(contribution.getType().equals(ContributionTypes.PROPOSAL)){
                        proposals++;
                    } else if(contribution.getType().equals(ContributionTypes.IDEA)){
                        ideas++;
                    } else if(contribution.getType().equals(ContributionTypes.DISCUSSION) || contribution.getType().equals(ContributionTypes.COMMENT)){
                        discussion++;
                    }
                    Integer total = proposals + ideas + discussion;
                    Integer totalProposalsIdeas = proposals + ideas;
                    String title = themeLoaded.getTitle();
                    String themeIdType = themeLoaded.getType().name();
                    themeStats.setDiscussion(discussion);
                    themeStats.setIdeas(ideas);
                    themeStats.setProposals(proposals);
                    themeStats.setTotal(total);
                    themeStats.setTotalProposalsIdeas(totalProposalsIdeas);
                    themeStats.setTitle(title);
                    themeStats.setType(themeIdType);
                    themesHash.put(themeLoaded.getThemeId(), themeStats);
                }
            }
        }
        return ok(Json.toJson(themesHash));
    }
}
