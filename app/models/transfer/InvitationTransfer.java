package models.transfer;


public class InvitationTransfer {
	private String email; 
	private Boolean moderator; 
	private Boolean coordinator;
	private Boolean expert;
	private Long targetId;
	private String targetType;
	private String invitationEmail;
	
	public InvitationTransfer() {
		super();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getModerator() {
		return moderator;
	}

	public void setModerator(Boolean moderator) {
		this.moderator = moderator;
	}

	public Boolean getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(Boolean coordinator) {
		this.coordinator = coordinator;
	}

	public Long getTargetId() {
		return targetId;
	}

	public void setTargetId(Long targetId) {
		this.targetId = targetId;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getInvitationEmail() {
		return invitationEmail;
	}

	public void setInvitationEmail(String invitationEmail) {
		this.invitationEmail = invitationEmail;
	}

	public Boolean getExpert() {
		return expert;
	}

	public void setExpert(Boolean expert) {
		this.expert = expert;
	}
}
