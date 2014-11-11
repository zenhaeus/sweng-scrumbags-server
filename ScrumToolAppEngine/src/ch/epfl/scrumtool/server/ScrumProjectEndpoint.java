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
            Constants.ANDROID_CLIENT_ID_LEONARDO_THINKPAD,
            Constants.ANDROID_CLIENT_ID_ARNO_HP},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumProjectEndpoint {
    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrumProject
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumProject", path = "operationstatus/insertProject")
    public OperationStatus insertScrumProject(ScrumProject scrumProject,
            User user) throws OAuthRequestException {
        
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            String userKey = scrumProject.getLastModUser();
            ScrumUser scrumUser = persistenceManager.getObjectById(ScrumUser.class, userKey);

            ScrumPlayer scrumPlayer = new ScrumPlayer();
            scrumPlayer.setAdminFlag(true);
            scrumPlayer.setRole(Role.PRODUCT_OWNER);

            /**
             * An project insertion implies always an insertion of a new Player
             * corresponding to the user inserting the project. Therefore the
             * timestamp and lastermoduser tags are the same
             */
            scrumPlayer.setLastModDate(scrumProject.getLastModDate());
            scrumPlayer.setLastModUser(scrumProject.getLastModUser());
            
            Set<ScrumIssue> issues = new HashSet<ScrumIssue>();
            scrumPlayer.setIssues(issues);

            Set<ScrumPlayer> scrumPlayers = new HashSet<ScrumPlayer>();
            scrumPlayers.add(scrumPlayer);

            Set<ScrumSprint> scrumSprints = new HashSet<ScrumSprint>();
            scrumProject.setSprints(scrumSprints);

            scrumProject.setBacklog(new HashSet<ScrumMainTask>());

            scrumUser.addPlayer(scrumPlayer);
            transaction.begin();
            persistenceManager.makePersistent(scrumUser);
            scrumProject.setPlayers(scrumPlayers);
            persistenceManager.makePersistent(scrumProject);
            scrumPlayer.setProject(scrumProject);
            transaction.commit();
            opStatus.setKey(scrumProject.getKey());
            opStatus.setSuccess(true);

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
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
    @ApiMethod(name = "updateScrumProject", path = "operationstatus/updateProject")
    public OperationStatus updateScrumProject(ScrumProject update, User user)
            throws OAuthRequestException {
        OperationStatus opStatus =  new OperationStatus();
        opStatus.setSuccess(false);
        
        AppEngineUtils.basicAuthentication(user);
        
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            ScrumProject scrumProject = persistenceManager.getObjectById(ScrumProject.class, update.getKey());
            scrumProject.setDescription(update.getDescription());
            scrumProject.setLastModDate(update.getLastModDate());
            scrumProject.setLastModUser(update.getLastModUser());
            scrumProject.setName(update.getName());
            
            transaction.begin();
            persistenceManager.makePersistent(scrumProject);
            transaction.commit();

            opStatus.setSuccess(true);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
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
    @ApiMethod(name = "removeScrumProject", path = "operationstatus/removeProject")
    public OperationStatus removeScrumProject(
            @Named("projectKey") String projectKey, User user)
            throws OAuthRequestException {
        
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumProject scrumproject = persistenceManager.getObjectById(ScrumProject.class, projectKey);
            for (ScrumPlayer p : scrumproject.getPlayers()) {
                persistenceManager.deletePersistent(p);
            }
            
            transaction.begin();
            // Tasks and sprints are deleted automatically (owned relationship)
            persistenceManager.deletePersistent(scrumproject);
            transaction.commit();

            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
            
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return opStatus;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

}
