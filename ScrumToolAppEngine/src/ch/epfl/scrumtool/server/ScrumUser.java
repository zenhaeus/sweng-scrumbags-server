package ch.epfl.scrumtool.server;

import java.util.HashSet;
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
    
    @Persistent
    private String lastName;

    @Persistent(mappedBy = "user")
    private Set<ScrumPlayer> players;

    @Persistent
    private long lastModDate;

    @Persistent
    private String lastModUser;
    
    @Persistent
    private long dateOfBirth;
    
    @Persistent
    private String companyName;
    
    @Persistent
    private String jobTitle;
    
    @Persistent
    private String gender;
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getGender() {
        return this.gender;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getLastName() {
        return this.lastName;
    }
    
    public void setDateOfBirth(long dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public long getDateOfBirth() {
        return this.dateOfBirth;
    }
    
    public String getCompanyName() {
        return this.companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
    
    public String getJobTitle() {
        return jobTitle;
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

    public Set<ScrumPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(Set<ScrumPlayer> players) {
        this.players = players;
    }

    public long getLastModDate() {
        return this.lastModDate;
    }

    public void setLastModDate(long date) {
        this.lastModDate = date;
    }

    public String getLastModUser() {
        return this.lastModUser;
    }

    public void setLastModUser(String user) {
        this.lastModUser = user;
    }

    public void addPlayer(ScrumPlayer player) {
        if (players == null) {
            this.players = new HashSet<ScrumPlayer>();
        }
        players.add(player);
    }

    public void removePlayer(ScrumPlayer player) {
        this.players.remove(player);
    }
}
