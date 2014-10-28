package ch.epfl.scrumtool.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;

/**
 * @author sylb
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Role {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;
    
	@Persistent
	private RoleEnum role;

	public Key getKey() {
		return key;
	}
	
	public RoleEnum getRole() {
		return role;
	}
	
	public void setRole(RoleEnum value) {
		this.role = value;
	}
}