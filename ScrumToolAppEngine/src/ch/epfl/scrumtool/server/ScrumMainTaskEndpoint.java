package ch.epfl.scrumtool.server;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import ch.epfl.scrumtool.AppEngineUtils;
import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

/**
 * 
 * @author aschneuw, sylb
 * 
 */

@Api(name = "scrumtool", 
version = "v1", 
    namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"), 
    clientIds = {
        Constants.ANDROID_CLIENT_ID_ARNO_MACBOOK,
        Constants.ANDROID_CLIENT_ID_JOEY_DESKTOP,
        Constants.ANDROID_CLIENT_ID_JOEY_LAPTOP,
        Constants.ANDROID_CLIENT_ID_LORIS_MACBOOK,
        Constants.ANDROID_CLIENT_ID_VINCENT_THINKPAD,
        Constants.ANDROID_CLIENT_ID_SYLVAIN_THINKPAD,
        Constants.ANDROID_CLIENT_ID_ALEX_MACBOOK,
        Constants.ANDROID_CLIENT_ID_VINCENT_LINUX }, audiences = { Constants.ANDROID_AUDIENCE })
public class ScrumMainTaskEndpoint {

    /**
     * This method gets the entity having primary key id. It uses HTTP GET
     * method.
     * 
     * @param id
     *            the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumMainTask")
    public ScrumMainTask getScrumMainTask(@Named("id") String id, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        ScrumMainTask scrummaintask = null;
        try {
            scrummaintask = mgr.getObjectById(ScrumMainTask.class, id);
        } finally {
            mgr.close();
        }
        return scrummaintask;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrummaintask
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumMainTask")
    public ScrumMainTask insertScrumMainTask(ScrumMainTask scrummaintask, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        try {
            mgr.makePersistent(scrummaintask);
        } finally {
            mgr.close();
        }
        return scrummaintask;
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
    @ApiMethod(name = "updateScrumMainTask")
    public ScrumMainTask updateScrumMainTask(ScrumMainTask scrummaintask, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (!containsScrumMainTask(scrummaintask)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            mgr.makePersistent(scrummaintask);
        } finally {
            mgr.close();
        }
        return scrummaintask;
    }

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param id
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumMainTask")
    public void removeScrumMainTask(@Named("id") String id, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumMainTask scrummaintask = mgr.getObjectById(
                    ScrumMainTask.class, id);
            mgr.deletePersistent(scrummaintask);
        } finally {
            mgr.close();
        }
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
        return CollectionResponse.<ScrumIssue> builder().setItems(execute).build();
    }

}
