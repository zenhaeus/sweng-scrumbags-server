package ch.epfl.scrumtool.server;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

    private static final int PERCENTAGE = 100;
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

    @Test
    public void testInsertScrumProject() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateScrumProject() {
        fail("Not yet implemented");
    }

    @Test
    public void testRemoveScrumProject() {
        fail("Not yet implemented");
    }

}
