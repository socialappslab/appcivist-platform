package models.transfer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import enums.ResponseStatus;

@JsonInclude(Include.NON_EMPTY)
@ApiModel(value="TransferResponseStatus ", description="Response Model used for ERRORS in the API")
public class TransferResponseStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5142156998573460710L;

	@ApiModelProperty(name="responseStatus", dataType="enums.ResponseStatus", allowableValues="BADREQUEST, UNAUTHORIZED, SERVERERROR, NOTAVAILABLE, NODATA, OK", value="Status of the request")
    private ResponseStatus responseStatus;
	@ApiModelProperty(name="responseMessage", dataType="String", value="Message explaining the response")
    private String statusMessage;
	@ApiModelProperty(name="errorTrace", dataType="String", value="Error trace for more details (if there is an error trace)")
	private String errorTrace;
	@ApiModelProperty(name="newResourceId", dataType="Long", value="If the status is OK and a new resource was created, this will indicate the new resource ID")
	private Long newResourceId;
	@ApiModelProperty(name="newResourceUrl", dataType="Long", value="If the status is OK and a new resource was created, this will indicate the new resource URL")
	private String newResourceURL;
	@ApiModelProperty(name="newResourceUuid", dataType="java.util.UUID", value="If the status is OK and a new resource was created, this will indicate the new resource Universal ID")
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
