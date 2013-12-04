/*
 * PGTreeNode.java
 *
 * Created on December 12, 2003, 9:11 AM
 */

package dpm.container.tree;

import dpm.content.ContentSearcherTree;
import dpm.content.DisplayUserObject;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.peer.Peer;
import javax.swing.tree.DefaultMutableTreeNode;
import net.jxta.peergroup.PeerGroup;
import dpm.content.state.*;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.content.designEntity.*;
import java.awt.*;
import java.awt.event.*;
import dpm.dpmApp.desktop.*;
import java.util.*;
import javax.swing.tree.*;
import net.jxta.discovery.*;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.protocol.DiscoveryResponseMsg;



/**
 *
 * @author  cumming
 *@since July 29, 2004
 */
public class PGTreeNode extends DefaultMutableTreeNode implements DpmTerms {
    private PGTreeNode parentNode;
    private ContentSearcherTree treeSearcher;
    private PeerGroup peerGroup;
    private PGTree tree;
    private DpmAppTopFrame topFrame;
    private Peer appUser;
    
    
    /** Creates a new instance of PGTreeNode */
    //public PGTreeNode(PeerGroup pg, Peer appUser, DpmAppTopFrame topFrame, boolean remoteSearching) {
    public PGTreeNode(PeerGroup pg, PGTreeNode parentNode, PGTree tree, DpmAppTopFrame topFrame) {
        super(new DisplayUserObject(pg, topFrame.getAppUser()), true); //true = allows children
        this.parentNode = parentNode;
        this.peerGroup = pg;
        this.tree = tree;
        this.topFrame = topFrame;
        
        /** As tasks are displayed, they are examined to see if their state can be advanced */
        this.treeSearcher = new ContentSearcherTree(this, tree, topFrame, SEARCHER_ON_AT_NODE_CONSTRUCTION); //false = remoteSearching off
        /**populate the content storage for this node */
        treeSearcher.localCache_To_csAdvAllTypes();
    }
    
    /**@since Dec 20, 2004 */
    public boolean isExpanded() {
        TreeNode[] pathToRoot = getPath();
        TreePath path = new TreePath(pathToRoot);
        if(tree != null) {
            return tree.isExpanded(path);
        }
        //System.out.println("tree is null");
        return false;
    }
    
    /**Add children and expands path
     * @since Dec 21, 2004 */
    public void addChildrenAndExpand() {
        if(tree != null) {
            /**This also expands the node */
            tree.addChildren(this);
            tree.expandPath(new TreePath(getPath()));
        }
    }
    
    /**@since Dec 22, 2004 */
    public void checkIfNodeRefreshNeeded(Object obj) {
        if(obj == null) {
            return;
        }
        /**If node isExpanded so its contents are visible */
        if(isExpanded()) {
            if(obj instanceof UserNamedEntity) {
                UserNamedEntity entity = (UserNamedEntity)obj;
                refreshDueToNewEntity(entity);
            }
            if(obj instanceof HistoryAdvertisement) {
                HistoryAdvertisement historyAdv = (HistoryAdvertisement)obj;
                refreshDueToNewHistoryAdv(historyAdv);
            }
        }
    }
    
    /**Decides if node should be refreshed because of incoming data
     * @since Dec 21, 2004 */
    public void refreshDueToNewEntity(UserNamedEntity entity) {
        String entityIDString = entity.getDesignEntityID().toString();
        DesignEntity existingEntity = getExistingLeafEntityUsingID(entityIDString);
        
        /**If entity being checked is new, then refresh */
        if(existingEntity == null) {
            String message = "Refresh NEEDED: Discovered new entity: " + entity.getBaseName();
            topFrame.printMessage(message);
            System.out.println(message);
            addChildrenAndExpand();
        }
    }
    
    /**@since Dec 22, 2004 */
    public void refreshDueToNewHistoryAdv(HistoryAdvertisement historyAdv) {
        String entityIDString = historyAdv.getDesignEntityID().toString();
        DesignEntity foundEntity = getExistingLeafEntityUsingID(entityIDString);
        
        /**If historyAdv pertains to an existing entity child */
        if(foundEntity != null) {
            PGTreeLeaf leaf = getExistingEntityLeaf(entityIDString);
            
            if(leaf != null) {
                LoopNetAdvertisement loopNetAdv = foundEntity.getLoopNetAdv();
                /**If display state is obsolete, refresh */
                if(loopNetAdv.before(leaf.getDisplayState(), historyAdv.getState())) {
                    String message = "Refresh NEEDED: new historyAdv received for entity: " +
                    historyAdv.getBaseName();
                    topFrame.printMessage(message);
                    System.out.println(message);
                    addChildrenAndExpand();
                }
            }
        }
    }
    
    /**@since Dec 22, 2004 */
    public DesignEntity getExistingLeafEntityUsingID(String entityIDString) {
        PGTreeLeaf leaf = getExistingEntityLeaf(entityIDString);
        if(leaf != null){
            UserNamedEntity entity = (UserNamedEntity)leaf.getObj();
            return entity;
        }
        return null;
    }
    
    /**Retrieves a PGTreeLeaf that holds an entity with a specific ID
     * @since Dec 22, 2004 */
    public PGTreeLeaf getExistingEntityLeaf(String entityIDString) {
        if(getChildCount() > 0) {
            /**Look for an existing entity leaf */
            for(Enumeration e = children(); e.hasMoreElements(); ) {
                Object obj = e.nextElement();
                if(obj instanceof PGTreeLeaf) {
                    PGTreeLeaf leaf = (PGTreeLeaf)obj;
                    Object leafObj = leaf.getObj();
                    if(leafObj instanceof UserNamedEntity) {
                        UserNamedEntity curEntity = (UserNamedEntity)leafObj;
                        String curEntityIDString = curEntity.getDesignEntityID().toString();
                        if(curEntityIDString.equals(entityIDString)) {
                            return leaf;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**@since Dec 20, 2004 */
    public boolean isCollapsed() {
        TreeNode[] pathToRoot = getPath();
        TreePath path = new TreePath(pathToRoot);
        if(tree != null) {
            return tree.isCollapsed(path);
        }
        return false;
    }
    
    /**@since Dec 10, 2004 */
    public String getStorageKeyFromPG_ID() {
        return peerGroup.getPeerGroupID().toString();
    }
    
    /**@since 14 Oct. 2004 */
    public void stopRemoteSearching() {
        getContentSearcherTree().setRemoteSearching(false);
    }
    /**@since 14 Oct. 2004 */
    public void startRemoteSearching() {
        getContentSearcherTree().setRemoteSearching(true);
    }
    
    
    //OK
    public PeerGroup getParentPeerGroup() {
        if(this.isRoot()) {
            return peerGroup;
        }
        else {
            return parentNode.getPeerGroup();
        }
    }
    
    /** Getter for property treeSearcher.
     * @return Value of property treeSearcher.
     *
     */
    public ContentSearcherTree getContentSearcherTree() {
        return treeSearcher;
    }
    
    /** Setter for property treeSearcher.
     * @param treeSearcher New value of property treeSearcher.
     *
     */
    public void setContentSearcherTree(ContentSearcherTree treeSearcher) {
        this.treeSearcher = treeSearcher;
    }
    
    /** Getter for property peerGroup.
     * @return Value of property peerGroup.
     *
     */
    public PeerGroup getPeerGroup() {
        return peerGroup;
    }
    
    /** Setter for property peerGroup.
     * @param peerGroup New value of property peerGroup.
     *
     */
    public void setPeerGroup(PeerGroup peerGroup) {
        this.peerGroup = peerGroup;
    }
    
    /** Getter for property topFrame.
     * @return Value of property topFrame.
     *
     */
    public DpmAppTopFrame getTopFrame() {
        return topFrame;
    }
    
    /** Setter for property topFrame.
     * @param topFrame New value of property topFrame.
     *
     */
    public void setTopFrame(DpmAppTopFrame topFrame) {
        this.topFrame = topFrame;
    }
    
    /** Getter for property treeSearcher.
     * @return Value of property treeSearcher.
     *
     */
    public ContentSearcherTree getTreeSearcher() {
        return treeSearcher;
    }
    
    /** Setter for property treeSearcher.
     * @param treeSearcher New value of property treeSearcher.
     *
     */
    public void setTreeSearcher(ContentSearcherTree treeSearcher) {
        this.treeSearcher = treeSearcher;
    }
    
    /** Getter for property tree.
     * @return Value of property tree.
     *
     */
    public dpm.container.tree.PGTree getTree() {
        return tree;
    }
    
    /** Setter for property tree.
     * @param tree New value of property tree.
     *
     */
    public void setTree(dpm.container.tree.PGTree tree) {
        this.tree = tree;
    }
    
    public void removeAllChildren() {
        super.removeAllChildren();
    }
    
    /** Getter for property parentNode.
     * @return Value of property parentNode.
     *
     */
    public dpm.container.tree.PGTreeNode getParentNode() {
        return parentNode;
    }
    
    /** Setter for property parentNode.
     * @param parentNode New value of property parentNode.
     *
     */
    public void setParentNode(dpm.container.tree.PGTreeNode parentNode) {
        this.parentNode = parentNode;
    }
    
    
}
