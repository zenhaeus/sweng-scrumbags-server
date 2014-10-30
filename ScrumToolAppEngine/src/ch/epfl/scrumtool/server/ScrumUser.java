package ch.epfl.scrumtool.server;

import java.util.Date;
import java.util.Set;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author sylb, aschneuw
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ScrumUser {
    @Persistent
    @PrimaryKey
    private String email;

    @Persistent
    private String name;

    @Persistent(mappedBy="user")
    private Set<Player> players;
    
    @Persistent
    private Date lastModDate;
    
    @Persistent
    private String lastModUser;
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public void setProjects(Set<Player> players) {
        this.players = players;
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
