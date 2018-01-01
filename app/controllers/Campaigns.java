package controllers;

import static play.data.Form.form;

import enums.*;
import http.Headers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import models.*;
import models.misc.Views;
import models.transfer.*;
import org.dozer.DozerBeanMapper;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.With;
import play.twirl.api.Content;
import security.SecurityModelConstants;
import utils.GlobalData;
import utils.LogActions;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feth.play.module.pa.PlayAuthenticate;

import delegates.CampaignDelegate;
import delegates.NotificationsDelegate;
import delegates.ResourcesDelegate;
import exceptions.ConfigurationException;

@Api(value = "03 campaign: Campaign Management", description = "Campaign Making Service: create and manage assembly campaigns")
@With(Headers.class)
public class Campaigns extends Controller {

    public static final Form<Campaign> CAMPAIGN_FORM = form(Campaign.class);
    public static final Form<WorkingGroup> GROUP_FORM = form(WorkingGroup.class);
    public static final Form<CampaignTransfer> CAMPAIGN_TRANSFER_FORM = form(CampaignTransfer.class);

    /**
     * GET /api/assembly/:aid/campaign
     * List campaigns of an Assembly
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json", value = "List campaigns of an Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findCampaignsByAssemblyId(
    		@ApiParam(name = "aid", value = "Assembly ID") Long aid, 
    		@ApiParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") String filter) {
        if (filter == null || filter.equals("ongoing")) {
            return ongoingCampaignsByAssemblyId(aid);
        } else if (filter.equals("past")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else if (filter.equals("future")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else {
            List<Campaign> campaigns = null;
            try {
                campaigns = Assembly.findCampaigns(aid);
            } catch (Exception e) {
                return internalServerError(Json.toJson(new TransferResponseStatus(
                        e.getMessage())));
            }
            if (!campaigns.isEmpty()) {
                return ok(Json.toJson(campaigns));
            }
            else
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No ongoing campaigns")));
        }
    }

    /**
     * GET /api/assembly/:aid/campaign/:cid
     * Read campaign by assembly ID and campaign ID
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Read campaign by campaign and assembly IDs")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findCampaignByAssemblyId(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
        Campaign campaign = Campaign.read(campaignId);
        return campaign != null ? ok(Json.toJson(campaign)) : ok(Json
                .toJson(new TransferResponseStatus("No campaign found")));
    }

    /**
     * GET       /api/campaign/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, produces = "application/json", value = "Read campaign by Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    public static Result findCampaignByUUID(@ApiParam(name = "uuid", value = "Campaign Universal ID (UUID)") UUID uuid) {
        try {

            Campaign summary = CampaignDelegate.getCampaignSummary(uuid);
            if (summary == null) {
                return ok(Json
                        .toJson(new TransferResponseStatus("No campaign found")));
            }
            //We have to show only ideas, discussions or proposals
            summary.setContributions(summary.getContributions().stream().filter(c -> {
                if (c.getType().equals(ContributionTypes.IDEA) || c.getType().equals(ContributionTypes.DISCUSSION)
                        || c.getType().equals(ContributionTypes.PROPOSAL)) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList()));
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            mapper.addMixIn(Campaign.class, Campaign.AssembliesVisibleMixin.class);
            mapper.addMixIn(Campaign.class, Campaign.AssemblyShortnameVisibleMixin.class);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(summary);

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
        } catch (Exception e) {
        	Logger.debug(e.getMessage());
        	e.printStackTrace();
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"+e.getMessage()))));
        }

    }

    /**
     * GET       /api/ballot/:uuid/campaign
     * Read campaign by assembly ID and campaign ID
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CampaignSummaryTransfer.class, responseContainer = "List", produces = "application/json", value = "Read campaign by voting ballot Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    public static Result findCampaignsByBallot(@ApiParam(name = "uuid", value = "Ballot Universal ID") UUID uuid) {
        List<CampaignSummaryTransfer> campaignSummaries = CampaignDelegate.findByBindingBallot(uuid);
        return campaignSummaries != null ? ok(Json.toJson(campaignSummaries)) : ok(Json
                .toJson(new TransferResponseStatus("No campaign found")));
    }

    /**
     * DELETE /api/assembly/:aid/campaign/:cid
     * Delete campaign by ID
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete campaign by campaign and assembly IDs", notes = "Only for COORDINATOS of assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result deleteCampaign(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
        // TODO: implement soft delete
        Campaign.delete(campaignId);
        return ok();
    }

    /**
     * PUT /api/assembly/:aid/campaign/:cid
     * Update campaign by ID
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update a campaign by its ID and the assembly ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Campaign object", value = "Campaign in json", dataType = "models.transfer.CampaignTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateCampaign(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<CampaignTransfer> newCampaignForm = CAMPAIGN_TRANSFER_FORM.bindFromRequest();
        if (newCampaignForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get( GlobalData.CAMPAIGN_CREATE_MSG_ERROR, newCampaignForm.errorsAsJson()));
            Logger.info("Error updating campaign");
            Logger.debug("=> " + newCampaignForm.errorsAsJson());
            return badRequest(Json.toJson(responseBody));
        } else {
            try {
                Logger.debug("Starting to update campaign by ID: "+campaignId);
                Campaign campaignOld = Campaign.read(campaignId);
                CampaignTransfer updatedCampaign = newCampaignForm.get();
                List<ComponentTransfer> updatedComponents = updatedCampaign.getTransientComponents();
                campaignOld.setCampaignId(campaignId);
                campaignOld.setGoal(updatedCampaign.getGoal());
                campaignOld.setTitle(updatedCampaign.getTitle());
                campaignOld.setShortname(updatedCampaign.getShortname());
                campaignOld.setUrl(updatedCampaign.getUrl());
                campaignOld.setListed(updatedCampaign.getListed());
                if (updatedCampaign.getCover() != null) {
                    ResourceTransfer coverT = updatedCampaign.getCover();
                    List<String> mappingFiles = Play.application().configuration()
                            .getStringList("appcivist.dozer.mappingFiles");
                    DozerBeanMapper mapper = new DozerBeanMapper(mappingFiles);
                    Resource cover = mapper.map(coverT, Resource.class);
                    if (updatedCampaign.getCover().getResourceId() != null) {
                        cover.update();
                        cover.refresh();
                        campaignOld.setCover(cover);
                    } else {
                        cover.save();
                        cover.refresh();
                        campaignOld.setCover(cover);
                    }
                }
                List<Component> componentList = new ArrayList<Component>();
                Logger.debug("Starting to update campaign components...");
                for (ComponentTransfer component : updatedComponents) {
                    if (component.getComponentId()!=null) {
                        Component componentOld = Component.read(component.getComponentId());
                        componentOld.setTitle(component.getTitle());
                        componentOld.setDescription(component.getDescription());
                        componentOld.setEndDate(component.getEndDate());
                        componentOld.setStartDate(component.getStartDate());
                        componentOld.setKey(component.getKey());
                        componentOld.setPosition(component.getPosition());
                        componentOld.setTimeline(component.getTimeline());
                        componentOld.update();
                        componentOld.refresh();
                        componentList.add(componentOld);

                        List<ComponentMilestoneTransfer> updatedMilestonesList = component.getMilestones();
                        Logger.debug("Starting to update component milestones for component:" + componentOld.getTitle() + "(" + componentOld.getComponentId() + ")");
                        int position = 1;
                        int milestoneCount = 0;
                        updatedMilestonesList.sort(Comparator.comparing(ComponentMilestoneTransfer::getDate));

                        for (ComponentMilestoneTransfer updateM : updatedMilestonesList) {
                            milestoneCount += 1;
                            if (updateM.getComponentMilestoneId() != null) {
                                ComponentMilestone oldMilestone = componentOld.getMilestoneById(updateM.getComponentMilestoneId());
                                int changes = 0;
                                if (oldMilestone.getTitle() != null
                                        && !oldMilestone.getTitle().equals(updateM.getTitle())) {

                                    if (updateM.getTitle() != null
                                            && !updateM.getTitle().isEmpty()
                                            && !updateM.getTitle().equals("")) {
                                        Logger.debug("Updating milestone title:" + updateM.getComponentMilestoneId());
                                        oldMilestone.setTitle(updateM.getTitle());
                                        changes++;
                                    }
                                } else if (oldMilestone.getTitle() == null && updateM.getTitle() != null && !updateM.getTitle().isEmpty()
                                        && !updateM.getTitle().equals("")) {
                                    Logger.debug("Updating milestone title:" + updateM.getComponentMilestoneId());
                                    oldMilestone.setTitle(updateM.getTitle());
                                    changes++;
                                }

                                if (oldMilestone.getDescription() != null
                                        && !oldMilestone.getDescription().equals(updateM.getDescription())) {
                                    Logger.debug("Updating milestone description:" + updateM.getComponentMilestoneId());
                                    oldMilestone.setDescription(updateM.getDescription());
                                    changes++;
                                } else if (oldMilestone.getDescription() == null && updateM.getDescription() != null) {
                                    Logger.debug("Updating milestone description:" + updateM.getComponentMilestoneId());
                                    oldMilestone.setDescription(updateM.getDescription());
                                    changes++;
                                }

                                if (oldMilestone.getDate() != null
                                        && !oldMilestone.getDate().equals(updateM.getDate())) {
                                    Logger.debug("Updating milestone date:" + updateM.getComponentMilestoneId());
                                    oldMilestone.setDate(updateM.getDate());
                                    changes++;
                                } else if (oldMilestone.getDate() == null && updateM.getDate() != null) {
                                    Logger.debug("Updating milestone date:" + updateM.getComponentMilestoneId());
                                    oldMilestone.setDate(updateM.getDate());
                                    changes++;
                                }

                                if (oldMilestone.getType().equals(ComponentMilestoneTypes.END)
                                        && oldMilestone.getPosition() != updatedMilestonesList.size()) {
                                    oldMilestone.setPosition(updatedMilestonesList.size());
                                    changes++;
                                }

                                if (changes > 0) {
                                    Logger.debug("Updating milestone:" + updateM.getComponentMilestoneId());
                                    if (position == 1) {
                                        position = milestoneCount;
                                    }
                                    if (!oldMilestone.getType().equals(ComponentMilestoneTypes.END)
                                            && !oldMilestone.getType().equals(ComponentMilestoneTypes.START))
                                        oldMilestone.setPosition(position);

                                    if (oldMilestone.getType().equals(ComponentMilestoneTypes.START))
                                        oldMilestone.setPosition(1);

                                    oldMilestone.update();
                                    position++;
                                } else {
                                    if (oldMilestone.getType().equals(ComponentMilestoneTypes.START)
                                            && oldMilestone.getPosition() != 1) {
                                        oldMilestone.setPosition(1);
                                        oldMilestone.update();
                                    }
                                }
                            } else {
                                // add new milestone
                                Logger.debug("Creating new milestone: " + updateM.getTitle());
                                updateM.setPosition(position);
                                updateM.setType(ComponentMilestoneTypes.REMINDER);
                                position++;
                                List<String> mappingFiles = Play.application().configuration()
                                        .getStringList("appcivist.dozer.mappingFiles");
                                DozerBeanMapper mapper = new DozerBeanMapper(mappingFiles);
                                ComponentMilestone updatedMObject = mapper.map(updateM, ComponentMilestone.class);
                                ComponentMilestone.create(updatedMObject);
                                componentOld.getResourceSpace().getMilestones().add(updatedMObject);
                            }
                        }

                        List<ComponentMilestoneTransfer> deletedMilestonesList = component.getDeletedMilestones();
                        for (ComponentMilestoneTransfer milestone : deletedMilestonesList) {
                            if (milestone.getComponentMilestoneId() != null) {
                                ComponentMilestone oldMilestone = ComponentMilestone.read(milestone.getComponentMilestoneId());
                                if (oldMilestone != null && !oldMilestone.getType().equals(ComponentMilestoneTypes.END)
                                        && !oldMilestone.getType().equals(ComponentMilestoneTypes.START)) {

                                    oldMilestone.setRemoved(true);
                                    oldMilestone.setRemoval(new Date());
                                    oldMilestone.update();
                                }
                            }
                        }


                        Logger.debug("Update component space: " + componentOld.getComponentId());
                        componentOld.getResourceSpace().update();
                        componentOld.refresh();
                        componentOld.getMilestones().sort(Comparator.comparing(ComponentMilestone::getDate));
                        int pos = 1;
                        for(ComponentMilestone cm: componentOld.getMilestones()) {

                            if (cm.getType().equals(ComponentMilestoneTypes.START)) {
                                cm.setPosition(1);
                            } else if (cm.getType().equals(ComponentMilestoneTypes.END)) {
                                    cm.setPosition(componentOld.getMilestones().size());
                            } else {
                                pos ++;
                                cm.setPosition(pos);
                            }
                            cm.update();
                        }
                    } else {
                        List<String> mappingFiles = Play.application().configuration()
                                .getStringList("appcivist.dozer.mappingFiles");
                        DozerBeanMapper mapper = new DozerBeanMapper(mappingFiles);
                        Component c = mapper.map(component, Component.class);
                        Component.create(campaignId, c);
                        campaignOld.getResources().addComponent(c);
                        campaignOld.getResources().update();
                        componentList.add(c);
                    }
                }

                List<CampaignTimelineEdge> timelineEdges = new ArrayList<>();
                int edges = 0;
                Set<Component> componentsSet = new LinkedHashSet<>();
                componentsSet.addAll(componentList);
                for (Component component : componentsSet) {

                    CampaignTimelineEdge edge = new CampaignTimelineEdge();
                    edge.setCampaign(campaignOld);
                    if (edges == 0) {
                        edge.setFromComponent(component);
                        edge.setStart(true);
                        timelineEdges.add(edge);
                        edges++;
                    } else {
                        if (edges < componentList.size() - 1) {
                            edge.setFromComponent(component);
                            timelineEdges.add(edge);
                        }
                        CampaignTimelineEdge prevEdge = timelineEdges.get(edges - 1);
                        prevEdge.setToComponent(component);
                        edges++;
                    }
                }
                for (CampaignTimelineEdge edge : campaignOld.getTimelineEdges()
                        ) {
                    edge.delete();
                }
                List<CampaignTimelineEdge> timelineEdgesLoaded = new ArrayList<>();
                for (CampaignTimelineEdge edge : timelineEdges
                        ) {
                    edge.save();
                    edge.refresh();
                    timelineEdgesLoaded.add(edge);
                }
                campaignOld.setComponents(componentList);
                campaignOld.setTimelineEdges(timelineEdgesLoaded);
                campaignOld.update();
                campaignOld.refresh();
                Logger.info("Updating campaign");
                Logger.debug("=> " + newCampaignForm.toString());
                ResourceSpace rs = Assembly.read(aid).getResources();

                /*Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(ResourceSpaceTypes.ASSEMBLY, NotificationEventName.UPDATED_CAMPAIGN, rs, campaignOld);
                });*/

                return ok(Json.toJson(campaignOld));
            } catch (Exception e) {
                Logger.info("Error updating campaign: "+e.getLocalizedMessage());
                return internalServerError(Json.toJson(new TransferResponseStatus(ResponseStatus.SERVERERROR,
                        "Error updating campaign: "+e.getLocalizedMessage(), e.getStackTrace().toString())));
            }
        }
    }

    @ApiOperation(httpMethod = "PUT", response = CampaignTransfer.class, produces = "application/json", value = "Create a Campaign Resources", notes = "Only for COORDINATORS. The templates will be used to import all the resources from a list of existing campaigns to the new")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Campaign simplified object", value = "Campaign in json", dataType = "models.transfer.CampaignTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)

    public static Result createResources(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
        try {
            final Form<CampaignTransfer> newCampaignForm = CAMPAIGN_TRANSFER_FORM
                    .bindFromRequest();

            if (newCampaignForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
                        newCampaignForm.errorsAsJson()));
                Ebean.rollbackTransaction();
                return badRequest(Json.toJson(responseBody));
            } else {
                CampaignTransfer campaignTransfer = newCampaignForm.get();
                campaignTransfer.setCampaignId(cid);
                CampaignTransfer aRet = CampaignDelegate.createResources(campaignTransfer);
                return ok(Json.toJson(aRet));
            }

        } catch (Exception e) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
                    "There was an internal error: " + e.getMessage()));
            e.printStackTrace();
            return internalServerError(Json.toJson(responseBody));
        }
    }

    @ApiOperation(httpMethod = "PUT", response = CampaignTransfer.class, produces = "application/json", value = "Cahnge campaign status to PUBLISHED", notes = "Only for COORDINATORS.")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)

    public static Result publish(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
        try {
            Campaign campaign = Campaign.read(cid);
            if (campaign == null) {
                return notFound();
            }
            return ok(Json.toJson(CampaignDelegate.publish(cid)));
        } catch (Exception e) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
                    "There was an internal error: " + e.getMessage()));
            e.printStackTrace();
            return internalServerError(Json.toJson(responseBody));
        }
    }

    /**
     * POST /api/assembly/:aid/campaign
     * Create a new Campaign
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = CampaignTransfer.class, produces = "application/json", value = "Create a new Campaign", notes = "Only for COORDINATORS.")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Campaign simplified object", value = "Campaign in json", dataType = "models.transfer.CampaignTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createCampaignInAssembly(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "templates", value = "List of campaign ids (separated by comma) to use as template for the current campaign") String templates) {
        try {
            Ebean.beginTransaction();
            // 1. obtaining the user of the requestor
            User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate
                    .getUser(session()));
            // 2. read the new campaign data from the body
            // another way of getting the body content => request().body().asJson()
            final Form<CampaignTransfer> newCampaignForm = CAMPAIGN_TRANSFER_FORM
                    .bindFromRequest();

            if (newCampaignForm.hasErrors()) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
                        newCampaignForm.errorsAsJson()));
                Ebean.rollbackTransaction();
                return badRequest(Json.toJson(responseBody));
            } else {
                CampaignTransfer campaignTransfer = newCampaignForm.get();
                CampaignTransfer newCampaign = CampaignDelegate.create(
                        campaignTransfer, campaignCreator, aid, templates);
                Ebean.commitTransaction();
                Campaign c = Campaign.read(newCampaign.getCampaignId());

                System.out.println("=== AFTER PERSIST == " + c.getAssemblies().size() );

                /*Assembly rs = Assembly.read(aid);


                Promise.promise(() -> {
                	try {
                        NotificationsDelegate.createNotificationEventsByType(ResourceSpaceTypes.CAMPAIGN.toString(), newCampaign.getUuid());
                    } catch (ConfigurationException e) {
                        Logger.error("Configuration error when creating notification events for contribution: " + LogActions.exceptionStackTraceToString(e));
                    } catch (Exception e) {
                        Logger.error("Error when notification creating events for contribution: " + LogActions.exceptionStackTraceToString(e));
                    }
                    return NotificationsDelegate.signalNotification(ResourceSpaceTypes.ASSEMBLY, NotificationEventName.NEW_CAMPAIGN, rs, c);
                });*/

                return ok(Json.toJson(newCampaign));
            }
        } catch (Exception e) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
                    "There was an internal error: " + e.getMessage()));
            Ebean.rollbackTransaction();
            e.printStackTrace();
            return internalServerError(Json.toJson(responseBody));
        }
    }

    /**
     * GET /api/assembly/:uuid/campaign
     * Given an Assembly Universal ID (uuid), return its campaigns
     *
     * @param uuid
     * @param filter
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json",
            value = "Given an Assembly Universal ID (uuid), return its campaigns",
            notes = "Only for MEMBERS of the assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "Campaigns not found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
    })
//    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.USER_RESOURCE_PATH)
    public static Result findCampaignsByAssemblyUUID(
            @ApiParam(name = "uuid", value = "Assembly's Universal ID") UUID uuid,
            @ApiParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") String filter) {
        if (filter == null || filter.equals("ongoing")) {
            return ongoingCampaignsByAssembly(uuid);
        } else if (filter.equals("past")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else if (filter.equals("future")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else {
            Assembly a = Assembly.readByUUID(uuid);
            List<Campaign> campaigns = a.getResources().getCampaigns();
            if (!campaigns.isEmpty())
                return ok(Json.toJson(campaigns));
            else
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No campaign found")));
        }
    }

    /**
     * GET /api/user/:uid/campaign
     * Given a users local ID (uid), returns an array of campaigns that are organized by that user's assemblies
     *
     * @param uid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json",
            value = "Given a users local ID (uid), returns an array of campaigns that are organized by that user's assemblies",
            notes = "Only for SELF")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Campaign Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String",
                    paramType = "header")
    })
    @Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
    public static Result campaignsByUserId(
            @ApiParam(name = "uid", value = "User's ID") Long uid,
            @ApiParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") String filter,
            @ApiParam(name = "assembly", value = "Assembly ID") Long aid) {
        if (filter == null || filter.equals("ongoing")) {
            return ongoingCampaignsByUserId(uid, aid!=null && aid>0 ? aid : null);
        } else if (filter.equals("past")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else if (filter.equals("future")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else {
            User u = User.read(uid);
            List<Membership> assemblyMemberships = aid != null && aid > 0?  
            				Membership.findByUserAndAssembly(u, aid)
            				: Membership.findByUser(u, "ASSEMBLY");
            List<Campaign> campaigns = new ArrayList<Campaign>();
            for (Membership membership : assemblyMemberships) {
                Assembly a = ((MembershipAssembly) membership).getAssembly();
                campaigns.addAll(a.getResources().getCampaigns());
            }
            if (!campaigns.isEmpty())
                return ok(Json.toJson(campaigns));
            else
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No ongoing campaigns")));
        }
    }

    /**
     * GET /api/user/:uuid/campaign
     * Given a users universal ID (uuid), returns an array of campaigns that are organized by that user's assemblies
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Campaign.class, responseContainer = "List", produces = "application/json",
            value = "Given a users universal ID (uuid), returns an array of campaigns that are organized by that user's assemblies",
            notes = "Only for SELF")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Campaign Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")
    })
    @Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
    public static Result campaignsByUserUuid(
            @ApiParam(name = "uuid", value = "User's Universal ID") UUID uuid,
            @ApiParam(name = "filter", value = "Filter campaign by status (ongoing, past, upcoming, all)", allowableValues = "ongoing,past,future,all", defaultValue = "ongoing") String filter,
            @ApiParam(name = "assembly", value = "Assembly UUID") UUID auuid) {
        if (filter == null || filter.equals("ongoing")) {
            return ongoingCampaignsByUserUuid(uuid, auuid!=null ? auuid : null);
        } else if (filter.equals("past")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else if (filter.equals("future")) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    "Not implemented")));
        } else {
            User u = User.findByUUID(uuid);
            Assembly target = Assembly.readByUUID(auuid);
            List<Membership> assemblyMemberships = auuid != null ?  
    				Membership.findByUserAndAssembly(u, target.getAssemblyId())
    				: Membership.findByUser(u, "ASSEMBLY");
            List<Campaign> campaigns = new ArrayList<Campaign>();
            for (Membership membership : assemblyMemberships) {
                Assembly a = ((MembershipAssembly) membership).getAssembly();
                campaigns.addAll(a.getResources().getCampaigns());
            }
            if (!campaigns.isEmpty())
                return ok(Json.toJson(campaigns));
            else
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No ongoing campaigns")));
        }
    }

    /**
     * GET /api/assembly/:aid/campaign/:cid/template
     * Get list of available contribution templates in a campaign
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Resource.class, responseContainer = "List", produces = "application/json", value = "Get list of available campaign templates", notes = "Get list of available campaign templates")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Campaign Template Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),})
    @SubjectPresent
    public static Result findContributionTemplatesInCampaign(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid) {
        // Must return resource, padTransfer, writable url or read only url??
        Campaign campaign = Campaign.read(cid);
        List<Resource> cts = campaign.getResources().getResources().stream().filter((r) ->
                ResourceTypes.CONTRIBUTION_TEMPLATE.equals(r.getResourceType())).collect(Collectors.toList());
        if (cts != null && !cts.isEmpty()) {
            //List<URL> urls = cts.stream().map(sc -> sc.getUrl()).collect(Collectors.toList());
            return ok(Json.toJson(cts));
        } else {
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No campaign templates")));
        }
    }

    /**
     * GET /api/campaign/template
     * Get list of available campaign templates
     *
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CampaignTemplate.class, responseContainer = "List", produces = "application/json", value = "Get list of available campaign templates", notes = "Get list of available campaign templates")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Campaign Template Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),})
    @SubjectPresent
    public static Result findCampaignTemplates() {
        List<CampaignTemplate> cts = CampaignTemplate.findAll();
        if (cts != null && !cts.isEmpty()) {
            return ok(Json.toJson(cts));
        } else {
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No campaign templates")));
        }
    }

    /**
     * GET /api/template/campaign/default
     * Get list of default campaign templates
     *
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, produces = "application/json", value = "Get list of default templates", notes = "Get list of default templates")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Default Template Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),})
    @SubjectPresent
    public static Result findCampaignTemplatesDefault() {
        InputStream inputStream = Play.application().classloader().getResourceAsStream("initial-data/configs/defaultCampaignTimeline.json");

        if (inputStream != null) {
            JsonNode jsonNodeArray = Json.parse(inputStream);
            for (JsonNode jsonNode : jsonNodeArray) {
                JsonNode arrNode = jsonNode.get("milestones");
                String date = arrNode.get(0).get("date").asText();
                System.out.println(date);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date parsedDate = df.parse(date);
                    Timestamp timestamp = new Timestamp(parsedDate.getTime());
                    Timestamp timestampNow = new Timestamp(new Date().getTime());
                    Long diff = timestampNow.getTime() - timestamp.getTime();
                    Long days = diff /(1000  * 60 * 60 * 24);
                    System.out.println("Diff "+days);
                    if (arrNode.isArray()) {
                        for (JsonNode objNode : arrNode) {
                            String dateMilestone = objNode.get("date").asText();
                            System.out.println(dateMilestone);
                            Date parsedDateMilestone = df.parse(dateMilestone);
                            GregorianCalendar cal = new GregorianCalendar();
                            cal.setTime(parsedDateMilestone);
                            cal.add(Calendar.DATE, days.intValue());
                            ((ObjectNode)objNode).put("date", df.format(new Timestamp(cal.getTimeInMillis())));
                        }
                    }
                } catch (ParseException e) {
                    return notFound(Json.toJson(new TransferResponseStatus(
                            "Format date error " + e.getMessage())));
                }
            }

            return ok(jsonNodeArray);
        } else {
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No campaign default templates")));
        }
    }

    /**
     * GET /api/template/assembly/default
     * Get list of default campaign templates
     *
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, produces = "application/json", value = "Get list of default templates", notes = "Get list of default templates")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Default Template Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),})
    @SubjectPresent
    public static Result findAssemblyTemplatesDefault() {
        InputStream inputStream = Play.application().classloader().getResourceAsStream("initial-data/configs/assemblyConfigDefinitions.json");

        if (inputStream != null) {
            JsonNode jsonNodeArray = Json.parse(inputStream);
            return ok(jsonNodeArray);
        } else {
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No assembly default templates")));
        }
    }

    /**
     * GET /api/template/group/default
     * Get list of default campaign templates
     *
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, produces = "application/json", value = "Get list of default templates", notes = "Get list of default templates")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Default Template Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({@ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),})
    @SubjectPresent
    public static Result findGroupTemplatesDefault() {
        InputStream inputStream = Play.application().classloader().getResourceAsStream("initial-data/configs/workingGroupConfigDefinitions.json");

        if (inputStream != null) {
            JsonNode jsonNodeArray = Json.parse(inputStream);
            return ok(jsonNodeArray);
        } else {
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No group default templates")));
        }
    }
    // Private not exposed Methods

    /**
     * Get campaigns that are ongoing given an User's Universal ID (uuid)
     *
     * @param uuid User's Universal ID (uuid)
     * @return
     */
    private static Result ongoingCampaignsByUserUuid(UUID uuid, UUID assemblyUuid) {
        User u = User.findByUUID(uuid);
        Assembly a = assemblyUuid !=null ? Assembly.readByUUID(assemblyUuid) : null;
        return ongoingCampaignsByUser(u, a !=null ? a.getAssemblyId() : null);
    }

    /**
     * Get campaigns that are ongoing given an User's Local ID (uid)
     *
     * @param uid User's Local Id
     * @return
     */
    private static Result ongoingCampaignsByUserId(Long uid, Long assemblyId) {
        User u = User.read(uid);
        return ongoingCampaignsByUser(u, assemblyId);
    }

    /**
     * Get campaigns that are ongoing given an User object
     *
     * @param u User's object
     * @return
     */
    private static Result ongoingCampaignsByUser(User u, Long assemblyId) {
        List<Membership> assemblyMemberships = assemblyId != null ? 
        		Membership.findByUserAndAssembly(u, assemblyId) 
        		: Membership.findByUser(u,"ASSEMBLY");
        List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();

        for (Membership membership : assemblyMemberships) {
            Long aid = ((MembershipAssembly) membership).getAssembly()
                    .getAssemblyId();
            List<Campaign> ongoing = Campaign
                    .getOngoingCampaignsFromAssembly(aid);
            if (ongoing != null)
                ongoingCampaigns.addAll(ongoing);
        }

        if (!ongoingCampaigns.isEmpty())
            return ok(Json.toJson(ongoingCampaigns));
        else
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No ongoing campaigns")));
    }

    /**
     * Get campaigns that are ongoing given an Assembly's Universal ID (uuid)
     *
     * @param uuid
     * @return
     */
    private static Result ongoingCampaignsByAssembly(UUID uuid) {
        Assembly a = Assembly.readByUUID(uuid);
        List<Campaign> ongoingCampaigns = new ArrayList<Campaign>();
        try {
            ongoingCampaigns.addAll(Campaign.getOngoingCampaignsFromAssembly(a));
        } catch (Exception e) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    e.getMessage())));
        }
        if (!ongoingCampaigns.isEmpty())
            return ok(Json.toJson(ongoingCampaigns));
        else
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No ongoing campaigns")));
    }

    /**
     * Get campaigns that are ongoing given an Assembly's Universal ID (uuid)
     *
     * @param
     * @return
     */
    private static Result ongoingCampaignsByAssemblyId(Long aid) {
        Assembly a = Assembly.read(aid);
        List<Campaign> ongoingCampaigns = new ArrayList<>();
        try {
            ongoingCampaigns.addAll(Campaign.getOngoingCampaignsFromAssembly(a));
        } catch (Exception e) {
            return internalServerError(Json.toJson(new TransferResponseStatus(
                    e.getMessage())));
        }
        if (!ongoingCampaigns.isEmpty())
            return ok(Json.toJson(ongoingCampaigns));
        else
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No ongoing campaigns")));
    }


    
    /** TODO For templates and contribution, the resources (and related pad) not confirmed after x time must be eliminated **/

    /**
     * POST /api/assembly/:aid/campaign/:cid/contribution/template
     * Create a new Resource CONTRIBUTION_TEMPLATE
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Resource.class, value = "Create a new Contribution Template for the campaign", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            // TODO: change the following to a more appropriate model, use this instead of the query parameter text
            @ApiImplicitParam(name = "text", value = "Template Body Text", dataType = "String", paramType = "body")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createContributionTemplateInCampaign(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
        User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        Resource res = ResourcesDelegate.createResource(campaignCreator, "", ResourceTypes.CONTRIBUTION_TEMPLATE, false, false);
        Campaign campaign = Campaign.read(campaignId);
        campaign.getResourceList().add(res);
        Campaign.update(campaign);
        if (res != null) {
            return ok(Json.toJson(res));
        } else {
            return internalServerError("The HTML text is malformed.");
        }

    }

    /**
     * PUT       /api/assembly/:aid/campaign/:cid/contribution/template/:rid
     * Confirms a Resource CONTRIBUTION_TEMPLATE
     *
     * @param aid
     * @param campaignId
     * @param rid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Resource.class, value = "Confirm Contribution Template")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result confirmContributionTemplateInCampaign(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId,
            @ApiParam(name = "Resource ID", value = "Contribution Template") Long rid) {
        Resource res = ResourcesDelegate.confirmResource(rid);

        return ok(Json.toJson(res));
    }

    /**
     * DELETE /api/assembly/:aid/campaign/:cid/contribution/template/:rid
     * Delete campaign by ID
     *
     * @param aid        assembly id
     * @param cid        campaign id
     * @param resourceId id of resource to delete
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Campaign.class, produces = "application/json", value = "Delete campaign cotribution template", notes = "Only for COORDINATOS of assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result deleteContributionTemplateInCampaign(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "rid", value = "Resource ID") Long resourceId) {
        Resource.delete(resourceId);

        return ok();
    }

    /**
     * POST /api/assembly/:aid/campaign/:cid/resource
     * Create a new Resource CONTRIBUTION_TEMPLATE
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Resource.class, value = "Create a new Resource for the campaign", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createCampaignResource(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
        User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        final Form<Resource> resourceForm = form(Resource.class).bindFromRequest();
        if (resourceForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
                    resourceForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            Resource newResource = resourceForm.get();
            newResource.setConfirmed(true);
            newResource.setContextUserId(campaignCreator.getUserId());
            newResource = Resource.create(newResource);
            Campaign campaign = Campaign.read(campaignId);
            campaign.getResourceList().add(newResource);
            Campaign.update(campaign);
            return ok(Json.toJson(newResource));
        }

    }

    /**
     * GET /api/assembly/:aid/campaign/:cid/resources
     * Returns the Resources associated to the campaign
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Resource.class, responseContainer = "List", value = "Lists campaign's resources", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result listCampaignResources(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        Campaign campaign = Campaign.read(campaignId);
        List<Resource> resources;
        if (all != null) {
            resources = campaign.getResourceList();
        } else {
            resources = campaign.getPagedResources(page, pageSize);
        }
        return ok(Json.toJson(resources));

    }

    /**
     * GET /api/campaign/:uuid/resources
     * Returns the Resources associated to the campaign
     *
     * @param campaignUUID
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Resource.class, responseContainer = "List", value = "Lists campaign's resources", notes = "Public View")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    public static Result listPublicCampaignResources(
            @ApiParam(name = "uuid", value = "Campaign UUID") UUID campaignUUID,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {

        try {
            if (pageSize == null) {
                pageSize = GlobalData.DEFAULT_PAGE_SIZE;
            }
            Campaign campaign = Campaign.readByUUID(campaignUUID);
            List<Resource> resources;
            if (all != null) {
                resources = campaign.getResourceList();
            } else {
                resources = campaign.getPagedResources(page, pageSize);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(resources);

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
        } catch (Exception e) {
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }

    }

    /**
     * GET /api/assembly/:aid/campaign/:cid/components
     * Returns the Components associated to the campaign
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, responseContainer = "List", value = "Lists campaign's components", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result listCampaignComponents(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        Campaign campaign = Campaign.read(campaignId);
        List<Component> components;
        if (all != null) {
            components = campaign.getComponentsByTimeline();
        } else {
            components = campaign.getPagedComponents(page, pageSize);
        }
        return ok(Json.toJson(components));

    }

    /**
     * GET /api/campaign/:uuid/components
     * Returns the Components associated to the campaign
     *
     * @param campaignUUID
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Component.class, responseContainer = "List", value = "Lists campaign's components", notes = "Public View")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    public static Result listPublicCampaignComponents(
            @ApiParam(name = "uuid", value = "Campaign UUID") UUID campaignUUID,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {

        try {
            if (pageSize == null) {
                pageSize = GlobalData.DEFAULT_PAGE_SIZE;
            }
            Campaign campaign = Campaign.readByUUID(campaignUUID);
            List<Component> components;
            if (all != null) {
                components = campaign.getComponentsByTimeline();
            } else {
                components = campaign.getPagedComponents(page, pageSize);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(components);

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
        } catch (Exception e) {
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }


    }

    /**
     * GET /api/assembly/:aid/campaign/:cid/themes
     * Returns the Themes associated to the campaign
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Theme.class, responseContainer = "List", value = "Lists campaign's themes", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result listCampaignThemes(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize,
            @ApiParam(name = "themeType", value = "String") String themeType,
            @ApiParam(name = "query", value = "String") String query) {
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        //Campaign campaign = Campaign.read(campaignId);
        List<Theme> themes;
        if (all != null) {
            themes = Campaign.getThemesByCampaignIdAndType(campaignId, themeType, null, null, query);
        } else {
            themes = Campaign.getThemesByCampaignIdAndType(campaignId, themeType, page, pageSize, query);
        }
        return ok(Json.toJson(themes));
    }

    /**
     * GET /api/campaign/:uuid/themes
     * Returns the Themes associated to the campaign
     *
     * @param campaignUUID
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Theme.class, responseContainer = "List", value = "Lists campaign's themes", notes = "Public View")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    public static Result listPublicCampaignThemes(
            @ApiParam(name = "uuid", value = "Campaign UUID") UUID campaignUUID,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {

        try {
            if (pageSize == null) {
                pageSize = GlobalData.DEFAULT_PAGE_SIZE;
            }
            Campaign campaign = Campaign.readByUUID(campaignUUID);
            List<Theme> themes;
            if (all != null) {
                themes = campaign.getThemes();
            } else {
                themes = campaign.getPagedThemes(page, pageSize);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(themes);

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
        } catch (Exception e) {
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }

    }

    /**
     * GET /api/assembly/:aid/campaign/:cid/groups
     * Returns the Working groups associated to the campaign
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer = "List", value = "Lists campaign's working groups", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result listCampaignGroups(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {

        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        Campaign campaign = Campaign.read(campaignId);
        List<WorkingGroup> workingGroups;
        if (all != null) {
            workingGroups = campaign.getWorkingGroups();
        } else {
            workingGroups = campaign.getPagedWorkingGroups(page, pageSize);
        }
        return ok(Json.toJson(workingGroups));

    }

    /**
     * GET /api/campaign/:uuid/groups
     * Returns the Working groups associated to the campaign
     *
     * @param campaignUUID
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = WorkingGroup.class, responseContainer = "List", value = "Lists campaign's working groups", notes = "Public view")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    public static Result listPublicCampaignGroups(
            @ApiParam(name = "uuid", value = "Campaign UUID") UUID campaignUUID,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {

        try {
            if (pageSize == null) {
                pageSize = GlobalData.DEFAULT_PAGE_SIZE;
            }
            Campaign campaign = Campaign.readByUUID(campaignUUID);
            List<WorkingGroup> workingGroups;
            if (all != null) {
                workingGroups = campaign.getWorkingGroups();
            } else {
                workingGroups = campaign.getPagedWorkingGroups(page, pageSize);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(workingGroups);

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
        } catch (Exception e) {
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }

    }

    /**
     * GET /api/assembly/:aid/campaign/:cid/timeline
     * Returns the CampaignTimelineEdges associated to the campaign
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CampaignTimelineEdge.class, responseContainer = "List", value = "Lists campaign's timeline", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result listCampaignTimeline(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {

        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        List<CampaignTimelineEdge> campaignTimelineEdges;
        Campaign campaign = Campaign.read(campaignId);
        if (all != null) {
            campaignTimelineEdges = campaign.getTimelineEdges();
        } else {
            campaignTimelineEdges = campaign.getPagedTimelineEdges(page, pageSize);
        }
        return ok(Json.toJson(campaignTimelineEdges));
    }

    /**
     * GET /api/public/campaign/:uuid/brief
     * Returns the Campaign brief: an html/svg TEXT that will be displayed as a welcome
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CampaignBriefTransfer.class, value = "Public endpoint for reading campaign's html/svg brief")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No Brief Found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result getCampaignBrief(
            @ApiParam(name = "uuid", value = "Campaign UUID") UUID uuid) {
        String brief = Campaign.getCampaignBriefByCampaignId(uuid);

        if (brief != null) {
            CampaignBriefTransfer cbt = new CampaignBriefTransfer();
            cbt.setCampaignUuid(uuid);
            cbt.setBrief(brief);
            return ok(Json.toJson(cbt));
        } else {
            return notFound(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Brief not found for campaign "+uuid))));
        }

    }



    /**
     * GET /api/campaign/:uuid/timeline
     * Returns the CampaignTimelineEdges associated to the campaign
     *
     * @param campaignUUID
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = CampaignTimelineEdge.class, responseContainer = "List", value = "Lists campaign's timeline", notes = "Public view")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Resource Object", value = "The new Resource in JSON", dataType = "models.Resource", paramType = "body")})
    public static Result listPublicCampaignTimeline(
            @ApiParam(name = "uuid", value = "Campaign UUID") UUID campaignUUID,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {

        try {


            if (pageSize == null) {
                pageSize = GlobalData.DEFAULT_PAGE_SIZE;
            }
            List<CampaignTimelineEdge> campaignTimelineEdges;
            Campaign campaign = Campaign.readByUUID(campaignUUID);
            if (all != null) {
                campaignTimelineEdges = campaign.getTimelineEdges();
            } else {
                campaignTimelineEdges = campaign.getPagedTimelineEdges(page, pageSize);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(campaignTimelineEdges);

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
        } catch (Exception e) {
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }


    }

    /**
     * GET /api/space/:sid/resources
     * Returns the Resources associated to the resource space
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Resource.class, responseContainer = "List", value = "Lists resource space's resources")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceResources(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid) {
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        List<Resource> resources;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            resources = resourceSpace.getResources();
        }
        return ok(Json.toJson(resources));

    }

    /**
     * GET /api/space/:sid/resources/:rid
     *
     * @param sid
     * @param rid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Resource.class, value = "Resource in a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result listSpaceResourcesById(@ApiParam(name = "sid", value = "ResourceSpace ID") Long sid,
                                                @ApiParam(name = "rid", value = "Resource ID") Long rid) {
        ResourceSpace resourceSpace = ResourceSpace.findByResource(sid,rid);
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with id "+sid)));
        } else {
            Resource resource = Resource.read(rid);
            return ok(Json.toJson(resource));
        }
    }

    /**
     * GET /api/space/:uuid/resources
     * Returns the Resources associated to the resource space
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Resource.class, responseContainer = "List", value = "Lists resource space's resources")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    public static Result listSpaceResourcesbyUuid(@ApiParam(name = "uuid", value = "ResourceSpace UUID") UUID uuid) {
        ResourceSpace resourceSpace = ResourceSpace.readByUUID(uuid);
        List<Resource> resources;
        if (resourceSpace == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No resource space found with uuid "+uuid)));
        } else {
            resources = resourceSpace.getResources();
            String result;
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
                result  = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(resources);
            } catch (Exception e) {
                e.printStackTrace();
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No resources with this space uuid")));
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
     * PUT /api/assembly/:aid/campaign/:cid/description
     * Update campaign description by ID
     *
     * @param aid
     * @param campaignId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update a campaign description by its ID and the assembly ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Campaign object", value = "Campaign in json", dataType = "models.Campaign", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateCampaignDescription(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long campaignId) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Campaign> newCampaignForm = CAMPAIGN_FORM.bindFromRequest();
        if (newCampaignForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CAMPAIGN_CREATE_MSG_ERROR,
                    newCampaignForm.errorsAsJson()));
            Logger.info("Error updating campaign");
            Logger.debug("=> " + newCampaignForm.errorsAsJson());
            return badRequest(Json.toJson(responseBody));
        } else {
            Campaign updatedCampaign = newCampaignForm.get();
            Campaign loadedCampaign = Campaign.read(campaignId);
            if(loadedCampaign==null){
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage("Campaign not found with id: "+ campaignId);
                return badRequest(Json.toJson(responseBody));
            }
            loadedCampaign.setTitle(updatedCampaign.getTitle());
            loadedCampaign.update();
            Logger.info("Updating campaign");
            Logger.debug("=> " + newCampaignForm.toString());
            Assembly rs = Assembly.read(aid);
            Campaign c = Campaign.read(loadedCampaign.getCampaignId());
            /*Promise.promise(() -> {
                return NotificationsDelegate.signalNotification(ResourceSpaceTypes.ASSEMBLY, NotificationEventName.UPDATED_CAMPAIGN, rs, c);
            });*/

            return ok(Json.toJson(loadedCampaign));
        }
    }

    /**
     * PUT /api/assembly/:aid/campaign/:cid/group/:gid/description
     * Update campaign description by ID
     *
     * @param aid
     * @param cid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = WorkingGroup.class, produces = "application/json", value = "Update a group description by its ID", notes = "Only for COORDINATORS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Group object", value = "Group in json", dataType = "models.WorkingGroup", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result updateCampaignGroupDescription(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "gid", value = "Group ID") Long gid) {
        // 1. read the campaign data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<WorkingGroup> newWorkingGroupForm = GROUP_FORM.bindFromRequest();
        if (newWorkingGroupForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.GROUP_CREATE_MSG_ERROR,
                    newWorkingGroupForm.errorsAsJson()));
            Logger.info("Error updating group");
            Logger.debug("=> " + newWorkingGroupForm.errorsAsJson());
            return badRequest(Json.toJson(responseBody));
        } else {
            WorkingGroup updatedGroup = newWorkingGroupForm.get();
            WorkingGroup loadedWorkingGroup = WorkingGroup.read(gid);
            if(loadedWorkingGroup==null){
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage("WorkingGroup not found with id: "+ gid);
                return badRequest(Json.toJson(responseBody));
            }
            loadedWorkingGroup.setName(updatedGroup.getName());
            loadedWorkingGroup.update();

            return ok(Json.toJson(loadedWorkingGroup));
        }
    }

}
