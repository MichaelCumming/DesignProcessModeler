/*
 * DesignEntityTransitionPolicy.java
 *
 * Created on January 15, 2004, 10:47 AM
 */

package dpm.content.state;

import dpm.content.ContentStorage;
import dpm.peer.Peer;
import java.util.*;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.content.constraint.*;

/**
 * Sets the policy on what inputs are required before a task's state can be changed.
 * Each task can have a separate DesignEntityTransitionPolicy. Currently only 'StandardTransitionPolicys' are used.
 * @author  cumming
 * @since January 15, 2004, 10:47 AM
 */
public class DesignEntityInputPolicy {
    protected String entityType;
    protected LoopNetAdvertisement loopNetAdv;
    protected Peer peer;
    /** Content storage that describes all the requiredInputs required for a particular task */
    protected ContentStorage transRoles;
    
    /** Creates a new instance of DesignEntityStateChangePolicies
     * Shows all the required requiredInputs for all transition types */
    public DesignEntityInputPolicy(String entityType, LoopNetAdvertisement loopNetAdv, Peer peer) {
        this.entityType = entityType;
        this.loopNetAdv = loopNetAdv;
        this.peer = peer;
        this.transRoles = new ContentStorage("java.lang.String", peer);
    }
    
    /** Specifies input from a particular role is needed to enable a particular transition.
     * Inputs are represented as DesignEntityInputAdvertisements */
    public void addInput(String transName, String roleName) {
        if (loopNetAdv.transNameOK(transName)) {
            /** transNames are the 'rows' (= storageKey),
             * roleNames are the 'columns' in the content storage table */
            //System.out.println("Added input in DEIP. TransName: " + transName + " RoleName: " + roleName);
            transRoles.addString(transName, roleName);
        }
        else {
            System.out.println("ERROR: attempted to add wrong transName in DesignEntityInputPolicy");
        }
    }

    /** Getter for property loopNetAdv.
     * @return Value of property loopNetAdv.
     *
     */
    public LoopNetAdvertisement getLoopNetAdv() {
        return loopNetAdv;
    }
    
    /** Setter for property loopNetAdv.
     * @param loopNetAdv New value of property loopNetAdv.
     *
     */
    public void setLoopNetAdv(LoopNetAdvertisement loopNetAdv) {
        this.loopNetAdv = loopNetAdv;
    }
    
    /** Getter for property transRoles.
     * @return Value of property transRoles.
     *
     */
    public dpm.content.ContentStorage getTransRoles() {
        return transRoles;
    }    
    
    /** Setter for property transRoles.
     * @param transRoles New value of property transRoles.
     *
     */
    public void setTransRoles(dpm.content.ContentStorage transRoles) {
        this.transRoles = transRoles;
    }
    
}
