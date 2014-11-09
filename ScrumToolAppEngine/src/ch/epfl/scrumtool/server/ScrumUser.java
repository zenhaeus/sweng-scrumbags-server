package ch.epfl.scrumtool.server;

import java.util.ArrayList;
import java.util.List;

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

    @Persistent(mappedBy = "user")
    private List<ScrumPlayer> players;

    @Persistent
    private long lastModDate;

    @Persistent
    private String lastModUser;

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

    public List<ScrumPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<ScrumPlayer> players) {
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
        if (players != null) {
            players.add(player);
        } else {
            this.players = new ArrayList<ScrumPlayer>();

        }
    }
}
