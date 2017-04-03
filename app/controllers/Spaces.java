package controllers;


import static play.data.Form.form;
import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import models.CustomFieldDefinition;
import models.CustomFieldValue;
import models.ResourceSpace;
import models.User;
import models.misc.Views;
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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feth.play.module.pa.PlayAuthenticate;

import enums.ResourceSpaceTypes;

@Api(value = "09 space: resource space management", description = "Resource space management")
@With(Headers.class)
public class Spaces extends Controller {

    public static final Form<CustomFieldDefinition> CUSTOM_FIELD_DEFINITION_FORM = form(CustomFieldDefinition.class);

    public static final Form<CustomFieldValue> CUSTOM_FIELD_VALUE_FORM = form(CustomFieldValue.class);

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
}
