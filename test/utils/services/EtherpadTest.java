package utils.services;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.running;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.UUID;

import models.Contribution;
import models.User;

import org.junit.BeforeClass;
import org.junit.Test;

import enums.ContributionTypes;
import play.Play;
import play.test.WithApplication;

public class EtherpadTest extends WithApplication {

	public static String APIKEY = "";
	
    @BeforeClass 
    public static void loadEtherpadKey() {
    	running(fakeApplication(inMemoryDatabase("test")), new Runnable() {
			public void run() {
				APIKEY = Play.application().configuration().getString("appcivist.services.etherpad.default.apiKey");
			}
		});
    }
	
	@Test
	public void createPad() throws MalformedURLException {
		EtherpadWrapper eth = new EtherpadWrapper("http://localhost:9001",APIKEY);
		String padId = UUID.randomUUID().toString();
		String text = "This is a test";
		eth.deletePad(padId);
		eth.createPad(padId,text);
		System.out.println("Pad ID: "+padId);
		System.out.println("Pad Text: "+text);
		String readOnlyUrl= eth.getReadOnlyUrl(padId);
		System.out.println("Pad readOnly URL: "+readOnlyUrl);
		String padText = eth.getText(padId);
		System.out.println("Pad read Text: "+padText);
		eth.deletePad(padId);
		assertThat(padText, equalTo(text));		
	}
		
	
	@Test
	public void createPadWithHTML() {
		EtherpadWrapper eth = new EtherpadWrapper("http://localhost:9001",APIKEY);
		String padId = UUID.randomUUID().toString();
		eth.createPad(padId);
		String html = "<html><header></header><body><h1>Section 1</h1><br><p>Description section 1</p>"
				+ "<h1><b>Section 2</b></h1><br><p>Description section 2</p>"
				+ "<h2><b>Section 3</b></h2><br><p>Description section 3</p>"
				+ "<h2><b>Section 4</b></h2><br><p>ö, ä, ü, ß</p></body></html>";
		System.out.println("Pad HTML new Text: "+html);
		try {
			eth.setHTML(padId, html);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String padHtmlText = eth.getHTML(padId);
		//eth.deletePad(padId);
		System.out.println("Pad HTML read Text: "+padHtmlText);
		assertThat(padHtmlText, containsString("Section 2"));
	}

}
