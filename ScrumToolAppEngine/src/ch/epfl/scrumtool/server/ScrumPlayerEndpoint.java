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
            Constants.ANDROID_CLIENT_ID_VINCENT_LINUX},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumPlayerEndpoint {

    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumPlayer")
    public ScrumPlayer getScrumPlayer(@Named("id") String id, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        ScrumPlayer scrumplayer = null;
        try {
            scrumplayer = mgr.getObjectById(ScrumPlayer.class, id);
        } finally {
            mgr.close();
        }
        return scrumplayer;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity already
     * exists in the datastore, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param scrumplayer the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumPlayer")
    public ScrumPlayer insertScrumPlayer(ScrumPlayer scrumplayer, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (containsScrumPlayer(scrumplayer)) {
                throw new EntityExistsException("Object already exists");
            }
            mgr.makePersistent(scrumplayer);
        } finally {
            mgr.close();
        }
        return scrumplayer;
    }

    /**
     * This method is used for updating an existing entity. If the entity does not
     * exist in the datastore, an exception is thrown.
     * It uses HTTP PUT method.
     *
     * @param scrumplayer the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumPlayer")
    public ScrumPlayer updateScrumPlayer(ScrumPlayer scrumplayer, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (!containsScrumPlayer(scrumplayer)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            mgr.makePersistent(scrumplayer);
        } finally {
            mgr.close();
        }
        return scrumplayer;
    }

    /**
     * This method removes the entity with primary key id.
     * It uses HTTP DELETE method.
     *
     * @param id the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumPlayer")
    public void removeScrumPlayer(@Named("id") String id, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumPlayer scrumplayer = mgr.getObjectById(ScrumPlayer.class, id);
            mgr.deletePersistent(scrumplayer);
        } finally {
            mgr.close();
        }
    }

    private boolean containsScrumPlayer(ScrumPlayer scrumplayer) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumPlayer.class, scrumplayer.getKey());
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
