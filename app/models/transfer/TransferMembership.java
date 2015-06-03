package models.transfer;

/**
 * Transfer model to receive membership creation objects
 * 
 * @author cdparra
 *
 */
public class TransferMembership {

	/**
	 * The id of the group or assembly
	 */
    private Long id;
    private Long userId;
    private String email;
    private String type;
    private String targetCollection;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTargetCollection() {
		return targetCollection;
	}

	public void setTargetCollection(String targetCollection) {
		this.targetCollection = targetCollection;
	}
}
