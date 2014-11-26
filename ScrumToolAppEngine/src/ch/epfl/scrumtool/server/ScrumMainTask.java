package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
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

    @Persistent(mappedBy="maintask")
    private Set<ScrumIssue> issues;

    @Persistent
    private ScrumProject project;

    @Persistent
    private Status status;
    
    @Persistent
    private Priority priority;

    @Persistent
    private long lastModDate;

    @Persistent
    private String lastModUser;
    
    @NotPersistent
    private int issuesFinished;
    
    @NotPersistent
    private int totalIssues;
    
    @NotPersistent
    private long totalTime;
    
    @NotPersistent
    private long timeFinished;
    
    

    /**
     * @return the issuesFinished
     */
    public int getIssuesFinished() {
        return issuesFinished;
    }

    /**
     * @param issuesFinished the issuesFinished to set
     */
    public void setIssuesFinished(int issuesFinished) {
        this.issuesFinished = issuesFinished;
    }

    /**
     * @return the totalIssues
     */
    public int getTotalIssues() {
        return totalIssues;
    }

    /**
     * @param totalIssues the totalIssues to set
     */
    public void setTotalIssues(int totalIssues) {
        this.totalIssues = totalIssues;
    }

    /**
     * @return the totalTime
     */
    public long getTotalTime() {
        return totalTime;
    }

    /**
     * @param totalTime the totalTime to set
     */
    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * @return the timeFinished
     */
    public long getTimeFinished() {
        return timeFinished;
    }

    /**
     * @param timeFinished the timeFinished to set
     */
    public void setTimeFinished(long timeFinished) {
        this.timeFinished = timeFinished;
    }

    public Priority getPriority() {
        return this.priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
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
    
    public ScrumProject getProject() {
        return this.project;
    }
    
    public void setProject(ScrumProject project) {
        this.project = project;
    }

}
