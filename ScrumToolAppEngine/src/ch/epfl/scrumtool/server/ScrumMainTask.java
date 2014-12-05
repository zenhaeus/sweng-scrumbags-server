package ch.epfl.scrumtool.server;

import static ch.epfl.scrumtool.server.Status.FINISHED;
import static ch.epfl.scrumtool.server.Status.IN_SPRINT;
import static ch.epfl.scrumtool.server.Status.READY_FOR_ESTIMATION;
import static ch.epfl.scrumtool.server.Status.READY_FOR_SPRINT;

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
 * @author Cyriaque Brousse
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
    private float totalTime;
    
    @NotPersistent
    private float timeFinished;
    
    /**
     * Verifies and sets the status for the task according to the issues'
     * status, and enforces the right status
     * 
     * @return true if any modifications were made, false otherwise
     */
    public boolean verifyAndSetStatusWithRespectToIssues() {
        // If issue set is null or empty, status should be READY_FOR_ESTIMATION
        if (issues == null || issues.isEmpty()) {
            if (status == READY_FOR_ESTIMATION) {
                return false;
            } else {
                setStatus(READY_FOR_ESTIMATION);
                return true;
            }
        }
        
        // Condition for FINISHED: all issues are FINISHED
        if (allIssuesHaveStatus(issues, FINISHED)) {
            if (status == FINISHED) {
                return false;
            } else {
                setStatus(FINISHED);
                return true;
            }
        }
        
        // Condition for READY_FOR_SPRINT: all issues are READY_FOR_SPRINT
        if (allIssuesHaveStatus(issues, READY_FOR_SPRINT)) {
            if (status == READY_FOR_SPRINT) {
                return false;
            } else {
                setStatus(READY_FOR_SPRINT);
                return true;
            }
        }
        
        // Condition for IN_SPRINT: (at least) one issue is IN_SPRINT and all
        // others are READY_FOR_SPRINT
        final Set<ScrumIssue> allInSprintIssues = allIssuesWithStatus(issues, IN_SPRINT);
        final Set<ScrumIssue> notInSprintIssues = new HashSet<ScrumIssue>(issues);
        notInSprintIssues.removeAll(allInSprintIssues);
        if (!allInSprintIssues.isEmpty() && allIssuesHaveStatus(notInSprintIssues, READY_FOR_SPRINT)) {
            if (status == IN_SPRINT) {
                return false;
            } else {
                setStatus(IN_SPRINT);
                return true;
            }
        } else { // Otherwise status is READY_FOR_ESTIMATION
            if (status == READY_FOR_ESTIMATION) {
                return false;
            } else {
                setStatus(READY_FOR_ESTIMATION);
                return true;
            }
        }
    }
    
    /**
     * @param issues
     *            the set to iterate on
     * @param status
     *            the status to check
     * @return true if all issues in the provided set have the specified status,
     *         false otherwise
     */
    private boolean allIssuesHaveStatus(Set<ScrumIssue> issues, Status status) {
        for (ScrumIssue i : issues) {
            if (i.getStatus() != status) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param issues
     *            the set to iterate on
     * @param status
     *            the status to check
     * @return the subset of {@code issues} where the status is the provided
     *         {@code status}
     */
    private Set<ScrumIssue> allIssuesWithStatus(Set<ScrumIssue> issues, Status status) {
        Set<ScrumIssue> allIssuesWithStatus = new HashSet<ScrumIssue>();
        for (ScrumIssue i : issues) {
            if (i.getStatus() == status) {
                allIssuesWithStatus.add(i);
            }
        }
        return allIssuesWithStatus;
    }

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
    public float getTotalTime() {
        return totalTime;
    }

    /**
     * @param totalTime the totalTime to set
     */
    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * @return the timeFinished
     */
    public float getTimeFinished() {
        return timeFinished;
    }

    /**
     * @param timeFinished the timeFinished to set
     */
    public void setTimeFinished(float timeFinished) {
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
