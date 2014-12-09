package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.HashSet;

import org.junit.Test;

public class ScrumSprintTest {
    private static final String KEY = "key";
    private static final String TITLE = "week 1";
    private static final long DEADLINE = Calendar.getInstance().getTimeInMillis()+1000;
    private static final long LAST_DATE = Calendar.getInstance().getTimeInMillis();
    private static final String LAST_USER = "example@mock.ch";

    private static ScrumSprint sprint = new ScrumSprint(); 
    
    @Test
    public void testSetGetKey() {
        sprint.setKey(KEY);
        assertEquals(KEY, sprint.getKey());
    }
    
    @Test
    public void testSetGetTitle() {
        sprint.setTitle(TITLE);
        assertEquals(TITLE, sprint.getTitle());
    }
    
    @Test
    public void testSetGetDeadline() {
        sprint.setDate(DEADLINE);
        assertEquals(DEADLINE, sprint.getDate());
    }
    
    @Test
    public void testSetGetLastModDate() {
        sprint.setLastModDate(LAST_DATE);
        assertEquals(LAST_DATE, sprint.getLastModDate());
    }
    
    @Test
    public void testSetGetLastModUser() {
        sprint.setLastModUser(LAST_USER);
        assertEquals(LAST_USER, sprint.getLastModUser());
    }
    
    @Test
    public void testSetGetIssues() {
        HashSet<ScrumIssue> issues = new HashSet<ScrumIssue>();
        sprint.setIssues(issues);
        assertEquals(issues, sprint.getIssues());
    }
    
    @Test
    public void testSetGetProject() {
        ScrumProject project = new ScrumProject();
        sprint.setProject(project);
        assertEquals(project, sprint.getProject());
    }
    
    @Test
    public void testAddRemoveIssue() {
        ScrumIssue issue = new ScrumIssue();
        sprint.addIssue(issue);
        assertEquals(1, sprint.getIssues().size());
        assertTrue(sprint.getIssues().contains(issue));
        sprint.removeIssue(issue);
        assertEquals(0, sprint.getIssues().size());
        assertFalse(sprint.getIssues().contains(issue));
    }
}
