/*
 * LinkTree.java
 *
 * Created on July 29, 2004
 */

package dpm.container.tree;

import dpm.content.ContentStorage;
import dpm.content.DesignEntity;
import dpm.content.advertisement.AdvUtilities;
import dpm.content.constraint.Link;
import dpm.content.designEntity.*;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.*;
import net.jxta.peergroup.PeerGroup;
import dpm.content.constraint.*;
import dpm.dpmApp.desktop.forms.*;
import dpm.peer.*;
import javax.swing.*;



/**
 * @author  cumming
 * @since July 29, 2004
 */
public class LinkTree extends JTree implements DpmTerms {
    protected TreeNode rootNode;
    protected DpmAppTopFrame topFrame;
    protected Peer appUser;
    protected LinkTreeForm form;
    private final boolean incoming;
    protected TreeModel model;
    private ContentStorage entities;
    private AdvUtilities advUtils;
    
    
    /** Creates a new instance of PGTree */
    public LinkTree(TreeNode rootNode, TreeModel model, DpmAppTopFrame topFrame, boolean incoming, LinkTreeForm form) { //, TreeNode root, DpmAppTopFrame topFrame) {
        super(model); //without this the tree doesn't display anything
        this.model = model;
        this.rootNode = rootNode;
        this.incoming = incoming;
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
        this.form = form;
        this.entities = topFrame.getAppUser().getUserNamedEntities();
        this.advUtils = topFrame.getAdvUtils();
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                /**Control whether double-clicking does anything here: */
                treeNodeClicked(evt);
            }
        });
        topFrame.toolTipRegister(this);        
    }
    
    /**Specifies what happens when user double-clicks on a node */
    private void treeNodeClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            if(selPath.getLastPathComponent() != null) {
                DefaultMutableTreeNode selNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
                //printMessage("double-click");
                if (selNode != null && selNode instanceof LinkTreeNode) {
                    addChildren((LinkTreeNode)selNode, null);
                }
            }
        }
    }
    
    /** Adds all linked children (which are always design entities).
     * User must double-click each time */
    public void addChildren(LinkTreeNode rootN, String linkType) { //if showing incoming, then incoming==true
        /**The rootN has already been added to the tree */
        /**Get latest children. Note: that 'incoming' variable used */
        if(rootN == null) {
            return;
        }
        clearChildren(rootN);
        //PeerGroup basePG = null;
        //rootN.getTreeSearcher().localCache_To_csAdvAllTypes(basePG);
        Set children = getLinkedChildren(rootN, this.incoming, linkType);
        /**For each entity linked to parent */
        if(children != null && !children.isEmpty()) {
            for(Iterator i = children.iterator(); i.hasNext(); ) {
                Link link = (Link)i.next();
                
                if(!link.isDeleted(appUser)) {
                    /**Get the linked entity described in the link */
                    DesignEntity linkedChild =  getLinkedEntity(link, incoming);
                    if(linkedChild != null && linkedChild != rootN.getDesignEntity()) {
                        /**Here design entity is displayed as the link */
                        addOneLinkChildNode(rootN, linkedChild);
                    }
                }
                else {
                    String sourceName = link.getSourceFullName();
                    String targetName = link.getTargetFullName();
                    System.out.println("GOOD! Prevented a link from being displayed. " +
                    "LinkSource: " + sourceName + " LinkTarget: " + targetName);
                }
            }
        }
        TreePath path = new TreePath(rootN);
        this.expandPath(path);
        this.scrollPathToVisible(path);
    }
    
    private Set getLinkedChildren(LinkTreeNode rootN, boolean incoming, String linkType) {
        if(rootN == null) {
            return null;
        }
        return rootN.getDesignEntity().getLinksByConstraintName(linkType, incoming);
    }
    
    /** NOTE: private--only this class actually adds LinkTreeNodes, other than root of tree */
    private void addOneLinkChildNode(LinkTreeNode parentNode, DesignEntity childToAdd) {
        if(parentNode == null) {
            //printMessage("Null root node in addOneLinkNode");
            return;
        }
        int atEnd = parentNode.getChildCount();
        //PeerGroup basePG = getPGofDesignEntity(childToAdd);
        LinkTreeNode newNode = new LinkTreeNode(childToAdd, this, appUser);//, basePG);
        parentNode.insert(newNode, atEnd);
        //return newNode;
    }
    
    /**Appropriate linked entity depends on the target or source ID found in the link */
    public DesignEntity getLinkedEntity(Link link, boolean incoming) {
        if(incoming == true) {
            return getLinkedEntity2(link.getSourceID().toString());
        }
        else {
            return getLinkedEntity2(link.getTargetID().toString());
        }
    }
    
    /**Gets a designEntity */
    public DesignEntity getLinkedEntity2(String entityIDString) {
        return entities.getEntityByIDString(entityIDString);
    }
    
    public void clearChildren(LinkTreeNode selNode) {
        selNode.removeAllChildren();
        DefaultTreeModel defModel = (DefaultTreeModel)model;
        defModel.reload(selNode);
    }
    
    public void printMessage(String s) {
        if(topFrame == null) {
            printMessage("ERROR: null topFrame in LinkTree: printMessage");
        }
        else {
            topFrame.printMessage(s);
        }
    }
    
    /** Getter for property rootNode.
     * @return Value of property rootNode.
     *
     */
    public TreeNode getRootNode() {
        return rootNode;
    }
    
    /** Setter for property rootNode.
     * @param rootNode New value of property rootNode.
     *
     */
    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }
    
}
