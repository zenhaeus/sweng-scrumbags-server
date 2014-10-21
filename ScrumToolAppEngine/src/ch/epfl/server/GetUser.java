package ch.epfl.server;

import com.google.appengine.api.datastore.Key;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Sylvain
 * 
 */
@Entity
public class GetUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;
    private String name;
    private String email;

    public Key getKey() {
        return key;
    }

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
}
