package ch.epfl.entity;

import java.util.Set;

import com.google.appengine.api.datastore.Key;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author sylb
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class User {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    
    @Persistent
    private long token;

    @Persistent
    private String name;

    @Persistent
    private String username;

    @Persistent
    private String email;

    @Persistent
    private Set<Key> projects;

    public Key getKey() {
        return key;
    }

    public long getToken() {
        return token;
    }

    public void setToken(long token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public Set<Key> getProjects() {
        return projects;
    }

    public void addProject(Key project) {
        projects.add(project);
    }

}
