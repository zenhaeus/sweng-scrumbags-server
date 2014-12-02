package ch.epfl.scrumtool.server;

import static ch.epfl.scrumtool.server.Status.FINISHED;
import static ch.epfl.scrumtool.server.Status.IN_SPRINT;
import static ch.epfl.scrumtool.server.Status.READY_FOR_ESTIMATION;
import static ch.epfl.scrumtool.server.Status.READY_FOR_SPRINT;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.datanucleus.annotations.Unowned;

/**
 * @author sylb, aschneuw, zenhaeus
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ScrumIssue {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    private String key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent
    private float estimation;
    
    @Persistent
    private ScrumMainTask maintask;

    @Unowned
    @Persistent
    private ScrumPlayer player;

    @Persistent
    private Status status;
    
    @Persistent
    private Priority priority;
    
    @Unowned
    @Persistent
    private ScrumSprint sprint;

    @Persistent
    private long lastModDate;

    @Persistent
    private String lastModUser;
    
    /**
     * Verifies the status for the issue according to the estimation and sprint
     * it is or is not assigned, and enforces the right status.
     * 
     * @return true if any modifications were made,
     *         false otherwise
     */
    public boolean verifyAndSetStatus() {
        if (status == FINISHED) {
            // No further checks needed
            return false;
        }
        
        if (Float.compare(estimation, 0f) <= 0) {
            if (status == READY_FOR_ESTIMATION) {
                return false;
            } else {
                status = READY_FOR_ESTIMATION;
                return true;
            }
        } else {
            // If no sprint was yet assigned, we must be in state
            // READY_FOR_SPRINT, otherwise in state IN_SPRINT
            if (sprint == null) {
                if (status == READY_FOR_SPRINT) {
                    return false;
                } else {
                    status = READY_FOR_SPRINT;
                    return true;
                }
            } else {
                if (status == IN_SPRINT) {
                    return false;
                } else {
                    status = IN_SPRINT;
                    return true;
                }
            }
        }
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Priority getPriority() {
        return this.priority;
    }
    
    public void setSprint(ScrumSprint sprint) {
        this.sprint = sprint;
    }
    
    public ScrumSprint getSprint() {
        return this.sprint;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String aKey) {
        this.key = aKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getEstimation() {
        return estimation;
    }

    public void setEstimation(float estimation) {
        this.estimation = estimation;
    }

    public ScrumPlayer getAssignedPlayer() {
        return player;
    }

    public void setAssignedPlayer(ScrumPlayer player) {
        this.player = player;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getLastModDate() {
        return this.lastModDate;
    }

    public void setLastModDate(long date) {
        this.lastModDate = date;
    }

    public String getLastModUser() {
        return this.lastModUser;
    }

    public void setLastModUser(String user) {
        this.lastModUser = user;
    }
    
    public ScrumMainTask getMainTask() {
        return this.maintask;
    }
    
    public void setMainTask(ScrumMainTask maintask) {
        this.maintask = maintask;
    }
}
