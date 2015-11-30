import java.io.IOException;
import java.util.List;

import models.misc.InitialDataConfig;

import org.apache.commons.io.FileUtils;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.libs.Yaml;
import play.mvc.Call;

import com.avaje.ebean.Ebean;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.wordnik.swagger.converter.ModelConverters;
import utils.IgnoreConverter;

import controllers.routes;


import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class Global extends GlobalSettings {

	public void beforeStart(Application app) {
	    Logger.info("Registering custom converter");
	    ModelConverters.addConverter(new IgnoreConverter(), true);
	}
	
	public void onStart(Application app) {
		Logger.info("Application has started");
		initializeData(app);
		initializeAuthenticationResolver();		
	}
	
	private void initializeData(Application app) {
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

		Boolean doNotLoadData = Play.application().configuration()
				.getBoolean("appcivist.db.noInitialData");
		if(doNotLoadData==null || !doNotLoadData) {
			List<String> dataLoadFiles = Play.application().configuration()
					.getStringList("appcivist.db.initial-data.files");
	
			loadDataFiles(dataLoadFiles);
		}
	}

	private void initializeAuthenticationResolver() {
		Logger.info("Initialiazing PlayAuthenticate Resolver");
		PlayAuthenticate.setResolver(new Resolver() {

			public Call login() {
				// Your login page
				return routes.Users.login();
			}

			public Call afterAuth() {
				// The user will be redirected to this page after authentication
				// if no original URL was saved
				return routes.Application.index();
			}

			public Call afterLogout() {
				return routes.Application.index();
			}

			public Call auth(final String provider) {
				// You can provide your own authentication implementation,
				// however the default should be sufficient for most cases
				return routes.AuthenticateLocal
						.authenticate(provider);
			}

			public Call askMerge() {
				return routes.Users.askMerge();
			}

			public Call askLink() {
				return routes.Users.askLink();
			}

			public Call onException(final AuthException e) {
				if (e instanceof AccessDeniedException) {
					return routes.Users
							.oAuthDenied(((AccessDeniedException) e)
									.getProviderKey());
				}

				// more custom problem handling here...
				return super.onException(e);
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private void loadDataFiles(List<String> dataLoadFiles) {
		if (dataLoadFiles.size() > 0) {
			Logger.info("Loading data using the folloiwing initial data files: ");
			Logger.info("--> " + dataLoadFiles.toString());

			for (String dataFile : dataLoadFiles) {
				InitialDataConfig fileConfig = InitialDataConfig
						.readByFileName(dataFile);
				Logger.info("---> AppCivist '" + dataFile + "' loading registry: "+fileConfig);
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
	
	// For CORS
	private class ActionWrapper extends Action.Simple {
		public ActionWrapper(Action<?> action) {
			this.delegate = action;
		}

		@Override
		public Promise<Result> call(Http.Context ctx)
				throws java.lang.Throwable {
			Http.Response response = ctx.response();
			response.setHeader("Access-Control-Allow-Origin", "*");
			Logger.debug("--> Setting CORRS HEADERs for path: "+ctx.request().path());
			Promise<Result> result = this.delegate.call(ctx);
			return result;
		}
	}

	@Override
	public Action<?> onRequest(Http.Request request,
			java.lang.reflect.Method actionMethod) {
		return new ActionWrapper(super.onRequest(request, actionMethod));
	}
	
}