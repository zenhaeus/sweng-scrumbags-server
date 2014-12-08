package ch.epfl.scrumtool.server;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityExistsException;

import ch.epfl.scrumtool.AppEngineUtils;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
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
        clientIds = {
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
            Constants.ANDROID_CLIENT_ID_ARNO_HP,
            Constants.ANDROID_CLIENT_ID_ARNO_THINKPAD
            },
        audiences = { 
            Constants.ANDROID_AUDIENCE }
        )
public class ScrumPlayerEndpoint {

    @ApiMethod(name = "addPlayerToProject")
    public KeyResponse addPlayerToProject(@Named("projectKey") String projectKey,
            @Named("userKey") String userEmail, @Named("role") String role,
            User user) throws ServiceException {
        if (projectKey == null || userEmail == null || role == null) {
            throw new NullPointerException();
        }
        
        AppEngineUtils.basicAuthentication(user);
    
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        ScrumPlayer scrumPlayer = null;
        ScrumUser scrumUser = null;
        ScrumProject scrumProject = null;
    
        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            scrumProject = AppEngineUtils.getObjectFromDatastore(ScrumProject.class, projectKey, persistenceManager);
            for (ScrumPlayer player : scrumProject.getPlayers()) {
                if (player.getUser().getEmail().equals(userEmail)) {
                    throw new EntityExistsException("Object already exists");
                }
            }
            scrumPlayer = new ScrumPlayer();
    
            try {
                scrumUser = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, userEmail, persistenceManager);
            } catch (ServiceException ex) {
                scrumUser = new ScrumUser();
                scrumUser.setEmail(userEmail);
                scrumUser.setLastModDate(lastDate);
                scrumUser.setName(userEmail.substring(0, userEmail.indexOf('@')));
                scrumUser.setLastModUser(lastUser);
            }
    
            scrumPlayer.setAdminFlag(false);
            scrumPlayer.setIssues(new HashSet<ScrumIssue>());
            scrumPlayer.setRole(Role.valueOf(role));
            scrumPlayer.setInvitedFlag(true);
            scrumPlayer.setLastModDate(lastDate);
            scrumPlayer.setLastModUser(lastUser);
    
            scrumUser.addPlayer(scrumPlayer);
    
            transaction.begin();
            persistenceManager.makePersistent(scrumUser);
            scrumProject.addPlayer(scrumPlayer);
            scrumProject.setLastModDate(lastDate);
            scrumPlayer.setLastModUser(lastUser);
            persistenceManager.makePersistent(scrumProject);
            scrumPlayer.setProject(scrumProject);
            persistenceManager.makePersistent(scrumPlayer);
            transaction.commit();
            return new KeyResponse(scrumPlayer.getKey());
    
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
    
            if (scrumPlayer != null) {
                scrumPlayer.setProject(null);
            }
    
            if (scrumUser != null) {
                scrumUser.setPlayers(null);
                sendNotificationEMail(scrumUser.getEmail(), scrumProject.getName());
            }
        }
    }

    @ApiMethod(name = "loadPlayers")
    public CollectionResponse<ScrumPlayer> loadPlayers(@Named("projectKey") String projectKey, User user)
        throws ServiceException {
        if (projectKey == null) {
            throw new NullPointerException();
        }
        
        AppEngineUtils.basicAuthentication(user);
    
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        List<ScrumPlayer> players = null;
    
        try {
            ScrumProject scrumProject = AppEngineUtils.getObjectFromDatastore(ScrumProject.class, projectKey, 
                    persistenceManager);
            players = new ArrayList<ScrumPlayer>();
            for (ScrumPlayer p : scrumProject.getPlayers()) {
                // lazy fetch
                p.getKey();
                p.getUser();
                p.getUser().getDateOfBirth();
                p.getUser().getName();
                p.getUser().getLastName();
                p.getUser().getCompanyName();
                p.getUser().getJobTitle();
                p.getUser().getGender();
                p.getUser().getEmail();
                p.getRole();
                p.getAdminFlag();
                p.getInvitedFlag();
                p.getProject();
                players.add(p);
            }
        } finally {
            persistenceManager.close();
        }
        for (ScrumPlayer p : players) {
            p.getUser().setPlayers(null);
            p.getProject().setBacklog(null);
            p.getProject().setPlayers(null);
            p.getProject().setSprints(null);
        }
        return CollectionResponse.<ScrumPlayer>builder().setItems(players)
                .build();
    }

    @ApiMethod(name = "loadInvitedPlayers")
    public CollectionResponse<ScrumPlayer> loadInvitedPlayers(User user)
        throws ServiceException {
        
        AppEngineUtils.basicAuthentication(user);
    
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        List<ScrumPlayer> players = null;
    
        try {
            ScrumUser scrumUser = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, user.getEmail(), 
                    persistenceManager);
            players = new ArrayList<ScrumPlayer>();
            for (ScrumPlayer p : scrumUser.getPlayers()) {
                if (p.getInvitedFlag()) {
                 // lazy fetch
                    p.getUser();
                    p.getUser().getDateOfBirth();
                    p.getUser().getName();
                    p.getUser().getLastName();
                    p.getUser().getCompanyName();
                    p.getUser().getJobTitle();
                    p.getUser().getGender();
                    p.getKey();
                    p.getRole();
                    p.getAdminFlag();
                    p.getInvitedFlag();
                    p.getProject();
                    players.add(p);
                }
            }
        } finally {
            persistenceManager.close();
        }
        for (ScrumPlayer p : players) {
            p.getUser().setPlayers(null);
            p.getProject().setBacklog(null);
            p.getProject().setPlayers(null);
            p.getProject().setSprints(null);
        }
        return CollectionResponse.<ScrumPlayer>builder().setItems(players)
                .build();
    }
    
    @ApiMethod(name = "updateScrumPlayer")
    public void updateScrumPlayer(ScrumPlayer update, User user)
        throws ServiceException {

        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumPlayer scrumPlayer = AppEngineUtils.getObjectFromDatastore(
                    ScrumPlayer.class, update.getKey(), persistenceManager);
            transaction.begin();
            scrumPlayer.setAdminFlag(update.getAdminFlag());
            scrumPlayer.setKey(update.getKey());
            scrumPlayer.setLastModDate(Calendar.getInstance().getTimeInMillis());
            scrumPlayer.setLastModUser(user.getEmail());
            scrumPlayer.setRole(update.getRole());
            scrumPlayer.setInvitedFlag(update.getInvitedFlag());
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
     * @param id
     *            the primary key of the entity to be deleted.
     * @throws ServiceException 
     */
    @ApiMethod(name = "removeScrumPlayer", path = "operationstatus/removeplayer")
    public void removeScrumPlayer(@Named("playerKey") String playerKey, User user)
        throws ServiceException {
        if (playerKey == null) {
            throw new NullPointerException();
        }
        
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            
            transaction.begin();
            ScrumPlayer scrumPlayer = AppEngineUtils.getObjectFromDatastore(ScrumPlayer.class, playerKey,
                    persistenceManager);
            ScrumUser scrumUser = scrumPlayer.getUser();
            scrumUser.setLastModDate(lastDate);
            scrumUser.setLastModUser(lastUser);
            persistenceManager.makePersistent(scrumUser);
            
            ScrumProject scrumProject = scrumPlayer.getProject();
            scrumProject.setLastModDate(lastDate);
            scrumProject.setLastModUser(lastUser);
            persistenceManager.makePersistent(scrumProject);

            persistenceManager.deletePersistent(scrumPlayer);
            transaction.commit();

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    private static void sendNotificationEMail(String address, String projectName) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        String msgBody = "Install the Android application. " 
                + "Login with the Google Account associated with this E-Mail-Address.";

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("scrumtoolapp@gmail.com", "ScrumToolAapp"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(address, address));
            msg.setSubject("Scrumtool: You have been added to the project: "+projectName+"");
            msg.setText(msgBody);
            Transport.send(msg);

        } catch (AddressException e) {
            // ...
        } catch (MessagingException e) {
            // ...
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



}
