/*
 * NewLinkForm.java
 *
 * Created on March 10, 2004, 2:23 PM
 */

package dpm.dpmApp.desktop.forms.links;

import dpm.content.DesignEntity;
import dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.forms.LinkForm;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import net.jxta.peergroup.PeerGroup;

/**
 * A form for creating the links needed for a entity-subentity relation
 * @author  cumming
 */
public class NewSubEntityParentChildForm extends LinkForm {
    
    /** Creates new form NewLinkForm */
    public NewSubEntityParentChildForm(PeerGroup parentPG, DpmAppTopFrame topFrame) {
        super(parentPG, topFrame);
        
        initComponents();
        this.sourcePage = addTreePage(sourcePanel, parentPG, ENTITIES);
        this.targetPage = addTreePage(targetPanel, parentPG, ENTITIES);
        this.sourceTree = sourcePage.getTree();
        this.targetTree = targetPage.getTree();
        
        super.setTitle("Sub-Entity Form");
        targetTransLabel.setText("Completion of Child enables");
        targetTransLabel1.setText("this transition of Parent");
        makeSubEntityButton.setText("Link Parent > Child");
        
        /**Only a source (=parent) tree listener is needed because
         * only the source tree transitions are relevant*/
        addSourceTreeListener();
        /**The transition of the parent that the subentity returns to */
        setReturnTransComboModel(sourcePage.getFirstDesignEntity());
        topFrame.setPosition(this.getWidth(), this.getHeight(), this);
        pack();
        show();
    }
    
    /**Refreshes the return trans comboBox when a source tree node is pressed */
    public void addSourceTreeListener() {
        sourceTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                setReturnTransComboModel(sourcePage.getSelectedDesignEntity());
            }
        });
    }
    
    public void setReturnTransComboModel(DesignEntity parentEntity) {
        /** Gets all the appropriate transition names */
        if(parentEntity != null) {
            Set allTrans = parentEntity.getLoopNetAdv().getTransitions();
            returnTransComboBox.setModel(new DefaultComboBoxModel(new Vector(allTrans)));
        }
        /**Else, nothing has been selected */
        else {
            Set emptySet = new HashSet();
            returnTransComboBox.setModel(new DefaultComboBoxModel(new Vector(emptySet)));
        }
        pack();
    }
    
    /**@return The transition which the subentity returns to */
    public String getSelectedReturnTransition() {
        String returnTrans = (String)returnTransComboBox.getSelectedItem();
        if(returnTrans == null) {
            topFrame.showErrorDialog("Please select an existing transition", this);
            return null;
        }
        return returnTrans;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        sourcePanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        targetTransLabel = new javax.swing.JLabel();
        targetTransLabel1 = new javax.swing.JLabel();
        returnTransComboBox = new javax.swing.JComboBox();
        cancelButton = new javax.swing.JButton();
        makeSubEntityButton = new javax.swing.JButton();
        targetPanel = new javax.swing.JPanel();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Sub-Entity Link Form");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        sourcePanel.setBorder(new javax.swing.border.TitledBorder(null, "Select Parent", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 12)));
        getContentPane().add(sourcePanel, new java.awt.GridBagConstraints());

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        targetTransLabel.setFont(new java.awt.Font("Arial", 1, 12));
        targetTransLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        targetTransLabel.setText("SubEntity returns to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        buttonsPanel.add(targetTransLabel, gridBagConstraints);

        targetTransLabel1.setFont(new java.awt.Font("Arial", 1, 12));
        targetTransLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        targetTransLabel1.setText("this transition of Parent");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        buttonsPanel.add(targetTransLabel1, gridBagConstraints);

        returnTransComboBox.setBackground(new java.awt.Color(255, 255, 255));
        returnTransComboBox.setFont(new java.awt.Font("Arial", 0, 12));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 10, 0);
        buttonsPanel.add(returnTransComboBox, gridBagConstraints);

        cancelButton.setFont(new java.awt.Font("Arial", 1, 12));
        cancelButton.setText("Cancel / Close Window");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonsPanel.add(cancelButton, gridBagConstraints);

        makeSubEntityButton.setFont(new java.awt.Font("Arial", 1, 12));
        makeSubEntityButton.setText("Link Parent > Child");
        makeSubEntityButton.setMaximumSize(new java.awt.Dimension(163, 25));
        makeSubEntityButton.setMinimumSize(new java.awt.Dimension(163, 25));
        makeSubEntityButton.setPreferredSize(new java.awt.Dimension(163, 25));
        makeSubEntityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeSubEntityButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        buttonsPanel.add(makeSubEntityButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(buttonsPanel, gridBagConstraints);

        targetPanel.setBorder(new javax.swing.border.TitledBorder(null, "Select Child", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 12)));
        getContentPane().add(targetPanel, new java.awt.GridBagConstraints());

        pack();
    }//GEN-END:initComponents
    
    private void makeSubEntityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeSubEntityButtonActionPerformed
        // Add your handling code here:
        /**Entity shown on the left */
        DesignEntity parentEntity = getSelectedEntity(sourcePage);
        /**Entity shown on the right */
        DesignEntity childEntity = getSelectedEntity(targetPage);
        PeerGroup parentPG = getParentPG(sourcePage);
        PeerGroup childPG = getParentPG(targetPage);
        /**The state that the child entity must be in before parent entity can resume */
        String childLastState = childEntity.getLoopNetAdv().getLastState();
        /**User selects any transition of the parentEntity */
        String returnTrans = getSelectedReturnTransition();
        
        try {
            /**Provides very basic error checking */
            if(sourceTargetOK(parentEntity, childEntity)) {
                /**First, attempt to create a constraint link that prevents parent from resuming,
                 * until after child is completed. Note that parent, child entities are reversed */
                LinkAdvertisement newSubLink = advUtils.createLinkAdvertisement(
                childEntity, parentEntity, DO_BEFORE, childLastState, returnTrans, childPG, parentPG);
                topFrame.refreshTreeWithNodeSelected();
                //this.dispose();
                
                /**If new sub entity can be created: does not conflict logically with existing ones */
//                if (newSubLink != null) {
//                    /**Second, create an information link that points from the parent to the subentity */
//                    advUtils.createLinkAdvertisement(
//                    childEntity, parentEntity, IS_SUB_ENTITY_OF, NOT_RELEVANT, NOT_RELEVANT, childPG, parentPG);
//                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_makeSubEntityButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // Add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton makeSubEntityButton;
    private javax.swing.JComboBox returnTransComboBox;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JPanel targetPanel;
    private javax.swing.JLabel targetTransLabel;
    private javax.swing.JLabel targetTransLabel1;
    // End of variables declaration//GEN-END:variables
    
    
}
