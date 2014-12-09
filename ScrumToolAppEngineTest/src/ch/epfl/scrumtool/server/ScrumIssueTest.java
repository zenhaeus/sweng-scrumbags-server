package ch.epfl.scrumtool.server;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Cyriaque Brousse
 */
public class ScrumIssueTest {
    
    private static final float POSITIVE_NBR = 42f;
    
    private static final String KEY = "gfsgfds5454gfes";
    private static final String NAME = "Issue name";
    private static final String DESCRIPTION = "Issue description";
    private static final float ESTIMATION_NONE = 0f;
    private static final float ESTIMATION_SOME = 5f;
    private static final ScrumMainTask MAIN_TASK = new ScrumMainTask();
    private static final ScrumPlayer PLAYER_NONE = null;
    private static final ScrumPlayer PLAYER_SOME = new ScrumPlayer();
    private static final Status STATUS_EST_NONE = Status.READY_FOR_ESTIMATION;
    private static final Status STATUS_EST_SOME = Status.READY_FOR_SPRINT;
    private static final Priority PRIORITY = Priority.NORMAL;
    private static final ScrumSprint SPRINT_NONE = null;
    private static final ScrumSprint SPRINT_SOME = new ScrumSprint();
    private static final long LAST_MOD_DATE = 0L;
    private static final String LAST_MOD_USER = "some@example.com";

    private static final ScrumIssue ISSUE = new ScrumIssue();
    
    @Before
    public void setup() {
        ISSUE.setAssignedPlayer(PLAYER_NONE);
        ISSUE.setDescription(DESCRIPTION);
        ISSUE.setKey(KEY);
        ISSUE.setEstimation(ESTIMATION_NONE);
        ISSUE.setLastModDate(LAST_MOD_DATE);
        ISSUE.setLastModUser(LAST_MOD_USER);
        MAIN_TASK.setKey("task_key");
        ISSUE.setMainTask(MAIN_TASK);
        ISSUE.setName(NAME);
        ISSUE.setPriority(PRIORITY);
        ISSUE.setSprint(SPRINT_NONE);
        ISSUE.setStatus(STATUS_EST_NONE);
    }
    
    @Test
    public void testSetPriority() {
        ISSUE.setPriority(Priority.URGENT);
        assertEquals(Priority.URGENT, ISSUE.getPriority());
    }

    @Test
    public void testGetPriority() {
        ISSUE.setPriority(PRIORITY);
        assertEquals(PRIORITY, ISSUE.getPriority());
    }

    @Test
    public void testSetSprint() {
        ISSUE.setSprint(SPRINT_SOME);
        assertEquals(SPRINT_SOME, ISSUE.getSprint());
    }

    @Test
    public void testGetSprint() {
        ISSUE.setSprint(SPRINT_NONE);
        assertEquals(SPRINT_NONE, ISSUE.getSprint());
    }

    @Test
    public void testGetKey() {
        assertEquals(KEY, ISSUE.getKey());
    }

    @Test
    public void testSetKey() {
        String key2 = "otherKey";
        ISSUE.setKey(key2);
        assertEquals(key2, ISSUE.getKey());
        ISSUE.setKey(KEY);
    }

    @Test
    public void testGetName() {
        assertEquals(NAME, ISSUE.getName());
    }

    @Test
    public void testSetName() {
        String name2 = "otherName";
        ISSUE.setName(name2);
        assertEquals(name2, ISSUE.getName());
        ISSUE.setName(NAME);
    }

    @Test
    public void testGetDescription() {
        assertEquals(DESCRIPTION, ISSUE.getDescription());
    }

    @Test
    public void testSetDescription() {
        String desc2 = "otherDesc";
        ISSUE.setDescription(desc2);
        assertEquals(desc2, ISSUE.getDescription());
        ISSUE.setDescription(DESCRIPTION);
    }

    @Test
    public void testGetEstimation() {
        assertEquals(ESTIMATION_NONE, ISSUE.getEstimation(), 0);
    }

    @Test
    public void testSetEstimation() {
        ISSUE.setEstimation(ESTIMATION_SOME);
        assertEquals(ESTIMATION_SOME, ISSUE.getEstimation(), 0);
        ISSUE.setEstimation(ESTIMATION_NONE);
    }

    @Test
    public void testGetAssignedPlayer() {
        assertEquals(PLAYER_NONE, ISSUE.getAssignedPlayer());
    }

    @Test
    public void testSetAssignedPlayer() {
        ISSUE.setAssignedPlayer(PLAYER_SOME);
        assertEquals(PLAYER_SOME, ISSUE.getAssignedPlayer());
        ISSUE.setAssignedPlayer(PLAYER_NONE);
    }

    @Test
    public void testGetStatus() {
        assertEquals(STATUS_EST_NONE, ISSUE.getStatus());
    }

    @Test
    public void testSetStatus() {
        ISSUE.setStatus(STATUS_EST_SOME);
        assertEquals(STATUS_EST_SOME, ISSUE.getStatus());
        ISSUE.setStatus(STATUS_EST_NONE);
    }

    @Test
    public void testGetLastModDate() {
        assertEquals(LAST_MOD_DATE, ISSUE.getLastModDate());
    }

    @Test
    public void testSetLastModDate() {
        long now = new Date().getTime();
        ISSUE.setLastModDate(now);
        assertEquals(now, ISSUE.getLastModDate());
        ISSUE.setLastModDate(LAST_MOD_DATE);
    }

    @Test
    public void testGetLastModUser() {
        assertEquals(LAST_MOD_USER, ISSUE.getLastModUser());
    }

    @Test
    public void testSetLastModUser() {
        String otherUser = "other@example.com";
        ISSUE.setLastModUser(otherUser);
        assertEquals(otherUser, ISSUE.getLastModUser());
        ISSUE.setLastModUser(LAST_MOD_USER);
    }

    @Test
    public void testGetMainTask() {
        assertEquals(MAIN_TASK.getKey(), ISSUE.getMainTask().getKey());
    }

    @Test
    public void testSetMainTask() {
        ScrumMainTask other = new ScrumMainTask();
        other.setKey("other_key");
        ISSUE.setMainTask(other);
        assertEquals(other.getKey(), ISSUE.getMainTask().getKey());
        ISSUE.setMainTask(MAIN_TASK);
    }
    
    @Test
    public void testVerifyAndSetStatusFinished() {
        ScrumIssue issue = newIssueWithStatus(Status.FINISHED);
        assertFalse(issue.verifyAndSetStatus());
    }

    @Test
    public void testVerifyAndSetStatusReadyEstimation() {
        ScrumIssue issue = newIssueWithStatusAndEstimation(null, 0f);
        if (issue.verifyAndSetStatus()) {
            assertEquals(Status.READY_FOR_ESTIMATION, issue.getStatus());
        } else {
            fail("Should have changed status to READY_FOR_ESTIMATION");
        }
        
        // now calling again verifyAndSetStatus should not change anything
        assertFalse(issue.verifyAndSetStatus());
    }
    
    @Test
    public void testVerifyAndSetStatusReadySprint() {
        ScrumIssue issue = newIssueWithStatusAndEstimation(null, POSITIVE_NBR);
        if (issue.verifyAndSetStatus()) {
            assertEquals(Status.READY_FOR_SPRINT, issue.getStatus());
        } else {
            fail("Should have changed status to READY_FOR_SPRINT");
        }
        
        // now calling again verifyAndSetStatus should not change anything
        assertFalse(issue.verifyAndSetStatus());
    }
    
    @Test
    public void testVerifyAndSetStatusInSprintWithEstimation() {
        ScrumIssue issue = newIssueWithStatusAndEstimation(null, POSITIVE_NBR);
        issue.setSprint(new ScrumSprint());
        if (issue.verifyAndSetStatus()) {
            assertEquals(Status.IN_SPRINT, issue.getStatus());
        } else {
            fail("Should have changed status to IN_SPRINT");
        }
        
        // now calling again verifyAndSetStatus should not change anything
        assertFalse(issue.verifyAndSetStatus());
    }
    
    @Test
    public void testVerifyAndSetStatusInSprintWithoutEstimation() {
        ScrumIssue issue = newIssueWithStatusAndEstimation(null, 0f);
        issue.setSprint(new ScrumSprint());
        if (issue.verifyAndSetStatus()) {
            assertEquals(Status.READY_FOR_ESTIMATION, issue.getStatus());
        } else {
            fail("Should have changed status to READY_FOR_ESTIMATION");
        }
        
        // now calling again verifyAndSetStatus should not change anything
        assertFalse(issue.verifyAndSetStatus());
    }

    private ScrumIssue newIssueWithStatus(Status status) {
        ScrumIssue issue = new ScrumIssue();
        issue.setStatus(status);
        return issue;
    }

    private ScrumIssue newIssueWithStatusAndEstimation(Status status, float estimation) {
        ScrumIssue issue = newIssueWithStatus(status);
        issue.setEstimation(estimation);
        return issue;
    }
}
