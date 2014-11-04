package ch.epfl.scrumtool.server;

import java.util.HashSet;
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
        clientIds = {Constants.ANDROID_CLIENT_ID_ARNO_MACBOOK, Constants.ANDROID_CLIENT_ID_JOEY_DESKTOP},
        audiences = {Constants.ANDROID_AUDIENCE}
        )

public class ScrumProjectEndpoint {
    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumProject")
    public ScrumProject getScrumProject(@Named("id") String id, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
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
    public ScrumProject insertScrumProject(ScrumProject scrumproject, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        try {
            String userKey = scrumproject.getLastModUser();
            ScrumUser scrumUser = mgr.getObjectById(ScrumUser.class, userKey);
            
            ScrumPlayer scrumPlayer = new ScrumPlayer();
            scrumPlayer.setAdminFlag(true);
            scrumPlayer.setRole(Role.PRODUCT_OWNER);
            
            /**
             * An project insertion implies always an insertion of a new Player corresponding
             * to the user inserting the project. Therefore the timestamp and lastermoduser tags
             * are the same
             */
            scrumPlayer.setLastModDate(scrumproject.getLastModDate());
            scrumPlayer.setLastModUser(scrumproject.getLastModUser());
            scrumPlayer.setUser(scrumUser);
            
            Set<ScrumPlayer> scrumPlayers = new HashSet<ScrumPlayer>();
            scrumPlayers.add(scrumPlayer);
            scrumproject.setPlayers(scrumPlayers);
            
            Set<ScrumSprint> scrumSprints = new HashSet<ScrumSprint>();
            scrumproject.setSprints(scrumSprints);
            
            scrumproject.setBacklog(new HashSet<ScrumMainTask>());
            
            tx.begin();
            mgr.makePersistent(scrumproject);
            tx.commit();
            
            
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
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
    public ScrumProject updateScrumProject(ScrumProject scrumproject, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
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
    public void removeScrumProject(@Named("id") String id, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
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
