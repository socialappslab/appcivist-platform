import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import delegates.ContributionsDelegate;
import enums.ContributionTypes;
import enums.NotificationEventName;
import models.*;
import models.transfer.NotificationSubscriptionTransfer;
import org.junit.Test;
import play.Logger;
import play.libs.Json;
import play.test.WithApplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertTrue;


public class NotificationTest extends WithApplication {

	private static final String BASE_URL = "http://localhost:9000";

	@Test
	public void testNotificationCampaign() {
		Assembly assembly = new Assembly();
		assembly.setUrl(BASE_URL);
		assembly.setShortname("TEST");
		assembly.setName("ASSEMBLY TESTING "+new Date().toString());
		assembly.setDescription("TESTING PURPOSE");
		JsonNode jsonNodeArray = Json.toJson(assembly);
		try {
			JsonNode obj = Json.parse(makeRequest(
					BASE_URL+"/api/assembly", "POST", jsonNodeArray));
			Logger.info("jsonAssembly+++++"+obj.toString());
			//suscribe user for this assembly
			NotificationSubscriptionTransfer suscribe = new NotificationSubscriptionTransfer();
			suscribe.setAlertEndpoint("carmen@example.com");
			suscribe.setEndpointType("email");
			UUID uuid = UUID.fromString(obj.get("uuid").asText());
			assertTrue(uuid!=null);
			suscribe.setOrigin(uuid);
			suscribe.setEventName(NotificationEventName.NEW_CAMPAIGN);
			JsonNode jsonNodeSuscribe = Json.toJson(suscribe);
			JsonNode objSuscribe = Json.parse(makeRequest(
					BASE_URL+"/api/notification/subscription", "POST", jsonNodeSuscribe));
			//now verify notifications for a new campaign in this assembly
			Logger.info("jsonSuscribe+++++"+objSuscribe);
			String responseStatusSuscribe = objSuscribe.get("responseStatus").asText();
			assertTrue(responseStatusSuscribe.equals("OK"));
			JsonNode objEvents= Json.parse(makeRequest(
					BASE_URL+"/api/notification/subscription", "GET", null));
			Logger.info("jsonEvents+++++"+objEvents.toString());
			if (objEvents.isArray()) {
				for (JsonNode objEvent : objEvents) {
					String origin =  objEvent.get("origin").asText();
					if(origin.equals(uuid.toString())){
						String eventName =  objEvent.get("eventName").asText();
						assertTrue(eventName.equals("NEW_CAMPAIGN"));
					}
				}
			}
			String assemblyId = obj.get("assemblyId").asText();
			Logger.info("jsonAssemblyId+++++"+assemblyId);

			Campaign campaign = new Campaign();
			campaign.setTitle("Notification test "+new Date().toString());
			campaign.setShortname("TEST");
			campaign.setGoal("Testing");
			campaign.setUrl(BASE_URL);
			JsonNode jsonNodeArrayCampaign = Json.toJson(campaign);
			JsonNode objCampaign = Json.parse(makeRequest(
					BASE_URL+"/api/assembly/"+assemblyId+"/campaign", "POST", jsonNodeArrayCampaign));
			Logger.info("jsonCampaign+++++"+objCampaign);
			String campaignId = objCampaign.get("campaignId").asText();
			//if the campaign is created, the campaignId must be not null. Check the email for notifications
			Logger.info("jsonCampaignId+++++"+campaignId);
			assertTrue(campaignId!=null);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String makeRequest(String myUrl,
									 String httpMethod, JsonNode parameters) throws Exception {
		URL url = null;
		url = new URL(myUrl);
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("SESSION_KEY", "89284ca8f53d8c4cc2f144e241e19aeab1cdf769-pa.u.exp=1487873601777&pa.p.id=password&pa.u.id=carmen%40example.com");
		DataOutputStream dos = null;
		conn.setRequestMethod(httpMethod);

		if (Arrays.asList("POST", "PUT").contains(httpMethod)) {
			String params = parameters.toString();
			conn.setDoOutput(true);
			dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(params);
			dos.flush();
			dos.close();
		}

		int respCode = conn.getResponseCode();
		if (respCode != 200 && respCode != 201) {
			String error = inputStreamToString(conn.getErrorStream());
			return error;
		}
		String inputString = inputStreamToString(conn.getInputStream());
		return inputString;
	}
  
	public static String inputStreamToString(InputStream is) throws Exception {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString(); 
	}
}
