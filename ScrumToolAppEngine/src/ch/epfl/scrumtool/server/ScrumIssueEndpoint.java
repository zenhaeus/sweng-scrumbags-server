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
        Constants.ANDROID_CLIENT_ID_ARNO_HP}, audiences = { Constants.ANDROID_AUDIENCE })
public class ScrumIssueEndpoint {

    /**
     * This method gets the entity having primary key id. It uses HTTP GET
     * method.
     * 
     * @param key
     *            the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumIssue")
    public ScrumIssue getScrumIssue(@Named("id") String key, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        ScrumIssue scrumIssue = null;
        try {
            scrumIssue = persistenceManager
                    .getObjectById(ScrumIssue.class, key);
        } finally {
            persistenceManager.close();
        }
        return scrumIssue;
    }

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
            @Named("mainTaskKey") String maintaskKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        ScrumMainTask mainTask = persistenceManager.getObjectById(
                ScrumMainTask.class, maintaskKey);
        mainTask.getIssues().add(scrumIssue);
        try {
            transaction.begin();
            persistenceManager.makePersistent(mainTask);
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

    @ApiMethod(name = "loadIssues")
    public CollectionResponse<ScrumIssue> loadIssues(
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
    public OperationStatus updateScrumMainTask(ScrumIssue scrumIssue, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            if (!containsScrumIssue(scrumIssue)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            transaction.begin();
            persistenceManager.makePersistent(scrumIssue);
            transaction.commit();

            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
            opStatus.setKey(scrumIssue.getKey());
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

    @ApiMethod(name = "insertIssueInSprint", path = "operationstatus/insertIssueInSprint")
    public OperationStatus insertScrumIssueInSprint(
            @Named("issueId") String issueKey,
            @Named("sprintId") String sprintKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        ScrumSprint scrumSprint = persistenceManager.getObjectById(
                ScrumSprint.class, sprintKey);
        ScrumIssue scrumIssue = persistenceManager.getObjectById(
                ScrumIssue.class, issueKey);
        scrumSprint.getIssues().add(scrumIssue);
        try {
            transaction.begin();
            persistenceManager.makePersistent(scrumSprint);
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

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param id
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumIssueFromSprint", path = "operationstatus/removeIssueFromSprint")
    public OperationStatus removeScrumIssueFromSprint(
            @Named("issueId") String issueKey,
            @Named("sprintId") String sprintKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        ScrumSprint scrumSprint = persistenceManager.getObjectById(
                ScrumSprint.class, sprintKey);
        ScrumIssue scrumIssue = persistenceManager.getObjectById(
                ScrumIssue.class, issueKey);
        scrumSprint.getIssues().remove(scrumIssue);
        try {
            transaction.begin();
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
    public OperationStatus removeScrumIssue(@Named("id") String key, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager persistenceManager = getPersistenceManager();
        try {
            ScrumIssue scrumIssue = persistenceManager.getObjectById(
                    ScrumIssue.class, key);
            persistenceManager.deletePersistent(scrumIssue);
            opStatus = new OperationStatus();
            opStatus.setKey(key);
            opStatus.setSuccess(true);
        } finally {

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
