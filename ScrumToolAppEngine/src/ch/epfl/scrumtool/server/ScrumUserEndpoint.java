package ch.epfl.scrumtool.server;

import java.util.Date;
import java.util.HashSet;
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
public class ScrumUserEndpoint {
    /**
     * Checks a user for the current eMail-Address already exists. In this case
     * it returns the corresponding Database object. Otherwise it creates a new
     * entry in the database and return the new record
     * 
     * @param eMail
     * @return
     */
    
    @ApiMethod(name = "loginUser")
    public ScrumUser loginUser(@Named("eMail") String eMail) {
        PersistenceManager persistenceManager = getPersistenceManager();
        ScrumUser scrumUser = null;
        try {
            scrumUser = persistenceManager.getObjectById(ScrumUser.class, eMail);
    
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            ScrumUser newUser = new ScrumUser();
            newUser.setEmail(eMail);
            Date date = new Date();
            newUser.setLastModDate(date.getTime());
            newUser.setLastModUser(eMail);
            newUser.setName(eMail);
            insertScrumUser(newUser);
    
            scrumUser = persistenceManager.getObjectById(ScrumUser.class, eMail);
        } finally {
            persistenceManager.close();
        }
        return scrumUser;
    }

    /**
     * @return
     * @throws OAuthRequestException
     */
    @ApiMethod(name = "loadProjects")
    public CollectionResponse<ScrumProject> loadProjects(
            @Named("userKey") String userKey, User user)
            throws OAuthRequestException {
        PersistenceManager persistenceManager = getPersistenceManager();
        Set<ScrumProject> projects = new HashSet<ScrumProject>();
    
        try {
            ScrumUser scrumUser = persistenceManager.getObjectById(ScrumUser.class, userKey);
            for (ScrumPlayer p : scrumUser.getPlayers()) {
                projects.add(p.getProject());
            }
    
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumProject>builder().setItems(projects).build();
    }

    /**
     * This method is used for updating an existing entity. If the entity does
     * not exist in the datastore, an exception is thrown. It uses HTTP PUT
     * method.
     * 
     * @param scrumUser
     *            the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumUser", path = "operationstatus/updateuser")
    public OperationStatus updateScrumUser(ScrumUser scrumUser, User user)
            throws OAuthRequestException {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            if (!containsScrumUser(scrumUser)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            
            //Create valid JDO object
            ScrumUser update = persistenceManager.getObjectById(ScrumUser.class, scrumUser.getEmail());
            update.setCompanyName(scrumUser.getCompanyName());
            update.setDateOfBirth(scrumUser.getDateOfBirth());
            update.setEmail(scrumUser.getEmail());
            update.setJobTitle(scrumUser.getJobTitle());
            update.setLastModDate(scrumUser.getLastModDate());
            update.setLastModUser(scrumUser.getLastModUser());
            update.setLastName(scrumUser.getLastName());
            update.setName(scrumUser.getName());
            update.setGender(scrumUser.getGender());
            
            transaction.begin();
            persistenceManager.makePersistent(update);
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
     * @param userKey
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumUser", path = "operationstatus/removeuser")
    public OperationStatus removeScrumUser(@Named("userKey") String userKey) {
        OperationStatus opStatus = new OperationStatus();
        opStatus.setSuccess(false);
        
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            transaction.begin();
            ScrumUser scrumUser = persistenceManager.getObjectById(ScrumUser.class, userKey);
            
            persistenceManager.deletePersistent(scrumUser);
            transaction.commit();
            
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return opStatus;
    }

    /**
     * Returns true if the DS contains the User
     * 
     * @param scrumUser
     * @return
     */
    private boolean containsScrumUser(ScrumUser scrumUser) {
        PersistenceManager persistenceManager = getPersistenceManager();
        boolean contains = true;
        try {
            persistenceManager.getObjectById(ScrumUser.class,
                    scrumUser.getEmail());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            persistenceManager.close();
        }
        return contains;
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrumUser
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    private ScrumUser insertScrumUser(ScrumUser scrumUser) {
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            if (scrumUser != null) {
            
                transaction.begin();
                persistenceManager.makePersistent(scrumUser);
                transaction.commit();
            }
            
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
        return scrumUser;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

}
