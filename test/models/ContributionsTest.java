package models;

import static org.junit.Assert.*;

import java.util.List;

import play.libs.F.Promise;
import play.test.WithApplication;

import org.junit.Test;

import delegates.ContributionsDelegate;
import enums.ContributionTypes;

public class ContributionsTest extends WithApplication {
	
	@Test
    public void testUpdateContribution() {
		Contribution c = Contribution.read(new Long(1));
		System.out.println("Contribution: "+c.getTitle());
		String oldTitle = c.getTitle();
		c.setTitle("[NEWTITLE]"+c.getTitle());
		c.update();
		c.refresh();
		System.out.println("Contribution: "+c.getTitle());
		assertTrue(!c.getTitle().equals(oldTitle));
		c.setTitle(oldTitle);
		c.update();
		c.refresh();
		System.out.println("Contribution: "+c.getTitle());
		assertTrue(c.getTitle().equals(oldTitle));
    }
	
	@Test
	public void testCreateContribution() {
		User u = User.findByUserId(1l);
		Contribution c = Contribution.create(u, "Test Create Contribution", "testing", ContributionTypes.DISCUSSION);
		c.update();
		c.refresh();
		System.out.println("Contribution: " + c.getTitle());
		assertTrue(c.getTitle().equals("Test Create Contribution"));		
	}
	
	@Test
	public void testUpdateCommentCounters() {
		System.out.println("Update Comment Counters");
		List<Contribution> contributions = Contribution.findAllByContainingSpace(74l);
        
		for (Contribution c: contributions){
    		ContributionsDelegate.resetParentCommentCountersToZero(c);
    		ContributionsDelegate.resetChildrenCommentCountersToZero(c);
		}

	}
}
