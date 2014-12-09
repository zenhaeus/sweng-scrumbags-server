package ch.epfl.scrumtool.server;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import ch.epfl.scrumtool.AppEngineUtils;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.users.User;

/**
 * @author aschneuw
 * @author sylb
 * @author Cyriaque Brousse
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
        if (scrumIssue == null || maintaskKey == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            ScrumMainTask scrumMainTask = AppEngineUtils.getObjectFromDatastore(ScrumMainTask.class, maintaskKey,
                    persistenceManager);
            transaction.begin();

            // Add issue
            scrumMainTask.getIssues().add(scrumIssue);
            persistenceManager.makePersistent(scrumMainTask);
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();

            // Assign the player if there is one
            if (playerKey != null) {
                ScrumPlayer scrumPlayer = AppEngineUtils.getObjectFromDatastore(ScrumPlayer.class, playerKey,
                        persistenceManager);
                scrumIssue.setAssignedPlayer(scrumPlayer);
                scrumPlayer.addIssue(scrumIssue);
                scrumPlayer.getProject();
                scrumPlayer.getAdminFlag();
                scrumPlayer.getRole();
                scrumPlayer.setLastModDate(lastDate);
                scrumPlayer.setLastModUser(lastUser);
                persistenceManager.makePersistent(scrumPlayer);
            }

            // Assign a sprint if there is one
            if (sprintKey != null) {
                ScrumSprint scrumSprint = AppEngineUtils.getObjectFromDatastore(ScrumSprint.class, sprintKey,
                        persistenceManager);
                scrumIssue.setSprint(scrumSprint);
                scrumSprint.addIssue(scrumIssue);
                scrumSprint.setLastModDate(lastDate);
                scrumSprint.setLastModUser(lastUser);
                persistenceManager.makePersistent(scrumSprint);
            }
            
            // Check status and update if necessary
            scrumIssue.verifyAndSetStatus();
            
            scrumIssue.setLastModDate(lastDate);
            scrumIssue.setLastModUser(lastUser);
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
        if (maintaskKey == null) {
            throw new NullPointerException();
        }

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();

        Set<ScrumIssue> issues = null;
        try {
            ScrumMainTask scrumMaintask = null;
            scrumMaintask = AppEngineUtils.getObjectFromDatastore(ScrumMainTask.class, maintaskKey, persistenceManager);
            issues = scrumMaintask.getIssues();

            // Lazy Fetch
            for (ScrumIssue i : issues) {
                i.getKey();
                i.getDescription();
                i.getName();
                i.getEstimation();
                i.getPriority();
                i.verifyAndSetStatus();
                i.getStatus();
                if (i.getAssignedPlayer() != null) {
                    ScrumPlayer p = i.getAssignedPlayer();
                    p.getAdminFlag();
                    p.getInvitedFlag();
                    p.getRole();
                    p.getUser().getName();
                    p.getUser().getEmail();
                    p.getUser().getCompanyName();
                    p.getUser().getDateOfBirth();
                    p.getUser().getGender();
                    p.getUser().getJobTitle();
                    p.getUser().getLastName();
                    persistenceManager.makeTransient(p.getUser());
                    p.getUser().setPlayers(null);
                    persistenceManager.makeTransient(p);
                    p.setProject(null);
                }
                if (i.getSprint() != null) {
                    i.getSprint().getKey();
                    i.getSprint().getDate();
                    i.getSprint().getTitle();
                    persistenceManager.makeTransient(i.getSprint());
                    i.getSprint().setProject(null);
                    i.getSprint().setIssues(null);
                }
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
        if (sprintKey == null) {
            throw new NullPointerException();
        }

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();

        Set<ScrumIssue> issues = null;
        try {
            ScrumSprint scrumSprint = null;
            scrumSprint = AppEngineUtils.getObjectFromDatastore(ScrumSprint.class, sprintKey, persistenceManager);
            issues = scrumSprint.getIssues();

         // Lazy Fetch
            for (ScrumIssue i : issues) {
                i.getKey();
                i.getDescription();
                i.getName();
                i.getEstimation();
                i.getPriority();
                i.verifyAndSetStatus();
                i.getStatus();
                if (i.getAssignedPlayer() != null) {
                    ScrumPlayer p = i.getAssignedPlayer();
                    p.getAdminFlag();
                    p.getInvitedFlag();
                    p.getRole();
                    p.getUser().getName();
                    p.getUser().getEmail();
                    p.getUser().getCompanyName();
                    p.getUser().getDateOfBirth();
                    p.getUser().getGender();
                    p.getUser().getJobTitle();
                    p.getUser().getLastName();
                    persistenceManager.makeTransient(p.getUser());
                    p.getUser().setPlayers(null);
                    persistenceManager.makeTransient(p);
                    p.setProject(null);
                }
                if (i.getSprint() != null) {
                    i.getSprint().getKey();
                    i.getSprint().getDate();
                    i.getSprint().getTitle();
                    persistenceManager.makeTransient(i.getSprint());
                    i.getSprint().setProject(null);
                    i.getSprint().setIssues(null);
                }
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
        if (userKey == null) {
            throw new NullPointerException();
        }

        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        
        Set<ScrumIssue> issues = new HashSet<ScrumIssue>();
        try {
            Set<ScrumPlayer> players = AppEngineUtils.getObjectFromDatastore(ScrumUser.class, userKey,
                    persistenceManager).getPlayers();
            for (ScrumPlayer p: players) {
                Set<ScrumIssue> is = p.getIssues(); 
                for (ScrumIssue issue: is) {
                    if (issue.getStatus() != Status.FINISHED) {
                        issues.add(issue);
                        issue.getDescription();
                        issue.getKey();
                        issue.getEstimation();
                        issue.getName();
                        issue.getPriority();
                        issue.verifyAndSetStatus();
                        issue.getStatus();
                        issue.getMainTask().getPriority();
                        issue.getMainTask().getStatus();
                        issue.getMainTask().getName();
                        issue.getMainTask().getDescription();
                        issue.getMainTask().getKey();
                        issue.getMainTask().getProject().getName();
                        issue.getMainTask().getProject().getDescription();
                        issue.getAssignedPlayer();
                        issue.getAssignedPlayer().getUser();
                        if (issue.getSprint() != null) {
                            issue.getSprint().getTitle();
                            issue.getSprint().getDate();
                            issue.getSprint().getKey();
                        }
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
                            issue.getSprint().setIssues(null);
                        }
                    }
                    issue.verifyAndSetStatus();
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
        if (projectKey == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();

        Set<ScrumIssue> issues = new HashSet<ScrumIssue>();
        try {
            ScrumProject project = AppEngineUtils.getObjectFromDatastore(ScrumProject.class, projectKey,
                    persistenceManager);
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
                        i.verifyAndSetStatus();
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
        if (update == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();
        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            
            transaction.begin();
            ScrumIssue scrumIssue = AppEngineUtils.getObjectFromDatastore(ScrumIssue.class, update.getKey(),
                    persistenceManager);
            scrumIssue.setName(update.getName());
            scrumIssue.setDescription(update.getDescription());
            scrumIssue.setEstimation(update.getEstimation());
            scrumIssue.setLastModDate(lastDate);
            scrumIssue.setLastModUser(lastUser);
            scrumIssue.setStatus(update.getStatus());
            scrumIssue.setPriority(update.getPriority());

            // update the player only if necessary
            if (playerKey != null) {
                if (scrumIssue.getAssignedPlayer() == null) {
                    ScrumPlayer scrumPlayer = AppEngineUtils.getObjectFromDatastore(ScrumPlayer.class, playerKey,
                            persistenceManager);
                    scrumPlayer.addIssue(scrumIssue);
                    scrumIssue.setAssignedPlayer(scrumPlayer);
                    scrumPlayer.setLastModDate(lastDate);
                    scrumPlayer.setLastModUser(lastUser);
                    persistenceManager.makePersistent(scrumPlayer);
                } else if (!scrumIssue.getAssignedPlayer().getKey().equals(playerKey)) {
                    scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
                    ScrumPlayer scrumPlayer = AppEngineUtils.getObjectFromDatastore(ScrumPlayer.class, playerKey,
                            persistenceManager);
                    scrumPlayer.addIssue(scrumIssue);
                    scrumIssue.setAssignedPlayer(scrumPlayer);
                    scrumPlayer.setLastModDate(lastDate);
                    scrumPlayer.setLastModUser(lastUser);
                    persistenceManager.makePersistent(scrumPlayer);
                }
            } else {
                if (scrumIssue.getAssignedPlayer() != null) {
                    scrumIssue.getAssignedPlayer().setLastModDate(lastDate);
                    scrumIssue.getAssignedPlayer().setLastModUser(lastUser);
                    scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
                    scrumIssue.setAssignedPlayer(null);
                }
            }

            // update the sprint only if necessary
            if (sprintKey != null) {
                if (scrumIssue.getSprint() == null) {
                    ScrumSprint scrumSprint = AppEngineUtils.getObjectFromDatastore(ScrumSprint.class, sprintKey,
                            persistenceManager);
                    scrumSprint.addIssue(scrumIssue);
                    scrumIssue.setSprint(scrumSprint);
                    scrumSprint.setLastModDate(lastDate);
                    scrumSprint.setLastModUser(lastUser);
                    persistenceManager.makePersistent(scrumSprint);
                } else if (!scrumIssue.getSprint().getKey()
                        .equals(sprintKey)) {
                    scrumIssue.getSprint().removeIssue(scrumIssue);
                    ScrumSprint scrumSprint = AppEngineUtils.getObjectFromDatastore(ScrumSprint.class, sprintKey,
                            persistenceManager);
                    scrumSprint.addIssue(scrumIssue);
                    scrumIssue.setSprint(scrumSprint);
                    scrumSprint.setLastModDate(lastDate);
                    scrumSprint.setLastModUser(lastUser);
                    persistenceManager.makePersistent(scrumSprint);
                }
            } else {
                if (scrumIssue.getSprint() != null) {
                    scrumIssue.getSprint().removeIssue(scrumIssue);
                    scrumIssue.getSprint().setLastModDate(lastDate);
                    scrumIssue.getSprint().setLastModUser(lastUser);
                    scrumIssue.setSprint(null);
                }
            }
            scrumIssue.verifyAndSetStatus();
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
        if (issueKey == null || sprintKey == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            transaction.begin();
            ScrumSprint scrumSprint = AppEngineUtils.getObjectFromDatastore(ScrumSprint.class, sprintKey,
                    persistenceManager);
            ScrumIssue scrumIssue = AppEngineUtils.getObjectFromDatastore(ScrumIssue.class, issueKey,
                    persistenceManager);
            scrumIssue.setSprint(scrumSprint);
            scrumSprint.getIssues().add(scrumIssue);
            scrumIssue.setLastModDate(lastDate);
            scrumIssue.setLastModUser(lastUser);
            scrumSprint.setLastModDate(lastDate);
            scrumSprint.setLastModUser(lastUser);
            scrumIssue.verifyAndSetStatus();
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
        if (issueKey == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);

        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            
            ScrumIssue scrumIssue = AppEngineUtils.getObjectFromDatastore(ScrumIssue.class, issueKey,
                    persistenceManager);
            if (scrumIssue.getSprint() == null) {
                throw new NotFoundException("This Issue is not assigned to any Sprint");
            }
            ScrumSprint scrumSprint = scrumIssue.getSprint();
            
            scrumIssue.setSprint(null);
            scrumSprint.getIssues().remove(scrumIssue);
            scrumIssue.setLastModDate(lastDate);
            scrumIssue.setLastModUser(lastUser);
            scrumSprint.setLastModDate(lastDate);
            scrumSprint.setLastModUser(lastUser);

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
        if (issueKey == null) {
            throw new NullPointerException();
        }
        AppEngineUtils.basicAuthentication(user);
        PersistenceManager persistenceManager = AppEngineUtils.getPersistenceManager();
        Transaction transaction = persistenceManager.currentTransaction();

        try {
            long lastDate = Calendar.getInstance().getTimeInMillis();
            String lastUser = user.getEmail();
            transaction.begin();
            ScrumIssue scrumIssue = AppEngineUtils.getObjectFromDatastore(ScrumIssue.class, issueKey, 
                    persistenceManager);
            if (scrumIssue.getSprint() != null) {
                scrumIssue.getSprint().removeIssue(scrumIssue);
                scrumIssue.getSprint().setLastModDate(lastDate);
                scrumIssue.getSprint().setLastModUser(lastUser);
                persistenceManager.makePersistent(scrumIssue.getSprint());
            }
            if (scrumIssue.getAssignedPlayer() != null) {
                scrumIssue.getAssignedPlayer().removeIssue(scrumIssue);
                scrumIssue.getAssignedPlayer().setLastModDate(lastDate);
                scrumIssue.getAssignedPlayer().setLastModUser(lastUser);
                persistenceManager.makePersistent(scrumIssue.getAssignedPlayer());
            }
            if (scrumIssue.getSprint() != null) {
                scrumIssue.getSprint().removeIssue(scrumIssue);
                scrumIssue.getSprint().setLastModDate(lastDate);
                scrumIssue.getSprint().setLastModUser(lastUser);
                persistenceManager.makePersistent(scrumIssue.getSprint());
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

}
