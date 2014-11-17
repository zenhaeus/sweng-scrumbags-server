package ch.epfl.scrumtool.server;

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
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

/**
 * 
 * @author aschneuw, sylb
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
        Constants.ANDROID_CLIENT_ID_LEONARDO_THINKPAD,
        Constants.ANDROID_CLIENT_ID_ARNO_HP }, audiences = { Constants.ANDROID_AUDIENCE })
public class ScrumIssueEndpoint {

    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrumIssue
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumIssue", path = "operationstatus/issueinsert")
    public OperationStatus insertScrumIssue(ScrumIssue scrumIssue,
            @Named("mainTaskKey") String maintaskKey,
            @Named("playerKey") String playerKey, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumMainTask scrumMainTask = persistenceManager.getObjectById(
                    ScrumMainTask.class, maintaskKey);
            transaction.begin();
            scrumMainTask.getIssues().add(scrumIssue);
            persistenceManager.makePersistent(scrumMainTask);
            if (playerKey != null) {
                ScrumPlayer scrumPlayer = persistenceManager.getObjectById(
                        ScrumPlayer.class, playerKey);
                scrumIssue.setAssignedPlayer(scrumPlayer);
                scrumPlayer.addIssue(scrumIssue);
                persistenceManager.makePersistent(scrumPlayer);
            }
            
            transaction.commit();

            opStatus.setKey(scrumIssue.getKey());
            opStatus.setSuccess(true);
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return opStatus;
    }

    @ApiMethod(name = "loadIssuesByMainTask")
    public CollectionResponse<ScrumIssue> loadIssuesByMainTask(
            @Named("mainTaskKey") String maintaskKey, User user)
            throws OAuthRequestException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();

        Set<ScrumIssue> issues = null;
        try {
            ScrumMainTask scrumMaintask = null;
            scrumMaintask = persistenceManager.getObjectById(
                    ScrumMainTask.class, maintaskKey);
            issues = scrumMaintask.getIssues();
            
            //Lazy Fetch
            for (ScrumIssue i : issues) {
                i.getAssignedPlayer();
                i.getSprint();
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumIssue> builder().setItems(issues)
                .build();
    }

    @ApiMethod(name = "loadIssuesBySprint")
    public CollectionResponse<ScrumIssue> loadIssuesBySprint(
            @Named("sprintKey") String sprintKey, User user)
            throws OAuthRequestException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();

        Set<ScrumIssue> issues = null;
        try {
            ScrumSprint scrumSprint = null;
            scrumSprint = persistenceManager.getObjectById(ScrumSprint.class,
                    sprintKey);
            issues = scrumSprint.getIssues();
            
          //Lazy Fetch
            for (ScrumIssue i : issues) {
                i.getAssignedPlayer();
                i.getSprint();
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumIssue> builder().setItems(issues)
                .build();
    }

    /**
     * This method is used for updating an existing entity. If the entity does
     * not exist in the datastore, an exception is thrown. It uses HTTP PUT
     * method.
     * 
     * @param scrumissue
     *            the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumIssue", path = "operationstatus/issueupdate")
    public OperationStatus updateScrumIssue(ScrumIssue update, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            if (!containsScrumIssue(update)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            transaction.begin();
            ScrumIssue scrumIssue = persistenceManager.getObjectById(
                    ScrumIssue.class, update.getKey());
            scrumIssue.setName(update.getName());
            scrumIssue.setDescription(update.getDescription());
            scrumIssue.setEstimation(update.getEstimation());
            scrumIssue.setLastModDate(update.getLastModDate());
            scrumIssue.setLastModUser(update.getLastModUser());
            scrumIssue.setStatus(update.getStatus());
            scrumIssue.setPriority(update.getPriority());
            if (!scrumIssue.getAssignedPlayer().getKey()
                    .equals(update.getAssignedPlayer().getKey())) {
                scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
                ScrumPlayer scrumPlayer = persistenceManager
                        .getObjectById(ScrumPlayer.class, update.getAssignedPlayer().getKey());
                scrumPlayer.addIssue(scrumIssue);
                scrumIssue.setAssignedPlayer(scrumPlayer);
                persistenceManager.makePersistent(scrumPlayer);
            }
            persistenceManager.makePersistent(scrumIssue);
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

    @ApiMethod(name = "insertIssueInSprint", path = "operationstatus/insertIssueInSprint")
    public OperationStatus insertScrumIssueInSprint(
            @Named("issueKey") String issueKey,
            @Named("sprintKey") String sprintKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            transaction.begin();
            ScrumSprint scrumSprint = persistenceManager.getObjectById(
                    ScrumSprint.class, sprintKey);
            ScrumIssue scrumIssue = persistenceManager.getObjectById(
                    ScrumIssue.class, issueKey);
            scrumIssue.setSprint(scrumSprint);
            scrumSprint.getIssues().add(scrumIssue);

            persistenceManager.makePersistent(scrumIssue);
            persistenceManager.makePersistent(scrumSprint);
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
    @ApiMethod(name = "removeScrumIssueFromSprint", path = "operationstatus/removeIssueFromSprint")
    public OperationStatus removeScrumIssueFromSprint(
            @Named("issueKey") String issueKey,
            @Named("sprintKey") String sprintKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumSprint scrumSprint = persistenceManager.getObjectById(
                    ScrumSprint.class, sprintKey);
            ScrumIssue scrumIssue = persistenceManager.getObjectById(
                    ScrumIssue.class, issueKey);
            scrumIssue.setSprint(null);
            scrumSprint.getIssues().remove(scrumIssue);

            transaction.begin();
            persistenceManager.makePersistent(scrumIssue);
            persistenceManager.makePersistent(scrumSprint);
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

    @ApiMethod(name = "removeScrumIssue", path = "operationstatus/removeIssue")
    public OperationStatus removeScrumIssue(@Named("issueKey") String issueKey,
            User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            transaction.begin();
            ScrumIssue scrumIssue = persistenceManager.getObjectById(
                    ScrumIssue.class, issueKey);
            if (scrumIssue.getSprint() != null){
                scrumIssue.getSprint().removeIssue(scrumIssue);
            }
            if (scrumIssue.getAssignedPlayer() != null){
                scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
            }
            persistenceManager.deletePersistent(scrumIssue);
            transaction.commit();
            opStatus.setKey(issueKey);
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
     * Return true if the DS contains the Issue
     * 
     * @param scrumissue
     * @return
     */
    private boolean containsScrumIssue(ScrumIssue scrumissue) {
        PersistenceManager persistenceManager = getPersistenceManager();
        boolean contains = true;
        try {
            persistenceManager.getObjectById(ScrumIssue.class,
                    scrumissue.getKey());
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
