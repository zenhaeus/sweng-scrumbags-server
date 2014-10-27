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
public class MainTask {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent(mappedBy="mainTask")
    private Set<Issue> issues;

    @Persistent
    private Project project;

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
    
    public void setdescription(String description) {
    	this.description = description;
    }

    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
    	this.status = status; 	
    }

    public Set<Issue> getIssues() {
        return issues;
    }
    
    public void setIssues(Set<Issue> issues) {
    	this.issues = issues;
    }
    
}
