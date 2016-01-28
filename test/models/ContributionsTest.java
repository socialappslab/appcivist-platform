package models;

import static org.junit.Assert.*;
import play.test.WithApplication;

import org.junit.Test;

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
}
