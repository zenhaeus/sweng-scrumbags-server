package ch.epfl.scrumtool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class AppEngineUtilsTest {
    private static final int PERCENTAGE = 100;
    private final LocalServiceTestHelper helper = 
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig()
                .setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
                .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private static final String USER_KEY = "joeyzenh@gmail.com";
    private static final String AUTH_DOMAIN = "epfl.ch";

    private final UserService userService = UserServiceFactory.getUserService();

    @Before
    public void setUp() throws Exception {
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    @Test(expected = OAuthRequestException.class)
    public void testBasicAuthenticationInvalid() throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(null);
    }

    @Test
    public void testBasicAuthenticationValid() throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(userLoggedIn());
    }
    
    private User userLoggedIn() {
        helper.setEnvEmail(USER_KEY);
        helper.setEnvAuthDomain(AUTH_DOMAIN);
        return userService.getCurrentUser();
    }
}
