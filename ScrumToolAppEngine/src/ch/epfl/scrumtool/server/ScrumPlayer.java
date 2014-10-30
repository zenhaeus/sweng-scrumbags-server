package ch.epfl.scrumtool.server;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ScrumPlayer {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String key;

    @Persistent
    private ScrumUser user;

    @Persistent
    private Role role;
    
    @Persistent
    private boolean admin;
    
    @Persistent
    private long lastModDate;
    
    @Persistent
    private String lastModUser;

    public String getKey() {
        return key;
    }
    
    public String setKey() {
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
    
    public long getLastModDate() {
        return this.lastModDate;
    }
    
    public void setLastModDate(long date){
        this.lastModDate = date;
    }
    
    public String getLastModUser() {
        return this.lastModUser;
    }
    
    public void setLastModUser(String user) {
        this.lastModUser = user;
    }
    
    public void setAdminFlag(boolean admin){
        this.admin = admin;
    } 
    
    public boolean getAdminFlag(){
        return this.admin;
    } 
}
