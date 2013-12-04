/*
 * NewUnAssumeRoleForm.java
 *
 * Created on Oct. 4, 2004
 */

package dpm.dpmApp.desktop.forms;

import dpm.content.DesignEntity;
import dpm.content.advertisement.AdvUtilities;
import dpm.content.advertisement.designEntity.related.RoleAdvertisement;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import net.jxta.peergroup.PeerGroup;




/**
 *
 * @author  cumming
 */
public class ResignRoleForm extends JFrame implements DpmTerms {
    private DesignEntity entity;
    private LoopNetAdvertisement loopNetAdv;
    private Peer appUser;
    private PeerGroup parentPG;
    private HashSet assumedRoleNames;
    private DpmAppTopFrame topFrame;
    private AdvUtilities advUtils;
    
    
    /** Creates new form NewRoleForm */
    public ResignRoleForm(DesignEntity entity, PeerGroup parentPG, DpmAppTopFrame topFrame) {
        this.entity = entity;
        this.loopNetAdv = entity.getLoopNetAdv();
        this.parentPG = parentPG;
        this.topFrame = topFrame;
        this.assumedRoleNames = entity.getAssumedRoleNamesThisAppUser();
        if(assumedRoleNames.size() < 1) {
            topFrame.showErrorDialog("You have not yet signed up for any roles, with this entity", this);
            return;
        }
        this.appUser = topFrame.getAppUser();
        this.advUtils = topFrame.getAdvUtils();
        initComponents();
        setTitle("Resign Role for: " + entity.getEntityType() + " " + entity.getFullName());
        setComboModel();
        topFrame.setPosition(this.getWidth(), this.getHeight(), this);
        pack();
        show();
    }
    
    /** Get all the role names and subtract the ones that have already been assumed by the peer,
     * for this task */
    //    public HashSet getUnassumedRoleNames() {
    //        /** all required roles for all tasks */
    //        HashSet allPolAdvs = appUser.getEntityRelatives().getPolicies().collapseAll();
    //        HashSet allRoleNames = new HashSet();
    //        for(Iterator i = allPolAdvs.iterator(); i.hasNext(); ) {
    //            PolicyAdvertisement polAdv = (PolicyAdvertisement)i.next();
    //            //System.out.println("Size of roles in tpAdv: " + String.valueOf(tpAdv.getRoles().size()));
    //            allRoleNames.addAll(polAdv.getRoles());
    //        }
    //        /** roles already assumed, by this peer, for this task */
    //        HashSet assumedRoleNames = entity.getAssumedRoleNamesThisAppUser();
    //
    //        allRoleNames.removeAll(assumedRoleNames);
    //        return allRoleNames;
    //    }
    
    /**A combobox that has unassumed role names as its model */
    public void setComboModel() {
        /** Gets all the appropriate role names */
        //HashSet unassumedRoleNames = getUnassumedRoleNames();
        //HashSet assumedRoleNames = entity.getAssumedRoleNamesThisAppUser();
        comboBox.setModel(new DefaultComboBoxModel(new Vector(this.assumedRoleNames)));
    }
    
    /**Creates inputs at the start of tasks.
     * Works on the principle: if you sign up for a role, then you must be aware of the task,
     * therefore, you can create an input for the first transition only */
    //retired 31 March 2004: assumes their is a first 'awareness' state
    //    public void createInputAdvIfAppropriate(String roleName) {
    //        if(loopNetAdv == null) {
    //            System.out.println("ERROR: loopNetAdv is null in NewRoleForm");
    //            return;
    //        }
    //        String curState = entity.getCurrentState();
    //        /** Only go on if task is at the new state */
    //        if(!curState.equals(entity.getLoopNetAdv().getFirstState())) {
    //            return;
    //        }
    //        /** 'first contact' is the next transition */
    //        String nextTrans = loopNetAdv.getFirstTransition();
    //        Set usefulRoles = entity.getUsefulRolesAssumedThisAppUser(nextTrans);
    //        /** if current role name is a 'useful' one, make an input adv for it */
    //        if(usefulRoles.contains(roleName)) {
    //            advUtils.createInputAdvertisement(entity, nextTrans, roleName, parentPG);
    //        }
    //    }
    
    public void printMessage(String s) {
        topFrame.printMessage(s);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        listPanel = new javax.swing.JPanel();
        comboBoxLabel = new javax.swing.JLabel();
        comboBox = new javax.swing.JComboBox();
        buttonsPanel = new javax.swing.JPanel();
        resignRoleButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Add Your Role for DesignEntity");
        setName("NewRoleFormFrame");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        listPanel.setLayout(new java.awt.GridBagLayout());

        listPanel.setPreferredSize(new java.awt.Dimension(275, 55));
        comboBoxLabel.setFont(new java.awt.Font("Arial", 1, 12));
        comboBoxLabel.setText("Select Existing Role Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 10, 5);
        listPanel.add(comboBoxLabel, gridBagConstraints);

        comboBox.setFont(new java.awt.Font("Arial", 0, 12));
        comboBox.setPreferredSize(new java.awt.Dimension(128, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        listPanel.add(comboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(listPanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        resignRoleButton.setFont(new java.awt.Font("Arial", 1, 12));
        resignRoleButton.setText("Resign From Role");
        resignRoleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resignRoleButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(resignRoleButton, new java.awt.GridBagConstraints());

        cancelButton.setFont(new java.awt.Font("Arial", 1, 12));
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(buttonsPanel, gridBagConstraints);

        pack();
    }//GEN-END:initComponents
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // Add your handling code here:
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    private void resignRoleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resignRoleButtonActionPerformed
        // Add your handling code here:
        String roleNameToDelete = (String)comboBox.getSelectedItem();
        if(roleNameToDelete == null) {
            topFrame.showErrorDialog("Please select an existing role name", this);
            return;
        }
        if(roleNameToDelete.equals(AUTHOR)) {
            topFrame.showErrorDialog("Cannot resign from the author role", this);
            return;
        }
        RoleAdvertisement roleAdvToDelete = advUtils.getRoleAdvToDelete(roleNameToDelete, entity);
        if(roleAdvToDelete == null) {
            topFrame.showErrorDialog("Could not find selected role", this);
            return;
        }
        /** Creates a DeleteAdv and publishes it [locally] in the parentPG.
         * Delete checker checks whether it has been already deleted, in following method */
        advUtils.deleteOneRoleAdv(roleAdvToDelete, parentPG);
        topFrame.refreshTreeWithLeafSelected();
        dispose();
    }//GEN-LAST:event_resignRoleButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        //System.exit(0);
        dispose();
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    //    public static void main(String args[]) {
    //        new NewRoleForm().show();
    //    }
    //
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox comboBox;
    private javax.swing.JLabel comboBoxLabel;
    private javax.swing.JPanel listPanel;
    private javax.swing.JButton resignRoleButton;
    // End of variables declaration//GEN-END:variables
    
}