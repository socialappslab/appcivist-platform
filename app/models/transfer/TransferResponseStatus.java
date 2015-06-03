package models.transfer;
import java.io.Serializable;

import enums.ResponseStatus;

public class TransferResponseStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5142156998573460710L;


    private ResponseStatus responseStatus;
	private String statusMessage;
	private String errorTrace;
	private Long newResourceId;
	private String newResourceURL;

	public TransferResponseStatus(ResponseStatus responseStatus,
			String statusMessage) {
		this.responseStatus = responseStatus;
		this.statusMessage = statusMessage;
	}
	
	public TransferResponseStatus(ResponseStatus responseStatus,
			String statusMessage, String errorTrace) {
		this.responseStatus = responseStatus;
		this.statusMessage = statusMessage;
		this.errorTrace = errorTrace;
	}

	public TransferResponseStatus() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the status_message
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * @param status_message the status_message to set
	 */
	public void setStatusMessage(String status_message) {
		this.statusMessage = status_message;
	}

	public ResponseStatus getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(ResponseStatus responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getErrorTrace() {
		return errorTrace;
	}

	public void setErrorTrace(String errorTrace) {
		this.errorTrace = errorTrace;
	}

	public Long getNewResourceId() {
		return newResourceId;
	}

	public void setNewResourceId(Long newResourceId) {
		this.newResourceId = newResourceId;
	}

	public String getNewResourceURL() {
		return newResourceURL;
	}

	public void setNewResourceURL(String newResourceURL) {
		this.newResourceURL = newResourceURL;
	}
	
	
	
}
