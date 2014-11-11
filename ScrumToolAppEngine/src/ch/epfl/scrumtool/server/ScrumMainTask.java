package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author sylb
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ScrumMainTask {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    private String key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent
    private Set<ScrumIssue> issues;

    @Persistent
    private ScrumProject project;

    @Persistent
    private Status status;

    @Persistent
    private long lastModDate;

    @Persistent
    private String lastModUser;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<ScrumIssue> getIssues() {
        return issues;
    }

    public void addIssue(ScrumIssue issue) {
        if (issues == null) {
            this.issues = new HashSet<ScrumIssue>();
        }
        this.issues.add(issue);
    }

    public void removeIssue(ScrumIssue issue) {
        this.issues.remove(issue);
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

}
