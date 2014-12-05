package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.HashSet;

import org.junit.Test;

/**
 * @author aschneuw
 * @author sylb
 * @author Cyriaque Brousse
 */
public class ScrumMainTaskTest {
    private static final String KEY = "key";
    private static final String NAME = "Name";
    private static final Priority PRIORITY = Priority.NORMAL;
    private static final Status STATUS = Status.READY_FOR_ESTIMATION;
    private static final long DATE = Calendar.getInstance().getTimeInMillis();
    private static final String LAST_USER = "sylvain@mock.ch";
    private static final int ISSUES_NUMBER = 4;
    private static final float ISSUES_TIME = Calendar.getInstance().getTimeInMillis();
    
    private static final ScrumMainTask MAIN_TASK = new ScrumMainTask();
    
    @Test
    public void testSetGetKey() {
        MAIN_TASK.setKey(KEY);
        assertEquals(KEY, MAIN_TASK.getKey());
    }
    
    @Test
    public void testSetGetName() {
        MAIN_TASK.setName(NAME);
        assertEquals(NAME, MAIN_TASK.getName());
    }
    
    @Test
    public void testSetGetIssues() {
        HashSet<ScrumIssue> issues = new HashSet<ScrumIssue>();
        MAIN_TASK.setIssues(issues);
        assertEquals(issues, MAIN_TASK.getIssues());
    }
    
    @Test
    public void testSetGetProject() {
        ScrumProject project = new ScrumProject();
        MAIN_TASK.setProject(project);
        assertEquals(project, MAIN_TASK.getProject());
    }
    
    @Test
    public void testSetGetStatus() {
        MAIN_TASK.setStatus(STATUS);
        assertEquals(STATUS, MAIN_TASK.getStatus());
    }
    
    @Test
    public void testSetGetPriority() {
        MAIN_TASK.setPriority(PRIORITY);
        assertEquals(PRIORITY, MAIN_TASK.getPriority());
    }
    
    @Test
    public void testSetGetLastModDate() {
        MAIN_TASK.setLastModDate(DATE);
        assertEquals(DATE, MAIN_TASK.getLastModDate());
    }
    
    @Test
    public void testSetGetLastModUser() {
        MAIN_TASK.setLastModUser(LAST_USER);
        assertEquals(LAST_USER, MAIN_TASK.getLastModUser());
    }
    
    @Test
    public void testSetGetIssuesFinished() {
        MAIN_TASK.setIssuesFinished(ISSUES_NUMBER);
        assertEquals(ISSUES_NUMBER, MAIN_TASK.getIssuesFinished());
    }
    
    @Test
    public void testSetGetTotalIssues() {
        MAIN_TASK.setTotalIssues(ISSUES_NUMBER);
        assertEquals(ISSUES_NUMBER, MAIN_TASK.getTotalIssues());
    }
    
    @Test
    public void testSetGetTotalTime() {
        MAIN_TASK.setTotalTime(ISSUES_TIME);
        assertEquals(Float.compare(ISSUES_TIME, MAIN_TASK.getTotalTime()), 0);
    }
    
    @Test
    public void testSetGetTimeFinished() {
        MAIN_TASK.setTimeFinished(ISSUES_TIME);
        assertEquals(Float.compare(ISSUES_TIME, MAIN_TASK.getTimeFinished()), 0);
    }
}
