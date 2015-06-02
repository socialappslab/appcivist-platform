//package models;
//
//import enums.CampaignTypesEnum;
//import enums.ContributionTypes;
//import enums.MessageType;
//import models.Contribution;
//
//import org.apache.xpath.operations.Mod;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.Computer;
//
//import play.api.test.FakeApplication;
//import play.api.test.WithApplication;
//import play.libs.F;
//import play.test.TestBrowser;
//
//import javax.persistence.Id;
//
//import java.util.Date;
//import java.util.LinkedList;
//import java.util.List;
//
//import static org.fest.assertions.Assertions.assertThat;
//import static play.test.Helpers.*;
//
//public class CoreTest extends play.test.WithApplication {
//
//	@Test
//
//    public void testCoreCreation() {
//
//        User user = new User();
//        Date creation = new Date();
//        Date removal = new Date();        
//
//        // 1. Create categories
//        Category c1 = new Category();
//        c1.setCategoryId(new Long(9999998));
//        c1.setTitle("Parks & Recreation");
//        c1.setDescription("Everything about parks and recreation");
//        Category c2 = new Category();
//        c2.setCategoryId(new Long(9999999));
//        c2.setTitle("Education");
//        c1.setDescription("Everything about education");
//        c1.save();
//        c2.save();
//        
//        // 2. Create pre-existing hashtags
//        Hashtag h1 = new Hashtag("pb2015"); h1.setHashtagId(new Long(9999998));
//        Hashtag h2 = new Hashtag("pb2015-vallejo"); h2.setHashtagId(new Long(9999999));
//        h1.save();
//        h2.save();
//        
//        // 3. Create instance of Assembly without a campaign
//        Assembly a = new Assembly();
//        a.setAssemblyId(new Long(9999999));
//        a.setCreator(user);
//        a.setLang("en_EN");
//        a.setName("Assembly Name");
//        a.setDescription("Assembly Description");
//        a.setCity("Asuncion");
//        a.setIcon("/assets/images/sfskyline-small.jpg");
//        a.getHashtags().add(h1);
//        a.getHashtags().add(h2);
//        a.getInterestCategories().add(c1);
//        a.getInterestCategories().add(c2);
//        a.save();
//        
//        // 4. Create a campaign
//        
//        // 4.1. Create some predefine phase definitions
//        PhaseDefinition pd1 = new PhaseDefinition("Proposal Making");
//        PhaseDefinition pd2 = new PhaseDefinition("Versioning");
//        PhaseDefinition pd3 = new PhaseDefinition("Deliberation");
//        PhaseDefinition pd4 = new PhaseDefinition("Voting");
//        
//        List<PhaseDefinition> defaultPhases = new LinkedList<PhaseDefinition>();
//        defaultPhases.add(pd1);
//        defaultPhases.add(pd2);
//        defaultPhases.add(pd3);
//        defaultPhases.add(pd4);
//        
//        // 4.2. create a predefined campaign type for PB
//        CampaignType ctype = new CampaignType(CampaignTypesEnum.PARTICIPATORY_BUDGETING, defaultPhases);
//        
//        ctype.save();
//        
//        Campaign c = new Campaign("Participatory Budgeting", 
//        		new Date(), new Date(), true, "", a, ctype, null);
//        
//        c.save();
//
//        a.getCampaigns().add(c);
//        a.update();
//        
//        // 5. Create instances of Contribution
//        Contribution i1 = new Contribution();
//        i1.setCreator(user);
//        i1.setCreation(creation);
//        i1.setRemoval(removal);
//        i1.setLang("en_EN");
//        i1.setContributionId(new Long(99991));
//        i1.setTitle("Issue Number 1");
//        i1.setText("Issue Brief 1");
//        i1.setType(ContributionTypes.ISSUE);
//        i1.setUpVotes(new Long(1));
//        i1.setAssembly(a);
//        i1.save();
//        
//        Contribution i2 = new Contribution();
//        i2.setCreator(user);
//        i2.setCreation(creation);
//        i2.setRemoval(removal);
//        i2.setLang("en_EN");
//        i2.setContributionId(new Long(99992));
//        i2.setTitle("Idea Number 1");
//        i2.setText("Idea Brief 1");
//        i2.setType(ContributionTypes.IDEA);
//        i2.setUpVotes(new Long(2));
//        i2.setAssembly(a);
//        i2.save();
//       
//        // 6. Create instances of Working Group
//        WorkingGroup w1 = new WorkingGroup();
//        w1.setCreator(user);
//        w1.setCreation(creation);
//        w1.setRemoval(removal);
//        w1.setLang("English");
//        w1.setGroupId(new Long(99991111));
//        w1.setName("Working Group Name 1");
//        w1.setText("Working Group Text 1");
//        w1.setExpiration(new Date());
//        w1.getAssemblies().add(a);
//        w1.save();
//        
//        WorkingGroup w2 = new WorkingGroup();
//        w2.setCreator(user);
//        w2.setCreation(creation);
//        w2.setRemoval(removal);
//        w2.setLang("English");
//        w2.setGroupId(new Long(99992222));
//        w2.setName("Working Group Name 2");
//        w2.setText("Working Group Text 2");
//        w2.setExpiration(new Date());
//        w2.getAssemblies().add(a);
//        w2.save();
//        
//        // 7. Create instances of Message
//        Message m1 = new Message();
//        m1.setCreator(user);
//        m1.setCreation(creation);
//        m1.setRemoval(removal);
//        m1.setLang("English");
//        m1.setMessageId(new Long(9999111));
//        m1.setTitle("Message Title 1");
//        m1.setText("Message Text 1");
//        m1.setType(MessageType.ANNOUNCEMENT);
//        m1.setTargetAssembly(a);
//        m1.save();
//        
//        Message m2 = new Message();
//        m2.setCreator(user);
//        m2.setCreation(creation);
//        m2.setRemoval(removal);
//        m2.setLang("English");
//        m2.setMessageId(new Long(9999222));
//        m2.setTitle("Message Title 2");
//        m2.setText("Message Text 2");
//        m2.setType(MessageType.GROUPMAIL);
//        m2.setTargetWorkingGroup(w1);
//        m2.save();
//      
//
//        // 4. Read all the instance from db and assert it exists and it has issues
//        Assembly a1 = Assembly.read(new Long(9999999));
//        assertThat(a1 != null);
//        assertThat(a1.getWorkingGroups().size() > 0);
//        assertThat(a1.getCampaigns().size()>0);
//        assertThat(a1.getHashtags().size()>0);
//        assertThat(a1.getCampaigns().get(0).getPhases().size()==4);
//        assertThat(a1.getCampaigns().get(0).getPhases().get(3).getDefinition().getName().equals("Voting"));
//        
//        Contribution i11 = Contribution.read(new Long(99991));
//        assertThat(i11 != null);
//        Contribution i22 = Contribution.read(new Long(99992));
//        assertThat(i22 != null);
//
//        Message m11 = Message.read(new Long(9999111));
//        assertThat(m11 != null);
//        Message m22 =  Message.read(new Long(9999222));
//        assertThat(m22 != null);
//
//        WorkingGroup w11 = WorkingGroup.read(new Long(99991111));
//        assertThat(w11 != null);
//        WorkingGroup w22 = WorkingGroup.read(new Long(99992222));
//        assertThat(w22 != null);
//
//        
//        // 5. Delete created data
//        a.delete();
//
//        // 6. Verify data was deleted
//        a1 = Assembly.read(new Long(9999999));
//        assertThat(a1 == null);
//        
//        // 7. Verify if all the rest of the data remain
//        i11 = Contribution.read(new Long(99991));
//        assertThat(i11 != null);
//        i22 = Contribution.read(new Long(99992));
//        assertThat(i22 != null);
//
//        m11 = Message.read(new Long(9999111));
//        assertThat(m11 != null);
//        m22 =  Message.read(new Long(9999222));
//        assertThat(m22 != null);
//
//        w11 = WorkingGroup.read(new Long(99991111));
//        assertThat(w11 != null);
//        w22 = WorkingGroup.read(new Long(99992222));
//        assertThat(w22 != null);
//
//       
//       
//
//        // 8. Delete everything
//        i11.delete();
//        i22.delete();
//        m11.delete();
//        m22.delete();
//        w11.delete();
//        w22.delete();
//    }
//}
