package enums;

import be.objectify.deadbolt.core.models.Role;

public enum MyRoles implements Role {
	USER, ADMIN, COORDINATOR, MEMBER, FOLLOWER, EXPERT, MODERATOR;
	
	@Override
	public String getName() {
		 return name();
	}
}
