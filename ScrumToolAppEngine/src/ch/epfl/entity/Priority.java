package ch.epfl.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

/**
 * @author sylb
 */
@Entity
public enum Priority {
    LOW("LOW"),
    NORMAL("NORMAL"),
    HIGH("HIGH"),
    URGENT("URGENT");
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Key key;
    private String stringValue;
    
    public Key getKey() {
        return key;
    }
    
    private Priority(String stringValue) {
        this.stringValue = stringValue;
    }
    
    @Override
    public String toString() {
        return stringValue;
    }
}
