package delegates;

import enums.ResourceTypes;
import models.Resource;
import models.User;
import play.Play;
import utils.GlobalData;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.UUID;

/**
 * Created by javierpf on 03/10/16.
 */
public class ResourcesDelegate {

    public static void deleteUnconfirmedContributionTemplates() {
        Resource.deleteUnconfirmedContributionTemplates(ResourceTypes.CONTRIBUTION_TEMPLATE);
    }

    public static Resource createResource(User campaignCreator, String text, ResourceTypes type) {
        Resource res = new Resource();
        res.setName("Proposal Template");
        res.setCreator(campaignCreator);
        res.setCreation(new Date());
        res.setResourceType(type);
        res.setConfirmed(false);
        UUID uid = UUID.randomUUID();
        res.setPadId(uid.toString());
        String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
        try {
            res.createPad(etherpadServerUrl, etherpadApiKey, text);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("ERROR");
        }
        Resource.create(res);
        return res;
    }

    public static Resource confirmResource(Long rid) {
        Resource res = Resource.read(rid);
        res.setConfirmed(true);
        res.update();
        return res;
    }
}
