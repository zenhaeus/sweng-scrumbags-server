package ch.epfl.scrumtool.server;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
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
            Constants.ANDROID_CLIENT_ID_CYRIAQUE_LAPTOP},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumMainTaskEndpoint {
    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrummaintask
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumMainTask", path="operationstatus/taskinsert")
    public OperationStatus insertScrumMainTask(ScrumMainTask scrummaintask, 
            @Named("projectKey") String projectKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        ScrumProject project = mgr.getObjectById(ScrumProject.class, projectKey);
        project.getBacklog().add(scrummaintask);
        try {
            tx.begin();
            mgr.makePersistent(project);
            tx.commit();
            opStatus.setKey(scrummaintask.getKey());
            opStatus.setSuccess(true);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            mgr.close();
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
            @Named("projectKey") String projectKey,
            User user) throws OAuthRequestException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        
        
        Set<ScrumMainTask> tasks = null;
        try {
            ScrumProject scrumproject = null;
            scrumproject = mgr.getObjectById(ScrumProject.class, projectKey);
            tasks = scrumproject.getBacklog();

            
            
        } finally {
            mgr.close();
        }
        return CollectionResponse.<ScrumMainTask>builder().setItems(tasks)
                .build();
    }

    /**
     * This method is used for updating an existing entity. If the entity does
     * not exist in the datastore, an exception is thrown. It uses HTTP PUT
     * method.
     * 
     * @param scrummaintask
     *            the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumMainTask", path="operationstatus/taskupdate")
    public OperationStatus updateScrumMainTask(ScrumMainTask scrummaintask, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        try {
            if (!containsScrumMainTask(scrummaintask)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            tx.begin();
            mgr.makePersistent(scrummaintask);
            tx.commit();
            
            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
            opStatus.setKey(scrummaintask.getKey());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
                opStatus = new OperationStatus();
                opStatus.setSuccess(true);
            }
            mgr.close();
        }
        return opStatus;
    }

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param id
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumMainTask", path="operationstatus/removeTask")
    public OperationStatus removeScrumMainTask(@Named("id") String id, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumMainTask scrummaintask = mgr.getObjectById(
                    ScrumMainTask.class, id);
            mgr.deletePersistent(scrummaintask);
            opStatus = new OperationStatus();
            opStatus.setKey(id);
            opStatus.setSuccess(true);
        } finally {
            
            mgr.close();
        }
        return opStatus;
    }

    private boolean containsScrumMainTask(ScrumMainTask scrummaintask) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumMainTask.class, scrummaintask.getKey());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            mgr.close();
        }
        return contains;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }
    
    
    /**
     * This method lists all the ScrumIssues inserted in datastore.
     * It uses HTTP GET method and paging support.
     *
     * @return A CollectionResponse class containing the list of all entities
     * persisted and a cursor to the next page.
     */
    @SuppressWarnings({ "unchecked" })
    @ApiMethod(name = "loadScrumIssues")
    public CollectionResponse<ScrumIssue> loadAllScrumIssue(
            @Named("id") String id,
            @Nullable @Named("limit") Integer limit,
            User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = null;
        List<ScrumIssue> execute = null;
        
        try {
            mgr = getPersistenceManager();
            Query query = mgr.newQuery(ScrumIssue.class);
            if (limit != null) {
                query.setRange(0, limit);
            }
            execute = (List<ScrumIssue>) query.execute();
        } finally {
            mgr.close();
        }
        return CollectionResponse.<ScrumIssue>builder().setItems(execute).build();
    }

}
