package delegates;

import enums.ResourceTypes;
import models.Resource;
import models.User;
import net.gjerull.etherpad.client.EPLiteException;
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

    public static Resource createResource(User creator, String text, ResourceTypes type) {
        if(text == null || "".equals(text)){
            text = "<html></html>";
        }
        Resource res = new Resource();
//        if (ResourceTypes.CONTRIBUTION_TEMPLATE.equals(type)) {
//            res.setName("Proposal Template");
//        } else {
//            res.setName("Contribution Proposal");
//        }
        res.setCreator(creator);
        res.setCreation(new Date());
        res.setResourceType(type);
        res.setConfirmed(false);
        UUID uid = UUID.randomUUID();
        res.setPadId(uid.toString());
        String etherpadServerUrl = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_SERVER);
        String etherpadApiKey = Play.application().configuration().getString(GlobalData.CONFIG_APPCIVIST_ETHERPAD_API_KEY);
        try {
            res.createReadablePad(etherpadServerUrl, etherpadApiKey, text);
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException");
            return null;
        } catch (EPLiteException e2) {
            System.out.println("EPLiteException");
            e2.printStackTrace();
            return null;
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
