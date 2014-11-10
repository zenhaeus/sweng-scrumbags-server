package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.persistence.EntityNotFoundException;

import ch.epfl.scrumtool.AppEngineUtils;
import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

/**
 * 
 * @author aschneuw
 * 
 */

@Api(name = "scrumtool", version = "v1", namespace = @ApiNamespace(ownerDomain = "epfl.ch", ownerName = "epfl.ch", packagePath = "scrumtool.server"), clientIds = {
        Constants.ANDROID_CLIENT_ID_ARNO_MACBOOK,
        Constants.ANDROID_CLIENT_ID_JOEY_DESKTOP,
        Constants.ANDROID_CLIENT_ID_JOEY_LAPTOP,
        Constants.ANDROID_CLIENT_ID_LORIS_MACBOOK,
        Constants.ANDROID_CLIENT_ID_VINCENT_THINKPAD,
        Constants.ANDROID_CLIENT_ID_SYLVAIN_THINKPAD,
        Constants.ANDROID_CLIENT_ID_ALEX_MACBOOK,
        Constants.ANDROID_CLIENT_ID_VINCENT_LINUX,
        Constants.ANDROID_CLIENT_ID_CYRIAQUE_LAPTOP,
        Constants.ANDROID_CLIENT_ID_LEONARDO_THINKPAD }, audiences = { Constants.ANDROID_AUDIENCE })
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
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus;
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction tx = persistenceManager.currentTransaction();
        try {
            ScrumUser scrumUser = persistenceManager.getObjectById(ScrumUser.class,
                    scrumProject.getLastModUser());

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
            scrumPlayer.setUser(scrumUser);

            scrumProject.setSprints(new HashSet<ScrumSprint>());
            scrumProject.setBacklog(new HashSet<ScrumMainTask>());
            scrumUser.addPlayer(scrumPlayer);

            tx.begin();
            persistenceManager.makePersistent(scrumPlayer);
            tx.commit();

            scrumProject.addPlayerKey(scrumPlayer.getKey());

            tx.begin();
            persistenceManager.makePersistent(scrumProject);
            tx.commit();

            scrumPlayer.setProjectKey(scrumProject.getKey());

            tx.begin();
            persistenceManager.makePersistent(scrumPlayer);
            tx.commit();

            opStatus = new OperationStatus();
            opStatus.setKey(scrumProject.getKey());
            opStatus.setSuccess(true);

        } finally {
            if (tx.isActive()) {
                tx.rollback();
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
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            if (!containsScrumProject(update)) {
                throw new EntityNotFoundException(
                        "This project does not exist in the Database");
            }
            transaction.begin();
            persistenceManager.makePersistent(update);
            transaction.commit();

            ScrumProject project = persistenceManager.getObjectById(ScrumProject.class,
                    update.getKey());
            project.setDescription(update.getDescription());
            project.setLastModDate(update.getLastModDate());
            project.setLastModUser(update.getLastModUser());
            project.setName(update.getName());

            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
            opStatus.setKey(update.getKey());
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
                opStatus = new OperationStatus();
                opStatus.setSuccess(true);
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
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        OperationStatus opStatus = null;
        try {
            ScrumProject scrumproject = persistenceManager.getObjectById(ScrumProject.class,
                    projectKey);
            persistenceManager.deletePersistent(scrumproject);
            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
            opStatus.setKey(projectKey);
        } finally {
            persistenceManager.close();
        }
        return opStatus;
    }

    /**
     * @return
     * @throws OAuthRequestException
     */
    @ApiMethod(name = "loadProjects")
    public CollectionResponse<ScrumProject> loadProjects(
            @Named("id") String userKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        Set<ScrumProject> projects = new HashSet<ScrumProject>();
        try {
            ScrumUser sUser = persistenceManager.getObjectById(ScrumUser.class,
                    user.getEmail());
            for (ScrumPlayer s : sUser.getPlayers()) {

                projects.add(persistenceManager.getObjectById(ScrumProject.class,
                        s.getProjectKey()));
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumProject> builder().setItems(projects)
                .build();
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

    private boolean containsScrumProject(ScrumProject scrumProject) {
        PersistenceManager persistenceManager = getPersistenceManager();
        boolean contains = true;
        try {
            persistenceManager.getObjectById(ScrumProject.class, scrumProject.getKey());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            persistenceManager.close();
        }
        return contains;
    }

}
