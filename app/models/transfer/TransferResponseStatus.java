package models.transfer;
import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResponseStatus;

@JsonInclude(Include.NON_EMPTY)
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
	private UUID newResourceUuid;

	public TransferResponseStatus(ResponseStatus responseStatus,
			String statusMessage) {
		this.responseStatus = responseStatus;
		this.statusMessage = statusMessage;
	}
	

	public TransferResponseStatus(String statusMessage) {
		this.statusMessage = statusMessage;
	}
	
	public TransferResponseStatus(ResponseStatus responseStatus,
			String statusMessage, String errorTrace) {
		this.responseStatus = responseStatus;
		this.statusMessage = statusMessage;
		this.errorTrace = errorTrace;
	}
	
	public TransferResponseStatus(ResponseStatus responseStatus,
			String statusMessage, String errorTrace, Long newId) {
		this.responseStatus = responseStatus;
		this.statusMessage = statusMessage;
		this.errorTrace = errorTrace;
		this.newResourceId=newId;
	}

	public TransferResponseStatus(ResponseStatus responseStatus,
			String statusMessage, String errorTrace, UUID newUuid) {
		this.responseStatus = responseStatus;
		this.statusMessage = statusMessage;
		this.errorTrace = errorTrace;
		this.newResourceUuid=newUuid;
	}
	

	public TransferResponseStatus(ResponseStatus responseStatus,
			String statusMessage, String errorTrace, Long newId, UUID newUuid) {
		this.responseStatus = responseStatus;
		this.statusMessage = statusMessage;
		this.errorTrace = errorTrace;
		this.newResourceId = newId;
		this.newResourceUuid=newUuid;
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

	public UUID getNewResourceUuid() {
		return newResourceUuid;
	}


	public void setNewResourceUuid(UUID newResourceUuid) {
		this.newResourceUuid = newResourceUuid;
	}


	public static TransferResponseStatus badMessage(String msg, String error) {
		return new TransferResponseStatus(ResponseStatus.BADREQUEST, msg, error);
	}
	
	public static TransferResponseStatus noDataMessage(String msg, String error) {
		return new TransferResponseStatus(ResponseStatus.NODATA, msg, error);
	}
	
	public static TransferResponseStatus notAvailableMessage(String msg, String error) {
		return new TransferResponseStatus(ResponseStatus.NOTAVAILABLE, msg, error);
	}
	
	public static TransferResponseStatus okMessage(String msg, String error) {
		return new TransferResponseStatus(ResponseStatus.OK, msg, error);
	}

	public static TransferResponseStatus okMessage(String msg, String error, Long resourceId) {
		return new TransferResponseStatus(ResponseStatus.OK, msg, error, resourceId);
	}

	public static TransferResponseStatus errorMessage(String msg, String error) {
		return new TransferResponseStatus(ResponseStatus.SERVERERROR, msg, error);
	}
	
	public static TransferResponseStatus unauthorizedMessage(String msg, String error) {
		return new TransferResponseStatus(ResponseStatus.UNAUTHORIZED, msg, error);
	}




	
}
