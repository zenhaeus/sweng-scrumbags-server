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
 * @author sylb, aschneuw, zenhaeus
 * 
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Project {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String key;

    @Persistent
    private String name;

    @Persistent
    private String description;
    
    @Persistent
    private Date lastModDate;
    
    @Persistent
    private String lastModUser;

    @Persistent
    private Set<Player> players;
    
    @Persistent(mappedBy="project")
    private Set<MainTask> backlog;
    
    @Persistent(mappedBy="project")
    private Set<Sprint> sprints;

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
