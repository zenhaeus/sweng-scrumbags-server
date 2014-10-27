package ch.epfl.entity;

import java.util.Date;
import java.util.Set;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * @author sylb
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Sprint {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    
    @Persistent
    private Date date;
    
    @Persistent(mappedBy="sprint")
    private Set<Issue> issues;
    
    public Key getKey() {
    	return key;
    }
    
    public Date getDate() {
    	return date;
    }
    
    public void setDate(Date date) {
    	this.date = date;
    }
    
    public Set<Issue> getIssues() {
    	return issues;
    }
    
    public void setIssues(Set<Issue> issues) {
    	this.issues = issues;
    }
    
}