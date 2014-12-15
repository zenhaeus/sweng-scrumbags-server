package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

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
    private static final String LAST_USER = "some@example.com";
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
    
    @Test
    public void testVerifyAndSetStatusFinished() {
        ScrumMainTask task = newTaskWithStatus(null);
        addIssueWithStatusToTask(Status.FINISHED, task);
        addIssueWithStatusToTask(Status.FINISHED, task);
        
        if (task.verifyAndSetStatusWithRespectToIssues()) {
            assertEquals(Status.FINISHED, task.getStatus());
        } else {
            fail("Should have changed status to FINISHED");
        }
        
        // calling again should not yield any changes
        assertFalse(task.verifyAndSetStatusWithRespectToIssues());
    }
    
    @Test
    public void testVerifyAndSetStatusReadySprint() {
        ScrumMainTask task = newTaskWithStatus(null);
        addIssueWithStatusToTask(Status.READY_FOR_SPRINT, task);
        addIssueWithStatusToTask(Status.READY_FOR_SPRINT, task);
        
        if (task.verifyAndSetStatusWithRespectToIssues()) {
            assertEquals(Status.READY_FOR_SPRINT, task.getStatus());
        } else {
            fail("Should have changed status to READY_FOR_SPRINT");
        }
        
        // calling again should not yield any changes
        assertFalse(task.verifyAndSetStatusWithRespectToIssues());
    }
    
    @Test
    public void testVerifyAndSetStatusInSprint() {
        ScrumMainTask task = newTaskWithStatus(null);
        addIssueWithStatusToTask(Status.IN_SPRINT, task);
        addIssueWithStatusToTask(Status.READY_FOR_SPRINT, task);
        addIssueWithStatusToTask(Status.READY_FOR_SPRINT, task);
        
        if (task.verifyAndSetStatusWithRespectToIssues()) {
            assertEquals(Status.IN_SPRINT, task.getStatus());
        } else {
            fail("Should have changed status to IN_SPRINT");
        }
        
        // calling again should not yield any changes
        assertFalse(task.verifyAndSetStatusWithRespectToIssues());
    }
    
    @Test
    public void testVerifyAndSetStatusReadyEstimation() {
        ScrumMainTask task = newTaskWithStatus(null);
        addIssueWithStatusToTask(Status.IN_SPRINT, task);
        addIssueWithStatusToTask(Status.READY_FOR_SPRINT, task);
        addIssueWithStatusToTask(Status.READY_FOR_ESTIMATION, task);
        
        if (task.verifyAndSetStatusWithRespectToIssues()) {
            assertEquals(Status.READY_FOR_ESTIMATION, task.getStatus());
        } else {
            fail("Should have changed status to READY_FOR_ESTIMATION");
        }
        
        // calling again should not yield any changes
        assertFalse(task.verifyAndSetStatusWithRespectToIssues());
    }
    
    @Test
    public void testVerifyAndSetStatusFinishedAndOthers() {
        ScrumMainTask task = newTaskWithStatus(null);
        addIssueWithStatusToTask(Status.FINISHED, task);
        addIssueWithStatusToTask(Status.IN_SPRINT, task);
        
        if (task.verifyAndSetStatusWithRespectToIssues()) {
            assertEquals(Status.IN_SPRINT, task.getStatus());
        } else {
            fail("Should have changed status to IN_SPRINT");
        }
        
     // calling again should not yield any changes
        assertFalse(task.verifyAndSetStatusWithRespectToIssues());
    }
    
    @Test
    public void testVerifyAndSetStatusEmptyIssueSet() {
        ScrumMainTask task = newTaskWithStatus(null);
        
        if (task.verifyAndSetStatusWithRespectToIssues()) {
            assertEquals(Status.READY_FOR_ESTIMATION, task.getStatus());
        } else {
            fail("Should have changed status to READY_FOR_ESTIMATION");
        }
        
        // calling again should not yield any changes
        assertFalse(task.verifyAndSetStatusWithRespectToIssues());
    }

    private ScrumMainTask newTaskWithStatus(Status status) {
        ScrumMainTask t = new ScrumMainTask();
        t.setStatus(status);
        return t;
    }
    
    private void addIssueWithStatusToTask(Status status, ScrumMainTask task) {
        ScrumIssue issue = new ScrumIssue();
        issue.setStatus(status);
        task.addIssue(issue);
    }
}
