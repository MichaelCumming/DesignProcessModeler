/*
 * HistoryForm.java
 *
 * Created on February 19, 2004, 4:13 PM
 */

package dpm.dpmApp.desktop.forms.information;

import dpm.content.DesignEntity;
import dpm.content.advertisement.designEntity.related.HistoryAdvertisement;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import java.util.Iterator;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;
import dpm.dpmApp.desktop.*;
import dpm.dpmApp.desktop.forms.*;


/**
 * A utility that displays a historyAdv ContentStorage as a slide show.
 * Could be applied to other ContentStorages.
 * @author  cumming
 */
public class HistoryInformationForm extends InformationForm implements DpmTerms {
    private DesignEntity entity;
    private String entityType;
    /** An array of StyledDocuments = models for JTextPanes */
    private StyledDocument[] frames;
    private static int FIRST_FRAME_INDEX;
    private static int LAST_FRAME_INDEX;
    private int curFrameIndex;
    private int numFrames;
 
    
    /** Creates new form HistoryForm */
    public HistoryInformationForm(DesignEntity entity, DpmAppTopFrame topFrame) {
        super(topFrame);
        this.entity = entity;
        this.entityType = entity.getEntityType();
        this.numFrames = appUser.getEntityRelatives().getHistories().numValues(entity.getDesignEntityID().toString());
        //System.out.println("HistViewer numFrames = " + String.valueOf(numFrames));
        /**number of frames to view*/
        if(numFrames < 1) {
            topFrame.showInfoDialog("No history is available for this design entity", this);
            return;
        }
        this.FIRST_FRAME_INDEX = 0;
        /** frame num, if counting from zero*/
        this.LAST_FRAME_INDEX = numFrames - 1;
        /** start off at the beginning */
        this.curFrameIndex = FIRST_FRAME_INDEX;
        /** init the frames array */
        this.frames = new StyledDocument [numFrames];
        initComponents();
        textPane.setBackground(INFO_DISPLAY_COLOR);
        loadEntityHistoryContent();
        topFrame.setPosition(this.getWidth(), this.getHeight(), this);
        loadFirstFrame();
        pack();
        show();
    }
    
    
    /** load all advs into an array  of StyledDocuments */
    public void loadEntityHistoryContent() {
        int curIndex = 0;
        if(appUser.historiesExist(entity)) {
            for(Iterator i = appUser.getHistoriesIterator(entity); i.hasNext(); ) {
                HistoryAdvertisement adv = (HistoryAdvertisement)i.next();
                frames[curIndex] = createOneHistAdvFrame(adv);
                curIndex++;
            }
        }
    }
    
    /** Takes a java object and prints it to screen */
    public StyledDocument createOneHistAdvFrame(HistoryAdvertisement histAdv) {
        String entityName = histAdv.getFullName();
        String transName = histAdv.getTransName();
        String entityState = histAdv.getState();
        /** Create a new model for a JTextPane */
        StyledDocument doc = new JTextPane().getStyledDocument();
        addStylesToDocument(doc);
        
        /**Insert entity name */
        insert("Entity name: ", doc, BOLD);
        insert(entityName, doc, REGULAR);
        insert(NEWLINE, doc, REGULAR);
        
        /**Insert entity type */
        insert("Entity type: ", doc, BOLD);
        insert(entityType, doc, REGULAR);
        insert(NEWLINE, doc, REGULAR);
        
        /**Insert transition name */
        insert("Transition name: ", doc, BOLD);
        insert(transName, doc, REGULAR);
        insert(NEWLINE, doc, REGULAR);
        
        /**Insert date */
        insert("Date transition enabled: ", doc, BOLD);
        insert(dateFormat.format(histAdv.getDateCreate()), doc, REGULAR);
        insert(NEWLINE, doc, REGULAR);
        
        /**Insert state name */
        insert("Lead to state: ", doc, BOLD);
        insert(entityState, doc, REGULAR);
        insert(NEWLINE, doc, REGULAR);
        
        /**Insert contributor names */
        insert("Peers who contributed to this state change: ", doc, BOLD);
        for (Iterator i = histAdv.getPeerRoles().getKeySet().iterator(); i.hasNext(); ) {
            String roleName = (String)i.next();
            if (histAdv.getPeerRoles().iterator(roleName) != null) {
                /** For all entries in the required inputs content storage */
                for (Iterator j = histAdv.getPeerRoles().iterator(roleName); j.hasNext(); ) {
                    String peerName = (String)j.next();
                    insert(NEWLINE, doc, REGULAR);
                    insert(roleName + ": " + peerName, doc, REGULAR);
                }
            }
            else {
                insert(NONE, doc, REGULAR);
            }
        }
        return doc;
    }
    
    public boolean atBeginning() {
        return curFrameIndex <= FIRST_FRAME_INDEX;
    }
    public boolean atEnd() {
        return curFrameIndex >= LAST_FRAME_INDEX;
    }
    public boolean inMiddle() {
        return (curFrameIndex > FIRST_FRAME_INDEX && curFrameIndex < LAST_FRAME_INDEX);
    }
    
    public void redraw() {
        enableAppropriateButtons();
        setTitleInfo();
    }
    
    public void setTitleInfo() {
        setTitle("History Viewer for " + entityType + " " + entity.getFullName() + ". " +
        "Frame: " + String.valueOf(curFrameIndex + 1) + "/" + String.valueOf(numFrames));
    }
    
    public void enableAppropriateButtons() {
        if(inMiddle()) {
            previousButton.setEnabled(true);
            toBeginButton.setEnabled(true);
            nextButton.setEnabled(true);
            toEndButton.setEnabled(true);
            /**if in the middle, then the other categories can't apply */
            return;
        }
        /** Not in the middle*/
        /** if only one frame */
        if(atBeginning() && atEnd()) {
            previousButton.setEnabled(false);
            toBeginButton.setEnabled(false);
            nextButton.setEnabled(false);
            toEndButton.setEnabled(false);
            return;
        }
        /**More than one frame*/
        if(atBeginning()) {
            previousButton.setEnabled(false);
            toBeginButton.setEnabled(false);
            nextButton.setEnabled(true);
            toEndButton.setEnabled(true);
        }
        if(atEnd()) {
            nextButton.setEnabled(false);
            toEndButton.setEnabled(false);
            previousButton.setEnabled(true);
            toBeginButton.setEnabled(true);
        }
    }
    
    public void displayOneAdv(int frameNum) {
        textPane.setStyledDocument(frames[frameNum]);
        curFrameIndex = frameNum;
        redraw();
    }
    
    public void loadFirstFrame() {
        displayOneAdv(FIRST_FRAME_INDEX);
        redraw();
    }
    
    public void loadLastFrame() {
        displayOneAdv(LAST_FRAME_INDEX);
        redraw();
    }
    
    public void loadNextFrame() {
        if(atEnd()) {
            redraw();
            return;
        }
        curFrameIndex++;
        displayOneAdv(curFrameIndex);
    }
    
    public void loadPrevFrame() {
        if(atBeginning()) {
            redraw();
            return;
        }
        curFrameIndex--;
        displayOneAdv(curFrameIndex);
    } 
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        displayPanel = new javax.swing.JPanel();
        scrollPane = new javax.swing.JScrollPane();
        textPane = new javax.swing.JTextPane();
        buttonsPanel = new javax.swing.JPanel();
        toBeginButton = new javax.swing.JButton();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        toEndButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setTitle("History Viewer");
        setName("HistoryViewerFrame");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        displayPanel.setLayout(new java.awt.GridBagLayout());

        displayPanel.setBackground(new java.awt.Color(255, 255, 255));
        scrollPane.setFont(new java.awt.Font("Arial", 0, 12));
        scrollPane.setPreferredSize(new java.awt.Dimension(350, 200));
        textPane.setBackground(new java.awt.Color(204, 255, 204));
        textPane.setFont(new java.awt.Font("Arial", 0, 12));
        textPane.setPreferredSize(new java.awt.Dimension(350, 350));
        scrollPane.setViewportView(textPane);

        displayPanel.add(scrollPane, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        getContentPane().add(displayPanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        toBeginButton.setFont(new java.awt.Font("Arial", 1, 12));
        toBeginButton.setText("<<");
        toBeginButton.setToolTipText("To beginning of history");
        toBeginButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toBeginButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(toBeginButton, new java.awt.GridBagConstraints());

        previousButton.setFont(new java.awt.Font("Arial", 1, 12));
        previousButton.setText("Previous");
        previousButton.setToolTipText("Previous item in history");
        previousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(previousButton, new java.awt.GridBagConstraints());

        nextButton.setFont(new java.awt.Font("Arial", 1, 12));
        nextButton.setText("Next");
        nextButton.setToolTipText("Next item in history");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(nextButton, new java.awt.GridBagConstraints());

        toEndButton.setFont(new java.awt.Font("Arial", 1, 12));
        toEndButton.setText(">>");
        toEndButton.setToolTipText("To end of history");
        toEndButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toEndButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(toEndButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        getContentPane().add(buttonsPanel, gridBagConstraints);

        pack();
    }//GEN-END:initComponents
    
    private void toEndButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toEndButtonActionPerformed
        // Add your handling code here:
        loadLastFrame();
    }//GEN-LAST:event_toEndButtonActionPerformed
    
    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        // Add your handling code here:
        loadNextFrame();
    }//GEN-LAST:event_nextButtonActionPerformed
    
    private void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousButtonActionPerformed
        // Add your handling code here:
        loadPrevFrame();
    }//GEN-LAST:event_previousButtonActionPerformed
    
    private void toBeginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toBeginButtonActionPerformed
        // Add your handling code here:
        loadFirstFrame();
    }//GEN-LAST:event_toBeginButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        //System.exit(0);
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    /**
     * @param args the command line arguments
     */
    //    public static void main(String args[]) {
    //        new HistoryForm().show();
    //    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel displayPanel;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton previousButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextPane textPane;
    private javax.swing.JButton toBeginButton;
    private javax.swing.JButton toEndButton;
    // End of variables declaration//GEN-END:variables
    
}
