package ch.epfl.scrumtool.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;



@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Player {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    @Persistent
    private ScrumUser user;

    @Persistent
    private Role role;

    @Persistent
    private Project project;

    public Key getKey() {
        return key;
    }

    public ScrumUser getAccount() {
        return user;
    }

    public void setAccount(ScrumUser user) {
        this.user = user;
    }

    
    public Role getRole() { 
        return role; 
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}
