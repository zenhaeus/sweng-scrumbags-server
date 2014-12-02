package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.epfl.scrumtool.PMF;
import ch.epfl.scrumtool.server.ScrumProject;
import ch.epfl.scrumtool.server.ScrumUser;
import ch.epfl.scrumtool.server.ScrumUserEndpoint;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
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
    private static final String AUTH_DOMAIN = "epfl.ch";
    private static final ScrumUserEndpoint ENDPOINT = new ScrumUserEndpoint();

    // User Attributes
    private static final String USER_KEY = "joeyzenh@gmail.com";
    private static final String COMPANY_NAME = "Company";
    private static final long DATE_OF_BIRTH = Calendar.getInstance().getTimeInMillis();
    private static final String JOB_TITLE = "CEO";
    private static final String NAME = "Name";
    private static final String LASTNAME = "Lastname";
    private static final long LAST_MOD_DATE = Calendar.getInstance().getTimeInMillis();
    private static final String LAST_MOD_USER = USER_KEY;
    private static final String GENDER = "male";

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
    public void testLoginUser() throws ServiceException {
        ScrumUser user = loginUser(USER_KEY);
        assertEquals(USER_KEY, user.getEmail());
        assertEquals(USER_KEY, user.getName());
    }

    @Test(expected = NullPointerException.class)
    public void testLoginUserNull() throws ServiceException {
        assertNull(loginUser(null));
        fail("should have thrown a NullPointerException");
    }

    @Test
    public void testRemoveExistingUser() throws ServiceException {
        ScrumUser user = loginUser(USER_KEY);
        ENDPOINT.removeScrumUser(user.getEmail(), userLoggedIn());
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonexistantUser() throws ServiceException {
        ENDPOINT.removeScrumUser("non-existing", userLoggedIn());
        fail("should have thrown a NotFoundException");
    }

    @Test(expected = ServiceException.class)
    public void testRemoveNullUser() throws ServiceException {
        ENDPOINT.removeScrumUser(null, userLoggedIn());
        fail("should have thrown a ServiceException");
    }

    @Test
    public void testUpdateExistingUser() throws ServiceException {
        ScrumUser user = loginUser(USER_KEY);
        ScrumUser updatedUser = user;
        updatedUser.setCompanyName(COMPANY_NAME);
        updatedUser.setDateOfBirth(DATE_OF_BIRTH);
        updatedUser.setJobTitle(JOB_TITLE);
        updatedUser.setName(NAME);
        updatedUser.setLastName(LASTNAME);
        updatedUser.setLastModDate(LAST_MOD_DATE);
        updatedUser.setLastModUser(LAST_MOD_USER);
        updatedUser.setGender(GENDER);
        ENDPOINT.updateScrumUser(updatedUser, userLoggedIn());
        updatedUser = PMF.get().getPersistenceManager().getObjectById(ScrumUser.class, USER_KEY);
        assertEquals(COMPANY_NAME, updatedUser.getCompanyName());
        assertEquals(DATE_OF_BIRTH, updatedUser.getDateOfBirth());
        assertEquals(JOB_TITLE, updatedUser.getJobTitle());
        assertEquals(NAME, updatedUser.getName());
        assertEquals(LASTNAME, updatedUser.getLastName());
        assertEquals(LAST_MOD_DATE, updatedUser.getLastModDate());
        assertEquals(LAST_MOD_USER, updatedUser.getLastModUser());
        assertEquals(GENDER, updatedUser.getGender());
    }
    
    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistantUser() throws ServiceException {
        ScrumUser notInDatastore = new ScrumUser();
        notInDatastore.setEmail(USER_KEY);
        ENDPOINT.updateScrumUser(notInDatastore, userLoggedIn());
        fail("should have thrown a NotFoundException");
    }

    @Test
    public void testLoadProjectsForExistingUser() throws ServiceException {
        loginUser(USER_KEY);
        Set<ScrumProject> projects = (Set<ScrumProject>) ENDPOINT.loadProjects(USER_KEY, userLoggedIn()).getItems();
        assertNotNull(projects);
    }
    
    /*
     * Test authentification
     */

    @Test(expected = UnauthorizedException.class)
    public void testRemoveScrumUserIsProtected() throws ServiceException {
        ENDPOINT.removeScrumUser(USER_KEY, userNotLoggedIn());
        fail("should have thrown an UnauthorizedException");
    }

    @Test(expected = UnauthorizedException.class)
    public void testUpdateScrumUserIsProtected() throws ServiceException {
        ENDPOINT.updateScrumUser(new ScrumUser(), userNotLoggedIn());
        fail("should have thrown an UnauthorizedException");
    }
    
    @Test(expected = UnauthorizedException.class)
    public void testLoadProjectsIsProtected() throws ServiceException {
        ENDPOINT.loadProjects(USER_KEY, userNotLoggedIn());
        fail("should have thrown an UnauthorizedException");
    }

    
    /*
     * Helper Methods
     */

    private ScrumUser loginUser(String email) throws ServiceException {
        return ENDPOINT.loginUser(email);
    }
    
    private User userLoggedIn() {
        helper.setEnvEmail(USER_KEY);
        helper.setEnvAuthDomain(AUTH_DOMAIN);
        return userService.getCurrentUser();
    }
    
    private User userNotLoggedIn() {
        helper.setEnvEmail(USER_KEY);
        helper.setEnvAuthDomain(AUTH_DOMAIN);
        helper.setEnvIsLoggedIn(false);
        return userService.getCurrentUser();
    }
}
