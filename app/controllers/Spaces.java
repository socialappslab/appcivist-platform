package controllers;


import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.ResourceSpaceTypes;
import enums.ResourceTypes;
import models.Assembly;
import models.Campaign;
import models.ResourceSpace;
import models.location.Location;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import models.misc.Views;
import models.transfer.TransferResponseStatus;
import play.Logger;
import play.libs.Json;
import play.mvc.*;
import http.Headers;
import play.twirl.api.Content;

@Api(value="space", hidden=true)
@With(Headers.class)
public class Spaces extends Controller {

    @ApiOperation(produces="application/json", value="Simple search of resource space", httpMethod="GET")
    public static Result getSpace(Long sid) {
        ResourceSpace rs = ResourceSpace.read(sid);
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.put("type", rs.getType().toString());

        String name = "";
        if (rs.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
            name = rs.getAssemblyResources().getName();
        } else if (rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
            name = rs.getWorkingGroupResources().getName();
        } else if (rs.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
            name = rs.getCampaign().getTitle();
        }
        json.put("name", name);

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
}
