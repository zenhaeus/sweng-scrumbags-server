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
 * @author sylb, aschneuw, zenhaeus
 * 
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ScrumProject {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
    private String key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent
    private long lastModDate;

    @Persistent
    private String lastModUser;

    @Persistent
    private Set<String> playerKeys;

    @Persistent(mappedBy = "project")
    private Set<ScrumMainTask> backlog;

    @Persistent(mappedBy = "project")
    private Set<ScrumSprint> sprints;

    public void addPlayerKey(String playerKey) {
        if (playerKeys == null) {
            playerKeys = new HashSet<String>();
        }
        playerKeys.add(playerKey);
    }
    
    public void removePlayerKey(String playerKey){
        playerKeys.remove(playerKey);
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

    public Set<String> getPlayerKeys() {
        return playerKeys;
    }

    public void setPlayerKeys(Set<String> playerKeys) {
        this.playerKeys = playerKeys;
    }

    public Set<ScrumMainTask> getBacklog() {
        return backlog;
    }

    public void setBacklog(Set<ScrumMainTask> backlog) {
        this.backlog = backlog;
    }

    public Set<ScrumSprint> getSprint() {
        return sprints;
    }

    public void setSprints(Set<ScrumSprint> sprints) {
        this.sprints = sprints;
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
