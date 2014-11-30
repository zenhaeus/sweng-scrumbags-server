package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;

import javax.persistence.EntityExistsException;

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
    
    private ScrumProject project;

    @Before
    public void setUp() throws Exception {
        project = new ScrumProject();
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

    // Load Players tests
    @Test(expected = NullPointerException.class)
    public void testLoadPlayersNullProject() throws ServiceException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.loadPlayers(null, userLoggedIn()).getItems();
        fail("loadPlayers should throw a NullPointerException when given a null projectKey");
    }

    @Test(expected = NotFoundException.class)
    public void testLoadPlayersNonExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.loadPlayers("non existing", userLoggedIn()).getItems();
        fail("loadPlayers should throw a JDOObjectNotFoundException when given a non existing project");
    }

    @Test
    public void testLoadPlayersExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        
        ArrayList<ScrumPlayer> players = (ArrayList<ScrumPlayer>) PLAYER_ENDPOINT.loadPlayers(projectKey, userLoggedIn())
                .getItems();
        assertEquals(true, players.get(0).getAdminFlag());
        assertEquals(USER_KEY,players.get(0).getUser().getEmail());
    }

    @Test(expected = UnauthorizedException.class)
    public void testLoadPlayersNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.loadPlayers(projectKey, userNotLoggedIn());
        fail("loadPlayers should throw a ServiceException when user is not logged in");
    }

    //Add Players tests

    @Test(expected = NullPointerException.class)
    public void testAddPlayerNullProject() throws ServiceException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.addPlayerToProject(null, USER_KEY, ROLE, userLoggedIn());
        fail("addPlayerToProject should throw a NullPointerException when given a null projectKey");
    }

    @Test(expected = NullPointerException.class)
    public void testAddPlayerNullRole() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, null, userLoggedIn());
        fail("addPlayerToProject should throw a NullPointerException when given a null role");

    }

    @Test(expected = NullPointerException.class)
    public void testAddPlayerNullUserEmail() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, null, ROLE, userLoggedIn());
        fail("addPlayerToProject should throw a NullPointerException when given a null userEmail");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddPlayerNonExistingRole() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, "non existing", userLoggedIn());
    }

    @Test(expected = NotFoundException.class)
    public void testAddPlayerNonExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.addPlayerToProject("non existing", USER_KEY, ROLE, userLoggedIn());
    }

    @Test
    public void testAddPlayerNonExistingUserEmail() throws ServiceException {
        loginUser(USER_KEY);
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
    public void testAddPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        ScrumPlayer player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, playerKey);
        assertEquals(USER2_KEY, player.getUser().getEmail());
        assertEquals(ROLE, player.getRole().name());
        assertEquals(projectKey, player.getProject().getKey());
        assertFalse(player.getAdminFlag());
    }
    
    @Test(expected = EntityExistsException.class)
    public void testAddExistingPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userLoggedIn()).getKey();
        fail("Adding twice the same user to the same project should thrwo an EntityExists Exception");
    }

    @Test(expected = UnauthorizedException.class)
    public void testAddPlayerNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER_KEY, ROLE, userNotLoggedIn());
    }

    // Remove Players tests

    @Test(expected = UnauthorizedException.class)
    public void testRemovePlayerNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.removeScrumPlayer(playerKey, userNotLoggedIn());
        fail("removePlayer should throw an ServiceException when the user is not logged in");
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistingPlayer() throws ServiceException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.removeScrumPlayer("non existing", userLoggedIn());
    }

    @Test
    public void testRemoveExistingPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        String playerKey = PLAYER_ENDPOINT.addPlayerToProject(projectKey, USER2_KEY, ROLE, userLoggedIn()).getKey();
        PLAYER_ENDPOINT.removeScrumPlayer(playerKey, userLoggedIn());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullPlayer() throws ServiceException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.removeScrumPlayer(null, userLoggedIn());
        fail("RemoveScrumPlayer should throw a NullPointerException when passing a null Player");;
    }

    //Update Players tests

    @Test
    public void testUpdateExistingPlayer() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ArrayList<ScrumPlayer> players = (ArrayList<ScrumPlayer>) PLAYER_ENDPOINT.loadPlayers(projectKey, userLoggedIn())
                .getItems();
        ScrumPlayer player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, players.get(0).getKey());
        player.setAdminFlag(false);
        player.setRole(Role.STAKEHOLDER);
        PLAYER_ENDPOINT.updateScrumPlayer(player, userLoggedIn());
        player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, players.get(0).getKey());
        assertFalse(player.getAdminFlag());
        assertEquals(player.getRole(), Role.STAKEHOLDER);
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateNullPlayer() throws ServiceException {
        loginUser(USER_KEY);
        PLAYER_ENDPOINT.updateScrumPlayer(null, userLoggedIn());
        fail("updateScrumPlayer should throw a nullPointerException when given a null player");
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistingPlayer() throws ServiceException {
        loginUser(USER_KEY);
        ScrumPlayer player = new ScrumPlayer();
        player.setKey("bogusKey");
        PLAYER_ENDPOINT.updateScrumPlayer(player, userLoggedIn());
        fail("updateScrumPlayer should throw a JDOObjectNotFoundException");
    }

    @Test(expected = UnauthorizedException.class)
    public void testUpdatePlayerNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project, userLoggedIn()).getKey();
        ArrayList<ScrumPlayer> players = (ArrayList<ScrumPlayer>) PLAYER_ENDPOINT.loadPlayers(projectKey, userLoggedIn())
                .getItems();
        ScrumPlayer player = PMF.get().getPersistenceManager().getObjectById(ScrumPlayer.class, players.get(0).getKey());
        player.setAdminFlag(false);
        player.setRole(Role.STAKEHOLDER);
        PLAYER_ENDPOINT.updateScrumPlayer(player, userNotLoggedIn());
        fail("updateScrumPlayer should throw an ServiceException when user is not logged in");
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
