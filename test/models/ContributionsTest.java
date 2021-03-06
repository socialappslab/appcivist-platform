package models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import play.Logger;
import play.libs.Json;
import play.test.WithApplication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import delegates.ContributionsDelegate;
import enums.ContributionTypes;

public class ContributionsTest extends WithApplication {

	@Test
	public void testCreateContributionInMemory() {
		running(fakeApplication(inMemoryDatabase("default")), new Runnable() {
			public void run() {
				// TODO We need to find a way to automatically create test data
				// for the inMemory testing database
				User creator = User.findByEmail("appcivistapp@gmail.com");
				String title = "Testing for contribution Model";
				String text = "Testing the module";
				ContributionTypes type = ContributionTypes.COMMENT;
				Contribution contribution = new Contribution(creator, title,
						text, type);

				contribution.save();

				Contribution savedC = Contribution.find.byId(contribution
						.getContributionId());

				assertNotNull(savedC);
				assertEquals(savedC.getTitle(), title);
				assertEquals(savedC.getText(), text);
				assertEquals(savedC.getType(), type);
			}
		});
	}

	@Test
	public void testUpdateContribution() {
		Contribution c = Contribution.read(new Long(1));
		System.out.println("Contribution: " + c.getTitle());
		String oldTitle = c.getTitle();
		c.setTitle("[NEWTITLE]" + c.getTitle());
		c.update();
		c.refresh();
		System.out.println("Contribution: " + c.getTitle());
		assertTrue(!c.getTitle().equals(oldTitle));
		c.setTitle(oldTitle);
		c.update();
		c.refresh();
		System.out.println("Contribution: " + c.getTitle());
		assertTrue(c.getTitle().equals(oldTitle));
	}

	private static final String BASE_URL = "http://localhost:9000";

	@Test
	public void testAddAndDeleteThemeInContribution() {
		Theme t = new Theme();
		t.setTitle("THEME1");
		t.setDescription("THEME TEST");
		List<Theme> themes = new ArrayList<Theme>();
		themes.add(t);
		JsonNode jsonNodeArray = Json.newObject();
		((ObjectNode) jsonNodeArray).put("themes", Json.toJson(themes));
		Logger.info("json+++++" + jsonNodeArray.toString());
		try {
			JsonNode obj = Json
					.parse(makeRequest(
							BASE_URL
									+ "/api/contribution/05ac4be4-9960-4975-a8b7-6de893c384f4/themes",
							"POST", jsonNodeArray));
			String themeId = "";
			if (obj.isArray()) {
				for (JsonNode objNode : obj) {
					String title = objNode.get("title").asText();
					String description = objNode.get("description").asText();
					themeId = objNode.get("themeId").asText();
					Logger.info("jsontitle+++++" + title);
					Logger.info("jsondescription+++++" + description);
					assertTrue(t.getTitle().equals(title));
					assertTrue(t.getDescription().equals(description));
				}
			}

			JsonNode objDelete = Json
					.parse(makeRequest(
							BASE_URL
									+ "/api/contribution/05ac4be4-9960-4975-a8b7-6de893c384f4/themes/"
									+ themeId, "DELETE", null));
			Object themesStr = objDelete.get("themes");
			Logger.info("jsonthemes+++++" + themesStr);
			assertTrue(themesStr == null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String makeRequest(String myUrl, String httpMethod,
			JsonNode parameters) throws Exception {
		URL url = null;
		url = new URL(myUrl);
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty(
				"SESSION_KEY",
				"89284ca8f53d8c4cc2f144e241e19aeab1cdf769-pa.u.exp=1487873601777&pa.p.id=password&pa.u.id=carmen%40example.com");
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

	@Test
	public void testCreateContribution() {
		User u = User.findByUserId(1l);
		Contribution c = Contribution.create(u, "Test Create Contribution",
				"testing", ContributionTypes.DISCUSSION);
		c.update();
		c.refresh();
		System.out.println("Contribution: " + c.getTitle());
		assertTrue(c.getTitle().equals("Test Create Contribution"));
	}

	@Test
	public void testUpdateCommentCounters() {
		System.out.println("Update Comment Counters");
		List<Contribution> contributions = Contribution
				.findAllByContainingSpace(74l);

		for (Contribution c : contributions) {
			ContributionsDelegate.resetParentCommentCountersToZero(c);
			ContributionsDelegate.resetChildrenCommentCountersToZero(c);
		}
	}

}
