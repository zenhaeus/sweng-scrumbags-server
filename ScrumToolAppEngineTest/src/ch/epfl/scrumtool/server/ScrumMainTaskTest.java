package ch.epfl.scrumtool.server;

import java.util.Calendar;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
/**
 * 
 * @author aschneuw, sylb
 *
 */
public class ScrumMainTaskTest {
    private static final double DELTA = 1e8; // used to test equality between 2 long

    private static final String KEY = "key";
    private static final String NAME = "Name";
    private static final Priority PRIORITY = Priority.NORMAL;
    private static final Status STATUS = Status.READY_FOR_ESTIMATION;
    private static final long DATE = Calendar.getInstance().getTimeInMillis();
    private static final String LAST_USER = "sylvain@mock.ch";
    private static final int ISSUES_NUMBER = 4;
    private static final float ISSUES_TIME = Calendar.getInstance().getTimeInMillis();
    
    private static ScrumMainTask mainTask = new ScrumMainTask();
    
    @Test
    public void testSetGetKey() {
        mainTask.setKey(KEY);
        assertEquals(KEY, mainTask.getKey());
    }
    
    @Test
    public void testSetGetName() {
        mainTask.setName(NAME);
        assertEquals(NAME, mainTask.getName());
    }
    
    @Test
    public void testSetGetIssues() {
        HashSet<ScrumIssue> issues = new HashSet<ScrumIssue>();
        mainTask.setIssues(issues);
        assertEquals(issues, mainTask.getIssues());
    }
    
    @Test
    public void testSetGetProject() {
        ScrumProject project = new ScrumProject();
        mainTask.setProject(project);
        assertEquals(project, mainTask.getProject());
    }
    
    @Test
    public void testSetGetStatus() {
        mainTask.setStatus(STATUS);
        assertEquals(STATUS, mainTask.getStatus());
    }
    
    @Test
    public void testSetGetPriority() {
        mainTask.setPriority(PRIORITY);
        assertEquals(PRIORITY, mainTask.getPriority());
    }
    
    @Test
    public void testSetGetLastModDate() {
        mainTask.setLastModDate(DATE);
        assertEquals(DATE, mainTask.getLastModDate());
    }
    
    @Test
    public void testSetGetLastModUser() {
        mainTask.setLastModUser(LAST_USER);
        assertEquals(LAST_USER, mainTask.getLastModUser());
    }
    
    @Test
    public void testSetGetIssuesFinished() {
        mainTask.setIssuesFinished(ISSUES_NUMBER);
        assertEquals(ISSUES_NUMBER, mainTask.getIssuesFinished());
    }
    
    @Test
    public void testSetGetTotalIssues() {
        mainTask.setTotalIssues(ISSUES_NUMBER);
        assertEquals(ISSUES_NUMBER, mainTask.getTotalIssues());
    }
    
    @Test
    public void testSetGetTotalTime() {
        mainTask.setTotalTime(ISSUES_TIME);
        assertEquals(ISSUES_TIME, mainTask.getTotalTime(), DELTA);
    }
    
    @Test
    public void testSetGetTimeFinished() {
        mainTask.setTimeFinished(ISSUES_TIME);
        assertEquals(ISSUES_TIME, mainTask.getTimeFinished(), DELTA);
    }
}
