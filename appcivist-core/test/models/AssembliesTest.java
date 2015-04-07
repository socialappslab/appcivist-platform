package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Assembly;
import models.services.Service;
import models.services.ServiceAuthentication;
import models.services.ServiceDefinition;
import models.services.ServiceOperation;
import models.services.ServiceOperationDefinition;
import models.services.ServiceParameter;
import models.services.ServiceParameterDataModel;
import models.services.ServiceParameterDefinition;

import org.junit.*;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.Json;
import play.libs.Yaml;
import play.test.*;
import play.libs.ws.*;
import play.libs.F.Function;
import play.libs.F.Promise;
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
	public static Long LOOMIO_CREATE_DISCUSSION_OP_ID = new Long(9);
	public static String LOOMIO_GROUP_ID = "2";
	
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
    	
    	// STEP 1: create a new appcivist orchestration, i.e., an Assembly
    	// e.g., orch = new AppCivistOrchestrator() ;
    	Assembly orch = new Assembly();
    	orch.setName("Urban Spaces Assembly");
    	orch.setDescription("An assembly that works on proposals for addressing problems in urban spaces");
    	orch.setCity("Paris");
    	System.out.println("DEMO v1 > #1 > Creating Assembly:'"+orch.getName()+"'");
    	
    	// STEP 2: Add service instances to the assembly: Loomio and Agora
    	System.out.println("DEMO v1 > #2 > Add connected services to the Assembly");
    	
    	// e.g., loomio = orch.getServiceInstance(Constants.LOOMIO_INFO) ;
    	Service loomio = Service.read(LOOMIO_SERVICE_ID);
    	System.out.println("DEMO v1 > ##2.1 > Reading and adding service 'Loomio'"+Json.toJson(loomio));
    	orch.addConnectedService(loomio);
    	
    	// e.g., agora =  orch.getServiceInstance(Constants.AGORA_INFO) ;
    	Service agora = Service.read(LOOMIO_SERVICE_ID);
    	System.out.println("DEMO v1 > ##2.1 > Reading and adding Service 'Agora'"+Json.toJson(agora));
    	orch.addConnectedService(agora);
    	
    	// TODO: Rethink operations adding to the assembly
    	// TODO: Link operationDefinitions to its potential matches in Assembly
    	// STEP 3: create a discussion in Loomio
    	// e.g., loomio.RESTCalls.createDiscussion(title, desc); //this is fake, is being used only for demo purposes
    	//setup complete, main job starts
    	System.out.println("DEMO v1 > #3 > Create a Discussion in 'Loomio'");
    	// TODO: implement operations finders in Service
    	
    	// STEP 3.1: Create the operation to create the discussion
    	// 3.1.1. => read the operation definition 
    	ServiceOperationDefinition createDiscussionDef = ServiceOperationDefinition.read(LOOMIO_CREATE_DISCUSSION_OP_ID);
    	System.out.println("DEMO v1 > ##3.1 > Read definition of [Loomio].createDiscussion operation :"+Json.toJson(createDiscussionDef));
    	
    	// TODO: put parameter values in a HashMap
    	String title = DISC_SAMPLE_TITLE;
    	String desc = DISC_SAMPLE_DESC;
    	String groupId = LOOMIO_GROUP_ID;
    	    	
    	// 3.1.2. => create an operation instance for that definition 
    	System.out.println("DEMO v1 > ##3.2 > Prepare instance of [Loomio].createDiscussion:");
    	ServiceOperation createDiscussion = new ServiceOperation();
    	createDiscussion.setDefinition(createDiscussionDef);
    	createDiscussion.setService(loomio);
    	createDiscussion.setAppCivistOperation("createDiscussion");
    	
    	
    	// 3.1.3. => setup parameters for the operation according to definition 
    	System.out.println("DEMO v1 > ##3.3 > Prepare parameters for instance of [Loomio].createDiscussion according to definition:"+Json.toJson(createDiscussionDef));
    	List<ServiceParameterDefinition> paramDefs = createDiscussionDef.getParameters();

    	for (ServiceParameterDefinition pDef : paramDefs) {
    		System.out.println("DEMO v1 > ###3.3.1 > Setting value for Parameter:"+Json.toJson(pDef));
    		ServiceParameter param = new ServiceParameter();
    		param.setServiceOperation(createDiscussion);
    		param.setServiceParameter(pDef);
    		
    		if(pDef.getName().equals("discussion")) {
    			if(pDef.getType().equals("BODY_PARAM")){
    				if(pDef.getDataType().equals("JSON")) {
    					Map<String,String> jsonBodyMap = new HashMap<String,String>();
    					List<ServiceParameterDataModel> dm = pDef.getDataModel();
    					
    					for (ServiceParameterDataModel part : dm) {
    						String key = part.getDataKey();
    						if (key.equals("title")) {
    							jsonBodyMap.put(key, title);
    						} else if (key.equals("description")) {
    							jsonBodyMap.put(key, desc);
    						} else if (key.equals("group_id")) {
    							jsonBodyMap.put(key, groupId);
    						}  else {
    							jsonBodyMap.put(key, part.getDefaultValue());
    						}
						}    		    		
    		    		param.setValue(Json.toJson(jsonBodyMap).toString());
    				}
    			}
    		}
    		System.out.println("DEMO v1 > ###3.3.2 > Value: "+Json.toJson(param));
    		createDiscussion.getParameters().add(param);
		}
    	
    	// STEP 4 => prepare and send service request/call
    	System.out.println("DEMO v1 > #4 > Prepare parameters for instance of [Loomio].createDiscussion according to definition:"+Json.toJson(createDiscussionDef));
    	
    	execute(createDiscussion);
    	
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

	private void execute(ServiceOperation op) {
		String serviceUrl = op.getService().getBaseUrl();
    	ServiceAuthentication serviceAuth = op.getService().getAuth().get(0);
    	String method = op.getDefinition().getMethod();
    	String opName = op.getDefinition().getName();
    	String opType = op.getDefinition().getType(); //TODO: Use op_type to either call a REST service or another type
    	List<ServiceParameter> pValues = op.getParameters();
    	ServiceOperationDefinition dependence = op.getDefinition().getDependsOf();
    	String modeOfDependence = op.getDefinition().getModeOfDependence();
    	
    	if (dependence!=null && modeOfDependence =="CHAIN") {
    		serviceUrl += "/" + dependence.getName() + LOOMIO_GROUP_ID; // TODO: how to include dependence parameter values
    	}
    	
    	WSRequestHolder holder = WS.url(serviceUrl+"/"+opName);
    	
		//.setTimeout(1000)
		//.setQueryParameter("paramKey", "paramValue");

    	if (serviceAuth.getAuthType().equals("COOKIE")) {
        	System.out.println("DEMO v1 > #4.1 > Setting up authentication:"+serviceAuth.getTokenParamName()+"="+serviceAuth.getToken());
    		holder.setHeader("Cookie",serviceAuth.getTokenParamName()+"="+serviceAuth.getToken());
    		holder.setHeader(serviceAuth.getTokenParamName(),serviceAuth.getToken());
    		holder.setAuth(serviceAuth.getToken());
    	}
    	
    	holder.setMethod(method);
    	//holder.setQueryString();
    	
    	for (ServiceParameter p : pValues) {
			if(p.getServiceParameter().getType().equals("BODY_PARAM")) {
				holder.setBody(p.getValue());
				if (p.getServiceParameter().getDataType().equals("JSON")) {
					holder.setHeader("Content-Type", "application/json");
				}
			}
		}
    	
    	
    	// You end by calling a method corresponding to the HTTP method you want to use. 
    	// This ends the chain, and uses all the options defined on the built request in the 
    	// WSRequestHolder.

    	Promise<JsonNode> responsePromise = holder.execute().map(
    	        new Function<WSResponse, JsonNode>() {
    	            public JsonNode apply(WSResponse response) {
    	                JsonNode json = response.asJson();
    	            	System.out.println("DEMO v1 > #4.1 > Receive response from operation:"+json.toString());
    	                return json;
    	            }
    	        }
    	);
	}

}
