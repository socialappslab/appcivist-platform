package models.transfer;

public class InvitationTransfer {
	private String email; 
	private Boolean moderator; 
	private Boolean coordinator; 
	
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
}
