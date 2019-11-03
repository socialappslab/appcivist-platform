package enums;

import be.objectify.deadbolt.core.models.Role;

public enum MyRoles implements Role {
	USER, ADMIN, COORDINATOR, MEMBER, FOLLOWER, EXPERT, MODERATOR, JURY;
	
	@Override
	public String getName() {
		 return name();
	}
}
