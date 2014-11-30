package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashSet;

import org.junit.Test;

public class ScrumPlayerTest {
    private static final String KEY = "ThisIsAKey";
    private static final Role ROLE = Role.SCRUM_MASTER;
    private static final boolean IS_ADMIN = true;
    private static final long DATE = Calendar.getInstance().getTimeInMillis();
    private static final String LAST_USER = "example@mock.ch";

    private static ScrumPlayer player = new ScrumPlayer();

    @Test
    public void testSetGetKey() {
        player.setKey(KEY);
        assertEquals(KEY, player.getKey());
    }
    
    @Test
    public void testSetGetRole() {
        player.setRole(ROLE);
        assertEquals(ROLE, player.getRole());
    }

    @Test
    public void testSetGetAdminFlag() {
        player.setAdminFlag(IS_ADMIN);
        assertEquals(IS_ADMIN, player.getAdminFlag());
    }

    @Test
    public void testSetGetLastModDate() {
        player.setLastModDate(DATE);
        assertEquals(DATE, player.getLastModDate());
    }

    @Test
    public void testSetGetLastModUser() {
        player.setLastModUser(LAST_USER);
        assertEquals(LAST_USER, player.getLastModUser());
    }

    @Test
    public void testSetGetProject() {
        ScrumProject project = new ScrumProject();
        player.setProject(project);
        assertEquals(project, player.getProject());
    }

    @Test
    public void testSetGetUser() {
        ScrumUser user = new ScrumUser();
        player.setUser(user);
        assertEquals(user, player.getUser());
    }
    
    @Test
    public void testSetGetIssues() {
        HashSet<ScrumIssue> issues = new HashSet<ScrumIssue>();
        player.setIssues(issues);
        assertEquals(issues, player.getIssues());
    }

    @Test
    public void testAddRemoveIssue() {
        ScrumIssue issue = new ScrumIssue();
        player.addIssue(issue);
        assertEquals(player.getIssues().size(), 1);
        assertTrue(player.getIssues().contains(issue));
        player.removeIssue(issue);
        assertEquals(player.getIssues().size(), 0);
        assertFalse(player.getIssues().contains(issue));
    }
}
