import java.io.IOException;
import java.util.List;

import models.misc.InitialDataConfig;

import org.apache.commons.io.FileUtils;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.Yaml;

import com.avaje.ebean.Ebean;

public class Global extends GlobalSettings {

	public void onStart(Application app) {
		Logger.info("Application has started");

		/**
		 * If the project configuration ask for the database to be clean,
		 * rebuild it (for dev purposes only)
		 * 
		 * TODO: remove from here and create a more secure external rebuild
		 * script instead
		 */
		Boolean cleanDB = Play.application().configuration()
				.getBoolean("appcivist.db.cleanBeforeStarting");
		if (cleanDB) {
			cleanDBAndRebuild(app);
		}

		List<String> dataLoadFiles = Play.application().configuration()
				.getStringList("appcivist.db.initial-data.files");

		loadDataFiles(dataLoadFiles);
	}

	@SuppressWarnings("rawtypes")
	private void loadDataFiles(List<String> dataLoadFiles) {
		if (dataLoadFiles.size() > 0) {
			Logger.info("Loading data using the folloiwing initial data files: ");
			Logger.info("--> " + dataLoadFiles.toString());

			for (String dataFile : dataLoadFiles) {
				InitialDataConfig fileConfig = InitialDataConfig
						.readByFileName(dataFile);

				if (fileConfig == null || !fileConfig.getLoaded()) {
					try {
						Logger.info("---> AppCivist: Loading '" + dataFile
								+ "'...");
						List list = (List) Yaml.load(dataFile);
						
						if(fileConfig!=null && !fileConfig.getLoaded()) {
							Logger.info("---> AppCivist: '" + dataFile
									+ "' was previously loaded, deleting before reload...");
							Ebean.delete(list);
						} else {
							Logger.info("---> AppCivist: '" + dataFile
								+ "' will be loaded to database now...");
							fileConfig = new InitialDataConfig(dataFile, true);	
						}
						Ebean.save(list);
						if(!fileConfig.getLoaded())
							fileConfig.setLoaded(true);
						fileConfig.save();
						Logger.info("---> AppCivist: '" + dataFile
								+ "' loaded successfully!");
					} catch (Exception e) {
						Logger.info("---> AppCivist: A problem occurred while loading '"
								+ dataFile + "'...");
						e.printStackTrace();
					}
				} else {
					Logger.info("---> AppCivist: '" + dataFile
							+ "' was already loaded...");
				}
			}
		}
	}

	private void cleanDBAndRebuild(Application app) {
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
			Logger.info("AppCivist: Dropping DB Tables => " + dropDdl);
			Ebean.execute(Ebean.createCallableSql(dropDdl));
			Ebean.commitTransaction();

			Ebean.beginTransaction();
			Logger.info("AppCivist: Creating DB Tables => " + createDdl);
			Ebean.execute(Ebean.createCallableSql(createDdl));
			Ebean.commitTransaction();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
	}
}