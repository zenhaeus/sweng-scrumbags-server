package ch.epfl.scrumtool.server;

import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(
        name = "scrumtool",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"),
        clientIds = {Constants.ANDROID_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumIssueEndpoint {

    /**
     * This method lists all the entities inserted in datastore.
     * It uses HTTP GET method and paging support.
     *
     * @return A CollectionResponse class containing the list of all entities
     * persisted and a cursor to the next page.
     */
    @SuppressWarnings({ "unchecked", "unused" })
    @ApiMethod(name = "listScrumIssue")
    public CollectionResponse<ScrumIssue> listScrumIssue(
            @Nullable @Named("cursor") String cursorString,
            @Nullable @Named("limit") Integer limit) {

        PersistenceManager mgr = null;
        Cursor cursor = null;
        List<ScrumIssue> execute = null;

        try {
            mgr = getPersistenceManager();
            Query query = mgr.newQuery(ScrumIssue.class);
            if (cursorString != null && cursorString != "") {
                cursor = Cursor.fromWebSafeString(cursorString);
                HashMap<String, Object> extensionMap = new HashMap<String, Object>();
                extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
                query.setExtensions(extensionMap);
            }

            if (limit != null) {
                query.setRange(0, limit);
            }

            execute = (List<ScrumIssue>) query.execute();
            cursor = JDOCursorHelper.getCursor(execute);
            if (cursor != null)
                cursorString = cursor.toWebSafeString();

            // Tight loop for fetching all entities from datastore and accomodate
            // for lazy fetch.
            for (ScrumIssue obj : execute)
                ;
        } finally {
            mgr.close();
        }

        return CollectionResponse.<ScrumIssue> builder().setItems(execute)
                .setNextPageToken(cursorString).build();
    }

    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumIssue")
    public ScrumIssue getScrumIssue(@Named("id") String id) {
        PersistenceManager mgr = getPersistenceManager();
        ScrumIssue scrumissue = null;
        try {
            scrumissue = mgr.getObjectById(ScrumIssue.class, id);
        } finally {
            mgr.close();
        }
        return scrumissue;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity already
     * exists in the datastore, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param scrumissue the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumIssue")
    public ScrumIssue insertScrumIssue(ScrumIssue scrumissue) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (containsScrumIssue(scrumissue)) {
                throw new EntityExistsException("Object already exists");
            }
            mgr.makePersistent(scrumissue);
        } finally {
            mgr.close();
        }
        return scrumissue;
    }

    /**
     * This method is used for updating an existing entity. If the entity does not
     * exist in the datastore, an exception is thrown.
     * It uses HTTP PUT method.
     *
     * @param scrumissue the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumIssue")
    public ScrumIssue updateScrumIssue(ScrumIssue scrumissue) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (!containsScrumIssue(scrumissue)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            mgr.makePersistent(scrumissue);
        } finally {
            mgr.close();
        }
        return scrumissue;
    }

    /**
     * This method removes the entity with primary key id.
     * It uses HTTP DELETE method.
     *
     * @param id the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumIssue")
    public void removeScrumIssue(@Named("id") String id) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumIssue scrumissue = mgr.getObjectById(ScrumIssue.class, id);
            mgr.deletePersistent(scrumissue);
        } finally {
            mgr.close();
        }
    }

    private boolean containsScrumIssue(ScrumIssue scrumissue) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumIssue.class, scrumissue.getKey());
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

}
