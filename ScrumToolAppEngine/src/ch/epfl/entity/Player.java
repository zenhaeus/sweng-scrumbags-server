package ch.epfl.entity;

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
public class Player {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private User user;

    @Persistent
    private Role role;

    @Persistent
    private Project project;

    public Key getKey() {
        return key;
    }

    public User getAccount() {
        return user;
    }

    public void setAccount(User user) {
        this.user = user;
    }

    
    public Role getRole() { 
        return role; 
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}
