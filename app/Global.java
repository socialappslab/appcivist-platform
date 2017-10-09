
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import akka.actor.Cancellable;
import delegates.ResourcesDelegate;
import enums.*;
import models.*;
import models.misc.InitialDataConfig;

import org.apache.commons.io.FileUtils;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.Play;
import play.api.mvc.Session;
import play.libs.Akka;
import play.libs.Yaml;
import play.mvc.Call;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import io.swagger.converter.ModelConverters;
import controllers.routes;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import service.PlayAuthenticateLocal;
import utils.GlobalData;
import utils.LogActions;

public class Global extends GlobalSettings {

	public void beforeStart(Application app) {
	    Logger.info("Registering custom converter");
        final ModelConverters converters = new ModelConverters();
        converters.addClassToSkip("com.avaje.ebean.bean.EntityBeanIntercept");
        converters.addPackageToSkip("com.avaje.ebean.bean");
	}
	
	public void onStart(Application app) {
		Logger.info("Application has started");
		initializeData(app);
		initializeAuthenticationResolver();
		initializeScheduler();
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
				return routes.Users.doLogin();
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

	@SuppressWarnings({ "rawtypes", "unused" })
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

						// update sequences to match the inserted ids
						String sql = "SELECT setval_max('public');";
						SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
						List<SqlRow> rows = sqlQuery.findList();
					} catch (Exception e) {
						Logger.info("---> AppCivist: A problem occurred while loading '"
								+ dataFile + "'...");
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						Logger.debug("Exception: "+e.getStackTrace().toString()+" | "+e.getMessage()+" | "+sw.toString());
					}
				} else {
					Logger.info("---> AppCivist: '" + dataFile
							+ "' was already loaded...");
				}
			}
		}
	}

	private void cleanDBAndRebuild(Application app) {
		List<String> evolutionScripts = new ArrayList<>();
		String evolution;
		Logger.info("Cleaning and rebuilding DB");
		
		try {
			// TODO: iterate on existing files rather than having them fixed
			evolution = FileUtils.readFileToString(app
					.getWrappedApplication().getFile(
							"conf/evolutions/default/1.sql"));
			evolutionScripts.add(0, evolution);

			evolution = FileUtils.readFileToString(app
					.getWrappedApplication().getFile(
							"conf/evolutions/default/2.sql"));
			evolutionScripts.add(1, evolution);

			evolution = FileUtils.readFileToString(app
					.getWrappedApplication().getFile(
							"conf/evolutions/default/3.sql"));
			evolutionScripts.add(2, evolution);
			

			evolution = FileUtils.readFileToString(app
					.getWrappedApplication().getFile(
							"conf/evolutions/default/4.sql"));
			evolutionScripts.add(3, evolution);
			

			evolution = FileUtils.readFileToString(app
					.getWrappedApplication().getFile(
							"conf/evolutions/default/5.sql"));
			evolutionScripts.add(4, evolution);
			
			int number = 1;
			for (String evolutionContent : evolutionScripts) {
				// Splitting the String to get Create & Drop DDL
				Logger.info("Deleting database objects from evolution script "+number++);
				String[] splittedEvolutionContent = evolutionContent
						.split("# --- !Ups");	
				String[] upsDowns = splittedEvolutionContent[1]
						.split("# --- !Downs");
				String createDdl = upsDowns[0];
				String dropDdl = null;
				
				if (upsDowns.length>1) 
					dropDdl = upsDowns[1];

				Ebean.beginTransaction();
				if(dropDdl!=null) {
					Logger.info("AppCivist: Dropping DB Tables => " + dropDdl);
					Ebean.execute(Ebean.createCallableSql(dropDdl));
					Ebean.commitTransaction();
				}
				
				Ebean.beginTransaction();
				Logger.info("AppCivist: Creating DB Tables => " + createDdl);
				Ebean.execute(Ebean.createCallableSql(createDdl));
				Ebean.commitTransaction();
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Cancellable scheduler;
	private Cancellable schedulerNotifications;

	private void initializeScheduler() {
		Logger.info("Initialize Schedulers...");
		int timeDelayFromAppStartToLogFirstLogInMs = 0;
		int timeGapBetweenMemoryLogsInMinutes = 1440; // 1 day
		scheduler = Akka.system().scheduler().schedule(
				Duration.create(timeDelayFromAppStartToLogFirstLogInMs, TimeUnit.MILLISECONDS),
				Duration.create(timeGapBetweenMemoryLogsInMinutes, TimeUnit.MINUTES),
				new Runnable() {
					@Override
					public void run() {
						Logger.info("Cron Job");
						// Call a function (to print JVM stats)
						ResourcesDelegate.deleteUnconfirmedContributionTemplates();
					}
				},
				Akka.system().dispatcher());
		schedulerNotifications = Akka.system().scheduler().schedule(
				Duration.create(timeDelayFromAppStartToLogFirstLogInMs, TimeUnit.MILLISECONDS),
				Duration.create(timeGapBetweenMemoryLogsInMinutes, TimeUnit.MINUTES),
				new Runnable() {
					@Override
					public void run() {
						Logger.info("Daily Newsletter Job");
						//TODO add notification service control to send not sent signals
					}
				},
				Akka.system().dispatcher());
		scheduler = Akka.system().scheduler().schedule(
				Duration.create(timeDelayFromAppStartToLogFirstLogInMs, TimeUnit.MILLISECONDS),
				Duration.create(timeGapBetweenMemoryLogsInMinutes, TimeUnit.MINUTES),
				new Runnable() {
					@Override
					public void run() {
						Logger.info("Ballot creation on Start");
						createBallot();
					}
				},
				Akka.system().dispatcher());
	}

	public void onStop(Application app) {
		Logger.info("Application shutdown...");
		scheduler.cancel();
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
			Promise<Result> result = this.delegate.call(ctx);
			return result;
		}
	}

	@Override
	public Action<?> onRequest(Http.Request request,
			java.lang.reflect.Method actionMethod) {
		String play_session = request.getHeader("SESSION_KEY");
		if (play_session == null || play_session.isEmpty()) {
			Logger.debug("Logging access of anonymous user");
			if (request.path().equals("/api/user/login")) {
				JsonNode body = request.body().asJson();
				JsonNode email_field = body.get("email");
				if (email_field!=null) {
					String email = email_field.asText();	
					LogActions.logActivity(email, request);		
				} else {
					LogActions.logActivity("ANONYMOUS", request);
				}
			} else {
				LogActions.logActivity("ANONYMOUS", request);		
			}
		} 
		return new ActionWrapper(super.onRequest(request, actionMethod));
	}

	private void createBallot() {
		Logger.info("Executing ballot");

		//Get Components of type voting with starting day = today

		Calendar calStart = Calendar.getInstance();
		calStart.setTime(new Date());
		calStart.set(Calendar.HOUR_OF_DAY, 0);
		calStart.set(Calendar.MINUTE, 0);
		calStart.set(Calendar.SECOND, 0);

		Calendar calEnd = Calendar.getInstance();
		calEnd.setTime(new Date());
		calEnd.set(Calendar.HOUR_OF_DAY, 23);
		calEnd.set(Calendar.MINUTE, 59);
		calEnd.set(Calendar.SECOND, 59);

		Logger.info("Searching Component Start day between: "+ calStart.getTime() + " and " +calEnd.getTime() );

		List<Component> components = Component.findVotingByStartingDay(calStart.getTime(), calEnd.getTime());
		Logger.info("Found "+ components.size() + " COMPONENT to create Voting ballots");

		//Find all campaigns related and create ballot
		for (Component component : components) {
			for (ResourceSpace spaces : component.getContainingSpaces()) {
				if (spaces.getType().equals(ResourceSpaceTypes.CAMPAIGN)) {
					Campaign campaign = spaces.getCampaign();
					Logger.info("Creating ballot for campaing:" + campaign.getCampaignId());

					//Campaign related, creating Ballot
					// 6. Create a decision ballot associated with this component and add it to the campaign
					Ballot ballot = new Ballot();
					Date startBallot = component.getStartDate();
					Date endBallot = component.getEndDate();

					// if component has no start date, use now
					startBallot = startBallot != null ? startBallot : Calendar.getInstance().getTime();

					// if component has no end date, use 30 days after startDate
					Calendar c = Calendar.getInstance();
					c.setTime(startBallot);
					c.add(Calendar.DATE, 30);
					endBallot = endBallot != null ? endBallot : c.getTime();

					ballot.setStartsAt(startBallot);
					ballot.setEndsAt(endBallot);
					ballot.setPassword(campaign.getShortname());

					Config publishedProposal = component.getResourceSpace()
							.getConfigByKey(
									GlobalData.CONFIG_CAMPAIGN_INCLUDE_PUBLISHED_PROPOSAL);
					Boolean published = false;
					if (publishedProposal != null && Boolean.valueOf(publishedProposal.getValue())) {
						//Update Campaign status or ballot
						//Create a BallotCandidate for ever Contribution.type = PROPOSAL
						published = true;
					}

					Config ballotEntityType = component.getResourceSpace()
							.getConfigByKey(
									GlobalData.CONFIG_COMPONENT_VOTING_BALLOT_ENTITY_TYPE);

					String entityType = ballotEntityType != null ? ballotEntityType.getValue() : null;
					ballot.setEntityType(entityType != null ? entityType : "PROPOSAL");

					Config votingSystemConfig = component.getResourceSpace()
							.getConfigByKey(
									GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM);

					VotingSystemTypes vtype = votingSystemConfig != null ? VotingSystemTypes
							.valueOf(votingSystemConfig.getValue())
							: VotingSystemTypes.DISTRIBUTED;
					String votesLimit = "5";
					String votesLimitMeaning = "TOKENS";
					if (vtype.equals(VotingSystemTypes.PLURALITY)) {
						votesLimitMeaning = "SELECTIONS"; // user can give vote on up to 'votesLimit' candidates
						Config votingVotesLimitConfig = component.getResourceSpace()
								.getConfigByKey(
										GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_PLURALITY_TYPE);
						votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
					} else if (vtype.equals(VotingSystemTypes.DISTRIBUTED)) {
						votesLimitMeaning = "TOKENS"; // user can distribute up to 'votesLimit' points among candidates
						Config votingVotesLimitConfig = component.getResourceSpace()
								.getConfigByKey(
										GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_DISTRIBUTED_POINTS);
						votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
					} else if (vtype.equals(VotingSystemTypes.RANGE)) {
						votesLimitMeaning = "RANGE"; // user can assign scores to candidates in the range of 'votesLimit' (min-max)
						Config votingVotesLimitConfig = component.getResourceSpace()
								.getConfigByKey(
										GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_RANGE_MAX_SCORE);
						votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
					} else if (vtype.equals(VotingSystemTypes.RANKED)) {
						votesLimitMeaning = "SELECTIONS"; // user can give vote on up to 'votesLimit' candidates
						Config votingVotesLimitConfig = component.getResourceSpace()
								.getConfigByKey(
										GlobalData.CONFIG_COMPONENT_VOTING_SYSTEM_RANKED_NUMBER_PROPOSALS);
						votesLimit = votingVotesLimitConfig != null ? votingVotesLimitConfig.getValue() : votesLimit;
					}
					ballot.setVotesLimit(votesLimit);
					ballot.setVotesLimitMeaning(VotesLimitMeanings.valueOf(votesLimitMeaning));
					ballot.setVotingSystemType(vtype);
					ballot.setRequireRegistration(false);
					ballot.setUserUuidAsSignature(true);
					ballot.setDecisionType("BINDING");
					ballot.setComponent(component);
					ballot.save();
					ballot.refresh();
					campaign.setCurrentBallotAsString(ballot.getUuid().toString());
					campaign.getResources().addBallot(ballot);
					campaign.getResources().update();
					campaign.update();

					//Creating candidates
					createBallotCandidates(campaign, ballot, published);

					//Adding configurations to ballot
					addBallotConfigurations(component.getConfigs(), ballot);
				}
			}
		}
	}

	private void createBallotCandidates(Campaign campaign, Ballot ballot, Boolean publishedProposal) {
		List<Contribution> contributions = campaign.getContributions();
		Boolean hasCandidates = false;
		for(Contribution c : contributions) {
			ContributionTypes ballotEntityType = ContributionTypes.valueOf(ballot.getEntityType());
			if(c.getType()!=null && c.getType().equals(ballotEntityType)){
				//if config campaign.include.all.published.proposals === TRUE,
				// change status of PUBLISHED to INBALLOT
				hasCandidates=true;
				Logger.info("Creating BallotCandidate for Contribution "+ c.getTitle() + "=="+ c.getContributionId() );
				if(publishedProposal){
					c.setStatus(ContributionStatus.INBALLOT);
					c.update();
				}

				//Creating candidate
				BallotCandidate candidate = new BallotCandidate();
				candidate.setBallotId(ballot.getId());
				candidate.setCandidateType(BallotCandidateTypes.CAMPAIGN);
				candidate.setCandidateUuid(c.getUuid());
				candidate.insert();

			}
		}
		//If ballot has not candidates, then set status to draft
		if(!hasCandidates){
			ballot.setStatus(BallotStatus.DRAFT);
		}
	}

	public void addBallotConfigurations(List<Config> configs ,  Ballot ballot){
		// Add Ballot configurations
		for (Config config : configs) {
			BallotConfiguration ballotConfig = new BallotConfiguration();
			ballotConfig.setBallotId(ballot.getId());
			ballotConfig.setKey(config.getKey());
			ballotConfig.setValue(config.getValue());
			ballotConfig.save();
			if (config.getKey().equals("component.voting.ballot.password")) {
				BallotRegistrationField brf = new BallotRegistrationField();
				brf.setBallotId(ballot.getId());
				brf.setDescription("The password used by non-users to vote on proposals through the voting ballot");
				brf.setExpectedValue(config.getValue());
				brf.setName("Ballot Password");
				brf.setPosition(0);
				brf.save();
				ballot.setPassword(config.getValue());
				ballot.update();
			}
		}

	}
}