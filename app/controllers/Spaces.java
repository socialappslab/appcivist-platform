package controllers;


import static play.data.Form.form;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import models.*;
import models.misc.Views;
import models.transfer.BallotTransfer;
import models.transfer.TransferResponseStatus;
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

@Api(value = "09 space: resource space management", description = "Resource space management")
@With(Headers.class)
public class Spaces extends Controller {

    public static final Form<CustomFieldDefinition> CUSTOM_FIELD_DEFINITION_FORM = form(CustomFieldDefinition.class);

    public static final Form<CustomFieldValue> CUSTOM_FIELD_VALUE_FORM = form(CustomFieldValue.class);

    public static final Form<BallotTransfer> BALLOT_TRANSFER_FORM = form(BallotTransfer.class);

    public static final Form<Ballot> BALLOT_FORM = form(Ballot.class);

    public static final Form<ResourceSpace> RESOURCE_SPACE_FORM = form(ResourceSpace.class);

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
}
