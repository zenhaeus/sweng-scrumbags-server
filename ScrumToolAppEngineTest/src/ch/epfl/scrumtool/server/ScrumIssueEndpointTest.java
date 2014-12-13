package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Random;

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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * 
 * @author
 *
 */
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
    private static final float ESTIMATION_1 = new Float(new Random().nextInt(Integer.MAX_VALUE));
    private static final Status STATUS = Status.READY_FOR_SPRINT;
    private static final Priority PRIORITY = Priority.HIGH;
    private static final String TITLE2 = "issue2 title";
    private static final String DESCRIPTION2 = "issue2 description";
    private static final float ESTIMATION_2 = new Float(new Random().nextInt(Integer.MAX_VALUE));
    private static final Status STATUS2 = Status.READY_FOR_ESTIMATION;
    private static final Priority PRIORITY2 = Priority.LOW;
    private static final String ROLE = Role.DEVELOPER.name();
    private static final long SPRINT_DATE = Calendar.getInstance().getTimeInMillis();
    private static final String SPRINT_TITLE = "sprint 1";
    
    private ScrumMainTask maintask;
    private ScrumProject project;
    private ScrumSprint sprint;
    private ScrumIssue issue;

    @Before
    public void setUp() throws Exception {
        maintask = new ScrumMainTask();
        maintask.setDescription("description");
        maintask.setLastModDate(Calendar.getInstance().getTimeInMillis());
        maintask.setName("MainTask");
        maintask.setLastModUser(USER_KEY);
        maintask.setPriority(Priority.NORMAL);
        
        project = new ScrumProject();
        project.setName("Project name");
        project.setDescription("Project description");
        
        sprint = new ScrumSprint();
        sprint.setDate(SPRINT_DATE);
        sprint.setTitle(SPRINT_TITLE);

        issue = new ScrumIssue();
        setIssue();
        
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
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        String playerKey = project.getPlayers().iterator().next().getKey();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn());
        HashSet<ScrumIssue> issues =
                (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadIssuesForUser(USER_KEY, userLoggedIn()).getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertIssueWithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertNull(issue.getSprint());
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
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn());
        HashSet<ScrumIssue> issues =
                (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadIssuesByMainTask(maintaskKey, userLoggedIn()).getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertIssueWithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());
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
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.loadIssuesByMainTask(maintaskKey, userNotLoggedIn()).getItems();
        fail("Should have thrown UnauthorizedException");
    }

    // LoadIssuesBySprint tests
    @Test
    public void testLoadIssuesBySprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, sprintKey, userLoggedIn());
        HashSet<ScrumIssue> issues =  (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadIssuesBySprint(sprintKey, userLoggedIn())
                .getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertIssueWithStatusCheck(Status.IN_SPRINT);
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());
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
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn());
        HashSet<ScrumIssue> issues =  (HashSet<ScrumIssue>) ISSUE_ENDPOINT.loadUnsprintedIssuesForProject(projectKey, 
                userLoggedIn()).getItems();
        assertNotNull(issues);
        assertEquals(1, issues.size());
        issue = issues.iterator().next();
        assertIssueWithoutStatusCheck();
        assertNull(issue.getSprint());
        assertNull(issue.getAssignedPlayer());
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
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.loadUnsprintedIssuesForProject(projectKey, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }

    // Insert Issue tests
    @Test
    public void testInsertIssueWithoutPlayerWithoutSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());
    }
    
    @Test
    public void testInsertIssueWithPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertNull(issue.getSprint());
    }
    
    @Test
    public void testInsertIssueWithSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, sprintKey, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithStatusCheck(Status.IN_SPRINT);
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());
    }
    
    @Test
    public void testInsertIssueWithPlayerWithSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey =
                ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, sprintKey, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithStatusCheck(Status.IN_SPRINT);
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertEquals(sprintKey, issue.getSprint().getKey());
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertNullIssue() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssue(null, maintaskKey, null, null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertIssueNullMaintask() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.insertScrumIssue(issue, null, null, null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertIssueNonExistingMaintask() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.insertScrumIssue(issue, "non-existing", null, null, userLoggedIn());
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testInsertIssueNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }

    // Insert Issue in sprint tests
    @Test
    public void testInsertExistingIssueInExistingSprintWhitoutPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssueInSprint(issueKey, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithStatusCheck(Status.IN_SPRINT);
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(SPRINT_DATE, sprint.getDate());
        assertEquals(SPRINT_TITLE, sprint.getTitle());
        assertNotNull(sprint.getIssues());
        assertEquals(1, sprint.getIssues().size());
        issue = sprint.getIssues().iterator().next();
        assertIssueWithoutStatusCheck();
    }
    
    @Test
    public void testInsertExistingIssueInExistingSprintWhitPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssueInSprint(issueKey, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithStatusCheck(Status.IN_SPRINT);
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertEquals(sprintKey, issue.getSprint().getKey());
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(SPRINT_DATE, sprint.getDate());
        assertEquals(SPRINT_TITLE, sprint.getTitle());
        assertNotNull(sprint.getIssues());
        assertEquals(1, sprint.getIssues().size());
        issue = sprint.getIssues().iterator().next();
        assertIssueWithStatusCheck(Status.IN_SPRINT);
        
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertNonExistingIssueInSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssueInSprint("non-existing", sprintKey, userLoggedIn());
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertNullIssueInSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssueInSprint(null, sprintKey, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertIssueInNonExistingSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssueInSprint(issueKey, "non-existing", userLoggedIn());
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertIssueInNullSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssueInSprint(issueKey, null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testInsertIssueInSprintNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn()).getKey();
        ISSUE_ENDPOINT.insertScrumIssueInSprint(issueKey, sprintKey, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }

    // Update Issue tests
    @Test
    public void testUpdateExistingIssue() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        
        // for the following comments it means from one state ==> to a new state
        // p stands for player, s for sprint
        // the backslash means without
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());
        
        // /p/s ==> /p/s
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithStatusCheck(Status.READY_FOR_SPRINT);
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());
        
        // /p/s ==> /ps
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithStatusCheck(Status.IN_SPRINT);
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // /ps ==> /p/s
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());

        // /p/s ==> p/s
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertNull(issue.getSprint());

        // p/s ==> /p/s
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());

        // /p/s ==> ps
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // ps ==> ps
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // ps ==> /ps
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // /ps ==> ps
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // ps ==> p/s 
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertNull(issue.getSprint());

        // p/s ==> /ps
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // /ps ==> /ps
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // /ps ==> p/s
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertNull(issue.getSprint());

        // p/s ==> p/s
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertNull(issue.getSprint());

        // p/s ==> ps
        setIssue2();
        ISSUE_ENDPOINT.updateScrumIssue(issue, playerKey, sprintKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssue2WithoutStatusCheck();
        assertEquals(playerKey, issue.getAssignedPlayer().getKey());
        assertEquals(sprintKey, issue.getSprint().getKey());

        // ps ==> /p/s
        setIssue();
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, null, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertNull(issue.getAssignedPlayer());
        assertNull(issue.getSprint());
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateNullIssue() throws ServiceException {
        ISSUE_ENDPOINT.updateScrumIssue(null, null, null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }

    @Test(expected = UnauthorizedException.class)
    public void testUpdateIssueNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn()).getKey();
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        ISSUE_ENDPOINT.updateScrumIssue(issue, null, null, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }

    // Remove Issue tests
    @Test
    public void testRemoveExistingIssueWithPlayerWithSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, sprintKey, userLoggedIn())
                .getKey();
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(1, sprint.getIssues().size());
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(1, maintask.getIssues().size());
        ScrumPlayer player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, playerKey);
        assertEquals(1, player.getIssues().size());
        ISSUE_ENDPOINT.removeScrumIssue(issueKey, userLoggedIn());
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(0, sprint.getIssues().size());
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(0, maintask.getIssues().size());
        player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, playerKey);
        assertEquals(0, player.getIssues().size());
    }
    
    @Test
    public void testRemoveExistingIssue() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn())
                .getKey();
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(1, maintask.getIssues().size());
        ISSUE_ENDPOINT.removeScrumIssue(issueKey, userLoggedIn());
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(0, maintask.getIssues().size());
    }
    
    @Test
    public void testRemoveExistingIssueWithPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, null, userLoggedIn())
                .getKey();
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(1, maintask.getIssues().size());
        ScrumPlayer player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, playerKey);
        assertEquals(1, player.getIssues().size());
        ISSUE_ENDPOINT.removeScrumIssue(issueKey, userLoggedIn());
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(0, maintask.getIssues().size());
        player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, playerKey);
        assertEquals(0, player.getIssues().size());
    }
    
    @Test
    public void testRemoveExistingIssueWithSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, sprintKey, userLoggedIn())
                .getKey();
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(1, sprint.getIssues().size());
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(1, maintask.getIssues().size());
        ISSUE_ENDPOINT.removeScrumIssue(issueKey, userLoggedIn());
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(0, sprint.getIssues().size());
        maintask = PMF.get().getPersistenceManager().getObjectById(ScrumMainTask.class, maintaskKey);
        assertEquals(0, maintask.getIssues().size());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullIssue() throws ServiceException {
        ISSUE_ENDPOINT.removeScrumIssue(null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistingIssue() throws ServiceException {
        ISSUE_ENDPOINT.removeScrumIssue("non-existing", userLoggedIn());
        fail("should have thrown NotFoundException");
    }

    @Test(expected = UnauthorizedException.class)
    public void testRemoveIssueNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, sprintKey, userLoggedIn())
                .getKey();
        ISSUE_ENDPOINT.removeScrumIssue(issueKey, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }

    // Remove issues from sprint tests
    @Test
    public void testRemoveExistingIssueFromExistingSprintWithUser() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, sprintKey, userLoggedIn())
                .getKey();
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(1, sprint.getIssues().size());
        assertEquals(sprintKey, issue.getSprint().getKey());
        ISSUE_ENDPOINT.removeScrumIssueFromSprint(issueKey, userLoggedIn());
        issue = PMF.get().getPersistenceManager().getObjectById(ScrumIssue.class, issueKey);
        assertIssueWithoutStatusCheck();
        assertNull(issue.getSprint());
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(0, sprint.getIssues().size());
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveIssueFromNullSprint() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn())
                .getKey();
        ISSUE_ENDPOINT.removeScrumIssueFromSprint(issueKey, userLoggedIn());
        fail("should have thrown a NotFoundException");
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullIssueFromSprint() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.removeScrumIssueFromSprint(null, userLoggedIn());
        fail("should have thrown a NullPointerException");
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistingIssueFromSprint() throws ServiceException {
        loginUser(USER_KEY);
        ISSUE_ENDPOINT.removeScrumIssueFromSprint("non-existing", userLoggedIn());
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testRemoveIssueFromSprintNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, null, null, userLoggedIn())
                .getKey();
        ISSUE_ENDPOINT.removeScrumIssueFromSprint(issueKey, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
    }
    
    private void setIssue() {
        issue.setName(TITLE);
        issue.setDescription(DESCRIPTION);
        issue.setPriority(PRIORITY);
        issue.setEstimation(ESTIMATION_1);
        issue.setStatus(STATUS);
    }
    
    private void setIssue2() {
        issue.setName(TITLE2);
        issue.setDescription(DESCRIPTION2);
        issue.setPriority(PRIORITY2);
        issue.setEstimation(ESTIMATION_2);
        issue.setStatus(STATUS2);
    }
    
    
    
    private void assertIssueWithoutStatusCheck() {
        assertEquals(PRIORITY, issue.getPriority());
        assertEquals(TITLE, issue.getName());
        assertEquals(DESCRIPTION, issue.getDescription());
        assertEquals(Float.compare(ESTIMATION_1, issue.getEstimation()), 0);
        assertEquals(USER_KEY, issue.getLastModUser());
    }
    
    private void assertIssueWithStatusCheck(Status status) {
        assertIssueWithoutStatusCheck();
        assertEquals(status, issue.getStatus());
    }
    
    private void assertIssue2WithoutStatusCheck() {
        assertEquals(PRIORITY2, issue.getPriority());
        assertEquals(TITLE2, issue.getName());
        assertEquals(DESCRIPTION2, issue.getDescription());
        assertEquals(Float.compare(ESTIMATION_2, issue.getEstimation()), 0);
        assertEquals(USER_KEY, issue.getLastModUser());
    }
    
    private void assertIssue2WithStatusCheck(Status status) {
        assertIssue2WithoutStatusCheck();
        assertEquals(status, issue.getStatus());
    }

    private ScrumUser loginUser(String email) throws ServiceException {
        return USER_ENDPOINT.loginUser(email, userLoggedIn());
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
