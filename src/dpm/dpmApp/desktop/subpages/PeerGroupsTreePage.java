/*
 * PeerGroupsTreePage.java
 *
 * Created on November 4, 2003, 2:32 PM
 */

package dpm.dpmApp.desktop.subpages;

import dpm.container.tree.DpmTreeCellRenderer;
import dpm.container.tree.PGTree;
import dpm.container.tree.PGTreeLeaf;
import dpm.container.tree.PGTreeNode;
import dpm.content.ContentSearcherTree;
import dpm.content.DesignEntity;
import dpm.content.DisplayUserObject;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmPage;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerGroupAdvertisement;



/** Main display page for hierarchical peergroups.
 * @author cumming
 * @since November 4, 2003, 2:32 PM
 */
public class PeerGroupsTreePage extends DpmPage { //implements { //DiscoveryListener, Runnable, ActionListener, ItemListener,  {
    protected JTree tree;
    protected String staticShow = null;
    private PGTreeNode root;
    protected DefaultMutableTreeNode latestSelNode;
    protected int width;
    protected int height;
    
    /** Creates a new PeerGroupsTreePage */
    public PeerGroupsTreePage
    (String name, DpmAppTopFrame topFrame, PeerGroup basePG, String staticShow, int width, int height) {
        super(name, DiscoveryService.GROUP, topFrame, basePG);
        this.staticShow = staticShow;
        this.width = width;
        this.height = height;
        initComponents();
        scrollPane.setPreferredSize(new Dimension(width, height));
        setRootAndTree();
    }
    
    /**Creates the root PGTreeNode and adds the tree */
    public void setRootAndTree() {
        //note: root has no tree yet (normally required)
        this.root = getNewTreeRoot(basePG);
        //TreeNode root = getNewTreeRoot(basePG);
        if(root == null) {
            System.out.println("ERROR: Root is null in PGTP constructor");
            return;
        }
        this.tree = new PGTree(root, new DefaultTreeModel(root), topFrame, staticShow);
        if(tree == null) {
            System.out.println("ERROR: Tree is null in PGTP constructor");
            return;
        }
        else {
            fixNullTree((PGTree)tree, root);
            setBasePGField();
            setSearcherStatusField();
        }
        TreeSelectionModel selModel = new DefaultTreeSelectionModel();
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setEditable(false);
        tree.setCellRenderer(new DpmTreeCellRenderer());
        tree.setShowsRootHandles(true);
        scrollPane.setViewportView(tree);
        
        
        //http://www.iam.ubc.ca/guides/javatut99/uiswing/components/tree.html
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode curSel = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                if(curSel == null) {
                    return;
                }
                DefaultMutableTreeNode oldSel = getLatestSelNode();
                restorePreviousSelNode(oldSel, curSel);
                
                DisplayUserObject label = (DisplayUserObject)curSel.getUserObject();
                if(curSel.getAllowsChildren()) {
                    label.setFont(PG_NODE_UNSELECTED);
                    label.setForeground(Color.BLUE);
                }
                else {
                    //label.setFont(PG_LEAF_SELECTED);
                    /**just make label blue when selected, @since Dec 23, 2004 */
                    label.setForeground(Color.BLUE);
                }
                /**Record what has just been selected */
                setLatestSelNode(curSel);
            }
        });
        /**See instead: PGTree.treeNodeClicked() */
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeExpanded(TreeExpansionEvent e) {
                //System.out.println("tree expanded");
                //if(TURN_SEARCHER_ON_AT_TREE_EXPANSION) {
                //                DefaultMutableTreeNode curSel = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                //                if(curSel != null && curSel instanceof PGTreeNode) {
                //                    System.out.println("tree expanded");
                //                    //                        PGTreeNode cur = (PGTreeNode)curSel;
                //                    //                        cur.startRemoteSearching();
                //                }
            }
            public void treeCollapsed(TreeExpansionEvent e) {
                //System.out.println("tree collapsed");
                //if(TURN_SEARCHER_OFF_AT_TREE_COLLAPSE) {
                //                DefaultMutableTreeNode curSel = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                //                if(curSel != null && curSel instanceof PGTreeNode) {
                //                    //System.out.println("tree collapsed");
                //                    //                        PGTreeNode cur = (PGTreeNode)curSel;
                //                    //                        cur.stopRemoteSearching();
                //                }
            }
        });
        /**Finally, add children below the root */
        root.addChildrenAndExpand();
    }
    
    public void restorePreviousSelNode(DefaultMutableTreeNode oldSel, DefaultMutableTreeNode newSel) {
        /**if this is the first selected node--do nothing */
        if (oldSel == null) {
            return;
        }
        if(oldSel.equals(newSel)) {
            return;
        }
        DisplayUserObject oldLabel = (DisplayUserObject)oldSel.getUserObject();
        /**if a peergroup node */
        if(oldSel.getAllowsChildren()) {
            oldLabel.setFont(PG_NODE_UNSELECTED);
            oldLabel.setForeground(Color.BLACK);
        }
        else {
            oldLabel.setFont(PG_LEAF_UNSELECTED);
            /**@since Dec 23, 2004 */
            oldLabel.setForeground(Color.BLACK);
        }
    }
    
    /**Allows users to move up peergroup tree one level at a time
     * @since 30 Sept 2004 */
    public void reviseRootNodeUp() {
        //System.out.println("BasePG before revision: " + this.basePG.getPeerGroupNameX());
        PeerGroup existingPG = this.basePG;
        PeerGroup parentPG = topFrame.getAppUser().getCsPG().getParentPG(existingPG);
        if(parentPG == null) {
            topFrame.showErrorDialog("Could not go to parent peergroup", this);
            return;
        }
        if(existingPG.equals(parentPG)) {
            topFrame.showErrorDialog("ParentPG is same as existing", this);
            return;
        }
        this.basePG = parentPG;
        //System.out.println("BasePG after revision: " + basePG.getPeerGroupNameX());
        setRootAndTree();
    }
    
    /**Not used currently (Dec 2004) */
    public void run() {
        //continuously makes new PGs from pgAdvs--if necessary
        printMessage(name + " thread now running");
        //
        while(true) { //loop forever
            try {
                Thread.sleep(WAIT_TIME);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public PGTreeNode getNewTreeRoot(PeerGroup pg) {
        //set active to true otherwise csAdvs don't get populated:
        return (new PGTreeNode(pg, null, null, topFrame)); //false: ContentSearcherTree = not active
    }
    
    //this is only required for the root node (which is constructed before a tree is constructed)
    public void fixNullTree(JTree tree, TreeNode root) {
        PGTreeNode r = (PGTreeNode)root;
        ContentSearcherTree rootSearcher = r.getTreeSearcher();
        rootSearcher.setTree((PGTree)tree);
        r.setTree((PGTree)tree);
        //rootSearcher.setActive(true);
    }
    
    //current:
    //assumes: one pgAdv is selected
    public void addToTreeNewPG_UsingAdv() {
        //PGTree tree = (PGTree)getTree();
        PeerGroupAdvertisement selPgAdv = getSelectedPGAdv();
        PGTreeLeaf leaf = getSelectedPGTreeLeaf();
        PGTreeNode parentNode = getParentNodeOfSelectedLeaf(leaf);
        PeerGroup parentPG = getParentPGOfSelectedLeaf(leaf);
        
        if(selPgAdv!= null && parentPG!=null) {
            PeerGroup newpg = topFrame.getPgUtilities().newPG_UsingAdv(selPgAdv, parentPG);
            /**redraw the tree at that level */
            parentNode.addChildrenAndExpand();
        }
    }
    
    //assumes: one PGTreeNode is selected
    public void addToTreeNewPG_NotUsingAdv(String pgName, String pgDesc, PeerGroup parentPG) {
        PGTreeNode parentNode = getSelectedPGTreeNode();
        PeerGroup newpg = topFrame.getPgUtilities().newPG_NotUsingAdv(pgName, pgDesc, null, parentPG);
        /**Note that new peergroup is added automatically to tree */
        /**Redraw the tree */
        parentNode.addChildrenAndExpand();
    }
    
    public void setBasePGField() {
        basePGField.setText(" " + basePG.getPeerGroupName());
    }
    
    public void setSearcherStatusField() {
        PGTree t = (PGTree)getTree();
        PGTreeNode activeN = (PGTreeNode)t.getActiveNode();
        searcherStatusField.setText(" " + activeN.getTreeSearcher().getStatusDescription());
    }
    
    public void setModel(TreeModel model) {
        tree.setModel(model);
    }
    
    public void printSelectedTreeNode() {
        //printMessage(getSelectedPGAdv().getName());
        TreePath tp = tree.getSelectionPath(); //from root to the selected node
        DefaultMutableTreeNode selNode = (DefaultMutableTreeNode)tp.getLastPathComponent();
        printMessage(selNode.getUserObject().toString());
    }
    
    public String getParentNodeName(PGTreeNode node) {
        if(node.isRoot()) {
            return node.getPeerGroup().getPeerGroupName();
        }
        else {
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
            return (String)parent.getUserObject().toString();
        }
    }
    
    //latest
    public boolean someNodeSelected() {
        return tree.getSelectionPath() != null;
    }
    
    //latest
    public boolean pgNodeSelected() {
        if(!someNodeSelected()) {
            return false;
        }
        TreeNode selNode = (TreeNode)tree.getSelectionPath().getLastPathComponent();
        return (selNode!=null && selNode instanceof PGTreeNode);
    }
    
    //latest
    public boolean pgLeafSelected() {
        if(!someNodeSelected()) {
            return false;
        }
        TreeNode selNode = (TreeNode)tree.getSelectionPath().getLastPathComponent();
        return (selNode!=null && selNode instanceof PGTreeLeaf);
    }
    
    //latest
    public PGTreeNode getSelectedPGTreeNode() {
        if(pgNodeSelected()) {
            TreeNode selNode = (TreeNode)tree.getSelectionPath().getLastPathComponent();
            return (PGTreeNode)selNode;
        }
        return null;
    }
    
    //latest
    public PGTreeLeaf getSelectedPGTreeLeaf() {
        if(pgLeafSelected()) {
            TreeNode selNode = (TreeNode)tree.getSelectionPath().getLastPathComponent();
            return (PGTreeLeaf)selNode;
        }
        return null;
    }
    
    public PGTreeLeaf selectFirstLeaf() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.setSelectionRow(i);
            if(pgLeafSelected()) {
                return getSelectedPGTreeLeaf();
            }
        }
        return null;
    }
    
    public DesignEntity getFirstDesignEntity() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.setSelectionRow(i);
            if(selectedIsDesignEntity()) {
                return getSelectedDesignEntity();
            }
        }
        return null;
    }
    
    //current:
    public PeerGroupAdvertisement getSelectedPGAdv() {
        PGTreeLeaf leaf = getSelectedPGTreeLeaf();
        Advertisement leafAdv = leaf.getAdv();
        if(!(leafAdv instanceof PeerGroupAdvertisement)) {
            topFrame.showErrorDialog("Please select a PG Leaf", topFrame);
            return null;
        }
        return (PeerGroupAdvertisement)leafAdv;
    }
    
    public boolean selectedIsDesignEntity() {
        PGTreeLeaf leaf = getSelectedPGTreeLeaf();
        return (leaf != null) && (leaf.getDesignEntity() != null);
    }
    
    //current:
    public DesignEntity getSelectedDesignEntity() {
        DesignEntity entity = null;
        PGTreeLeaf leaf = getSelectedPGTreeLeaf();
        if(leaf != null) {
            entity = leaf.getDesignEntity();
            if(entity == null) {
                return null;
            }
        }
        return entity;
    }
    
    public LoopNetAdvertisement getSelectedLoopNetAdv() {
        PGTreeLeaf leaf = getSelectedPGTreeLeaf();
        LoopNetAdvertisement loopAdv = leaf.getLoopNetAdv();
        if(loopAdv == null) {
            return null;
        }
        return loopAdv;
    }
    
    //current:
    public PGTreeNode getParentNodeOfSelectedLeaf(PGTreeLeaf leaf) {
        return (PGTreeNode)leaf.getParent();
    }
    
    //current: getSelectedPGAdvParentPG
    public PeerGroup getParentPGOfSelectedLeaf(PGTreeLeaf leaf) {
        return getParentNodeOfSelectedLeaf(leaf).getPeerGroup();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JTree jtree;

        scrollPane = new javax.swing.JScrollPane();
        jtree = new javax.swing.JTree();
        buttonsPanel = new javax.swing.JPanel();
        basePGField = new javax.swing.JTextField();
        searcherStatusField = new javax.swing.JTextField();
        basePGLabel = new javax.swing.JLabel();
        searcherStatusLabel = new javax.swing.JLabel();
        reviseToParentButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setName("PeerGroupsTreePagePanel");
        scrollPane.setFont(new java.awt.Font("Arial", 0, 12));
        scrollPane.setMinimumSize(new java.awt.Dimension(400, 400));
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 400));
        jtree.setFont(new java.awt.Font("Arial", 0, 12));
        jtree.setDoubleBuffered(true);
        jtree.setShowsRootHandles(true);
        jtree.setAutoscrolls(true);
        scrollPane.setViewportView(jtree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(scrollPane, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setFont(new java.awt.Font("Arial", 0, 12));
        basePGField.setBackground(new java.awt.Color(204, 204, 204));
        basePGField.setFont(new java.awt.Font("Arial", 0, 11));
        basePGField.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        buttonsPanel.add(basePGField, gridBagConstraints);

        searcherStatusField.setBackground(new java.awt.Color(204, 204, 204));
        searcherStatusField.setEditable(false);
        searcherStatusField.setFont(new java.awt.Font("Arial", 0, 11));
        searcherStatusField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        searcherStatusField.setBorder(null);
        searcherStatusField.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        buttonsPanel.add(searcherStatusField, gridBagConstraints);

        basePGLabel.setFont(new java.awt.Font("Arial", 1, 11));
        basePGLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        basePGLabel.setText("BasePG");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        buttonsPanel.add(basePGLabel, gridBagConstraints);

        searcherStatusLabel.setFont(new java.awt.Font("Arial", 1, 11));
        searcherStatusLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        searcherStatusLabel.setText("Searcher Status");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        buttonsPanel.add(searcherStatusLabel, gridBagConstraints);

        reviseToParentButton.setFont(new java.awt.Font("Arial", 1, 11));
        reviseToParentButton.setText("^ Up");
        reviseToParentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reviseToParentButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        buttonsPanel.add(reviseToParentButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(buttonsPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    private void reviseToParentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reviseToParentButtonActionPerformed
        // Add your handling code here:
        reviseRootNodeUp();
    }//GEN-LAST:event_reviseToParentButtonActionPerformed
    
    
    public void printMessage(String s) {
        if(topFrame == null) {
            System.out.println("ERROR: null topFrame in PGTreePage: printMessage");
        }
        else {
            topFrame.printMessage(s);
        }
    }
    
    public void actionPerformed(ActionEvent e) {
    }
    
    
    /** Getter for property latestSelNode.
     * @return Value of property latestSelNode.
     *
     */
    public DefaultMutableTreeNode getLatestSelNode() {
        return latestSelNode;
    }
    
    /** Setter for property latestSelNode.
     * @param latestSelNode New value of property latestSelNode.
     *
     */
    public void setLatestSelNode(DefaultMutableTreeNode latestSelNode) {
        this.latestSelNode = latestSelNode;
    }
    
    /** Getter for property tree.
     * @return Value of property tree.
     *
     */
    public JTree getTree() {
        return tree;
    }
    
    /** Setter for property tree.
     * @param tree New value of property tree.
     *
     */
    public void setTree(JTree tree) {
        this.tree = tree;
    }
    
    /** Getter for property root.
     * @return Value of property root.
     *
     */
    public PGTreeNode getRoot() {
        return root;
    }
    
    /** Setter for property root.
     * @param root New value of property root.
     *
     */
    public void setRoot(PGTreeNode root) {
        this.root = root;
    }
    
    //    public void itemStateChanged(ItemEvent event) {
    //        if (event.getSource().equals(contentDisplayList)) {
    //            int index = contentDisplayList.getSelectedIndex();
    //            PeerAdvertisement adv = null;
    //            if (index >= 0 && index < content.size()) {
    //                adv = (PeerAdvertisement)content.elementAt(index);
    //            }
    //            //if (adv != null) manager.selectedPeerChanged(adv);
    //        }
    //    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField basePGField;
    private javax.swing.JLabel basePGLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton reviseToParentButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextField searcherStatusField;
    private javax.swing.JLabel searcherStatusLabel;
    // End of variables declaration//GEN-END:variables
    
}
