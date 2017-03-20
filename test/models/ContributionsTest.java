package models;

import be.objectify.deadbolt.java.DeadboltModule;
import be.objectify.deadbolt.java.cache.HandlerCache;
import ch.qos.logback.core.net.SyslogOutputStream;
import com.amazonaws.util.json.JSONObject;
import org.junit.Test;
import play.Application;
import play.Mode;
import play.api.cache.CacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.FakeApplication;
import play.test.WithApplication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ContributionsTest extends WithApplication {
	
	@Test
    public void testUpdateContribution() {
		Contribution c = Contribution.read(new Long(1));
		System.out.println("Contribution: "+c.getTitle());
		String oldTitle = c.getTitle();
		c.setTitle("[NEWTITLE]"+c.getTitle());
		c.update();
		c.refresh();
		System.out.println("Contribution: "+c.getTitle());
		assertTrue(!c.getTitle().equals(oldTitle));
		c.setTitle(oldTitle);
		c.update();
		c.refresh();
		System.out.println("Contribution: "+c.getTitle());
		assertTrue(c.getTitle().equals(oldTitle));
    }

	private static final String BASE_URL = "http://localhost:9000";

	@Test
	public void testAddThemeToContribution() {
		Contribution c = Contribution.read(new Long(1));
		Theme t = new Theme();
		t.setTitle("THEME1");
		t.setDescription("THEME TEST");
		List<Theme> themes = new ArrayList<Theme>();
		themes.add(t);
		JSONObject obj = null;
		try {
			obj = new JSONObject(makeRequest(
                    BASE_URL, "POST", new JSONObject(themes)));
			//assertTrue(obj.getBoolean("isSuccessfull"));

			JSONObject body = obj.getJSONObject("body");
			System.out.println("OBJ" + body);
//
//			assertEquals(student.getAge(), body.getInt("age"));
//			assertEquals(student.getFirstName(), body.getString("firstName"));
//			assertEquals(student.getLastName(), body.getString("lastName"));
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	public static String makeRequest(String myUrl,
									 String httpMethod, JSONObject parameters) throws Exception {
		URL url = null;
		url = new URL(myUrl);
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "application/json");
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
