package ch.epfl.scrumtool.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * @author sylb
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Issue {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent
    private float estimation;

    @Persistent
    private Player player;
    
    @Persistent
    private MainTask task;
    
    @Persistent
    private Sprint sprint;
    
    @Persistent
    private Status status;
    
    public Key getKey() {
        return key;
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

    public Player getAssignedPlayer() {
        return player;
    }
    
    public void setAsignedPlayer(Player player) {
        this.player = player;
    }
    
    public MainTask getTask() {
        return task;
    }
    
    public void setTask(MainTask task) {
        this.task = task;
    }
    
    public Sprint getSprint() {
        return sprint;
    }
    
    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

   public Status getStatus() {
        return status;
    }
   
   public void setStatus(Status status) {
       this.status = status;
   }

}
