package ch.epfl.scrumtool.server;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import ch.epfl.scrumtool.AppEngineUtils;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
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
            Constants.ANDROID_CLIENT_ID_ARNO_HP,
            Constants.ANDROID_CLIENT_ID_ARNO_THINKPAD
            },
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
    public KeyResponse insertScrumProject(ScrumProject scrumProject,
            User user) throws ServiceException {
        
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            
            ScrumUser scrumUser = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, user.getEmail(),
                    persistenceManager);

            ScrumPlayer scrumPlayer = new ScrumPlayer();
            scrumPlayer.setAdminFlag(true);
            scrumPlayer.setRole(Role.PRODUCT_OWNER);
            scrumPlayer.setUser(scrumUser);

            /**
             * An project insertion implies always an insertion of a new Player
             * corresponding to the user inserting the project. Therefore the
             * timestamp and lastermoduser tags are the same
             */
            scrumPlayer.setLastModDate(lastDate);
            scrumPlayer.setLastModUser(lastUser);
            
            Set<ScrumIssue> issues = new HashSet<ScrumIssue>();
            scrumPlayer.setIssues(issues);

            Set<ScrumPlayer> scrumPlayers = new HashSet<ScrumPlayer>();
            scrumPlayers.add(scrumPlayer);

            Set<ScrumSprint> scrumSprints = new HashSet<ScrumSprint>();
            scrumProject.setSprints(scrumSprints);

            scrumProject.setBacklog(new HashSet<ScrumMainTask>());
            scrumProject.setLastModDate(lastDate);
            scrumProject.setLastModUser(lastUser);

            scrumUser.addPlayer(scrumPlayer);
            
            transaction.begin();

            persistenceManager.makePersistent(scrumUser);
            scrumProject.setPlayers(scrumPlayers);
            persistenceManager.makePersistent(scrumProject);
            scrumPlayer.setProject(scrumProject);
            persistenceManager.makePersistent(scrumPlayer);
            transaction.commit();
            return new KeyResponse(scrumProject.getKey());

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
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
    public void updateScrumProject(ScrumProject update, User user)
            throws ServiceException {
        
        AppEngineUtils.basicAuthentication(user);
        
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            ScrumProject scrumProject = AppEngineUtils.getObjectFromDatastore(ScrumProject.class, update.getKey(),
                    persistenceManager);
            transaction.begin();
            scrumProject.setDescription(update.getDescription());
            scrumProject.setLastModDate(Calendar.getInstance().getTimeInMillis());
            scrumProject.setLastModUser(user.getEmail());
            scrumProject.setName(update.getName());
            persistenceManager.makePersistent(scrumProject);
            transaction.commit();

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param id
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumProject", path = "operationstatus/removeProject")
    public void removeScrumProject(
            @Named("projectKey") String projectKey, User user)
            throws ServiceException {
        if (projectKey == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            transaction.begin();
            ScrumProject scrumproject = AppEngineUtils.getObjectFromDatastore(ScrumProject.class, projectKey,
                    persistenceManager);
            for (ScrumPlayer p : scrumproject.getPlayers()) {
                p.getUser().setLastModDate(Calendar.getInstance().getTimeInMillis());
                p.getUser().setLastModUser(user.getEmail());
                persistenceManager.makePersistent(p.getUser());
                persistenceManager.deletePersistent(p);
            }
    
            // Tasks, issues and sprints are deleted automatically (owned relationship)
            persistenceManager.deletePersistent(scrumproject);
            transaction.commit();
            
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

}
