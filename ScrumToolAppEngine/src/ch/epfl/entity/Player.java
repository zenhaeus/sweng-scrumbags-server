package ch.epfl.entity;

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
public class Player {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Key key;
    private User user;
//    private Role role; TODO implement the enum

    public Key getKey() {
        return key;
    }
    
    public User getAccount() {
        return user;
    }

    public void setAccount(User user) {
        this.user = user;
    }

    /*
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    */

}
