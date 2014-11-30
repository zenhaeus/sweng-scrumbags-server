package ch.epfl.scrumtool.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.appengine.api.oauth.OAuthRequestException;
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

    private static final String USER_KEY = "vincent.debieux@gmail.com";

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }
    
    //Insert Sprint tests
    @Test
    public void testInsertSprint() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertSprintNullProjectKey() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertSprintNonExistingProject() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertNullSprint() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testInsertSprintNotLoggedIn() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    //Update Sprint tests
    @Test
    public void testUpdateExistingSprint() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testUpdateNullSprint() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testUpdateNonExistingSprint() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testUpdateSprintNotLoggedIn() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    //Load Sprints tests
    @Test
    public void testLoadSprintsExistingProject() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadSprintsNonExistingProject() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadSprintsNullProjectKey() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testLoadSprintsNotLoggedIn() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    //Remove Sprint tests
    @Test
    public void testRemoveExistingSprint() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testRemoveNonExistingSprint() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testRemoveNullSprintKey() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    @Test
    public void testRemoveSprintNotLoggedIn() throws OAuthRequestException{
        fail("Not yet Implemented");
    }
    
    private ScrumUser loginUser(String email) {
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
