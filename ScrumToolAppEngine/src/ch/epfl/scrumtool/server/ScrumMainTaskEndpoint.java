package ch.epfl.scrumtool.server;

import java.util.Calendar;
import java.util.Set;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import ch.epfl.scrumtool.AppEngineUtils;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.users.User;

/**
 * 
 * @author aschneuw, sylb
 * 
 */

@Api(
        name = "scrumtool",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"),
        clientIds = {   Constants.ANDROID_CLIENT_ID_ARNO_MACBOOK, 
            Constants.ANDROID_CLIENT_ID_JOEY_DESKTOP, 
            Constants.ANDROID_CLIENT_ID_JOEY_LAPTOP,
            Constants.ANDROID_CLIENT_ID_LORIS_MACBOOK,
            Constants.ANDROID_CLIENT_ID_VINCENT_THINKPAD,
            Constants.ANDROID_CLIENT_ID_SYLVAIN_THINKPAD,
            Constants.ANDROID_CLIENT_ID_ALEX_MACBOOK,
            Constants.ANDROID_CLIENT_ID_VINCENT_LINUX,
            Constants.ANDROID_CLIENT_ID_CYRIAQUE_LAPTOP,
            Constants.ANDROID_CLIENT_ID_LEONARDO_THINKPAD,
            Constants.ANDROID_CLIENT_ID_ARNO_HP,
            Constants.ANDROID_CLIENT_ID_ARNO_THINKPAD
        },
        audiences = {
            Constants.ANDROID_AUDIENCE
            }
        )
public class ScrumMainTaskEndpoint {
    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrumMaintask
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumMainTask", path = "operationstatus/taskinsert")
    public KeyResponse insertScrumMainTask(ScrumMainTask scrumMaintask,
            @Named("projectKey") String projectKey, User user)
            throws ServiceException {
        if (projectKey == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);
        
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            ScrumProject scrumProject =
                    AppEngineUtils.getObjectFromDatastore(ScrumProject.class, projectKey, persistenceManager);
            
            transaction.begin();
            
            scrumProject.getBacklog().add(scrumMaintask);
            scrumMaintask.setLastModDate(lastDate);
            scrumMaintask.setLastModUser(lastUser);
            scrumProject.setLastModDate(lastDate);
            scrumProject.setLastModUser(lastUser);
            persistenceManager.makePersistent(scrumProject);
            transaction.commit();
            return new KeyResponse(scrumMaintask.getKey());
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    /**
     * @param projectKey
     * @param user
     * @return CollectionResponse<ScrumMainTask>
     * @throws ServiceException
     */
    @ApiMethod(name = "loadMainTasks")
    public CollectionResponse<ScrumMainTask> loadMainTasks(
            @Named("projectKey") String projectKey, User user)
            throws ServiceException {
        if (projectKey == null) {
            throw new NullPointerException();
        }

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();

        Set<ScrumMainTask> tasks = null;
        try {
            ScrumProject scrumProject = null;
            scrumProject = AppEngineUtils.getObjectFromDatastore(ScrumProject.class,
                    projectKey, persistenceManager);
            tasks = scrumProject.getBacklog();
            
            for (ScrumMainTask t: tasks) {
                computeMainTaskIssueInfo(t);
            }
            
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumMainTask>builder().setItems(tasks).build();
    }

    /**
     * This method is used for updating an existing entity. If the entity does
     * not exist in the datastore, an exception is thrown. It uses HTTP PUT
     * method.
     * 
     * @param scrumMaintask
     *            the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumMainTask", path = "operationstatus/taskupdate")
    public void updateScrumMainTask(ScrumMainTask update, User user)
            throws ServiceException {
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            transaction.begin();
            ScrumMainTask scrumMainTask = AppEngineUtils.getObjectFromDatastore(ScrumMainTask.class, update.getKey(),
                    persistenceManager);
            scrumMainTask.setDescription(update.getDescription());
            scrumMainTask.setLastModDate(Calendar.getInstance().getTimeInMillis());
            scrumMainTask.setLastModUser(user.getEmail());
            scrumMainTask.setName(update.getName());
            scrumMainTask.setStatus(update.getStatus());
            scrumMainTask.setPriority(update.getPriority());

            persistenceManager.makePersistent(scrumMainTask);
            transaction.commit();

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param mainTaskKey
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumMainTask", path = "operationstatus/removeTask")
    public void removeScrumMainTask(
            @Named("mainTaskKey") String mainTaskKey, User user)
            throws ServiceException {
        if (mainTaskKey == null) {
            throw new NullPointerException();
        }

        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumMainTask scrumMaintask = AppEngineUtils.getObjectFromDatastore(ScrumMainTask.class, mainTaskKey,
                    persistenceManager);
            transaction.begin();
            scrumMaintask.getProject().setLastModDate(Calendar.getInstance().getTimeInMillis());
            scrumMaintask.getProject().setLastModUser(user.getEmail());
            persistenceManager.makePersistent(scrumMaintask.getProject());
            persistenceManager.deletePersistent(scrumMaintask);
            transaction.commit();

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    public static void computeMainTaskIssueInfo(ScrumMainTask t) {
        float estimatedTime = 0;
        float estimatedTimeFinished = 0;
        int issuesFinished = 0;

        for (ScrumIssue i : t.getIssues()) {
            estimatedTime += i.getEstimation();
            if (i.getStatus() == Status.FINISHED) {
                issuesFinished++;
                estimatedTimeFinished += i.getEstimation();
            }
        }

        t.setTimeFinished(estimatedTimeFinished);
        t.setTotalIssues(t.getIssues().size());
        t.setTotalTime(estimatedTime);
        t.setIssuesFinished(issuesFinished);
    }

}
