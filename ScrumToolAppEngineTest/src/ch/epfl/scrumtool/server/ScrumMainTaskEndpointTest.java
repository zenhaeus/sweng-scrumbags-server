package ch.epfl.scrumtool.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;

import javax.jdo.JDOObjectNotFoundException;

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

/**
 * 
 * @author
 * 
 */
public class ScrumMainTaskEndpointTest {

    // Since we use the High Replication Datastore we need to add
    // .setDefaultHightRepJob...

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(PERCENTAGE))
            .setEnvIsAdmin(true).setEnvIsLoggedIn(true);

    private final UserService userService = UserServiceFactory.getUserService();

    private static final int PERCENTAGE = 100;
    private static final String AUTH_DOMAIN = "epfl.ch";
    private static final ScrumProjectEndpoint PROJECT_ENDPOINT = new ScrumProjectEndpoint();
    private static final ScrumMainTaskEndpoint MAINTASK_ENDPOINT = new ScrumMainTaskEndpoint();
    private static final ScrumIssueEndpoint ISSUE_ENDPOINT = new ScrumIssueEndpoint();
    private static final ScrumUserEndpoint USER_ENDPOINT = new ScrumUserEndpoint();

    private static final String USER_KEY = "vincent.debieux@gmail.com";

    private static final String NAME = "Name";
    private static final String DESCRIPTION = "Description";
    private static final Priority PRIORITY = Priority.NORMAL;
    private static final Status STATUS = Status.READY_FOR_ESTIMATION;

    private ScrumProject project;
    private ScrumMainTask mainTask;

    @Before
    public void setUp() throws Exception {
        project = new ScrumProject();
        project.setDescription("description");
        project.setLastModDate(Calendar.getInstance().getTimeInMillis());
        project.setName("Project");
        project.setLastModUser(USER_KEY);

        mainTask = new ScrumMainTask();
        mainTask.setName(NAME);
        mainTask.setDescription(DESCRIPTION);
        mainTask.setPriority(PRIORITY);
        mainTask.setStatus(STATUS);

        helper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        helper.tearDown();
    }

    // Load Maintask tests
    @Test
    public void testLoadMainTaskExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.insertScrumMainTask(mainTask, projectKey,
                userLoggedIn());
        ArrayList<ScrumMainTask> tasks = new ArrayList<ScrumMainTask>(
                (HashSet<ScrumMainTask>) MAINTASK_ENDPOINT.loadMainTasks(
                        projectKey, userLoggedIn()).getItems());
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertNotNull(tasks.get(0));
        assertEquals(NAME, tasks.get(0).getName());
        assertEquals(DESCRIPTION, tasks.get(0).getDescription());
        assertEquals(PRIORITY, tasks.get(0).getPriority());
        assertEquals(Status.READY_FOR_ESTIMATION, tasks.get(0).getStatus());
        assertEquals(USER_KEY, tasks.get(0).getLastModUser());
    }

    @Test(expected = NotFoundException.class)
    public void testLoadMainTaskNonExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        MAINTASK_ENDPOINT.loadMainTasks("non existing key", userLoggedIn())
                .getItems();
        fail("loadMainTasks should throw a JDOObjectNotFoundException when given a non existing project");
    }

    @Test(expected = NullPointerException.class)
    public void testLoadMainTaskNullProject() throws ServiceException {
        loginUser(USER_KEY);
        MAINTASK_ENDPOINT.loadMainTasks(null, userLoggedIn()).getItems();
        fail("loadMainTasks should throw a NullPointerException when given a null projectKey");
    }

    @Test(expected = UnauthorizedException.class)
    public void testLoadMainTaskProjectNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.loadMainTasks(projectKey, userNotLoggedIn());
        fail("loadMainTasks should throw a UnauthorizedException when user is not logged in");
    }

    // Insert Maintask tests
    @Test
    public void testInsertMainTaskExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        String mainTaskKey = MAINTASK_ENDPOINT.insertScrumMainTask(mainTask,
                projectKey, userLoggedIn()).getKey();
        mainTask = PMF.get().getPersistenceManager()
                .getObjectById(ScrumMainTask.class, mainTaskKey);
        assertEquals(projectKey, mainTask.getProject().getKey());
        assertEquals(NAME, mainTask.getName());
        assertEquals(DESCRIPTION, mainTask.getDescription());
        assertEquals(PRIORITY, mainTask.getPriority());
        assertEquals(Status.READY_FOR_ESTIMATION, mainTask.getStatus());
        assertEquals(USER_KEY, mainTask.getLastModUser());
    }

    @Test(expected = NullPointerException.class)
    public void testInsertNullMainTask() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.insertScrumMainTask(null, projectKey, userLoggedIn());
        fail("insertScrumMainTask should throw a a NullPointerException when given a null mainTask");
    }

    @Test(expected = NullPointerException.class)
    public void testInsertMainTaskNullProjectKey() throws ServiceException {
        loginUser(USER_KEY);
        ScrumMainTask mainTask = new ScrumMainTask();
        MAINTASK_ENDPOINT.insertScrumMainTask(mainTask, null, userLoggedIn());
        fail("insertScrumMainTask should throw a a NullPointerException when given a null projectKey");
    }

    @Test(expected = NotFoundException.class)
    public void testInsertMainTaskNonExistingProject() throws ServiceException {
        loginUser(USER_KEY);
        ScrumMainTask mainTask = new ScrumMainTask();
        MAINTASK_ENDPOINT.insertScrumMainTask(mainTask,
                "non existing projectKey", userLoggedIn());
        fail("insertScrumMainTask should throw a a NotFoundException when given a non existing projectKey");
    }

    @Test(expected = UnauthorizedException.class)
    public void testInsertMainTaskNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        ScrumMainTask mainTask = new ScrumMainTask();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.insertScrumMainTask(mainTask, projectKey,
                userNotLoggedIn());
        fail("insertMainTask should throw a UnauthorizedException when user is not logged in");
    }

    // Update Maintask tests
    @Test
    public void testUpdateExistingMainTask() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.insertScrumMainTask(mainTask, projectKey,
                userLoggedIn());
        HashSet<ScrumMainTask> tasks = (HashSet<ScrumMainTask>) MAINTASK_ENDPOINT
                .loadMainTasks(projectKey, userLoggedIn()).getItems();
        ScrumMainTask mainTask = PMF
                .get()
                .getPersistenceManager()
                .getObjectById(ScrumMainTask.class,
                        tasks.iterator().next().getKey());
        mainTask.setName("test2");
        mainTask.setDescription("desc2");
        mainTask.setPriority(Priority.HIGH);
        mainTask.setStatus(Status.FINISHED);
        MAINTASK_ENDPOINT.updateScrumMainTask(mainTask, userLoggedIn());
        mainTask = PMF
                .get()
                .getPersistenceManager()
                .getObjectById(ScrumMainTask.class,
                        tasks.iterator().next().getKey());
        assertEquals(projectKey, mainTask.getProject().getKey());
        assertEquals("test2", mainTask.getName());
        assertEquals("desc2", mainTask.getDescription());
        assertEquals(Priority.HIGH, mainTask.getPriority());
        assertEquals(Status.READY_FOR_ESTIMATION, mainTask.getStatus());
        assertEquals(USER_KEY, mainTask.getLastModUser());
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateNonExistingMainTaskKey() throws ServiceException {
        loginUser(USER_KEY);
        ScrumMainTask mainTask = new ScrumMainTask();
        mainTask.setKey("non-existingKey");
        MAINTASK_ENDPOINT.updateScrumMainTask(mainTask, userLoggedIn());
        fail("updateScrumMainTask should throw a a NotFoundException when given a non existing mainTaskKey");
    }

    @Test(expected = NullPointerException.class)
    public void testUpdateNullMainTask() throws ServiceException {
        loginUser(USER_KEY);
        MAINTASK_ENDPOINT.updateScrumMainTask(null, userLoggedIn());
        fail("updateScrumMainTask should throw a a NullPointerException when given a null mainTaskKey");
    }

    @Test(expected = UnauthorizedException.class)
    public void testUpdateMainTaskNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.insertScrumMainTask(mainTask, projectKey,
                userLoggedIn());
        HashSet<ScrumMainTask> tasks = (HashSet<ScrumMainTask>) MAINTASK_ENDPOINT
                .loadMainTasks(projectKey, userLoggedIn()).getItems();
        ScrumMainTask mainTask = PMF
                .get()
                .getPersistenceManager()
                .getObjectById(ScrumMainTask.class,
                        tasks.iterator().next().getKey());
        MAINTASK_ENDPOINT.updateScrumMainTask(mainTask, userNotLoggedIn());
        fail("updateScrumMainTask should throw a a UnauthorizedException when the user is not logged in");
    }

    // Remove Maintask tests
    @Test
    public void testRemoveExistingMainTask() throws ServiceException {
        loginUser(USER_KEY);
        ScrumMainTask mainTask = new ScrumMainTask();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        String mainTaskKey = MAINTASK_ENDPOINT.insertScrumMainTask(mainTask,
                projectKey, userLoggedIn()).getKey();
        String issueKey = ISSUE_ENDPOINT.insertScrumIssue(new ScrumIssue(),
                mainTaskKey, null, null, userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.removeScrumMainTask(mainTaskKey, userLoggedIn());
        ScrumProject project = PMF.get().getPersistenceManager()
                .getObjectById(ScrumProject.class, projectKey);
        assertEquals(0, project.getBacklog().size());
        try {
            PMF.get().getPersistenceManager()
                    .getObjectById(ScrumIssue.class, issueKey);
        } catch (JDOObjectNotFoundException e) {
            // expected
        }
    }

    @Test(expected = NotFoundException.class)
    public void testRemoveNonExistingMainTask() throws ServiceException {
        loginUser(USER_KEY);
        MAINTASK_ENDPOINT
                .removeScrumMainTask("non-existingKey", userLoggedIn());
        fail("removeScrumMainTask should throw a JDOObjectNotFoundException when given a non existing mainTask");
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullMainTask() throws ServiceException {
        loginUser(USER_KEY);
        MAINTASK_ENDPOINT.removeScrumMainTask(null, userLoggedIn());
        fail("removeScrumMainTask should throw a NullPointerException when given a null mainTask");
    }

    @Test(expected = UnauthorizedException.class)
    public void testRemoveMainTaskNotLoggedIn() throws ServiceException {
        loginUser(USER_KEY);
        ScrumMainTask mainTask = new ScrumMainTask();
        String projectKey = PROJECT_ENDPOINT.insertScrumProject(project,
                userLoggedIn()).getKey();
        String mainTaskKey = MAINTASK_ENDPOINT.insertScrumMainTask(mainTask,
                projectKey, userLoggedIn()).getKey();
        MAINTASK_ENDPOINT.removeScrumMainTask(mainTaskKey, userNotLoggedIn());
        fail("removeScrumMainTask should throw an UnauthorizedException when the user is not logged in");
    }

    // // ComputeMaintaskInfos tests
    // @Test
    // public void testComputeMainTaskInfos() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testComputeMainTaskInfosNullMainTask() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testComputeMainTaskInfosforMaintaskWithNullIssues() {
    // fail("Not yet implemented");
    // }

    private ScrumUser loginUser(String email) throws ServiceException {
        return USER_ENDPOINT.loginUser(email, userLoggedIn());
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
