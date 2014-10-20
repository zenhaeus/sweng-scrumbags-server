package ch.epfl.server;

import ch.epfl.scrumtool.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "getuserendpoint", namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "server"))
public class GetUserEndpoint {

    /**
     * This method lists all the entities inserted in datastore.
     * It uses HTTP GET method and paging support.
     *
     * @return A CollectionResponse class containing the list of all entities
     * persisted and a cursor to the next page.
     */
    @SuppressWarnings({ "unchecked", "unused" })
    @ApiMethod(name = "listGetUser")
    public CollectionResponse<GetUser> listGetUser(
            @Nullable @Named("cursor") String cursorString,
            @Nullable @Named("limit") Integer limit) {

        EntityManager mgr = null;
        Cursor cursor = null;
        List<GetUser> execute = null;

        try {
            mgr = getEntityManager();
            Query query = mgr.createQuery("select from GetUser as GetUser");
            if (cursorString != null && cursorString != "") {
                cursor = Cursor.fromWebSafeString(cursorString);
                query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
            }

            if (limit != null) {
                query.setFirstResult(0);
                query.setMaxResults(limit);
            }

            execute = (List<GetUser>) query.getResultList();
            cursor = JPACursorHelper.getCursor(execute);
            if (cursor != null)
                cursorString = cursor.toWebSafeString();

            // Tight loop for fetching all entities from datastore and accomodate
            // for lazy fetch.
            for (GetUser obj : execute)
                ;
        } finally {
            mgr.close();
        }

        return CollectionResponse.<GetUser> builder().setItems(execute)
                .setNextPageToken(cursorString).build();
    }

    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getGetUser")
    public GetUser getGetUser(@Named("id") Long id) {
        EntityManager mgr = getEntityManager();
        GetUser getuser = null;
        try {
            getuser = mgr.find(GetUser.class, id);
        } finally {
            mgr.close();
        }
        return getuser;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity already
     * exists in the datastore, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param getuser the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertGetUser")
    public GetUser insertGetUser(GetUser getuser) {
        EntityManager mgr = getEntityManager();
        try {
            if (containsGetUser(getuser)) {
                throw new EntityExistsException("Object already exists");
            }
            mgr.persist(getuser);
        } finally {
            mgr.close();
        }
        return getuser;
    }

    /**
     * This method is used for updating an existing entity. If the entity does not
     * exist in the datastore, an exception is thrown.
     * It uses HTTP PUT method.
     *
     * @param getuser the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateGetUser")
    public GetUser updateGetUser(GetUser getuser) {
        EntityManager mgr = getEntityManager();
        try {
            if (!containsGetUser(getuser)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            mgr.persist(getuser);
        } finally {
            mgr.close();
        }
        return getuser;
    }

    /**
     * This method removes the entity with primary key id.
     * It uses HTTP DELETE method.
     *
     * @param id the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeGetUser")
    public void removeGetUser(@Named("id") Long id) {
        EntityManager mgr = getEntityManager();
        try {
            GetUser getuser = mgr.find(GetUser.class, id);
            mgr.remove(getuser);
        } finally {
            mgr.close();
        }
    }

    private boolean containsGetUser(GetUser getuser) {
        EntityManager mgr = getEntityManager();
        boolean contains = true;
        try {
            if(getuser.getKey() == null) {
                return false;
            }
            GetUser item = mgr.find(GetUser.class, getuser.getKey());
            if (item == null) {
                contains = false;
            }
        } finally {
            mgr.close();
        }
        return contains;
    }

    private static EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

}
