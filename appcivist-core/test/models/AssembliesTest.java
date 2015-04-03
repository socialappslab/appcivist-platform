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
       	Issue i = new Issue();
    	i.setIssueId(new Long(999));
    	i.setTitle("Issue Title");
    	i.setBrief("Issue Brief");
    	
    	Assembly a = new Assembly();
    	a.setAssemblyId(new Long(999));
    	a.setName("Assembly Name");
    	a.setDescription("Assembly Description");
    	a.setCity("Asuncion");
    	a.setIcon("/assets/images/sfskyline-small.jpg");
    	a.setUrl("/api/assembly/4");
    	a.getIssues().add(i);
    	
    	a.save();
    	Assembly a1 = Assembly.read(new Long(999));
    	assertThat(a1!=null);
    	assertThat(a1.getIssues().size()>0);
    	
    	i.delete();
    	a.delete();
    	
    	a1 = Assembly.read(new Long(999));
    	assertThat(a1==null);
    	assertThat(a1.getIssues().size()==0);
    }
    
    

    @Test
    public void testYAMLDataLoading() {
    	List list = (List) Yaml.load("initial-data/initial-data.yml");
    	Ebean.save(list);   
    	Assembly a2 = Assembly.read(new Long(1));
    	assertThat(a2!=null);
    	assertThat(a2.getIssues().size()>0);
    	
    	
    	
    	
    }
    
    
    /**
     * Implementation of Issue #20
     */
    @Test 
    public void testOrchestrationDemo1(){
//    	orch = new AppCivistOrchestrator() ;
//    	loomio = orch.getServiceInstance(Constants.LOOMIO_INFO) ;
//    	agora =  orch.getServiceInstance(Constants.AGORA_INFO) ;
//
//    	String title = Constants.SAMPLE_TITLE ;
//    	String desc = Constants.SAMPLE_DESC ;
//
//    	loomio.RESTCalls.createDiscussion(title, desc); //this is fake, is being used only for demo purposes
//
//    	//setup complete, main job starts
//
//    	AppCivistDiscussion d = loomio.RESTCalls.getDiscussions().get(0); //gets the first discussion, uses AppCivist data structure
//
//    	AppCivistElectionInstance e = agora.RESTCalls.createAgora(d);
//
//    	for (AppCivistDecisions disc : d.getMotions()){
//    	 e.addVoteOption(disc.motion.title, disc.motion.url);
//    	}
//
//    	//at the end of this, our agora instance should have an election set up inside it.
//
//    	Utils.printInfo(agora.RESTCalls.getElectionInstance(e.getID())); //should print all voting options
//
//    	//end of demo 1 
    }

}
