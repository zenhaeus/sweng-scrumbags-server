package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.org.codehaus.jackson.sym.Name;
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
    private static final ScrumProjectEndpoint PROJECT_ENDPOINT = new ScrumProjectEndpoint();
    private static final ScrumMainTaskEndpoint TASK_ENDPOINT = new ScrumMainTaskEndpoint();

    private static final String USER_KEY = "vincent.debieux@gmail.com";
    private static final String USER2_KEY = "joeyzenh@gmail.com";
    private static final String TITLE = "issue title";
    private static final String DESCRIPTION = "issue description";
    private static final long TIME = Calendar.getInstance().getTimeInMillis();
    private static final Status STATUS = Status.READY_FOR_SPRINT;
    private static final Priority PRIORITY = Priority.HIGH;
    private static final double DELTA = 1e8; // used to test equality between 2 long
    private static final String ROLE = Role.DEVELOPER.name();

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
    
    //TODO assign SPRINT and PLAYER to test LOADs

    // LoadIssuesForUser tests
    @Test
    public void testLoadIssuesForUser() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setName("Name");
        project.setDescription("Description");
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        String playerKey = project.getPlayers().iterator().next().getKey();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn());
        HashSet<ScrumIssue> issues =  (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadIssuesForUser(USER_KEY, userLoggedIn()).getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
    }
    
    @Test(expected = NotFoundException.class)
    public void testLoadIssuesForNonExistingUser() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesForUser("non-existing", userLoggedIn()).getItems();
        fail("Should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testLoadIssuesForNullUser() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesForUser(null, userLoggedIn()).getItems();
        fail("Should have thrown NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testLoadIssuesForUserNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesForUser(USER_KEY, userNotLoggedIn()).getItems();
        fail("Should have thrown UnauthorizedException");
    }

    // LoadIssuesByMaintask tests
    @Test
    public void testLoadIssuesByMainTask() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn());
        HashSet<ScrumIssue> issues =  (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadIssuesByMainTask(maintaskKey, userLoggedIn()).getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
    }
    
    @Test(expected = NotFoundException.class)
    public void testLoadIssuesByNonExistingMainTask() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesByMainTask("non-existing", userLoggedIn()).getItems();
        fail("Should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testLoadIssuesByNullMainTask() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesByMainTask(null, userLoggedIn()).getItems();
        fail("Should have thrown NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testLoadIssuesByMainTaskNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.loadIssuesByMainTask(maintaskKey, userNotLoggedIn()).getItems();
        fail("Should have thrown UnauthorizedException");
    }

    // LoadIssuesBySprint tests
    @Test
    public void testLoadIssuesBySprint() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, sprintKey, userLoggedIn());
        HashSet<ScrumIssue> issues =  (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadIssuesBySprint(sprintKey, userLoggedIn())
                .getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
    }
    
    
    @Test(expected = NotFoundException.class)
    public void testLoadIssuesByNonExistingSprint() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesBySprint("non-existing", userLoggedIn()).getItems();
        fail("Should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testLoadIssuesByNullSprint() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesBySprint(null, userLoggedIn()).getItems();
        fail("Should have thrown NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testLoadIssuesBySprintNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadIssuesBySprint(USER_KEY, userNotLoggedIn()).getItems();
        fail("Should have thrown UnauthorizedException");
    }

    // LoadUnsprintedIssues tests
    @Test
    public void testLoadUnsprintedIssuesExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn());
        HashSet<ScrumIssue> issues =  (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadUnsprintedIssuesForProject(projectKey, 
                userLoggedIn()).getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
    }
    
    @Test(expected = NotFoundException.class)
    public void testLoadUnsprintedIssuesNonExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadUnsprintedIssuesForProject("non-existing", userLoggedIn());
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testLoadUnsprintedIssuesNullProject() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.loadUnsprintedIssuesForProject(null, userLoggedIn());
        fail("should have thrown NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testLoadUnsprintedIssuesNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.loadUnsprintedIssuesForProject(projectKey, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }

    // Insert Issue tests
    @Test
    public void testInsertIssueWithoutPlayerWithoutSprint() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());
        }
    
    @Test
    public void testInsertIssueWithPlayer() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertNull(issue.getSprint());
    }
    
    @Test
    public void testInsertIssueWithSprint() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, sprintKey, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());
    }
    
    @Test
    public void testInsertIssueWithPlayerWithSprint() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(TIME);
        issue.setStatus(STATUS);
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, sprintKey, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE , issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(TIME, issue.getEstimation(), DELTA);
        assertEquals(STATUS, issue.getStatus());
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertEquals(sprintKey, issue.getSprint().getKey());    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertNullIssue() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssue(null, maintaskKey, null, null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertIssueNullMaintask() throws ServiceException {
        loginUser(USER_KEY);
        ScrumIssue issue = new ScrumIssue();
        ISSUE_ENDPOINT.insertScrumIssue(issue, null, null, null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertIssueNonExistingMaintask() throws ServiceException {
        loginUser(USER_KEY);
        ScrumIssue issue = new ScrumIssue();
        ISSUE_ENDPOINT.insertScrumIssue(issue, "non-existing", null, null, userLoggedIn());
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testInsertIssueNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ScrumIssue issue = new ScrumIssue();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }

    // Insert Issue in sprint tests
    @Test
    public void testInsertExistingIssueInExistingSprint() {
        fail("Not yet Implemented");
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertNonExistingIssueInSprint() {
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertNullIssueInSprint() {
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertIssueInNonExistingSprint() {
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertIssueInNullSprint() {
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testInsertIssueInSprintNotLoggedIn() {
        fail("should have thrown UnauthorizedException");
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

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistingIssue() {
        fail("should have thrown NotFoundException");
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateNullIssue() {
        fail("should have thrown a NullPointerException");
    }

    @Test(expected = UnauthorizedException.class)
    public void testUpdateIssueNotLoggedIn() {
        fail("should have thrown UnauthorizedException");
    }

    // Remove Issue tests
    @Test
    public void testRemoveExistingIssue() {
        fail("Not yet Implemented");
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullIssue() {
        fail("should have thrown a NullPointerException");
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistingIssue() {
        fail("should have thrown NotFoundException");
    }

    @Test(expected = UnauthorizedException.class)
    public void testRemoveIssueNotLoggedIn() {
        fail("should have thrown UnauthorizedException");
    }

    // Remove issues from sprint tests
    @Test
    public void testRemoveExistingIssueFromExistingSprint() {
        fail("Not yet Implemented");
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveIssueFromNullSprint() {
        fail("should have thrown a NullPointerException");
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullIssueFromSprint() {
        fail("should have thrown a NullPointerException");
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistingIssueFromSprint() {
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NotFoundException.class)
    public void testRemoveIssueFromNonExistingSprint() {
        fail("should have thrown NotFoundException");
    }
    @Test(expected = UnauthorizedException.class)
    public void testRemoveIssueFromSprintNotLoggedIn() {
        fail("should have thrown UnauthorizedException");
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
