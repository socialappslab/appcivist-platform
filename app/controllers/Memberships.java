package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import models.Membership;
import models.transfer.TransferMembership;
import models.User;
import models.WorkingGroup;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.*;
import utils.GlobalData;
import utils.ResponseStatusBean;

import static play.data.Form.form;

public class Memberships extends Controller{

    public static final Form<TransferMembership> MEMBERSHIP_FORM = form(TransferMembership.class);

    /**
     *
     * @param membershipType
     * @return
     */
    public static Result createInvitation(String membershipType) {
        // 1. obtaining the user of the requestor
        User wGroupUser = User.findByAuthUserIdentity(PlayAuthenticate
                .getUser(session()));

        // 2. read the new group data from the body
        // another way of getting the body content => request().body().asJson()
        final Form<TransferMembership> newMembershipForm = MEMBERSHIP_FORM.bindFromRequest();

        if (newMembershipForm.hasErrors()) {
            ResponseStatusBean responseBody = new ResponseStatusBean();
            responseBody.setStatusMessage(Messages.get(
                    GlobalData.MEMBERSHIP_INVITATION_CREATE_MSG_ERROR, newMembershipForm.errorsAsJson()));
            return badRequest(Json.toJson(responseBody));
        } else {

            TransferMembership transferMembership = newMembershipForm.get();

            // Obtaining the workingGroup
            WorkingGroup newWorkingGroup = WorkingGroup.read(transferMembership.getId());

            // Check if the user has the correct role to send invitations for this group
            if (hasRole(newWorkingGroup, wGroupUser)) {
                ResponseStatusBean responseBody = new ResponseStatusBean();

                if(transferMembership.getUserId() != null){
                    //User user = User.findByUserId(transferMembership.getUserId());
                    //String token = Membership.generateVerificationRecord(user);
                }else{

                }

                return ok(Json.toJson(responseBody));

            } else {
                return unauthorized("You don't have the correct rights to do that");
            }
        }
    }

    public static Result createRequest(String membershipType){
        return ok();
    }

    private static Boolean hasRole(WorkingGroup workingGroup, User user){
        Boolean role = false;
        Integer cont = 0;

        if(workingGroup.getMembershipRole() != null && user.getRoles().size() > 0) {
            while (role == false || cont < user.getRoles().size()) {
                if (workingGroup.getMembershipRole().toString().equals(user.getRoles().get(cont).getName())) {
                    role = true;
                }
                cont++;
            }
        }
        return role;
    }

}
