package security;

import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import enums.ManagementTypes;
import enums.MembershipTypes;
import enums.MyRoles;
import enums.ResourceSpaceTypes;
import models.*;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Http.Context;

import java.util.List;

public class SpaceDynamicResourceHandler extends AbstractDynamicResourceHandler {

    public Promise<Boolean> checkPermission(String permissionValue,
            DeadboltHandler deadboltHandler, Context ctx) {
        // TODO Auto-generated method stub
        return super.checkPermission(permissionValue, deadboltHandler, ctx);
    }

    @Override
    public Promise<Boolean> isAllowed(String rule, String resource, DeadboltHandler deadboltHandler, Context context) {
        return deadboltHandler.getSubject(context)
                               .map(subjectOption -> {
                                   final boolean[] allowed = {false};
                                   if (new DeadboltAnalyzer().hasRole(subjectOption, "ADMIN")) {
                                       allowed[0] = true;
                                   } else {
                                       subjectOption.ifPresent(subject -> {
                                           User u = User.findByUserName(subject.getIdentifier());
                                           if (u!=null) u.setSessionLanguage();
                                           String path = context.request().path();
                                           Long spaceId = null;
                                           if (rule.equals("CoordinatorOfSpace") && resource.equals(SecurityModelConstants.SPACE_RESOURCE_PATH)) {
                                               spaceId = MyDynamicResourceHandler.getIdFromPath(path, resource);
                                               ResourceSpace resourceSpace = ResourceSpace.read(spaceId);
                                               Long assemblyId = null;
                                               Long groupId = null;
                                               if(resourceSpace.getType().equals(ResourceSpaceTypes.ASSEMBLY)){
                                                   assemblyId = resourceSpace.getAssemblyResources().getAssemblyId();
                                               } else if(resourceSpace.getType().equals(ResourceSpaceTypes.CAMPAIGN)){
                                                   Campaign campaign = resourceSpace.getCampaign();
                                                   List<Assembly> assemblies = Assembly.findAssemblyFromCampaign(campaign.getCampaignId());
                                                   if(assemblies!=null && assemblies.size()!=0){
                                                       assemblyId=assemblies.get(0).getAssemblyId();
                                                   }else{
                                                       allowed[0] = false;
                                                   }
                                               } else if(resourceSpace.getType().equals(ResourceSpaceTypes.COMPONENT)){
                                                   Component component = resourceSpace.getComponent();
                                                   List<Assembly> assemblies = Assembly.findAssemblyFromComponent(component.getComponentId());
                                                   if(assemblies!=null && assemblies.size()!=0){
                                                       assemblyId=assemblies.get(0).getAssemblyId();
                                                   }else{
                                                       allowed[0] = false;
                                                   }
                                               } else if(resourceSpace.getType().equals(ResourceSpaceTypes.WORKING_GROUP)){
                                                    groupId = resourceSpace.getWorkingGroupResources().getGroupId();
                                               }
                                               if(assemblyId!=null){
                                                   Assembly a = null;
                                                   AssemblyProfile ap = null;
                                                   a = Assembly.read(assemblyId);
                                                   if (a != null) {
                                                       ap = a.getProfile();
                                                   }

                                                   Boolean assemblyNotOpen = true;
                                                   if (ap != null) {
                                                       assemblyNotOpen = !ap.getManagementType().equals(ManagementTypes.OPEN);

                                                   }
                                                   allowed[0] = MembershipAssembly.hasRole(u, a, MyRoles.COORDINATOR);
                                                   if (!assemblyNotOpen) {
                                                       Logger.debug("AUTHORIZATION --> Checking if user is Coordinator");
                                                       allowed[0] = false;
                                                   }
                                               } else if (groupId!=null){
                                                   WorkingGroup wg = WorkingGroup.read(groupId);
                                                   Boolean groupNotOpen = !wg.getProfile().getManagementType().equals(ManagementTypes.OPEN);
                                                   if(wg.getIsTopic()){
                                                       groupNotOpen = false;
                                                   }
                                                   Boolean isCoordinator =  MembershipGroup.hasRole(u, wg, MyRoles.COORDINATOR);
                                                   if (isCoordinator && groupNotOpen) {
                                                       Logger.debug("AUTHORIZATION --> Checking if user is Coordinator");
                                                       allowed[0] = true;
                                                   } else {
                                                       allowed[0] = false;
                                                   }
                                               }else {
                                                   allowed[0] = false;
                                               }
                                           }else{
                                               allowed[0] = false;
                                           }
                                           Logger.debug("--> User authorization for "+resource+" "+spaceId+" is "+allowed[0]);
                                       });
                                   }
                                   return allowed[0];
                               });    
    }
}
