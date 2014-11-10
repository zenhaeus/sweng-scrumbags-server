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
public class ScrumIssueEndpoint {

    /**
     * This method gets the entity having primary key id. It uses HTTP GET method.
     *
     * @param id the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumIssue")
    public ScrumIssue getScrumIssue(@Named("id") String id, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        ScrumIssue scrumissue = null;
        try {
            scrumissue = mgr.getObjectById(ScrumIssue.class, id);
        } finally {
            mgr.close();
        }
        return scrumissue;
    }
    
    

    /**
     * This inserts a new entity into App Engine datastore. If the entity already
     * exists in the datastore, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param scrumissue the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumIssue", path="operationstatus/issueinsert")
    public OperationStatus insertScrumIssue(ScrumIssue scrumissue, 
            @Named("mainTaskKey") String mainTaskKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        ScrumMainTask mainTask = mgr.getObjectById(ScrumMainTask.class, mainTaskKey);
        mainTask.getIssues().add(scrumissue);
        try {
            tx.begin();
            mgr.makePersistent(mainTask);
            tx.commit();
            opStatus.setKey(scrumissue.getKey());
            opStatus.setSuccess(true);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            mgr.close();
        }
        
        return opStatus;
    }
    
    @ApiMethod(name = "loadIssues")
    public CollectionResponse<ScrumIssue> loadIssues(
            @Named("mainTaskKey") String mainTaskKey,
            User user) throws OAuthRequestException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager mgr = getPersistenceManager();
        
        
        Set<ScrumIssue> issues = null;
        try {
            ScrumMainTask scrumMainTask = null;
            scrumMainTask = mgr.getObjectById(ScrumMainTask.class, mainTaskKey);
            issues = scrumMainTask.getIssues();

            
            
        } finally {
            mgr.close();
        }
        return CollectionResponse.<ScrumIssue>builder().setItems(issues)
                .build();
    }

    /**
     * This method is used for updating an existing entity. If the entity does not
     * exist in the datastore, an exception is thrown.
     * It uses HTTP PUT method.
     *
     * @param scrumissue the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumIssue", path="operationstatus/issueupdate")
    public OperationStatus updateScrumMainTask(ScrumIssue scrumIssue, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        try {
            if (!containsScrumIssue(scrumIssue)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            tx.begin();
            mgr.makePersistent(scrumIssue);
            tx.commit();
            
            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
            opStatus.setKey(scrumIssue.getKey());
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
    
    
    @ApiMethod(name = "insertIssueInSprint", path="operationstatus/insertIssueInSprint")
    public OperationStatus insertScrumIssueInSprint(@Named("issueId") String issueId, 
            @Named("sprintId") String sprintId, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        ScrumSprint sprint = mgr.getObjectById(ScrumSprint.class, sprintId);
        ScrumIssue issue = mgr.getObjectById(ScrumIssue.class, issueId);
        sprint.getIssues().add(issue);
        try {
            tx.begin();
            mgr.makePersistent(sprint);
            tx.commit();
            opStatus.setKey(issue.getKey());
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
     * This method removes the entity with primary key id.
     * It uses HTTP DELETE method.
     *
     * @param id the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumIssueFromSprint", path="operationstatus/removeIssueFromSprint")
    public OperationStatus removeScrumIssueFromSprint(@Named("issueId") String issueId,
            @Named("sprintId") String sprintId, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        PersistenceManager mgr = getPersistenceManager();
        Transaction tx = mgr.currentTransaction();
        ScrumSprint sprint = mgr.getObjectById(ScrumSprint.class, sprintId);
        ScrumIssue issue = mgr.getObjectById(ScrumIssue.class, issueId);
        sprint.getIssues().remove(issue);
        try {
            tx.begin();
            mgr.makePersistent(sprint);
            tx.commit();
            opStatus.setSuccess(true);
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            mgr.close();
        }
        
        return opStatus;
    }
    
    @ApiMethod(name = "removeScrumIssue", path="operationstatus/removeIssue")
    public OperationStatus removeScrumIssue(@Named("id") String id, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumIssue scrumIssue = mgr.getObjectById(
                    ScrumIssue.class, id);
            mgr.deletePersistent(scrumIssue);
            opStatus = new OperationStatus();
            opStatus.setKey(id);
            opStatus.setSuccess(true);
        } finally {
            
            mgr.close();
        }
        return opStatus;
    }

    private boolean containsScrumIssue(ScrumIssue scrumissue) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumIssue.class, scrumissue.getKey());
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
