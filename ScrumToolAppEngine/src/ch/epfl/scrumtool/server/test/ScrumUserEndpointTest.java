package ch.epfl.scrumtool.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.epfl.scrumtool.server.ScrumUser;
import ch.epfl.scrumtool.server.ScrumUserEndpoint;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * Tests for ScrumUserEndpoint
 * @author zenhaeus
 *
 */
public class ScrumUserEndpointTest {

    // Since we use the High Replication Datastore we need to add .setDefaultHightRepJob...
    
    private final LocalServiceTestHelper helper = 
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                .setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
                .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final UserService userService = UserServiceFactory.getUserService();

    private static final int PERCENTAGE = 100;
    private static final String USER_KEY_1 = "joeyzenh@gmail.com";
    private static final String AUTH_DOMAIN = "epfl.ch";
    private static final ScrumUserEndpoint ENDPOINT = new ScrumUserEndpoint();

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }
    
    /*
     * Test database functionalities
     */
    
    @Test
    public void testLoginUser() {
        ScrumUser user = loginUser(USER_KEY_1);
        assertEquals(USER_KEY_1, user.getEmail());
        assertEquals(USER_KEY_1, user.getName());
    }
    
    @Test
    public void testRemoveExistingUser() throws OAuthRequestException {
        ScrumUser user = loginUser(USER_KEY_1);
        boolean removalDone = ENDPOINT.removeScrumUser(user.getEmail(), userLoggedIn()).getSuccess();
        assertTrue("Removing existing user should succeed", removalDone);
    }
    
    @Test
    public void testRemoveNonexistantUser() throws OAuthRequestException {
        boolean removalDone = ENDPOINT.removeScrumUser(USER_KEY_1, userLoggedIn()).getSuccess();
        assertFalse("Removing nonexistant user should fail", removalDone);
    }
    
    /*
     * Test authentification
     */
    
    @Test(expected = OAuthRequestException.class)
    public void testRemoveScrumUserIsProtected() throws OAuthRequestException {
        ENDPOINT.removeScrumUser(USER_KEY_1, userNotLoggedIn());
    }

    @Test(expected = OAuthRequestException.class)
    public void testUpdateScrumUserIsProtected() throws OAuthRequestException {
        ENDPOINT.updateScrumUser(new ScrumUser(), userNotLoggedIn());
    }
    
    @Test(expected = OAuthRequestException.class)
    public void testLoadProjectsIsProtected() throws OAuthRequestException {
        ENDPOINT.loadProjects(USER_KEY_1, userNotLoggedIn());
    }

    
    /*
     * Helper Methods
     */
    private ScrumUser loginUser(String email) {
        return ENDPOINT.loginUser(email);
    }
    
    private User userLoggedIn() {
        helper.setEnvEmail(USER_KEY_1);
        helper.setEnvAuthDomain(AUTH_DOMAIN);
        return userService.getCurrentUser();
    }
    
    private User userNotLoggedIn() {
        helper.setEnvEmail(USER_KEY_1);
        helper.setEnvAuthDomain(AUTH_DOMAIN);
        helper.setEnvIsLoggedIn(false);
        return userService.getCurrentUser();
    }
}
