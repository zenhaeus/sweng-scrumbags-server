package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.persistence.EntityNotFoundException;

import ch.epfl.scrumtool.AppEngineUtils;
import ch.epfl.scrumtool.PMF;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
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
        }, audiences = 
                { 
            Constants.ANDROID_AUDIENCE 
            }
        )
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
    public KeyResponse insertScrumIssue(ScrumIssue scrumIssue,
            @Named("mainTaskKey") String maintaskKey,
            @Nullable @Named("playerKey") String playerKey,
            @Nullable @Named("SprintKey") String sprintKey, User user)
            throws ServiceException {
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumMainTask scrumMainTask = persistenceManager.getObjectById(
                    ScrumMainTask.class, maintaskKey);
            transaction.begin();

            // Add the issue
            scrumMainTask.getIssues().add(scrumIssue);
            persistenceManager.makePersistent(scrumMainTask);

            // Assign the player if there is one
            if (playerKey != null) {
                ScrumPlayer scrumPlayer = persistenceManager.getObjectById(
                        ScrumPlayer.class, playerKey);
                scrumIssue.setAssignedPlayer(scrumPlayer);
                scrumPlayer.addIssue(scrumIssue);
                scrumPlayer.getProject();
                scrumPlayer.getAdminFlag();
                scrumPlayer.getRole();
                persistenceManager.makePersistent(scrumPlayer);
            }

            // Assign a sprint if there is one
            if (sprintKey != null) {
                ScrumSprint scrumSprint = persistenceManager.getObjectById(
                        ScrumSprint.class, sprintKey);
                scrumIssue.setSprint(scrumSprint);
                scrumSprint.addIssue(scrumIssue);
                persistenceManager.makePersistent(scrumSprint);
            }

            persistenceManager.makePersistent(scrumIssue);
            transaction.commit();

            return new KeyResponse(scrumIssue.getKey());
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    @ApiMethod(name = "loadIssuesByMainTask")
    public CollectionResponse<ScrumIssue> loadIssuesByMainTask(
            @Named("mainTaskKey") String maintaskKey, User user)
            throws ServiceException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();

        Set<ScrumIssue> issues = null;
        try {
            ScrumMainTask scrumMaintask = null;
            scrumMaintask = persistenceManager.getObjectById(
                    ScrumMainTask.class, maintaskKey);
            issues = scrumMaintask.getIssues();

            // Lazy Fetch
            for (ScrumIssue i : issues) {
                if (i.getAssignedPlayer() != null) {
                    i.getAssignedPlayer().getUser();
                }
                i.getSprint();
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumIssue>builder().setItems(issues)
                .build();
    }

    @ApiMethod(name = "loadIssuesBySprint")
    public CollectionResponse<ScrumIssue> loadIssuesBySprint(
            @Named("sprintKey") String sprintKey, User user)
            throws ServiceException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();

        Set<ScrumIssue> issues = null;
        try {
            ScrumSprint scrumSprint = null;
            scrumSprint = persistenceManager.getObjectById(ScrumSprint.class,
                    sprintKey);
            issues = scrumSprint.getIssues();

            // Lazy Fetch
            for (ScrumIssue i : issues) {
                if (i.getAssignedPlayer() != null) {
                    i.getAssignedPlayer().getUser();
                    i.getAssignedPlayer().getRole();
                    persistenceManager.makeTransient(i.getAssignedPlayer());
                    persistenceManager.makeTransient(i.getAssignedPlayer().getUser());
                    i.getAssignedPlayer().setIssues(null);
                    i.getAssignedPlayer().getUser().setPlayers(null);
                    
                }
                i.getSprint();
                persistenceManager.makeTransient(i.getSprint());
                i.getSprint().setIssues(null);
                i.getSprint().setProject(null);
                i.getStatus();
                i.getPriority();
                
                
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumIssue>builder().setItems(issues).build();
    }

    @ApiMethod(name = "loadIssuesForUser")
    public CollectionResponse<ScrumIssue> loadIssuesForUser(
            @Named("userKey") String userKey, User user)
            throws ServiceException {

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        
        Set<ScrumIssue> issues = new HashSet<ScrumIssue>();
        try {
            Set<ScrumPlayer> players = persistenceManager.getObjectById(ScrumUser.class, userKey).getPlayers();
            for (ScrumPlayer p: players) {
                Set<ScrumIssue> is = p.getIssues(); 
                for (ScrumIssue issue: is) {
                    if (issue.getStatus() != Status.FINISHED) {
                        issues.add(issue);
                        issue.getMainTask().getProject();
                        issue.getAssignedPlayer().getUser();
                        issue.getPriority();
                        issue.getSprint();
                        persistenceManager.makeTransient(issue);
                        persistenceManager.makeTransient(issue.getSprint());
                        persistenceManager.makeTransient(issue.getAssignedPlayer());
                        persistenceManager.makeTransient(issue.getMainTask());
                        persistenceManager.makeTransient(issue.getMainTask().getProject());
                        persistenceManager.makeTransient(issue.getAssignedPlayer().getUser());
                        issue.getAssignedPlayer().getUser().setPlayers(null);
                        issue.getAssignedPlayer().setIssues(null);
                        issue.getMainTask().setIssues(null);
                        issue.getMainTask().getProject().setBacklog(null);
                        issue.getMainTask().getProject().setPlayers(null);
                        issue.getMainTask().getProject().setSprints(null);
                        if (issue.getSprint() != null) {
                            issue.getSprint().setProject(null);
                        }
                        
                        
                    }
                }
            }
            
        } finally {
            persistenceManager.close();
        }
        
        
        
        return CollectionResponse.<ScrumIssue>builder().setItems(issues).build();
    }
    
    /**
     * This methods returns all the issues of the given project that are not
     * yet assigned to any sprint.
     * 
     * @param projectKey
     * @param user
     * @return
     * @throws ServiceException
     */
    @ApiMethod(name = "loadUnsprintedIssuesForProject")
    public CollectionResponse<ScrumIssue> loadUnsprintedIssuesForProject(
            @Named("projectKey") String projectKey, User user) throws ServiceException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();

        Set<ScrumIssue> issues = new HashSet<ScrumIssue>();
        try {
            ScrumProject project = persistenceManager.getObjectById(ScrumProject.class, projectKey);
            for (ScrumMainTask m : project.getBacklog()) {
                for (ScrumIssue i : m.getIssues()) {
                    if (i.getSprint() == null) {
                        i.getAssignedPlayer();
                        i.getMainTask();
                        i.getPriority();
                        i.getStatus();
                        persistenceManager.makeTransient(i);
                        
                        if (i.getAssignedPlayer() != null) {
                            i.getAssignedPlayer().getUser();
                            persistenceManager.makeTransient(i.getAssignedPlayer());
                            persistenceManager.makeTransient(i.getAssignedPlayer().getUser());
                            i.getAssignedPlayer().getUser().setPlayers(null);
                            i.getAssignedPlayer().setIssues(null);
                        }
                        persistenceManager.makeTransient(i.getMainTask());
                        i.getMainTask().setIssues(null);
                        issues.add(i);
                    }
                }
            }
        } finally {
            persistenceManager.close();
        }
        return CollectionResponse.<ScrumIssue>builder().setItems(issues).build();
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
    public void updateScrumIssue(ScrumIssue update,
            @Nullable @Named("playerKey") String playerKey,
            @Nullable @Named("SprintKey") String sprintKey,
            User user)
            throws ServiceException {

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

            if (playerKey != null) {
                if (scrumIssue.getAssignedPlayer() == null) {
                    ScrumPlayer scrumPlayer = persistenceManager.getObjectById(
                            ScrumPlayer.class, playerKey);
                    scrumPlayer.addIssue(scrumIssue);
                    scrumIssue.setAssignedPlayer(scrumPlayer);
                    persistenceManager.makePersistent(scrumPlayer);
                } else if (!scrumIssue.getAssignedPlayer().getKey()
                        .equals(playerKey)) {
                    scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
                    ScrumPlayer scrumPlayer = persistenceManager.getObjectById(
                            ScrumPlayer.class, playerKey);
                    scrumPlayer.addIssue(scrumIssue);
                    scrumIssue.setAssignedPlayer(scrumPlayer);
                    persistenceManager.makePersistent(scrumPlayer);
                }
            } else {
                if (scrumIssue.getAssignedPlayer() != null) {
                    scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
                }
            }

            // update the sprint only if necessary
            if (sprintKey != null) {
                if (scrumIssue.getSprint() == null) {
                    ScrumSprint scrumSprint = persistenceManager.getObjectById(
                            ScrumSprint.class, sprintKey);
                    scrumSprint.addIssue(scrumIssue);
                    scrumIssue.setSprint(scrumSprint);
                    persistenceManager.makePersistent(scrumSprint);
                } else if (!scrumIssue.getSprint().getKey()
                        .equals(sprintKey)) {
                    scrumIssue.getSprint().removeIssue(scrumIssue);
                    ScrumSprint scrumSprint = persistenceManager.getObjectById(
                            ScrumSprint.class, sprintKey);
                    scrumSprint.addIssue(scrumIssue);
                    scrumIssue.setSprint(scrumSprint);
                    persistenceManager.makePersistent(scrumSprint);
                }
            } else {
                if (scrumIssue.getSprint() != null) {
                    scrumIssue.getSprint().removeIssue(scrumIssue);
                }
            }
            persistenceManager.makePersistent(scrumIssue);
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    @ApiMethod(name = "insertIssueInSprint", path = "operationstatus/insertIssueInSprint")
    public void insertScrumIssueInSprint(
            @Named("issueKey") String issueKey,
            @Named("sprintKey") String sprintKey, User user)
            throws ServiceException {
        AppEngineUtils.basicAuthentication(user);

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
     */
    @ApiMethod(name = "removeScrumIssueFromSprint", path = "operationstatus/removeIssueFromSprint")
    public void removeScrumIssueFromSprint(
            @Named("issueKey") String issueKey, User user)
            throws ServiceException {
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {

            ScrumIssue scrumIssue = persistenceManager.getObjectById(
                    ScrumIssue.class, issueKey);
            
            ScrumSprint scrumSprint = scrumIssue.getSprint();
            
            scrumIssue.setSprint(null);
            scrumSprint.getIssues().remove(scrumIssue);

            transaction.begin();
            persistenceManager.makePersistent(scrumIssue);
            persistenceManager.makePersistent(scrumSprint);
            transaction.commit();

        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
    }

    @ApiMethod(name = "removeScrumIssue", path = "operationstatus/removeIssue")
    public void removeScrumIssue(@Named("issueKey") String issueKey,
            User user) throws ServiceException {
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            transaction.begin();
            ScrumIssue scrumIssue = persistenceManager.getObjectById(
                    ScrumIssue.class, issueKey);
            if (scrumIssue.getSprint() != null) {
                scrumIssue.getSprint().removeIssue(scrumIssue);
            }
            if (scrumIssue.getAssignedPlayer() != null) {
                scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
            }
            if (scrumIssue.getSprint() != null) {
                scrumIssue.getSprint().removeIssue(scrumIssue);
            }
            persistenceManager.deletePersistent(scrumIssue);
            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            persistenceManager.close();
        }
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
