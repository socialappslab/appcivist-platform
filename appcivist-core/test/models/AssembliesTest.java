package models;

import java.util.List;

import models.Assembly;

import org.junit.*;

import com.avaje.ebean.Ebean;

import play.Application;
import play.GlobalSettings;
import play.libs.Yaml;
import play.test.*;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class AssembliesTest extends WithApplication {
	
	
//	FakeApplication fakeApp = Helpers.fakeApplication();
//
//	FakeApplication fakeAppWithGlobal = fakeApplication(new GlobalSettings() {
//	  @Override
//	  public void onStart(Application app) {
//	    System.out.println("Starting FakeApplication");
//	  }
//	});
//
//	FakeApplication fakeAppWithMemoryDb = fakeApplication(inMemoryDatabase("test"));
	
	
	
    @Test
    public void testAssemblyIssueCreation() {
    	
    	
//    	Issue i = new Issue();
//    	i.setIssueId(new Long(999));
//    	i.setTitle("Issue Title");
//    	i.setBrief("Issue Brief");
//    	
//    	Assembly a = new Assembly();
//    	a.setAssemblyId(new Long(999));
//    	a.setName("Assembly Name");
//    	a.setDescription("Assembly Description");
//    	a.setCity("Asuncion");
//    	a.setIcon("/assets/images/sfskyline-small.jpg");
//    	a.setUrl("/api/assembly/4");
//    	a.getIssues().add(i);
//    	
//    	a.save();
//    	Assembly a1 = Assembly.read(new Long(999));
//    	assertThat(a1!=null);
//    	assertThat(a1.getIssues().size()>0);
    	
    	List list = (List) Yaml.load("initial-data.yml");
    	Ebean.save(list);      	

    	//a.save();
    	Assembly a2 = Assembly.read(new Long(1));
    	assertThat(a2!=null);
    	assertThat(a2.getIssues().size()>0);
    	
    	
    	
    }

}
