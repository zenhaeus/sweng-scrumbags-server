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
public class ScrumProjectEndpoint {

    /**
     * This method lists all the entities inserted in datastore.
     * It uses HTTP GET method and paging support.
     *
     * @return A CollectionResponse class containing the list of all entities
     * persisted and a cursor to the next page.
     */
    @SuppressWarnings({ "unchecked", "unused" })
    @ApiMethod(name = "listScrumProject")
    public CollectionResponse<ScrumProject> listScrumProject(
            @Nullable @Named("cursor") String cursorString,
            @Nullable @Named("limit") Integer limit) {

        PersistenceManager mgr = null;
        Cursor cursor = null;
        List<ScrumProject> execute = null;

        try {
            mgr = getPersistenceManager();
            Query query = mgr.newQuery(ScrumProject.class);
            if (cursorString != null && cursorString != "") {
                cursor = Cursor.fromWebSafeString(cursorString);
                HashMap<String, Object> extensionMap = new HashMap<String, Object>();
                extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
                query.setExtensions(extensionMap);
            }

            if (limit != null) {
                query.setRange(0, limit);
            }

            execute = (List<ScrumProject>) query.execute();
            cursor = JDOCursorHelper.getCursor(execute);
            if (cursor != null)
                cursorString = cursor.toWebSafeString();

            // Tight loop for fetching all entities from datastore and accomodate
            // for lazy fetch.
            for (ScrumProject obj : execute)
                ;
        } finally {
            mgr.close();
        }

        return CollectionResponse.<ScrumProject> builder().setItems(execute)
                .setNextPageToken(cursorString).build();
    }

    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumProject")
    public ScrumProject getScrumProject(@Named("id") String id) {
        PersistenceManager mgr = getPersistenceManager();
        ScrumProject scrumproject = null;
        try {
            scrumproject = mgr.getObjectById(ScrumProject.class, id);
        } finally {
            mgr.close();
        }
        return scrumproject;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity already
     * exists in the datastore, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param scrumproject the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumProject")
    public ScrumProject insertScrumProject(ScrumProject scrumproject) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (containsScrumProject(scrumproject)) {
                throw new EntityExistsException("Object already exists");
            }
            mgr.makePersistent(scrumproject);
        } finally {
            mgr.close();
        }
        return scrumproject;
    }

    /**
     * This method is used for updating an existing entity. If the entity does not
     * exist in the datastore, an exception is thrown.
     * It uses HTTP PUT method.
     *
     * @param scrumproject the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumProject")
    public ScrumProject updateScrumProject(ScrumProject scrumproject) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (!containsScrumProject(scrumproject)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            mgr.makePersistent(scrumproject);
        } finally {
            mgr.close();
        }
        return scrumproject;
    }

    /**
     * This method removes the entity with primary key id.
     * It uses HTTP DELETE method.
     *
     * @param id the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumProject")
    public void removeScrumProject(@Named("id") String id) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumProject scrumproject = mgr.getObjectById(ScrumProject.class,
                    id);
            mgr.deletePersistent(scrumproject);
        } finally {
            mgr.close();
        }
    }

    private boolean containsScrumProject(ScrumProject scrumproject) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumProject.class, scrumproject.getKey());
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
