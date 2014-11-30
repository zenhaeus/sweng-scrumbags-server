package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class ScrumPlayerEndpointTest {

    // Since we use the High Replication Datastore we need to add .setDefaultHightRepJob...

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
            .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final UserService userService = UserServiceFactory.getUserService();

    private static final int PERCENTAGE = 100;
    private static final String AUTH_DOMAIN = "epfl.ch";
    private static final ScrumProjectEndpoint PROJECT_ENDPOINT = new ScrumProjectEndpoint();
    private static final ScrumPlayerEndpoint PLAYER_ENDPOINT = new ScrumPlayerEndpoint();
    private static final ScrumUserEndpoint USER_ENDPOINT = new ScrumUserEndpoint();

    private static final String USER_KEY = "vincent.debieux@gmail.com";
    private static final String PROJECT_KEY = "ThisIsAProjectKey";
    private static final String PLAYER_KEY = "ThisIsAPlayerKey";
    private static final String ROLE = "SCRUM_MASTER";
    private static final ScrumUser USER = new ScrumUser();
    private static final ScrumPlayer PLAYER = new ScrumPlayer();
    private static final ScrumProject PROJECT = new ScrumProject();

    @Before
    public void setUp() throws Exception {
        USER.setEmail(USER_KEY);
        USER.setName(USER_KEY);
        USER.addPlayer(PLAYER);
        PLAYER.setKey(PLAYER_KEY);
        PLAYER.setUser(USER);
        PLAYER.setAdminFlag(true);
        PLAYER.setRole(Role.SCRUM_MASTER);
        PLAYER.setLastModUser(USER_KEY);
        PLAYER.setLastModDate(123456789);
        PLAYER.setProject(PROJECT);
        PROJECT.setKey(PROJECT_KEY);
        PROJECT.addPlayer(PLAYER);
        PROJECT.setDescription("ThisIsAProjectDescription");
        
        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    // Load Players tests
    @Test(expected = NullPointerException.class)
    public void testLoadPlayersNullProject() throws OAuthRequestException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.loadPlayers(null, userLoggedIn()).getItems();
        fail("loadPlayers should throw a NullPointerException when given a null projectKey");
    }

    @Test(expected = javax.jdo.JDOObjectNotFoundException.class)
    public void testLoadPlayersNonExistingProject() throws OAuthRequestException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.loadPlayers(PROJECT_KEY, userLoggedIn()).getItems();
        fail("loadPlayers should throw a JDOObjectNotFoundException when given a non existing project");
    }

    @Test
    public void testLoadPlayersExistingProject() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        
        ArrayList<ScrumPlayer> players = (ArrayList<ScrumPlayer>) PLAYER_ENDPOINT.loadPlayers(projectKey, userLoggedIn())
                .getItems();
        assertEquals(true, players.get(0).getAdminFlag());
        assertEquals(USER_KEY,players.get(0).getUser().getEmail());
    }

    @Test(expected = OAuthRequestException.class)
    public void testLoadPlayersNotLoggedIn() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        
        PLAYER_ENDPOINT.loadPlayers(projectKey, userNotLoggedIn());
        fail("loadPlayers should throw a OAuthRequestException when user is not logged in");
    }

    //Add Players tests

    @Test
    public void testAddPlayerNullProject() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(null, USER_KEY, ROLE, userLoggedIn()).getSuccess();
        assertFalse(result);
    }

    @Test
    public void testAddPlayerNullRole() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, null, userLoggedIn()).getSuccess();
        assertFalse(result);
    }

    @Test
    public void testAddPlayerNullUserEmail() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, null, ROLE, userLoggedIn()).getSuccess();
        assertFalse(result);
    }

    @Test
    public void testAddPlayerNonExistingRole() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, "non existing", userLoggedIn())
                .getSuccess();
        assertFalse(result);
    }

    @Test
    public void testAddPlayerNonExistingProject() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject("non existing", USER_KEY, ROLE, userLoggedIn())
                .getSuccess();
        assertFalse(result);
    }

    @Test
    public void testAddPlayerNonExistingUserEmail() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, "non existing", ROLE, userLoggedIn())
                .getSuccess();
        assertFalse(result);
    }

    @Test
    public void testAddPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userLoggedIn()).getSuccess();
        assertTrue(result);
    }

    @Test
    public void testAddPlayerNotLoggedIn() throws OAuthRequestException {
        loginUser(USER_KEY);
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userNotLoggedIn())
                .getSuccess();
        assertFalse(result);
    }

    // Remove Players tests

    @Test
    public void testRemovePlayerNotLoggedIn() throws OAuthRequestException {
        PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userLoggedIn());
        boolean removalResult = PLAYER_ENDPOINT.removeScrumPlayer(PLAYER_KEY, userNotLoggedIn()).getSuccess();
        assertFalse(removalResult);
    }

    @Test
    public void testRemoveNonExistingPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        boolean removalResult = PLAYER_ENDPOINT.removeScrumPlayer(PLAYER_KEY, userLoggedIn()).getSuccess();
        assertFalse(removalResult);
    }

    @Test
    public void testRemoveExistingPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userLoggedIn());
        boolean removalResult = PLAYER_ENDPOINT.removeScrumPlayer(PLAYER_KEY, userLoggedIn()).getSuccess();
        assertFalse(removalResult);
    }

    @Test
    public void testRemoveNullPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        boolean removalResult = PLAYER_ENDPOINT.removeScrumPlayer(null, userLoggedIn()).getSuccess();
        assertFalse(removalResult);
    }

    //Update Players tests

    @Test
    public void testUpdateNullPlayer() throws OAuthRequestException {
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
        assertFalse(result);
    }

    @Test
    public void testUpdateNonExistingPlayer() throws OAuthRequestException {
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
        assertFalse(result);
    }

    @Test
    public void testUpdateExistingPlayer() throws OAuthRequestException {
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
        assertTrue(result);
        //TODO check more properties
    }

    @Test
    public void testUpdatePlayerNotLoggedIn() throws OAuthRequestException {
        PROJECT_ENDPOINT.insertScrumProject(PROJECT, userLoggedIn());
        PLAYER_ENDPOINT.addPlayerToProject(PROJECT_KEY, USER_KEY, ROLE, userLoggedIn());
        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
        assertFalse(result);
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
