/**
 * 
 */
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
 * 
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Project {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent
    private Player admin;

    @Persistent(mappedBy="project")
    private Set<Player> players;

    @Persistent(mappedBy="project")
    private Set<MainTask> backlog;
    
    @Persistent(mappedBy="project")
    private Set<Sprint> sprints;

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

    public void setDescription(String description) {
        this.description = description;
    }

    public Player getAdmin() {
        return admin;
    }

    public void setAdmin(Player admin) {
        this.admin = admin;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Set<Player> players) {
        this.players = players;
    }

    public Set<MainTask> getBacklog() {
        return backlog;
    }

    public void setBacklog(Set<MainTask> backlog) {
        this.backlog = backlog;
    }
    
    public Set<Sprint> getSprint() {
    	return sprints;
    }
    
    public void setSprints(Set<Sprint> sprints) {
    	this.sprints = sprints;
    }

    public int getChangesCount(User user) {
        return 1;
    }

}
