/*
 * Policy.java
 *
 * Created on March 24, 2004, 8:58 AM
 */

package dpm.content.constraint;

import dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement;
import dpm.peer.Peer;


/**
 *
 * @author  cumming
 *A policy represents a role [name] that ANY Peer can assume and then can make input,
 *for the entity to change state
 */
public class Policy extends Constraint {
    
    public Policy(PolicyAdvertisement polAdv, Peer appUser) {
        super(polAdv, appUser);
    }
    
    public dpm.content.advertisement.designEntity.related.ConstraintAdv getAdv() {
        return (PolicyAdvertisement)adv;
    }
    
}
