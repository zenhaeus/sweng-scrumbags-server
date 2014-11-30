package ch.epfl.scrumtool.server;

import static org.junit.Assert.fail;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.server.spi.ServiceException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class ScrumIssueEndpointTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
            .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final UserService userService = UserServiceFactory.getUserService();

    private static final int PERCENTAGE = 100;
    private static final String AUTH_DOMAIN = "epfl.ch";
    private static final ScrumSprintEndpoint SPRINT_ENDPOINT = new ScrumSprintEndpoint();
    private static final ScrumPlayerEndpoint PLAYER_ENDPOINT = new ScrumPlayerEndpoint();
    private static final ScrumUserEndpoint USER_ENDPOINT = new ScrumUserEndpoint();
    private static final ScrumIssueEndpoint ISSUE_ENDPOINT = new ScrumIssueEndpoint();

    private static final String USER_KEY = "vincent.debieux@gmail.com";
    private static final String USER2_KEY = "joeyzenh@gmail.com";

    private ScrumMainTask maintask;

    @Before
    public void setUp() throws Exception {
        maintask = new ScrumMainTask();
        maintask.setDescription("description");
        maintask.setLastModDate(Calendar.getInstance().getTimeInMillis());
        maintask.setName("MainTask");
        maintask.setLastModUser(USER_KEY);
        maintask.setPriority(Priority.NORMAL);
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    // LoadIssuesForUser tests
    @Test
    public void testLoadIssuesForUser() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesForNonExistingUser() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesForNullUser() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesForUserNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // LoadIssuesByMaintask tests
    @Test
    public void testLoadIssuesByMainTask() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesByNonExistingMainTask() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesByNullMainTask() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesByMainTaskNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // LoadIssuesBySprint tests
    @Test
    public void testLoadIssuesBySprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesByNonExistingSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesByNullSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadIssuesBySprintNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // LoadUnsprintedIssues tests
    @Test
    public void testLoadUnsprintedIssuesExistingProject() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadUnsprintedIssuesNonExistingProject() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadUnsprintedIssuesNullProject() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadUnsprintedIssuesNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // Insert Issue tests
    @Test
    public void testInsertIssue() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueWithPlayer() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueWithSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueWithPlayerWithSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertNullIssue() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueNullMaintask() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueNonExistingMaintask() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // Insert Issue in sprint tests
    @Test
    public void testInsertExistingIssueInExistingSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertNonExistingIssueInSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertNullIssueInSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueInNonExistingSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueInNullSprint() {
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertIssueInSprintNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // Update Issue tests
    @Test
    public void testUpdateExistingIssue() {
        fail("Not yet Implemented");
        //without player and sprint
        //with sprint
        //with player
        //with player and sprint
    }

    @Test
    public void testUpdateNonExistingIssue() {
        fail("Not yet Implemented");
    }

    @Test
    public void testUpdateNullIssue() {
        fail("Not yet Implemented");
    }

    @Test
    public void testUpdateIssueNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // Remove Issue tests
    @Test
    public void testRemoveExistingIssue() {
        fail("Not yet Implemented");
    }

    @Test
    public void testRemoveNullIssue() {
        fail("Not yet Implemented");
    }

    @Test
    public void testRemoveNonExistingIssue() {
        fail("Not yet Implemented");
    }

    @Test
    public void testRemoveIssueNotLoggedIn() {
        fail("Not yet Implemented");
    }

    // Remove issues from sprint tests
    @Test
    public void testRemoveExistingIssueFromExistingSprint() {
        fail("Not yet Implemented");
    }

    @Test
    public void testRemoveIssueFromNullSprint() {
        fail("Not yet Implemented");
    }

    @Test
    public void testRemoveNullIssueFromSprint() {
        fail("Not yet Implemented");
    }

    @Test
    public void testRemoveNonExistingIssueFromSprint() {
        fail("Not yet Implemented");
    }

    public void testRemoveIssueFromSprintNotLoggedIn() {
        fail("Not yet Implemented");
    }

    private ScrumUser loginUser(String email) throws ServiceException {
        return USER_ENDPOINT.loginUser(email);
    }

    private User userLoggedIn() {
        helper.setEnvEmail(USER_KEY);
        helper.setEnvAuthDomain(AUTH_DOMAIN);
        helper.setEnvIsLoggedIn(true);
        return userService.getCurrentUser();
    }

    private User userNotLoggedIn() {
        helper.setEnvEmail(USER_KEY);
        helper.setEnvAuthDomain(AUTH_DOMAIN);
        helper.setEnvIsLoggedIn(false);
        return userService.getCurrentUser();
    }
}
