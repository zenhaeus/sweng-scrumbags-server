package ch.epfl.scrumtool.server;

import java.util.Calendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    private static ScrumSprint sprint = new ScrumSprint();


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
        sprint.setDate(10000);
        sprint.setTitle("Title");
        String sprintKey = SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userLoggedIn()).getKey();
        assertNotNull(sprintKey);
        sprint = PMF.get().getPersistenceManager().getObjectById(ScrumSprint.class, sprintKey);
        assertEquals(projectKey, sprint.getProject().getKey());
        assertEquals(10000,sprint.getDate());
        assertEquals("Title", sprint.getTitle());
    }
    
    @Test(expected = NullPointerException.class)
    public void testInsertSprintNullProjectKey() throws ServiceException{
        loginUser(USER_KEY);
        SPRINT_ENDPOINT.insertScrumSprint(null, sprint, userLoggedIn());
        fail("should have thrown a NullPointerEsception");
    }
    
    @Test(expected = NotFoundException.class)
    public void testInsertSprintNonExistingProject() throws ServiceException{
        loginUser(USER_KEY);
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
        SPRINT_ENDPOINT.insertScrumSprint(projectKey, sprint, userNotLoggedIn());
        fail("should have thrown an UnauthorizedException");    }
    
    //Update Sprint tests
    @Test
    public void testUpdateExistingSprint() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testUpdateNullSprint() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testUpdateNonExistingSprint() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testUpdateSprintNotLoggedIn() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    //Load Sprints tests
    @Test
    public void testLoadSprintsExistingProject() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadSprintsNonExistingProject() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadSprintsNullProjectKey() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadSprintsNotLoggedIn() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    //Remove Sprint tests
    @Test
    public void testRemoveExistingSprint() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testRemoveNonExistingSprint() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testRemoveNullSprintKey() throws ServiceException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testRemoveSprintNotLoggedIn() throws ServiceException{
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
