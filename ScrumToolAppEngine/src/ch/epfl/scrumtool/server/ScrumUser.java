package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

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

    @Persistent
    private Set<Key> projects = new HashSet<Key>();

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

    public Set<String> getProjects() {
        final Logger log = Logger.getLogger(ScrumUser.class.getName()); 
        log.info("Trying to get projects");
        Set<String> keys = new HashSet<String>();
        for (Key k : projects) {
            keys.add(k.toString());
        }
        return keys;
    }

    public void setProjects(Set<String> projects) {
        this.projects.clear();
        for (String k : projects) {
            this.projects.add(KeyFactory.stringToKey(k));
        }
    }

}
