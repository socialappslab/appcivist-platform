package controllers;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import be.objectify.deadbolt.java.actions.Dynamic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feth.play.module.pa.PlayAuthenticate;
import enums.ResourceSpaceTypes;
import enums.ResourceTypes;
import io.swagger.annotations.*;
import models.*;
import models.location.Location;

import models.misc.Views;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;
import http.Headers;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;

import static play.data.Form.form;

@Api(value="space", hidden=true)
@With(Headers.class)
public class Spaces extends Controller {

    public static final Form<CustomFieldDefinition> CUSTOM_FIELD_DEFINITION_FORM = form(CustomFieldDefinition.class);

    public static final Form<CustomFieldValue> CUSTOM_FIELD_VALUE_FORM = form(CustomFieldValue.class);

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
        List<CustomFieldValue> customFieldValues = new ArrayList<CustomFieldValue>();
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            List<CustomFieldDefinition> customFieldDefinitions = resourceSpace.getCustomFieldDefinitions();
            for (CustomFieldDefinition customFieldDefinition:customFieldDefinitions
                 ) {
                customFieldValues.add(customFieldDefinition.getCustomFieldValue());
            }
        }
        return ok(Json.toJson(customFieldValues));
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
        User creator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
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
                newCustomFieldValue.setContextUserId(creator.getUserId());
                newCustomFieldValue = CustomFieldValue.create(newCustomFieldValue);
                resourceSpace.getCustomFieldDefinitions().add(newCustomFieldValue.getCustomFieldDefinition());
                resourceSpace.update();
                return ok(Json.toJson(newCustomFieldValue));
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
            List<CustomFieldDefinition> customFieldDefinitions = resourceSpace.getCustomFieldDefinitions();
            for (CustomFieldDefinition customFieldDefinition:customFieldDefinitions
                    ) {
                customFieldValues.add(customFieldDefinition.getCustomFieldValue());
            }
        }
        return ok(Json.toJson(customFieldValues));
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
        User creator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
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
                newCustomFieldValue.setContextUserId(creator.getUserId());
                newCustomFieldValue.setCustomFieldDefinition(customFieldValue.getCustomFieldDefinition());
                newCustomFieldValue.update();
                return ok(Json.toJson(newCustomFieldValue));
            }
        }
    }
}
