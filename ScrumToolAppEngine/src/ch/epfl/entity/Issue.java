package ch.epfl.entity;

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
    private Task task;
//    private Status status; TODO later
    
    public Key getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getEstimation() {
        return estimation;
    }

    public Player getAssignedPlayer() {
        return player;
    }


//    public Status getStatus() {
//        return status;
//    }

}
