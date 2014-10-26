package ch.epfl.scrumtool.server;

import java.util.Set;

import com.google.appengine.api.datastore.Key;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author sylb, aschneuw
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ScrumUser {
    @PrimaryKey
    private String name;

    @Persistent
    private String email;

    @Persistent
    private Set<Key> projects;

    
    public String getName() {
        return name;
    }

    public void setName(String nName) {
        this.name = nName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String eEmail) {
        this.email = eEmail;
    }
    
    public Set<Key> getProjects() {
        return projects;
    }

    public void addProject(Key project) {
        projects.add(project);
    }

}
