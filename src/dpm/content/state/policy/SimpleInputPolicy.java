/*
 * SimpleInputPolicy.java
 *
 * Created on January 15, 2004, 1:47 PM
 */

package dpm.content.state.policy;

import dpm.peer.Peer;
import java.util.Collection;
import java.util.*;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.dpmApp.desktop.*;
import dpm.content.constraint.*;
import dpm.content.state.policy.*;
import dpm.content.state.*;


/**
 * The most obvious transition policy. Peers can also add additional inputs during the history of a task
 * @author  cumming
 * @since January 15, 2004, 1:47 PM
 */
public class SimpleInputPolicy extends DesignEntityInputPolicy implements DpmTerms {
    
    /** Creates a new instance of StandardInputPolicy.
     * These are which roles are required to make input for a task to advance its state */
    public SimpleInputPolicy(String entityType, LoopNetAdvertisement loopNetAdv, Peer appUser) {
        super(entityType, loopNetAdv, appUser);
        TreeSet transitions = loopNetAdv.getTransitions();
        
        /**For all transitions, except the last one (eg. Retired) */
        for (int i=0; i < loopNetAdv.getNumberOfTransitions()-1; i++) {
            String curTrans = loopNetAdv.getValue(i, transitions);
            
            addInput(curTrans, getClient(entityType));
            addInput(curTrans, getPerformer(entityType));
        }
        /**Add no policies to the last (reuse) transition */
        String curTrans = loopNetAdv.getValue(loopNetAdv.getNumberOfTransitions()-1, transitions);
        addInput(curTrans, ENTITY_RECYCLER);
    }
    
    public String getClient(String entityType) {
        if (
        entityType.equals(TOI_COURSE) ||
        entityType.equals(TOI_ASSIGNMENT) ||
        entityType.equals(TOI_EXAM)) {
            return TOI_CLIENT; //e.g. "toi_admin"
        }
        return NON_TOI_CLIENT; //e.g. "client"
    }
    
    public String getPerformer(String entityType) {
        if (
        entityType.equals(TOI_COURSE) ||
        entityType.equals(TOI_ASSIGNMENT) ||
        entityType.equals(TOI_EXAM)) {
            return TOI_PERFORMER; //e.g. "student"
        }
        return NON_TOI_PERFORMER; //e.g. "performer"
    }
    
}
