package ch.epfl.scrumtool.server;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import ch.epfl.scrumtool.AppEngineUtils;
import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;


/**
 * 
 * @author aschneuw
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
public class ScrumSprintEndpoint {
    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumSprint")
    public ScrumSprint getScrumSprint(@Named("id") String id, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
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
    public ScrumSprint insertScrumSprint(ScrumSprint scrumsprint, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
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
    public ScrumSprint updateScrumSprint(ScrumSprint scrumsprint, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
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
    public void removeScrumSprint(@Named("id") String id, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
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
