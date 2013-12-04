/*
 * LinkTreeLeaf.java
 *
 * Created on July 29, 2004
 */

package dpm.container.tree;

import dpm.content.DisplayUserObject;
import javax.swing.tree.DefaultMutableTreeNode;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import dpm.peer.Peer;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.content.designEntity.*;

/**
 *
 * @author  cumming
 * @since July 29, 2004
 */

/**NOTE: not yet used; as of Aug 11, 2004.
 * All design entities shown in tree are LinkTreeNodes - which allow children beneath them. */
public class LinkTreeLeaf extends DefaultMutableTreeNode {
    private Object obj;
    private int jxtaType;
    
    
    /** Creates a new instance of PGTreeNode */
    public LinkTreeLeaf(Object obj, DpmAppTopFrame topFrame) {
        super(new DisplayUserObject(obj, topFrame.getAppUser()), false); //false = doesn't allow children
        //super("test", false); //doesn't allow children
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
