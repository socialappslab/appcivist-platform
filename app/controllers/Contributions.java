package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import models.Contribution;
import models.ContributionCollection;
import models.User;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utils.GlobalData;
import utils.ResponseStatusBean;

import java.util.List;

import static play.data.Form.form;

public class Contributions extends Controller{

    public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);

    /**
     * Return the full list of contributions
     *
     * @return Contribution list
     */
    @Security.Authenticated(Secured.class)
    public static Result findContributions() {
        ContributionCollection contributions = Contribution.findAll();
        return ok(Json.toJson(contributions));
    }

    @Security.Authenticated(Secured.class)
    public static Result findContribution(Long contributionId) {
        Contribution contribution = Contribution.read(contributionId);
        return ok(Json.toJson(contribution));
    }

    @Security.Authenticated(Secured.class)
    public static Result deleteContribution(Long contributionId) {
        Contribution.delete(contributionId);
        return ok();
    }

    @Security.Authenticated(Secured.class)
    public static Result createContribution() {
        // 1. obtaining the user of the requestor
        User contributionCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();

        if (newContributionForm.hasErrors()) {
            ResponseStatusBean responseBody = new ResponseStatusBean();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Contribution newContribution = newContributionForm.get();

            if(newContribution.getLang() == null)
                newContribution.setLang(contributionCreator.getLocale());

            ResponseStatusBean responseBody = new ResponseStatusBean();

            if( Contribution.readByTitle(newContribution.getTitle()) > 0 ){
                Logger.info("Contribution already exists");
            }
            else{
                if (newContribution.getCreator() == null){
                    newContribution.setCreator(contributionCreator);
                }

                Contribution.create(newContribution);
                Logger.info("Creating new contribution");
                Logger.debug("=> " + newContributionForm.toString());

                responseBody.setNewResourceId(newContribution.getContributionId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CONTRIBUTION_CREATE_MSG_SUCCESS,
                        newContribution.getTitle()));
                responseBody.setNewResourceURL(GlobalData.CONTRIBUTION_BASE_PATH+"/"+newContribution.getContributionId());
            }

            return ok(Json.toJson(responseBody));
        }
    }

    @Security.Authenticated(Secured.class)
    public static Result updateContribution(Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();

        if (newContributionForm.hasErrors()) {
            ResponseStatusBean responseBody = new ResponseStatusBean();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Contribution newContribution = newContributionForm.get();

            ResponseStatusBean responseBody = new ResponseStatusBean();

            if( newContribution.readByTitle(newContribution.getTitle()) > 0 ){
                Logger.info("Contribution already exists");
            }
            else {
                newContribution.setContributionId(contributionId);
                newContribution.update();
                Logger.info("Creating new contribution");
                Logger.debug("=> " + newContributionForm.toString());

                responseBody.setNewResourceId(newContribution.getContributionId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CONTRIBUTION_CREATE_MSG_SUCCESS,
                        newContribution.getTitle()/*, roleCreator.getIdentifier()*/));
                responseBody.setNewResourceURL(GlobalData.CONTRIBUTION_BASE_PATH + "/" + newContribution.getContributionId());
            }

            return ok(Json.toJson(responseBody));
        }
    }
}
