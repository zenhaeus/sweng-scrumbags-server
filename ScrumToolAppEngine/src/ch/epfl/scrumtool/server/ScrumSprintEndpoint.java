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


/**
 * 
 * @author aschneuw
 *
 */


@Api(
        name = "scrumtool",
        version = "v1",
        namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"),
        clientIds = {Constants.ANDROID_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumSprintEndpoint {

    /**
     * This method lists all the entities inserted in datastore.
     * It uses HTTP GET method and paging support.
     *
     * @return A CollectionResponse class containing the list of all entities
     * persisted and a cursor to the next page.
     */
    @SuppressWarnings({ "unchecked", "unused" })
    @ApiMethod(name = "listScrumSprint")
    public CollectionResponse<ScrumSprint> listScrumSprint(
            @Nullable @Named("cursor") String cursorString,
            @Nullable @Named("limit") Integer limit) {

        PersistenceManager mgr = null;
        Cursor cursor = null;
        List<ScrumSprint> execute = null;

        try {
            mgr = getPersistenceManager();
            Query query = mgr.newQuery(ScrumSprint.class);
            if (cursorString != null && !cursorString.equals(Constants.EMPTY_STRING)) {
                cursor = Cursor.fromWebSafeString(cursorString);
                HashMap<String, Object> extensionMap = new HashMap<String, Object>();
                extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
                query.setExtensions(extensionMap);
            }

            if (limit != null) {
                query.setRange(0, limit);
            }

            execute = (List<ScrumSprint>) query.execute();
            cursor = JDOCursorHelper.getCursor(execute);
            if (cursor != null) {
                cursorString = cursor.toWebSafeString();
            }

            
            for (ScrumSprint obj : execute) {
                // Tight loop for fetching all entities from datastore and accomodate
                // for lazy fetch.
            }
                
        } finally {
            mgr.close();
        }

        return CollectionResponse.<ScrumSprint>builder().setItems(execute)
                .setNextPageToken(cursorString).build();
    }

    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumSprint")
    public ScrumSprint getScrumSprint(@Named("id") String id) {
        PersistenceManager mgr = getPersistenceManager();
        ScrumSprint scrumsprint = null;
        try {
            scrumsprint = mgr.getObjectById(ScrumSprint.class, id);
        } finally {
            mgr.close();
        }
        return scrumsprint;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity already
     * exists in the datastore, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param scrumsprint the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumSprint")
    public ScrumSprint insertScrumSprint(ScrumSprint scrumsprint) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (containsScrumSprint(scrumsprint)) {
                throw new EntityExistsException("Object already exists");
            }
            mgr.makePersistent(scrumsprint);
        } finally {
            mgr.close();
        }
        return scrumsprint;
    }

    /**
     * This method is used for updating an existing entity. If the entity does not
     * exist in the datastore, an exception is thrown.
     * It uses HTTP PUT method.
     *
     * @param scrumsprint the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumSprint")
    public ScrumSprint updateScrumSprint(ScrumSprint scrumsprint) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (!containsScrumSprint(scrumsprint)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            mgr.makePersistent(scrumsprint);
        } finally {
            mgr.close();
        }
        return scrumsprint;
    }

    /**
     * This method removes the entity with primary key id.
     * It uses HTTP DELETE method.
     *
     * @param id the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumSprint")
    public void removeScrumSprint(@Named("id") String id) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumSprint scrumsprint = mgr.getObjectById(ScrumSprint.class, id);
            mgr.deletePersistent(scrumsprint);
        } finally {
            mgr.close();
        }
    }

    private boolean containsScrumSprint(ScrumSprint scrumsprint) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumSprint.class, scrumsprint.getKey());
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
