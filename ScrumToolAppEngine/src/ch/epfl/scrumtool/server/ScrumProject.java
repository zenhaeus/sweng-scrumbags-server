package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.datanucleus.annotations.Unowned;

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

    @Unowned
    @Persistent
    private Set<ScrumPlayer> players;

    @Persistent(mappedBy = "project")
    private Set<ScrumMainTask> backlog;

    @Persistent(mappedBy = "project")
    private Set<ScrumSprint> sprints;

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

    public Set<ScrumPlayer> getPlayers() {
        return players;
    }
    
    public void addPlayer(ScrumPlayer player) {
        if (players == null) {
            players = new HashSet<ScrumPlayer>();
        }
        this.players.add(player);
    }
    
    public void removePlayer(ScrumPlayer player) {
        this.players.remove(player);
    }

    public void setPlayers(Set<ScrumPlayer> players) {
        this.players = players;
    }

    public Set<ScrumMainTask> getBacklog() {
        return backlog;
    }
    
    public void addMaintask(ScrumMainTask maintask) {
        if (backlog == null) {
            backlog = new HashSet<ScrumMainTask>();
        }
        this.backlog.add(maintask);
    }
    
    public void removeMaintask(ScrumMainTask maintask) {
        this.backlog.remove(maintask);
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
    
    public void addSprint(ScrumSprint sprint) {
        if (sprints == null) {
            sprints = new HashSet<ScrumSprint>();
        }
        this.sprints.add(sprint);
    }
    
    public void removeSprint(ScrumSprint sprint) {
        this.sprints.remove(sprint);
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
