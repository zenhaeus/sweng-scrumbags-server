package ch.epfl.scrumtool.server;

import java.util.Date;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author sylb
 *
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Sprint {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String key;
    
    @Persistent
    private Date date;
    
    @Persistent
    private Set<Issue> issues;
    
    @Persistent
    private Date lastModDate;
    
    @Persistent
    private String lastModUser;
    
    public String getKey() {
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
    
    public Date getLastModDate() {
        return this.getLastModDate();
    }
    
    public void setLastModDate(Date date){
        this.lastModDate = date;
    }
    
    public String getLastModUser() {
        return this.lastModUser;
    }
    
    public void setLastModUser(String user) {
        this.lastModUser = user;
    }
    
}