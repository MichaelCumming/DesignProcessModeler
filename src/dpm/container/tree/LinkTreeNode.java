/*
 * LinkTreeNode.java
 * Created on July 29, 2004
 */

package dpm.container.tree;

import dpm.content.ContentSearcherTree;
import dpm.content.DisplayUserObject;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.peer.*;
import javax.swing.tree.DefaultMutableTreeNode;
import net.jxta.peergroup.PeerGroup;
import dpm.content.state.*;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import java.awt.*;
import java.awt.event.*;


/**
 * @author  cumming
 * @since July 29, 2004
 */
public class LinkTreeNode extends DefaultMutableTreeNode {
    private DesignEntity designEntity;
    private LinkTree tree;
    private LinkTreeNode parentNode;
    private Peer appUser;
    private ContentSearcherTree treeSearcher;
    
    
    /** Creates a new instance of LinkTreeNode */
    public LinkTreeNode(DesignEntity de, LinkTree tree, Peer appUser) {
        super(new DisplayUserObject(de, appUser), true); //true = allows children
        this.designEntity = de;
        this.tree = tree;
        this.appUser = appUser;
    }
    
     
    /** Getter for property tree.
     * @return Value of property tree.
     *
     */
    public dpm.container.tree.LinkTree getTree() {
        return tree;
    }
    
    /** Setter for property tree.
     * @param tree New value of property tree.
     *
     */
    public void setTree(dpm.container.tree.LinkTree tree) {
        this.tree = tree;
    }
    
    public void removeAllChildren() {
        super.removeAllChildren();
    }
    
    /** Getter for property designEntity.
     * @return Value of property designEntity.
     *
     */
    public dpm.content.DesignEntity getDesignEntity() {
        return designEntity;
    }
    
    /** Setter for property designEntity.
     * @param designEntity New value of property designEntity.
     *
     */
    public void setDesignEntity(dpm.content.DesignEntity designEntity) {
        this.designEntity = designEntity;
    }
    
}
