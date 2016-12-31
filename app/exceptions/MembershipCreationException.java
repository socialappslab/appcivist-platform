package exceptions;

import com.fasterxml.jackson.databind.JsonNode;
import enums.ResponseStatus;

import javax.xml.ws.Response;

@SuppressWarnings("serial")
public class MembershipCreationException extends Exception {
	ResponseStatus status;
	JsonNode response;
	String msg;

	public MembershipCreationException() {
		super();
	}
	public MembershipCreationException(ResponseStatus status, String message) {
		super();
		this.status = status;
		this.msg = message;
	}

	public String getMessage(){
		return msg;
	}
	public MembershipCreationException(ResponseStatus status, JsonNode node) {
		super();
		this.status = status;
		this.response = node;
	}
	public MembershipCreationException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public MembershipCreationException(String message) {
		super(message);
	}

	public MembershipCreationException(Throwable throwable) {
		super(throwable);
	}

	public JsonNode getResponse(){
		return response;
	}
	public ResponseStatus getResponseStatus(){
		return status;
	}
}
