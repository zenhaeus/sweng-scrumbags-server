package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.jdo.JDOObjectNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * @author joey
 *
 */
public class ScrumProjectEndpointTest {
    
    private final LocalServiceTestHelper helper = 
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                .setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
                .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final UserService userService = UserServiceFactory.getUserService();

    private static final ScrumSprintEndpoint SPRINT_ENDPOINT = new ScrumSprintEndpoint();
    private static final ScrumPlayerEndpoint PLAYER_ENDPOINT = new ScrumPlayerEndpoint();
    private static final ScrumUserEndpoint USER_ENDPOINT = new ScrumUserEndpoint();
    private static final ScrumIssueEndpoint ISSUE_ENDPOINT = new ScrumIssueEndpoint();
    private static final ScrumProjectEndpoint PROJECT_ENDPOINT = new ScrumProjectEndpoint();
    private static final ScrumMainTaskEndpoint TASK_ENDPOINT = new ScrumMainTaskEndpoint();

    private static final String USER_KEY = "some@example.com";
    private static final String NAME = "Murcs";
    private static final String DESCRIPTION = "The coolest app ever";
    private static final String USER2_KEY = "other@example.com";
    private static final String ROLE = Role.STAKEHOLDER.name();
    
    private ScrumProject project;

    private static final int PERCENTAGE = 100;
    private static final String AUTH_DOMAIN = "epfl.ch";

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        project = new ScrumProject();
        project.setName(NAME);
        project.setDescription(DESCRIPTION);
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    // insertion tests
    @Test
    public void testInsertProject() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertProject();
        ScrumPlayer player = project.getPlayers().iterator().next();
        assertFalse(player.getInvitedFlag());
        assertTrue(player.getAdminFlag());
        assertEquals(USER_KEY, player.getUser().getEmail());
        assertEquals(project.getKey(), player.getProject().getKey());
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertNullProject() throws ServiceException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(null, userLoggedIn(USER_KEY));
        fail("should have thrown NullPointerException");
    }
    
    @Test(expected = ForbiddenException.class)
    public void testInsertProjectNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(project, userNotLoggedIn()).getKey();
        fail("Should have thrown ForbiddenException");
    }

    // update tests
    @Test
    public void testUpdateProject() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertProject();
        project.setName("Project2");
        project.setDescription("description2");
        PROJECT_ENDPOINT.updateScrumProject(project, userLoggedIn(USER_KEY));
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertEquals("Project2", project.getName());
        assertEquals("description2", project.getDescription());
        assertNotNull(project.getPlayers());
        assertEquals(1, project.getPlayers().size());
        assertEquals(USER_KEY, project.getPlayers().iterator().next().getUser().getEmail());
    }
    
    @Test(expected = NullPointerException.class)
    public void testUpdateNullProject() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertProject();
        PROJECT_ENDPOINT.updateScrumProject(null, userLoggedIn(USER_KEY));
        fail("should have thrown NullPointerException");
    }
    
    @Test(expected = ForbiddenException.class)
    public void testUpdateProjectNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertProject();
        project.setName("Project2");
        project.setDescription("description2");
        PROJECT_ENDPOINT.updateScrumProject(project, userNotLoggedIn());
        fail("should have thrown ForbiddenException");
    }
    
    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistingProject() throws ServiceException {
        project.setKey("non-existing");
        PROJECT_ENDPOINT.updateScrumProject(project, userLoggedIn(USER_KEY));
        fail("should have thrown NotFoundException");
    }

    // remove tests
    @Test(expected = JDOObjectNotFoundException.class)
    public void testRemoveProjectAsAdmin() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertProject();
        PROJECT_ENDPOINT.removeScrumProject(projectKey, userLoggedIn(USER_KEY));
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        fail("should have thrown a JDOObjectNotFoundException");
    }
    
    @Test
    public void testRemoveProjectAsNotAdmin() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertProject();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn(USER_KEY)).getKey();
        PROJECT_ENDPOINT.removeScrumProject(projectKey, userLoggedIn(USER2_KEY));
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertTrue(project.getPlayers().size() == 1);
    }
    
    @Test
    public void testRemoveProjectWithEverything() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        ScrumMainTask maintask = new ScrumMainTask();
        String maintaskKey = TASK_ENDPOINT.insertScrumMainTask(maintask, projectKey, userLoggedIn(USER_KEY)).getKey();
        String playerKey =
                PLAYER_ENDPOINT.addPlayerToProject(projectKey, "joeyzenh@gmail.com", Role.DEVELOPER.name(),
                        userLoggedIn(USER_KEY)).getKey();
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn(USER_KEY)).getKey();
        ScrumIssue issue = new ScrumIssue();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(issue, maintaskKey, playerKey, sprintKey,
                userLoggedIn(USER_KEY)).getKey();
        PROJECT_ENDPOINT.removeScrumProject(projectKey, userLoggedIn(USER_KEY));
        if (entityExists(ScrumProject.class, projectKey)) {
            fail("removeProject did not remove project");
        }
        if (entityExists(ScrumMainTask.class, maintaskKey)) {
            fail("removeProject did not remove maintask");
        }
        if (entityExists(ScrumPlayer.class, playerKey)) {
            fail("removeProject did not remove player");
        }
        if (entityExists(ScrumSprint.class, sprintKey)) {
            fail("removeProject did not remove sprint");
        }
        if (entityExists(ScrumIssue.class, issueKey)) {
            fail("removeProject did not remove project");
        }
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullProject() throws ServiceException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.removeScrumProject(null, userLoggedIn(USER_KEY));
        fail("should have thrown NullPointerException");
    }

    @Test(expected = ForbiddenException.class)
    public void testRemoveProjectNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn(USER_KEY)).getKey();
        project = PMF.get().getPersistenceManager().getObjectById(ScrumProject.class, projectKey);
        assertProject();
        PROJECT_ENDPOINT.removeScrumProject(projectKey, userNotLoggedIn());
        fail("should have thrown a ForbiddenException");
    }
    
    private void assertProject() {
        assertEquals(NAME, project.getName());
        assertEquals(DESCRIPTION, project.getDescription());
        assertNotNull(project.getPlayers());
        assertEquals(1, project.getPlayers().size());
        assertEquals(USER_KEY, project.getPlayers().iterator().next().getUser().getEmail());
        assertEquals(USER_KEY, project.getLastModUser());
    }
    
    private boolean entityExists(Class<?> c, String key) {
        try {
            PMF.get().getPersistenceManager().getObjectById(c, key);
        } catch (JDOObjectNotFoundException e) {
            return false;
        }
        return true;
    }

    private ScrumUser loginUser(String email) throws ServiceException {
        return USER_ENDPOINT.loginUser(email, userLoggedIn(USER_KEY));
    }

    private User userLoggedIn(String userKey) {
        helper.setEnvEmail(userKey);
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
