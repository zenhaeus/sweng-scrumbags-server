package ch.epfl.scrumtool.server;

import java.util.ArrayList;
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
public class ScrumSprintEndpoint {
    /**
     * This method gets the entity having primary key id. It uses HTTP GET
     * method.
     * 
     * @param sprintKey
     *            the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumSprint")
    public ScrumSprint getScrumSprint(@Named("sprintKey") String sprintKey, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        ScrumSprint scrumSprint = null;
        try {
            scrumSprint = persistenceManager.getObjectById(ScrumSprint.class, sprintKey);
        } finally {
            persistenceManager.close();
        }
        return scrumSprint;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrumSprint
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    @ApiMethod(name = "insertScrumSprint")
    public ScrumSprint insertScrumSprint(ScrumSprint scrumSprint, User user)
            throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            if (containsScrumSprint(scrumSprint)) {
                throw new EntityExistsException("Object already exists");
            }
            
            transaction.begin();
            persistenceManager.makePersistent(scrumSprint);
            transaction.commit();
            
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return scrumSprint;
    }

    /**
     * This method is used for updating an existing entity. If the entity does
     * not exist in the datastore, an exception is thrown. It uses HTTP PUT
     * method.
     * 
     * @param scrumSprint
     *            the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumSprint", path = "operationstatus/updatesprint")
    public OperationStatus updateScrumSprint(ScrumSprint scrumSprint, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        
        AppEngineUtils.basicAuthentication(user);
        
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            if (!containsScrumSprint(scrumSprint)) {
                throw new EntityNotFoundException("Object does not exist");
            }
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

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param sprintKey
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumSprint", path = "operationstatus/removesprint")
    public OperationStatus removeScrumSprint(@Named("sprintKey") String sprintKey, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        
        AppEngineUtils.basicAuthentication(user);
        
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            ScrumSprint scrumSprint = persistenceManager.getObjectById(ScrumSprint.class, sprintKey);
            
            transaction.begin();
            persistenceManager.deletePersistent(scrumSprint);
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
     * Returns true if the DS containts the Sprint
     * 
     * @param scrumSprint
     * @return
     */
    private boolean containsScrumSprint(ScrumSprint scrumSprint) {
        PersistenceManager persistenceManager = getPersistenceManager();
        boolean contains = true;
        try {
            persistenceManager.getObjectById(ScrumSprint.class,
                    scrumSprint.getKey());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            persistenceManager.close();
        }
        return contains;
    }

    @ApiMethod(name = "loadSprints")
    public CollectionResponse<ScrumSprint> loadSprints(
            @Named("projectKey") String projectKey, User user)
            throws OAuthRequestException {
        PersistenceManager persistenceManager = null;
        List<ScrumSprint> sprints = null;

        try {
            persistenceManager = getPersistenceManager();
            ScrumProject project = persistenceManager.getObjectById(ScrumProject.class, projectKey);
            sprints = new ArrayList<ScrumSprint>();
            for (ScrumSprint s : project.getSprint()) {
                sprints.add(s);
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumSprint>builder().setItems(sprints).build();
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

}
