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
public class ScrumSprint {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    private String key;

    @Persistent
    private Date date;

    @Persistent
    private Set<ScrumIssue> issues;

    @Persistent
    private long lastModDate;

    @Persistent
    private String lastModUser;

    @Persistent
    private ScrumProject project;

    public String getKey() {
        return key;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<ScrumIssue> getIssues() {
        return issues;
    }

    public void setIssues(Set<ScrumIssue> issues) {
        this.issues = issues;
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

    public void setProject(ScrumProject project) {
        this.project = project;
    }

    public ScrumProject getProject() {
        return this.project;
    }
    
}