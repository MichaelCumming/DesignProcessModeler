/*
 * Constraint.java
 *
 * Created on March 24, 2004, 8:58 AM
 */

package dpm.content.constraint;

import dpm.content.advertisement.designEntity.related.ConstraintAdv;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import net.jxta.document.Advertisement;


/**
 *
 * @author  cumming
 */
public abstract class Constraint implements DpmTerms {
    protected ConstraintAdv adv;
    protected Peer appUser;
        
    /** Creates a new instance of Constraint. Constraint is an abstract class, and
     * therefore is only constructed using its sub-classes */
    protected Constraint(ConstraintAdv adv, Peer appUser) {
        this.adv = adv;
        this.appUser = appUser;
    }
    
    /** Getter for property adv.
     * @return Value of property adv.
     *
     */
    public dpm.content.advertisement.designEntity.related.ConstraintAdv getAdv() {
        return adv;
    }  
   
    /** Getter for property adv.
     * @return Value of property adv.
     *
     */
//    public net.jxta.document.Advertisement getAdv() {
//        return (net.jxta.document.Advertisement)adv;
//    }
    
}
