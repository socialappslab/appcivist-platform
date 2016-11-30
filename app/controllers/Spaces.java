package controllers;


import java.util.List;

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
            name = rs.getWorkingGroupForum().getName();
        } else if (rs.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
            name = rs.getCampaign().getTitle();
        }
        json.put("name", name);

        return Results.ok(json);
    }
}
