package ch.epfl.entity;

import com.google.appengine.api.datastore.Key;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author sylb
 */
@Entity
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Key key;
    private String name;
    private String description;
    private float estimation;
    private Player player;
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

    /*
    public Status getStatus() {
        return status;
    }
    */

}
