package controllers;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;
import delegates.ContributionsDelegate;
import delegates.NotificationsDelegate;
import delegates.ResourcesDelegate;
import delegates.WorkingGroupsDelegate;
import enums.*;
import exceptions.ConfigurationException;
import exceptions.MembershipCreationException;
import http.Headers;
import io.swagger.annotations.*;
import models.*;
import models.location.Location;
import models.misc.Views;
import models.transfer.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Lang;
import play.i18n.Messages;
import play.libs.F;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import play.twirl.api.Content;
import providers.MyUsernamePasswordAuthProvider;
import security.SecurityModelConstants;
import service.PlayAuthenticateLocal;
import utils.GlobalData;
import utils.GlobalDataConfigKeys;
import utils.LogActions;
import utils.Packager;
import utils.security.HashGenerationException;
import utils.services.EtherpadWrapper;
import utils.services.PeerDocWrapper;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.persistence.EntityNotFoundException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static play.data.Form.form;
import static security.CoordinatorOrAuthorDynamicResourceHandler.checkIfCoordinator;

@Api(value = "05 contribution: Contribution Making", description = "Contribution Making Service: contributions by citizens to different spaces of civic engagement")
@With(Headers.class)
public class Contributions extends Controller {

    public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);
    public static final Form<ContributionFeedback> CONTRIBUTION_FEEDBACK_FORM = form(ContributionFeedback.class);
    public static final Form<Resource> ATTACHMENT_FORM = form(Resource.class);
    public static final Form<ThemeListTransfer> THEMES_FORM = form(ThemeListTransfer.class);
    public static final Form<User> AUTHORS_FORM = form(User.class);
    public static final String CONTRIBUTION_ID_PARAM = "{contribution_id}";
    public static final String EXTENDED_PAD_NAME = "contribution_doc_"+ CONTRIBUTION_ID_PARAM;
    public static final String CONTRIBUTION_FILE_NAME = "contribution_"+ CONTRIBUTION_ID_PARAM;
    public static final Form<NonMemberAuthor> NON_MEMBER_AUTHORS_FORM = form(NonMemberAuthor.class);

    private static BufferedReader br;

    public enum CSVHeaders {
        code, source, title, description, category, keywords, date, location, group, authors, phones, emails
    }

    /**
     * GET       /api/assembly/:aid/contribution
     *
     * @param aid
     * @param space
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findAssemblyContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "space", value = "Resource space name within assembly from which we want to query contributions", allowableValues = "forum,resources", defaultValue = "forum") String space,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {
        Assembly a = Assembly.read(aid);
        ResourceSpace rs = null;
        if (a != null) {
            if (space != null && space.equals("forum"))
                rs = a.getForum();
            else
                rs = a.getResources();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);

        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No resource space for assembly: " + aid)));
    }


    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List",
            produces = "application/json", value = "Get contributions childrens or parent by type")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    public static Result getContributionChildrenOrParent(
            @ApiParam(name = "uuid", value = "Contribution UUID") UUID uuid,
            @ApiParam(name = "type", value = "Type of contributions",
                    allowableValues = "FORKS, MERGES, PARENT") String type) {

        List<Contribution> contributions = Contribution.findChildrenOrParents(uuid, type);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for contribution: " + uuid)));
    }


    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List",
            produces = "application/json", value = "Get contributions childrens or parent by type")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    public static Result getContributionMergeAuthors(
            @ApiParam(name = "uuid", value = "Contribution UUID") UUID uuid) {

        Set<User> contributions = Contribution.findMergeAuthors(uuid);
        return contributions == null || contributions.isEmpty() ? notFound(Json.toJson(new TransferResponseStatus(
                "No authors for contribution: " + uuid))) : ok(Json.toJson(contributions));
    }

        /**
         * GET       /api/assembly/:aid/campaign/:cid/component/:ciid/contribution
         *
         * @param aid
         * @param cid
         * @param ciid
         * @param type
         * @return
         */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List",
            produces = "application/json", value = "Get contributions in a component of a campaign within an assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findCampaignComponentContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "ciid", value = "Component ID") Long ciid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {
        Component c = Component.read(cid, ciid);
        ResourceSpace rs = null;
        if (c != null) {

            rs = c.getResourceSpace();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for {assembly, campaign, component}: " + aid + ", " + cid + ", " + ciid)));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution
     *
     * @param aid
     * @param cid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in a Campaign")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findCampaignContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {

        Campaign c = Campaign.read(cid);
        ResourceSpace rs = null;
        if (c != null) {
            rs = c.getResources();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for {assembly, campaign}: " + aid + ", " + cid)));
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/contribution
     *
     * @param aid
     * @param gid
     * @param space
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contributions in a Working Group")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findAssemblyGroupContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid,
            @ApiParam(name = "space", value = "Resource space name within the working group from which we want to query contributions", allowableValues = "forum,resources", defaultValue = "forum") String space,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note, discussion", defaultValue = "idea") String type) {
        WorkingGroup wg = WorkingGroup.read(gid);
        ResourceSpace rs = null;
        if (wg != null) {
            if (space != null && space.equals("forum"))
                rs = wg.getForum();
            else
                rs = wg.getResources();
        }
        List<Contribution> contributions = ContributionsDelegate
                .findContributionsInResourceSpace(rs, type, null);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No resource space for assembly: " + aid)));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, produces = "application/json", value = "Get contribution by ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {

        Contribution contribution = Contribution.read(contributionId);
        if(contribution.getExtendedTextPad() != null && contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.PEERDOC)) {
            User user = User.findByAuthUserIdentity(PlayAuthenticate
                    .getUser(session()));

                PeerDocWrapper peerDocWrapper = new PeerDocWrapper(user);

                try {
                    contribution.getExtendedTextPad().setUrl(new URL(contribution.getExtendedTextPad()
                            .getUrlAsString() + "?user=" + peerDocWrapper.encrypt()));

                } catch (Exception e) {
                    contribution.setErrorsInExtendedTextPad("Error reading the pad " + e.getMessage());
                    return ok(Json.toJson(contribution));
                }

        }
        return ok(Json.toJson(contribution));
    }

    /**
     * GET       /api/space/:sid/contribution
     *
     * @param sid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json",
            value = "Get contributions in a specific Resource Space",
            notes = "Every entity in AppCivist has a Resource Space to associate itself to other entities")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result findResourceSpaceContributions(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type,
            @ApiParam(name = "by_text", value = "String") String byText,
            @ApiParam(name = "by_location", value = "String") String byLocation,
            @ApiParam(name = "groups", value = "List") List<Integer> byGroup,
            @ApiParam(name = "themes", value = "List") List<Integer> byTheme,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "by_author", value = "Author ID") Integer authorId,
            @ApiParam(name = "page", value = "Page", defaultValue = "0") Integer page,
            @ApiParam(name = "pageSize", value = "Number of elements per page") Integer pageSize,
            @ApiParam(name = "sorting", value = "Ordering of proposals",
                    allowableValues = "date_asc, date_desc, random, popularity_asc, popularity_desc, most_commented_asc, most_commented_desc, most_commented_public_asc, most_commented_public_desc, most_commented_members_asc, most_commented_members_desc")
                    String sorting,
            @ApiParam(name = "status", value = "String") String status,
            @ApiParam(name = "format", value = "Export format", allowableValues = "JSON,CSV,TXT,PDF,RTF,DOC")
                    String format,
            @ApiParam(name = "includeExtendedText", value = "Include or not extended text") String includeExtendedText,
            @ApiParam(name = "extendedTextFormat", value = "Include or not extended text", allowableValues =
                    "JSON,CSV,TXT,PDF,RTF,DOC") String extendedTextFormat,
            @ApiParam(name = "collectionFileFormat", value = "Select the format for the file that contains the collection of contributions",
                    allowableValues = "JSON,CSV") String collectionFileFormat,
            @ApiParam(name = "selectedContributions", value = "Array of contribution IDs to get") List<String> selectedContributions,
            @ApiParam(name = "statusStartDate", value = "String") String statusStartDate,
            @ApiParam(name = "statusEndDate", value = "String") String statusEndDate,
            @ApiParam(name = "excludeCreatedByUser", value = "Array of created users to exclude IDs to get") List<Long>  excludeCreatedByUser,
            @ApiParam(name = "createdByOnly", value = "Include or not only creators authors" , defaultValue = "false") String createdByOnly)
    {

        User user = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        format = format.toUpperCase();
        extendedTextFormat = extendedTextFormat.toUpperCase();
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        boolean creatorOnly = false;
        if(createdByOnly != null && createdByOnly.equals("true")) {
            creatorOnly = true;
        }
        ResourceSpace rs = ResourceSpace.read(sid);
        List<Contribution> contributions;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("containingSpaces", rs.getResourceSpaceId());
        if (type != null && !type.isEmpty()) {
            ContributionTypes mappedType = ContributionTypes.valueOf(type.toUpperCase());
            conditions.put("type", mappedType);
        }
        if (byText != null && !byText.isEmpty()) {
            conditions.put("by_text", byText);
        }
        if (byLocation != null && !byLocation.isEmpty()) {
            conditions.put("by_location", byLocation);
        }
        if (authorId != null && authorId != 0) {
            conditions.put("by_author", authorId);
        }
        if (byGroup != null && !byGroup.isEmpty()) {
            conditions.put("group", byGroup);
        }
        if (excludeCreatedByUser != null && !excludeCreatedByUser.isEmpty()) {
            conditions.put("excludeCreatedByUser", excludeCreatedByUser);
        }
        if (byTheme != null && !byTheme.isEmpty()) {
            conditions.put("theme", byTheme);
        }
        if (sorting != null && !sorting.isEmpty()) {
            conditions.put("sorting", sorting);
        }
        if (selectedContributions != null && !selectedContributions.isEmpty()) {
            conditions.put("selectedContributions", selectedContributions);
        }
        if (status != null && !status.isEmpty()) {
            conditions.put("status", status);
        } else if (!rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
            conditions.put("status", "PUBLISHED,INBALLOT,SELECTED,PUBLIC_DRAFT");
        }
        try {
            if (statusEndDate != null && !statusEndDate.isEmpty()) {

                conditions.put("statusEndDate", dateFormat.parse(statusEndDate));
            }
            if (statusStartDate != null && !statusStartDate.isEmpty()) {
                conditions.put("statusStartDate", dateFormat.parse(statusStartDate));
            }
        } catch (ParseException e) {
            return badRequest(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.BADREQUEST,
                            "Error in date formatting: " + e.getMessage())));

        }

        PaginatedContribution pag = new PaginatedContribution();
        List<Contribution> contribs = ContributionsDelegate.findContributions(conditions, null, null, creatorOnly);
        contributions = ContributionsDelegate.findContributions(conditions, page, pageSize, creatorOnly);
        pag.setPageSize(pageSize);
        pag.setTotal(contribs.size());
        pag.setPage(page);

        if (all != null) {
            pag.setPageSize(pag.getTotal());
            pag.setPage(0);
            pag.setList(contribs);
            contributions.clear();
            contributions.addAll(contribs);
        } else {
            pag.setList(contributions);
        }
        if (contributions == null || contributions.isEmpty()) {
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No contributions for {resource space}: " + sid + ", type=" + type)));
        } else {
            Boolean sendMail = false;
            if (!(format.equals("JSON") || format.equals("CSV")) || includeExtendedText.toUpperCase().equals("TRUE")) {
                Logger.info("Format is not json or csv and include extendedtext is true, mail will be send");
                sendMail = true;
            }
            if (!sendMail) {
                Logger.info("Mail will not send");
                if (format.equals("JSON")) {
                    return ok(Json.toJson(pag));
                }

                if (format.equals("CSV")) {
                    response().setContentType("application/csv");
                    response().setHeader("Content-disposition", "attachment; filename=proposal.csv");
                    try {
                        Logger.info("Preparing CSV file");
                        File tempFile = File.createTempFile("contributions.csv", ".tmp");
                        CSVPrinter csvFilePrinter = null;
                        FileWriter fileWriter = new FileWriter(tempFile);

                        int first = 0;
                        for(Contribution contribution: contributions) {
                            Logger.info("Creating csv row for contribution " + contribution.getContributionId());
                            LinkedHashMap<String, String> contributionMap = getContributionMapToExport(contribution);
                            if(first == 0) {
                                first = 1;
                                CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(contributionMap.keySet().toArray(new String[contributionMap.keySet().size()]));
                                csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                            }

                            for(String key: contributionMap.keySet()) {
                                csvFilePrinter.print((contributionMap.get(key)));
                            }
                            csvFilePrinter.println();
                        }
                        fileWriter.flush();
                        fileWriter.close();
                        if(csvFilePrinter != null) {
                            csvFilePrinter.close();
                        }

                        return ok(tempFile);
                    } catch (Exception e) {
                        Logger.error("Error generating csv file");
                        e.printStackTrace();
                        return internalServerError(Json
                                .toJson(new TransferResponseStatus(
                                        ResponseStatus.SERVERERROR,
                                        "Error reading contribution stats: " + e.getMessage())));
                    }
                }

                //if mail will be send we package the files into a zip and send the download link by mail
            } else {
                Logger.info("Packing files to send by email");
                String finalFormat = format;
                String finalExtendedTextFormat = extendedTextFormat;
                F.Promise.promise(() -> {
                    try {
                        List<File> aRet = new ArrayList<>();
                        if (finalFormat.toUpperCase().equals("JSON")) {
                            aRet.add(getExportFileJson(contributions, true, finalFormat));
                        } else if (collectionFileFormat != null &&
                                (collectionFileFormat.toUpperCase().equals("JSON"))) {
                            aRet.add(getExportFileJson(contributions, true, collectionFileFormat));
                        } else if (collectionFileFormat != null &&
                                (collectionFileFormat.toUpperCase().equals("CSV"))) {
                            for(Contribution contribution : contributions) {
                                aRet.add(getExportFile(contribution, includeExtendedText, finalExtendedTextFormat, collectionFileFormat));
                            }
                        } else {
                            aRet.add(getExportFileJson(contributions, true, "JSON"));
                            for(Contribution contribution : contributions) {
                                aRet.add(getExportFile(contribution, includeExtendedText, finalExtendedTextFormat, "CSV"));
                            }
                        }
                        for (Contribution contribution : contributions) {
                            aRet.add(getExportFile(contribution, includeExtendedText, finalExtendedTextFormat, finalFormat));
                            if (includeExtendedText.toUpperCase().equals("TRUE")) {
                                File file = getPadFile(contribution, finalExtendedTextFormat, finalFormat, user);
                                if (file != null) {
                                    aRet.add(file);
                                }
                            }
                        }

                        Logger.info("EXPORT: Preparing ZIP file for exported contributions...");
                        String fileName = "contribution" + new Date().getTime() + ".zip";

                        String appBasePath = Play.application().path().getAbsolutePath();
                        String path = Play.application().configuration().getString("application.contributionFilesPath") + fileName;

                        if (!Play.application().configuration().getBoolean("application.contributionFilesPathIsAbsolute")) {
                            path = appBasePath + path;
                        }

                        File zip = new File(path);
                        Logger.info("EXPORT: Packing export in "+path + "files " + aRet);
                        Packager.packZip(zip, aRet);
                        String url = Play.application().configuration().getString("application.contributionFiles") + fileName;
                        Logger.info("EXPORT: Preparing email to send "+url);
                        MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
                        provider.sendZipContributionFile(url, user.getEmail());
                    } catch (DocumentException e) {
                        Logger.info(e.getMessage());
                        Logger.debug(e.getStackTrace().toString());
                    }
                    return Optional.ofNullable(null);
                });
            }
        }
        return ok("The file will be sent to your email when it is ready");
    }

    /**
     * GET       /api/space/:sid/contribution/:cid
     *
     * @param sid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, produces = "application/json",
            value = "Get contribution by id in a specific Resource Space",
            notes = "Every entity in AppCivist has a Resource Space to associate itself to other entities")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result findResourceSpaceContributionById(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid,
            @ApiParam(name = "format", value = "Export format", allowableValues = "JSON,CSV,TXT,PDF,RTF,DOC")
                    String format,
            @ApiParam(name = "includeExtendedText", value = "Include or not extended text") String includeExtendedText,
            @ApiParam(name = "extendedTextFormat", value = "Include or not extended text", allowableValues =
                    "JSON,CSV,TXT,PDF,RTF,DOC") String extendedTextFormat,
            @ApiParam(name = "flat", value = "Flat version of the campaign") String flat) {
        Logger.debug("Finding Contribution "+cid+" in Resource Space "+sid);
        ResourceSpace rs = ResourceSpace.findByContribution(sid, cid);
        format = format.toUpperCase();
        if (rs == null) {
            return notFound(Json
                    .toJson(new TransferResponseStatus("No contribution found with id " + cid + "in space " + sid)));
        }

        Contribution contribution = Contribution.read(cid);
        if(flat.equals("true")) {
            try {
                DateFormat bdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Map<String, Object> aRet = new HashMap<>();
                aRet.put("title", contribution.getTitle());
                aRet.put("text", contribution.getText());
                aRet.put("creation", bdFormat.format(contribution.getCreation()));
                if (contribution.getLastUpdate() != null) {
                    aRet.put("lastUpdate", bdFormat.format(contribution.getLastUpdate()));
                }
                return ok(Json.toJson(aRet));

            } catch (Exception e) {
                e.printStackTrace();
                return ok();
            }
        }
        contribution.setCustomFieldValues(CustomFieldValue.findAllByTargetUUID(contribution.getUuidAsString()));
        List<Contribution> contributions = new ArrayList<>();
        contributions.add(contribution);
        Boolean sendMail = false;
        if (!(format.equals("JSON") || format.equals("CSV")) || includeExtendedText.toUpperCase().equals("TRUE")) {
            sendMail = true;
            Logger.debug("Contribution export will be produced in format "+format+" and sent by email");
        }
        if (!sendMail) {
            Logger.debug("Contribution in "+format+" will be sent to client");
            if(format.equals("JSON")) {
                return ok(Json.toJson(contribution));
            }
            if(format.equals("CSV")) {
                    response().setContentType("application/csv");
                    response().setHeader("Content-disposition", "attachment; filename=proposal.csv");
                    Logger.debug("Contribution in CSV being produced based on JSON");
                    try {
                        Logger.info("Preparing CSV file");
                        File tempFile = File.createTempFile("contributions.csv", ".tmp");
                        CSVPrinter csvFilePrinter;
                        FileWriter fileWriter = new FileWriter(tempFile);
                        Logger.info("Creating csv row for contribution " + contribution.getContributionId());
                        LinkedHashMap<String, String> contributionMap = getContributionMapToExport(contribution);
                        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(contributionMap.keySet().toArray(new String[contributionMap.keySet().size()]));
                        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                        for(String key: contributionMap.keySet()) {
                            csvFilePrinter.print((contributionMap.get(key)));
                        }
                        csvFilePrinter.println();
                        fileWriter.flush();
                        fileWriter.close();
                        csvFilePrinter.close();
                        return ok(tempFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return internalServerError(Json.toJson(
                                new TransferResponseStatus("There was an internal error: " + e.getMessage())));
                    }
            }
        } else {
            Logger.debug("Contribution in "+format+" will be produced in promise to sent by email (includeExtendedText = "+includeExtendedText);
            String finalFormat = format;
            F.Promise.promise(() -> {
                List<File> aRet = new ArrayList<>();
                switch (finalFormat.toUpperCase()) {
                    case "JSON":
                        aRet.add(getExportFileJson(contributions, false, finalFormat));
                        break;
                    default:
                        try {
                            aRet.add(getExportFile(contribution, includeExtendedText, extendedTextFormat, finalFormat));
                            break;
                        } catch (DocumentException e) {
                            Logger.info("DocumentException when exporting file");
                            e.printStackTrace();
                            return internalServerError(Json.toJson(
                                    new TransferResponseStatus("There was an internal error: " + e.getMessage())));
                        }
                }
                try {
                    User user = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
                    Logger.info("EXPORT: Preparing ZIP file for export");
                    if (includeExtendedText.toUpperCase().equals("TRUE")) {
                        Logger.info("TRying to include extended text file");
                        File file = getPadFile(contribution, extendedTextFormat, finalFormat, user);
                        if (file != null) {
                            aRet.add(file);
                            Logger.info("Extended text included");

                        }
                    }

                    String fileName = "contribution" + new Date().getTime() + ".zip";

                    String appBasePath = Play.application().path().getAbsolutePath();
                    String path = Play.application().configuration().getString("application.contributionFilesPath") + fileName;

                    if (!Play.application().configuration().getBoolean("application.contributionFilesPathIsAbsolute")) {
                        path = appBasePath + path;
                    }

                    File zip = new File(path);
                    Logger.info("EXPORT: Packing exported contribution in zip File: "+path);
                    Packager.packZip(zip, aRet);
                    String url = Play.application().configuration().getString("application.contributionFiles") + fileName;
                    MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
                    provider.sendZipContributionFile(url, user.getEmail());
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                    Logger.info("Error in export Promise: "+e.getMessage());
                }
                return Optional.ofNullable(null);
            });

        }
        return ok("The file will be sent to your email when it is ready");
    }

    /**
     * GET       /api/space/:sid/contribution
     *
     * @param sid
     * @param type
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json",
            value = "Get contributions in a specific Resource Space",
            notes = "Every entity in AppCivist has a Resource Space to associate itself to other entities")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result findResourceSpacePinnedContributions(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type) {

        ContributionTypes mappedType = null;
        if (type != null)
            ContributionTypes.valueOf(type.toUpperCase());
        List<Contribution> contributions = ContributionsDelegate.findPinnedContributionsInSpace(sid, mappedType);
        if (contributions == null) {
            contributions = new ArrayList<Contribution>();
        }
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No pinned contributions for {resource space}: " + sid + ", type=" + type)));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/stats
     *
     * @param aid
     * @param cid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionStatistics.class, produces = "application/json",
            value = "Get contributions statistics")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution stats found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionStats(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid) {
        try {
            ContributionStatistics stats = new ContributionStatistics(coid);
            return ok(Json.toJson(stats));
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution stats: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/group/:gid/contribution/:coid/stats
     *
     * @param aid
     * @param cid
     * @param gid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionStatistics.class, produces = "application/json",
            value = "Get workgroup contributions statistics")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution stats found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readWGContributionStats(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "gud", value = "Group ID") Long gid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid) {
        try {
            ContributionStatistics stats = new ContributionStatistics(gid, coid);
            return ok(Json.toJson(stats));
        } catch (Exception e) {
            e.printStackTrace();
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading workgroup contribution stats: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/feedback
     *
     * @param aid
     * @param cid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get contributions feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionFeedbacks(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid) {
        try {
            List<ContributionFeedback> feedbacks = ContributionFeedback.getFeedbacksByContribution(coid);
            User user;
            NonMemberAuthor nma;

            for (ContributionFeedback contributionFeedback: feedbacks) {
                Map<String, Object> info = new HashMap<>();
                Long userId = contributionFeedback.getUserId();
                if (userId!=null) {
                    user = User.findByUserId(contributionFeedback.getUserId());
                    info.put("id", user.getUserId());
                    info.put("name", user.getName());
                    if (user.getProfilePic()!=null)
                        info.put("profilePic", user.getProfilePic().getUrlAsString());
                    contributionFeedback.setUserId(null);
                } else {
                    nma = contributionFeedback.getNonMemberAuthor();
                    if (nma==null) {
                        info.put("id", -1);
                        info.put("name", "");
                    } else {
                        info.put("id", nma.getId());
                        info.put("name", nma.getName());
                        contributionFeedback.setNonMemberAuthor(null);
                    }
                }

                contributionFeedback.setUser(info);
            }
            if (feedbacks==null || feedbacks.size() == 0) {
                Logger.debug("There are no feedbacks for contribution " + coid + " in assembly " + aid + " and campaign " + cid);
                feedbacks = new ArrayList<>();
            }
            return ok(Json.toJson(feedbacks));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/feedback/:fid
     *
     * @param aid
     * @param cid
     * @param coid
     * @param fid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, produces = "application/json",
            value = "Get individual ContributionFeedback")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedback found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionFeedback(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "fid", value = "Feedback ID") Long fid) {
        try {
            ContributionFeedback feedback = ContributionFeedback.read(fid);
            if (feedback==null) {
                Logger.debug("There are no feedbacks for contribution " + coid + " in assembly " + aid + " and campaign " + cid);
                feedback = new ContributionFeedback();
            }
            return ok(Json.toJson(feedback));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/group/:gid/contribution/:coid/feedback?type=x
     *
     * @param aid
     * @param gid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get Contribution Feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfGroup", meta = SecurityModelConstants.GROUP_RESOURCE_PATH)
    public static Result readContributionFeedbackPrivate(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Group ID") Long gid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "type", value = "Type") String type) {
        try {
            List<ContributionFeedback> feedbacks = ContributionFeedback.getPrivateFeedbacksByContributionTypeAndWGroup(coid, gid, type);
            if (feedbacks==null || feedbacks.size() == 0) {
                Logger.debug("There are no feedbacks for contribution " + coid + " in assembly " + aid + " and group " + gid);
                feedbacks = new ArrayList<>();
            }
            return ok(Json.toJson(feedbacks));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/contribution/:coid/feedback?type=x
     *
     * @param aid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get Contribution Feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    //@Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.AUTHOR_OF_CONTRIBUTION_FEEDBACK)
    @Restrict({@Group(GlobalData.USER_ROLE)})
    public static Result readContributionFeedbackNoGroupId(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "type", value = "Type") String type) {
        try {
            // 1. obtaining the user of the requestor
            User author = User.findByAuthUserIdentity(PlayAuthenticate
                    .getUser(session()));
            Membership m = MembershipAssembly.findByUserAndAssemblyIds(author.getUserId(), aid);
            List<ContributionFeedback> feedbacks = new ArrayList<>();
            if (m!=null) {
                feedbacks = ContributionFeedback.getPrivateFeedbacksByContributionType(coid, null, type);
            } else {
                feedbacks = ContributionFeedback.getPrivateFeedbacksByContributionType(coid, author.getUserId(), type);
            }
            if (feedbacks==null || feedbacks.size() == 0) {
                Logger.debug("There are no feedbacks for contribution " + coid + " in assembly " + aid);
                feedbacks = new ArrayList<>();
            }
            return ok(Json.toJson(feedbacks));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/contribution/:couuid/feedback?type=x
     *
     * @param couuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json",
            value = "Get Contribution Feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution feedbacks found", response = TransferResponseStatus.class)})
    public static Result readContributionFeedbackPublic(
            @ApiParam(name = "couuid", value = "Contribution UUID") String couuid,
            @ApiParam(name = "type", value = "Type") String type) {
        try {
            Contribution co = Contribution.readByUUID(UUID.fromString(couuid));
            List<ContributionFeedback> feedbacks = ContributionFeedback.getPublicFeedbacksByContributionType(co.getContributionId(), type);
            for (ContributionFeedback contributionFeedback: feedbacks) {
                Map<String, Object> info = new HashMap<>();
                User user = User.findByUserId(contributionFeedback.getUserId());
                info.put("id", user.getUserId());
                info.put("name", user.getName());
                if (user.getProfilePic()!=null)
                    info.put("profilePic", user.getProfilePic().getUrlAsString());
                contributionFeedback.setUserId(null);
                contributionFeedback.setUser(info);
            }
            if (feedbacks==null || feedbacks.size() == 0) {
                Logger.debug("There are no feedbacks for contribution " + couuid);
                feedbacks = new ArrayList<>();
            }
            return ok(Json.toJson(feedbacks));
        } catch (Exception e) {
            Logger.error("Error retrieving feedbacks", e);
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error reading contribution feedbacks: " + e.getMessage())));
        }
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid/padid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = PadTransfer.class, produces = "application/json", value = "Get the pad of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findContributionPadId(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution c = Contribution.read(contributionId);
        if (c != null) {
            Resource pad = c.getExtendedTextPad();
            String padId = pad.getPadId();
            PadTransfer p = new PadTransfer();
            p.setPadId(padId);
            if (padId != null) {
                return ok(Json.toJson(p));
            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No Pad for this Contribution")));
            }
        }
        return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID " + contributionId + " not found")));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid/comments
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Read comments on a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findContributionComments(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution c = Contribution.read(contributionId);
        if (c != null) {
            List<Contribution> comments = Contribution.readCommentsOfSpace(c.getResourceSpaceId());
            if (comments != null) {
                return ok(Json.toJson(comments));
            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No comments on this contribution")));
            }
        }
        return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID " + contributionId + " not found")));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid/contributions
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Read associated contributions of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result findAssociatedContributions(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Integer") Integer page,
            @ApiParam(name = "pageSize", value = "Integer") Integer pageSize) {
        Contribution c = Contribution.read(contributionId);
        if (c == null) {
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution with ID " + contributionId + " not found")));
        }
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        List<Contribution> associatedContributions;
        if (all != null) {
            associatedContributions = c.getAssociatedContributions();
        } else {
            associatedContributions = c.getPagedAssociatedContributions(page, pageSize);
        }
        return ok(Json.toJson(associatedContributions));
    }

    /**
     * GET       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, produces = "application/json", value = "Get contribution by its Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    // TODO: add API token support, some API enpoints must be available only for registered clients
    public static Result findContributionByUUID(
            @ApiParam(name = "uuid", value = "Contribution Universal ID") UUID uuid,
            @ApiParam(name = "flat", value = "Flat version of the contribution") String flat) {
        Contribution contribution;
        try {
            contribution = Contribution.readByUUID(uuid);

            if(flat.equals("true")) {
                try {
                    DateFormat bdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Map<String, Object> aRet = new HashMap<>();
                    aRet.put("title", contribution.getTitle());
                    aRet.put("text", contribution.getText());
                    aRet.put("creation", bdFormat.format(contribution.getCreation()));
                    if (contribution.getLastUpdate() != null) {
                        aRet.put("lastUpdate", bdFormat.format(contribution.getLastUpdate()));
                    }
                    return ok(Json.toJson(aRet));

                } catch (Exception e) {
                    Logger.error(e.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    Logger.debug("Exception: "+e.getStackTrace().toString()+" | "+e.getMessage()+" | "+sw.toString());
                    TransferResponseStatus response = new TransferResponseStatus();
                    response.setStatusMessage("Error: "+e.getMessage());
                    response.setErrorTrace(sw.toString());
                    response.setResponseStatus(ResponseStatus.SERVERERROR);
                    return internalServerError(Json.toJson(response));
                }
            }

            //if the contribution is published and has a peerdoc, return the peerdoc url without user
            if(contribution.getStatus().equals(ContributionStatus.PUBLISHED) &&
                    contribution.getExtendedTextPad() != null
                    && contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.PEERDOC)) {
                    PeerDocWrapper peerDocWrapper = new PeerDocWrapper(null);
                    try {
                        contribution.getExtendedTextPad().setUrl(new URL(contribution.getExtendedTextPad()
                                .getUrlAsString() + "?user=" + peerDocWrapper.encrypt()));

                    } catch (Exception e) {
                        contribution.setErrorsInExtendedTextPad("Error reading the pad " + e.getMessage());
                        return ok(Json.toJson(contribution));
                    }
                }


        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with this uuid")));
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        String result;
        try {
            result = mapper.writerWithView(Views.Public.class).writeValueAsString(contribution);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return internalServerError(Json.toJson(new TransferResponseStatus(ResponseStatus.SERVERERROR, "Error while mapping the public view of the contribution")));
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

        //return ok(Json.toJson(contribution));
    }

    /**
     * GET       /api/contribution/:uuid/history
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionHistory.class, responseContainer = "List", produces = "application/json", value = "Get contribution histories by its Universal ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution histories found", response = TransferResponseStatus.class)})
    public static Result findContributionHistoryByUUID(
            @ApiParam(name = "uuid", value = "Contribution Universal ID") UUID uuid) {
        List<ContributionHistory> contributionHistories;
        String result;
        try {
            Contribution contribution = Contribution.readByUUID(uuid);
            contributionHistories = ContributionHistory.getContributionsHistory(contribution.getContributionId());
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            result  = mapper.writerWithView(Views.Public.class)
                    .writeValueAsString(contributionHistories);
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution histories with this uuid")));
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

    /**
     * GET       /api/space/:uuid/contribution
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contribution by its Universal Resource Space ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    // TODO: add API token support, some API enpoints must be available only for registered clients
    public static Result findResourceSpaceContributionsByUUID(
            @ApiParam(name = "uuid", value = "Resource Space Universal ID") UUID uuid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type,
            @ApiParam(name = "by_text", value = "String") String byText,
            @ApiParam(name = "groups", value = "List") List<Integer> byGroup,
            @ApiParam(name = "themes", value = "List") List<Integer> byTheme,
            @ApiParam(name = "all", value = "Boolean") String all,
            @ApiParam(name = "page", value = "Page", defaultValue = "0") Integer page,
            @ApiParam(name = "pageSize", value = "Number of elements per page") Integer pageSize,
            @ApiParam(name = "sorting", value = "Ordering of proposals") String sorting,
            @ApiParam(name = "status", value = "Status of Contributions", defaultValue = "") String status) { // DRAFT, NEW, PUBLISHED, INBALLOT, SELECTED, EXCLUDED,ARCHIVED,MODERATED,
        if (pageSize == null) {
            pageSize = GlobalData.DEFAULT_PAGE_SIZE;
        }
        try {
            ResourceSpace rs = ResourceSpace.readByUUID(uuid);
            List<Contribution> contributions;
            Map<String, Object> conditions = new HashMap<>();
            conditions.put("containingSpaces", rs.getResourceSpaceId());
            if (type != null && !type.isEmpty()) {
                ContributionTypes mappedType = ContributionTypes.valueOf(type.toUpperCase());
                conditions.put("type", mappedType);
            }
            if (byText != null && !byText.isEmpty()) {
                conditions.put("by_text", byText);
            }
            if (byGroup != null && !byGroup.isEmpty()) {
                conditions.put("group", byGroup);
            }
            if (byTheme != null && !byTheme.isEmpty()) {
                conditions.put("theme", byTheme);
            }
            if (sorting != null && !sorting.isEmpty()) {
                conditions.put("sorting", sorting);
            }
            if (status != null && !status.isEmpty()) {
                conditions.put("status",status);
            } else {
                conditions.put("status","PUBLISHED,INBALLOT,SELECTED,PUBLIC_DRAFT");
            }

            PaginatedContribution pag = new PaginatedContribution();
            if (all != null) {
                contributions = ContributionsDelegate.findContributions(conditions, null, null, false);
            } else {
                List<Contribution> contribs = ContributionsDelegate.findContributions(conditions, null, null, false);
                contributions = ContributionsDelegate.findContributions(conditions, page, pageSize, false);
                pag.setPageSize(pageSize);
                pag.setTotal(contribs.size());
                pag.setPage(page);
                pag.setList(contributions);
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
            String result;
            if(all != null){
                result = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(contributions);
            } else {
                result = mapper.writerWithView(Views.Public.class)
                        .writeValueAsString(pag);
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

        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(Json.toJson(Json
                    .toJson(new TransferResponseStatus("Error processing request"))));
        }

    }
    /**
     * GET       /api/space/:uuid/contribution/public/pinned
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "Get contribution by its Universal Resource Space ID")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    // TODO: add API token support, some API enpoints must be available only for registered clients
    public static Result findResourceSpacePinnedContributionsByUUID(
            @ApiParam(name = "uuid", value = "Resource Space Universal ID") UUID uuid,
            @ApiParam(name = "type", value = "Type of contributions", allowableValues = "forum_post, comment, idea, question, issue, proposal, note", defaultValue = "") String type) {
        ResourceSpace rs = ResourceSpace.readByUUID(uuid);
        ContributionTypes mappedType = null;
        if (type != null)
            mappedType = ContributionTypes.valueOf(type.toUpperCase());
        List<Contribution> contributions = ContributionsDelegate
                .findPinnedContributionsInResourceSpace(rs, mappedType, ContributionStatus.PUBLISHED);
        return contributions != null ? ok(Json.toJson(contributions))
                : notFound(Json.toJson(new TransferResponseStatus(
                "No contributions for {resource space}: " + uuid + ", type=" + type)));
    }

    /**
     * GET       /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = ContributionHistory.class, responseContainer = "List", produces = "application/json", value = "Get contributions change history")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    public static Result getContributionsChangeHistory(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) throws Exception {
        List<ContributionHistory> contributionHistories = ContributionHistory.getContributionsHistory(contributionId);
        return ok(Json.toJson(contributionHistories));
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid
     *
     * @param peerDocId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = TransferResponseStatus.class,  produces = "application/json", value = "PUT contribution and title by PeerDocId")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result putContributionByPeerDocId(
            @ApiParam(name = "pid", value = "PeerDoc ID") String peerDocId,
            @ApiParam(name = "title", value = "New Title") String title,
            @ApiParam(name = "description", value = "New Description") String description,
            @ApiParam(name = "lastActivity", value = "Last activity") String lastUpdate) throws Exception {
        try {
            Logger.info("Updating contribution from external service based on document ID: " + peerDocId);
            Contribution contribution = Contribution.getByPeerDocId(peerDocId);
            if(title.isEmpty() && lastUpdate.isEmpty() && description.isEmpty()) {
                Logger.info("All parameters are empty. Nothing to update");
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage("Nothing to update");
                return badRequest(Json.toJson(responseBody));
            }
            if(contribution!=null) {
                Logger.info("Contribution found: " + contribution.getContributionId());
                if (!title.isEmpty()) {
                    Logger.debug("Updating title to: " + title);
                    contribution.setTitle(title);
                }
                if (!description.isEmpty()) {
                    Logger.debug("Updating description to: " + description);
                    contribution.setText(description);
                }

                if(!lastUpdate.isEmpty()) {
                    Logger.debug("Updating lastUpdate to: " + lastUpdate);
                    lastUpdate =  java.net.URLDecoder.decode(lastUpdate, "UTF-8");
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                    DateFormat bdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
                    Date date = format.parse(lastUpdate);
                    Date toDb = bdFormat.parse(bdFormat.format(date));
                    Logger.debug("Formatted lastUpdate: " + toDb.toString());
                    contribution.setLastUpdate(toDb);
                }
                Contribution.update(contribution);
                Logger.info("Contribution updated!");
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setResponseStatus(ResponseStatus.OK);
                responseBody.setStatusMessage("Contribution updated");
                return ok(Json.toJson(responseBody));
            } else {
                Logger.info("No contribution found for document ID");
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setResponseStatus(ResponseStatus.NODATA);
                responseBody.setStatusMessage("No contribution found for the given peerDocId");
                return notFound(Json.toJson(responseBody));
            }
        } catch (ParseException e) {
            String msg = "Error parsing the lastUpdate date, the expected format is YYYY-MM-DD HH:mm:ss";
            Logger.info(msg);
            TransferResponseStatus responseBody = getErrorMessage(e,msg);
            return internalServerError(Json.toJson(responseBody));
        } catch (Exception e) {
            String msg = "There was an error with you request " + e.getMessage();
            Logger.info(msg);
            TransferResponseStatus responseBody = getErrorMessage(e,msg);
            return internalServerError(Json.toJson(responseBody));
        }
    }

    private static TransferResponseStatus getErrorMessage(Exception e, String message) {
        TransferResponseStatus responseBody = new TransferResponseStatus();
        responseBody.setResponseStatus(ResponseStatus.SERVERERROR);
        responseBody.setStatusMessage(message);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        String sStackTrace = sw.toString();
        responseBody.setErrorTrace(sStackTrace);
        return responseBody;
    }

    /**
     * POST       /api/contribution/history
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", produces = "application/json", value = "Generates histories for all contributions")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result createHistories() {
        List<Contribution> contributions = Contribution.findAll();
        for(Contribution c : contributions){
            ContributionHistory.createHistoricFromContribution(c);
        }
        return ok("ok");
    }

    /**
     * POST       /api/contribution/clean/history
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", produces = "application/json", value = "Delete duplicate contributions change history")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result deleteUnchangedContributionHistories() throws Exception {
        List<Contribution> contributions = Contribution.findAll();
        for(Contribution c : contributions){
            List<ContributionHistory> contributionHistories = ContributionHistory.getContributionsHistory(c.getContributionId());
            boolean createdHistory = false;
            for (ContributionHistory contributionHistory :contributionHistories
                    ) {
                if(createdHistory){
                    if (contributionHistory.getChanges().getAssociationChanges().isEmpty() && contributionHistory.getChanges().getExternalChanges().isEmpty()
                            && contributionHistory.getChanges().getInternalChanges().isEmpty()){
                        contributionHistory.softRemove();
                    }
                }
                if (contributionHistory.getChanges().getAssociationChanges().isEmpty() && contributionHistory.getChanges().getExternalChanges().isEmpty()
                        && contributionHistory.getChanges().getInternalChanges().isEmpty()){
                    createdHistory = true;
                }
            }
        }
        return ok("ok");
    }

    /**
     * POST       /api/contribution/clean/feedback
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", produces = "application/json", value = "Archives duplicated feedbacks")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header") })
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result cleanUnarchivedContributionFeedbacks() throws Exception {

        List<ContributionFeedback> feedbacks = ContributionFeedback.findAll();

        for(ContributionFeedback feedback : feedbacks){
            List<ContributionFeedback> relatedFeedbacks = ContributionFeedback.findPreviousContributionFeedback(
                    feedback.getContributionId(), feedback.getUserId(), feedback.getWorkingGroupId(),
                    feedback.getType(), feedback.getStatus(), feedback.getNonMemberAuthor());
            if(relatedFeedbacks != null && relatedFeedbacks.size() > 1){
                relatedFeedbacks.stream().sorted((feedback1, feedback2) -> feedback1.getCreation().
                        compareTo(feedback2.getCreation()));
                //We have to mark every feedback (but the last, wich is the newer) as archived
                relatedFeedbacks.remove(relatedFeedbacks.size() - 1);
                for(ContributionFeedback relatedFeedback : relatedFeedbacks){
                    relatedFeedback.setArchived(true);
                    relatedFeedback.update();
                }
            }
        }
        return ok("ok");
    }


    /* CREATE ENDPOINTS
     * TODO: reduce complexity by removing uncessary create methods
     */

    /**
     * POST       /api/space/:sid/contribution
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create a contribution in a specific Resource Space")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "ASSEMBLY_ID", value = "The real author of the post", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_SOURCE", value = "Indicates the name of the providerId", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_SOURCE_URL", value = "Source to the original post", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_USER_SOURCE_ID", value = "Email or id of the user in the source social network", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_USER_SOURCE_URL", value = "Link to the user", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_USER_NAME", value = "User name in source", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_USER_EMAIL", value = "User email in source", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "IGNORE_ADMIN_USER", value = "Boolean that indicates if AppCivist should or should not consider the ADMIN user as author", dataType = "String", paramType = "header")})
    @SubjectPresent
    public static Result createContributionInResourceSpaceWithId(@ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
    	// 1. obtaining the user of the request
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        User social_ideation_author = null;
        NonMemberAuthor non_member_author = null;


        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();

            // If TYPE is not declared, default is COMMENT
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            ResourceSpace rs = ResourceSpace.read(sid);
            Result unauth = checkRsDraftState(rs, newContribution.getType());
            if (unauth!=null) return unauth;

            // Check headers if the request comes from SocialIdeation. Only Contributions of type IDEA, PROPOSAL, DISCUSSION and COMMENT will be created from SI
            if (newContribution.getType().equals(ContributionTypes.IDEA)
                    || newContribution.getType().equals(ContributionTypes.PROPOSAL)
                    || newContribution.getType().equals(ContributionTypes.DISCUSSION)
                    || newContribution.getType().equals(ContributionTypes.COMMENT)) {
            	Integer result = ContributionsDelegate.checkSocialIdeationHeaders();
            	if (result == -1){
                    Logger.info("Missing Social Ideation Headers");
            		return badRequest("Missing Social Ideation Headers");
            	} else if (result == 1){
                    HashMap<String,String> headerMap = ContributionsDelegate.getSocialIdeationHeaders();
                    newContribution.setSource(headerMap.get("SOCIAL_IDEATION_SOURCE"));
                    newContribution.setSourceUrl(headerMap.get("SOCIAL_IDEATION_SOURCE_URL"));
                    social_ideation_author = User.findByEmail(headerMap.get("SOCIAL_IDEATION_USER_EMAIL"));
                    if (social_ideation_author == null){
                        non_member_author = NonMemberAuthor.findBySourceAndUrl(headerMap.get("SOCIAL_IDEATION_SOURCE"), headerMap.get("SOCIAL_IDEATION_USER_SOURCE_URL"));
                        if (non_member_author == null){
                            non_member_author = createSocialIdeationNonMemberAuthor(headerMap);
                        }
                        newContribution.setNonMemberAuthor(non_member_author);
                    }
                }
            }

            // Check if there is a ContributionTemplate configured in the current component of the campaign.
            ContributionTemplate template = null;
            if (rs != null && newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                // TODO: make the template stored in the campaign rather than the proposal making component
                List<Component> components = rs.getComponents();
                if (components != null && !components.isEmpty()) {
                    for (Component component : components) {
                        String componentKey = component.getKey();
                        if (componentKey != null && componentKey.toLowerCase().equals("proposalmaking")) {
                            List<ContributionTemplate> templates = component.getTemplates();
                            if (templates != null && !templates.isEmpty()) {
                                template = templates.get(0);
                                break;
                            }
                        }
                    }
                }
            }

            newContribution.setContextUserId(author.getUserId());
            if (social_ideation_author != null){
                newContribution.setContextUserId(social_ideation_author.getUserId());
            }
            Contribution c;

            Ebean.beginTransaction();

            // Create contribution and associated resources
            try {
                // create contribution
                if(social_ideation_author != null){
                    c = createContribution(newContribution, social_ideation_author, type, template, rs);
                } else if (non_member_author != null) {
                    c = createContribution(newContribution, null, type, template, rs);
                } else {
                    c = createContribution(newContribution, author, type, template, rs);
                }

                // created associated peerdoc if config force collaborative editor is configured
                if (c.getType().equals(ContributionTypes.PROPOSAL)) {
                    Config conf = rs.getConfigByKey(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_FORCE_COLLABORATIVE_EDITOR);
                    String confValue = conf != null ? conf.getValue().toLowerCase() : null;
                    if (confValue!=null) {
                        if(confValue.equals(ResourceTypes.PEERDOC.name().toLowerCase())) {
                            PeerDocWrapper peerDocWrapper = new PeerDocWrapper(author);
                            Logger.info("Creating PEERDOC for contribution "+c.getContributionId());
                            peerDocWrapper.createPad(c, rs.getResourceSpaceUuid());
                        } else if (confValue.equals(ResourceTypes.PAD.name().toLowerCase()) || confValue.equals("etherpad")) {
                            // TODO: automatically create the etherpad PAD
                        }
                    }
                }
                c.refresh();

                Logger.info("Adding new contribution ("+c.getContributionId()+") to Resource Space ("+rs.getType()+", "+rs.getResourceSpaceId()+")");
                c.getContainingSpaces().add(rs);
                rs.getContributions().add(c);
                rs.update();
                if (rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
                    for(ResourceSpace rspaces: rs.getWorkingGroupResources().getCampaignsResourceSpaces()) {
                        Logger.info("Adding new contribution to WG campaing too");
                        rspaces.getContributions().add(c);
                        c.getContainingSpaces().add(rspaces);
                        rspaces.update();
                    }
                }

                Contribution.addContributionAuthorsToWG(newContribution, rs);
                Ebean.commitTransaction();


            } catch (Exception e) {
                Ebean.endTransaction();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
                Logger.error(e.getMessage());
                Logger.error(sStackTrace);
                pw.close();
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.getMessage())));
            }

            // Signal a notification asynchronously
            Logger.info("Notification will be sent if it is IDEA or PROPOSAL: " + c.getType());
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInResourceSpace(rs, c);
            });
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/group/:gid/contribution
     *
     * @param aid
     * @param gid
     * @param space
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create contributions in the Working Group of an Assembly")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createAssemblyGroupContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "gid", value = "Working Group ID") Long gid,
            @ApiParam(name = "space", value = "Resource Space within Working Group", allowableValues = "resources, forum", defaultValue = "resources") String space) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            WorkingGroup wg = WorkingGroup.read(gid);
            ResourceSpace rs = space != null && space.equals("forum") ? wg
                    .getForum() : wg.getResources();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution c;
            try {
                c = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (c != null) {
                rs.addContribution(c);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssemblyGroup(wg, c);
            });
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/contribution/:cid/comment
     *
     * @param aid
     * @param cid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create comment on contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createContributionComment(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            Contribution c = Contribution.read(cid);
            ResourceSpace rs = c.getResourceSpace();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution cNew;
            try {
                cNew = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (cNew != null) {
                rs.addContribution(cNew);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInContribution(c, cNew);
            });
            return ok(Json.toJson(cNew));
        }
    }

    /**
     * POST      /api/assembly/:aid/forumpost
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create Assembly forum post",
            notes = "An Assembly Forum POST is a contribution of type FORUM_POST in the 'forum' resource space of an Assembly")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createAssemblyForumPost(@ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            Assembly a = Assembly.read(aid);
            ResourceSpace rs = a.getForum();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution cNew;
            try {
                cNew = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (cNew != null) {
                rs.addContribution(cNew);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssembly(a, cNew);
            });
            return ok(Json.toJson(cNew));
        }
    }

    /**
     * POST      /api/assembly/:aid/group/:gid/forumpost
     *
     * @param aid
     * @param gid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Crete forum post in Working Group",
            notes = "A forum post is a contribution of type FORUM_POST in the the 'forum' resource space of the Working Group")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createWorkingGroupForumPost(@ApiParam(name = "aid", value = "Assembly ID") Long aid,
                                                     @ApiParam(name = "gid", value = "Working Group ID") Long gid) {
        // 1. obtaining the user of the requestor
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {
            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            WorkingGroup wg = WorkingGroup.read(gid);
            ResourceSpace rs = wg.getForum();
            ContributionTemplate template = null;
            if (newContribution.getType().equals(ContributionTypes.PROPOSAL)) {
                List<ContributionTemplate> templates = rs.getTemplates();
                if (templates != null && !templates.isEmpty())
                    template = rs.getTemplates().get(0);
            }
            Contribution c;
            try {
                c = createContribution(newContribution, author, type, template, rs);
            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            if (c != null) {
                rs.addContribution(c);
                rs.update();
            }

            // Signal a notification asynchronously
            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssemblyGroup(wg, c);
            });
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST      /api/assembly/:aid/contribution/:cid/attachment
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Resource.class, produces = "application/json", value = "Add an attachment to a contribution",
            notes = "An attachment is a RESOURCE (with an URL) added to the 'resources' resource space of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Resource form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Attachment object", value = "Body of Contribution in JSON", required = true, dataType = "models.Resource", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result addAttachmentContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Resource> newAttachmentForm = ATTACHMENT_FORM
                .bindFromRequest();
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        if (newAttachmentForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
                    newAttachmentForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            Contribution c = Contribution.read(contributionId);
            ResourceSpace contributionRs = c.getResourceSpace();
            Resource newAttachment = newAttachmentForm.get();
            newAttachment.setCreator(author);
            contributionRs.addResource(newAttachment);
            contributionRs.update();
            return ok(Json.toJson(newAttachment));
        }
    }

    /* Update Endpoints */

    /**
     * GET       /api/assembly/:aid/contribution/:cid/feedback
     *
     * @param aid
     * @param coid
     * @return
     */
    // TODO: REVIEW to evaluate if removing
    // TODO: erased from routes
    @ApiOperation(httpMethod = "GET", response = ContributionFeedback.class, produces = "application/json", value = "Read contribution Feedback",
            notes = "Feedback on a contribution is a summary of its ups/downs/favs (TBD if this endpoint will remain)")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "ContributionFeedback form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result readContributionFeedbackByUser(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "cid", value = "Contribution ID") Long coid) {
        User user = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        ContributionFeedback feedback = ContributionFeedback.findByContributionAndUserId(coid, user.getUserId());
        if (feedback != null) {
            return ok(Json.toJson(feedback));
        } else {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("User Feedback Does Not Exist Yet");
            return notFound(Json.toJson(responseBody));
        }
    }

    /**
     * PUT       /api/assembly/:aid/campaign/:caid/contribution/:cid/feedback
     *
     * @param aid
     * @param caid
     * @param cid
     * @return
     */
    // TODO: REVIEW to evaluate if removing
    @ApiOperation(httpMethod = "PUT", response = ContributionStatistics.class, responseContainer = "List", produces = "application/json", value = "Update Feedback on a Contribution",
            notes = "Feedback on a contribution is a summary of its ups/downs/favs (TBD if this endpoint will remain)")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Statistics object", value = "Body of Contribution Statistics in JSON", required = true, dataType = "models.ContributionStatistics", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "ASSEMBLY_ID", value = "The real author of the post", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_SOURCE", value = "Indicates the name of the providerId", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_SOURCE_URL", value = "Source to the original post", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_USER_SOURCE_ID", value = "Email or id of the user in the source social network", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_USER_SOURCE_URL", value = "Link to the user", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "SOCIAL_IDEATION_USER_NAME", value = "User name in source", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "IGNORE_ADMIN_USER", value = "Boolean that indicates if AppCivist should or should not consider the ADMIN user as author", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContributionFeedback(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "caid", value = "Campaign ID") Long caid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        User social_ideation_author = null;
        NonMemberAuthor non_member_author = null;
        final Form<ContributionFeedback> updatedFeedbackForm = CONTRIBUTION_FEEDBACK_FORM.bindFromRequest();
        ContributionStatistics updatedStats = new ContributionStatistics(cid);
        Contribution contribution = Contribution.read(cid);
        if (updatedFeedbackForm.hasErrors()) {
            return contributionFeedbackError(updatedFeedbackForm);
        } else {
            ContributionFeedback feedback = updatedFeedbackForm.get();
            Campaign campaignPath = Campaign.read(caid);
            if (campaignPath==null) {
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No campaign with id: " + caid )));
            }

            Integer result = ContributionsDelegate.checkSocialIdeationHeaders();
            if (result == -1){
                Logger.info("Missing Social Ideation Headers");
                return badRequest("Missing Social Ideation Headers");
            } else if (result == 1){
                HashMap<String,String> headerMap = ContributionsDelegate.getSocialIdeationHeaders();
                social_ideation_author = User.findByProviderAndKey(
                        headerMap.get("SOCIAL_IDEATION_SOURCE"), headerMap.get("SOCIAL_IDEATION_USER_SOURCE_ID"));
                if (social_ideation_author == null){
                    non_member_author = NonMemberAuthor.findBySourceAndUrl(
                            headerMap.get("SOCIAL_IDEATION_SOURCE"),
                            headerMap.get("SOCIAL_IDEATION_USER_SOURCE_URL"));
                    if (non_member_author == null){
                        non_member_author = new NonMemberAuthor();
                        non_member_author.setName(headerMap.get("SOCIAL_IDEATION_USER_NAME"));
                        non_member_author.setSourceUrl(headerMap.get("SOCIAL_IDEATION_USER_SOURCE_URL"));
                        non_member_author.setSource(headerMap.get("SOCIAL_IDEATION_SOURCE"));
                    }
                }
            }

            // Feedback of tpye TECHNICAL ASSESSMENT, check the password for technical assessment
            if (feedback.getType() != null && feedback.getType()
                    .equals(ContributionFeedbackTypes.TECHNICAL_ASSESSMENT)) {
                List<Config> configs = Config
                        .findByCampaignAndKey(
                                campaignPath.getUuid(),
                                GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);
                // TODO: Leaving the following as example for other cases where
                // we have to read all the configs at once
                // Map<String, Config> configMap =
                // Config.convertConfigsToMap(configs);
                // Config c =
                // configMap.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);

                boolean authorized = false || configs == null || configs.isEmpty();
                for (Config config : configs) {
                    if (feedback.getPassword() != null && feedback // there is a
                                                                    // password
                                                                    // so verify
                            .getPassword().equals(config.getValue())) {
                        authorized = true;
                    }
                }

                if (!authorized) {
                    return unauthorized(Json.toJson(new TransferResponseStatus(
                            ResponseStatus.UNAUTHORIZED,
                            "Password in feedback form is incorrect")));
                }
            }

            feedback.setContribution(contribution);
            if (social_ideation_author != null) {
                feedback.setUserId(social_ideation_author.getUserId());
            } else if (non_member_author != null) {
                feedback.setNonMemberAuthor(non_member_author);
            } else {
                feedback.setUserId(author.getUserId());
            }
            List<ContributionFeedback> existingFeedbacks = ContributionFeedback.findPreviousContributionFeedback(feedback.getContributionId(),
                    feedback.getUserId(), feedback.getWorkingGroupId(), feedback.getType(), feedback.getStatus(), feedback.getNonMemberAuthor());

            Ebean.beginTransaction();
            try {
                //If we found a previous feedback, we set that feedback as archived
                if (existingFeedbacks != null) {
                    for (ContributionFeedback existingFeedback : existingFeedbacks) {
                        existingFeedback.setArchived(true);
                        existingFeedback.update();
                    }
                }

                // We have to do some authorization control
                if (feedback.getWorkingGroupId() != null) {
                    //The user has to be member of working group
                    Membership m = MembershipGroup.findByUserAndGroupId(feedback.getUserId(), feedback.getWorkingGroupId());
                    List<SecurityRole> membershipRoles = m!=null ? m.filterByRoleName(MyRoles.MEMBER.getName()) : null;

                    if (feedback.getType().equals(ContributionFeedbackTypes.WORKING_GROUP) && feedback.getOfficialGroupFeedback() != null && feedback.getOfficialGroupFeedback()) {
                        //The user has to be coordinator of working group
                        membershipRoles =  m!=null ? m.filterByRoleName(MyRoles.COORDINATOR.getName()) : null;
                        if (membershipRoles == null || membershipRoles.isEmpty()) {
                            Logger.error("User has to be coordinator of working group");
                            return unauthorized(Json
                                    .toJson(new TransferResponseStatus(
                                            ResponseStatus.UNAUTHORIZED,
                                            "User has to be coordinator of working group")));
                        }
                    }

                }

                // Make sure ContributionFeedback Type and Status are correct
                // And remove the id if there is one
                feedback.setId(null);
                ContributionFeedback.create(feedback);
                contribution.setPopularity(new Long(updatedStats.getUps() - updatedStats.getDowns()).intValue());
                contribution.update();
                ContributionHistory.createHistoricFromContribution(contribution);
                Ebean.commitTransaction();

            } catch (Exception e) {
                Promise.promise(() -> {
                    Logger.error(LogActions.exceptionStackTraceToString(e));
                    return true;
                });
                return contributionFeedbackError(feedback, e.getLocalizedMessage());
            } finally {
                Ebean.endTransaction();
            }

            return ok(Json.toJson(updatedStats));
        }
    }



    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Fork a Contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result forkContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid) {

        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));
        Contribution contribution = Contribution.read(coid);
        if(contribution == null) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("No contribution found");
            return notFound(Json.toJson(responseBody));
        }
        try {
            Contribution forked = Contribution.fork(contribution, author);
            if(forked == null) {
                TransferResponseStatus response = new TransferResponseStatus();
                response.setResponseStatus(ResponseStatus.SERVERERROR);
                response.setStatusMessage("No ok response from peerdoc");
                return internalServerError(Json.toJson(response));
            }

            return ok(Json.toJson(forked));

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("Error while updating contribution => ",
                    LogActions.exceptionStackTraceToString(e));
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(e.getMessage());
            return Controller
                    .internalServerError(Json.toJson(responseBody));
        }

    }




        /**
         * PUT       /api/campaign/:cuuid/contribution/:uuid/feedback
         *
         * @param cuuid
         * @param uuid
         * @return
         */
    // TODO: REVIEW to evaluate if removing
    @ApiOperation(httpMethod = "PUT", response = ContributionFeedback.class, responseContainer = "List", produces = "application/json", value = "Update Feedback on a Contribution",
            notes = "Feedback on a contribution is a summary of its ups/downs/favs (TBD if this endpoint will remain)")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution feedback form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Feedback object", value = "Body of Contribution Feedback in JSON", required = true, dataType = "models.ContributionFeedback", paramType = "body")})
    public static Result updateContributionFeedbackNonMemberAuthor(
            @ApiParam(name = "cuuid", value = "Campaign UUID") UUID cuuid,
            @ApiParam(name = "uuid", value = "Contribution UUID") UUID uuid) {
        final Form<ContributionFeedback> updatedFeedbackForm = CONTRIBUTION_FEEDBACK_FORM.bindFromRequest();
        Contribution contribution = Contribution.readByUUID(uuid);
        if (contribution==null){
            return notFound(Json.toJson(new TransferResponseStatus(
                    "No contributions with uuid: " + uuid )));
        }
        ContributionStatistics updatedStats = new ContributionStatistics(contribution.getContributionId());
        if (updatedFeedbackForm.hasErrors()) {
            return contributionFeedbackError(updatedFeedbackForm);
        } else {
            ContributionFeedback feedback = updatedFeedbackForm.get();
            Campaign campaignPath = Campaign.readByUUID(cuuid);
            if (campaignPath==null){
                return notFound(Json.toJson(new TransferResponseStatus(
                        "No campaign with uuid: " + cuuid )));
            }
            // Feedback of tpye TECHNICAL ASSESSMENT, check the password for technical assessment
            if (feedback.getType().equals(
                    ContributionFeedbackTypes.TECHNICAL_ASSESSMENT)) {
                List<Config> configs = Config
                        .findByCampaignAndKey(
                                campaignPath.getUuid(),
                                GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);
                // TODO: Leaving the following as example for other cases where we have to read all the configs at once
                // Map<String, Config> configMap = Config.convertConfigsToMap(configs);
                // Config c = configMap.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_EXTENDED_FEEDBACK_PASSWORD);

                boolean authorized = false || configs == null
                        || configs.isEmpty();
                for (Config config : configs) {
                    if (feedback.getPassword() != null && feedback // there is a password so verify
                            .getPassword().equals(config.getValue())) {
                        authorized = true;
                    }
                }

                if (!authorized) {
                    return unauthorized(Json.toJson(new TransferResponseStatus(
                            ResponseStatus.UNAUTHORIZED,
                            "Password in feedback form is incorrect")));
                }
            }
            feedback.setContribution(contribution);
            List<ContributionFeedback> existingFeedbacks = ContributionFeedback.findPreviousContributionFeedback(feedback.getContributionId(),
                    feedback.getUserId(), feedback.getWorkingGroupId(), feedback.getType(), feedback.getStatus(), feedback.getNonMemberAuthor());

            Ebean.beginTransaction();
            try {
                feedback.setContribution(contribution);

                //If we found a previous feedback, we set that feedback as archived
                if (existingFeedbacks != null) {
                    for (ContributionFeedback existingFeedback : existingFeedbacks) {
                        existingFeedback.setArchived(true);
                        existingFeedback.update();
                    }


                }

                // We have to do some authorization control
                if (feedback.getWorkingGroupId() != null) {
                    //The user has to be member of working group
                    Membership m = MembershipGroup.findByUserAndGroupId(feedback.getUserId(), feedback.getWorkingGroupId());
                    List<SecurityRole> membershipRoles = m!=null ? m.filterByRoleName(MyRoles.MEMBER.getName()) : null;

                    if (feedback.getType().equals(ContributionFeedbackTypes.WORKING_GROUP) && feedback.getOfficialGroupFeedback() != null && feedback.getOfficialGroupFeedback()) {
                        //The user has to be coordinator of working group
                        membershipRoles =  m!=null ? m.filterByRoleName(MyRoles.COORDINATOR.getName()) : null;
                        if (membershipRoles == null || membershipRoles.isEmpty()) {
                            Logger.error("User has to be coordinator of working group");
                            return unauthorized(Json
                                    .toJson(new TransferResponseStatus(
                                            ResponseStatus.UNAUTHORIZED,
                                            "User has to be coordinator of working group")));
                        }
                    }

                }

                // Make sure ContributionFeedback Type and Status are correct
                ContributionFeedback.create(feedback);

                //NEW_CONTRIBUTION_FEEDBACK NOTIFICATION
               /* NotificationEventName eventName = existingFeedbacks != null ? NotificationEventName.NEW_CONTRIBUTION_FEEDBACK : NotificationEventName.UPDATED_CONTRIBUTION_FEEDBACK;
                Promise.promise(() -> {
                    Contribution c = Contribution.read(feedback.getContributionId());
                    for (Long campId : c.getCampaignIds()) {
                        Campaign campaign = Campaign.read(campId);
                        NotificationsDelegate.signalNotification(ResourceSpaceTypes.CAMPAIGN, eventName, campaign, feedback);
                    }
                    return true;
                });*/

               /* feedback.getWorkingGroupId();
                Promise.promise(() -> {
                    return NotificationsDelegate.signalNotification(
                            ResourceSpaceTypes.WORKING_GROUP,
                            eventName,
                            WorkingGroup.read(feedback.getWorkingGroupId()).getResources(),
                            feedback);
                });*/

                contribution.setPopularity(new Long(updatedStats.getUps() - updatedStats.getDowns()).intValue());
                contribution.update();
                ContributionHistory.createHistoricFromContribution(contribution);
                Ebean.commitTransaction();
            } catch (Exception e) {
                Promise.promise(() -> {
                    Logger.error(LogActions.exceptionStackTraceToString(e));
                    return true;
                });
                return contributionFeedbackError(feedback, e.getLocalizedMessage());
            }
            finally {
                Ebean.endTransaction();
            }

            return ok(Json.toJson(updatedStats));
        }
    }

    /**
     * PUT       /api/assembly/:aid/contributions/popularity
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Response status")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContributionsPopularity(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid) {
        List<Contribution> contributions = Contribution.findAll();
        for (Contribution c : contributions) {
            ContributionStatistics updatedStats = new ContributionStatistics(c.getContributionId());
            c.setPopularity(new Long(updatedStats.getUps() - updatedStats.getDowns()).intValue());
            c.update();
        }
        return ok(Json.toJson("Ok"));

    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, produces = "application/json",
            value = "Update contribution in Assembly", notes = "The lastUpdate date must be in YYYY-MM-DD HH:mm:ss format")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result updateContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();
        User author = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
                    newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
			Ebean.beginTransaction();
			try {
	            Contribution newContribution = newContributionForm.get();

	            if(newContribution.getStatus().equals(ContributionStatus.PUBLISHED) ||
                        newContribution.getStatus().equals(ContributionStatus.FORKED_PUBLISHED) ||
                        newContribution.getStatus().equals(ContributionStatus.MERGED)) {
                    final boolean[] allowed = {false};
                    checkIfCoordinator(newContribution, allowed, author);
                    if(!allowed[0]) {
                        TransferResponseStatus responseBody = new TransferResponseStatus();
                        responseBody.setStatusMessage(Messages.get(
                                "contribution.unauthorized.creation",
                                ResourceSpaceTypes.ASSEMBLY));
                        return unauthorized(Json.toJson(responseBody));
                    }
                }

                // Public Draft: all can see and comment; only authors can edit
                // Forked Public Draft: all can see and comment; only authors can edit
                // Forked Private Draft: only authors can see, edit, comment
                if((newContribution.getStatus().equals(ContributionStatus.PUBLIC_DRAFT)
                       || newContribution.getStatus().equals(ContributionStatus.FORKED_PUBLIC_DRAFT)
                        || newContribution.getStatus().equals(ContributionStatus.FORKED_PRIVATE_DRAFT)) &&
                        !newContribution.getAuthors().contains(author)) {
                    TransferResponseStatus responseBody = new TransferResponseStatus();
                    responseBody.setStatusMessage(Messages.get(
                            "contribution.unauthorized.creation",
                            ResourceSpaceTypes.ASSEMBLY));
                    return unauthorized(Json.toJson(responseBody));
                }

	            newContribution.setContributionId(contributionId);
	            newContribution.setContextUserId(author.getUserId());

//	            Contribution existingContribution = Contribution.read(contributionId);
//	            for (Field field : existingContribution.getClass().getDeclaredFields()) {
//                    field.setAccessible(true);
//                    if (field.getName().toLowerCase().contains("ebean") || field.isAnnotationPresent(ManyToMany.class)
//                            || field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToMany.class)
//                            || field.isAnnotationPresent(OneToOne.class)) {
//                        continue;
//                    }
//                    field.set(existingContribution, field.get(newContribution));
//	            }
//				existingContribution.setContextUserId(author.getUserId());
//
//				Contribution.update(existingContribution);
	            Contribution updatedContribution = Contribution.readAndUpdate(newContribution, contributionId, author.getUserId());
				ResourceSpace rs = Assembly.read(aid).getResources();
				Promise.promise(() -> {
					return NotificationsDelegate
							.updatedContributionInResourceSpace(rs,
									updatedContribution);
				});

				Ebean.commitTransaction();
	            return ok(Json.toJson(updatedContribution));
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error("Error while updating contribution => ",
						LogActions.exceptionStackTraceToString(e));
				TransferResponseStatus responseBody = new TransferResponseStatus();
				responseBody.setStatusMessage(e.getMessage());
				return Controller
						.internalServerError(Json.toJson(responseBody));
			} finally {
                Ebean.endTransaction();
            }
        }
    }

    /**
     * POST      /api/space/:sid/space/:new_sid
     *
     * @param sid
     * @param new_sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = String.class, produces = "application/json", value = "Assign a resouce space to other resource space")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result assignResourceSpaceToResourceSpace(
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid,
            @ApiParam(name = "new_sid", value = "New Resource Space ID") Long new_sid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        ResourceSpace rs = ResourceSpace.read(sid);
        ResourceSpace rsNew = ResourceSpace.read(new_sid);

        ResourceSpace rCombined =  ResourceSpace.setResourceSpaceItems(rs,rsNew);
        try {
            rCombined.update();
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when assigning Resource Space to Resource Space: " + e.toString())));
        }
        return ok();
    }

    /**
     * POST      /api/assembly/:aid/contribution/:cid/space/:sid
     *
     * @param aid
     * @param cid
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Assign a contribution to a resource space")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result assignContributionResourceSpace(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid,
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        Contribution contribution = Contribution.read(cid);
        if (contribution == null) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("No contribution found");
            return notFound(Json.toJson(responseBody));
        }
        ResourceSpace rsNew = ResourceSpace.read(contribution.getResourceSpaceId());
        ResourceSpace rs = ResourceSpace.read(sid);
        ResourceSpace rCombined = ResourceSpace.setResourceSpaceItems(rs,rsNew);
        List<Contribution> forks = Contribution.findChildrenOrParents(contribution.getUuid(), "FORKS");
        List<Contribution> merges = Contribution.findChildrenOrParents(contribution.getUuid(), "MERGES");
        List<Contribution>  contributions = new ArrayList<>();
        if(forks != null) {
            contributions.addAll(forks);
        }
        if(merges!= null) {
            contributions.addAll(merges);
        }
        for(Contribution contribution1: contributions) {
            rsNew = ResourceSpace.read(contribution1.getResourceSpaceId());
            rCombined = ResourceSpace.setResourceSpaceItems(rs,rsNew);
        }

        try {
            rCombined.update();
            Contribution.addContributionAuthorsToWG(contribution, rsNew);
            ContributionHistory.createHistoricFromContribution(contribution);
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when assigning Contribution to Resource Space: " + e.toString())));
        }

        // Signal a notification asynchronously
        Promise.promise(() -> {
            return NotificationsDelegate.newContributionInResourceSpace(rs,contribution);
        });
        return ok(Json.toJson(contribution));
    }

    /**
     * DELETE      /api/assembly/:aid/contribution/:cid/space/:sid
     *
     * @param aid
     * @param cid
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete a contribution to a resource space")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result deleteContributionResourceSpace(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid,
            @ApiParam(name = "sid", value = "Resource Space ID") Long sid) {
        User author = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        Contribution contribution = Contribution.read(cid);

        if (contribution == null) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage("No contribution found");
            return notFound(Json.toJson(responseBody));
        }
        ResourceSpace rsNew = ResourceSpace.read(contribution.getResourceSpaceId());
        ResourceSpace rs = ResourceSpace.read(sid);
        try {


            rs.getContributions().remove(contribution);
            rs.update();
            ContributionHistory.createHistoricFromContribution(contribution);



        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when removing Contribution from Resource Space: " + e.toString())));
        }
        return ok();
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid/moderate
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Contribution moderation. Soft deletes contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result moderateContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution id") Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();
        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,
                    newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {
            Contribution contributionFromDatabase = Contribution.read(contributionId);
            Contribution moderated = newContributionForm.get();
            contributionFromDatabase.setModerationComment(moderated.getModerationComment());

            // ContributionsDelegate.updateCommentCounters(contributionFromDatabase, "-");

            Contribution.update(contributionFromDatabase);
            Contribution.softDelete(contributionFromDatabase);

            ResourceSpace rs = Assembly.read(aid).getResources();


            Promise.promise(() -> {
                ContributionsDelegate.updateCommentCounters(contributionFromDatabase, "-");
                return ok();
            });
            return ok();
        }
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid/softremoval
     * TODO: create a dynamic handler to check if the contribution belongs to the user
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Logical removal of contribution in Assembly")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    //@Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result softDeleteContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution c = Contribution.read(contributionId);

        // ContributionsDelegate.updateCommentCounters(c, "-");

        Contribution.softDelete(contributionId);
        ResourceSpace rs = Assembly.read(aid).getResources();
        Promise.promise(() -> {
            ContributionsDelegate.updateCommentCounters(c, "-");
            return ok();
            //return NotificationsDelegate.updatedContributionInResourceSpace(rs, c);
        });
        return ok();
    }

    /**
     * PUT       /api/assembly/:aid/contribution/:cid/recover
     * TODO: create a dynamic handler to check if the contribution belongs to the user
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = String.class, produces = "application/json", value = "Logical recovery of contribution Assembly")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "ModeratorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result recoverContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {
        Contribution.softRecovery(contributionId);
        Contribution c = Contribution.read(contributionId);
        ResourceSpace rs = Assembly.read(aid).getResources();
        /*Promise.promise(() -> {
            return NotificationsDelegate.updatedContributionInResourceSpace(rs, c);
        });*/
        return ok();
    }

    /**
     * DELETE    /api/assembly/:aid/contribution/:cid
     *
     * @param aid
     * @param contributionId
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = String.class, produces = "application/json", value = "Delete a contribution (will remove it from the database)",
            notes = "Only for ADMINS")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result forceDeleteContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long contributionId) {

        // NEW PROMISE
        Contribution contribution = Contribution.read(contributionId);
        Promise.promise(() -> {
             ContributionsDelegate.updateCommentCounters(contribution, "-");
             return ok();
        });
        Contribution.delete(contributionId);
        return ok();
    }


    /**
     * POST  /api/contribution/:uuid/themes
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Theme.class, responseContainer = "List", produces = "application/json", value = "Add themes to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Theme form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Theme objects", value = "Themes to add to the contribution", dataType = "models.transfer.ThemeListTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result addThemeToContribution(
            @ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid,
            @ApiParam(name = "replace", value = "Replace current list of themes", defaultValue = "true") Boolean replace) {
        Contribution contribution;
        contribution = Contribution.readByUUID(uuid);

        try {
            List<Theme> themes = THEMES_FORM.bindFromRequest().get().getThemes();
            ResourceSpace contributionRS = addTheme(contribution, themes, replace);
            return ok(Json.toJson(contributionRS.getThemes()));
        } catch (Exception e) {
            Logger.info("Exception occurred while trying to add themes: "+e.getLocalizedMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            String sStackTrace = sw.toString();
            Logger.debug("Error trace: "+sStackTrace);
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    public static ResourceSpace addTheme(Contribution contribution, List<Theme> themes, boolean replace) {
        List<Theme> toCreate = new ArrayList<>();
        List<Theme> toAdd = new ArrayList<>();
        List<Theme> toAddToCampaign = new ArrayList<>();
        ResourceSpace contributionRS = contribution.getResourceSpace();
        Logger.info("Updating contribution last update date...");
        contribution.setLastUpdate(new Date());
        contribution.update();
        contribution.refresh();
        // Step 1: create new EMERGENT themes
        // - Create theme of type `EMERGENT` only if another theme with the same title and type does not exist yet,
        // - Otherwise reuse the theme. Do not allow new `OFFICIAL_PRE_DEFINED` themes.
        List<Theme> newThemes = themes.stream().filter(t -> t.getThemeId() == null).collect(Collectors.toList());
        for (Theme theme : newThemes) {
            // If the theme EMERGENT, reuse, do not duplicate
            if(theme.getType().equals(ThemeTypes.EMERGENT)) {
                List<Theme> existing = Theme.findByTitleAndType(theme.getTitle(), theme.getType());
                if (existing !=null && !existing.isEmpty()) {
                    // reuse existing
                    Theme reusedTheme = existing.get(0);
                    Logger.info("Theme "+ theme.getTitle()+" already exist and is EMERGENT, reusing existing with ID "+reusedTheme.getThemeId()+" created on "+reusedTheme.getCreation());
                    toAdd.add(reusedTheme);
                    toAddToCampaign.add(reusedTheme);
                } else {
                    toCreate.add(theme);
                    Logger.info("Theme "+ theme.getTitle()+" is a new EMERGENT theme/keyword, creating!");
                }
            } else {
                Logger.info("Theme "+ theme.getTitle()+" is a new theme, but its type IS NOT EMERGENT, discarding!");
            }
        }
        Logger.info("Creating new EMERGENT themes...");
        toCreate.forEach(Model::save);
        Logger.info("Adding new EMERGENT themes to parent campaigns...");
        // add newThemes to compaings
        contribution.getCampaignIds().forEach(id -> {
            Campaign campaign = Campaign.find.byId(id);
            ResourceSpace campaignRS = campaign.getResources();
            List<Theme> campaignThemes = campaignRS.getThemes();
            campaignRS.getThemes().addAll(toCreate);
            List<Theme> addToCampaignIfNotAddedYet =
                    toAddToCampaign.stream().filter(
                            t -> campaignThemes.stream()
                                    .noneMatch(o -> o.getThemeId().equals(t.getThemeId()))).collect(Collectors.toList());
            campaignRS.getThemes().addAll(addToCampaignIfNotAddedYet);
            campaignRS.update();
        });
        List<Theme> existing = themes.stream().filter(t -> t.getThemeId()!=null).collect(Collectors.toList());
        List<Theme> addToExistingIfNotAddedYet =
                toAddToCampaign.stream().filter(
                        t -> existing.stream()
                                .noneMatch(o -> o.getThemeId().equals(t.getThemeId()))).collect(Collectors.toList());
        existing.addAll(addToExistingIfNotAddedYet); // add to existing the themes that were added as new but actually existed in another campaign
        List<Theme> contributionThemes = contributionRS.getThemes();

        if (replace) {
            Logger.info("Adding existing EMERGENT and OFFICIAL_PRE_DEFINED themes to the unified list of themes...");
            // the list under toCreate should already be included because on creation, they got their themeIds
            contribution.setThemes(existing);
            contribution.update();
        } else {
            contributionThemes.addAll(toCreate);
            // Step 2: if there are existing thems in the list, make sure they are added only if they were not added before
            List<Theme> newExistingThemes =
                    existing.stream().filter(
                            t -> contributionThemes.stream()
                                    .noneMatch(o -> o.getThemeId().equals(t.getThemeId()))).collect(Collectors.toList());
            Logger.info("Adding new existing EMERGENT and OFFICIAL_PRE_DEFINED themes to contribution...");
            toAdd.addAll(newExistingThemes);
            Logger.info("Expanding original list of themes...");
            contributionThemes.addAll(toAdd);
            contributionRS.update();
        }
        return contributionRS;
    }

    /**
     * POST  /api/contribution/:uuid/authors
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Add a author to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authors objects", value = "Authors to add to the contribution", dataType = "models.User", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result addAuthorToContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);

        try {
            User user = AUTHORS_FORM.bindFromRequest().get();
            User author = User.read(user.getUserId());
            boolean authorExist = contribution.getAuthors().contains(author);
            if(!authorExist) {
                contribution.getAuthors().add(author);
                contribution.setLastUpdate(new Date());
                contribution.update();
                contribution.refresh();
                F.Promise.promise(() -> {
                    sendAuthorAddedMail(author, null, contribution, contribution.getContainingSpaces().get(0));
                    PeerDocWrapper peerDocWrapper = new PeerDocWrapper(authorActive);
                    peerDocWrapper.updatePeerdocPermissions(contribution);
                    return Optional.ofNullable(null);
                });
                return ok(Json.toJson(contribution));
            }else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Author already in contribution")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * POST  /api/contribution/:uuid/nonmemberauthors
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Add a non member author to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authors objects", value = "Authors to add to the contribution", dataType = "models.NonMemberAuthor", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result addNonMemberAuthorToContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);
        try {
            NonMemberAuthor author = NON_MEMBER_AUTHORS_FORM.bindFromRequest().get();
            // Check if there is already a user for the email in the form
            User user = User.findByEmail(author.getEmail());
            boolean authorExist = false;
            boolean userExist = false;
            if (user !=null) {
                authorExist = contribution.getAuthors().contains(user);
                userExist = true;
            } else {
                authorExist = contribution.getNonMemberAuthors().contains(author);
            }
            contribution.setLastUpdate(new Date());
            if (!authorExist && !userExist) {
                // create the non member author and add it to the contribution
                author = NonMemberAuthor.create(author);
                contribution.getNonMemberAuthors().add(author);
                contribution.update();
                contribution.refresh();
                List<NonMemberAuthor> authors = new ArrayList<>();
                authors.add(author);
                F.Promise.promise(() -> {
                    sendAuthorAddedMail(null, authors, contribution, contribution.getContainingSpaces().get(0));
                    PeerDocWrapper peerDocWrapper = new PeerDocWrapper(authorActive );
                    peerDocWrapper.updatePeerdocPermissions(contribution);
                    return Optional.ofNullable(null);
                });
                return ok(Json.toJson(author));
            } else if (authorExist && !userExist) {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Non Member Author already in contribution")));
            } else if (!authorExist && userExist) {
                // add it as author
                contribution.getAuthors().add(user);
                contribution.update();
                contribution.refresh();
                F.Promise.promise(() -> {
                    sendAuthorAddedMail(user, null, contribution, contribution.getContainingSpaces().get(0));
                    PeerDocWrapper peerDocWrapper = new PeerDocWrapper(authorActive );
                    peerDocWrapper.updatePeerdocPermissions(contribution);
                    return Optional.ofNullable(null);
                });

                return ok(Json.toJson(user));
            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Author already in contribution")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * DELETE  /api/contribution/:uuid/themes/:tid
     *
     * @param uuid
     * @param tid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, produces = "application/json", value = "Add a theme to a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Theme objects", value = "Themes to add to the contribution", dataType = "models.transfer.ThemeListTransfer", paramType = "body"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result deleteThemeFromContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid,
                                                     @ApiParam(name = "tid", value = "Theme's Id") Long tid) {
        Contribution contribution;

        contribution = Contribution.readByUUID(uuid);

        try {
            Theme theme = Theme.read(tid);
            contribution.setLastUpdate(new Date());
            contribution.getThemes().remove(theme);
            contribution.update();
            return ok(Json.toJson(contribution));
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * DELETE  /api/contribution/:uuid/authors/:auuid
     *
     * @param uuid
     * @param auuid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, produces = "application/json", value = "Delete an author from a contribution")
    @ApiResponses(value = {@ApiResponse(code = BAD_REQUEST, message = "Contribution form has errors", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "OnlyMeAndCoordinatorOfAssembly", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result deleteAuthorFromContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid,
                                                      @ApiParam(name = "auuid", value = "Author's Universal Id (UUID)") UUID auuid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);

        try {
            User author = User.findByUUID(auuid);
            contribution.setLastUpdate(new Date());
            boolean authorExist = contribution.getAuthors().contains(author);
            if(authorExist) {
                contribution.getAuthors().remove(author);
                contribution.update();
                contribution.refresh();
                F.Promise.promise(() -> {
                    PeerDocWrapper peerDocWrapper = new PeerDocWrapper(authorActive );
                    peerDocWrapper.updatePeerdocPermissions(contribution);
                    return Optional.ofNullable(null);
                });
                return ok(Json.toJson(contribution));
            }else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Uuid given is not a contribution author")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }

    /**
     * DELETE  /api/contribution/:uuid/nonmemberauthors/:auuid
     *
     * @param uuid
     * @param nmaid
     * @return
     */
    @ApiOperation(httpMethod = "DELETE", response = Contribution.class, produces = "application/json", value = "Delete a non member author from a contribution")
    @ApiResponses(value = {@ApiResponse(code = NOT_FOUND, message = "Non Member Author or contribution was not found ", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result deleteNonMemberAuthorFromContribution(@ApiParam(name = "uuid", value = "Contribution's Universal Id (UUID)") UUID uuid,
                                                      @ApiParam(name = "auuid", value = "Non Member Author's Id (Long)") Long nmaid) {
        Contribution contribution;
        User authorActive = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        contribution = Contribution.readByUUID(uuid);

        try {
            contribution.setLastUpdate(new Date());
            NonMemberAuthor author = NonMemberAuthor.read(nmaid);
            boolean authorExist = contribution.getNonMemberAuthors().contains(author);
            if(authorExist) {
                contribution.getNonMemberAuthors().remove(author);
                contribution.update();
                contribution.refresh();
                F.Promise.promise(() -> {
                    PeerDocWrapper peerDocWrapper = new PeerDocWrapper(authorActive );
                    peerDocWrapper.updatePeerdocPermissions(contribution);
                    return Optional.ofNullable(null);
                });
                return ok(Json.toJson(new TransferResponseStatus(ResponseStatus.OK, "Non Member Author was Removed")));
            }else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Uuid given is not a contribution author")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No contribution with the given uuid")));
        }
    }
    /**
     * GET  /api/contributions
     *
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json", value = "List contributions")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class),
            @ApiResponse(code = 200, message = "List of contributions", response = ApiResponseTransfer.class)})
    public static Result all(@ApiParam(name = "page", defaultValue = "1", value = "Page to retrieve") String pageStr,
                             @ApiParam(name = "by_uuid", value = "Filter contributions by UUID") String byUuid,
                             @ApiParam(name = "by_theme_id", value = "Filter contributions by theme") String byThemeIdStr) {
        try {
            Integer page = Integer.parseInt(pageStr);
            Long byThemeId = null;

            if (!byThemeIdStr.equals("")) {
                byThemeId = Long.parseLong(byThemeIdStr);
            }
            List<Contribution> contributions = ContributionsDelegate.findBy(page, byUuid, byThemeId);
            ApiResponseTransfer<Contribution> rsp = new ApiResponseTransfer<Contribution>();
            rsp.setResults(contributions);
            rsp.setPageSize(ContributionsDelegate.PAGE_SIZE);
            rsp.setTotalCount(ContributionsDelegate.countBy(byUuid, byThemeId));
            return ok(Json.toJson(rsp));
        } catch (Exception e) {
            e.printStackTrace();
            return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Problems fetching list of contributions")));
        }
    }

    /*
     * Non-exposed methods: creation methods
     */


    // ToDo: check if is not possible to get assembly, campaign and other ids directly from the referrer
    private static String getContributionMailUrl(Contribution contribution, Assembly a, Campaign c, WorkingGroup wg) {

        String contributionUUID = contribution.getUuidAsString();
        String auuid = null;
        String cuuid = null;
        String guuid = null;
        String conuuid = contributionUUID;
        String url = Play.application().configuration().getString("application.uiUrl")
                + "/p/assembly/{auuid}/campaign/{cuuid}/contribution/{conuuid}";

        if (a!=null) { // contribution was inserted in an assembly, return only the
            auuid = a.getUuidAsString();
            url = Play.application().configuration().getString("application.uiUrl")
                    + "/p/assembly/{auuid}/contribution/{conuuid}";
            url = url.replace("{auuid}", auuid).replace("{conuuid}", conuuid);
        } else if (c!=null) { // contribution is in the namespace of the campaign
            Long aid = c.getAssemblies().get(0);
            Assembly campaignAssembly = Assembly.read(aid);
            auuid = campaignAssembly.getUuidAsString();
            cuuid = c.getUuidAsString();
            // check if contribution has a wg
            List<WorkingGroup> wgs = contribution.getWorkingGroupAuthors();
            if (wgs !=null && wgs.size()>0) {
                guuid = wgs.get(0).getUuid().toString();
                url = Play.application().configuration().getString("application.uiUrl")
                        + "/p/assembly/{auuid}/campaign/{cuuid}/group/{guuid}/contribution/{conuuid}";
                url = url.replace("{auuid}", auuid)
                        .replace("{cuuid}", cuuid)
                        .replace("{guuid}", guuid)
                        .replace("{conuuid}", conuuid);
            } else {
                url = url.replace("{auuid}", auuid)
                        .replace("{cuuid}", cuuid)
                        .replace("{conuuid}", conuuid);
            }
        } else if (wg!=null) {
            Long cid = wg.getCampaigns().get(0);
            Campaign gCampaign = Campaign.read(cid);
            Long aid = gCampaign.getAssemblies().get(0);
            Assembly campaignAssembly = Assembly.read(aid);
            auuid = campaignAssembly.getUuidAsString();
            cuuid = gCampaign.getUuidAsString();
            guuid = wg.getUuid().toString();
            url = Play.application().configuration().getString("application.uiUrl")
                    + "/p/assembly/{auuid}/campaign/{cuuid}/group/{guuid}/contribution/{conuuid}";
            url = url.replace("{auuid}", auuid)
                    .replace("{cuuid}", cuuid)
                    .replace("{guuid}", guuid)
                    .replace("{conuuid}", conuuid);
        } else {
            url = Play.application().configuration().getString("application.uiUrl") + "/p/contribution/{conuuid}";
            url = url.replace("{conuuid}", conuuid);
        }
        return url;
    }

    private static String getMemberAuthorTemplate(Contribution contribution, Campaign campaign) {
        String template = null;
        for(Config config: campaign.getConfigs()) {
            if (config.getKey().equals(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_AUTHORSHIP_INVITATION_EMAIL_TEMPLATE)) {
                template = config.getValue();
            }
        }
        return template;
    }

    private static Lang getMailLang(String lang, Contribution contribution, String initiativeLang) {
        Lang aRet = Lang.forCode(lang);
        if(lang == null) {
            if (initiativeLang == null) {
                Campaign campaign = Campaign.find.byId(contribution.getCampaignIds().get(0));
                aRet = campaign.getLang() == null ? null : Lang.forCode(campaign.getLang());
                if (aRet == null) {
                    Assembly assembly = Assembly.findById(contribution.getAssemblyId());
                    aRet = assembly.getLang() == null ? null : Lang.forCode(assembly.getLang());
                    Logger.info("Changed MailLang to use Assembly Lang: "+aRet);
                } else {
                    Logger.info("Changed MailLang to use Campaign Lang: "+aRet);
                }
            } else {
                aRet = Lang.forCode(initiativeLang);
            }
        }
        return aRet;
    }

    public static void sendAuthorAddedMail(User memberAuthor, List<NonMemberAuthor> nonMemberAuthors,
                                           Contribution contribution, ResourceSpace container) {
        Logger.debug("Preparing email to send to added author(s)...");
        String contributionUUID = contribution.getUuidAsString();
        Assembly containerAssembly = null; // ToDo: personalize emails for contributions in assemblies
        Campaign containerCampaign= null;
        WorkingGroup containerGroup = null; // ToDo: personalize emails for contributions in wgs
        Contribution containerContribution = null; // ToDo: personalize emails for contributions in other contributions

        // Get container object
        if (container.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
            containerAssembly = container.getAssemblyResources();
        } else if (container.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
            containerCampaign = container.getCampaign();
        } else if (container.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
            containerGroup = container.getWorkingGroupResources();
        } else {
            containerContribution = container.getContribution();
        }

        String template = null;
        if (containerCampaign != null) {
            template = getMemberAuthorTemplate(contribution, containerCampaign);
        }

        Logger.debug("Preparing parameters...");
        // Email parameters
        String cType = contribution.getType().toString().toLowerCase();
        String cTypeKey = "appcivist.contribution."+cType;
        String cTypePluralKey = cTypeKey+"s";

        String initiativeName =
                containerAssembly != null ? containerAssembly.getShortname() :
                        containerCampaign != null ? containerCampaign.getShortname() :
                                containerGroup !=null ? containerGroup.getName() :
                                        containerContribution!=null ? containerContribution.getTitle() : "";
        String initiativeLang =
                containerAssembly != null ? containerAssembly.getLang() :
                        containerCampaign != null ? containerCampaign.getLang() :
                                containerGroup !=null ? containerGroup.getLang() :
                                        containerContribution!=null ? containerContribution.getLang() : null;
        String url = getContributionMailUrl(contribution, containerAssembly, containerCampaign, containerGroup);

        Logger.info("MailLang to use initially: "+initiativeLang);
        Logger.debug("Email to send with the following params: "
                + "cType = " + cType
                + ", initiativeName=" + initiativeName
                + ", initiativeLang=" + initiativeLang
                + ", url=" + url
                +", containingResourceSpaceType=" + container.getType()
                +", author =" + (memberAuthor !=null ? memberAuthor.toString() : "")
                +", nonMemberAuthor=" + (nonMemberAuthors!=null ? nonMemberAuthors.toString() : ""));

        if (memberAuthor!=null) {
            sendMailToAuthor(memberAuthor.getLang(), memberAuthor.getEmail(), contribution, initiativeLang, cTypeKey,
                    cTypePluralKey, initiativeName, url, template);
        }

        if (nonMemberAuthors !=null) {
            for(NonMemberAuthor author: nonMemberAuthors) {
                if(author.getEmail() != null) {
                    sendMailToAuthor(author.getLang(), author.getEmail(), contribution, initiativeLang, cTypeKey,
                            cTypePluralKey, initiativeName, url, template);
                }
            }
        }
    }

    private static void sendMailToAuthor(String authorLang, String authorEmail, Contribution contribution,
                                         String initiativeLang, String cTypeKey, String cTypePluralKey,
                                         String initiativeName, String url, String template) {
        Lang lang = getMailLang(authorLang, contribution, initiativeLang);
        if (lang == null) {
            lang = Lang.forCode(Messages.get(GlobalData.DEFAULT_LANGUAGE));
            Logger.info("Changed MailLang to use Default: "+lang);
        }
        String bodyText;
        String subject;
        ctx().changeLang(lang);
        String cTypeTranslated = Messages.get(lang, cTypeKey);
        String cTypePluralTranslated = Messages.get(lang, cTypePluralKey);
        bodyText = Messages.get(lang,"mail.notification.add.nonmember", cTypeTranslated, initiativeName, cTypeTranslated, cTypeTranslated, url);
        subject = Messages.get(lang,"mail.notification.add.nonmember.subject" , initiativeName);
        MembershipInvitation membershipInvitation = new MembershipInvitation();
        membershipInvitation.setEmail(authorEmail);
        MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
        if(template != null) {
            template = template.replace("[link]", url);
            provider.sendInvitationByEmail(membershipInvitation, template, subject);
        } else {
            provider.sendInvitationByEmail(membershipInvitation, bodyText, subject);
        }
    }

    /**
     * This method is reused by all other contribution creation methods to centralize its logic
     *
     * @param newContrib
     * @param author
     * @param type
     * @param etherpadServerUrl
     * @param etherpadApiKey
     * @param t
     * @param containerResourceSpace
     * @return
     * @throws MalformedURLException
     * @throws MembershipCreationException
     */
    public static Contribution createContribution(Contribution newContrib,
                                                  User author, ContributionTypes type, String etherpadServerUrl, String etherpadApiKey,
                                                  ContributionTemplate t, ResourceSpace containerResourceSpace)
            throws MalformedURLException, MembershipCreationException, UnsupportedEncodingException, ConfigurationException {

        newContrib.setType(type);
        List<NonMemberAuthor> nonMemberAuthors = new ArrayList<NonMemberAuthor>();
        // Create NonMemberAuthors associated with the Contribution
        if(newContrib.getNonMemberAuthors()!=null && newContrib.getNonMemberAuthors().size()>0){
            for (NonMemberAuthor nonMemberAuthor:newContrib.getNonMemberAuthors()) {
                nonMemberAuthor.save();
                nonMemberAuthor.refresh();
                nonMemberAuthors.add(nonMemberAuthor);
            }
            newContrib.setNonMemberAuthors(nonMemberAuthors);
            newContrib.setNonMemberAuthor(nonMemberAuthors.get(0));
        }

        // Add author to the proposal and use its Lang as the language of the Proposal
        // TODO: derive language from the text
        if (author != null) {
            newContrib.addAuthor(author);
            newContrib.setCreator(author);
            newContrib.setContextUserId(author.getUserId());
            newContrib.setLang(author.getLanguage());
            F.Promise.promise(() -> {
                if (Subscription.findByUserIdAndSpaceId(author, containerResourceSpace.getUuid().toString()).isEmpty()) {
                    Subscription subscription = new Subscription();
                    subscription.setSubscriptionType(SubscriptionTypes.REGULAR);
                    subscription.setUserId(author.getUuid().toString());
                    String uuid = "";
                    if (containerResourceSpace.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
                        uuid = containerResourceSpace.getAssemblyResources().getUuidAsString();
                    } else if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                        uuid = containerResourceSpace.getCampaign().getUuidAsString();
                    } else if (containerResourceSpace.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
                        uuid = containerResourceSpace.getWorkingGroupResources().getUuid().toString();
                    } else if (containerResourceSpace.getType().equals(ResourceSpaceTypes.COMPONENT)) {
                        uuid = containerResourceSpace.getComponent().getUuid().toString();
                    }
                    subscription.setSpaceId(uuid);
                    subscription.setSpaceType(ResourceSpaceTypes.CONTRIBUTION);
                    Logger.info("Creating subscription to the resource space of the contribution");
                    NotificationsDelegate.subscribeToEvent(subscription);
                    subscription.insert();
                }
                return Optional.ofNullable(null);
            });
        }

        // If still there is no language, try first the first NonMemberAuthor and then the Campaign, WG, and Assembly, in that order
        if (newContrib.getLang() == null){
            if (newContrib.getNonMemberAuthor() != null && newContrib.getNonMemberAuthor().getLang()!=null) {
                newContrib.setLang(newContrib.getNonMemberAuthor().getLang());
            }
            if (newContrib.getLang() == null){
                if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                    Campaign c = containerResourceSpace.getCampaign();
                    newContrib.setLang(c.getLang());
                } else if (containerResourceSpace.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
                    WorkingGroup wg = containerResourceSpace.getWorkingGroupResources();
                    newContrib.setLang(wg.getLang());
                } else if (containerResourceSpace.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
                    Assembly a = containerResourceSpace.getAssemblyResources();
                    newContrib.setLang(a.getLang());
                }
            }
        }


        if(type != null
        		&& (type.equals(ContributionTypes.PROPOSAL) || type.equals(ContributionTypes.NOTE))) {
	        if (etherpadServerUrl == null || etherpadServerUrl.isEmpty()) {
	            // read etherpad server url from config file
	            Logger.info("Etherpad URL was not configured");
	            etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
	        }

	        if (etherpadApiKey == null || etherpadApiKey.isEmpty()) {
	            // read etherpad server url from config file
	            Logger.info("Etherpad API Key was not configured");
	            etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
	        }

	        Logger.info("Using Etherpad server at: " + etherpadServerUrl);
	        Logger.debug("Using Etherpad API Key: " + etherpadApiKey);
        }

        // If contribution is a proposal and there is no working group associated as author,
        // create one automatically with the creator as coordinator

        // Create group if not provided
        Config requireWGConfig = containerResourceSpace.getConfigByKey(GlobalDataConfigKeys.APPCIVIST_REQUIRE_GROUP_AUTHORSHIP);
        Config autoCreateWGConfig = containerResourceSpace.getConfigByKey(GlobalDataConfigKeys.APPCIVIST_CREATE_GROUP_ON_NEW_PROPOSALS);
        Boolean requireWG = requireWGConfig != null ? requireWGConfig.getValue().toLowerCase() == "true" : false;
        Boolean autoCreateWG = autoCreateWGConfig != null ? autoCreateWGConfig.getValue().toLowerCase() == "true" : false;
        Boolean createWG = false;
        List<WorkingGroup> workingGroupAuthors = newContrib.getWorkingGroupAuthors();
        List<WorkingGroup> workingGroupAuthorsLoaded = new ArrayList<WorkingGroup>();
        String newWorkingGroupName = "WG for '" + newContrib.getTitle() + "'";
        if (workingGroupAuthors != null && !workingGroupAuthors.isEmpty()) {
            WorkingGroup wg = workingGroupAuthors.get(0);
            if (wg.getGroupId() == null && wg.getUuid() == null) {
                // if proposal contains the definition of a new WG, set workingGroupAuthors to null and create the group
                newWorkingGroupName = wg.getName();
                workingGroupAuthors = null;
                newContrib.setWorkingGroupAuthors(null);
                createWG = true; // the proposal has info for a new WG
            } else {
                // if the proposal contains one or more WGs as authoring groups
                for (WorkingGroup wgroup: newContrib.getWorkingGroupAuthors()) {
                    WorkingGroup dbGroup = WorkingGroup.read(wgroup.getGroupId());
                    if (dbGroup == null) {
                        dbGroup = WorkingGroup.readByUUID(wgroup.getUuid());
                    }
                    workingGroupAuthorsLoaded.add(dbGroup);
                }
                newContrib.setWorkingGroupAuthors(workingGroupAuthorsLoaded);
            }
        }

        // If the proposal does not define an existing WG and
        // -- it contains the definition for a new group,
        // -- or the resource space requires a WG and it is configured to allow automatic creation
        // then create the WG
        WorkingGroup newWorkingGroup = new WorkingGroup();
        Boolean workingGroupIsNew = true;
        if (workingGroupAuthors==null || workingGroupAuthors.isEmpty()) {
            if (createWG || (requireWG && autoCreateWG)) {
                // if the resource space allows automatically creating the WG
                newWorkingGroup.setCreator(author);
                newWorkingGroup.setName(newWorkingGroupName);
                newWorkingGroup.setLang(author.getLanguage());
                newWorkingGroup.setExistingThemes(newContrib.getExistingThemes());
                newWorkingGroup.setListed(false);
                newWorkingGroup
                        .setInvitationEmail("Hello! You have been invited to be a member of the "
                                + "Working Group \"" + newWorkingGroup.getName() + "\" in AppCivist. "
                                + "If you are interested, follow the link attached to this "
                                + "invitation to accept it. If you are not interested, you "
                                + "can just ignore this message");
                newWorkingGroup.setMajorityThreshold("simple");
                newWorkingGroup.setBlockMajority(false);

                WorkingGroupProfile newWGProfile = new WorkingGroupProfile();
                newWGProfile.setIcon("https://s3-us-west-1.amazonaws.com/appcivist-files/icons/justicia-140.png");
                newWGProfile.setManagementType(ManagementTypes.COORDINATED_AND_MODERATED);
                newWGProfile.setSupportedMembership(SupportedMembershipRegistration.INVITATION_AND_REQUEST);
                newWorkingGroup.setProfile(newWGProfile);
                newWorkingGroup = WorkingGroup.create(newWorkingGroup);
                containerResourceSpace.addWorkingGroup(newWorkingGroup);
                newContrib.getWorkingGroupAuthors().add(newWorkingGroup);
                workingGroupAuthorsLoaded.add(newWorkingGroup);
            } else if (requireWG && !autoCreateWG) {
                // if WGs are required on proposals, the proposal doesn't have one and automatic creation is disabled,
                // throw a configuration exception
                throw new ConfigurationException("A working groups is required in this space. Automatic creation of it is disabled. Contact your administrator");
            }
        }

        Boolean addIdeaToProposals = false;
        String allowEmergentDefault = GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_EMERGENT_THEMES);
        Boolean allowEmergent = allowEmergentDefault != null && allowEmergentDefault.equals("TRUE");
        if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)
        		&& type != null && (type.equals(ContributionTypes.PROPOSAL) || type.equals(ContributionTypes.NOTE) || type.equals(ContributionTypes.IDEA))) {
            Campaign c = containerResourceSpace.getCampaign();

        	List<Config> campaignConfigs = c.getConfigs();
        	Integer hasStatusConfig = 0;
            Integer hasEtherpadConfig = 0;
            Integer hasIdeasDuringProposal = 0;

        	for(Config cc: campaignConfigs){
        		if (type.equals(ContributionTypes.PROPOSAL)) {
        			if (cc.getKey().equals(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS)) {
        				hasStatusConfig = 1;
	        			if (newContrib.getStatus() == null && cc.getValue().equalsIgnoreCase("DRAFT")) {
	        				newContrib.setStatus(ContributionStatus.DRAFT);
	        			} else if (newContrib.getStatus() == null && cc.getValue().equalsIgnoreCase("PUBLISHED")) {
	        				newContrib.setStatus(ContributionStatus.PUBLISHED);
	        			} else if (newContrib.getStatus() == null) {
	        				newContrib.setStatus(ContributionStatus.PUBLISHED);
	        			}
        			}
        		}

        		if (type != null && (type.equals(ContributionTypes.PROPOSAL) || type.equals(ContributionTypes.NOTE))) {
	    			if (cc.getKey().equals(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD)){
	    				hasEtherpadConfig = 1;
	    				//if (cc.getValue().equalsIgnoreCase("FALSE")) {
	    				//	ContributionsDelegate.createAssociatedPad(etherpadServerUrl, etherpadApiKey, newContrib, t, containerResourceSpace.getResourceSpaceUuid());
	    				//}
	    	        }
	                if (cc.getKey().equals(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ALLOW_EMERGENT_THEMES)){
	                    if (cc.getValue().equalsIgnoreCase("TRUE")) {
	                        allowEmergent = true;
	                    }
	                }
	                if (type.equals(ContributionTypes.IDEA)) {
	                    if (cc.getKey().equals(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ENABLE_IDEAS_DURING_PROPOSALS)){
	                        hasIdeasDuringProposal = 1;
	                        if (cc.getValue().equals("TRUE")) {
	                            addIdeaToProposals=true;
	                        }
	                    }
	    			}
        		}
        	}
        	// If the configuration is not defined, get the defaults values
        	if (newContrib.getStatus() == null && hasStatusConfig == 0 && type.equals(ContributionTypes.PROPOSAL)) {
        		String status = GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_PROPOSAL_DEFAULT_STATUS);
        		newContrib.setStatus(ContributionStatus.valueOf(status));
        	}

			if (type != null
					&& (type.equals(ContributionTypes.PROPOSAL) || type
							.equals(ContributionTypes.NOTE))) {
				if (hasEtherpadConfig == 0) {
					String etherpad = GlobalDataConfigKeys.CONFIG_DEFAULTS
							.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_DISABLE_ETHERPAD);
					//if (etherpad.equalsIgnoreCase("FALSE"))
					//	ContributionsDelegate.createAssociatedPad(etherpadServerUrl, etherpadApiKey, newContrib, t, containerResourceSpace.getResourceSpaceUuid());
				}
			}
            if (hasIdeasDuringProposal == 0 && type.equals(ContributionTypes.IDEA)) {
                String ideasDuringProposal = GlobalDataConfigKeys.CONFIG_DEFAULTS.get(GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_ENABLE_IDEAS_DURING_PROPOSALS);
                if (ideasDuringProposal.equals("TRUE")){
                    addIdeaToProposals=true;
                }
            }
        }
        if (containerResourceSpace.getType().equals(ResourceSpaceTypes.WORKING_GROUP)){
            WorkingGroup wg = containerResourceSpace.getWorkingGroupResources();
            if (wg==null)
                wg = containerResourceSpace.getWorkingGroupForum();
            List<Config> groupConfigs = wg.getConfigs();
            for(Config cc: groupConfigs){
                if (cc.getKey().equals(GlobalDataConfigKeys.APPCIVIST_WG_ALLOW_EMERGENT_THEMES)){
                    if (cc.getValue().equalsIgnoreCase("TRUE")) {
                        allowEmergent = true;
                    }
                }
            }
        }
        Logger.info("Creating new contribution");
        Logger.debug("=> " + newContrib.toString());

        // Get list of BRAINSTORMING contributions that inspire the new one
        List<Contribution> inspirations = newContrib.getAssociatedContributions();

        List<User> authorsLoaded = new ArrayList<User>();
        Map<Long,Boolean> authorAlreadyAdded = new HashMap<>();
        for (User user: newContrib.getAuthors()) {
            Long userId = user.getUserId();
            User auth = User.read(userId);
            Boolean alreadyAdded = authorAlreadyAdded.get(userId);
            if (alreadyAdded == null || !alreadyAdded) {
                authorsLoaded.add(auth);
                authorAlreadyAdded.put(auth .getUserId(), true);
            }
        }
        newContrib.setAuthors(authorsLoaded);

        if (newContrib.getCover()!=null && newContrib.getCover().getResourceId()!=null){
            Resource cover = Resource.read(newContrib.getCover().getResourceId());
            newContrib.setCover(cover);
        }
        List<Theme> themeListEmergent = new ArrayList<Theme>();
        List<Theme> themeListOfficial = newContrib.getOfficialThemes();
        newContrib.setExistingThemes(newContrib.getOfficialThemes()==null?new ArrayList<Theme>():newContrib.getOfficialThemes());
        if(newContrib.getEmergentThemes()!=null && newContrib.getEmergentThemes().size()>0){
            List<Theme> themeList = new ArrayList<Theme>();
            for (Theme theme:newContrib.getEmergentThemes()) {
                if (theme.getThemeId()==null && allowEmergent){
                    theme.save();
                    theme.refresh();
                    themeListEmergent.add(theme);
                }else if (allowEmergent){
                    themeListEmergent.add(theme);
                }
                themeList.add(theme);
            }
            newContrib.getExistingThemes().addAll(themeList);
            //emergent themes in themeListEmergent to associate to campaign or wg
        }

        Contribution.create(newContrib, containerResourceSpace);

        // Check that contribution was correctly added to WGs
        if (workingGroupAuthors!=null && !workingGroupAuthors.isEmpty()) {
            Integer numberOfWGs = workingGroupAuthors.size();
            List<WorkingGroup> refreshedGroups = newContrib.getWorkingGroupAuthors();
            Integer numberOfRefreshedWgs = refreshedGroups !=null ? refreshedGroups.size() : 0;

            if (numberOfRefreshedWgs<numberOfWGs) {
                Logger.debug("Orphan detected. Request WGs = "+numberOfWGs+ ". Created contribution WGs = "+numberOfRefreshedWgs);
                Logger.debug("Adding again to WGs, checking if already exists first");
                // Add contribution to working group authors only if it is not there
                WorkingGroupsDelegate.addContributionToWorkingGroups(newContrib, workingGroupAuthors, true);
            }
        }

        newContrib.refresh();
        Logger.info("Contribution created with id = "+newContrib.getContributionId());
        F.Promise.promise(() -> {
            sendAuthorAddedMail(null, nonMemberAuthors, newContrib, containerResourceSpace);
                    PeerDocWrapper peerDocWrapper = new PeerDocWrapper(author);
                    peerDocWrapper.updatePeerdocPermissions(newContrib);
                    return Optional.ofNullable(null);
        });
        //Previously we also asked the associated contribution to be PROPOSAL,
        //but now any type of contribution can be associated to another
        if (inspirations != null && !inspirations.isEmpty()) {
            ResourceSpace cSpace = ResourceSpace.read(newContrib.getResourceSpaceId());
            for (Contribution inspiration : inspirations) {
                Contribution c = Contribution.read(inspiration.getContributionId());
                cSpace.addContribution(c);
            }
            cSpace.update();
            cSpace.refresh();

            // If the proposal is coming from brainstorming, invite the
            // commenters of the
            // each brainstorming contributions
            if (!inspirations.isEmpty() && workingGroupIsNew) {
                inviteCommentersInInspirationList(inspirations, newContrib, newWorkingGroup);
            }
        }

        //associate themes created to space
        if (containerResourceSpace != null && (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)
                || containerResourceSpace.getType().equals(ResourceSpaceTypes.WORKING_GROUP))) {
            List<Theme> themeListEmergentCpWg = new ArrayList<Theme>();
            for (Theme theme: themeListEmergent) {
                if(theme.getType().equals(ThemeTypes.EMERGENT)){
                    themeListEmergentCpWg.add(theme);
                }
            }
            containerResourceSpace.getThemes().addAll(themeListEmergentCpWg);
            containerResourceSpace.update();
            containerResourceSpace.refresh();
        }

        if(addIdeaToProposals) {
            List<Long> assignToContributions = newContrib.getAssignToContributions();
            List<Contribution> contributionList = new ArrayList<Contribution>();
            if (assignToContributions != null) {
	            for (Long cid : assignToContributions) {
	                Contribution contribution = Contribution.read(cid);
	                if (contribution.getType().equals(ContributionTypes.PROPOSAL)) {
	                    contributionList.add(contribution);
	                }
	            }
            }
            newContrib.setAssociatedContributions(contributionList);
            newContrib.getResourceSpace().update();
            newContrib.getResourceSpace().refresh();
        }

        ContributionStatusAudit.create(newContrib);

        return newContrib;
    }


    /**
     * It looks into the list of comments of the brainstorming contributions that serve as inspiration
     * for a new proposal
     *
     * @param inspirations
     * @param newContrib
     * @param newWorkingGroup
     * @throws MembershipCreationException
     */
    private static void inviteCommentersInInspirationList(
            List<Contribution> inspirations, Contribution newContrib,
            WorkingGroup newWorkingGroup) throws MembershipCreationException {
        HashMap<String, Boolean> invitedEmails = new HashMap<String, Boolean>();
        for (Contribution inspirationObject : inspirations) {
            Contribution inspiration = Contribution.read(inspirationObject.getContributionId());
            List<Contribution> comments = inspiration.getComments();
            for (Contribution comment : comments) {
                User commentAuthor = comment.getAuthors().get(0);
                String authorEmail = commentAuthor.getEmail();
                String creatorEmail = newContrib.getAuthors().get(0).getEmail();
                Boolean invited = invitedEmails.get(authorEmail);
                if ((invited == null || !invited) && !authorEmail.equals(creatorEmail)) {
                    InvitationTransfer invitation = new InvitationTransfer();
                    invitation.setEmail(authorEmail);
                    invitation.setInvitationEmail(newWorkingGroup.getInvitationEmail());
                    invitation.setTargetId(newWorkingGroup.getGroupId());
                    invitation.setTargetType("GROUP");
                    invitation.setCoordinator(false);
                    invitation.setModerator(false);
                    MembershipInvitation.create(invitation, newWorkingGroup.getCreator(), newWorkingGroup);
                    invitedEmails.put(authorEmail, true);
                }
            }
        }

    }

    public static Result createContributionResult(Contribution newContrib,
                                                  User author, ContributionTypes type, ContributionTemplate t, ResourceSpace containerResourceSpace) {
        try {
            return ok(Json.toJson(createContribution(newContrib, author, type, t, containerResourceSpace)));
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when creating Contribution: " + e.toString())));
        }
    }

    public static Contribution createContribution(Contribution newContrib,
                                                  User author, ContributionTypes type, ContributionTemplate t,
                                                  ResourceSpace containerResourceSpace) throws MalformedURLException, MembershipCreationException, UnsupportedEncodingException, ConfigurationException {
        // TODO: dynamically obtain etherpad server URL and Key from component configuration
        return createContribution(newContrib, author, type, null, null, t, containerResourceSpace);
    }

    public static Result createContributionInAssembly(
            Contribution newContrib, User author, Assembly a,
            ContributionTypes type) {
        ResourceSpace rs = a.getResources();

        ContributionTemplate template = null;
        if (newContrib.getType() != null && newContrib.getType().equals(ContributionTypes.PROPOSAL)) {
            List<ContributionTemplate> templates = rs.getTemplates();
            if (templates != null && !templates.isEmpty())
                template = rs.getTemplates().get(0);
        }

        Contribution c;
        try {
            c = createContribution(newContrib, author, type, template, rs);
        } catch (Exception e) {
            return internalServerError(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.SERVERERROR,
                            "Error when creating contribution: " + e.toString())));
        }
        rs.addContribution(c);
        rs.update();
        return ok(Json.toJson(c));
    }

    public static Result updateContributionResult(Long sid, Long id,
                                                  Contribution updatedContribution, ContributionTypes type) {
        updatedContribution.setContributionId(id);
        if (updatedContribution.getType().equals(type)) {
            Contribution.update(updatedContribution);
            Logger.info("Updating contribution");
            Logger.debug("=> " + updatedContribution.toString());
            return ok(Json.toJson(updatedContribution));
        } else {
            return contributionUpdateError(updatedContribution, "Update on "
                    + type + " for Contribution that is not of this type");
        }
    }

    /*
     * Non-exposed methods: error Message Results
     */
    private static Result contributionCreateError(
            Form<Contribution> newContributionForm) {
        return contributionOperationError(
                GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newContributionForm
                        .errorsAsJson().toString());
    }

    private static Result contributionFeedbackError(Form<ContributionFeedback> newStatsForm) {
        // TODO Auto-generated method stub
        return contributionOperationError(
                GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newStatsForm
                        .errorsAsJson().toString());
    }

    private static Result contributionFeedbackError(ContributionFeedback feedback, String errorMsg) {
        // TODO Auto-generated method stub
        return contributionOperationError(
                GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR, "{ error : " + errorMsg + ", object : "
                        + Json.toJson(feedback));
    }

    public static Result contributionUpdateError(
            Form<Contribution> updatedContributionForm) {
        return contributionOperationError(
                GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR,
                updatedContributionForm.errorsAsJson().toString());
    }

    public static Result contributionUpdateError(
            Contribution updatedContribution, String errorMsg) {
        return contributionOperationError(
                GlobalData.CONTRIBUTION_UPDATE_MSG_ERROR,
                "{ error : " + errorMsg + ", object : "
                        + Json.toJson(updatedContribution));
    }

    public static Result contributionCreateError(Contribution newContribution,
                                                 String errorMsg) {
        return contributionOperationError(GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, "{ error : " + errorMsg + ", object : "
                + Json.toJson(newContribution));
    }

    public static Result contributionOperationError(String msgi18nCode,
                                                    String msg) {
        TransferResponseStatus responseBody = new TransferResponseStatus();
        responseBody.setStatusMessage(Messages.get(msgi18nCode) + ": " + msg);
        return badRequest(Json.toJson(responseBody));
    }

    /** IDEAS **/
    /**
     * POST      /api/assembly/:aid/campaign/:cid/contribution/import
     * Import ideas file
     *
     * @param aid Assembly Id
     * @param cid Campaing Id
     * @return
     */
    @ApiOperation(httpMethod = "POST", consumes = "application/csv", value = "Import CSV file with campaign ideas",
            notes = "CSV format: idea title, idea summary, idea author, idea theme <br/>" +
                    "The values must be separated by coma (,). If the theme column has more than one theme, then it must be separated by dash (-).")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result importContributionsOld(
            @ApiParam(name = "aid", value = "Assembly id") Long aid,
            @ApiParam(name = "cid", value = "Campaign id") Long cid) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
        Campaign campaign = null;

        if (uploadFilePart != null) {
            try {
                campaign = Campaign.read(cid);
                ResourceSpace rs = null;
                if (campaign != null) {
                    rs = campaign.getResources();
                }
                br = new BufferedReader(new FileReader(uploadFilePart.getFile()));
                String cvsSplitBy = ",";
                String line = br.readLine();
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    String[] cell = line.split(cvsSplitBy);
                    Contribution c = new Contribution();
                    c.setTitle(cell[0]);
                    c.setText(cell[1]);
                    // TODO existing author
                    c.setFirstAuthorName(cell[2]);
                    // TODO existing theme
                    List<Theme> themesList = new ArrayList<Theme>();
                    String themeSplitBy = "-";
                    String[] themes = cell[3].split(themeSplitBy);
                    for (String theme : themes) {
                        Theme t = new Theme();
                        t.setTitle(theme);
                        themesList.add(t);
                    }
                    c.setThemes(themesList);
                    c.setSourceCode(cell[4]);

                    Resource res = null;

                    switch (cell[5]) {
                        case "IDEA":
                            c.setType(ContributionTypes.IDEA);
                            break;
                        case "PROPOSAL":
                            c.setType(ContributionTypes.PROPOSAL);
                            // Etherpad support
                            String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
                            String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);

                            if (cell[6] != null) {
                                res = ResourcesDelegate.createResource(null, cell[6], ResourceTypes.PROPOSAL, true, false);
                            } else {
                                List<Resource> templates = ContributionsDelegate.getTemplates(aid.toString(), cid.toString());

                                if (templates != null) {
                                    // if there are more than one, then use the last
                                    String padId = templates.get(templates.size() - 1).getPadId();
                                    EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
                                    String templateHtml = wrapper.getHTML(padId);
                                    // save the etherpad
                                    res = ResourcesDelegate.createResource(null, templateHtml, ResourceTypes.PROPOSAL, true, false);
                                }
                            }
                            break;
                        default:
                            break;
                    }

                    rs.addContribution(c);
                    ResourceSpace.update(rs);

                    if (res != null) {
                        List<Resource> resources = new ArrayList<>();
                        resources.add(res);
                        c.setExistingResources(resources);
                    }

                    Contribution.create(c,null);

                    // Feedback support
                    if (cell.length == 9) {
                        String feedback = cell[7];
                        String feedbackUser = cell[8];
                        ContributionFeedback cFeed = new ContributionFeedback();
                        switch (feedback) {
                            case "up":
                                cFeed.setUp(true);
                                break;
                            case "down":
                                cFeed.setDown(true);
                                break;
                            default:
                                break;
                        }
                        User user = User.findByUserName(feedbackUser);
                        cFeed.setUserId(user.getUserId());
                        cFeed.setContribution(c);
                        cFeed.setType(ContributionFeedbackTypes.TECHNICAL_ASSESSMENT);
                        ContributionFeedback.create(cFeed);
                    }

                }
                //Ebean.commitTransaction();
            } catch (EntityNotFoundException ex) {
                ex.printStackTrace();
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Internal server error : " + ex.toString())));
            } catch (Exception e) {
                //Ebean.rollbackTransaction();
                e.printStackTrace();
                return contributionFeedbackError(null, e.getLocalizedMessage());
            }
        }
        return ok();
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/export
     * Export ideas file
     *
     * @param aid Assembly Id
     * @param cid Campaing Id
     * @return
     */
    @ApiOperation(httpMethod = "GET", produces = "application/csv", value = "Export campaign ideas to a CSV file")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result exportContributions(
            @ApiParam(name = "aid", value = "Assembly id") Long aid,
            @ApiParam(name = "cid", value = "Campaign id") Long cid) {
        String csv = "idea title,idea summary,idea author,idea theme, source code, type\n";
        Campaign campaign = null;
        try {
            campaign = Campaign.read(cid);
            ResourceSpace rs = null;
            if (campaign != null) {
                rs = campaign.getResources();
                List<Contribution> contributions = ContributionsDelegate
                        .findContributionsInResourceSpace(rs, null);
                for (Contribution c : contributions) {
                    csv = csv + (c.getTitle() != null ? c.getTitle() : "") + ",";
                    csv = csv + (c.getAssessmentSummary() != null ? c.getAssessmentSummary() : "") + ",";
                    // TODO existing author
                    csv = csv + (c.getFirstAuthorName() != null ? c.getFirstAuthorName() : "");
                    csv = csv + ",";
                    int themeSize = c.getThemes().size();
                    for (int i = 0; i < themeSize; i++) {
                        if (i > 0 && i < themeSize + 1) {
                            csv = csv + "-";
                        }
                        csv = csv + c.getThemes().get(i).getTitle();
                    }
                    csv = csv + "," + (c.getSourceCode() != null ? c.getSourceCode() : "");
                    csv = csv + "," + c.getType().toString() + "\n";
                }

            }
        } catch (EntityNotFoundException ex) {
            return notFound(Json.toJson(new TransferResponseStatus("The campaign doesn't exist")));
        }

        response().setContentType("application/csv");
        response().setHeader("Content-disposition", "attachment; filename=contributions.csv");
        File tempFile;
        try {
            tempFile = File.createTempFile("contributions.csv", ".tmp");
            FileUtils.writeStringToFile(tempFile, csv);
            return ok(tempFile);
        } catch (IOException e) {
            return internalServerError(Json.toJson(new TransferResponseStatus("Internal server error: " + e.getMessage())));
        }

    }

    /**
     * Date Parser
     * @param dateString
     * @return
     */
    public static LocalDate parseCreationDate(String dateString) throws Exception{
        List<String> formatStrings = Arrays.asList("MM/dd/yyyy", "yyyy-MM-dd");

        for (String formatString : formatStrings)
        {
            try
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatString);
                return LocalDate.parse(dateString,formatter );
            }
            catch (Exception e) {
                Logger.error("Error parsing date: "+dateString);
                throw (DateTimeParseException) new DateTimeParseException("Text cannot be parsed to a Date: fraction", dateString, 0).initCause(e);
            }
        }
        return null;
    }


    /**
     * POST      /api/space/:sid/import
     * Import ideas file
     *
     * @param sid Resource Space Id
     * @return
     */
    @ApiOperation(httpMethod = "POST", consumes = "application/csv", value = "Import CSV file with campaign ideas or proposals",
            notes = "CSV format: the values must be separated by coma (;). If the theme column has more than one theme, then it must be separated by dash (-).")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result importContributionsResourceSpace(
            @ApiParam(name = "sid", value = "Resource Space id") Long sid,
            @ApiParam(name = "type", value = "Contribution Type", allowableValues = "IDEA, PROPOSAL, COMMENT", defaultValue = "IDEA") String type,
            @ApiParam(name = "createThemes", value = "Contribution Type", defaultValue = "false") Boolean createThemes) {

        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            return notFound(Json.toJson(new TransferResponseStatus("The resource space doesn't exist")));
        }
        if (resourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
            Campaign campaign = resourceSpace.getCampaign();
            return importContributions(null, campaign.getCampaignId(), type, createThemes);
        } else {
            return badRequest(Json.toJson(new TransferResponseStatus("Not implemented")));
        }
    }

    /**
     * POST /api/space/:sid/import-comment-feedback
     * @param sid
     * @return
     */
    @ApiOperation(
            httpMethod = "POST",
            consumes = "application/csv",
            value = "Import CSV of assessments for contributions",
            notes = "Values must be separated by coma (,). Possible columns: source_code, source, id, uuid, name, phone, email, ups, downs, benefit, feasibility, need, text_assessment. If no id nor uuid is provided, code and source_code are used to identify the contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "CoordinatorOfSpace", meta = SecurityModelConstants.SPACE_RESOURCE_PATH)
    public static Result importContributionFeedbacks(
            @ApiParam(name = "aid", value = "Resource Space id") Long sid) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
        ResourceSpace resourceSpace = ResourceSpace.find.byId(sid);
        if(resourceSpace == null || uploadFilePart == null ||  resourceSpace.getCampaign() == null) {
            String errorMessage = uploadFilePart == null ? "Missing import file" : "Campaign does not exist";
            Logger.error(errorMessage);
            return badRequest(
                    Json.toJson(
                            new TransferResponseStatus(ResponseStatus.SERVERERROR, errorMessage)));
        }
        try {
            Reader in = new FileReader(uploadFilePart.getFile());
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            Ebean.beginTransaction();
            for (CSVRecord record : records) {
                Contribution contribution = null;
                String sourceCode = "";
                String source = "";
                // Retrieve contribution for which this assessment is provided
                String cid = record.get("id");
                String cuuid = record.get("uuid");
                if (cid == null || cid.trim().isEmpty()) {
                    if (cuuid == null || cuuid.trim().isEmpty()) {
                        sourceCode = record.get("source_code");
                        source = record.get("source");
                        contribution = Contribution.findBySourceCodeAndSource(source, sourceCode);
                    } else {
                        contribution = Contribution.readByUUID(UUID.fromString(cuuid));
                    }
                } else {
                    contribution = Contribution.read(Long.parseLong(cid));
                }
                if (contribution != null) {
                    String name = record.get("name");
                    String phone = record.get("phone");
                    String email = record.get("email");
                    String text_assessment = record.get("text_assessment");
                    String benefit = record.get("benefit");
                    String feasibility = record.get("feasibility");
                    String need = record.get("need");
                    String ups = record.get("ups");
                    String downs = record.get("downs");

                    ContributionFeedback contributionFeedback = new ContributionFeedback();
                    contributionFeedback.setContribution(contribution);
                    contributionFeedback.setType(ContributionFeedbackTypes.IMPORTED);
                    if (text_assessment != null && !text_assessment.trim().isEmpty())
                        contributionFeedback.setTextualFeedback(text_assessment);
                    if (need != null && !need.trim().isEmpty())
                        contributionFeedback.setNeed(Integer.valueOf(need));
                    if (feasibility != null && !feasibility.trim().isEmpty())
                        contributionFeedback.setFeasibility(Integer.valueOf(feasibility));
                    if (benefit != null && !benefit.trim().isEmpty())
                        contributionFeedback.setBenefit(Integer.valueOf(benefit));

                    if (ups != null && !ups.trim().isEmpty()) {
                        processUpDownVotes(contributionFeedback, true, Integer.valueOf(ups), name, phone, email);
                    }

                    if (downs != null && !downs.trim().isEmpty()) {
                        processUpDownVotes(contributionFeedback, false, Integer.valueOf(downs), name, phone, email);
                    }

                    contributionFeedback.save();
                    HashMap<String,Object> authors = processAuthorsInImport(name, phone, email, contribution);
                    addAuthorsToFeedback(contributionFeedback,authors);
                } else {
                    throw new Exception("No contribution found using [source_code, source, id, uuid] = [" + source + "," + sourceCode + "," + cid + "," + cuuid);
                }
            }
            Ebean.commitTransaction();
        } catch (Exception e) {
            Logger.error(e.getMessage());
            Logger.error("---> AppCivist: A problem occurred importing feedbacks");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Logger.debug("Exception: "+e.getStackTrace().toString()+" | "+e.getMessage()+" | "+sw.toString());
            TransferResponseStatus response = new TransferResponseStatus();
            response.setStatusMessage("Error importing feedback: "+e.getMessage());
            response.setErrorTrace(sw.toString());
            response.setResponseStatus(ResponseStatus.SERVERERROR);
            return internalServerError(Json.toJson(response));
        } finally {
            Ebean.endTransaction();
        }

        return ok();
    }

        /**
         * POST      /api/assembly/:aid/campaign/:cid/contribution/import
         * Import ideas file
         *
         * @param aid Assembly Id
         * @param cid Campaing Id
         * @return
         */
    @ApiOperation(httpMethod = "POST", consumes = "application/csv", value = "Import CSV file with campaign ideas or proposals",
            notes = "CSV format: the values must be separated by coma (;). If the theme column has more than one theme, then it must be separated by dash (-).")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No campaign found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "CSV file", dataType = "file", paramType = "form"),
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
 //   @Dynamic(value = "CoordinatorOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result importContributions(
            @ApiParam(name = "aid", value = "Assembly id") Long aid,
            @ApiParam(name = "cid", value = "Campaign id") Long cid,
            @ApiParam(name = "type", value = "Contribution Type", allowableValues = "IDEA, PROPOSAL, COMMENT", defaultValue = "IDEA") String type,
            @ApiParam(name = "createThemes", value = "Contribution Type", defaultValue = "false") Boolean createThemes) {
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadFilePart = body.getFile("file");
        Campaign campaign = Campaign.read(cid);

        if (createThemes == null) {
            createThemes = false;
        }

        if(! (type.equals("IDEA") || type.equals("PROPOSAL") || type.equals("COMMENT")) ) {
            return badRequest(Json.toJson(new TransferResponseStatus("Not supported Contribution Type: " + type)));
        }

        if (uploadFilePart != null && campaign != null) {
            try {
                br = new BufferedReader(new FileReader(uploadFilePart.getFile()));
                // Supported Format:
                // code*, source, title*, description*, category*, keywords, date, location, group, name, phone, email
                Iterable<CSVRecord> records = CSVFormat.DEFAULT
                        .withHeader(CSVHeaders.class)
                        .withAllowMissingColumnNames()
                        .withIgnoreHeaderCase()
                        .withIgnoreSurroundingSpaces()
                        .withSkipHeaderRecord().parse(br);
                try {
                    Ebean.beginTransaction();
                    for (CSVRecord record : records) {
                        Contribution c = new Contribution();
                        switch (type) {
                            case "IDEA":
                                Logger.info("Importing IDEA");
                                c.setType(ContributionTypes.IDEA);
                                break;
                            case "PROPOSAL":
                                Logger.info("Importing PROPOSAL");
                                c.setType(ContributionTypes.PROPOSAL);
                                break;
                            case "COMMENT":
                                Logger.info("Importing COMMENT");
                                c.setType(ContributionTypes.COMMENT);
                                break;
                        }

                        // SET source_code  title   text
                        if(!record.isSet(CSVHeaders.code.name())
                                || record.get(CSVHeaders.code) == null
                                || record.get(CSVHeaders.code).equals("") ) {
                            return badRequest(Json.toJson(Json
                                    .toJson(
                                            new TransferResponseStatus(
                                                    "Error parsing the CSV, code is missing for "
                                                            + type))));
                        }
                        String sourceCode = record.get(CSVHeaders.code);
                        Logger.info(type + "["+ sourceCode+"]: Starting to import...");
                        c.setSourceCode(sourceCode);
                        c.setSource(record.get(CSVHeaders.source));
                        String title = "";
                        String description = "";

                        if((!record.isSet(CSVHeaders.title.name())
                                    || record.get(CSVHeaders.title) == null
                                    || record.get(CSVHeaders.title).equals(""))
                                && (!record.isSet(CSVHeaders.description.name())
                                    || record.get(CSVHeaders.description) == ""
                                    || record.get(CSVHeaders.description) != "")) {
                            return badRequest(Json.toJson(Json
                                    .toJson(new TransferResponseStatus("Error parsing the CSV," +
                                            " either a title or a description is needed in " + type + " " + sourceCode))));
                        } else if (record.isSet(CSVHeaders.title.name()) && !record.isSet(CSVHeaders.description.name())) {
                            description = title = record.get(CSVHeaders.title);
                        } else if (!record.isSet(CSVHeaders.title.name()) && record.isSet(CSVHeaders.description.name())) {
                            description = title = record.get(CSVHeaders.description);
                        } else {
                            title = record.get(CSVHeaders.title);
                            description = record.get(CSVHeaders.description);
                        }

                        // if title too long, trim to 50 chars...
                        if (title.length()>50) {
                            title = title.substring(0,50)+"...";
                        }

                        // code*, source, title*, description*, category*, keywords, date, location, group, name, phone, email
                        c.setTitle(title);
                        c.setText(description);

                        if(!record.isSet(CSVHeaders.category.name())
                                || record.get(CSVHeaders.category) == null
                                || record.get(CSVHeaders.category).equals("")) {
                            return badRequest(Json.toJson(Json
                                    .toJson(new TransferResponseStatus("Error parsing the CSV," +
                                            " a category is missing for idea: "+type+" "+sourceCode))));
                        }

                        String categoriesLine = record.get(CSVHeaders.category);
                        String[] categories = categoriesLine.split(",");

                        List<Theme> existing = new ArrayList<>();
                        List<Theme> newThemes = new ArrayList<>();
                        for (String category : categories) {
                            Logger.debug(type + "["+ sourceCode+"]: Importing category => " + category);
                            List<Theme> themes = campaign.filterThemesByTitle(category.trim());
                            if (themes.size()>0) {
                                Logger.debug(type + "["+ sourceCode+"]: Importing => "
                                        + themes.size() + " themes found that match category " + category);
                                existing.addAll(themes);
                            } else if (createThemes){
                                Theme t = new Theme();
                                t.setTitle(category);
                                t.setType(ThemeTypes.OFFICIAL_PRE_DEFINED);
                                newThemes.add(t);
                                campaign.addTheme(t);
                            } else {
                                return badRequest(Json.toJson(Json
                                        .toJson(new TransferResponseStatus("Error parsing the CSV," +
                                                " category "+category+"does no exist: "+type+" "+sourceCode))));
                            }
                        }
                        c.setExistingThemes(existing);
                        if (createThemes) {
                            c.setThemes(newThemes);
                        }

                        if(record.isSet(CSVHeaders.keywords.name())) {
                            String emergentThemesLine = record.get(CSVHeaders.keywords);
                            String[] emergenthemes = emergentThemesLine.split(",");
                            existing = new ArrayList<>();
                            for (String category : emergenthemes) {
                                Logger.debug(type + "["+ sourceCode+"]: Importing => emergentTheme => " + category);
                                List<Theme> themes = campaign.filterThemesByTitle(category.trim());

                                if (themes.size() == 0) {
                                    //create new theme
                                    Theme t = new Theme();
                                    t.setDescription(category);
                                    t.setTitle(category);
                                    t.setType(ThemeTypes.EMERGENT);

                                    //add theme to contribution
                                    if (c.getThemes() == null) {
                                        c.setThemes(new ArrayList<>());
                                    }
                                    c.getThemes().add(t);

                                    //add theme to campaing
                                    campaign.addTheme(t);

                                } else {
                                    //reuse theme
                                    Logger.debug(type + "["+ sourceCode+"]: Importing => emergentTheme => "
                                            + themes.size() + " themes found that match category " + category);
                                    for (Theme match : themes) {
                                        c.getExistingThemes().add(match);
                                    }
                                }

                            }
                        } else {
                            Logger.debug(type + "["+ sourceCode+"]: Importing => NO KEYWORDS");
                        }

                        // date
                        String creationDate = "";
                        if(!record.isSet(CSVHeaders.date.name())
                                || record.get(CSVHeaders.date) == null
                                || record.get(CSVHeaders.date).equals("")) {
                            Logger.debug(type + "["+ sourceCode+"]: Importing => No creation date defined, using TODAY");
                        } else {
                            creationDate = record.get(CSVHeaders.date);
                            Logger.debug(type + "["+ sourceCode+"]: Importing => Creation date => " + creationDate);
                            //
                            try {
                                LocalDate ldt = parseCreationDate(creationDate);
                                if (ldt != null) {
                                    c.setCreation(Date.from(ldt.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                }
                            } catch (DateTimeParseException dte) {
                                return badRequest(Json.toJson(Json
                                        .toJson(new TransferResponseStatus("Error parsing the CSV," +
                                                " date must be formatted as \"MM/dd/yyyy\" or \"yyyy-MM-dd\" (in "+type+" "+sourceCode+")"))));
                            }
                        }

                        // location
                        String placeName = "";
                        if(!record.isSet(CSVHeaders.location.name())
                                || record.get(CSVHeaders.location) == null
                                || record.get(CSVHeaders.location).equals("")) {
                            Logger.debug(type + "["+ sourceCode+"]: Importing => No location defined");
                        } else {
                            // LocalDate ldt = LocalDate.parse(creationDate, formatter);
                            placeName = record.get(CSVHeaders.location);
                            //Create new location
                            Logger.debug(type + "["+ sourceCode+"]: Importing => Location => " + placeName);
                            Location l = new Location();
                            l.setPlaceName(placeName);
                            c.setLocation(l);
                        }

                        // group
                        String wgName = "";
                        if(!record.isSet(CSVHeaders.group.name())
                                || record.get(CSVHeaders.group) == null
                                || record.get(CSVHeaders.group).equals("")) {
                            Logger.debug(type + "["+ sourceCode+"]: Importing => No group defined");
                        } else {
                            wgName = record.get(CSVHeaders.group);
                            // Add IDEA to Working Group
                            Logger.debug("Importing => wg => " + wgName);
                            WorkingGroup wg = WorkingGroup.readByName(wgName);
                            if (wg != null) {
                                Logger.info("Addng contribution to WG => " + wgName);
                                wg.addContribution(c);
                                wg.update();
                            }
                        }

                        // SET author name, email and phone
                        List<NonMemberAuthor> existingNonMemberAuthors = new ArrayList<>();
                        List<User> existingUserAuthors = new ArrayList<>();

                        String authors = "";
                        String phones = "";
                        String emails = "";
                        if((!record.isSet(CSVHeaders.authors.name())
                                    || record.get(CSVHeaders.authors) == null
                                    || record.get(CSVHeaders.authors).equals(""))
                                && (!record.isSet(CSVHeaders.phones.name())
                                    || record.get(CSVHeaders.phones) == null
                                    || record.get(CSVHeaders.phones).equals(""))
                                && (!record.isSet(CSVHeaders.emails.name())
                                    || record.get(CSVHeaders.emails) == null)
                                    || record.get(CSVHeaders.emails).equals("")) {
                            Logger.debug(type + "["+ sourceCode+"]: Importing => No aurhors defined");
                        } else {
                            authors = record.get(CSVHeaders.authors);
                            phones = record.get(CSVHeaders.phones);
                            emails = record.get(CSVHeaders.emails);

                            String [] authorArray = authors.split(",");
                            String [] phoneArray = phones.split(",");
                            String [] emailsArray = emails.split(",");

                            Logger.debug("Importing => authors => " + authors);
                            int index = 0;
                            for (String author : authorArray) {
                                String phone = phoneArray != null ? phoneArray.length > index ? phoneArray[index] : "" : "";
                                String email = emailsArray != null ? emailsArray.length > index ? emailsArray[index] : "" : "";
                                index++;

                                HashMap<String,Object> mapOfAuthors = processAuthorsInImport(author, phone, email, c);
                                if (authors!=null && mapOfAuthors != null) {
                                    User userAuthor = (User) mapOfAuthors.get("user");
                                    if (userAuthor==null) {
                                        NonMemberAuthor nonMemberAuthor = (NonMemberAuthor) mapOfAuthors.get("nonUser");
                                        existingNonMemberAuthors.add(nonMemberAuthor);
                                    } else {
                                        existingUserAuthors.add(userAuthor);
                                    }
                                }

                            }
                        }

                        ResourceSpace resourceSpace;
                        // Create the contribution
                        if(type.equals("COMMENT")) {
                            Contribution origin = Contribution.findBySourceCodeAndSource(c.getSource(), c.getSourceCode());
                            if(origin == null) {
                                throw new Exception("No contribution found for the source: "+ c.getSource() + " and source code: "+ c.getSourceCode());
                            }
                            resourceSpace = origin.getResourceSpace();
                            c.setSourceCode(null);
                        } else {
                            Logger.info("Adding contribution to campaign...");
                            resourceSpace = ResourceSpace.read(campaign.getResourceSpaceId());
                            resourceSpace.getContributions().add(c);
                        }
                        c.getContainingSpaces().add(resourceSpace);
                        Contribution.create(c, resourceSpace);
                        c.refresh();
                        resourceSpace.update();
                        Boolean updateContribution = false;
                        if (existingNonMemberAuthors!=null && existingNonMemberAuthors.size()>0) {
                            c.getNonMemberAuthors().addAll(existingNonMemberAuthors);
                            updateContribution = true;
                        }
                        if (existingUserAuthors!=null && existingUserAuthors.size()>0) {
                            c.getAuthors().addAll(existingUserAuthors);
                            updateContribution = true;
                        }
                        if (updateContribution) {
                            c.update();
                        }
                    }
                    Ebean.commitTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                    return internalServerError(Json.toJson(new TransferResponseStatus(ResponseStatus.SERVERERROR,
                            "Tried to import contributions of type: " + type + "\n"
                                    + e.getClass() + ": " + e.getMessage())));
                } finally {
                    Ebean.endTransaction();
                }
            } catch (Exception e) {
                Logger.error(e.getMessage());
                return internalServerError(
                        Json.toJson(
                                new TransferResponseStatus(ResponseStatus.SERVERERROR,
                                        "Tried to import contributions of type: " + type + "\n"
                                                + e.getClass() + ": " + e.getMessage())));
            }
            return ok();
        } else {
            String errorMessage = uploadFilePart == null ? "Missing import file" : "Campaign does not exist";
            Logger.error(errorMessage);
            return internalServerError(
                    Json.toJson(
                            new TransferResponseStatus(ResponseStatus.SERVERERROR,
                                    "Tried to import contributions of type: " + type + "\n"
                                            + errorMessage)));
        }
    }


    /**
     * POST      /api/contribution/pad
     * Create a new Resource PROPOSAL from CONTRIBUTION_TEMPLATE
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Campaign.class, produces = "application/json", value = "Create a new propolsal from contribution template")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contribution found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result createContributionPad(
            @ApiParam(name = "aid", value = "Assembly ID") String aid,
            @ApiParam(name = "cid", value = "Contribution ID") String cid) {
        User campaignCreator = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
        // 1: find into campaign templates, 2: find into assembly templates, 3: find generic templates
        List<Resource> templates = ContributionsDelegate.getTemplates(aid, cid);

        if (templates != null) {
            // if there are more than one, then use the last
            String padId = templates.get(templates.size() - 1).getPadId();
            EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
            String templateHtml = wrapper.getHTML(padId);
            Resource res = ResourcesDelegate.createResource(campaignCreator, templateHtml, ResourceTypes.PROPOSAL, false, true);
            //Create this relationship when the contribution is saved
            //Assembly ass = Assembly.read(aid);
            //ass.getResources().addResource(res);
            //ass.update();
            return ok(Json.toJson(res));
        } else {
            return notFound(Json.toJson(new TransferResponseStatus("There are no templates available")));
        }

    }

    /**
     * PUT       /api/contribution/pad
     * Confirm a Resource PROPOSAL
     *
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Resource.class, produces = "application/json", value = "Create a new Campaign")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result confirmContributionPad(
            @ApiParam(name = "rid", value = "Resource (that represents that PAD) ID") Long rid) {
        Resource res = ResourcesDelegate.confirmResource(rid);
        return ok(Json.toJson(res));
    }

    /**
     * PUT /api/assembly/:aid/contribution/:cid/:status
     * Confirm a Resource PROPOSAL
     *
     * @param aid
     * @param cid
     * @param status
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update status of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "AuthorOrCoordinator", meta = SecurityModelConstants.CONTRIBUTION_RESOURCE_PATH)
    public static Result updateContributionStatus(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Contribution ID") Long cid,
            @ApiParam(name = "status", value = "New Status for the Contribution",
                    allowableValues = "NEW,PUBLISHED,EXCLUDED,ARCHIVED") String status) {

        Contribution c = Contribution.read(cid);
        Http.Session s = session();
        Logger.debug("Session = "+(s != null ? s : "[no session found]"));
        AuthUser u = PlayAuthenticateLocal.getUser(s);
        Logger.debug("AuthUser = "+(u != null ? u.getId() : "[no user found]"));
        User user = User.findByAuthUserIdentity(u);

        if(user == null || (c.getStatus().equals(ContributionStatus.PUBLISHED) &&  !user.isAdmin())) {
            return badRequest(Json.toJson(new TransferResponseStatus("The contribution is already " +
                    "published and cannot be changed anymore")));
        }
        String upStatus = status.toUpperCase();

        // Authors of the parent of a fork can merge, and therefore, they are the only ones who can change the status
        // from FORK_* to MERGE_*.
        if(upStatus.contains("MERGE") && (c.getParent() !=null && !c.getParent().getAuthors().contains(user))) {
            return badRequest(Json.toJson(new TransferResponseStatus("Only the author of the parent " +
                    "contribution can change from FORK to MERGE")));
        }

        List<String> checkStatus = checkContributionRequirementsFields(c, upStatus);
        if(checkStatus != null && !checkStatus.isEmpty()) {
            String lang = c.getLang();
            String msg;
            String statusI;
            if(lang != null)  {
                try {
                    statusI = Messages.get(Lang.forCode(c.getLang()),
                            "appcivist.contribution.status." + upStatus.toLowerCase());
                } catch (Exception e) {
                    statusI = upStatus;
                }
                msg = Messages.get(Lang.forCode(c.getLang()),"appcivist.contribution.change.status",
                        checkStatus, statusI);
            } else {
                try {
                    statusI = Messages.get(
                            "appcivist.contribution.status." + upStatus.toLowerCase());
                } catch (Exception e) {
                    statusI = upStatus;
                }
                msg = Messages.get("appcivist.contribution.change.status", checkStatus, statusI);
            }

            return badRequest(Json.toJson(new TransferResponseStatus(msg)));
        }
        Logger.debug("Updating contribution status. User = "+(user != null ? user.getUserId() : "[no user found]"));
        PeerDocWrapper peerDocWrapper = new PeerDocWrapper(user);
        try {
            Boolean change = peerDocWrapper.changeStatus(c, ContributionStatus.valueOf(status));
            if(change != null && !change) {
                return internalServerError(Json.toJson(new TransferResponseStatus(
                        ResponseStatus.SERVERERROR,
                        "Error publishing peerdoc")));
            }
        } catch (Exception e) {
            TransferResponseStatus response = new TransferResponseStatus();
            response.setResponseStatus(ResponseStatus.SERVERERROR);
            response.setStatusMessage(e.getMessage());
            Logger.error("PEERDOC: A problem occurred while updating PEERDOC status: '"+ e.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String trace = sw.toString();
            Logger.debug("PEERDOC: Exception stack trace:\n"+e.getStackTrace().toString()+"\nPEERDOC: "+e.getMessage()+"\nPEERDOC: "+trace);
            response.setErrorTrace(trace);
            response.setNewResourceURL("");
            return internalServerError(Json.toJson(response));
        }
        c.setStatus(ContributionStatus.valueOf(upStatus));
        Contribution.update(c);
        return ok(Json.toJson(c));
    }

    @ApiOperation(httpMethod = "PUT", response = Campaign.class, produces = "application/json", value = "Update status of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result mergeContribution(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "pid", value = "Parent Contribution ID") Long pid,
            @ApiParam(name = "cid", value = "Fork contribution ID") Long cid) {

        User user = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));
        Contribution parent = Contribution.find.byId(pid);
        Contribution child = Contribution.find.byId(cid);

        Logger.debug("USER " +  user);
        Logger.debug("PARENT " +  parent);
        Logger.debug("CHILD " +  child);
        Logger.debug("PARENT CREATOR " + parent.getCreator().getUsername());
        Logger.debug("PARENT AUTHORS " + parent.getAuthors());


        boolean isAuthor = Contribution.find.where().eq("contributionId", parent.getContributionId())
                .eq("authors.userId", user.getUserId()).findUnique() != null;
        if (!isAuthor) {
            isAuthor = parent.getCreator()!= null && parent.getCreator().getUserId().equals(user.getUserId());
        }
        if(!isAuthor) {

            return badRequest(Json
                    .toJson(new TransferResponseStatus(
                            ResponseStatus.UNAUTHORIZED,
                            "Only authors can merge contributions")));
        }

        if(parent == null || child == null || child.getParent() == null
                || !child.getParent().getContributionId().equals(parent.getContributionId())) {
            return notFound(Json.toJson(new TransferResponseStatus("No contribution found")));
        }
        try {
            Contribution aRet = Contribution.merge(parent, child, user);
            if(aRet == null) {
                TransferResponseStatus response = new TransferResponseStatus();
                response.setResponseStatus(ResponseStatus.SERVERERROR);
                response.setStatusMessage("No ok response from peerdoc");
                return internalServerError(Json.toJson(response));
            }
            return ok(Json.toJson(aRet));
        } catch (Exception e) {
            Logger.error(e.getMessage());
            e.printStackTrace();
            TransferResponseStatus response = new TransferResponseStatus();
            response.setResponseStatus(ResponseStatus.SERVERERROR);
            response.setStatusMessage(e.getMessage());
            return internalServerError(Json.toJson(response));
        }
    }


            /**
             *
             */
    /**
     * POST      /api/public/space/:uuid/contribution
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create an anonymous contribution in a campaign")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionInSpacePublic(@ApiParam(name = "uuid", value = "Universal ID of the target campaign") String uuid) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            ResourceSpace resourceSpace = ResourceSpace.readByUUID(UUID.fromString(uuid));
            Result unauth = checkRsDraftState(resourceSpace, newContribution.getType());
            if (unauth!=null) return unauth;

            ContributionTemplate template = null;
            Contribution c;
            try {
                c = createContribution(newContribution, null, type, template, resourceSpace);
                resourceSpace.addContribution(c);
                resourceSpace.update();
                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInResourceSpace(resourceSpace,c);
                });

            } catch (Exception e) {
                e.printStackTrace();
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create an anonymous contribution within another contribution")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Error creating contribution", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContribution(@ApiParam(name = "uuid", value = "Universal ID of the target contribution") String uuid) {
        //TODO uuid from who? the contribution must be associated with the resource space at least

        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            ContributionTemplate template = null;
            Contribution c;
            Contribution inContribution = Contribution.readByUUID(UUID.fromString(uuid));

            if ((newContribution.getType().equals(ContributionTypes.COMMENT)
                    || newContribution.getType().equals(ContributionTypes.DISCUSSION)) &&
                    inContribution.getStatus().equals(ContributionStatus.DRAFT)) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "contribution.unauthorized.creation",
                        ResourceSpaceTypes.CONTRIBUTION));
                return unauthorized(Json.toJson(responseBody));
            }
            try {
                c = createContribution(newContribution, null, type, template, inContribution.getResourceSpace());
                if (type.equals(ContributionTypes.COMMENT) || type.equals(ContributionTypes.DISCUSSION)) {
                    if (inContribution.getForum().getContributions() == null) {
                        inContribution.getForum().setContributions(new ArrayList<Contribution>());
                    }

                    // Integer forumCommentCount = inContribution.getForumCommentCount();
                    // inContribution.setForumCommentCount(forumCommentCount+1);


                    inContribution.getForum().getContributions().add(c);
                    inContribution.getForum().update();
                    // NEW PROMISE
                    Promise.promise(() -> {
                        ContributionsDelegate.updateCommentCounters(c, "+");
                        return ok();
                    });

                } else {
                    inContribution.getResourceSpace().getContributions().add(c);
                    inContribution.getResourceSpace().update();
                }
                // Signal a notification asynchronously
                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInContribution(c, inContribution);
                });
            } catch (Exception e) {
                Logger.error("Error creating ", e);
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            return ok(Json.toJson(c));
        }
    }


    /**
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create an anonymous contribution in a campaign")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnCampaign(@ApiParam(name = "uuid", value = "Universal ID of the target campaign") String uuid) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            Campaign campaign = Campaign.readByUUID(UUID.fromString(uuid));

            if ((newContribution.getType().equals(ContributionTypes.COMMENT)
                    || newContribution.getType().equals(ContributionTypes.DISCUSSION)) &&
                    campaign.getStatus().equals(CampaignStatus.DRAFT)) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "contribution.unauthorized.creation",
                        ResourceSpaceTypes.CAMPAIGN));
                return unauthorized(Json.toJson(responseBody));
            }

            ContributionTemplate template = null;
            Contribution c;
            try {
                c = createContribution(newContribution, null, type, template, campaign.getResources());

                if (type.equals(ContributionTypes.COMMENT) || type.equals(ContributionTypes.DISCUSSION)) {
                    if (campaign.getForum().getContributions() == null) {
                        campaign.getForum().setContributions(new ArrayList<Contribution>());
                    }
                    campaign.getForum().getContributions().add(c);
                    campaign.getForum().update();
                } else {
                    campaign.getResources().getContributions().add(c);
                    campaign.getResources().update();
                }

                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInResourceSpace(campaign.getResources(),
                            c);
                });

            } catch (Exception e) {
                e.printStackTrace();
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }

            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInCampaign(campaign, c);
            });

            return ok(Json.toJson(c));
        }
    }

    /**
     * POST       /api/group/:uuid/contribution
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create an anonymous contribution in a working group")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnWorkingGroup(@ApiParam(name = "uuid", value = "Universal ID of the target working group") String uuid) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            WorkingGroup wgroup = WorkingGroup.readByUUID(UUID.fromString(uuid));

            if ((newContribution.getType().equals(ContributionTypes.COMMENT)
                    || newContribution.getType().equals(ContributionTypes.DISCUSSION)) &&
                    wgroup.getStatus().equals(WorkingGroupStatus.DRAFT)) {
                TransferResponseStatus responseBody = new TransferResponseStatus();
                responseBody.setStatusMessage(Messages.get(
                        "contribution.unauthorized.creation",
                        ResourceSpaceTypes.WORKING_GROUP));
                return unauthorized(Json.toJson(responseBody));
            }

            ContributionTemplate template = null;
            Contribution c;
            try {
                c = createContribution(newContribution, null, type, template, wgroup.getResources());

                if (type.equals(ContributionTypes.COMMENT) || type.equals(ContributionTypes.DISCUSSION)) {
                    if (wgroup.getForum().getContributions() == null) {
                        wgroup.getForum().setContributions(new ArrayList<Contribution>());
                    }
                    wgroup.getForum().getContributions().add(c);
                    wgroup.getForum().update();
                } else {
                    wgroup.getResources().getContributions().add(c);
                    wgroup.getResources().update();
                }

                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInResourceSpace(wgroup.getResources(),
                            c);
                });

            } catch (Exception e) {
                e.printStackTrace();
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }

            Promise.promise(() -> {
                return NotificationsDelegate.newContributionInAssemblyGroup(wgroup, c);
            });

            return ok(Json.toJson(c));
        }
    }

    /**
     * POST       /api/contribution/:uuid
     *
     * @param uuid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Contribution.class, produces = "application/json", value = "Create anonymous contribution in Assembly")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Contribution Object", value = "Body of Contribution in JSON", required = true, dataType = "models.Contribution", paramType = "body")})
    public static Result createAnonymousContributionOnAssembly(@ApiParam(name = "uuid", value = "Universal ID of the target contribution") String uuid) {
        // 1. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM
                .bindFromRequest();

        if (newContributionForm.hasErrors()) {
            return contributionCreateError(newContributionForm);
        } else {

            Contribution newContribution = newContributionForm.get();
            ContributionTypes type = newContribution.getType();
            if (type == null) {
                type = ContributionTypes.COMMENT;
            }

            ContributionTemplate template = null;

            Assembly assembly = Assembly.readByUUID(UUID.fromString(uuid));
            if ((newContribution.getType().equals(ContributionTypes.COMMENT)
                    || newContribution.getType().equals(ContributionTypes.DISCUSSION)) &&
                    assembly.getStatus().equals(AssemblyStatus.DRAFT)) {
                    TransferResponseStatus responseBody = new TransferResponseStatus();
                    responseBody.setStatusMessage(Messages.get(
                            "contribution.unauthorized.creation",
                            ResourceSpaceTypes.ASSEMBLY));
                    return unauthorized(Json.toJson(responseBody));
            }

            Contribution c;
            try {
                c = createContribution(newContribution, null, type, template, assembly.getResources());
                Promise.promise(() -> {
                    return NotificationsDelegate.newContributionInResourceSpace(assembly.getResources(), c);
                });

            } catch (Exception e) {
                return internalServerError(Json
                        .toJson(new TransferResponseStatus(
                                ResponseStatus.SERVERERROR,
                                "Error when creating Contribution: " + e.toString())));
            }
            return ok(Json.toJson(c));
        }
    }

    /**
     * POST       /api/contribution/language
     *
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = String.class, produces = "application/json", value = "Update all contribution languages")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result updateAllContributionLanguages() {
        List<Contribution> contributionList = Contribution.findAll();
        for (Contribution contribution: contributionList) {
            User author = contribution.getFirstAuthor();
            contribution.setLang(null);
            if (author != null) {
                contribution.setLang(author.getLanguage());
            }
            if(contribution.getLang()==null){
                NonMemberAuthor nonMemberAuthor = contribution.getNonMemberAuthor();
                if (nonMemberAuthor != null && nonMemberAuthor.getLang()!=null) {
                    contribution.setLang(nonMemberAuthor.getLang());
                }else{
                    List<ResourceSpace> resourceSpaces = contribution.getContainingSpaces();
                    if(resourceSpaces!=null && resourceSpaces.size()!=0) {
                        ResourceSpace containerResourceSpace = resourceSpaces.get(0);
                        if (containerResourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
                            Campaign c = containerResourceSpace.getCampaign();
                            c = c==null ? containerResourceSpace.getCampaignForum() : c;
                            if (c!=null)
                            	contribution.setLang(c.getLang());
                            else
                            	Logger.debug("Contribution Language update Failed for Contribution "
                            					+contribution.getContributionId()
                            					+". Campaign associated to container RS "+containerResourceSpace.getResourceSpaceId()+" was null");
                        } else if (containerResourceSpace.getType().equals(ResourceSpaceTypes.WORKING_GROUP)) {
                            WorkingGroup wg = containerResourceSpace.getWorkingGroupResources();
                            wg = wg==null ? containerResourceSpace.getWorkingGroupForum() : wg;
                            if (wg!=null)
                            	contribution.setLang(wg.getLang());
                            else
                            	Logger.debug("Contribution Language update Failed for Contribution "
                            					+contribution.getContributionId()
                            					+". WG associated to container RS "+containerResourceSpace.getResourceSpaceId()+" was null");
                        } else if (containerResourceSpace.getType().equals(ResourceSpaceTypes.ASSEMBLY)) {
                            Assembly a = containerResourceSpace.getAssemblyResources();
                            a = a==null ? containerResourceSpace.getAssemblyForum() : a;
                            if (a!=null)
                            	contribution.setLang(a.getLang());
                            else
                            	Logger.debug("Contribution Language update Failed for Contribution "
                            					+contribution.getContributionId()
                            					+". Assembly associated to container RS "+containerResourceSpace.getResourceSpaceId()+" was null");
                        }
                    }
                    if(contribution.getLang()==null){
                        contribution.setLang(GlobalData.DEFAULT_LANGUAGE);
                    }
                }
            }
            contribution.update();
        }
        return ok(Json.toJson("OK"));
    }

    /**
     * GET       /api/assembly/:aid/campaign/:cid/contribution/:coid/body
     *
     * @param aid
     * @param cid
     * @param coid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the pad body url of a Contribution in a Assembly")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Result findAssemblyContributionPadBody(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "rev", value = "Revision", defaultValue = "0") Long rev,
            @ApiParam(name = "format", value = "String", allowableValues = "text, html", defaultValue = "html") String format) {
        Contribution c = Contribution.read(coid);
        return getPadHTML(c, rev, format);
    }

    /**
     * GET       /api/contribution/:couuid/body
     *
     * @param couuid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = String.class, produces = "application/json", value = "Get the pad body url of a Contribution")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No contributions found", response = TransferResponseStatus.class)})
    public static Result findContributionPadBody(
            @ApiParam(name = "couuid", value = "Contribution UUID") UUID couuid,
            @ApiParam(name = "rev", value = "Revision", defaultValue = "0") Long rev,
            @ApiParam(name = "format", value = "String", allowableValues = "text, html", defaultValue = "html") String format) {
        Contribution c = Contribution.readByUUID(couuid);
        return getPadHTML(c, rev, format);
    }

    public static Result getPadHTML(Contribution c, Long rev, String format) {
        String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);

        String etherpadProductionServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_PRODUCTION_ETHERPAD_SERVER);
        String etherpadProductionApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_PRODUCTION_ETHERPAD_API_KEY);

        String etherpadTestServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_TEST_ETHERPAD_SERVER);
        String etherpadTestApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_TEST_ETHERPAD_API_KEY);

        if (c != null) {
            Long revision = rev !=null && rev != 0 ? rev : c.getPublicRevision();
            Resource pad = c.getExtendedTextPad();
            String padId = pad.getPadId();
            String finalFormat = format != null && format == "text" ? "TEXT":"HTML";
            String padStoredKey = pad.getResourceAuthKey();

            String padUrl = pad.getUrlAsString();
            String[] parts = padUrl.split("/p/");

            if (padStoredKey!=null) {
                etherpadApiKey = padStoredKey;
            }
            Logger.info("Etherpad Server in conf file: "+etherpadServerUrl);

            if (etherpadServerUrl==null) {
                etherpadServerUrl = "http://etherpad.appcivist.org";
            }
            Logger.info("Etherpad Server we will use: "+etherpadServerUrl);
            Logger.info("Reading from etherpad: "+parts);
            if (!etherpadServerUrl.equals(parts[0])) {
                etherpadServerUrl = parts[0];
                if (padStoredKey==null && etherpadServerUrl.equals(etherpadProductionServerUrl)) {
                    etherpadApiKey = etherpadProductionApiKey;
                } else if (padStoredKey==null && etherpadServerUrl.equals(etherpadTestServerUrl)) {
                    etherpadApiKey = etherpadTestApiKey;
                } else {
                    // Try to use the URL as a config path in case a sysadmin wants to used this way
                    parts = etherpadServerUrl.split("://");
                    if (parts!=null && parts.length>1) {
                        String domain = parts[1];
                        String apiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD+"."+domain+".apiKey");
                        if (padStoredKey==null && apiKey!=null) {
                            etherpadApiKey=apiKey;
                        } else if (padStoredKey==null) {
                            Logger.info("The API Key for Etherpad Server ("+domain+") of this PAD is not available in our configs. We are letting the call fail with on of the existing keys");
                        }
                    } else {
                        Logger.info("The etherpad url of this PAD is malformed ("+etherpadServerUrl+"). We are letting the call fail with one of the existing keys");
                    }
                }
            }

            EtherpadWrapper wrapper = new EtherpadWrapper(etherpadServerUrl, etherpadApiKey);
            Map<String,Object> result = new HashMap<>();
            String body ="";
            if (padId != null) {
                if(finalFormat.equals("TEXT")){
                    if(rev == null || rev == 0) {
                        Logger.info("Downloading: "+etherpadServerUrl+"/getText?padID="+padId+"&apikey="+etherpadApiKey);
                        body = wrapper.getText(padId);
                    } else {
                        Logger.info("Downloading: "+etherpadServerUrl+"/getText?padID="+padId+"&apikey="+etherpadApiKey+"&rev="+revision);
                        body = wrapper.getTextRevision(padId,revision);
                    }
                }else if(finalFormat.equals("HTML")){
                    if(rev == null || rev == 0) {
                        Logger.info("Downloading: "+etherpadServerUrl+"/getHTML?padID="+padId+"&apikey="+etherpadApiKey);
                        body = wrapper.getHTML(padId);
                    } else {
                        Logger.info("Downloading: "+etherpadServerUrl+"/getHTML?padID="+padId+"&apikey="+etherpadApiKey+"&rev="+revision);
                        body = wrapper.getHTMLRevision(padId,revision);
                    }
                }
                result.put("text", body);
                result.put("rev", revision);
                result.put("format",format);
                return ok(Json.toJson(result));

            } else {
                return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "No Pad for this Contribution")));
            }
        }
        return notFound(Json.toJson(new TransferResponseStatus(ResponseStatus.NODATA, "Contribution was not found")));
    }

    /**
     * PUT       /api/space/:sid/contribution/comment/reset
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "PUT", response = Contribution.class, produces = "application/json", value = "Update comment counts on contributions", notes="Only for ADMINS")
    @ApiResponses(value = {@ApiResponse(code = INTERNAL_SERVER_ERROR, message = "Status not valid", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result updateContributionCounters (@ApiParam(name = "sid", value = "Resource Space ID") Long sid){
    	List<Contribution> contributions = Contribution.findAllByContainingSpace(sid);

    	Promise.promise( () -> {
	    	for (Contribution c: contributions){
	        		ContributionsDelegate.resetParentCommentCountersToZero(c);
	        		ContributionsDelegate.resetChildrenCommentCountersToZero(c);

	    	}
	    	return true;
    	});

        return ok();
    }

    /**
     * GET       /api/space/:sid/contribution/word/frequency
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = HashMap.class, responseContainer = "List", produces = "application/json",
			value = "List of words in proposals or ideas from a given resource space with its frequency")
	  @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Promise<Result> wordsFrecuency (@ApiParam(name = "sid", value = "Resource Space ID")Long sid) {
    	List<Contribution> contributions   = Contribution.findAllByContainingSpace(sid);
    	Promise<Result> resultPromise = Promise.promise( () -> {
    		List<Long> ids = new ArrayList<Long>();

	    	for (Contribution c: contributions){
	    		Logger.info("Contribution ID: " + c.getContributionId());
	    		ids.add(c.getContributionId());
	    	}

	    	Map<String,Integer> wordFrequency = ContributionsDelegate.wordsWithFrequencies(ids);
	    	return ok(Json.toJson(wordFrequency));
    	});

    	return resultPromise;
    }


    /**
     * GET       /api/space/:sid/contribution/search
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = Contribution.class, responseContainer = "List", produces = "application/json",
			value = "Contribution containing the given word")
	  @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
        @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Promise<Result> searchContributionsByText (@ApiParam(name = "sid", value = "Resource Space ID")Long sid,
    														 @ApiParam(name = "byText", value = "Text to be search in the title or text of contributions")String byText) {

    	List<Contribution> contributions = Contribution.findAllByContainingSpace(sid);

    	Promise<Result> resultPromise = Promise.promise( () -> {
    		List<Long> ids = new ArrayList<Long>();

	    	for (Contribution c: contributions){
	    		Logger.info("Contribution ID: " + c.getContributionId());
	    		ids.add(c.getContributionId());
	    	}

	    	List<Contribution> c = ContributionsDelegate.findContributionsByText(ids, byText);
	    	return ok(Json.toJson(c));
    	});

    	return resultPromise;
    }

    /**
     * GET       /api/space/:sid/words
     *
     * @param sid
     * @return
     */
    @ApiOperation(httpMethod = "GET", response = HashMap.class, responseContainer = "List", produces = "application/json",
            value = "List of words in contributions from a given resource space with its frequency")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No resource space found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    public static Promise<Result> wordsFrecuencyByType (@ApiParam(name = "sid", value = "Resource Space ID")Long sid,
                                                        @ApiParam(name = "type", value = "Type of contributions",
                                                                allowableValues = "PROPOSAL, IDEA, DISCUSSION, PROPOSAL_AND_IDEAS, ALL") String type) {
        if(type==null || type.isEmpty()){
            type="ALL";
        }
        ResourceSpace resourceSpace = ResourceSpace.read(sid);
        if (resourceSpace == null) {
            Promise<Result> resultPromise = Promise.promise(() -> {
                return notFound(Json
                        .toJson(new TransferResponseStatus("No resource space found with id " + sid)));
            });
            return resultPromise;
        }
        List<Contribution> contributions = Contribution.findAllByContainingSpace(resourceSpace.getResourceSpaceId());
        if(type.equals("PROPOSAL")){
            contributions = Contribution.readListByContainingSpaceAndType(resourceSpace.getResourceSpaceId(),ContributionTypes.PROPOSAL);
        } else if(type.equals("IDEA")){
            contributions = Contribution.readListByContainingSpaceAndType(resourceSpace.getResourceSpaceId(),ContributionTypes.IDEA);
        } else if(type.equals("DISCUSSION")){
            contributions = Contribution.findAllByContainingSpaceOrTypes(resourceSpace,ContributionTypes.DISCUSSION,ContributionTypes.COMMENT);
        } else if(type.equals("PROPOSAL_AND_IDEAS")){
            contributions = Contribution.findAllByContainingSpaceOrTypes(resourceSpace,ContributionTypes.PROPOSAL,ContributionTypes.IDEA);
        }else {

        }
        List<Contribution> contributionsFiltered = new ArrayList<Contribution>(contributions);

        Promise<Result> resultPromise = Promise.promise( () -> {
            List<Long> ids = new ArrayList<Long>();

            for (Contribution c: contributionsFiltered){
                Logger.info("Contribution ID: " + c.getContributionId());
                ids.add(c.getContributionId());
            }

            Map<String,Integer> wordFrequency = ContributionsDelegate.wordsWithFrequenciesInContributions(ids);
            return ok(Json.toJson(wordFrequency));
        });

        return resultPromise;

    }

    /**
     * POST               /api/assembly/:aid/campaign/:cid/contribution/:coid/document
     *
     * @param aid
     * @return
     */
    @ApiOperation(httpMethod = "POST", response = Integer.class, produces = "application/json", value = "Adds a document to a proposal")
    @ApiResponses(value = {@ApiResponse(code = 404, message = "No group found", response = TransferResponseStatus.class)})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "SESSION_KEY", value = "User's session authentication key", dataType = "String", paramType = "header")})
    @Dynamic(value = "MemberOfAssembly", meta = SecurityModelConstants.ASSEMBLY_RESOURCE_PATH)
    public static Result createPad(
            @ApiParam(name = "aid", value = "Assembly ID") Long aid,
            @ApiParam(name = "cid", value = "Campaign ID") Long cid,
            @ApiParam(name = "coid", value = "Contribution ID") Long coid,
            @ApiParam(name = "typeDocument", value = "Type of document",
                    allowableValues = "gdoc, etherpad, peerdoc") String typeDocument,
            @ApiParam(name = "contributionTemplateId", value = "Contribution template ID") Long contributionTemplateId,
            @ApiParam(name = "resourceTemplateId", value = "Resource ID") Long resourceTemplateId) {

        try {
            JsonNode body = request().body().asJson();
            Contribution contribution = Contribution.read(coid);
            Campaign campaign = Campaign.read(cid);
            ContributionTemplate template = null;
            URL resourceUrl = null;
            ResourceTypes resourceTypes;
            if(resourceTemplateId != null) {
                Resource resource = Resource.read(resourceTemplateId);
                if (resource == null ||
                        (!resource.getResourceType().equals(ResourceTypes.GDOC)
                        && !resource.getResourceType().equals(ResourceTypes.WEBPAGE))
                        || !resource.getIsTemplate()) {
                    return badRequest(Json.toJson(Json
                            .toJson(new TransferResponseStatus("Resource not exist or is not GDOC or HTML or template"))));
                }
                if (resource.getResourceType().equals(ResourceTypes.GDOC)) {
                    resourceUrl = utils.TextUtils.getExportGdocUrl(resource.getUrlAsString(), "html");
                } else {
                    resourceUrl = resource.getUrl();
                }
            }
            if(contributionTemplateId != null) {
                template = ContributionTemplate.read(contributionTemplateId);
            }

            String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
            String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);

            resourceTypes = ResourceTypes.PAD;

            if (typeDocument.equals("etherpad")) {
                if (body.get("etherpadServerUrl") != null) {
                    etherpadServerUrl = body.get("etherpadServerUrl").asText();
                }
                resourceTypes = ResourceTypes.PAD;
            }

            if (typeDocument.equals("gdoc")) {
                if (body.get("gdocLink") != null) {
                    etherpadServerUrl = body.get("gdocLink").asText();
                }
                resourceTypes = ResourceTypes.GDOC;
            }

            if(typeDocument.equals("peerdoc")) {
                User user = User.findByAuthUserIdentity(PlayAuthenticate
                        .getUser(session()));
                PeerDocWrapper peerDocWrapper = new PeerDocWrapper(user);
                try {
                    Map<String,String> response = peerDocWrapper.createPad(contribution, campaign.getResources().getUuid());
                    response.put("path", response.get("path")+"?user="+peerDocWrapper.encrypt());
                    return ok(Json.toJson(response));
                } catch (Exception e) {
                    TransferResponseStatus response = new TransferResponseStatus();
                    response.setResponseStatus(ResponseStatus.SERVERERROR);
                    response.setStatusMessage(e.getMessage());
                    Logger.error("PEERDOC: A problem occurred while getting PEERDOC document: '"+ e.getMessage());
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String trace = sw.toString();
                    Logger.debug("PEERDOC: Exception stack trace:\n"+e.getStackTrace().toString()+"\nPEERDOC: "+e.getMessage()+"\nPEERDOC: "+trace);
                    response.setErrorTrace(trace);
                    response.setNewResourceURL("");
                    return internalServerError(Json.toJson(response));
                }
            }

            boolean storeKey = false;
            if (body.get("etherpadServerApiKey") != null) {
                etherpadApiKey = body.get("etherpadServerApiKey").asText();
                storeKey = true;
            }
            // save the etherpad
            ContributionsDelegate.createAssociatedPad(etherpadServerUrl,
                    etherpadApiKey,
                    contribution,
                    template,
                    campaign.getResources().getUuid(),
                    resourceTypes, storeKey, resourceUrl);


        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Error creating the pad " + e.getMessage());
            return internalServerError(Json.toJson(errors));
        }
        return ok("ok");

    }

    private static File getPadFile(Contribution contribution, String extendedTextFormat, String format, User user)
            throws IOException, GeneralSecurityException, HashGenerationException {
        String selectFormat;

        if (extendedTextFormat.equals("")) {
            if (format.equals("CSV") || format.equals("JSON")) {
                selectFormat = "DOC";
            } else {
                selectFormat = format;
            }
        } else {
            selectFormat = extendedTextFormat;
        }

        if (contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.PEERDOC)) {
            selectFormat = "html";
        }

        String fileName = "/tmp/" + EXTENDED_PAD_NAME.replace(CONTRIBUTION_ID_PARAM,
                contribution.getContributionId().toString()+ "."+selectFormat.toLowerCase());
        File tempFile = new File(fileName);
        Logger.debug("Saving file to: "+fileName);
        OutputStream out = new FileOutputStream(tempFile);

        String url = contribution.getExtendedTextPad().getUrlAsString();
        Logger.debug("Downloading extended text: "+url);
        if (contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.GDOC)) {
            try {

                url = utils.TextUtils.getExportGdocUrl(url, selectFormat).toString();
            } catch (IndexOutOfBoundsException e) {
                Logger.info("Error in GDOC text pad url " + url, e);
            }
        } else if (contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.PAD)) {
            String padId = contribution.getExtendedTextPad().getPadId();
            // Transform URL from read only to write url
            // From [.../p/r.613e...] TO [.../p/{padId}]
            String[] padUrlParts = url.split("/p/");
            url = padUrlParts[0]+"/p/"+padId;
            url = url + "/export/" + selectFormat.toLowerCase();

            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            try {

                httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(url)).execute().download(out);
                out.close();
                return tempFile;

            } catch (IOException  e) {
                out.close();
                Logger.error("Error downloading extendedPAD ", e);
            }

        } else if (contribution.getExtendedTextPad().getResourceType().equals(ResourceTypes.PEERDOC)) {
            Logger.info("Trying to export peerdoc as html");
            PeerDocWrapper peerDocWrapper = new PeerDocWrapper(user);
            String html = peerDocWrapper.export(contribution.getExtendedTextPad());
            if(html != null) {
                try (Writer w = new OutputStreamWriter(out, "UTF-8")) {
                    w.write(html);
                    return tempFile;
                }
            }
        }

        return null;
    }

    private static File getExportFileJson(List<Contribution> contributions, Boolean list, String format) throws IOException {
        String fileName = "/tmp/contributions."+format;
        Logger.debug("Exporting list of contributions "+ (contributions !=null ? contributions.size() : 0) + " to "+format);
        Logger.debug("Saving list of contributions in "+fileName);
        File tempFile = null;
        if (contributions!=null && contributions.size()>0) {
            tempFile = new File(fileName + ".json");
            FileWriter writer = new FileWriter(tempFile);
            if (list) {
                writer.write(prettyPrintJsonString(Json.toJson(contributions)));
            } else {
                writer.write(prettyPrintJsonString(Json.toJson(contributions.get(0))));
            }

        }
        return tempFile;
    }

    private static LinkedHashMap<String, String> getContributionMapToExport(Contribution contribution) {
        LinkedHashMap<String,String> contributionMap = new LinkedHashMap<>();
        Logger.debug("EXPORT: Exporting metadata...");

        Format formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        contributionMap.put("Creation", formatter.format(contribution.getCreation()));
        contributionMap.put("Contribution Id", contribution.getContributionId()+"");
        contributionMap.put("UUID", contribution.getUuidAsString());
        contributionMap.put("Title",contribution.getTitle());
        contributionMap.put("Brief Summary", contribution.getPlainText());
        contributionMap.put("Budget", contribution.getBudget());
        contributionMap.put("Location", contribution.getLocation()!=null ? contribution.getLocation().getPlaceName() : "");
        contributionMap.put("Type", contribution.getType()!=null ? contribution.getType().name() : "NO_TYPE");
        contributionMap.put("Status",contribution.getStatus() !=null ? contribution.getStatus().name() : "NO_STATUS");
        contributionMap.put("Source", contribution.getSource());
        contributionMap.put("Source Code", contribution.getSourceCode());
        contributionMap.put("Source Url", contribution.getSourceUrl());
        contributionMap.put("Read Only Pad Url", contribution.getReadOnlyPadUrl());

        Logger.debug("EXPORT: Exporting creator...");

        contributionMap.put("Creator/Name", contribution.getCreator()!=null ? contribution.getCreator().getName() : "NO_CREATOR");
        contributionMap.put("Creator/Email", contribution.getCreator()!=null ? contribution.getCreator().getEmail() : "NO_CREATOR");


        Logger.debug("EXPORT: Group author...");
        if (contribution.getWorkingGroupAuthors()!=null && contribution.getWorkingGroupAuthors().size()>0)
            contributionMap.put("Group",contribution.getWorkingGroupAuthors().get(0).getName());
        StringJoiner authors = new StringJoiner(";");
        StringJoiner authorsMail = new StringJoiner(";");
        Logger.debug("EXPORT: Exporting user authors...");
        for (User u: contribution.getAuthors()) {
            authors.add(u.getName());
            authorsMail.add(u.getEmail());
        }
        contributionMap.put("Authors/name", authors.toString());

        contributionMap.put("Authors/email", authorsMail.toString());

        StringJoiner themes = new StringJoiner(";");
        Logger.debug("EXPORT: Exporting themes...");
        for (Theme t: contribution.getOfficialThemes()) {
            themes.add(t.getTitle());
        }
        contributionMap.put("Themes", themes.toString());
        Logger.debug("EXPORT: Exporting keywords...");
        StringJoiner keywords = new StringJoiner(";");
        for (Theme t: contribution.getEmergentThemes()) {
            keywords.add(t.getTitle());
        }
        contributionMap.put("Keywords", keywords.toString());

        Logger.debug("EXPORT: Adding main document link...");
        contributionMap.put("Main document link", contribution.getExtendedTextPad() !=null ? contribution.getExtendedTextPad().getUrlAsString() : "[no extended document]");
        contributionMap.put("Main document type", contribution.getExtendedTextPad() !=null ? contribution.getExtendedTextPad().getResourceType().name() : "[no extended document]");

        Logger.debug("EXPORT: Adding attachment links...");
        StringJoiner attachments = new StringJoiner(";");
        for (Resource t: contribution.getResourceSpace().getResources()) {
            attachments.add(t.getTitle() + " : " + t.getUrlAsString());
        }
        contributionMap.put("Attachments and Media", attachments.toString());

        Logger.debug("EXPORT: Adding contribution feedbacks...");
        Integer totalUp = 0;
        Integer totalDown = 0;
        Integer totalFav = 0;
        Integer totalFlag = 0;
        for(ContributionFeedback feedback : contribution.getContributionFeedbacks()) {
            if (feedback.getUp()) {
                totalUp += 1;
            }
            if (feedback.getDown()) {
                totalDown += 1;
            }
            if (feedback.getFav()) {
                totalFav += 1;
            }
            if (feedback.getFlag()) {
                totalFlag += 1;
            }
        }
        contributionMap.put("Contribution Feedbacks/Up", totalUp.toString());
        contributionMap.put("Contribution Feedbacks/Down", totalDown.toString());
        contributionMap.put("Contribution Feedbacks/Fav", totalFav.toString());
        contributionMap.put("Contribution Feedbacks/Flag", totalFlag.toString());

        Logger.debug("EXPORT: Adding custom fields...");
        contributionMap.put("Additional Information","");
        int cfcount = 0;
        for (CustomFieldValue t: contribution.getCustomFieldValues()) {
            String fieldName = t.getCustomFieldDefinition().getName();
            String fieldValue = t.getValue();
            contributionMap.put( ++cfcount +". "+fieldName, fieldValue);
        }
        return contributionMap;
    }

    private static File getExportFile(Contribution contribution, String includeExtendedText, String extendedTextFormat,
                                      String format) throws Exception {

        /* fields:
        contribution:
         creation, contributionId, uuid, plainText, type, status, source, sourceCode, sourceUrl,
         readOnlyPadUrl
        creator:
            name, email
        authors:
            email(s), name(s)
        themes (OFFICIAL_PREDEFINED):
            names
        keywords (EMERGENTS):
            names
        extended text pad
            url, resourceType
        contribution feedbacks
            up, down, fav, flag
        */

        Logger.debug("Exporting contribution " + (contribution != null ? contribution.getContributionId() : "(null)") + " to " + format);
        File tempFile = null;
        LinkedHashMap<String,String> contributionMap = getContributionMapToExport(contribution);
        String fileName = "/tmp/" + CONTRIBUTION_FILE_NAME.replace(CONTRIBUTION_ID_PARAM,contribution.getContributionId().toString());
        Logger.debug("Preparing to save temporal file => " + fileName);
        switch (format.toUpperCase()) {
            case "CSV":
                tempFile = new File(fileName+".csv");
                CSVPrinter csvFilePrinter;
                CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(contributionMap.keySet()
                        .toArray(new String[contributionMap.keySet().size()]));
                FileWriter fileWriter = new FileWriter(tempFile);
                csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                for(String key: contributionMap.keySet()) {
                    csvFilePrinter.print(contributionMap.get(key));
                }
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
                Logger.debug("EXPORT: File "+fileName+".csv was successfully created");
                break;

            case "PDF":
                tempFile = new File(fileName+".pdf");
                Logger.debug("EXPORT: Saving contribution in "+fileName+".pdf");
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                Document document = new Document();
                PdfWriter.getInstance(document, fileOutputStream);
                document.open();
                PdfPTable table = new PdfPTable(contributionMap.size());
                table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
                table.getDefaultCell().setPadding(5f);
                Logger.debug("EXPORT: Writing data to file "+fileName+".pdf");

                for (Object o : contributionMap.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    Logger.debug("EXPORT: Writing "+pair.getKey().toString()+"");
                    String key = pair.getKey().toString();
                    String value = pair.getValue() != null ? pair.getValue().toString() : "\n";
                    document.add(new Paragraph(key + ": " + value));
                }
                document.close();
                Logger.debug("EXPORT: File "+fileName+".pdf was succesfully created");
                break;

            case "TXT":
                String newLine = System.getProperty("line.separator");
                tempFile = new File(fileName+".txt");
                Logger.debug("EXPORT: Saving contribution in "+fileName+".txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
                for (Object o : contributionMap.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    Logger.debug("EXPORT: Writing "+pair.getKey().toString()+"");
                    String key = pair.getKey().toString();
                    String value = pair.getValue() != null ? pair.getValue().toString() : "\n";
                    writer.write(key + ": " + value);
                    writer.write(newLine);
                }
                writer.close();
                Logger.debug("EXPORT: File "+fileName+".txt was succesfully created");
                break;
            case "RTF":
                tempFile = new File(fileName+".rtf");
                fileOutputStream = new FileOutputStream(tempFile);
                Logger.debug("EXPORT: Saving contribution in "+fileName+".rtf");
                document = new Document();
                RtfWriter2.getInstance(document, fileOutputStream);
                // Create a new Paragraph for the footer
                Paragraph par = new Paragraph("Page ");
                par.setAlignment(Element.ALIGN_RIGHT);
                par.add(new RtfPageNumber());
                RtfHeaderFooter footer = new RtfHeaderFooter(par);
                document.setFooter(footer);
                document.open();
                StringBuilder head = new StringBuilder();
                StringBuilder detail = new StringBuilder();
                Iterator it = contributionMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    it.remove();
                    String key = pair.getKey().toString();
                    String value = pair.getValue() != null ? pair.getValue().toString() : "\n";

                    if(it.hasNext()) {
                        head.append(key).append("\t");
                        detail.append(value).append("\t");
                    }else{
                        head.append(key).append("\n");
                        detail.append(value);
                    }
                }
                String text = head + detail.toString();
                document.add(new Paragraph(text));
                document.close();
                Logger.debug("EXPORT: File "+fileName+".rtf was succesfully created");
                break;
            case "DOC":
                tempFile = new File(fileName+".doc");
                Logger.debug("Saving contribution in "+fileName+".doc");
                XWPFDocument doc = new XWPFDocument();
                FileOutputStream out = new FileOutputStream(tempFile);
                XWPFParagraph paragraph = doc.createParagraph();
                XWPFRun run = paragraph.createRun();
                for (Object o : contributionMap.entrySet()) {
                    Map.Entry pair = (Map.Entry) o;
                    String key = pair.getKey().toString();
                    String value = pair.getValue() != null ? pair.getValue().toString() : "\n";
                    run.setText(key + ": " + value+"\n");
                }
                doc.write(out);
                //Close document
                out.close();
                Logger.debug("EXPORT: File "+fileName+".doc was succesfully created");
                break;
        }
        return tempFile;
    }

    private static String prettyPrintJsonString(JsonNode jsonNode) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(jsonNode.toString(), Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return "Sorry, pretty print didn't work";
        }
    }

    private static Result checkRsDraftState(ResourceSpace rs, ContributionTypes t) {
        if (t!=null  && (t.equals(ContributionTypes.COMMENT) || t.equals(ContributionTypes.DISCUSSION))) {
            Config noCommentsIfDraftConfig = rs.getConfigByKey(GlobalDataConfigKeys.APPCIVIST_DISABLE_COMMENTS_IN_DRAFTS);
            String noCommentsIfDraft = noCommentsIfDraftConfig != null ? noCommentsIfDraftConfig.getValue() : null;
            if (noCommentsIfDraft != null && noCommentsIfDraft.toLowerCase().equals("true")) {
                boolean unauth = false;
                if (rs.getType().equals(ResourceSpaceTypes.ASSEMBLY) &&
                        rs.getAssemblyResources().getStatus().equals(AssemblyStatus.DRAFT)) {
                    unauth = true;
                } else if (rs.getType().equals(ResourceSpaceTypes.CAMPAIGN) &&
                        rs.getCampaign().getStatus().equals(CampaignStatus.DRAFT)) {
                    unauth = true;
                } else if (rs.getType().equals(ResourceSpaceTypes.WORKING_GROUP) &&
                        rs.getWorkingGroupResources().getStatus().equals(WorkingGroupStatus.DRAFT)) {
                    unauth = true;
                } else if (rs.getType().equals(ResourceSpaceTypes.CONTRIBUTION)) {
                    unauth = rs.getContribution() != null
                            ? rs.getContribution().getStatus().equals(ContributionStatus.DRAFT) :
                            rs.getForumContribution() != null
                                    ? rs.getForumContribution().getStatus().equals(ContributionStatus.DRAFT) : false;
                }

                if (unauth) {
                    TransferResponseStatus responseBody = new TransferResponseStatus();
                    responseBody.setStatusMessage(Messages.get(
                            "contribution.unauthorized.creation",
                            rs.getType()));
                    return unauthorized(Json.toJson(responseBody));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static List<String> checkContributionRequirementsFields(Contribution c, String upStatus) {
        String configKey;
        switch (ContributionStatus.valueOf(upStatus)) {
            case PUBLIC_DRAFT:
                configKey = GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_PUBLIC_DRAFT_STATUS_REQ;
                break;
            case PUBLISHED:
                configKey = GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_PUBLIC_STATUS_REQ;
                break;
            case FORKED_PUBLIC_DRAFT:
                configKey = GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_FORKED_PUBLIC_DRAFT_STATUS_REQ;
                break;
            case FORKED_PUBLISHED:
                configKey = GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_FORKED_PUBLISHED_STATUS_REQ;
                break;
            case MERGED:
                configKey = GlobalDataConfigKeys.APPCIVIST_CAMPAIGN_CONTRIBUTION_MERGED_STATUS_REQ;
                break;
            default:
                return null;
        }
        List<String> requirements = new ArrayList<>();

        if(c.getCampaignIds().size() == 0) {
            Logger.info("No campaigns found in this contribution");
            return null;
        }
        Campaign campaing = Campaign.find.byId(c.getCampaignIds().get(0));
        for(Config config: campaing.getConfigs()) {
            if(config.getKey().equals(configKey)) {
                requirements = Arrays.asList(config.getValue().split(","));
            }
        }

        Logger.info("Requirements config: " + requirements);
        List<String> customFields = new ArrayList<>();
        List<String> contributionFields = new ArrayList<>();
        if(!requirements.isEmpty()) {
            for(String requirement: requirements) {
                String output = requirement.substring(0, 1).toUpperCase() + requirement.substring(1);
                try {
                    boolean present = false;
                    switch (requirement) {
                        case "theme":
                            for (Theme theme : c.getThemes()) {
                                if (theme.getType().equals(ThemeTypes.OFFICIAL_PRE_DEFINED)) {
                                    present = true;
                                }
                            }
                            if (!present) {
                                contributionFields.add(requirement);
                            }
                            break;
                        case "keyword":
                            for (Theme theme : c.getThemes()) {
                                if (theme.getType().equals(ThemeTypes.EMERGENT)) {
                                    present = true;
                                }
                            }
                            if (!present) {
                                contributionFields.add(requirement);
                            }
                            break;
                        case "attachment":
                            if (c.getExistingResources() != null && c.getExistingResources().isEmpty()) {
                                contributionFields.add(requirement);
                            }
                            break;
                        case "media":
                            for(Resource resource: c.getExistingResources()) {
                                if (resource.getResourceType().equals(ResourceTypes.AUDIO) ||
                                        resource.getResourceType().equals(ResourceTypes.VIDEO)) {
                                    present = true;
                                }
                            }
                            if(!present) {
                                contributionFields.add(requirement);
                            }
                            break;
                        case "group":
                            if(c.getWorkingGroupAuthors() != null && c.getWorkingGroupAuthors().isEmpty()) {
                                contributionFields.add(requirement);
                            }
                            break;

                        default:
                            Object object = Contribution.class.getMethod("get" + output).invoke(c);
                            if (object == null || (object instanceof String
                                    && ((String) object).trim().isEmpty())) {
                            contributionFields.add(requirement);
                            break;
                        }
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    Logger.info("ERROR " + e.getMessage());
                    customFields.add(requirement);
                }
            }
        } else {
            return null;
        }
        if (customFields.isEmpty() && contributionFields.isEmpty()) {
            return null;
        }
        Logger.info("required custom fields: " + customFields);
        List<String> custom = new ArrayList<>(customFields);
        for(CustomFieldValue customFieldValue: c.getResourceSpace().getCustomFieldValues()) {
            for(String requirement: customFields) {
                if(customFieldValue.getCustomFieldDefinition().getName().equals(requirement)
                        && (customFieldValue.getValue() != null) && (!customFieldValue.getValue().trim().isEmpty())) {
                    custom.remove(requirement);
                    Logger.info(requirement +" field is present");
                }
            }
        }
        custom.addAll(contributionFields);
        Logger.info("Required fields: " + custom.toString());
        return custom;
    }


    private static void processUpDownVotes(ContributionFeedback feedback, Boolean isUpVote, Integer votes,
                                           String name, String phone, String email) {
        if (isUpVote)
            feedback.setUp(true);
        else
            feedback.setDown(true);

        Integer numberOfVotes = Math.abs(votes) - 1;
        Boolean multipleVotes = numberOfVotes > 0;

        // create and save additional feedbacks
        if (multipleVotes) {
            for (int i = 0; i<numberOfVotes; i++) {
                ContributionFeedback contributionFeedback = new ContributionFeedback();
                contributionFeedback.setContribution(feedback.getContribution());
                contributionFeedback.setType(feedback.getType());
                if (isUpVote) {
                    contributionFeedback.setUp(true);
                } else {
                    contributionFeedback.setDown(true);
                }
                contributionFeedback.save();
                HashMap<String,Object> authors = processAuthorsInImport(name, phone, email, feedback.getContribution());
                addAuthorsToFeedback(contributionFeedback,authors);
            }
        }

    }

    private static HashMap<String,Object> processAuthorsInImport(String name, String phone, String email, Contribution c) {
        HashMap<String, Object> authors = new HashMap<>();

        boolean createNewMember = false;

        User existingUserAuthor = null;
        // Try to find first an user by the email
        if (email !=null && !email.equals("")) {
            existingUserAuthor = User.findByEmail(email);
        }

        if (existingUserAuthor == null && name != null && !name.equals("")) {
            List<User> existingUserAuthors = User.findByName(name);
            // If more than one user matches the name
            // criteria, we'll skip the author set up
            if (existingUserAuthors != null && existingUserAuthors.size() == 1) {
                Logger.info("The author was FOUND!");
                authors.put("user",existingUserAuthors.get(0));
            } else {
                List<NonMemberAuthor> nonMemberAuthors = NonMemberAuthor.find.where().eq("name", name).findList();
                if (nonMemberAuthors.isEmpty()) {
                    createNewMember = true;
                } else {
                    Logger.info("Non member author was FOUND!");
                    NonMemberAuthor nma = nonMemberAuthors.get(0);
                    authors.put("nonUser",nma);
                    return authors;
                }
            }
        } else {
            authors.put("user",existingUserAuthor);
            return authors;
        }

        if (createNewMember) {
            Logger.info("Importing => non member author => " + name);
            // Create a non member author
            NonMemberAuthor nma = new NonMemberAuthor();
            nma.setName(name);
            nma.setPhone(phone);
            nma.setEmail(email);
            nma.save();
            nma.refresh();
            authors.put("nonUser",nma);
            return authors;
        }
        return null;
    }

    private static void addAuthorsToFeedback(ContributionFeedback feedback, HashMap<String,Object> authors) {
        if (authors!=null) {
            User userAuthor = (User) authors.get("user");
            if (userAuthor==null) {
                NonMemberAuthor nonMemberAuthor = (NonMemberAuthor) authors.get("nonUser");
                feedback.setNonMemberAuthor(nonMemberAuthor);
            } else {
                feedback.setUserId(userAuthor.getUserId());
            }
            feedback.update();
        }
    }

    @ApiOperation(httpMethod = "PUT", response = TransferResponseStatus.class,  produces = "application/json", value = "PUT update peerdoc permissions " +
            "for all contribtuions")
    @Restrict({@Group(GlobalData.ADMIN_ROLE)})
    public static Result updatePeerDocPermission () {

        List<Contribution> contributionList =  Contribution.getAllWithPeerDoc();
        Integer contributionCount = 0;
        for (Contribution contribution : contributionList) {
            Logger.info("Updating contribution " + contribution.getUuidAsString());
            for(User user: contribution.getAuthors()) {
                contributionCount ++;
                PeerDocWrapper peerDocWrapper = new PeerDocWrapper(user);
                Logger.info("Updating user " + user.getUsername());
                try {
                    peerDocWrapper.updatePeerdocPermissions(contribution);
                } catch (NoSuchPaddingException | InvalidAlgorithmParameterException |
                        HashGenerationException | BadPaddingException | IllegalBlockSizeException |
                        NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Logger.error("Error updating peerdoc permisions " + e.getMessage());
                }
            }
        }

        return ok("Updated " + contributionCount + " contributions of " + contributionList.size());

    }

    private static NonMemberAuthor createSocialIdeationNonMemberAuthor(HashMap<String, String> headerMap) {
        NonMemberAuthor non_member_author = new NonMemberAuthor();
        non_member_author.setName(headerMap.get("SOCIAL_IDEATION_USER_NAME"));
        non_member_author.setSourceUrl(headerMap.get("SOCIAL_IDEATION_USER_SOURCE_URL"));
        non_member_author.setSource(headerMap.get("SOCIAL_IDEATION_SOURCE"));
        return non_member_author;
    }

}




class PaginatedContribution {

    @JsonView(Views.Public.class)
    private int pageSize;

    @JsonView(Views.Public.class)
    private int page;

    @JsonView(Views.Public.class)
    private int total;

    @JsonView(Views.Public.class)
    private List<Contribution> list;

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Contribution> getList() {
        return list;
    }

    public void setList(List<Contribution> list) {
        this.list = list;
    }

}
