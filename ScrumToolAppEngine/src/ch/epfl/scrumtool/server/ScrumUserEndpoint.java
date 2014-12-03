package ch.epfl.scrumtool.server;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Named;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import ch.epfl.scrumtool.AppEngineUtils;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.InternalServerErrorException;
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
public class ScrumUserEndpoint {
    /**
     * Checks a user for the current eMail-Address already exists. In this case
     * it returns the corresponding Database object. Otherwise it creates a new
     * entry in the database and return the new record
     * 
     * @param eMail
     * @return
     * @throws ServiceException 
     */
    
    @ApiMethod(name = "loginUser")
    public ScrumUser loginUser(@Named("eMail") String eMail) throws ServiceException {
        if (eMail == null) {
            throw new NullPointerException();
        }
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        ScrumUser scrumUser = null;
        try {
            scrumUser = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, eMail, persistenceManager);
    
        } catch (ServiceException ex) {
            ScrumUser newUser = new ScrumUser();
            newUser.setEmail(eMail);
            newUser.setLastModDate(Calendar.getInstance().getTimeInMillis());
            newUser.setLastModUser(eMail);
            newUser.setName(eMail);
            insertScrumUser(newUser);

            scrumUser = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, eMail, persistenceManager);
        } finally {
            persistenceManager.close();
        }
        return scrumUser;
    }

    /**
     * @return
     * @throws ServiceException
     */
    @ApiMethod(name = "loadProjects")
    public CollectionResponse<ScrumProject> loadProjects(@Named("userKey") String userKey, User user)
        throws ServiceException {
        AppEngineUtils.basicAuthentication(user);
        if (userKey == null) {
            return null;
        }
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Set<ScrumProject> projects = new HashSet<ScrumProject>();
    
        try {
            ScrumUser scrumUser = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, userKey, persistenceManager);
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
    public void updateScrumUser(ScrumUser scrumUser, User user) throws ServiceException {
        AppEngineUtils.basicAuthentication(user);
        if (scrumUser == null) {
            throw new InternalServerErrorException("Null");
        }
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            //Create valid JDO object
            ScrumUser update = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, scrumUser.getEmail(),
                    persistenceManager);
            update.setCompanyName(scrumUser.getCompanyName());
            update.setDateOfBirth(scrumUser.getDateOfBirth());
            update.setEmail(scrumUser.getEmail());
            update.setJobTitle(scrumUser.getJobTitle());
            update.setLastModDate(Calendar.getInstance().getTimeInMillis());
            update.setLastModUser(user.getEmail());
            update.setLastName(scrumUser.getLastName());
            update.setName(scrumUser.getName());
            update.setGender(scrumUser.getGender());
            
            transaction.begin();
            persistenceManager.makePersistent(update);
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
     * @param userKey
     *            the primary key of the entity to be deleted.
     * @throws ServiceException 
     */
    @ApiMethod(name = "removeScrumUser", path = "operationstatus/removeuser")
    public void removeScrumUser(@Named("userKey") String userKey, User user) throws ServiceException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        
        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            transaction.begin();
            ScrumUser scrumUser = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, userKey, persistenceManager);
            for (ScrumPlayer p : scrumUser.getPlayers()) {
                ScrumProject project = p.getProject();
                project.setLastModDate(lastDate);
                project.setLastModUser(lastUser);
                persistenceManager.makePersistent(project);
                for (ScrumIssue i : p.getIssues()) {
                    i.setAssignedPlayer(null);
                    i.setLastModDate(lastDate);
                    i.setLastModUser(lastUser);
                    persistenceManager.makePersistent(i);
                }
            }
            persistenceManager.deletePersistent(scrumUser);
            transaction.commit();
            
        } catch (JDOObjectNotFoundException e) {
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
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
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
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

}
