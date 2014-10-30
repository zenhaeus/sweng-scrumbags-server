package ch.epfl.scrumtool.server;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * @author sylb, aschneuw, zenhaeus
 */

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class ScrumIssue {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String key;

    @Persistent
    private String name;

    @Persistent
    private String description;

    @Persistent
    private float estimation;

    @Persistent
    private ScrumPlayer player;
    
    @Persistent
    private Status status;
    
    @Persistent
    private Date lastModDate;
    
    @Persistent
    private String lastModUser;
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public float getEstimation() {
        return estimation;
    }
    
    public void setEstimation(float estimation) {
        this.estimation = estimation;
    }

    public ScrumPlayer getAssignedPlayer() {
        return player;
    }
    
    public void setAssignedPlayer(ScrumPlayer player) {
        this.player = player;
    }
    

   public Status getStatus() {
        return status;
    }
   
   public void setStatus(Status status) {
       this.status = status;
   }
   
   public Date getLastModDate() {
       return this.getLastModDate();
   }
   
   public void setLastModDate(Date date){
       this.lastModDate = date;
   }
   
   public String getLastModUser() {
       return this.lastModUser;
   }
   
   public void setLastModUser(String user) {
       this.lastModUser = user;
   }

}
