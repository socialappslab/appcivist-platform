import java.util.List;

import models.Assembly;
import play.*;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

public class Global extends GlobalSettings {

    public void onStart(Application app) {
        Logger.info("Application has started");
        if (Assembly.findAll().getAssemblies().isEmpty()) {
        	Ebean.save((List) Yaml.load("initial-data.yml"));
        }
        
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }

}