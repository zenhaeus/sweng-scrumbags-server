package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

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
            Constants.ANDROID_CLIENT_ID_CYRIAQUE_LAPTOP,
            Constants.ANDROID_CLIENT_ID_LEONARDO_THINKPAD},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumProjectEndpoint {
    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrumproject
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumProject", path="operationstatus/insertProject")
    public OperationStatus insertScrumProject(ScrumProject scrumproject, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus;
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        try {
            String userKey = scrumproject.getLastModUser();
            ScrumUser scrumUser = mgr.getObjectById(ScrumUser.class, userKey);

            ScrumPlayer scrumPlayer = new ScrumPlayer();
            scrumPlayer.setAdminFlag(true);
            scrumPlayer.setRole(Role.PRODUCT_OWNER);

            /**
             * An project insertion implies always an insertion of a new Player
             * corresponding to the user inserting the project. Therefore the
             * timestamp and lastermoduser tags are the same
             */
            scrumPlayer.setLastModDate(scrumproject.getLastModDate());
            scrumPlayer.setLastModUser(scrumproject.getLastModUser());
            

            Set<ScrumPlayer> scrumPlayers = new HashSet<ScrumPlayer>();
            scrumPlayers.add(scrumPlayer);
            

            Set<ScrumSprint> scrumSprints = new HashSet<ScrumSprint>();
            scrumproject.setSprints(scrumSprints);

            scrumproject.setBacklog(new HashSet<ScrumMainTask>());
            
            
            
            

            scrumUser.addPlayer(scrumPlayer);
            tx.begin();
            mgr.makePersistent(scrumUser);
            tx.commit();
            scrumproject.setPlayers(scrumPlayers);
            tx.begin();
            mgr.makePersistent(scrumproject);
            tx.commit();
            scrumPlayer.setProject(scrumproject);
            tx.begin();
            mgr.makePersistent(scrumproject);
            tx.commit();
            opStatus = new OperationStatus();
            opStatus.setKey(scrumproject.getKey());
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
     * This method is used for updating an existing entity. If the entity does
     * not exist in the datastore, an exception is thrown. It uses HTTP PUT
     * method.
     * 
     * @param scrumproject
     *            the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumProject", path="operationstatus/updateProject")
    public OperationStatus updateScrumProject(ScrumProject update, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = null;
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumProject project = mgr.getObjectById(ScrumProject.class, update.getKey());
            project.setDescription(update.getDescription());
            project.setLastModDate(update.getLastModDate());
            project.setLastModUser(update.getLastModUser());
            project.setName(update.getName());
            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
        } finally {
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
    @ApiMethod(name = "removeScrumProject", path="operationstatus/removeProject")
    public OperationStatus removeScrumProject(@Named("projectKey") String projectKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        OperationStatus opStatus = null;
        try {
            ScrumProject scrumproject = mgr.getObjectById(ScrumProject.class,
                    projectKey);
            mgr.deletePersistent(scrumproject);
            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
        } finally {
            mgr.close();
        }
        return opStatus;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

}
