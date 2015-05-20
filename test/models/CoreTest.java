package models;


import enums.MessageType;
import org.apache.xpath.operations.Mod;
import org.junit.Ignore;
import org.junit.Test;
import play.api.test.WithApplication;

import javax.persistence.Id;
import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;

public class CoreTest{

    @Test
    public void testCoreCreation() {

        User user = new User();
        Date creation = new Date();
        Date removal = new Date();

        // 1. Create instances of Issue
        Issue i1 = new Issue();
        i1.setCreator(user);
        i1.setCreation(creation);
        i1.setRemoval(removal);
        i1.setLang("English");
        i1.setIssueId(new Long(1));
        i1.setTitle("Issue Number 1");
        i1.setBrief("Issue Brief 1");
        i1.setType("Issue Type 1");
        i1.setLikes(new Long(1));

        Issue i2 = new Issue();
        i2.setCreator(user);
        i2.setCreation(creation);
        i2.setRemoval(removal);
        i2.setLang("English");
        i2.setIssueId(new Long(2));
        i2.setTitle("Issue Number 2");
        i2.setBrief("Issue Brief 2");
        i2.setType("Issue Type 2");
        i2.setLikes(new Long(2));

        // 2. Create instances of Organization
        Organization o1 = new Organization();
        o1.setCreator(user);
        o1.setCreation(creation);
        o1.setRemoval(removal);
        o1.setLang("English");
        o1.setOrgId(new Long(11));
        o1.setName("Organization Name 1");
        o1.setName("Organization Description 1");
        o1.setAddress("Organization Address 1");

        Organization o2 = new Organization();
        o2.setCreator(user);
        o2.setCreation(creation);
        o2.setRemoval(removal);
        o2.setLang("English");
        o2.setOrgId(new Long(22));
        o2.setName("Organization Name 2");
        o2.setName("Organization Description 2");
        o2.setAddress("Organization Address 2");

        // 3. Create instances of Message
        Message m1 = new Message();
        m1.setCreator(user);
        m1.setCreation(creation);
        m1.setRemoval(removal);
        m1.setLang("English");
        m1.setMessageId(new Long(111));
        m1.setTitle("Message Title 1");
        m1.setText("Message Text 1");
        m1.setType(MessageType.ANNOUNCEMENT);

        Message m2 = new Message();
        m2.setCreator(user);
        m2.setCreation(creation);
        m2.setRemoval(removal);
        m2.setLang("English");
        m2.setMessageId(new Long(222));
        m2.setTitle("Message Title 2");
        m2.setText("Message Text 2");
        m2.setType(MessageType.GROUPMAIL);

        // 4. Create instances of Working Group

        WorkingGroup w1 = new WorkingGroup();
        w1.setCreator(user);
        w1.setCreation(creation);
        w1.setRemoval(removal);
        w1.setLang("English");
        w1.setGroupId(new Long(1111));
        w1.setName("Working Group Name 1");
        w1.setText("Working Group Text 1");
        w1.setExpiration(new Date());

        WorkingGroup w2 = new WorkingGroup();
        w2.setCreator(user);
        w2.setCreation(creation);
        w2.setRemoval(removal);
        w2.setLang("English");
        w2.setGroupId(new Long(2222));
        w2.setName("Working Group Name 2");
        w2.setText("Working Group Text 2");
        w2.setExpiration(new Date());

        // 5. Create instances of Module
        Module mod1 = new Module();
        mod1.setCreator(user);
        mod1.setCreation(creation);
        mod1.setRemoval(removal);
        mod1.setLang("English");
        mod1.setModId(new Long(11111));
        mod1.setName("Module Name 1");
        mod1.setConfiguration(new Config());

        Module mod2 = new Module();
        mod2.setCreator(user);
        mod2.setCreation(creation);
        mod2.setRemoval(removal);
        mod2.setLang("English");
        mod2.setModId(new Long(22222));
        mod2.setName("Module Name 2");
        mod2.setConfiguration(new Config());

        // 6. Create instances of Phase
        Phase p1 = new Phase();
        p1.setCreator(user);
        p1.setCreation(creation);
        p1.setRemoval(removal);
        p1.setLang("English");
        p1.setPhaseId(new Long(111111));
        p1.setStart_date(new Date());
        p1.setEnd_date(new Date());
        p1.setUpdate(new Date());
        p1.setName("Phase Name 1");

        Phase p2 = new Phase();
        p2.setCreator(user);
        p2.setCreation(creation);
        p2.setRemoval(removal);
        p2.setLang("English");
        p2.setPhaseId(new Long(222222));
        p2.setStart_date(new Date());
        p2.setEnd_date(new Date());
        p2.setUpdate(new Date());
        p2.setName("Phase Name 2");

        // 7. Create instance of Assembly
        Assembly a = new Assembly();
        a.setCreator(user);
        a.setCreation(creation);
        a.setRemoval(removal);
        a.setLang("English");
        a.setAssemblyId(new Long(999));
        a.setName("Assembly Name");
        a.setDescription("Assembly Description");
        a.setCity("Asuncion");
        a.setIcon("/assets/images/sfskyline-small.jpg");
        a.setUrl("/api/assembly/4");

        a.getIssues().add(i1);
        a.getIssues().add(i2);
        a.getOrganizations().add(o1);
        a.getOrganizations().add(o2);
        a.getMessages().add(m1);
        a.getMessages().add(m2);
        a.getWorkingGroups().add(w1);
        a.getWorkingGroups().add(w2);
        a.getModules().add(mod1);
        a.getModules().add(mod2);
        a.getPhases().add(p1);
        a.getPhases().add(p2);

        // 3. Save Everything
        i1.save();
        i2.save();
        o1.save();
        o2.save();
        m1.save();
        m2.save();
        w1.save();
        w2.save();
        mod1.save();
        mod2.save();
        p1.save();
        p2.save();
        a.save();

        // 4. Read all the instance from db and assert it exists and it has issues
        Assembly a1 = Assembly.read(new Long(999));
        assertThat(a1 != null);
        assertThat(a1.getIssues().size() > 0);
        assertThat(a1.getOrganizations().size() > 0);
        assertThat(a1.getMessages().size() > 0);
        assertThat(a1.getWorkingGroups().size() > 0);
        assertThat(a1.getModules().size() > 0);
        assertThat(a1.getPhases().size() > 0);

        Issue i11 = Issue.read(new Long(1));
        assertThat(i11 != null);
        Issue i22 = Issue.read(new Long(2));
        assertThat(i22 != null);

        Organization o11 = Organization.read(new Long(11));
        assertThat(o11 != null);
        Organization o22 = Organization.read(new Long(22));
        assertThat(o22 != null);

        Message m11 = Message.read(new Long(111));
        assertThat(m11 != null);
        Message m22 =  Message.read(new Long(222));
        assertThat(m22 != null);

        WorkingGroup w11 = WorkingGroup.read(new Long(1111));
        assertThat(w11 != null);
        WorkingGroup w22 = WorkingGroup.read(new Long(2222));
        assertThat(w22 != null);

        Module mod11 = Module.read(new Long(11111));
        assertThat(mod11 != null);
        Module mod22 = Module.read(new Long(22222));
        assertThat(mod22 != null);

        Phase p11 = Phase.read(new Long(111111));
        assertThat(p11 != null);
        Phase p22 = Phase.read(new Long(222222));
        assertThat(p22 != null);

        // 5. Delete created data
        a.delete();

        // 6. Verify data was deleted
        a1 = Assembly.read(new Long(999));
        assertThat(a1 == null);
        assertThat(a1.getIssues().size() == 0);

        // 7. Verify if all the rest of the data remain
        i11 = Issue.read(new Long(1));
        assertThat(i11 != null);
        i22 = Issue.read(new Long(2));
        assertThat(i22 != null);

        o11 = Organization.read(new Long(11));
        assertThat(o11 != null);
        o22 = Organization.read(new Long(22));
        assertThat(o22 != null);

        m11 = Message.read(new Long(111));
        assertThat(m11 != null);
        m22 =  Message.read(new Long(222));
        assertThat(m22 != null);

        w11 = WorkingGroup.read(new Long(1111));
        assertThat(w11 != null);
        w22 = WorkingGroup.read(new Long(2222));
        assertThat(w22 != null);

        mod11 = Module.read(new Long(11111));
        assertThat(mod11 != null);
        mod22 = Module.read(new Long(22222));
        assertThat(mod22 != null);

        p11 = Phase.read(new Long(111111));
        assertThat(p11 != null);
        p22 = Phase.read(new Long(222222));
        assertThat(p22 != null);
    }

}
