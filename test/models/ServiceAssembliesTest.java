package models;

import java.util.*;

import models.services.ServiceAssembly;
import models.services.ServiceCampaign;
import models.services.ServiceIssue;
import models.services.Service;
import models.services.ServiceAuthentication;
import models.services.ServiceDefinition;
import models.services.ServiceOperation;
import models.services.ServiceOperationDefinition;
import models.services.ServiceParameter;
import models.services.ServiceParameterDataModel;
import models.services.ServiceParameterDefinition;
import models.services.ServiceResource;

import org.junit.*;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;

import engine.Composer;
import engine.Runner;
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
 * Simple (JUnit) tests that can call all parts of a play app. If you are
 * interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ServiceAssembliesTest extends WithApplication {

	// ServiceAssembly Details
	private static final String ASSEMBLY_TITLE = "Urban Spaces Assembly";
	private static final String ASSEMBLY_DESCRIPTION = "An assembly that works on proposals for addressing problems in urban spaces";
	private static final String ASSEMBLY_CITY = "Paris";
	
	// The service details listed below are based on the example data included in "initial-data/orchestration-example-1.yml"
	// Agora Service Details
	private static final Long AGORA_SERVICE_ID = new Long(3);
	private static final Long AGORA_CREATE_ELECTION_OP_ID = new Long(11);
	private static final Long AGORA_READ_ELECTION_OP_ID = new Long(8);
	private static final Long AGORA_ELECTION_ID = new Long(19);
	private static final Long AGORA_ID = new Long(4);
	private static final String AGORA_CREATE_AGORA_OP_ID = "14";
	
	// Etherpad Service Details
	private static final Long ETHERPAD_SERVICE_ID = new Long(1);
	private static final String ETHERPAD_READ_TEXT_OP_ID = "1";
	private static final String ETHERPAD_CREATE_PAD_OP_ID = "2";

	// Loomio Service Details
	private static final Long LOOMIO_SERVICE_ID = new Long(2);
	private static final Long LOOMIO_CREATE_DISCUSSION_OP_ID = new Long(9);
	private static final Long LOOMIO_CREATE_PROPOSAL_OP_ID = new Long(13);
	private static final String LOOMIO_GROUP_ID = "2";
	private static final String LOOMIO_CREATE_GROUP_OP_ID = "10";

	// Test Data Examples
	public static final String PROP_SAMPLE_TITLE = "Garden Proposal";
	public static final String PROP_SAMPLE_DESC = "Let's make a Garden!";
	public static final String PROP_SAMPLE_TITLE_2 = "Library Proposal";
	public static final String PROP_SAMPLE_DESC_2 = "Let's make a Library!";
	public static final String PROP_SAMPLE_TITLE_3 = "Park Proposal";
	public static final String PROP_SAMPLE_DESC_3 = "Let's make a Park!";
	
	public static final String GROUP_SAMPLE_TITLE = "Urban Spaces Group";
	public static final String GROUP_SAMPLE_DESC = "Group discussing urban spaces ideas";
	
	public static final String DISC_SAMPLE_TITLE = "Empty lot in telegraph and channing";
	public static final String DISC_SAMPLE_DESC = "How might we best use the empty lot?";
	public String DEMO_VERSION = "1";

	@Test
	public void testServiceAssemblyIssueCreation() {

		// 1. Create instance of Issue
		ServiceIssue i = new ServiceIssue();
		i.setIssueId(new Long(999));
		i.setTitle("Issue Title");
		i.setBrief("Issue Brief");

		// 2. Create instance of ServiceAssembly
		ServiceAssembly a = new ServiceAssembly();
		a.setAssemblyId(new Long(999));
		a.setName("Assembly Name");
		a.setDescription("Assembly Description");
		a.setCity("Asuncion");
		a.setIcon("/assets/images/sfskyline-small.jpg");
		a.setUrl("/api/assembly/4");
		a.getServiceIssues().add(i);

		// 3. Save Assembly (and its corresponding issues)
		a.save();

		// 4. Read the assembly for db and assert it exists and it has issues
		ServiceAssembly a1 = ServiceAssembly.read(new Long(999));
		assertThat(a1 != null);
		assertThat(a1.getServiceIssues().size() > 0);

		// 5. Delete created data
		i.delete();
		a.delete();

		// 6. Verify data was deleted
		a1 = ServiceAssembly.read(new Long(999));
		assertThat(a1 == null);
		assertThat(a1.getServiceIssues().size() == 0);
	}

	@Test
	public void testYAMLDataLoading() {

		// VirtualFile appRoot = VirtualFile.open(Play.applicationPath);
		// Play.javaPath.add(0, appRoot.child("test/sub/packages/data.yml"));

		System.out.println("PATH: " + Play.application().path());
		System.out.println("PATH: " + Play.application().configuration());

		// 1. Load unit testing data

		List list = (List) Yaml.load("initial-data/unit-test-data.yml");
		Ebean.save(list);

		// 2. Read loaded Service and its corresponding relationships
		ServiceDefinition sd = ServiceDefinition.read(new Long(999));
		ServiceOperationDefinition sod = sd.getOperations().get(0);
		ServiceParameterDefinition spd = sod.getParameters().get(0);
		ServiceParameterDataModel spdm = spd.getDataModel().get(0);

		// 3. Read Service related instances from DB
		ServiceOperationDefinition sodDB = ServiceOperationDefinition
				.read(new Long(999));
		ServiceParameterDefinition spdDB = ServiceParameterDefinition
				.read(new Long(999));
		ServiceParameterDataModel spdmDB = ServiceParameterDataModel
				.read(new Long(999));

		// 4. Compare DB readed objects with Service related instances
		assertThat(sd != null);
		assertThat(sd.getOperations().size() > 0);
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
	public void testOrchestrationDemo1() {
		// Observation: Using Preloaded Data (see Global.onStart())
		DEMO_VERSION = "1";
		/******************************************************************************************
		 * STEP 1: 
		 * - Create a new AppCivist orchestration, i.e., an ServiceAssembly
		 * ==> e.g., orch = new AppCivistOrchestrator() ;
		 */
		ServiceAssembly orchestration = 
					new ServiceAssembly(ASSEMBLY_TITLE,
								 	ASSEMBLY_DESCRIPTION, 
								 	ASSEMBLY_CITY);

		/******************************************************************************************
		 * STEP 2: 
		 * - Add Service Instances to the assembly: Loomio & Agora
		 * - In this example, we add already existing Service Instances, otherwise we need to 
		 *   create new instances by searching for the ServiceDefinition and instantiating
		 *   by indicating its BaseUrl and obtaining authentication credentials
		 */
		System.out.println("DEMOv"+DEMO_VERSION+" > #1 > Add connected services to the ServiceAssembly");

		// e.g., loomio = orch.getServiceInstance(Constants.LOOMIO_INFO) ;
		Service loomio = Service.read(LOOMIO_SERVICE_ID);
		System.out
				.println("DEMOv"+DEMO_VERSION+" > #1.1 > Reading and adding service 'Loomio'"
						+ Json.toJson(loomio));
		orchestration.addConnectedService(loomio);

		// e.g., agora = orch.getServiceInstance(Constants.AGORA_INFO) ;
		Service agora = Service.read(AGORA_SERVICE_ID);
		System.out
				.println("DEMOv"+DEMO_VERSION+" > #1.2 > Reading and adding Service 'Agora'"
						+ Json.toJson(agora));
		orchestration.addConnectedService(agora);

		/******************************************************************************************
		 * STEP 3: 
		 * - Map AppCivist Operations to Services where to lookup for specific operation definitions
		 * - Map AppCivist Operations to specific definitions inside the Services
		 * - Map specific resources to existing ServiceResource instances 
		 * TODO: Map operation inputs/outputs between operations 
		 * TODO: Modify mappings to store them in entities (rather than HashMaps)
		 * TODO: Modify mappings to store actual entities and not IDs
		 */

		System.out.println("DEMOv"+DEMO_VERSION+" > #2 > Create a Discussion in 'Loomio'");

		orchestration.addOperationServiceMapping("createGroup", loomio);
		orchestration.addOperationServiceMapping("createIssue", loomio);
		orchestration.addOperationServiceMapping("createDiscussion", loomio);
		orchestration.addOperationServiceMapping("createProposal", loomio);
		orchestration.addOperationServiceMapping("readProposal", loomio);
		orchestration.addOperationServiceMapping("readProposalFromGroup", loomio);
		orchestration.addOperationServiceMapping("createElection", agora);
		orchestration.addOperationServiceMapping("readElection", agora);

		// TODO: replace operation ids for ServiceOperationDefinition
		loomio.addOperationMapping("createGroup",LOOMIO_CREATE_GROUP_OP_ID + "");
		loomio.addOperationMapping("createDiscussion",LOOMIO_CREATE_DISCUSSION_OP_ID + "");
		loomio.addOperationMapping("createProposal",LOOMIO_CREATE_PROPOSAL_OP_ID+"");
		agora.addOperationMapping("createElection", AGORA_CREATE_ELECTION_OP_ID+ "");
		agora.addOperationMapping("readElection", AGORA_READ_ELECTION_OP_ID+ "");

		// TODO: add also support to resource mappings
//		orchestration.addResourceMappings("mainGroup", LOOMIO_GROUP_ID+"");
//		orchestration.addResourceMappings("agoraGroup", AGORA_ID+"");
//		orchestration.addResourceMappings("agoraElection", AGORA_ELECTION_ID+"");

		/******************************************************************************************
		 * STEP 3: Actual Orchestration Execution
		 * - For each action, we create an operation (i.e, ServiceOperation) 
		 *   based on its definition (e.g., ServiceOperationDefinition)
		 * - To know what definition, we read the information in the orchestration (i.e., the assembly) 
		 * - The Demo does the following: 
		 * 		1. add Loomio group creation
		 * 		2. Creates an ISSUE as a discussion in Loomio
		 * 		3. Creates PROPOSALS within the created GROUP
		 * 		4. Reads the PROPOSALS
		 * 		5: add Agora group creation
		 * 		6. Creates an election in Agora (with proposals as options and the issue as question)
		 * 		7. Prints the final definition of the ServiceAssembly for this demo
		 * - Example: To create a discussion in Loomio, we need to instantiate the operation 
		 *   "createDiscussion" using the OperationDefinition to which it is mapped in the 
		 *   selected service ("loomio") => this is the task of the "composition engine"
		 * - Instantiating the operation implies filling in also the values for its requested 
		 * 	 parameters
		 * - Once created the operation, we need to execute it => this is done by the 
		 *   "execution engine"
		 * 
		 * Fake example as defined in issue #20:
		 * 
		 * orch = new AppCivistOrchestrator() ;
		 * loomio = orch.getServiceInstance(Constants.LOOMIO_INFO) ;
		 * agora =  orch.getServiceInstance(Constants.AGORA_INFO) ;
		 * 
		 * String title = Constants.SAMPLE_TITLE ;
		 * String desc = Constants.SAMPLE_DESC ;
		 * 
		 * loomio.RESTCalls.createDiscussion(title, desc); //this is fake, is being used only for demo purposes
		 * 
		 * //setup complete, main job starts
		 * 
		 * AppCivistDiscussion d = loomio.RESTCalls.getDiscussions().get(0); // gets the first discussion, 
		 * 																	 // uses AppCivist data structure
		 * 
		 * AppCivistElectionInstance e = agora.RESTCalls.createAgora(d);
		 * 
		 * for (AppCivistDecisions disc : d.getMotions()){
		 *  e.addVoteOption(disc.motion.title, disc.motion.url);
		 * }
		 * 
		 * // at the end of this, our agora instance should have an election set up inside it.
		 * 
		 * Utils.printInfo(agora.RESTCalls.getElectionInstance(e.getID())); //should print all voting options
		 * 
		 * //end of demo 1 
		 */
		System.out.println("DEMOv"+DEMO_VERSION+" > #3 > Create a Discussion in 'Loomio'");
		
		// TODO: implement operations finders in Service
		// TODO: automatically link operation inputs and outputs

		//		ServiceResource loomioGroup = new ServiceResource();
		//		loomioGroup.setKeyName("group");
		//		loomioGroup.setBody("{'group_id':"+LOOMIO_GROUP_ID+"}");
		// TODO: model also the data model of the expected output for each operations

		// 1. Create a group in loomio for the issue and the discussions
		String groupName = "[LOOMIO]: "+GROUP_SAMPLE_TITLE;
		String groupDesc = GROUP_SAMPLE_DESC;
		ServiceResource loomioGroup = createGroupInLoomio(orchestration, groupName, groupDesc, "GROUP");	
		JsonNode jsonLoomioGroup = Json.parse(loomioGroup.getBody());
		String groupNameCreated = jsonLoomioGroup.get("groups").get(0).get("name").asText();
		assertThat(groupNameCreated.equals(groupName));
		
		// 2. Create a discussion for describing the issue
		String disc_title = "ISSUE:"+DISC_SAMPLE_TITLE;
		String disc_desc = DISC_SAMPLE_DESC;
		ServiceResource issue = createDiscussionInLoomio(orchestration, loomioGroup, disc_title, disc_desc,  "ISSUE");
		JsonNode jsonLoomioDiscussion = Json.parse(issue.getBody());
		String issueTitle = jsonLoomioDiscussion.get("discussions").get(0).get("title").asText();
		assertThat(issueTitle.equals(disc_title));
		
		// 3. Create three different discussions for working groups to come up with proposals
		disc_title = PROP_SAMPLE_TITLE + " 1";
		disc_desc = PROP_SAMPLE_DESC + " 1";
		ServiceResource d1 = createDiscussionInLoomio(orchestration, loomioGroup, disc_title, disc_desc, "DISCUSSION");
		JsonNode jsonLoomioDiscussion1 = Json.parse(d1.getBody());
		String dTitle1 = jsonLoomioDiscussion1.get("discussions").get(0).get("title").asText();
		assertThat(dTitle1.equals(disc_title));

		disc_title = PROP_SAMPLE_TITLE + " 2";
		disc_desc = PROP_SAMPLE_DESC + " 2";		
		ServiceResource d2 = createDiscussionInLoomio(orchestration, loomioGroup, disc_title, disc_desc, "DISCUSSION");
		JsonNode jsonLoomioDiscussion2 = Json.parse(d2.getBody());
		String dTitle2 = jsonLoomioDiscussion2.get("discussions").get(0).get("title").asText();
		assertThat(dTitle2.equals(disc_title));

		disc_title = PROP_SAMPLE_TITLE + " 3";
		disc_desc = PROP_SAMPLE_DESC + " 3";
		ServiceResource d3 = createDiscussionInLoomio(orchestration, loomioGroup, disc_title, disc_desc, "DISCUSSION");
		JsonNode jsonLoomioDiscussion3 = Json.parse(d3.getBody());
		String dTitle3 = jsonLoomioDiscussion3.get("discussions").get(0).get("title").asText();
		assertThat(dTitle3.equals(disc_title));
		
		List<ServiceResource> discussions = new ArrayList<ServiceResource>();
		discussions.add(d1);
		discussions.add(d2);
		discussions.add(d3);
		
		// 4. Create final proposals in each discussion
		List<ServiceResource> proposals = new ArrayList<ServiceResource>();
		
		int num = 1;
		for (ServiceResource discussion : discussions) {
			ServiceResource proposal = createLoomioProposal(orchestration, loomioGroup, discussion, "PROPOSAL");
			JsonNode jsonLoomioProposal = Json.parse(proposal.getBody());
			String proposalTitle = jsonLoomioProposal.get("proposals").get(0).get("name").asText();
			assertThat(proposalTitle.equals("FINAL PROPOSAL: "+PROP_SAMPLE_TITLE+num));			
			proposals.add(proposal);
			num++;
		}

		// Before creating the election, we know need to create an Agora, which is a group. 
		// But we have previously assigned createGroup to Loomio, so that needs to be updated
		orchestration.removeOperationServiceMapping("createGroup");
		orchestration.addOperationServiceMapping("createGroup", agora);
		agora.addOperationMapping("createGroup",AGORA_CREATE_AGORA_OP_ID+ "");
		
		// 5. Create agora for hosting the election
		//		ServiceResource agoraGroup = new ServiceResource();
		//		agoraGroup.setKeyName("group");
		//		agoraGroup.setBody("{'agora_id':"+AGORA_ID+"}");
		ServiceResource agoraGroup = createGroupInAgora(orchestration, groupName, groupDesc, "GROUP");	
		JsonNode jsonAgoraGroup = Json.parse(agoraGroup.getBody());
		String agoraPrettyName = jsonAgoraGroup.get("pretty_name").asText();
		assertThat(agoraPrettyName.equals(groupName));
		
		// 6. Create the election in the recently created Agora
		ServiceResource election = createAgoraElection(orchestration, agoraGroup, proposals, issue, "ELECTION");
		JsonNode jsonAgoraElection = Json.parse(election.getBody());
		String electionPrettyName = jsonAgoraElection.get("pretty_name").asText();
		assertThat(electionPrettyName.equals("Choose a proposal for: "+issueTitle));

		System.out.println("DEMOv"+DEMO_VERSION+" > #4 > Created Election => "+election.getBody());
		System.out.println("DEMOv"+DEMO_VERSION+" > #5 > Rendered ServiceAssembly => "+Json.toJson(orchestration));
	}

	/**
	 * Implementation of Issue #18
	 * Create a single unit test class to execute the passage between campaigns of an assembly that goes from Proposal Making directly to Voting, using Etherpad, Loomio and Agora, as follows:
	 * 
	 * Proposal Making: based on issues, working groups draft proposals to address them
	 * 
	 * 1. Start by creating an Issue => creating a Pad on Etherpad
	 * 2. Create at least 3 Working Group (WG) => publicly listed groups in Loomio, which work with invitation only and allow public and private * discussions
	 * 3. Create 5 Proposals related to the Issue, and each to a different WG => creating a Pad on Etherpad
	 * 4. Move to the next campaign, of Deliberation, copying the resources that were created and the ones that are still open to the next campaign.
	 * 
	 * Deliberation: active discussion and evaluation of proposals by the whole community
	 * 
	 * Create a Public Working Group, open to all members of the community => in Loomio, create a Public Group with the name of the ServiceAssembly and * Issue
	 * 4. Start by creating a Public Discussion for each proposal => in Loomio, create a public discussion for each proposal that was created in the * previous campaign, within the public group
	 * 5. Move to the next campaign, of Voting
	 * 
	 * Voting:
	 * 
	 * 6. Start by creating a Public Group where the voting will happen => create an agora in Agora
	 * 7. Read the Proposals from the Deliberation campaign copy their titles + URLs as options in a new Election within the created group => create * an election in Agora => copy an election in Agora
	 */
	@Test
	public void testOrchestrationDemo2() {
		// Observation: Using Preloaded Data (see Global.onStart())
		DEMO_VERSION = "2";
		
		/******************************************************************************************
		 * STEP 1: 
		 * - Create a new AppCivist orchestration, i.e., an ServiceAssembly
		 * ==> e.g., orch = new AppCivistOrchestrator() ;
		 */
		ServiceAssembly orchestration = 
					new ServiceAssembly(ASSEMBLY_TITLE,
								 	ASSEMBLY_DESCRIPTION, 
								 	ASSEMBLY_CITY);

		/******************************************************************************************
		 * STEP 2: 
		 * - Add Service Instances to the assembly: Loomio & Agora
		 * - In this example, we add already existing Service Instances, otherwise we need to 
		 *   create new instances by searching for the ServiceDefinition and instantiating
		 *   by indicating its BaseUrl and obtaining authentication credentials
		 */
		System.out.println("DEMOv2 > #1 > Add connected services to the ServiceAssembly");

		// e.g., loomio = orch.getServiceInstance(Constants.LOOMIO_INFO) ;
		Service loomio = Service.read(LOOMIO_SERVICE_ID);
		System.out.println("DEMOv2 > #1.1 > Reading and adding service 'Loomio'"+Json.toJson(loomio));
		orchestration.addConnectedService(loomio);

		// e.g., agora = orch.getServiceInstance(Constants.AGORA_INFO) ;
		Service agora = Service.read(AGORA_SERVICE_ID);
		System.out.println("DEMOv2 > #1.2 > Reading and adding Service 'Agora'"+Json.toJson(agora));
		orchestration.addConnectedService(agora);

		// e.g., etherpad = orch.getServiceInstance(Constants.ETHERPAD_INFO) ;
		Service etherpad = Service.read(ETHERPAD_SERVICE_ID);
		System.out.println("DEMOv2 > #1.3 > Reading and adding Service 'Etherpad'"+Json.toJson(etherpad));
		orchestration.addConnectedService(etherpad);
		
		/******************************************************************************************
		 * STEP 3: Configure the assembly
		 * - Map AppCivist Operations to Services where to lookup for specific operation definitions
		 * - Map AppCivist Operations to specific definitions inside the Services
		 * - Map specific resources to existing ServiceResource instances 
		 * - Create an issue object and its campaigns
		 * TODO: Map operation inputs/outputs between operations 
		 * TODO: Modify mappings to store them in entities (rather than HashMaps)
		 * TODO: Modify mappings to store actual entities and not IDs
		 */
		
		/**
		 * 
		 * 1. Start by creating an Issue => creating a Pad on Etherpad
		 * 2. Create at least 3 Working Group (WG) => publicly listed groups in Loomio, which work with invitation only and allow public and private * discussions
		 * 3. Create 5 Proposals related to the Issue, and each to a different WG => creating a Pad on Etherpad
		 * 4. Move to the next campaign, of Deliberation, copying the resources that were created and the ones that are still open to the next campaign.
		 * 
		 * 5. Start by creating a Public Discussion for each proposal => in Loomio, create a public discussion for each proposal that was created in the * previous campaign, within the public group
		 * 6. Move to the next campaign, of Voting
		 * 
		 * 7. Start by creating a Public Group where the voting will happen => create an agora in Agora
		 * 8. Read the Proposals from the Deliberation campaign copy their titles + URLs as options in a new Election within the created group => create * an election in Agora => copy an election in Agora
		 */
		
		System.out.println("DEMOv2 > #3.1 > Adding operation mappings to the ServiceAssembly");

		orchestration.addOperationServiceMapping("createGroup", loomio); 		// there is a create group in both agora and loomio and we need both
		orchestration.addOperationServiceMapping("createGroup", agora); 		// there is a create group in both agora and loomio and we need both
		orchestration.addOperationServiceMapping("createDiscussion", loomio);
		orchestration.addOperationServiceMapping("createIssue", etherpad);
		orchestration.addOperationServiceMapping("readIssue", etherpad);
		orchestration.addOperationServiceMapping("createProposal", etherpad);
		orchestration.addOperationServiceMapping("readProposal", etherpad);
		orchestration.addOperationServiceMapping("createElection", agora);
		orchestration.addOperationServiceMapping("readElection", agora);

		System.out.println("DEMOv2 > #3.2 > Adding operation mappings to the services in the ServiceAssembly");
		// TODO: replace operation ids for ServiceOperationDefinition
		loomio.addOperationMapping("createGroup",LOOMIO_CREATE_GROUP_OP_ID + "");
		loomio.addOperationMapping("createDiscussion",LOOMIO_CREATE_DISCUSSION_OP_ID + "");
		loomio.addOperationMapping("createProposal",LOOMIO_CREATE_PROPOSAL_OP_ID+"");
		
		agora.addOperationMapping("createGroup", AGORA_CREATE_AGORA_OP_ID+ "");
		agora.addOperationMapping("createElection", AGORA_CREATE_ELECTION_OP_ID+ "");
		agora.addOperationMapping("readElection", AGORA_READ_ELECTION_OP_ID+ "");
		
		etherpad.addOperationMapping("createProposal", ETHERPAD_CREATE_PAD_OP_ID);
		etherpad.addOperationMapping("readProposal", ETHERPAD_READ_TEXT_OP_ID);
		etherpad.addOperationMapping("createIssue", ETHERPAD_CREATE_PAD_OP_ID);
		etherpad.addOperationMapping("readIssue", ETHERPAD_READ_TEXT_OP_ID);

		System.out.println("DEMOv2 > #3.3 > Creating an Issue object to hold later the actual issue that will be created and to store information of the decision making campaigns");
		// Create the Issue object
		String issue_title = "ISSUE:"+DISC_SAMPLE_TITLE;
		String issue_desc = DISC_SAMPLE_DESC;
		ServiceIssue issueObject = new ServiceIssue();
		issueObject.setServiceAssembly(orchestration);
		issueObject.setTitle(issue_title);
		issueObject.setBrief(issue_desc);
		
		System.out.println("DEMOv2 > #3.4 > Creating 3 Campaign objects for each stage of the process (Proposal Making, Deliberation, Voting)");
		ServiceCampaign proposals = new ServiceCampaign();
		proposals.setIssue(issueObject);
		proposals.setEnabled(true);
		proposals.setName("Proposal Making Phase");
		issueObject.addCampaign(proposals);

		ServiceCampaign deliberation = new ServiceCampaign();
		deliberation.setIssue(issueObject);
		deliberation.setEnabled(true);
		deliberation.setName("Deliberation Phase");
		issueObject.addCampaign(deliberation);

		ServiceCampaign voting = new ServiceCampaign();
		voting.setIssue(issueObject);
		voting.setEnabled(true);
		voting.setName("Voting Phase");
		issueObject.addCampaign(voting);
		
		// TODO: add also support to resource mappings
		//		orchestration.addResourceMappings("mainGroup", LOOMIO_GROUP_ID+"");
		//		orchestration.addResourceMappings("agoraGroup", AGORA_ID+"");
		//		orchestration.addResourceMappings("agoraElection", AGORA_ELECTION_ID+"");
		
		/******************************************************************************************
		 * STEP 4: Read operation definitions, setup parameters and execute
		 * 
		 */
		
		// TODO: implement operations finders in Service
		// TODO: automatically link operation inputs and outputs

		//		ServiceResource loomioGroup = new ServiceResource();
		//		loomioGroup.setKeyName("group");
		//		loomioGroup.setBody("{'group_id':"+LOOMIO_GROUP_ID+"}");
		// TODO: model also the data model of the expected output for each operations

		// We are using the exact service now to call the service (not the orchestration), an option when same operations are available in multiple services
		
		/**
		 * 1. Start by creating an Issue => creating a Pad on Etherpad
		 */
		ServiceResource padResponse = createIssueInEtherpad(etherpad, issueObject,  "ISSUE");
		// The createService does not return the created resource, so we have to read it afterwards
		ServiceResource issue = readTextInEtherpad(etherpad, issueObject.getTitle(),  "ISSUE");
		issueObject.setResource(issue);
		proposals.addCampaignResource(issue);
		orchestration.addResourceMappings("ISSUE", issue); // TODO: right now, there is some redundancy with ServiceResources, remove it later
		orchestration.addServiceIssue(issueObject);
		JsonNode jsonEtherpadIssue = Json.parse(issue.getBody());
		System.out.println("DEMOv2 > #4.1 > Created ISSUE on 'Etherpad': "+jsonEtherpadIssue.toString());
		String issueText = jsonEtherpadIssue.get("data").get("text").asText();
		assertThat(issueText.equals(issue_desc)); // will fail becaus Etherpad does not return the text

		
		/**
		 * 2. Create at least 3 Working Group (WG) => publicly listed groups in Loomio, which work with invitation only and allow public and private * discussions
		 */
		
		List<ServiceResource> workingGroups = new ArrayList<ServiceResource>();
		for (int i = 1; i < 4; i++) {
			String groupName = "[LOOMIO WG"+i+"]: "+GROUP_SAMPLE_TITLE;
			String groupDesc = GROUP_SAMPLE_DESC;
			ServiceResource loomioGroup = createGroupInLoomio(loomio, groupName, groupDesc, "GROUP");
			// TODO: Groups objects? 
			workingGroups.add(loomioGroup);
			proposals.addCampaignResource(loomioGroup);
			orchestration.addResourceMappings("GROUP", loomioGroup);
			JsonNode jsonLoomioGroup = Json.parse(loomioGroup.getBody());
			String groupNameCreated = jsonLoomioGroup.get("groups").get(0).get("name").asText();
			assertThat(groupNameCreated.equals(groupName));
			System.out.println("DEMOv2 > #4."+(i+1)+" > Created GROUP in 'Loomio': "+jsonLoomioGroup.toString());			
		}
		
		
		/**
		 * 3. Create 5 Proposals related to the Issue, and each to a different WG => creating a Pad on Etherpad
		 */
		List<ServiceResource> proposalListEtherpad = new ArrayList<ServiceResource>();
		for (int i = 1; i < 6; i++) {
			String proposalTitle = PROP_SAMPLE_TITLE + " " + i;
			String proposalText = PROP_SAMPLE_DESC + " " + i;
			
			padResponse = createProposalInEtherpad(etherpad, proposalTitle, proposalText,  "PROPOSAL");
			// The createService does not return the created resource, so we have to read it afterwards
			ServiceResource proposal = readTextInEtherpad(etherpad, proposalTitle,  "PROPOSAL");
			proposalListEtherpad.add(proposal);
			proposals.addCampaignResource(proposal);
			orchestration.addResourceMappings("PROPOSAL", proposal); // TODO: right now, there is some redundancy with ServiceResources, remove it later
			orchestration.addServiceIssue(issueObject);
			JsonNode jsonProposal = Json.parse(proposal.getBody());
			String jsonProposalText = jsonProposal.get("data").get("text").asText();
			assertThat(jsonProposalText.equals(issue_desc)); // will fail becaus Etherpad does not return the text
			System.out.println("DEMOv2 > #4."+(i+4)+" > Created PROPOSAL on 'Etherpad': "+jsonProposal.toString());	

			System.out.println("DEMOv2 > #4."+(i+4)+" > Assign PROPOSAL to working group "+(i%3+1));
			// assign the proposal to the working group
			workingGroups.get(i%3).addRelatedResource(proposal);
		}
		
		
		/**
		 * 4. Move to the next campaign, of Deliberation, copying the resources that were created and the ones that are still open to the next campaign.
		 */
		// TODO: Use campaign resources instead of locally defined workingGroups (now, this is a fix) 
		List<ServiceResource> proposalListLoomio = new ArrayList<ServiceResource>();

		int groupNum=0;
	    for (ServiceResource serviceResource : proposals.getCampaignResources()) {
		//for (ServiceResource serviceResource : workingGroups) {
			
			if (serviceResource.getType().equals("GROUP")) { // replace by reading from the hashmap of resources
				groupNum++;
				ServiceResource loomioGroup = serviceResource;
				List<ServiceResource> groupProposals = loomioGroup.getRelatedResources();
				deliberation.addCampaignResource(loomioGroup); // Adding the group to Deliberation
				
				int proposalNum = 0;
				int i = 1;
				for (ServiceResource groupEtherpadProposal : groupProposals) {
					proposalNum++;
					// Create a Discussion for the GROUP Proposal
					JsonNode jsonGroup = Json.parse(loomioGroup.getBody());
					String groupNameCreated = jsonGroup.get("groups").get(0).get("name").asText();
					String disc_title = "Proposal #"+proposalNum+" of Group #"+groupNum;
					
					JsonNode jsonProposal = Json.parse(groupEtherpadProposal.getBody());
					String jsonProposalText = jsonProposal.get("data").get("text").asText();
					String disc_desc = "Discussion about proposal #"+proposalNum+" of Group #"+groupNum+"\n Group Name: "+groupNameCreated+"\n Proposal Details:\n"+jsonProposalText;
					ServiceResource discussion = createDiscussionInLoomio(orchestration, loomioGroup, disc_title, disc_desc,  "DISCUSSION");
					JsonNode jsonLoomioDiscussion = Json.parse(discussion.getBody());
					String discussionTitle = jsonLoomioDiscussion.get("discussions").get(0).get("title").asText();
					assertThat(discussionTitle.equals(disc_title));	
					
					discussion.addRelatedResource(groupEtherpadProposal);
					deliberation.addCampaignResource(discussion);
					orchestration.addResourceMappings("DISCUSSION", discussion);

					System.out.println("DEMOv2 > #5."+(i+9)+" > Created DISCUSSION on 'Loomio': "+jsonLoomioDiscussion.toString());	
					i++;
					// Copy the Proposal into a proposal for the Discussion
					
					// TODO: RENAME method to create proposal when it receives the orchestration
					ServiceResource loomioProposal = createLoomioProposal(loomio, loomioGroup, discussion, "PROPOSAL");
					JsonNode jsonLoomioProposal = Json.parse(loomioProposal.getBody());
					String proposalTitle = jsonLoomioProposal.get("proposals").get(0).get("name").asText();
					assertThat(proposalTitle.equals(disc_title));			

					System.out.println("DEMOv2 > #5."+(i+9)+" > Created PROPOSAL on 'Loomio' DISCUSSION: "+jsonLoomioProposal.toString());	
					proposalListLoomio.add(loomioProposal);

					discussion.addRelatedResource(loomioProposal); // TODO: how to differentiate in the ServiceResource between Loomio and Etherpad proposals
					deliberation.addCampaignResource(loomioProposal);
					orchestration.addResourceMappings("PROPOSAL", loomioProposal);					
					i++;
				}
			}
		}
		
		/**
		 * 6. Move to the next campaign, of Voting
		 * 
		 * 7. Start by creating a Public Group based on the Issue where the voting will 
		 *    happen => create an agora in Agora
		 */
	    
	    String groupName = issue_title; // TODO: PROBLEM, the response body from the createPad op does not include the padID
	    String groupDesc = Json.parse(issue.getBody()).get("data").get("text").asText();

 		ServiceResource agoraGroup = createGroupInAgora(agora, groupName, groupDesc, "GROUP");	
		JsonNode jsonAgoraGroup = Json.parse(agoraGroup.getBody());
		String agoraPrettyName = jsonAgoraGroup.get("pretty_name").asText();
		assertThat(agoraPrettyName.equals(groupName));
		voting.addCampaignResource(agoraGroup);
		orchestration.addResourceMappings("GROUP", agoraGroup);
		System.out.println("DEMOv2 > #5.14 > Created GROUP in 'Agora': "+jsonAgoraGroup.toString());	
		
	    for (ServiceResource finalProposal : proposalListLoomio) {
			voting.addCampaignResource(finalProposal);
		}

		/**
		 * 8. Read the Proposals from the Deliberation campaign copy their titles + URLs as options in a new Election within the created group => create * an election in Agora => copy an election in Agora
		 */
	    
	    String electionTitle = "Voting Proposals for Issue: '"+issue_title+"'";
		String electionQuestion = "Which proposal is the best?";
		String electionDescription = "This election is to choose the winning proposal for the issue => "+issue_title;
		
	    
		ServiceResource election = createAgoraElection(orchestration, agoraGroup, proposalListLoomio, 
				electionTitle, electionQuestion, electionDescription, "ELECTION");
		JsonNode jsonAgoraElection = Json.parse(election.getBody());
		String electionPrettyName = jsonAgoraElection.get("pretty_name").asText();
		voting.addCampaignResource(election);
		orchestration.addResourceMappings("ELECTION", election);
		assertThat(electionPrettyName.equals(electionTitle));

		System.out.println("DEMOv2 > #6 > Created Election => "+election.getBody());
	    System.out.println("DEMOv2 > #7 > Rendered ServiceAssembly => "+Json.toJson(orchestration));
	}
	
	/**
	 * Creates a group using the group creation operation as defined by the service loomio
	 * 
	 * @param orchestration
	 * @param expectedResourceType
	 * @return
	 */
	private ServiceResource createGroupInLoomio(
				ServiceAssembly orchestration, 
				String groupName, 
				String groupDesc,
				String expectedResourceType) {

		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		Map<String, Object> bodyParamValues = new HashMap<String, Object>();
		bodyParamValues.put("name", groupName);
		bodyParamValues.put("description", groupDesc);
		bodyParamValues.put("visible_to", "public");
		bodyParamValues.put("discussion_privacy_options", "public_only");
		
		rootParamValues.put("group",bodyParamValues);
		
		ServiceOperation createLoomioGroup = 
				Composer.createOperationInstance(
						"createGroup", 
						orchestration, 
						rootParamValues, 
						expectedResourceType);
		
		ServiceResource result = Runner.execute(createLoomioGroup);
		return result;
	}	
	
	/**
	 * Creates a group using the group creation operation as defined by the service loomio
	 * 
	 * @param orchestration
	 * @param expectedResourceType
	 * @return
	 */
	private ServiceResource createGroupInLoomio(
				Service loomio, 
				String groupName, 
				String groupDesc,
				String expectedResourceType) {

		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		Map<String, Object> bodyParamValues = new HashMap<String, Object>();
		bodyParamValues.put("name", groupName);
		bodyParamValues.put("description", groupDesc);
		bodyParamValues.put("visible_to", "public");
		bodyParamValues.put("discussion_privacy_options", "public_only");
		
		rootParamValues.put("group",bodyParamValues);
		
		ServiceOperation createLoomioGroup = 
				Composer.createOperationInstance(
						"createGroup", 
						loomio, 
						rootParamValues, 
						expectedResourceType);
		
		ServiceResource result = Runner.execute(createLoomioGroup);
		return result;
	}	

	/**
	 * Creates an instance of the Operation "createDiscussion", using the specific 
	 * OperationDefinition to which it is mapped
	 * 
	 * @param orch ServiceAssembly that contains information about what service holds the definition of the operation
	 * @param expectedResourceType What's the type of the output (i.e., the type with respect to the resources 
	 * 			in the context of AppCivist, e.g., Issue, Proposal, Discussion, Comment, Election, Vote)
	 * @param string 
	 * @param disc_desc 
	 * @return
	 */
	private ServiceResource createDiscussionInLoomio(
				ServiceAssembly orch,
				ServiceResource loomioGroup,
				String title, 
				String desc,
				String expectedResourceType) {
			
		JsonNode jsonLoomioGroup = Json.parse(loomioGroup.getBody());
		String groupId = jsonLoomioGroup.get("groups").get(0).get("id").asText();
		
		//String groupId = LOOMIO_GROUP_ID;
		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		Map<String, Object> bodyParamValues = new HashMap<String, Object>();
		// TODO: Param keys should be obtained from the ServiceParameterDefinition 
		bodyParamValues.put("title", title);
		bodyParamValues.put("description", desc);
		bodyParamValues.put("group_id", groupId);
		rootParamValues.put("discussion", bodyParamValues);

		// TODO: how to manage cascading requirements? e.g., if a group in loomio is public, so must be its discussions
		bodyParamValues.put("private", "true");

		
		ServiceOperation createDiscussion = 
				Composer.createOperationInstance(
						"createDiscussion", 
						orch, 
						rootParamValues, 
						expectedResourceType);

		// STEP 4 => prepare and send service request/call
		System.out
				.println("DEMOv"+DEMO_VERSION+" > #[createDiscussionInLoomio].1 > Prepare parameters for instance of [Loomio].createDiscussion according to definition:"
						+ Json.toJson(createDiscussion.getParameters()));

		ServiceResource result = Runner.execute(createDiscussion);
		result.setType(expectedResourceType);
		return result;
	}
	
	/**
	 * Creates a motion inside a discussion of loomio
	 * @param orchestration
	 * @param loomioGroup
	 * @param discussion
	 * @param expectedResourceType
	 * @return
	 */
	private ServiceResource createLoomioProposal(ServiceAssembly orchestration,
			ServiceResource loomioGroup, ServiceResource discussion, String expectedResourceType) {

		// TODO: do we need the group here?
		JsonNode jsonLoomioGroup = Json.parse(loomioGroup.getBody());
		String groupName = jsonLoomioGroup.get("groups").get(0).get("name").asText();

		JsonNode jsonLoomioDiscussion = Json.parse(discussion.getBody());
		String discussionTitle = jsonLoomioDiscussion.get("discussions").get(0).get("title").asText();
		String discussionDescription = jsonLoomioDiscussion.get("discussions").get(0).get("description").asText();
		String discussionId = jsonLoomioDiscussion.get("discussions").get(0).get("id").asText();
		
		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		Map<String, Object> bodyParamValues = new HashMap<String, Object>();
		bodyParamValues.put("name", "FINAL PROPOSAL: "+discussionTitle);
		bodyParamValues.put("description", "The proposal is: "+discussionDescription);
		bodyParamValues.put("discussion_id", discussionId);
		bodyParamValues.put("closing_at", "2015-04-10");
		rootParamValues.put("proposal", bodyParamValues);
		
		ServiceOperation createLoomioGroup = 
				Composer.createOperationInstance(
						"createProposal", 
						orchestration, 
						rootParamValues, 
						expectedResourceType);
		
		ServiceResource result = Runner.execute(createLoomioGroup);
		return result;
	}
	
	/**
	 * Creates a motion inside a discussion of loomio
	 * @param orchestration
	 * @param loomioGroup
	 * @param discussion
	 * @param expectedResourceType
	 * @return
	 */
	private ServiceResource createLoomioProposal(Service service,
			ServiceResource loomioGroup, ServiceResource discussion, String expectedResourceType) {

		// TODO: do we need the group here?
		JsonNode jsonLoomioGroup = Json.parse(loomioGroup.getBody());
		String groupName = jsonLoomioGroup.get("groups").get(0).get("name").asText();

		JsonNode jsonLoomioDiscussion = Json.parse(discussion.getBody());
		String discussionTitle = jsonLoomioDiscussion.get("discussions").get(0).get("title").asText();
		String discussionDescription = jsonLoomioDiscussion.get("discussions").get(0).get("description").asText();
		String discussionId = jsonLoomioDiscussion.get("discussions").get(0).get("id").asText();
		
		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		Map<String, Object> bodyParamValues = new HashMap<String, Object>();
		bodyParamValues.put("name", "FINAL PROPOSAL: "+discussionTitle);
		bodyParamValues.put("description", "The proposal is: "+discussionDescription);
		bodyParamValues.put("discussion_id", discussionId);
		bodyParamValues.put("closing_at", "2015-04-10");
		rootParamValues.put("proposal", bodyParamValues);
		
		ServiceOperation createLoomioGroup = 
				Composer.createOperationInstance(
						"createProposal", 
						service, 
						rootParamValues, 
						expectedResourceType);
		
		ServiceResource result = Runner.execute(createLoomioGroup);
		return result;
	}

	/**
	 * TODO: unify AppCivist operations and separate them from the part of creating the paramValues Map
	 * 
	 * Creates a group in Agora when there is only one "createGroup" service mapped in the 
	 * orchestration
	 * 
	 * @param orchestration
	 * @param groupName
	 * @param groupDesc
	 * @param expectedResourceType
	 * @return
	 */
	private ServiceResource createGroupInAgora(ServiceAssembly orchestration,
			String groupName, String groupDesc, String expectedResourceType) {	
		Service s = orchestration.getServiceForOperation("createGroup");
		return createGroupInAgora(s, groupName, groupDesc, expectedResourceType );
	}
	
	/**
	 * Creates a group in Agora
	 * 
	 * @param orchestration
	 * @param groupName
	 * @param groupDesc
	 * @param expectedResourceType
	 * @return
	 */
	private ServiceResource createGroupInAgora(Service service,
			String groupName, String groupDesc, String expectedResourceType) {
		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		Map<String, Object> bodyParamValues = new HashMap<String, Object>();
		bodyParamValues.put("pretty_name", groupName);
		bodyParamValues.put("short_description", groupDesc);
		bodyParamValues.put("is_vote_secret", "false");
		rootParamValues.put("agora", bodyParamValues);

		ServiceOperation createAgora = Composer.createOperationInstance(
				"createGroup", service, rootParamValues,
				expectedResourceType);

		ServiceResource result = Runner.execute(createAgora);

		return result;
	}
	
	/**
	 * Creates an election using the issue as title and question and proposals as choices to the question
	 * @param orch
	 * @param agoraGroup
	 * @param proposals
	 * @param issue
	 * @param expectedResult
	 * @return
	 */
	private ServiceResource createAgoraElection(
			ServiceAssembly orch,
			ServiceResource agoraGroup, 
			List<ServiceResource> proposals, 
			ServiceResource issue,
			String expectedResult) {
		
		JsonNode issueBody = Json.parse(issue.getBody());
		String electionTitle = "Voting Proposals for Issue: '"+issueBody.get("discussions").get(0).get("id").asText()+"'";
		String electionPrettyName = "Which proposal is the best?";
		String electionDescription = "This election is to choose the winning proposal for the issue => "+issueBody.get("discussions").get(0).get("title").asText();
		return createAgoraElection(orch, agoraGroup, proposals, electionTitle, electionPrettyName, electionDescription, expectedResult);
	}

	private ServiceResource createAgoraElection(
			ServiceAssembly orch,
			ServiceResource agoraGroup, 
			List<ServiceResource> proposals, 
			String electionTitle,
			String electionQuestion, 
			String electionDescription,
			String expectedResult) {

		JsonNode jsonAgoraGroup = Json.parse(agoraGroup.getBody());
		String agoraId = jsonAgoraGroup.get("id").asText();
		
		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		Map<String, Object> bodyParamValues = new HashMap<String, Object>();

		List<Map<String, Object>> questions = new ArrayList<Map<String, Object>>();
		Map<String, Object> question = new HashMap<String, Object>();
		
		bodyParamValues.put("pretty_name",electionTitle );
		bodyParamValues.put("description", electionDescription);
		bodyParamValues.put("is_vote_secret", true);
//		bodyParamValues.put("from_date", "2014-05-01");
//		bodyParamValues.put("to_date", "2014-05-03");
		bodyParamValues.put("short_description", "Election");
		bodyParamValues.put("action", "create_election");
		
		question.put("a", "ballot/question");
		question.put("tally_type", "ONE_CHOICE");
		question.put("max", 1);
		question.put("min", 0);
		question.put("num_seats", 1);
		question.put("question", electionQuestion);
		question.put("randomize_answer_order", true);
		
		List<Map<String, Object>> answers = new ArrayList<Map<String, Object>>();
		
		for (ServiceResource proposal : proposals) {
			JsonNode jsonLoomioProposal = Json.parse(proposal.getBody());
			String proposalTitle = jsonLoomioProposal.get("proposals").get(0).get("name").asText();
			Map<String, Object> a = new HashMap<String, Object>();
			a.put("value", proposalTitle);
			a.put("a", "ballot/answer");
			a.put("url", "");
			a.put("details", "");
			answers.add(a);
		}
		
		question.put("answers", answers);
		questions.add(question);
		
		bodyParamValues.put("questions", questions);
		
		rootParamValues.put("agora_id", agoraId + "");
		rootParamValues.put("actionBody", bodyParamValues);
		
		ServiceOperation createElection = Composer
					.createOperationInstance(
							"createElection", 
							orch, 
							rootParamValues, 
							expectedResult);

		// STEP 4 => prepare and send service request/call
		System.out
				.println("DEMOv"+DEMO_VERSION+" > #[createAgoraElection].1 > Prepare parameters for instance of [Loomio].createDiscussion according to definition:"
						+ Json.toJson(createElection.getParameters()));

		ServiceResource result = Runner.execute(createElection);
		return result;
	}
	
	private ServiceResource readAgoraElection(ServiceAssembly orch, String expectedResourceType) {
		Map<String, Object> paramValues = new HashMap<String, Object>();
		paramValues.put("id",AGORA_ELECTION_ID+"");
		
		ServiceOperation readElection = Composer
					.createOperationInstance(
							"readElection", 
							orch, 
							paramValues, 
							expectedResourceType);

		// STEP 4 => prepare and send service request/call
		System.out
				.println("DEMOv"+DEMO_VERSION+" > #[readAgoraElection].1 > Prepare parameters for instance of [Loomio].createDiscussion according to definition:"
						+ Json.toJson(readElection.getParameters()));

		ServiceResource result = Runner.execute(readElection);
		result.setType(expectedResourceType);
		return result;
	}
	
	private ServiceResource createIssueInEtherpad(Service etherpad,
			ServiceIssue issueObject, String expectedResourceType) {
		return createPadInEtherpad(etherpad, issueObject.getTitle(), issueObject.getBrief(), expectedResourceType);
	}
	
	private ServiceResource createProposalInEtherpad(Service etherpad,
			String title, String text, String expectedResourceType) {
		return createPadInEtherpad(etherpad, title, text, expectedResourceType);
	}
	
	private ServiceResource createPadInEtherpad(Service etherpad,
			String title, String text, String expectedResourceType) {
		
		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		rootParamValues.put("padID",title);
		rootParamValues.put("text",title+"\n"+text);
		
		ServiceOperation createIssue = 
				Composer.createOperationInstance(
						"createIssue", 
						etherpad, 
						rootParamValues, 
						expectedResourceType);
		
		ServiceResource result = Runner.execute(createIssue);
		return result;
	}
	
	private ServiceResource readTextInEtherpad(Service etherpad,
			String padId, String expectedResourceType) {
		
		Map<String, Object> rootParamValues = new HashMap<String, Object>();
		rootParamValues.put("padID",padId);
		
		ServiceOperation createIssue = 
				Composer.createOperationInstance(
						"readIssue", 
						etherpad, 
						rootParamValues, 
						expectedResourceType);
		
		ServiceResource result = Runner.execute(createIssue);
		return result;
	}
	
}
