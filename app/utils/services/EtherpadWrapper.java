package utils.services;

import java.util.Map;

import play.Play;
import net.gjerull.etherpad.client.EPLiteClient;

@SuppressWarnings({"unchecked","unused"})
public class EtherpadWrapper {

	private String etherpadServerUrl = "";
	private String etherpadApiKey = "";
	private EPLiteClient client;
	
	public static final String PAD_PATH_URL = "/p";
	
	public EtherpadWrapper() {
		this.etherpadServerUrl = Play.application().configuration().getString("appcivist.services.etherpad.default.serverBaseUrl");
		this.etherpadApiKey = Play.application().configuration().getString("appcivist.services.etherpad.default.apiKey");
		this.client = new EPLiteClient(this.etherpadServerUrl, this.etherpadApiKey);
	}

	public EtherpadWrapper(String server, String key) {
		if (server != null && !server.isEmpty() && key != null
				&& !key.isEmpty()) {
			this.etherpadServerUrl = server;
			this.etherpadApiKey = key;
		} else {
			etherpadServerUrl = Play
					.application()
					.configuration()
					.getString(
							"appcivist.services.etherpad.default.serverBaseUrl");
			this.etherpadApiKey = Play.application().configuration()
					.getString("appcivist.services.etherpad.default.apiKey");
		}

		this.client = new EPLiteClient(this.etherpadServerUrl, this.etherpadApiKey);
	}

	/*
	 * Getters and setters
	 */
	public String getEtherpadServerUrl() {
		return etherpadServerUrl;
	}

	public void setEtherpadServerUrl(String etherpadServerUrl) {
		this.etherpadServerUrl = etherpadServerUrl;
	}

	public String getEtherpadApiKey() {
		return etherpadApiKey;
	}

	public void setEtherpadApiKey(String etherpadApiKey) {
		this.etherpadApiKey = etherpadApiKey;
	}

	/*
	 * EtherpadClient instance
	 */
	public EPLiteClient getEtherpadClient() {
		return this.client;
	}
	
	/*
	 * API requests
	 */

	public void createPad(String padId) {
		this.client.createPad(padId);
	}

	public void createPad(String padId, String text) {
		this.client.createPad(padId, text);
	}
	
	public void deletePad(String padId) {
		this.client.deletePad(padId);
	}
		
	public String getReadOnlyId(String padId) {
		return getReadOnlyPadIdFromResponse(this.client.getReadOnlyID(padId)) ;
	}

	public String getHTML(String padId) {
		return getHtmlFromResponse(this.client.getHTML(padId));
	}

	public String getHTMLRevision(String padId, Long rev) {
		return getHtmlFromResponse(this.client.getHTML(padId, rev));
	}

	public String getText(String padId) {
		return getTextFromResponse(this.client.getText(padId));
	}

	public String getTextRevision(String padId, Long rev) {
		return getTextFromResponse(this.client.getText(padId,rev));
	}

	public void setHTML(String padId, String html) {
		this.client.setHTML(padId, html);
	}

	public void setText(String padId, String text) {
		this.client.setText(padId, text);
	}
	
	public String getReadOnlyUrl(String padId) {
		String id = getReadOnlyId(padId);
		return id != null ? this.etherpadServerUrl+PAD_PATH_URL+"/"+id : null;
	}

	public String buildReadOnlyUrl(String readOnlyId) {
		return readOnlyId != null ? this.etherpadServerUrl+PAD_PATH_URL+"/"+readOnlyId : null;
	}

	public String getEditUrl(String padId) {
		return this.etherpadServerUrl+PAD_PATH_URL+"/"+padId;
	}

	public String getReadOnlyEmbedCode(String padId) {
		String id = getReadOnlyId(padId);
		return id != null ? this.etherpadServerUrl+PAD_PATH_URL+"/" + id : null;
	}
	
	/**
	 * Methods to parse the response and get specific data fields
	 * 0 everything ok
	 * 1 wrong parameters
	 * 2 internal error
	 * 3 no such function
	 * 4 no or wrong API Key
	 */
	
	private int getCodeFromResponse(Map<String,Object> response) {
		if (response!=null) {
			Object code = response.get("code");
			if (code==null) {
				return -1;
			} else {
				return (int) code;
			}
		}
		return -1;
	}
	
	private String getMsgFromResponse(Map<String,Object> response) {
		return response !=null ? (String) response.get("msg") : null;
	}
	
	private Map<String,Object> getDataFromResponse(Map<String,Object> response) {
		if(getCodeFromResponse(response)==0) {
			return response !=null ? (Map<String,Object>) response.get("data") : null;
		} else {
			return null;
		}
	}

	private Object getDataFieldFromResponse(Map<String, Object> response, String field) {
		if (getCodeFromResponse(response) == 0) {
			Map<String, Object> data = getDataFromResponse(response);
			if (data != null) {
				return (Map<String, Object>) response.get(field);
			}
		} else {
			Object dataField = response.get(field);
			return dataField;
		}
		
		return null;
	}
	
	private String getHtmlFromResponse(Map<String,Object> response) {
		Object dataField = getDataFieldFromResponse(response, "html"); 
		return dataField != null ? (String) dataField : null;
	}
	
	private String getTextFromResponse(Map<String,Object> response) {
		Object dataField = getDataFieldFromResponse(response, "text");
		return dataField != null ? (String) dataField : null;
	}
	
	private String getGroupIdFromResponse(Map<String,Object> response) {
		Object dataField = getDataFieldFromResponse(response, "groupID");
		return dataField != null ? (String) dataField : null;
	}
	
	private String getSessionIdFromResponse(Map<String,Object> response) {
		Object dataField = getDataFieldFromResponse(response, "sessionID");
		return dataField != null ? (String) dataField : null;
	}

	private String getAuthorIdFromResponse(Map<String,Object> response) {
		Object dataField = getDataFieldFromResponse(response, "authorID");
		return dataField != null ? (String) dataField : null;
	}

	private String getPadIdFromResponse(Map<String,Object> response) {
		Object dataField = getDataFieldFromResponse(response, "padID");
		return dataField != null ? (String) dataField : null;
	}

	private String getReadOnlyPadIdFromResponse(Map<String,Object> response) {
		Object dataField = getDataFieldFromResponse(response, "readOnlyID");
		return dataField != null ? (String) dataField : null;
	}	
}
