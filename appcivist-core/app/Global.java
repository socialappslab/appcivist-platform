import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import models.Assembly;
import models.User;
import play.*;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

public class Global extends GlobalSettings {
	
	@SuppressWarnings("rawtypes")
	public void onStart(Application app) {
		Logger.info("Application has started");

		Boolean cleanDB = Play.application().configuration()
				.getBoolean("appcivist.db.cleanBeforeStarting");
		// TODO: find a better way for purging the database (instead of
		// dropping/creating all over again
		if (cleanDB && !Assembly.findAll().getAssemblies().isEmpty()) {
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

				Ebean.beginTransaction();
				Logger.info("AppCivist: Dropping DB Tables => "+dropDdl);
				Ebean.execute(Ebean.createCallableSql(dropDdl));
				Ebean.commitTransaction();

				Ebean.beginTransaction();
				Logger.info("AppCivist: Creating DB Tables => "+createDdl);
				Ebean.execute(Ebean.createCallableSql(createDdl));
				Ebean.commitTransaction();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (User.findAll().isEmpty()) {
			Boolean loadTestUsers = Play.application().configuration()
					.getBoolean("appcivist.db.loadTestUsers");
			if (loadTestUsers) {
				Logger.info("AppCivist: Loading Test Users...");
				List list = (List) Yaml.load("initial-data/test-users.yml");
				Ebean.save(list);
			}
		}
		
		if (Assembly.findAll().getAssemblies().isEmpty()) {

			Boolean loadTestOrchestration = Play.application().configuration()
					.getBoolean("appcivist.db.loadTestOrchestration");
			if (loadTestOrchestration) {
				Logger.info("AppCivist: Loading Test Assemblies and services...");
				List list = (List) Yaml
						.load("initial-data/orchestration-example-1.yml");
				Ebean.save(list);
			}
		}
	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}

}