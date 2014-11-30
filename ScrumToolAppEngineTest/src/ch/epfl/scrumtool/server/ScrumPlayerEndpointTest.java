package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.persistence.EntityExistsException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.quartz.ObjectAlreadyExistsException;

import ch.epfl.scrumtool.PMF;

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
    private static final String USER2_KEY = "joeyzenh@gmail.com";
    private static final String ROLE = "SCRUM_MASTER";

    @Before
    public void setUp() throws Exception {
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
        PLAYER_ENDPOINT.loadPlayers("non existing", userLoggedIn()).getItems();
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

    @Test(expected = NullPointerException.class)
    public void testAddPlayerNullProject() throws OAuthRequestException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.addPlayerToProject(null, USER_KEY, ROLE, userLoggedIn()).getSuccess();
        fail("addPlayerToProject should throw a NullPointerException when given a null projectKey");
    }

    @Test(expected = NullPointerException.class)
    public void testAddPlayerNullRole() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, null, userLoggedIn()).getSuccess();
        fail("addPlayerToProject should throw a NullPointerException when given a null role");

    }

    @Test(expected = NullPointerException.class)
    public void testAddPlayerNullUserEmail() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, null, ROLE, userLoggedIn()).getSuccess();
        fail("addPlayerToProject should throw a NullPointerException when given a null userEmail");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPlayerNonExistingRole() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, "non existing", userLoggedIn())
                .getSuccess();
        assertFalse(result);
    }

    @Test(expected = JDOObjectNotFoundException.class)
    public void testAddPlayerNonExistingProject() throws OAuthRequestException {
        loginUser(USER_KEY);
        boolean result = PLAYER_ENDPOINT.addPlayerToProject("non existing", USER_KEY, ROLE, userLoggedIn())
                .getSuccess();
        assertFalse(result);
    }

    @Test
    public void testAddPlayerNonExistingUserEmail() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn())
                .getKey();
        ScrumPlayer player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, playerKey);
        assertEquals(USER2_KEY, player.getUser().getEmail());
        assertEquals(ROLE, player.getRole().name());
        assertEquals(projectKey, player.getProject().getKey());
        assertFalse(player.getAdminFlag());
    }

    @Test
    public void testAddPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        ScrumPlayer player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, playerKey);
        assertEquals(USER2_KEY, player.getUser().getEmail());
        assertEquals(ROLE, player.getRole().name());
        assertEquals(projectKey, player.getProject().getKey());
        assertFalse(player.getAdminFlag());
    }
    
    @Test(expected = EntityExistsException.class)
    public void testAddExistingPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userLoggedIn()).getKey();
        fail("Adding twice the same user to the same project should thrwo an EntityExists Exception");
    }

    @Test(expected = OAuthRequestException.class)
    public void testAddPlayerNotLoggedIn() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        boolean result = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userNotLoggedIn())
                .getSuccess();
        assertFalse(result);
    }

    // Remove Players tests

    @Test(expected = OAuthRequestException.class)
    public void testRemovePlayerNotLoggedIn() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.removeScrumPlayer(playerKey, userNotLoggedIn()).getSuccess();
        fail("removePlayer should throw an OAuthRequestException when the user is not logged in");
    }

    @Test(expected = JDOObjectNotFoundException.class)
    public void testRemoveNonExistingPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        boolean removalResult = PLAYER_ENDPOINT.removeScrumPlayer("non existing", userLoggedIn()).getSuccess();
        assertFalse(removalResult);
    }

    @Test
    public void testRemoveExistingPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        boolean removalResult = PLAYER_ENDPOINT.removeScrumPlayer(playerKey, userLoggedIn()).getSuccess();
        assertTrue(removalResult);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        boolean removalResult = PLAYER_ENDPOINT.removeScrumPlayer(null, userLoggedIn()).getSuccess();
        assertFalse(removalResult);
    }

    //Update Players tests

    @Test
    public void testUpdateNullPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userLoggedIn()).getKey();
        
//        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
//        assertFalse(result);
    }

    @Test
    public void testUpdateNonExistingPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userLoggedIn());
//        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
//        assertFalse(result);
    }

    @Test
    public void testUpdateExistingPlayer() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userLoggedIn());
//        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
//        assertTrue(result);
        //TODO check more properties
    }

    @Test
    public void testUpdatePlayerNotLoggedIn() throws OAuthRequestException {
        loginUser(USER_KEY);
        ScrumProject project = new ScrumProject();
        project.setDescription("");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userLoggedIn());
//        boolean result = PLAYER_ENDPOINT.updateScrumPlayer(PLAYER, userNotLoggedIn()).getSuccess();
//        assertFalse(result);
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
