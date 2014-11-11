package ch.epfl.scrumtool.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

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
            Constants.ANDROID_CLIENT_ID_LEONARDO_THINKPAD},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumUserEndpoint {

    /**
     * This method gets the entity having primary key id. It uses HTTP GET
     * method.
     * 
     * @param id
     *            the primary key of the java bean.
     * @return The entity with primary key id.
     */
    @ApiMethod(name = "getScrumUser")
    public ScrumUser getScrumUser(@Named("id") String id, User user) throws OAuthRequestException {
        PersistenceManager mgr = getPersistenceManager();
        ScrumUser scrumuser = null;
        try {
            scrumuser = mgr.getObjectById(ScrumUser.class, id);
        } finally {
            mgr.close();
        }
        return scrumuser;
    }

    /**
     * @return
     * @throws OAuthRequestException
     */
    @ApiMethod(name = "loadProjects")
    public CollectionResponse<ScrumProject> loadProjects(
            @Named("id") String id, User user) throws OAuthRequestException {
        PersistenceManager mgr = null;
        List<ScrumProject> projects = null;        
        

        try {
            mgr = getPersistenceManager();
            
            ScrumUser sU = mgr.getObjectById(ScrumUser.class, id);
            projects = new ArrayList<ScrumProject>();
            for (ScrumPlayer p: sU.getPlayers()) {
                ScrumProject pr = p.getProject();
                //Load all properties
                pr.getDescription();
                pr.getName();
                pr.getLastModDate();
                pr.getLastModUser();
                
                projects.add(pr);
            }
            
        } finally {
            mgr.close();
        }
        return CollectionResponse.<ScrumProject>builder().setItems(projects)
                .build();
    }

    /**
     * This inserts a new entity into App Engine datastore. If the entity
     * already exists in the datastore, an exception is thrown. It uses HTTP
     * POST method.
     * 
     * @param scrumuser
     *            the entity to be inserted.
     * @return The inserted entity.
     */
    private ScrumUser insertScrumUser(ScrumUser scrumuser) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (scrumuser != null) {
                if (containsScrumUser(scrumuser)) {
                    throw new EntityExistsException("Object already exists");
                }
            }
            mgr.makePersistent(scrumuser);
        } finally {
            mgr.close();
        }
        return scrumuser;
    }

    /**
     * This method is used for updating an existing entity. If the entity does
     * not exist in the datastore, an exception is thrown. It uses HTTP PUT
     * method.
     * 
     * @param scrumuser
     *            the entity to be updated.
     * @return The updated entity.
     */
    @ApiMethod(name = "updateScrumUser")
    public ScrumUser updateScrumUser(ScrumUser scrumuser, User user) throws OAuthRequestException {
        PersistenceManager mgr = getPersistenceManager();
        try {
            if (!containsScrumUser(scrumuser)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            mgr.makePersistent(scrumuser);
        } finally {
            mgr.close();
        }
        return scrumuser;
    }

    /**
     * This method removes the entity with primary key id. It uses HTTP DELETE
     * method.
     * 
     * @param id
     *            the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumUser")
    public void removeScrumUser(@Named("id") String id) {
        PersistenceManager mgr = getPersistenceManager();
        try {
            ScrumUser scrumuser = mgr.getObjectById(ScrumUser.class, id);
            mgr.deletePersistent(scrumuser);
        } finally {
            mgr.close();
        }
    }

    private boolean containsScrumUser(ScrumUser scrumuser) {
        PersistenceManager mgr = getPersistenceManager();
        boolean contains = true;
        try {
            mgr.getObjectById(ScrumUser.class, scrumuser.getEmail());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            mgr.close();
        }
        return contains;
    }

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
        PersistenceManager mgr = getPersistenceManager();
        ScrumUser user = null;
        try {
            user = mgr.getObjectById(ScrumUser.class, eMail);

        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            ScrumUser newUser = new ScrumUser();
            newUser.setEmail(eMail);
            Date date = new Date();
            newUser.setLastModDate(date.getTime());
            newUser.setLastModUser(eMail);
            newUser.setName(eMail);
            insertScrumUser(newUser);

            user = mgr.getObjectById(ScrumUser.class, eMail);
        } finally {
            mgr.close();
        }
        return user;
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

}
