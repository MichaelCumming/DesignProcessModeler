/*
 * NewTest.java
 *
 * Created on December 3, 2003, 3:30 PM
 */

package dpm.dpmApp.desktop.forms;

import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmPage;
import dpm.dpmApp.desktop.subpages.*;
import dpm.peer.Peer;
import javax.swing.JFrame;
import net.jxta.peergroup.PeerGroup;
import dpm.dpmApp.desktop.*;


/** The user input form for New Peergroups.
 * @author cumming
 * @since December 3, 2003, 3:30 PM
 */
public class NewPeerGroupForm extends JFrame implements DpmTerms {
    protected DpmAppTopFrame topFrame;
    protected DpmPage parentPage; //the page that shows peergroups
    protected PeerGroup parentPG;
    protected Peer appUser;
    
    /** Creates new form NewTest */
    public NewPeerGroupForm(DpmAppTopFrame topFrame, PeerGroup parentPG) {
        this.topFrame = topFrame;
        this.parentPG = parentPG;
        this.appUser = topFrame.getAppUser();
        //this.parentPage = parentPage;
        initComponents();
        topFrame.setPosition(this.getWidth(), this.getHeight(), this);
        pack();
        show();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        fieldsPanel = new javax.swing.JPanel();
        pgDescriptionLabel = new javax.swing.JLabel();
        pgNameLabel = new javax.swing.JLabel();
        pgNameField = new javax.swing.JTextField();
        pgDescriptionField = new javax.swing.JTextField();
        buttonsPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("Add PG Name and Description");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        fieldsPanel.setLayout(new java.awt.GridBagLayout());

        pgDescriptionLabel.setFont(new java.awt.Font("Arial", 1, 12));
        pgDescriptionLabel.setText("Description");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        fieldsPanel.add(pgDescriptionLabel, gridBagConstraints);

        pgNameLabel.setFont(new java.awt.Font("Arial", 1, 12));
        pgNameLabel.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        fieldsPanel.add(pgNameLabel, gridBagConstraints);

        pgNameField.setColumns(15);
        pgNameField.setFont(new java.awt.Font("Arial", 0, 12));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        fieldsPanel.add(pgNameField, gridBagConstraints);

        pgDescriptionField.setColumns(15);
        pgDescriptionField.setFont(new java.awt.Font("Arial", 0, 12));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        fieldsPanel.add(pgDescriptionField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        getContentPane().add(fieldsPanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        okButton.setFont(new java.awt.Font("Arial", 1, 12));
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(okButton, new java.awt.GridBagConstraints());

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
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        getContentPane().add(buttonsPanel, gridBagConstraints);

        pack();
    }//GEN-END:initComponents
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // Add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // Add your handling code here:
         if(pgNameField.getText() == null || pgNameField.getText().equals(EMPTY_STRING)) {
            topFrame.showErrorDialog("Please enter a name", this);
            return;
        }
        String pgName = pgNameField.getText(); 
       
        if(pgDescriptionField.getText() == null || pgDescriptionField.getText().equals(EMPTY_STRING)) {
            topFrame.showErrorDialog("Please enter a description", this);
            return;
        }
        String pgDescription = pgDescriptionField.getText();
        PeerGroupsTreePage treePage = topFrame.getSelectedPGTreePage();
        /**make a node from a selected PGTreeNode, then redraw the tree: */
        treePage.addToTreeNewPG_NotUsingAdv(pgName, pgDescription, parentPG);
        topFrame.refreshTreeWithNodeSelected();
        this.dispose();
    }//GEN-LAST:event_okButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        //System.exit(0);
        this.dispose();
    }//GEN-LAST:event_exitForm
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel fieldsPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField pgDescriptionField;
    private javax.swing.JLabel pgDescriptionLabel;
    private javax.swing.JTextField pgNameField;
    private javax.swing.JLabel pgNameLabel;
    // End of variables declaration//GEN-END:variables
    
}