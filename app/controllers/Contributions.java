package controllers;

import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;

import models.Assembly;
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
import models.transfer.TransferResponseStatus;
import static play.data.Form.form;

public class Contributions extends Controller{

    public static final Form<Contribution> CONTRIBUTION_FORM = form(Contribution.class);

    /**
     * Return the full list of contributions
     *
     * @return Contribution list
     */
    @SubjectPresent
    public static Result findContributions(Long aid) {
        ContributionCollection contributions = Contribution.findAllByAssembly(aid);
        return ok(Json.toJson(contributions));
    }

    @SubjectPresent
    public static Result findContribution(Long aid, Long contributionId) {
        Contribution contribution = Contribution.read(contributionId);
        return ok(Json.toJson(contribution));
    }

    @SubjectPresent
    public static Result deleteContribution(Long aid, Long contributionId) {
        Contribution.delete(contributionId);
        return ok();
    }

    @SubjectPresent
    public static Result createContribution(Long aid) {
        // 1. obtaining the user of the requestor
        User contributionCreator = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session()));

        // 2. read the new role data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();

        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR, newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Contribution newContribution = newContributionForm.get();

            if(newContribution.getLang() == null)
                newContribution.setLang(contributionCreator.getLanguage());
            newContribution.setAssembly(Assembly.read(aid));
            
            TransferResponseStatus responseBody = new TransferResponseStatus();

            if( Contribution.readByTitle(newContribution.getTitle()) > 0 ){
                Logger.info("Contribution already exists");
            }
            else{
                if (newContribution.getAuthor() == null){
                    newContribution.setAuthor(contributionCreator);
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

    @SubjectPresent
    public static Result updateContribution(Long aid, Long contributionId) {
        // 1. read the new contribution data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<Contribution> newContributionForm = CONTRIBUTION_FORM.bindFromRequest();

        if (newContributionForm.hasErrors()) {
            TransferResponseStatus responseBody = new TransferResponseStatus();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.CONTRIBUTION_CREATE_MSG_ERROR,newContributionForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            Contribution newContribution = newContributionForm.get();

            TransferResponseStatus responseBody = new TransferResponseStatus();

                newContribution.setContributionId(contributionId);
                Contribution.update(newContribution);
                Logger.info("Creating new contribution");
                Logger.debug("=> " + newContributionForm.toString());

                responseBody.setNewResourceId(newContribution.getContributionId());
                responseBody.setStatusMessage(Messages.get(
                        GlobalData.CONTRIBUTION_CREATE_MSG_SUCCESS,
                        newContribution.getTitle()/*, roleCreator.getIdentifier()*/));
                responseBody.setNewResourceURL(GlobalData.CONTRIBUTION_BASE_PATH + "/" + newContribution.getContributionId());
        
            return ok(Json.toJson(responseBody));
        }
    }
}
