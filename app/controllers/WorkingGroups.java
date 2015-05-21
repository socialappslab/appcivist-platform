package controllers;

import models.WorkingGroup;
import models.services.Service;
import models.services.ServiceCollection;
import models.services.ServiceOperation;
import models.services.ServiceOperationCollection;
import play.mvc.*;
import play.libs.Json;

public class WorkingGroups extends Controller{

    /**
     * Return the full list of assemblies
     * @return WorkingGroup list
     */
    @Security.Authenticated(Secured.class)
    public static Result findWorkingGroups() {
        List<WorkingGroup> workingGroups = WorkingGroup.findAll();
        return ok(Json.toJson(workingGroups));
    }
}
