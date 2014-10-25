package ch.epfl.entity;

import java.util.Set;

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
public class Task {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent(mappedBy = "task")
    private Set<Issue> issues;

    @Persistent
    private Project project;

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

//    public Status getStatus() {
//        return status;
//    }

    public Set<Issue> getIssues() {
        return issues;
    }
}
