package models;

import java.util.List;

import models.Assembly;
import models.services.Service;
import models.services.ServiceDefinition;
import models.services.ServiceOperationDefinition;
import models.services.ServiceParameterDataModel;
import models.services.ServiceParameterDefinition;

import org.junit.*;

import com.avaje.ebean.Ebean;

import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.Json;
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

	public static Long ETHERPAD_SERVICE_ID = new Long(1);
	public static Long LOOMIO_SERVICE_ID = new Long(2); 
	public static Long AGORA_SERVICE_ID = new Long(3); 
	
	public static String PROP_SAMPLE_TITLE = "Garden Proposal";
	public static String PROP_SAMPLE_DESC = "Let's make a Garden!";
	public static String GROUP_SAMPLE_TITLE = "Urban Spaces Group";
	public static String GROUP_SAMPLE_DESC = "Group discussing urban spaces ideas";
	public static String DISC_SAMPLE_TITLE = "Issue: empty lot in telegraph and channing";
	public static String DISC_SAMPLE_DESC = "How might we best use the empty lot?";

    @Test
    public void testAssemblyIssueCreation() { 	
       
    	// 1. Create instance of Issue
    	Issue i = new Issue();
    	i.setIssueId(new Long(999));
    	i.setTitle("Issue Title");
    	i.setBrief("Issue Brief");

    	// 2. Create instance of Assembly
    	Assembly a = new Assembly();
    	a.setAssemblyId(new Long(999));
    	a.setName("Assembly Name");
    	a.setDescription("Assembly Description");
    	a.setCity("Asuncion");
    	a.setIcon("/assets/images/sfskyline-small.jpg");
    	a.setUrl("/api/assembly/4");
    	a.getIssues().add(i);
    	
    	// 3. Save Assembly (and its corresponding issues)
    	a.save();
    	
    	// 4. Read the assembly for db and assert it exists and it has issues
    	Assembly a1 = Assembly.read(new Long(999));
    	assertThat(a1!=null);
    	assertThat(a1.getIssues().size()>0);
    	
    	// 5. Delete created data
    	i.delete();
    	a.delete();
    	
    	// 6. Verify data was deleted
    	a1 = Assembly.read(new Long(999));
    	assertThat(a1==null);
    	assertThat(a1.getIssues().size()==0);
    }
    
    @Test
    public void testYAMLDataLoading() {
    	
//    	VirtualFile appRoot = VirtualFile.open(Play.applicationPath);
//    	Play.javaPath.add(0, appRoot.child("test/sub/packages/data.yml"));
    	
    	System.out.println("PATH: "+Play.application().path());
    	System.out.println("PATH: "+Play.application().configuration());


    	// 1. Load unit testing data
    	
    	List list = (List) Yaml.load("initial-data/unit-test-data.yml");
    	Ebean.save(list);   
    	
    	// 2. Read loaded Service and its corresponding relationships
    	ServiceDefinition sd = ServiceDefinition.read(new Long(999));
    	ServiceOperationDefinition sod = sd.getOperations().get(0); 
    	ServiceParameterDefinition spd = sod.getParameters().get(0);
    	ServiceParameterDataModel spdm = spd.getDataModel().get(0);

    	// 3. Read Service related instances from DB
    	ServiceOperationDefinition sodDB = ServiceOperationDefinition.read(new Long(999)); 
    	ServiceParameterDefinition spdDB = ServiceParameterDefinition.read(new Long(999));
    	ServiceParameterDataModel spdmDB = ServiceParameterDataModel.read(new Long(999));
    	
    	// 4. Compare DB readed objects with Service related instances
    	assertThat(sd!=null);
    	assertThat(sd.getOperations().size()>0);
    	assertThat(sod.getName().equals(sodDB.getName()));
    	assertThat(spd.getName().equals(spdDB.getName()));
    	assertThat(spdm.getDataKey().equals(spdmDB.getDataKey()));

    	// 5. Delete data
    	spdmDB.delete();
    	spdDB.delete();
    	sodDB.delete();
    	sd.delete();
    }
    
    
    /**
     * Implementation of Issue #20
     */
    @Test 
    public void testOrchestrationDemo1(){
    	
    	// Observation: Data was preloaded by the GlobalSettings class of the 
    	// application (see Global.onStart())
    	
//    	orch = new AppCivistOrchestrator() ;
    	Assembly a = new Assembly();
    	a.setName("Urban Spaces Assembly");
    	a.setDescription("An assembly that works on proposals for addressing problems in urban spaces");
    	a.setCity("Paris");
    	
//    	loomio = orch.getServiceInstance(Constants.LOOMIO_INFO) ;
    	Service loomio = Service.read(LOOMIO_SERVICE_ID);
    	System.out.println(Json.toJson(loomio));
    	
//    	agora =  orch.getServiceInstance(Constants.AGORA_INFO) ;
    	Service agora = Service.read(LOOMIO_SERVICE_ID);
    	System.out.println(Json.toJson(agora));
    	
    	String title = DISC_SAMPLE_TITLE;
    	String desc = DISC_SAMPLE_DESC;
    	
    	
    	/* TODO:
    	 * 1. Create a general REST service operation call in the Service class
    	 * 		=> specify the operations, the parameters and build automaticall the REST call 
    	 * 2. Create a method to build the required data as specified in the parameter definition
    	 * 
    	 */
    	
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
