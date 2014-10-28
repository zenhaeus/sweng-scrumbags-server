package ch.epfl.scrumtool.server;

import java.util.HashSet;
import java.util.Set;

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
    @PrimaryKey
    private String mName;

    @Persistent
    private String mEmail;

    @Persistent
    private Set<Key> mProjects;

    
    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }
    
    public Set<String> getProjects() {
        Set<String> keys = new HashSet<String>();
        for(Key k : mProjects) {
            keys.add(k.toString());
        }
        return keys;
    }

    public void setProjects(Set<String> projects) {
        this.mProjects.clear();
        for(String k : projects) {
            this.mProjects.add(KeyFactory.stringToKey(k));
        }
    }

}
