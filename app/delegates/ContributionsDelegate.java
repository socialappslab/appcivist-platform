package delegates;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.avaje.ebean.Ebean;
import enums.ContributionTypes;
import models.*;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;

import org.dozer.DozerBeanMapper;
import play.Play;
import utils.services.EtherpadWrapper;
import enums.ResourceTypes;

public class ContributionsDelegate {

    public static DozerBeanMapper mapper;

    public static final Integer PAGE_SIZE = 50;

    static {
        List<String> mappingFiles = Play.application().configuration().getStringList("appcivist.dozer.mappingFiles");
        mapper = new DozerBeanMapper(mappingFiles);
    }

    /**
     * The find property is an static property that facilitates database query
     * creation
     */
    static Model.Finder<Long, Contribution> finder = new Model.Finder<>(
            Contribution.class);


    /**
     * Find contributions based on the given criteria.
     *
     * @param page      Pagination information. Asumes a page size of 50 elements. Page start index is 1.
     * @param byUuid    Find contributions based on uuid.
     * @param byThemeId Find contributions based on theme_id.
     * @return List of contributions.
     */
    public static List<Contribution> findBy(Integer page, String byUuid, Long byThemeId) {
        ExpressionList<Contribution> q = finder.where();

        if (byUuid != null && !byUuid.equals("")) {
            q.eq("uuid", byUuid);
        }

        if (byThemeId != null) {
            q.eq("resourceSpace.themes.themeId", byThemeId);
        }
        return q.findPagedList(page - 1, PAGE_SIZE).getList();
    }

    /**
     * Find row count based on filtering criteria.
     *
     * @param byUuid
     * @param byThemeId
     * @return
     */
    public static Integer countBy(String byUuid, Long byThemeId) {
        ExpressionList<Contribution> q = finder.where();

        if (byUuid != null && !byUuid.equals("")) {
            q.eq("uuid", byUuid);
        }

        if (byThemeId != null) {
            q.eq("resourceSpace.themes.themeId", byThemeId);
        }
        return q.findRowCount();
    }

    /**
     * Return queries based on a query and a filter
     */
    public static List<Contribution> findContributionsInResourceSpace(Long sid, String query) {
        return query != null && !query.isEmpty() ? Contribution
                .findAllByContainingSpace(sid) : Contribution
                .findAllByContainingSpaceAndQuery(sid, query);
    }

    /**
     * Return queries based on a query and a filter
     */
    public static List<Contribution> findPagedContributionsInResourceSpace(Long sid, String query, Integer page, Integer pageSize) {
        return query != null && !query.isEmpty() ?  Contribution
                .findPagedByContainingSpaceAndQuery(sid, query, page, pageSize) : Contribution
                .findPagedByContainingSpace(sid, page, pageSize);
    }

    public static List<Contribution> findContributionsInResourceSpace(
            ResourceSpace rs, String type, String query) {
        if (type != null && !type.isEmpty()) {
            return query != null && !query.isEmpty() ?
                    Contribution.findAllByContainingSpaceAndTypeAndQuery(rs, type, query) :
                    	Contribution.findAllByContainingSpaceAndType(rs, type);
        } else {
            return query != null && !query.isEmpty() ?
                    findContributionsInResourceSpace(rs.getResourceSpaceId(), query) :
                    rs != null ? rs.getContributions() : null;
        }
    }

    public static List<Contribution> findPagedContributionsInResourceSpace(
            ResourceSpace rs, String type, String query, Integer page, Integer pageSize) {
        if (type != null && !type.isEmpty()) {
            return query != null && !query.isEmpty() ?
                    Contribution.findPagedByContainingSpaceAndTypeAndQuery(rs, type, query, page, pageSize) :
                    Contribution.findPagedByContainingSpaceAndType(rs, type, page, pageSize);
        } else {
            return findPagedContributionsInResourceSpace(rs.getResourceSpaceId(), query, page, pageSize);
        }
    }
    
	public static List<Contribution> findContributionsInResourceSpace(
			ResourceSpace rs, Integer type) {
		return Contribution.findAllByContainingSpaceAndType(rs, type);
	}

    // TODO: Add campaign Template
    public static Resource createAssociatedPad(String ethServerBaseUrl, String ethApiToken, Contribution c, UUID resourceSpaceUUID) throws MalformedURLException {
        EtherpadWrapper eth = new EtherpadWrapper(ethServerBaseUrl, ethApiToken);
        // Create pad and set text
        String padId = c.getUuidAsString();
        String text = c.getText();
        eth.createPad(padId, text);
        String readId = eth.getReadOnlyId(padId);
        String readurl = eth.buildReadOnlyUrl(readId);

        if (readurl != null) {
            createResourceAndUpdateContribution(padId, readId, readurl, resourceSpaceUUID, c);
        }
        return null;
    }

    public static Resource createAssociatedPad(String ethServerBaseUrl, String ethApiToken, Contribution c, ContributionTemplate t, UUID resourceSpaceConfigsUUID) throws MalformedURLException {
        EtherpadWrapper eth = new EtherpadWrapper(ethServerBaseUrl, ethApiToken);
        // Create pad and set text
        String padId = UUID.randomUUID().toString();
        String templateText = t != null ? prepareTemplate(t) : c.getText();

        if (t != null) {
            eth.createPad(padId);
            eth.setHTML(padId, templateText);
        } else {
            eth.createPad(padId, templateText);
        }
        String readId = eth.getReadOnlyId(padId);
        String readurl = eth.buildReadOnlyUrl(readId);

        if (readurl != null) {
            createResourceAndUpdateContribution(padId, readId, readurl, resourceSpaceConfigsUUID, c);
        }
        return null;
    }

    private static void createResourceAndUpdateContribution(String padId,
                                                            String readId, String readurl, UUID resourceSpaceConfigsUUID, Contribution c) throws MalformedURLException {
        Resource r = new Resource(new URL(readurl));
        r.setPadId(padId);
        r.setResourceType(ResourceTypes.PAD);
        r.setReadOnlyPadId(readId);
        r.setResourceSpaceWithServerConfigs(resourceSpaceConfigsUUID);
        c.setExtendedTextPad(r);
    }

    public static String readHTMLofAssociatedPad(String ethServerBaseUrl, String ethApiToken, Contribution c) {
        EtherpadWrapper eth = new EtherpadWrapper(ethServerBaseUrl, ethApiToken);
        // Create pad and set text
        String padId = c.getUuidAsString();
        String html = eth.getHTML(padId);
        return html;
    }

    public static String readTextLOfAssociatedPad(String ethServerBaseUrl, String ethApiToken, Contribution c) {
        EtherpadWrapper eth = new EtherpadWrapper(ethServerBaseUrl, ethApiToken);
        // Create pad and set text
        String padId = c.getUuidAsString();
        String text = eth.getText(padId);
        return text;
    }

    public static String readAssociatedPadReadOnlyId(String ethServerBaseUrl, String ethApiToken, Contribution c) {
        EtherpadWrapper eth = new EtherpadWrapper(ethServerBaseUrl, ethApiToken);
        // Create pad and set text
        String padId = c.getUuidAsString();
        String readOnlyId = eth.getReadOnlyId(padId);
        return readOnlyId;
    }

    private static String prepareTemplate(ContributionTemplate t) {
        String header = "<html><header></header><body>";
        String tBody = "";
        for (ContributionTemplateSection section : t.getTemplateSections()) {
            tBody += "<strong>" + section.getTitle() + " (" + section.getLength() + " words)" + "</strong><br>" + section.getDescription() + "<br><br>";
        }
        return header + tBody + "</body>";
    }

}
