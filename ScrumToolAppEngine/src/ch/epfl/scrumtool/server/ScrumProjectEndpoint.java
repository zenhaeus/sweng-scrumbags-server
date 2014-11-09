package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * @param scrumproject
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumProject", path = "operationstatus/insertProject")
    public OperationStatus insertScrumProject(ScrumProject scrumproject,
            User user) throws OAuthRequestException {
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
            scrumPlayer.setUser(scrumUser);

            Set<ScrumPlayer> scrumPlayers = new HashSet<ScrumPlayer>();
            scrumPlayers.add(scrumPlayer);
            scrumproject.setPlayers(scrumPlayers);

            Set<ScrumSprint> scrumSprints = new HashSet<ScrumSprint>();
            scrumproject.setSprints(scrumSprints);

            scrumproject.setBacklog(new HashSet<ScrumMainTask>());

            tx.begin();
            mgr.makePersistent(scrumproject);
            scrumPlayer.setProjectKey(scrumproject.getKey());
            mgr.makePersistent(scrumPlayer);
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
    @ApiMethod(name = "updateScrumProject", path = "operationstatus/updateProject")
    public OperationStatus updateScrumProject(ScrumProject update, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        try {
            if (!containsScrumProject(update)) {
                throw new EntityNotFoundException(
                        "This project does not exist in the Database");
            }
            tx.begin();
            mgr.makePersistent(update);
            tx.commit();

            ScrumProject project = mgr.getObjectById(ScrumProject.class,
                    update.getKey());
            project.setDescription(update.getDescription());
            project.setLastModDate(update.getLastModDate());
            project.setLastModUser(update.getLastModUser());
            project.setName(update.getName());

            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
            opStatus.setKey(update.getKey());
        } finally {
            if (tx.isActive()) {
                tx.rollback();
                opStatus = new OperationStatus();
                opStatus.setSuccess(true);
            }
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
    @ApiMethod(name = "removeScrumProject", path = "operationstatus/removeProject")
    public OperationStatus removeScrumProject(
            @Named("projectKey") String projectKey, User user)
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
            opStatus.setKey(projectKey);
        } finally {
            mgr.close();
        }
        return opStatus;
    }

    /**
     * @return
     * @throws OAuthRequestException
     */
    @SuppressWarnings("unchecked")
    @ApiMethod(name = "loadProjects")
    public CollectionResponse<ScrumProject> loadProjects(
            @Named("id") String userKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        Set<ScrumProject> projects = new HashSet<ScrumProject>();
        try {
            ScrumUser sUser = mgr.getObjectById(ScrumUser.class,
                    user.getEmail());
            for (ScrumPlayer s : sUser.getPlayers()) {
                projects.add(mgr.getObjectById(ScrumProject.class,
                        s.getProjectKey()));
            }
        } finally {
            mgr.close();
        }
        return CollectionResponse.<ScrumProject> builder().setItems(projects)
                .build();
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

    private boolean containsScrumProject(ScrumProject scrumProject) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumProject.class, scrumProject.getKey());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            mgr.close();
        }
        return contains;
    }

}
