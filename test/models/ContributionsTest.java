package models;

import static or.junit.Assert.*;

import org.junit.Test;

import static play.test.*;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;



public class ContributionsTest extends WithApplication {
	@Test
	running(FakeApplication(inMemoryDatabase()),new Runnable(){
		public void run(){
			User creator;

			String title="Testing for contribution Model";
			String text="Testing the module";
			ContributionTypes type=ContributionTypes.COMMENT;
			Contribution contribution = new Contribution(creator,title,text,type);

			contribution.save();

    		Contribution savedC=Contribution.find.byId(contribution.id);

    		assertThat(savedC).isNotNull;
    		/*assertThat(savedC.creator)*/
    		assertThat(savedC.title).isEqualTo(title);
    		assertThat(savedC.text).isEqualTo(text);
    		/*assertThat(savedC.type)*/
		}
	});
}
