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
            Constants.ANDROID_CLIENT_ID_LEONARDO_THINKPAD},
        audiences = {Constants.ANDROID_AUDIENCE}
        )
public class ScrumPlayerEndpoint {


    @ApiMethod(name = "insertScrumPlayer", path="operationstatus/insertplayer")
    public OperationStatus insertScrumPlayer(ScrumPlayer scrumPlayer, 
            @Named("projectKey") String projectKey,
            @Named("userKey") String userKey,
            User user) throws OAuthRequestException {
        OperationStatus opStatus = null;
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            if (containsScrumPlayer(scrumPlayer)) {
                throw new EntityExistsException("Object already exists");
            }
            ScrumUser scrumUser = persistenceManager.getObjectById(ScrumUser.class, userKey);
            scrumPlayer.setUser(scrumUser);
            
            ScrumProject scrumProject = persistenceManager.getObjectById(ScrumProject.class, projectKey);
            scrumProject.getPlayers().add(scrumPlayer);
            
            transaction.begin();
            persistenceManager.makePersistent(scrumPlayer);
            transaction.commit();
            
            opStatus = new OperationStatus();
            opStatus.setKey(scrumPlayer.getKey());
            opStatus.setSuccess(true);
            
        } finally {
            persistenceManager.close();
        }
        return opStatus;
    }
    
    
    @ApiMethod(name = "getScrumPlayer")
    public ScrumPlayer getScrumIssue(@Named("id") String key, User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        ScrumPlayer scrumPlayer = null;
        try {
            scrumPlayer = persistenceManager.getObjectById(ScrumPlayer.class, key);
        } finally {
            persistenceManager.close();
        }
        return scrumPlayer;
    }
    

    @ApiMethod(name = "updateScrumPlayer")
    public OperationStatus updateScrumPlayer(ScrumPlayer scrumplayer, User user) throws OAuthRequestException {
        OperationStatus opStatus = null;
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        try {
            if (!containsScrumPlayer(scrumplayer)) {
                throw new EntityNotFoundException("Object does not exist");
            }
            persistenceManager.makePersistent(scrumplayer);
            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
        } finally {
            persistenceManager.close();
        }
        return opStatus;
    }

    /**
     * This method removes the entity with primary key id.
     * It uses HTTP DELETE method.
     *
     * @param id the primary key of the entity to be deleted.
     */
    @ApiMethod(name = "removeScrumPlayer")
    public OperationStatus removeScrumPlayer(@Named("playerKey") String playerKey, 
            User user) throws OAuthRequestException {
        AppEngineUtils.basicAuthentication(user);
        OperationStatus opStatus = null;
        PersistenceManager persistenceManager = getPersistenceManager();
        try {
            ScrumPlayer scrumPlayer = persistenceManager.getObjectById(ScrumPlayer.class, playerKey);
            persistenceManager.deletePersistent(scrumPlayer);
            opStatus = new OperationStatus();
            opStatus.setSuccess(true);
        } finally {
            persistenceManager.close();
        }
        return opStatus;
    }

    private boolean containsScrumPlayer(ScrumPlayer scrumPlayer) {
        PersistenceManager persistenceManager = getPersistenceManager();
        boolean contains = true;
        try {
            persistenceManager.getObjectById(ScrumPlayer.class, scrumPlayer.getKey());
        } catch (javax.jdo.JDOObjectNotFoundException ex) {
            contains = false;
        } finally {
            persistenceManager.close();
        }
        return contains;
    }
    
    @ApiMethod(name = "loadPlayers")
    public CollectionResponse<ScrumPlayer> loadPlayers(@Named("projectKey") String projectKey,
            User user) throws OAuthRequestException {
        PersistenceManager persistenceManager = null;
        List<ScrumPlayer> players = null;

        try {
            persistenceManager = getPersistenceManager();
            ScrumProject scrumProject = persistenceManager.getObjectById(ScrumProject.class, projectKey);
            players = new ArrayList<ScrumPlayer>();
            for (ScrumPlayer p: scrumProject.getPlayers()) {
                players.add(p);
            }
            
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumPlayer>builder().setItems(players)
                .build();
    }

    private static PersistenceManager getPersistenceManager() {
        return PMF.get().getPersistenceManager();
    }

}
