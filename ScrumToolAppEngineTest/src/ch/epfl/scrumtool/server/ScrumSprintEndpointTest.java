package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
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
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * @author vincent
 *
 */
public class ScrumSprintEndpointTest {
    // Since we use the High Replication Datastore we need to add .setDefaultHightRepJob...

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
            .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final UserService userService = UserServiceFactory.getUserService();

    private static final int PERCENTAGE = 100;
    private static final String AUTH_DOMAIN = "epfl.ch";
    private static final ScrumUserEndpoint USER_ENDPOINT = new ScrumUserEndpoint();
    private static final ScrumSprintEndpoint SPRINT_ENDPOINT = new ScrumSprintEndpoint();
    private static final ScrumProjectEndpoint PROJECT_ENDPOINT = new ScrumProjectEndpoint();

    private static final String USER_KEY = "vincent.debieux@gmail.com";
    private ScrumProject project = new ScrumProject();


    @Before
    public void setUp() throws Exception {
        project.setDescription("description");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        helper.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }
    
    //Insert Sprint tests
    @Test
    public void testInsertSprint() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        sprint.setDate(10000);
        sprint.setTitle("Title");
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        assertNotNull(sprintKey);
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(projectKey, sprint.getProject().getKey());
        assertEquals(10000,sprint.getDate());
        assertEquals("Title", sprint.getTitle());
        assertEquals(USER_KEY, sprint.getLastModUser());
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertSprintNullProjectKey() throws ServiceException{
        loginUser(USER_KEY);
        ScrumSprint sprint = new ScrumSprint();
        SPRINT_ENDPOINT.insertScrumSprint(null, sprint, userLoggedIn());
        fail("should have thrown a NullPointerEsception");
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertSprintNonExistingProject() throws ServiceException{
        loginUser(USER_KEY);
        ScrumSprint sprint = new ScrumSprint();
        SPRINT_ENDPOINT.insertScrumSprint("non-existing", sprint, userLoggedIn());
        fail("should have thrown a NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertNullSprint() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        SPRINT_ENDPOINT.insertScrumSprint(projectKey, null, userLoggedIn());
        fail("should have thrown a NullPointerEsception");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testInsertSprintNotLoggedIn() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userNotLoggedIn());
        fail("should have thrown an UnauthorizedException");
    }
    
    //Update Sprint tests
    @Test
    public void testUpdateExistingSprint() throws ServiceException{
        final String title = "Title";
        final long date = Calendar.getInstance().getTimeInMillis();
        HashSet<ScrumIssue> issues = new HashSet<ScrumIssue>();
        
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        ScrumSprint update = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertNotSame(date, update.getDate());
        assertNotSame(title, update.getTitle());
        assertNotSame(issues, update.getIssues());
        assertEquals(USER_KEY, sprint.getLastModUser());

        update.setDate(date);
        update.setIssues(issues);
        update.setTitle(title);
        SPRINT_ENDPOINT.updateScrumSprint(update, userLoggedIn());
        update = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(date, update.getDate());
        assertEquals(title, update.getTitle());
        assertEquals(issues, update.getIssues());
        assertEquals(USER_KEY, sprint.getLastModUser());
    }
    
    @Test(expected = NullPointerException.class)
    public void testUpdateNullSprint() throws ServiceException{
        loginUser(USER_KEY);
        SPRINT_ENDPOINT.updateScrumSprint(null, userLoggedIn());
        fail("should have thrown a NullPointerEsception");
    }
    
    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistingSprint() throws ServiceException{
        loginUser(USER_KEY);
        ScrumSprint sprint = new ScrumSprint();
        sprint.setKey("non-existing");
        SPRINT_ENDPOINT.updateScrumSprint(sprint, userLoggedIn());
        fail("should have thrown a NotFoundException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testUpdateSprintNotLoggedIn() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        sprint.setKey(sprintKey);
        SPRINT_ENDPOINT.updateScrumSprint(sprint, userNotLoggedIn());
        fail("should have thrown a UnauthorizedException");
    }
    
    //Load Sprints tests
    @Test
    public void testLoadSprintsExistingProject() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        sprint.setDate(1000);
        sprint.setTitle("Title");
        SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn());
        ArrayList<ScrumSprint> sprints = new ArrayList<ScrumSprint>((HashSet<ScrumSprint>) SPRINT_ENDPOINT.loadSprints(
                projectKey, userLoggedIn()).getItems());
        assertNotNull(sprints);
        assertEquals(1, sprints.size());
        assertNotNull(sprints.get(0));
        assertEquals(1000, sprints.get(0).getDate());
        assertEquals("Title", sprints.get(0).getTitle());
    }
    
    @Test(expected = NotFoundException.class)
    public void testLoadSprintsNonExistingProject() throws ServiceException{
        loginUser(USER_KEY);
        SPRINT_ENDPOINT.loadSprints("non-existing", userLoggedIn()).getItems();
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testLoadSprintsNullProjectKey() throws ServiceException{
        loginUser(USER_KEY);
        SPRINT_ENDPOINT.loadSprints(null, userLoggedIn()).getItems();
        fail("should have thrown NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testLoadSprintsNotLoggedIn() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn());
        SPRINT_ENDPOINT.loadSprints(projectKey, userNotLoggedIn()).getItems();
        fail("should have thrown UnauthorizedException");
    }
    
    //Remove Sprint tests
    @Test
    public void testRemoveExistingSprint() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        SPRINT_ENDPOINT.removeScrumSprint(sprintKey, userLoggedIn());
        ArrayList<ScrumSprint> sprints = new ArrayList<ScrumSprint>((HashSet<ScrumSprint>) SPRINT_ENDPOINT.loadSprints(
                projectKey, userLoggedIn()).getItems());
        assertNotNull(sprints);
        assertEquals(0, sprints.size());
    }
    
    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistingSprint() throws ServiceException{
        loginUser(USER_KEY);
        SPRINT_ENDPOINT.removeScrumSprint("non-existing", userLoggedIn());
        fail("should have thrown NotFoundException");
    }
    
    @Test(expected = NullPointerException.class)
    public void testRemoveNullSprintKey() throws ServiceException{
        loginUser(USER_KEY);
        SPRINT_ENDPOINT.removeScrumSprint(null, userLoggedIn());
        fail("should have thrown NullPointerException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testRemoveSprintNotLoggedIn() throws ServiceException{
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ScrumSprint sprint = new ScrumSprint();
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        SPRINT_ENDPOINT.removeScrumSprint(sprintKey, userNotLoggedIn());
        fail("should have thrown UnauthorizedException");
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
