package ch.epfl.entity;

import java.util.Set;

import com.google.appengine.api.datastore.Key;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author sylb
 */
@Entity
public class Task {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

	private Key key;
    private String name;
    private String description;
    private Set<Issue> issues;
//    private Status status; TODO to implement later

    public Key getKey() {
        return key;
    }
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /*
    public Status getStatus() {
        return status;
    }
    */

    public Set<Issue> getIssues() {
        return issues;
    }

}
