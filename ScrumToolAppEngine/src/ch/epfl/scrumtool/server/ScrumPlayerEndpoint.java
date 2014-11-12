package ch.epfl.scrumtool.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.persistence.EntityExistsException;
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
public class ScrumPlayerEndpoint {

    @ApiMethod(name = "updateScrumPlayer")
    public OperationStatus updateScrumPlayer(ScrumPlayer update, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            if (!containsScrumPlayer(update)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            transaction.begin();
            ScrumPlayer scrumPlayer = persistenceManager.getObjectById(
                    ScrumPlayer.class, update.getKey());
            scrumPlayer.setAdminFlag(update.getAdminFlag());
            scrumPlayer.setKey(update.getKey());
            scrumPlayer.setLastModDate(update.getLastModDate());
            scrumPlayer.setLastModUser(update.getLastModUser());
            scrumPlayer.setRole(update.getRole());
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
    @ApiMethod(name = "removeScrumPlayer", path = "operationstatus/removeplayer")
    public OperationStatus removeScrumPlayer(
            @Named("playerKey") String playerKey, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            transaction.begin();
            ScrumPlayer scrumPlayer = persistenceManager.getObjectById(
                    ScrumPlayer.class, playerKey);

            persistenceManager.deletePersistent(scrumPlayer);
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

    @ApiMethod(name = "loadPlayers")
    public CollectionResponse<ScrumPlayer> loadPlayers(
            @Named("projectKey") String projectKey, User user)
            throws OAuthRequestException {
        PersistenceManager persistenceManager = null;
        List<ScrumPlayer> players = null;

        try {
            persistenceManager = getPersistenceManager();
            ScrumProject scrumProject = persistenceManager.getObjectById(
                    ScrumProject.class, projectKey);
            players = new ArrayList<ScrumPlayer>();
            for (ScrumPlayer p : scrumProject.getPlayers()) {
                p.getUser(); // lazy fetch
                players.add(p);
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumPlayer>builder().setItems(players).build();
    }

    @ApiMethod(name = "addPlayerToProject")
    public OperationStatus addPlayerToProject(ScrumProject project,
            @Named("userKey") String userEmail, @Named("role") String role,
            User user) throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            for (ScrumPlayer player : project.getPlayers()) {
                if (player.getUser().getEmail().equals(userEmail)) {
                    throw new EntityExistsException("Object already exists");
                }
            }
            transaction.begin();
            ScrumPlayer scrumPlayer = new ScrumPlayer();
            ScrumUser scrumUser = null;
            try {
                scrumUser = persistenceManager.getObjectById(ScrumUser.class,
                        userEmail);
            } catch (javax.jdo.JDOObjectNotFoundException ex) {
                scrumUser = new ScrumUser();
                scrumUser.setEmail(userEmail);
                scrumUser.setLastModDate((new Date()).getTime());
                scrumUser.setName(userEmail);
                scrumUser.setProjects(new HashSet<ScrumPlayer>());
                scrumUser.setLastModUser(userEmail);
                persistenceManager.makePersistent(scrumUser);
            }

            scrumPlayer.setUser(scrumUser);
            scrumPlayer.setAdminFlag(false);
            scrumPlayer.setIssues(new HashSet<ScrumIssue>());
            scrumPlayer.setRole(Role.valueOf(role)); // FIXME
            scrumPlayer.setLastModDate((new Date()).getTime());
            scrumPlayer.setLastModUser(user.getEmail());

            ScrumProject scrumProject = persistenceManager.getObjectById(
                    ScrumProject.class, project.getKey());
            scrumProject.getPlayers().add(scrumPlayer);
            scrumPlayer.setProject(scrumProject);

            persistenceManager.makePersistent(scrumProject);
            persistenceManager.makePersistent(scrumPlayer);
            transaction.commit();

            opStatus.setKey(scrumPlayer.getKey());
            opStatus.setSuccess(true);

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return opStatus;
    }

    private boolean containsScrumPlayer(ScrumPlayer scrumPlayer) {
        PersistenceManager persistenceManager = getPersistenceManager();
        boolean contains = true;
        try {
            persistenceManager.getObjectById(ScrumPlayer.class,
                    scrumPlayer.getKey());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            persistenceManager.close();
        }
        return contains;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }
}
