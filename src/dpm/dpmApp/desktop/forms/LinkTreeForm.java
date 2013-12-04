/*
 * LinkTreeForm.java
 *
 * Created on August 3, 2004, 11:39 AM
 */

package dpm.dpmApp.desktop.forms;

import dpm.container.tree.DpmTreeCellRenderer;
import dpm.container.tree.LinkTree;
import dpm.container.tree.LinkTreeNode;
import dpm.content.DesignEntity;
import dpm.content.DisplayUserObject;
import dpm.content.constraint.Link;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import net.jxta.peergroup.PeerGroup;
import dpm.container.tree.*;


/**
 *
 * @author  cumming
 */
public class LinkTreeForm extends JFrame implements DpmTerms {
    private DpmAppTopFrame topFrame;
    private DesignEntity rootEntity;
    private LinkTreeNode rootNode;
    private Peer appUser;
    protected JTree tree;
    protected DefaultMutableTreeNode latestSelNode;
    
    
    /** Creates new form LinkTreeForm */
    public LinkTreeForm(DpmAppTopFrame topFrame, DesignEntity rootEntity, PeerGroup basePG) {
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
        this.rootEntity = rootEntity;
        
        initComponents();
        scrollPane.setPreferredSize(new Dimension(PAGE_WIDTH, PAGE_HEIGHT));
        /**Note: rootNode has no tree yet (normally required) */
        this.rootNode = new LinkTreeNode(rootEntity, null, appUser);
        if(rootNode == null) {
            return;
        }
        /**refreshTree() sets this.tree */
        refreshTree(rootNode, DO_BEFORE, true); //true = incoming selected
        /**Sets the available linkTypes (e.g. 'doBefore') in a comboBox */
        setLinkTypeComboModel();
        /**Sets the available directions (e.g. 'incoming/outgoing') in a comboBox */
        setDirectionComboModel();
        setPosition(getWidth(), topFrame.getHeight(), this);
        pack();
        show();
    }
    
    /** Sets position to the left of the topFrame, aligned with its top */
    public void setPosition(int width, int height, JFrame frame) {
        //Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(0, (screenSize.height - height) / 2);
        //frame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
    }
    
    public void setDirectionComboModel() {
        /** Gets all the appropriate role names */
        Set directions = new HashSet();
        directions.add(INCOMING);
        directions.add(OUTGOING);
        directionComboBox.setModel(new DefaultComboBoxModel(new Vector(directions)));
        directionComboBox.setSelectedItem((String)INCOMING);
    }
    
    public void setLinkTypeComboModel() {
        /** Gets all the appropriate role names */
        linkTypeComboBox.setModel(new DefaultComboBoxModel(new Vector(getLinkTypeNames())));
        linkTypeComboBox.setSelectedItem((String)DO_BEFORE);
    }
    
    /**Get all constraint (link) names, including doBefores */
    public Set getLinkTypeNames() {
        Set constraintNames = new HashSet();
        /**First add basic constraint names */
        constraintNames.addAll(getBasicConstraintNames());
        
        /**Add all existing constraint names */
        Set allLinks = topFrame.getAppUser().getEntityRelatives().getAllLinks().collapseAll();
        for(Iterator i = allLinks.iterator(); i.hasNext(); ) {
            Link link = (Link)i.next();
            String conName = link.getLinkAdv().getConstraintName();
            constraintNames.add(conName);
        }
        return constraintNames;
    }
    
    public final static Set getBasicConstraintNames() {
        Set set = new HashSet();
        set.add(DO_BEFORE);
        return set;
    }
    
    /**Tree is set at construction, and can be changed due to actions by user */
    public void refreshTree(LinkTreeNode rootN, String linkType, boolean incoming) {
        /**Set the title to show root entity and incoming/outgoing label */
        setFormTitle();
        this.tree = new LinkTree(rootN, new DefaultTreeModel(rootN), topFrame, incoming, this);
        if(tree == null) {
            topFrame.printMessage("ERROR: Tree is null in LinkTreeForm constructor");
            return;
        }
        else {
            fixNullTree(tree, rootN);
        }
        tree.setDoubleBuffered(true);
        TreeSelectionModel selModel = new DefaultTreeSelectionModel();
        selModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //tree.setFocusable(true);
        tree.setEditable(false);
        tree.setCellRenderer(new DpmTreeCellRenderer());
        tree.setShowsRootHandles(true);
        /**'tree' replaces 'jtree' in initComponents... */
        scrollPane.setViewportView(tree);
        
        /** See: http://www.iam.ubc.ca/guides/javatut99/uiswing/components/tree.html */
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            /**Changes the nodes font when user clicks on node */
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode curSel = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
                if (curSel == null) {
                    return;
                }
                restorePreviousSelNode(curSel);
                DisplayUserObject label = (DisplayUserObject)curSel.getUserObject();
                if(curSel.getAllowsChildren()) {
                    label.setFont(PG_NODE_SELECTED);
                }
                else {
                    label.setFont(PG_LEAF_SELECTED);
                }
                setLatestSelNode(curSel);
            }
        });
        /**Finally, add children below the rootN */
        LinkTree t = (LinkTree)tree;
        /**Add tree, filtered by linkType (=constraintName) */
        t.addChildren(rootN, (String)linkTypeComboBox.getSelectedItem());
    }
    
    public void setFormTitle() {
        String prefix;
        String suffix;
        boolean incoming = incomingIsSelected();
        if (incoming == true) {
            prefix = "Links INTO: ";
            suffix = " (links point up TOWARDS root)";
        }
        else {
            prefix = "Links OUT FROM: ";
            suffix = " (links point down FROM root)";
        }
        /**Set the title to the JFrame */
        setTitle(prefix + rootEntity.getFullName() + suffix);
    }
    
    /**Refreshes the tree according to whether a node has been selected,
     * according to incoming/outgoing, and according to selected linkType */
    public void refreshTreeAccordingToSelNode() {
        LinkTreeNode selNode = getSelectedLinkTreeNode();
        String selType = (String)linkTypeComboBox.getSelectedItem();
        boolean incoming = incomingIsSelected();
        
        if (selType == null) {
            System.out.println("ERROR: no selType in LinkTreeForm.refreshTreeAccordingToSelNode()");
            return;
        }
        if (selNode == null) {
            /**Use rootNode defined at construction */
            refreshTree(rootNode, selType, incoming);
        }
        else {
            /**Use the selNode from the LinkTreeForm */
            refreshTree(selNode, selType, incoming);
        }
    }
    
    /**Return a boolean 'true' if incoming is selected */
    public boolean incomingIsSelected() {
        String direction = (String)directionComboBox.getSelectedItem();
        if (direction == null) {
            return false;
        }
        if (direction.equals(INCOMING)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**Used if user selects node in this form */
    public LinkTreeNode getSelectedLinkTreeNode() {
        if(linkNodeSelected()) {
            TreeNode selNode = (TreeNode)tree.getSelectionPath().getLastPathComponent();
            return (LinkTreeNode)selNode;
        }
        return null;
    }
    
    //latest
    public boolean linkNodeSelected() {
        /**If any node is selected */
        if(tree.getSelectionPath() == null) {
            return false;
        }
        TreeNode selNode = (TreeNode)tree.getSelectionPath().getLastPathComponent();
        return (selNode != null && selNode instanceof LinkTreeNode);
    }
    
    /**this is only required for the rootNode node (which is constructed before a tree is constructed) */
    public void fixNullTree(JTree tree, TreeNode rootNode) {
        LinkTreeNode r = (LinkTreeNode)rootNode;
        r.setTree((LinkTree)tree);
    }
    
    public void restorePreviousSelNode(DefaultMutableTreeNode curSel) {
        DefaultMutableTreeNode oldSel = getLatestSelNode();
        //if this is the first selected node--do nothing
        if (oldSel == null) {
            //printMessage("Old select was null");
            return;
        }
        if(oldSel == curSel) {
            //printMessage("New select same as old select");
            return;
        }       
        DisplayUserObject oldLabel = (DisplayUserObject)oldSel.getUserObject();
        //if a peergroup node
        if(oldSel.getAllowsChildren()) {
            oldLabel.setFont(PG_NODE_UNSELECTED);
        }
        else {
            oldLabel.setFont(PG_LEAF_UNSELECTED);
        }
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
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        scrollPane = new javax.swing.JScrollPane();
        jtree = new javax.swing.JTree();
        buttonsPanel = new javax.swing.JPanel();
        selectNewRootButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        comboBoxPanel = new javax.swing.JPanel();
        directionLabel = new javax.swing.JLabel();
        linkTypeComboBox = new javax.swing.JComboBox();
        directionComboBox = new javax.swing.JComboBox();
        linkTypeLabel = new javax.swing.JLabel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        scrollPane.setFont(new java.awt.Font("Arial", 0, 12));
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 400));
        scrollPane.setViewportView(jtree);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(scrollPane, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        selectNewRootButton.setFont(new java.awt.Font("Arial", 1, 12));
        selectNewRootButton.setText("Select New Root");
        selectNewRootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNewRootButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        buttonsPanel.add(selectNewRootButton, gridBagConstraints);

        closeButton.setFont(new java.awt.Font("Arial", 1, 12));
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        buttonsPanel.add(closeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        getContentPane().add(buttonsPanel, gridBagConstraints);

        comboBoxPanel.setLayout(new java.awt.GridBagLayout());

        directionLabel.setFont(new java.awt.Font("Arial", 0, 12));
        directionLabel.setText("Link direction:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        comboBoxPanel.add(directionLabel, gridBagConstraints);

        linkTypeComboBox.setFont(new java.awt.Font("Arial", 0, 12));
        linkTypeComboBox.setPreferredSize(new java.awt.Dimension(85, 20));
        linkTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkTypeComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        comboBoxPanel.add(linkTypeComboBox, gridBagConstraints);

        directionComboBox.setFont(new java.awt.Font("Arial", 0, 12));
        directionComboBox.setPreferredSize(new java.awt.Dimension(85, 20));
        directionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directionComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        comboBoxPanel.add(directionComboBox, gridBagConstraints);

        linkTypeLabel.setFont(new java.awt.Font("Arial", 0, 12));
        linkTypeLabel.setText("Link name:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        comboBoxPanel.add(linkTypeLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        getContentPane().add(comboBoxPanel, gridBagConstraints);

        pack();
    }//GEN-END:initComponents
    
    private void linkTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkTypeComboBoxActionPerformed
        // Add your handling code here:
        refreshTreeAccordingToSelNode();
    }//GEN-LAST:event_linkTypeComboBoxActionPerformed
    
    private void directionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directionComboBoxActionPerformed
        // Add your handling code here:
        refreshTreeAccordingToSelNode();
    }//GEN-LAST:event_directionComboBoxActionPerformed
    
    private void selectNewRootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNewRootButtonActionPerformed
        // Add your handling code here:
        /**Rebuild the tree according to the selected node */
        LinkTreeNode curSelNode = getSelectedLinkTreeNode();
        if(curSelNode == null) {
            topFrame.showErrorDialog("Please select a tree node", this);
            return;
        }
        rootEntity = curSelNode.getDesignEntity();
        //DesignEntity newRootEntity = (DesignEntity)curSel.getUserObject();
        rootNode = new LinkTreeNode(rootEntity, null, appUser);
        setFormTitle();
        refreshTreeAccordingToSelNode();
    }//GEN-LAST:event_selectNewRootButtonActionPerformed
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // Add your handling code here:
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        //System.exit(0);
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    /** Getter for property latestSelNode.
     * @return Value of property latestSelNode.
     *
     */
    public DefaultMutableTreeNode getLatestSelNode() {
        return latestSelNode;
    }
    
    /**
     * @param args the command line arguments
     */
    //    public static void main(String args[]) {
    //        new LinkTreeForm().show();
    //    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel comboBoxPanel;
    private javax.swing.JComboBox directionComboBox;
    private javax.swing.JLabel directionLabel;
    private javax.swing.JTree jtree;
    private javax.swing.JComboBox linkTypeComboBox;
    private javax.swing.JLabel linkTypeLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JButton selectNewRootButton;
    // End of variables declaration//GEN-END:variables
    
}
