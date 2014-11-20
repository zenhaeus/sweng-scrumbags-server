package ch.epfl.scrumtool.server;

import java.util.Set;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.persistence.EntityNotFoundException;

import ch.epfl.scrumtool.AppEngineUtils;
import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.oauth.OAuthRequestException;
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
            Constants.ANDROID_CLIENT_ID_ARNO_HP},
        audiences = {Constants.ANDROID_AUDIENCE}
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
    public OperationStatus insertScrumMainTask(ScrumMainTask scrumMaintask,
            @Named("projectKey") String projectKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            ScrumProject scrumProject = persistenceManager.getObjectById(
                    ScrumProject.class, projectKey);
            transaction.begin();

            scrumProject.getBacklog().add(scrumMaintask);
            persistenceManager.makePersistent(scrumProject);
            transaction.commit();
            opStatus.setKey(scrumMaintask.getKey());
            opStatus.setSuccess(true);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }

        return opStatus;
    }

    /**
     * @param projectKey
     * @param user
     * @return CollectionResponse<ScrumMainTask>
     * @throws OAuthRequestException
     */
    @ApiMethod(name = "loadMainTasks")
    public CollectionResponse<ScrumMainTask> loadMainTasks(
            @Named("projectKey") String projectKey, User user)
            throws OAuthRequestException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();

        Set<ScrumMainTask> tasks = null;
        try {
            ScrumProject scrumProject = null;
            scrumProject = persistenceManager.getObjectById(ScrumProject.class,
                    projectKey);
            tasks = scrumProject.getBacklog();
            
            //Lazy Fetch
            for (ScrumMainTask t : tasks) {
                for (ScrumIssue i : t.getIssues()) {
                    i.getAssignedPlayer();
                    i.getSprint();
                }
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
    public OperationStatus updateScrumMainTask(ScrumMainTask update, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            if (!containsScrumMainTask(update)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            transaction.begin();
            ScrumMainTask scrumMainTask = persistenceManager.getObjectById(
                    ScrumMainTask.class, update.getKey());
            scrumMainTask.setDescription(update.getDescription());
            scrumMainTask.setLastModDate(update.getLastModDate());
            scrumMainTask.setLastModUser(update.getLastModUser());
            scrumMainTask.setName(update.getName());
            scrumMainTask.setStatus(update.getStatus());
            scrumMainTask.setPriority(update.getPriority());

            persistenceManager.makePersistent(scrumMainTask);
            transaction.commit();

            opStatus.setSuccess(true);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return opStatus;
    }

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param mainTaskKey
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumMainTask", path = "operationstatus/removeTask")
    public OperationStatus removeScrumMainTask(
            @Named("mainTaskKey") String mainTaskKey, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumMainTask scrumMaintask = persistenceManager.getObjectById(
                    ScrumMainTask.class, mainTaskKey);
            transaction.begin();
            persistenceManager.deletePersistent(scrumMaintask);
            transaction.commit();

            opStatus.setKey(mainTaskKey);
            opStatus.setSuccess(true);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return opStatus;
    }

    /**
     * Return true if the DS contains the Maintaks
     * 
     * @param scrumMaintask
     * @return
     */
    private boolean containsScrumMainTask(ScrumMainTask scrumMaintask) {
        PersistenceManager persistenceManager = getPersistenceManager();
        boolean contains = true;
        try {
            persistenceManager.getObjectById(ScrumMainTask.class,
                    scrumMaintask.getKey());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            persistenceManager.close();
        }
        return contains;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

}
