package ch.epfl.scrumtool.server;

import static org.junit.Assert.fail;

import java.util.Calendar;

import org.junit.After;
import org.junit.Test;

import com.google.api.server.spi.ServiceException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class ScrumMaintaskEndpointTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
            .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final UserService userService = UserServiceFactory.getUserService();

    private static final int PERCENTAGE = 100;
    private static final String AUTH_DOMAIN = "epfl.ch";
    private static final ScrumProjectEndpoint PROJECT_ENDPOINT = new ScrumProjectEndpoint();
    private static final ScrumMainTaskEndpoint TASK_ENDPOINT = new ScrumMainTaskEndpoint();
    private static final ScrumUserEndpoint USER_ENDPOINT = new ScrumUserEndpoint();

    private static final String USER_KEY = "vincent.debieux@gmail.com";
    
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }
    
    // Load Maintask tests
    @Test
    public void testLoadMainTaskExistingProject() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testLoadMainTaskNonExistingProject() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testLoadMainTaskNullProject() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testLoadMainTaskProjectNotLoggedIn() {
        fail("Not yet implemented");
    }
    
    // Insert Maintask tests
    @Test
    public void testInsertMainTaskExistingProject() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testInsertNullMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testInsertMainTaskNullProject() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testInsertMainTaskNonExistingProject() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testInsertMainTaskNotLoggedIn() {
        fail("Not yet implemented");
    }
    
    // Update Maintask tests
    @Test
    public void testUpdateExistingMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testUpdateNonExistingMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testUpdateNullMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testUpdateMainTaskNotLoggedIn() {
        fail("Not yet implemented");
    }
    
    // Remove Maintask tests
    @Test
    public void testRemoveExistingMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testRemoveNonExistingMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testRemoveNullMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testRemoveMainTaskNotLoggedIn() {
        fail("Not yet implemented");
    }
    
    // ComputeMaintaskInfos tests
    @Test
    public void testComputeMainTaskInfos() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testComputeMainTaskInfosNullMainTask() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testComputeMainTaskInfosforMaintaskWithNullIssues() {
        fail("Not yet implemented");
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
