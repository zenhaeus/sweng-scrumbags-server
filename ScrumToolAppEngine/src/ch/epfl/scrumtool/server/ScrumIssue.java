package ch.epfl.scrumtool.server;

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
