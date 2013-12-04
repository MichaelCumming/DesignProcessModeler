/*
 * PGTreeNode.java
 *
 * Created on December 12, 2003, 9:11 AM
 */

package dpm.container.tree;

import dpm.content.DesignEntity;
import dpm.content.DisplayUserObject;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;


/**
 *
 * @author  cumming
 */
//NEW from 12.Dec.2003
public class PGTreeLeaf extends DefaultMutableTreeNode {
    private Object obj;
    private int jxtaType;
    
    
    /** Creates a new instance of PGTreeNode */
    public PGTreeLeaf(Object obj, DpmAppTopFrame topFrame) {
        super(new DisplayUserObject(obj, topFrame.getAppUser()), false); //false = doesn't allow children
        this.obj = obj;
        this.jxtaType = determineJxtaType(obj);
    }   
    
    public int determineJxtaType(Object obj) {
        if(obj instanceof PeerAdvertisement) {
            return DiscoveryService.PEER;
        }
        if(obj instanceof PeerGroupAdvertisement) {
            return DiscoveryService.GROUP;
        }
        else {
            return DiscoveryService.ADV;
        }
    }
    
    /**The state as displayed by the node--
     * as opposed to the dynamically determined currentState
     * @since Dec 22, 2004 */
    public String getDisplayState() {
        DisplayUserObject duo = (DisplayUserObject)getUserObject();
        return duo.getDisplayState();
    }    
    
    /** Getter for property jxtaType.
     * @return Value of property jxtaType.
     *
     */
    public int getJxtaType() {
        return jxtaType;
    }
    
    /** Setter for property jxtaType.
     * @param jxtaType New value of property jxtaType.
     *
     */
    public void setJxtaType(int jxtaType) {
        this.jxtaType = jxtaType;
    }
    
    /** Getter for property obj.
     * @return Value of property obj.
     *
     */
    public java.lang.Object getObj() {
        return obj;
    }
    
    public Advertisement getAdv() {
        if(obj instanceof Advertisement) {
            return (Advertisement)obj;
        }
        return null;
    }
    
    public LoopNetAdvertisement getLoopNetAdv() {
        if(obj instanceof LoopNetAdvertisement) {
            return (LoopNetAdvertisement)obj;
        }
        return null;
    }
    
    public DesignEntity getDesignEntity() {
        if(obj instanceof DesignEntity) {
            return (DesignEntity)obj;
        }
        return null;
    }   
    
    /** Setter for property obj.
     * @param obj New value of property obj.
     *
     */
    public void setObj(java.lang.Object obj) {
        this.obj = obj;
    }
    
}
