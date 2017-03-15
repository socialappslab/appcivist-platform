package delegates;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import models.Assembly;
import models.Campaign;
import models.Contribution;
import models.ContributionTemplate;
import models.ContributionTemplateSection;
import models.Resource;
import models.ResourceSpace;
import net.gjerull.etherpad.client.EPLiteException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.dozer.DozerBeanMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;

import play.Logger;
import play.Play;
import utils.TextUtils;
import utils.services.EtherpadWrapper;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import enums.ContributionStatus;
import enums.ContributionTypes;
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

    public static List<Contribution> findContributions(Map<String, Object> conditions, Integer page, Integer pageSize){

        ExpressionList<Contribution> where = null;
        String rawQuery = "select distinct t0.contribution_id, t0.creation, t0.last_update, t0.lang, t0.removal,\n " +
                "  t0.removed, t0.uuid, t0.title, t0.text, t0.type, t0.status, t0.text_index,\n" +
                "  t0.moderation_comment, "/*t0.location_location_id, t0.non_member_author_id, */ + "t0.budget, "/*t0.priority,*/+"\n " +
                "  t0.action_due_date, t0.action_done, t0.action, t0.assessment_summary, " /*t0.extended_text_pad_resource_id,\n"*/ +
                "  t0.source_code, t0.popularity, t0.pinned, t0.comment_count, t0.forum_comment_count, t0.total_comments from contribution t0\n ";
        String sorting = " order by pinned desc nulls last";

        if(conditions != null){
            for(String key : conditions.keySet()){
                Object value = conditions.get(key);
                switch (key){
                    case "group":
                        rawQuery += "join resource_space_contributions rscwg on rscwg.contribution_contribution_id = t0.contribution_id\n" +
                                "  join resource_space rswg on rswg.resource_space_id = rscwg.resource_space_resource_space_id\n " +
                                "  join working_group wg on wg.resources_resource_space_id = rswg.resource_space_id\n ";
                        break;
                    case "containingSpaces":
                        rawQuery += "join resource_space_contributions rsc on rsc.contribution_contribution_id = t0.contribution_id \n " +
                                "join resource_space rs on rs.resource_space_id = rsc.resource_space_resource_space_id\n ";
                        break;
                    case "theme":
                        rawQuery += "join resource_space_theme rst on rst.resource_space_resource_space_id = t0.resource_space_resource_space_id\n ";
                        break;
                    case "sorting":
                        String sortingValue = (String) value;
                        if (sortingValue.equals("popularity") || sortingValue.equals("popularity_desc")) {
                            sorting +=", popularity desc nulls last";
                        } else if (sortingValue.equals("popularity_asc")) {
                            sorting +=", popularity asc nulls last";
                        } else if (sortingValue.equals("random")) {
                            // TODO find a way of producing a a REAL random ordering
                            sorting +=", uuid desc nulls last"; // create the illusion of random ordering
                        } else if (sortingValue.equals("date_asc")) {
                            sorting +=", creation asc nulls last";
                        } else if (sortingValue.equals("date_desc")) {
                            sorting +=", creation desc nulls last";
                        } else if (sortingValue.equals("most_commented") || sortingValue.equals("most_commented_desc")) {
                        	sorting +=", total_comments desc nulls last";
                        } else if (sortingValue.equals("most_commented_asc")) {
                        	sorting +=", total_comments asc nulls last";
                        } else if (sortingValue.equals("most_commented_members") || sortingValue.equals("most_commented_members_desc")) {
                        	sorting +=", comment_count desc nulls last";
                        } else if (sortingValue.equals("most_commented_members_asc")) {
                        	sorting +=", comment_count asc nulls last";
                        } else if (sortingValue.equals("most_commented_public") || sortingValue.equals("most_commented_public_desc")) {
                        	sorting +=", forum_comment_count desc nulls last";
                        } else if (sortingValue.equals("most_commented_public_asc")) {
                        	sorting +=", forum_comment_count asc nulls last";
                        }
                        break;
                }
            }
            rawQuery += sorting;
            RawSql rawSql = RawSqlBuilder.parse(rawQuery).create();
            where = finder.setRawSql(rawSql).where();

            for(String key : conditions.keySet()){
                Object value = conditions.get(key);
                //We look at the keys, some of them have special treatment
                switch (key){
                    case "group":
                        List<Integer> groups = (List)value;
                        Expression previous = null;
                        for(Integer g : groups){
                            Expression e = Expr.eq("wg.group_id", g.longValue());
                            if(previous == null){
                                previous = e;
                            }else{
                                previous = Expr.or(previous, e);
                            }
                        }
                        where.add(previous);
                        break;
                    case "containingSpaces":
                        where.add(Expr.eq("rs.resource_space_id", value));
                        break;
                    case "by_text":
                        Expression expression = Expr.or(Expr.ilike("t0.title", "%" + ((String)value).toLowerCase() + "%"),
                                Expr.ilike("t0.text", "%" + ((String)value).toLowerCase() + "%"));
                        where.add(expression);
                        break;
                    case "theme":
                        List<Integer> themes = (List)value;
                        Expression p = null;
                        for(Integer t : themes){
                            Expression e = Expr.eq("rst.theme_theme_id", t.longValue());
                            if(p == null){
                                p = e;
                            }else{
                                p = Expr.or(p, e);
                            }
                        }
                        where.add(p);
                        break;
                    case "sorting":
                        break;
                    default:
                        if(value instanceof String){
                            where.ilike(key, ("t0."+(String)value).toLowerCase() + "%");
                        }else{
                            where.eq("t0." + key, value);
                        }

                }
            }            
        }
        where.add(Expr.not(Expr.eq("removed",true)));

        List<Contribution> contributions;
        if(page != null && pageSize != null){
            contributions = where.findPagedList(page, pageSize).getList();
        }else{
            contributions = where.findList();
        }
        return contributions;

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
        Boolean isHtml = TextUtils.isHtml(templateText);
        eth.createPad(padId);
        if(isHtml) {
        	try {
        		Logger.info("Trying to create etherpad of proposal with HTML");
				eth.setHTML(padId, templateText);
			} catch (EPLiteException e) {
				try {
					Logger.info("Etherpad of proposal with HTML failed. Trying to fix the HTML body");
					Document doc = Jsoup.parseBodyFragment(templateText);
					String wellFormedHtml = doc.html();
					String wellFormedHtmlUnescaped = StringEscapeUtils.unescapeHtml4(wellFormedHtml); 
					eth.setHTML(padId, wellFormedHtmlUnescaped);
				} catch (EPLiteException e2) {
					Logger.info("Etherpad of proposal with HTML failed. Trying to fix the HTML body manually.");
					String wellFormedHtml = "<!DOCTYPE html><html><head><title></title></head><body>" + templateText + "</body></html>";
					String wellFormedHtmlUnescaped = StringEscapeUtils.unescapeHtml4(wellFormedHtml); 
					eth.setHTML(padId, wellFormedHtmlUnescaped);
				}
				
			}
        	
        } else {
        	eth.setText(padId, templateText);
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

    public static List<Resource> getTemplates(String aid, String cid) {
        List<Resource> templates = new ArrayList<Resource>();
        if (cid != null && cid.compareTo("") != 0) {
            Campaign c = Campaign.read(Long.parseLong(cid));
            List<Resource> resources = c.getResources().getResources();
            for (Resource r: resources) {
                if (r.getResourceType().equals(ResourceTypes.CONTRIBUTION_TEMPLATE)) {
                    templates.add(r);
                }
            }
        }
        if (aid != null && aid.compareTo("") != 0 && templates.isEmpty()) {
            Assembly a = Assembly.read(Long.parseLong(aid));
            List<Resource> resources = a.getResources().getResources();
            for (Resource r: resources) {
                if (r.getResourceType().equals(ResourceTypes.CONTRIBUTION_TEMPLATE)) {
                    templates.add(r);
                }
            }
        }
        if(templates.isEmpty()){
            templates = Resource.findByResourceType(ResourceTypes.CONTRIBUTION_TEMPLATE);
        }
        return templates;
    }

	public static List<Contribution> findPinnedContributionsInSpace(Long sid, ContributionTypes type) {
		return Contribution.findPinnedInSpace(sid, type);
	}

	public static List<Contribution> findPinnedContributionsInResourceSpace(ResourceSpace rs, ContributionTypes type, ContributionStatus status) {
		Long sid;
		if (rs!=null) {
			sid = rs.getResourceSpaceId();
			return Contribution.findPinnedInSpace(sid, type, status);
		} else {
			return null;
		}
			
	}

    public static void updateCommentCounters(Contribution c,  String op){     
        List<ResourceSpace> containingSpaces = c.getContainingSpaces();
        for (ResourceSpace rs : containingSpaces) {
            Contribution parent = Contribution.findByResourceSpaceId(rs.getResourceSpaceId());
            Integer anonymous = 0;
            Integer commentCount = 0;
            if (parent == null){
                parent = Contribution.findByForumResourceSpaceId(rs.getResourceSpaceId());
                anonymous = 1;
            }
            if (parent != null){
            	Integer totalComments = parent.getTotalComments();
                if (anonymous==0){
                    commentCount = parent.getCommentCount();
                    if (op == "+"){
                        parent.setCommentCount(commentCount + 1);
                        parent.setTotalComments(totalComments + 1);
                    } else {
                        parent.setCommentCount(commentCount - 1);
                        parent.setTotalComments(totalComments - 1);
                    }
                } else {
                    commentCount = parent.getForumCommentCount();
                    if (op == "+"){
                        parent.setForumCommentCount(commentCount + 1);
                        parent.setTotalComments(totalComments + 1);
                    } else {
                        parent.setForumCommentCount(commentCount - 1);
                        parent.setTotalComments(totalComments - 1);
                    }
                }
                parent.update();
                updateCommentCounters(parent, op);
            }

        }

    }

}
