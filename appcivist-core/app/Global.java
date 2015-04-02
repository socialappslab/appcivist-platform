import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import models.Assembly;
import play.*;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

public class Global extends GlobalSettings {

	public void onStart(Application app) {
		Logger.info("Application has started");
		if (Assembly.findAll().getAssemblies().isEmpty()) {
			List list = (List) Yaml.load("initial-data.yml");
			Ebean.save(list);
			// List list = (List)
			// Yaml.load("initial-data/bidirectional-one2many-test.yml");
			// Ebean.save(list);
		} else {

			// TODO: make the below code available only in dev mode

			// Delete and Create the Database again
			// Reading the evolution file
			String evolutionContent;
			try {
				evolutionContent = FileUtils.readFileToString(app
						.getWrappedApplication().getFile(
								"conf/evolutions/default/1.sql"));

				// Splitting the String to get Create & Drop DDL
				String[] splittedEvolutionContent = evolutionContent
						.split("# --- !Ups");
				String[] upsDowns = splittedEvolutionContent[1]
						.split("# --- !Downs");
				String createDdl = upsDowns[0];
				String dropDdl = upsDowns[1];

				Ebean.execute(Ebean.createCallableSql(dropDdl));
				Ebean.execute(Ebean.createCallableSql(createDdl));
				
				List list = (List) Yaml.load("initial-data.yml");
				Ebean.save(list);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}

}