/*
 * PGTree.java
 *
 * Created on December 12, 2003, 9:41 AM
 */

package dpm.container.tree;

import dpm.container.PGUtilities;
import dpm.content.ContentStorage;
import dpm.content.DisplayUserObject;
import dpm.content.advertisement.AdvUtilities;
import dpm.content.advertisement.IPGAdvertisement;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.*;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerGroupAdvertisement;
import dpm.content.advertisement.IPGMemberAdvertisement;
import dpm.content.advertisement.chat.ChatAdvertisement;
import dpm.content.advertisement.chat.PrivateChatAdvertisement;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.content.constraint.Link;
import dpm.content.designEntity.UserNamedEntity;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import dpm.content.*;



/**
 *
 * @author  cumming
 * @since December 12, 2003, 9:41 AM
 */
public class PGTree extends JTree implements DpmTerms {
    protected TreeNode rootNode;
    protected String staticShow = null;
    protected TreeNode activeNode; //the node that has a tree searcher working--only ONE, or NONE, works at a time
    protected DpmAppTopFrame topFrame;
    protected Peer appUser;
    protected TreeModel model;
    private AdvUtilities advUtils;
    private PGUtilities pgUtils;
    private ContentStorage peerGroups;
    private ContentStorage iPeerGroups;
    private ContentStorage entities;
    private ContentStorage loopNets;
    private ContentStorage memberPeers;
    private ContentStorage links;
    private ContentStorage privateMessages;
    private ContentStorage messages;
    
    
    /** Creates a new instance of PGTree */
    public PGTree(TreeNode rootNode, TreeModel model, DpmAppTopFrame topFrame, String staticShow) {
        super(model); //without this the tree doesn't display anything
        this.model = model;
        this.rootNode = rootNode;
        this.activeNode = rootNode;
        this.staticShow = staticShow;
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
        this.advUtils = topFrame.getAdvUtils();
        this.pgUtils = topFrame.getPgUtilities();
        
        /**Content Storage for adding data to tree */
        this.peerGroups = topFrame.getAppUser().getCsAdvPG();
        this.iPeerGroups = topFrame.getAppUser().getCsAdvIPG();
        this.entities = topFrame.getAppUser().getUserNamedEntities();
        this.loopNets = topFrame.getAppUser().getLoopNets();
        this.memberPeers = topFrame.getAppUser().getMembers();
        this.links = topFrame.getAppUser().getEntityRelatives().getAllLinks();
        this.privateMessages = topFrame.getAppUser().getPrivateChatAdvs();
        this.messages = topFrame.getAppUser().getChatAdvs();
        /**@since Dec 22, 2004 */
        this.setDoubleBuffered(true);
        
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                treeNodeClicked(evt);
            }
        });
        topFrame.toolTipRegister(this);
    }
    
    /**This is where all tree refresh code is now centralized */
    private void treeNodeClicked(MouseEvent e) {
        /**If double-clicked, add children */
        if (e.getClickCount() == 2) {
            TreePath selPath = getPathForLocation(e.getX(), e.getY());
            if(selPath != null) {
                if(selPath.getLastPathComponent() != null) {
                    PGTreeNode selNode = (PGTreeNode)selPath.getLastPathComponent();
                    if(selNode != null) {
                        addChildren(selNode);
                    }
                }
            }
        }
    }
    
    /**The base method for refreshing trees
     * revised: Dec 10, 2004 */
    public void addChildren(PGTreeNode node) {
        if(node != null) {  
            clearChildren(node);
            ContentSearcherTree searcher = node.getTreeSearcher();
            //PeerGroup basePG = ((DisplayUserObject)node.getUserObject()).getPeerGroup();
            /**First, populate content storage. This takes little time */
            searcher.localCache_To_csAdvAllTypes();            
            /**Add content to tree */           
            addAllContentInCSToTree(node);
            /**Expand the tree to show new addition */
            expandOneNode(node);
            
            /**NOTE: Now get the remote advs for next time (this takes time) */
            searcher.getRemoteAdvsAllTypes();
        }
    }
    
    /**Transfer all content from all content storages to a PGTree */
    private void addAllContentInCSToTree(PGTreeNode selNode) {
        String show;
        if(staticShow != null) {
            /**Some pages have static displays. e.g. NewLinkForm */
            show = staticShow;
        }
        else {
            /**For pages with user controllable displays. e.g. DpmAppTopFrame */
            show = topFrame.getShow();
        }
        /**Peergroups are shown in any case */
        addPGTypeAdvs(selNode, peerGroups, iPeerGroups);
        
        if(show.equals(ALL) || show.equals(ENTITIES)) {
            addEntities(selNode, entities);
        }
        if(show.equals(ALL) || show.equals(NETS)) {
            addNets(selNode, loopNets);
        }
        if(show.equals(ALL) || show.equals(PEERS)) {
            addMemberAdvs(selNode, memberPeers);
        }
        if(show.equals(ALL) || show.equals(LINKS)) {
            addLinks(selNode, links);
        }
        /**@since 24 Sept 2004 */
        if(show.equals(ALL) || show.equals(MESSAGES)) {
            addPrivateChatMessages(selNode, privateMessages);
            addChatMessages(selNode, messages);
        }
    }
    
    /** Can be either an advertisement or a DesignEntity */
    private void addOneLeaf(PGTreeNode rootNode, Object obj) {
        /** an ordinary leaf */
        DefaultMutableTreeNode leaf = new PGTreeLeaf(obj, topFrame);
        int atEnd = rootNode.getChildCount();
        rootNode.insert(leaf, atEnd);
    }
    
    /** NOTE: private. Only this class adds PGNodes */
    private void addOnePGNode(PGTreeNode parentNode, PeerGroupAdvertisement pgAdv) {
        if(parentNode == null) {
            printMessage("Null root node in addOnePGNode");
            return;
        }
        PeerGroup parentPG = parentNode.getPeerGroup();
        PGTreeNode newNode = null;
        PeerGroup pg = null;
        /** This checks if PG has already been instantiated: */
        PeerGroup existingPG = pgUtils.retrieveExistingPG(parentPG, pgAdv);
        if(existingPG != null){
            pg = existingPG;
        }
        else {
            pg = pgUtils.newPG_UsingAdv(pgAdv, parentPG);
        }
        int atEnd = parentNode.getChildCount();
        newNode = new PGTreeNode(pg, parentNode, this, topFrame);
        parentNode.insert(newNode, atEnd);
        //expandOneNode(parentNode);
    }
    
    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public void expandAll() {
        TreeNode root = (TreeNode)this.getModel().getRoot();
        /** Traverse tree from root */
        expandAll(new TreePath(root), true);
    }
    
    public void collapseAll() {
        TreeNode root = (TreeNode)this.getModel().getRoot();
        /** Traverse tree from root */
        expandAll(new TreePath(root), false);
    }
    
    public void expandOneNode(PGTreeNode node) {
        //this works:
        //System.out.println(">>node before: isExpanded() = " + node.isExpanded());
        expandPath(new TreePath(node.getPath()));
        //System.out.println(">>node after: isExpanded() = " + node.isExpanded());        
    }
    
    public void collapseOneNode(PGTreeNode node) {
        collapsePath(new TreePath(node.getPath()));
    }
    
    public void expandAll(TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(path, expand);
            }
        }
        /** Expansion or collapse must be done bottom-up */
        if(expand) {
            this.expandPath(parent);
        } else {
            this.collapsePath(parent);
        }
    }  
    
    private void addPGTypeAdvs(PGTreeNode rootNode, ContentStorage pgs, ContentStorage ipgs) {
        /**Adds instantiated peergroups and records those added */
        Set alreadyDrawn = addIPGs(rootNode, ipgs);
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(pgs.iterator(storageKey) != null) {
            for (Iterator i = pgs.iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof PeerGroupAdvertisement) {
                    PeerGroupAdvertisement pgAdv = (PeerGroupAdvertisement)obj;
                    if(!appUser.isDeleted(pgAdv)) {
                        /**This adds pgAdv, if they haven't been already drawn */
                        addOnePGAdvLeaf(rootNode, pgAdv, alreadyDrawn);
                    }
                    else {
                        System.out.println("NOTE: Prevented pg " + pgAdv.getName() + " from being displayed");
                    }
                }
            }
        }
    }
    
    /**Adds ipgs and returns those added */
    private Set addIPGs(PGTreeNode rootNode, ContentStorage ipgs) {
        Set alreadyDrawn = new HashSet();
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(ipgs.iterator(storageKey) != null) {
            for (Iterator i = ipgs.iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof IPGAdvertisement) {
                    IPGAdvertisement ipgAdv = (IPGAdvertisement)obj;
                    PeerGroupAdvertisement pgAdv = topFrame.getPgUtilities().ipgAdv_To_pgAdv(ipgAdv);
                    
                    if(!appUser.isDeleted(pgAdv)) {
                        addOnePGNode(rootNode, pgAdv);
                        /** if added a node: */
                        alreadyDrawn.add(pgAdv);
                    }
                    else {
                        System.out.println("NOTE: Prevented ipg " + ipgAdv.getName() + " from being displayed");
                    }
                }
            }
        }
        return alreadyDrawn;
    }
    
    /**@since 25 nov. 2004 */
    private void addMemberAdvs(PGTreeNode rootNode, ContentStorage cs) {
        /**Retreive the text of the PGNode = peergroup name */
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(cs.iterator(storageKey) != null) {
            for (Iterator i = cs.iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof IPGMemberAdvertisement) {
                    IPGMemberAdvertisement memberAdv = (IPGMemberAdvertisement)obj;
                    if(!memberAdv.isDeleted(appUser)) {
                        addOneLeaf(rootNode, memberAdv);
                    }
                }
            }
        }
    }
    
    private void addEntities(PGTreeNode rootNode, ContentStorage entities) {
        PeerGroup parentPG = rootNode.getPeerGroup();
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(entities.iterator(storageKey) != null) {
            for (Iterator i = entities.iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof UserNamedEntity) {
                    UserNamedEntity entity = (UserNamedEntity)obj;
                    /**Before display, check if task can change state */
                    advUtils.changeDesignEntityStateIfPossible(entity, parentPG);
                    if(!entity.isDeleted(appUser)) {
                        addOneLeaf(rootNode, entity);
                    }
                    else {
                        System.out.println("NOTE: Prevented entity " + entity.getFullName() + " from being displayed");
                    }
                }
            }
        }
    }
    
    private void addNets(PGTreeNode rootNode, ContentStorage nets) {
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(nets.iterator(storageKey) != null) {
            for (Iterator i = nets.iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof LoopNetAdvertisement) {
                    LoopNetAdvertisement netAdv = (LoopNetAdvertisement)obj;
                    
                    if(!netAdv.isDeleted(appUser)) {
                        addOneLeaf(rootNode, netAdv);
                    }
                    else {
                        System.out.println("NOTE: Prevented net " + netAdv.getNetName() + " from being displayed");
                    }
                }
            }
        }
    }
    
    private void addLinks(PGTreeNode rootNode, ContentStorage links) {
        //For Testing links show up in dpmNet: see PGTree
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(links.iterator(storageKey) != null) {
            for (Iterator i = links.iterator(storageKey); i.hasNext(); ) {
                Link link = (Link)i.next();
                
                if(!link.getLinkAdv().isDeleted(appUser)) {
                    addOneLeaf(rootNode, link.getLinkAdv());
                }
                else {
                    String sourceName = link.getSourceFullName();
                    String targetName = link.getTargetFullName();
                    System.out.println("NOTE: Prevented a link from being displayed. " +
                    "LinkSource: " + sourceName + " LinkTarget: " + targetName);
                }
            }
        }
    }
    
    private void addChatMessages(PGTreeNode rootNode, ContentStorage messages) {
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(messages.iterator(storageKey) != null) {
            for (Iterator i = messages.iterator(storageKey); i.hasNext(); ) {
                ChatAdvertisement message = (ChatAdvertisement)i.next();
                
                if(!message.isDeleted(appUser)) {
                    addOneLeaf(rootNode, message);
                }
                else {
                    String authorName = message.getAuthorName();
                    System.out.println("NOTE: Prevented a chat message from being displayed. " +
                    "Author name: " + authorName);
                }
            }
        }
    }
    
    private void addPrivateChatMessages(PGTreeNode rootNode, ContentStorage messages) {
        //String storageKey = (String)rootNode.getUserObject().toString();
        //STORE_PG
        String storageKey = rootNode.getStorageKeyFromPG_ID();
        
        if(messages.iterator(storageKey) != null) {
            for (Iterator i = messages.iterator(storageKey); i.hasNext(); ) {
                PrivateChatAdvertisement message = (PrivateChatAdvertisement)i.next();
                
                if(!message.isDeleted(appUser)) {
                    addOneLeaf(rootNode, message);
                }
                else {
                    String authorName = message.getAuthorName();
                    System.out.println("NOTE: Prevented a chat message from being displayed. " +
                    "Author name: " + authorName);
                }
            }
        }
    }
    
    /** Assumed NOT to be a previously instantiated pgAdv */
    private void addOnePGAdvLeaf(PGTreeNode rootNode, PeerGroupAdvertisement pgAdv, Set alreadyDrawn) {
        /** if already drawn, don't do anything else */
        if(!(alreadyDrawn(pgAdv, alreadyDrawn))) {
            /** draw as a pgAdv leaf */
            if(!appUser.isDeleted(pgAdv)) {
                addOneLeaf(rootNode, pgAdv);
            }
        }
    }
    
    public boolean alreadyDrawn(PeerGroupAdvertisement pgAdv, Set alreadyDrawn) {
        if(pgAdv == null || alreadyDrawn.isEmpty()) {
            return false;
        }
        String pgIDString = pgAdv.getPeerGroupID().toString();
        for (Iterator i = alreadyDrawn.iterator(); i.hasNext(); ) {
            PeerGroupAdvertisement cur = (PeerGroupAdvertisement)i.next();
            if (cur.getPeerGroupID().toString().equals(pgIDString)) {
                return true;
            }
        }
        return false;
    }
    
    public void clearChildren(PGTreeNode selNode) {
        selNode.removeAllChildren();
        DefaultTreeModel defModel = (DefaultTreeModel)model;
        defModel.reload(selNode);
    }
    
    /** NOTE @deprecated */
    //    public void constructOnePGTreeLevel(PGTreeNode rootNode, TreePath path, ContentStorage csPG) {
    //        if(rootNode != null) {
    //            //rootNode will always have a PG
    //            PeerGroup rootPG = ((DisplayUserObject)rootNode.getUserObject()).getPeerGroup();
    //            //String storageKey = (String)rootNode.getUserObject().toString();
    //            //STORE_PG
    //            String storageKey = rootNode.getStorageKeyFromPG_ID();
    //
    //            //printMessage("NOTE: rootName in dOTL: " + rootName);
    //            int childCount = -1; //position of child in each level
    //            String childName = null;
    //            DefaultMutableTreeNode newChildNode = null;
    //            //
    //            //page.getTree();
    //            //tree.scrollPathToVisible(new TreePath(rootNode.getPath()));
    //            //
    //            if (csPG == null) topFrame.showErrorDialog("ERROR: content storage is null in tree page. Node: " + storageKey, topFrame);
    //            //
    //            if(!csPG.isEmpty(storageKey)) {
    //                //get the storage list indexed by rootName
    //                for (Iterator i = csPG.iterator(storageKey); i.hasNext(); ) {
    //                    PeerGroup childPG  = (PeerGroup)i.next();
    //                    if(!childPG.getPeerGroupName().equals("NetPeerGroup")) {
    //                        //found an appropriate PG
    //                        childCount++;
    //                        //constructOneTreeNode(path, childPG, rootNode, childCount);
    //                    }
    //                }
    //                //now recurse on all children breadth-first
    //                //why BFE? because CS table is arranged making this easy:
    //                //with CS table, can easily do all children at the same time
    //                //work on all the children that were just added to the parentNode:
    //                //Enumeration e = rootNode.breadthFirstEnumeration(); //not for just one level
    //                Enumeration e = rootNode.children();
    //                //
    //                //                while (e.hasMoreElements()) {
    //                //                    DefaultMutableTreeNode newRootNode = (DefaultMutableTreeNode)e.nextElement();
    //                //                    path = path.pathByAddingChild(newRootNode);
    //                //                    constructOnePGTreeLevel(newRootNode, path, csPG);
    //                //                }
    //            }
    //        }
    //    }
    
    public void printMessage(String s) {
        if(topFrame == null) {
            System.out.println("ERROR: null topFrame in PGTree: printMessage");
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
    
    /** Getter for property activeNode.
     * @return Value of property activeNode.
     *
     */
    public TreeNode getActiveNode() {
        return activeNode;
    }
    
    /** Setter for property activeNode.
     * @param activeNode New value of property activeNode.
     *
     */
    public void setActiveNode(TreeNode activeNode) {
        this.activeNode = activeNode;
    }
    
}
