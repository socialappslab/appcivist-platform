package utils.services;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.MalformedURLException;
import java.util.UUID;

import org.junit.Test;

public class EtherpadTest {


	@Test
	public void createPad() throws MalformedURLException {
		EtherpadWrapper eth = new EtherpadWrapper("http://etherpad.littlemacondo.com","779ca795340d4b9582e18236e3613a1a6f1891f2482ba64d9494e421276ce889");
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
		EtherpadWrapper eth = new EtherpadWrapper("http://etherpad.littlemacondo.com","779ca795340d4b9582e18236e3613a1a6f1891f2482ba64d9494e421276ce889");
		String padId = UUID.randomUUID().toString();
		eth.createPad(padId);
		String html = "<html><header></header><body><h1>Section 1</h1><br><p>Description section 1</p>"
				+ "<h1><b>Section 2</b></h1><br><p>Description section 2</p>"
				+ "<h2><b>Section 3</b></h2><br><p>Description section 3</p>"
				+ "<h2><b>Section 4</b></h2><br><p>Description section 4</p></body></html>";
		System.out.println("Pad HTML new Text: "+html);
		eth.setHTML(padId, html);
		String padHtmlText = eth.getHTML(padId);
		eth.deletePad(padId);
		System.out.println("Pad HTML read Text: "+padHtmlText);
		assertThat(padHtmlText, containsString("Section 2"));
	}

}
