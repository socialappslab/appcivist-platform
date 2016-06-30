# Security Annotations
Annotations to use in Controllers to manage access control

*Restrict to Role Groups*
By using the following annotation on top of a Controller method, the corresponding endpoint is accessible only to 
users that have the list of ROLES specified in the list

    ```java
    @Restrict(@Group(GlobalData.USER_ROLE)) // only those with the USER_ROLE
    @Restrict(@Group(GlobalData.USER_ROLE),@Group(GlobalData.ADMIN_ROLE)) // only those with the USER_ROLE or the ADMIN_ROLE
    @Restrict(@Group(GlobalData.USER_ROLE,GlobalData.ADMIN_ROLE)) // only those with the USER_ROLE AND ADMIN_ROLE
    ```

*Dynamic Access Control*
Refer to access rules that are more specific and are implemented by the DynamicResourceHandlers in the security package. 
You can use them via the following annotation, where value indicates which of the dynamic handlers to use (as they 
are defined in security.MyDynamicResourceHandler) :

    ```java   
    @Dynamic(value = "OnlyMe", meta = SecurityModelConstants.USER_RESOURCE_PATH)
    ```

As of now, the following dynamic handlers are avaiable: 

* *MemberOfAssembly:* the user that sent the request is member of the assembly to which the resource belongs
* *CoordinatorOfGroup:* the user that sent the request is coordinator of the working group to which the resource belongs
* *CoordinatorOfAssembly:* the user that sent the request is member of the assembly to which the resource belongs
* *CanInviteToGroup:* the user has permission to invite others to the group
* *CanInviteToAssembly:* the user has permission to invete other to the assembly
* *OnlyMeAndAdmin:* the request will be successful only if the requested is under the user's resource space or if he/she is ADMIN
* *OnlyMe:* the request will be successful only if the requested is under the user's resource space 