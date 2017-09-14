package enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ggaona on 17/8/17.
 */
public class UserProfileConfigsTypes {

   public static List< String> otherPreferences = Arrays.asList(
            "notifications.preference.contributed-contributions.auto-subscription",
            "notifications.preference.my-working-group.auto-subscription",
            "notifications.preference.working-groups-contributed.signal.auto-subscription");


   public static final Map<String, String> preferencesNewsletter = new HashMap<String, String>(){
      {
         put("notifications.default.service", "defaultService");
         put("notifications.preference.campaign-newsletters.auto-subscription", "enabled");
         put( "notifications.preference.newsletter.frequency", "frequency");
         put( "notifications.preference.newsletter.service", "identity");

      }
   };

   public static List<String> entities = Arrays.asList(
           "notifications.service.email",
           "notifications.service.email.identity" ,
           "notifications.service.facebook-messenger",
           "notifications.service.facebook-messenger.identity",
           "notifications.service.twitter-messenger",
           "notifications.service.twitter.identity");


   public static String CAMPAIGN_NEWSLETTER_AUTO_SUBSCRIPTION = "notifications.preference.campaign-newsletters.auto-subscription";
}
