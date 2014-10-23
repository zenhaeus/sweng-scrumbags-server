package ch.epfl.entity;

import javax.persistence.Entity;

/**
 * @author sylb
 */
@Entity
public enum Role {
    PRODUCT_OWNER, STAKEHOLDER, SCRUM_MASTER, DEVELOPER;
}
// TODO find how to implement an enum