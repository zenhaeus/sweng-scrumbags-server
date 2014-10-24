/**
 * 
 */
package ch.epfl.entity;

import java.util.Set;

import com.google.appengine.api.datastore.Key;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author sylb
 * 
 */
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
    private String name;
    private String description;
    private Player admin;
    private Set<Player> players;
    private Set<Task> backlog;

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

    public Set<Task> getBacklog() {
        return backlog;
    }

    public void setBacklog(Set<Task> backlog) {
        this.backlog = backlog;
    }

    public int getChangesCount(User user) {
        return 1;
    }

}
