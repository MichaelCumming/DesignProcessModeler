/*
 * InputsOutputsForm.java
 *
 * Created on March 17, 2004, 3:07 PM
 */

package dpm.dpmApp.desktop.forms;

//import com.touchgraph.graphlayout.*;
//import com.touchgraph.graphlayout.interaction.TGUIManager;
import dpm.content.advertisement.designEntity.related.constraint.*;
import dpm.content.DesignEntity;
import dpm.content.designEntity.*;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import java.awt.Color;
import java.util.Iterator;
import java.util.*;
import javax.swing.JFrame;
import dpm.peer.Peer;
import dpm.content.constraint.*;


/**
 *
 * @author  cumming
 */
public class InputsOutputsForm extends JFrame implements DpmTerms {
    private DesignEntity baseEntity;
    private DpmAppTopFrame topFrame;
    private Peer appUser;
//    private GLPanel glPanel;
//    private TGUIManager tgUIManager;
//    private TGPanel tgPanel;
    /**A map of nodes created, to avoid creating the same one twice */
    private HashMap allNodes = new HashMap();
    
    
    /** Creates new form InputsOutputsForm */
    public InputsOutputsForm(DesignEntity baseEntity, DpmAppTopFrame topFrame) {
        this.baseEntity = baseEntity;
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
//        this.tgUIManager = new TGUIManager();
//        this.glPanel = new GLPanel(this);
//        this.tgPanel = glPanel.getTgPanel();
        //tgPanel.setPreferredSize(new java.awt.Dimension(450, 300));
        initComponents();
        setTitle("Links into and away from: " + baseEntity.getEntityType() + " " + baseEntity.getFullName());
        //graphPanel.add(glPanel);
//        try {
//            /**add all nodes, starting with a node for the baseEntity */
//            //addAllNodes();
//        }
//        catch (TGException e) {
//            e.printStackTrace();
//        }
        /** pack after adding panels */
        pack();
        topFrame.setPosition(this.getWidth(), this.getHeight(), this);
        show();
    }
    
//    public void addAllNodes() throws TGException {
//        Node baseNode = addNode(baseEntity);
//        /**Select (make yellow) the first node added */
//        tgPanel.setSelect(tgPanel.getGES().getFirstNode());
//        
//        addIncomingNodesAndEdges(baseNode, baseEntity);
//        addOutgoingNodesAndEdges(baseNode, baseEntity);
//    }
    
    /**
     * Adds a PNet Node, with its ID and label being the current node count plus 1.
     * @see com.touchgraph.graphlayout.Node  @author Michael Cumming
     * @since 17.Mar.2004
     */
//    public Node addNode(DesignEntity entity) throws TGException {
//        String padding = " ";
//        String label = padding + entity.getFullName() + padding;
//        
//        /**See if an existing node has already been created */
//        Node existingNode = (Node)allNodes.get(label);
//        if(existingNode != null) {
//            return existingNode;
//        }
//        
//        String id = String.valueOf(tgPanel.getNodeCount() + 1);
//        String suffix = EMPTY_STRING;
//        
//        int nodeType = -1;
//        int nodeShape = -1;
//        Color nodeColor = Color.black;
//        
//        if(entity instanceof UserNamedEntity) {
//            nodeShape = ENTITY_SHAPE;
//            nodeType = ENTITY_TYPE;
//            nodeColor = ENTITY_COLOR;
//        }
//        else {
//            System.out.println("ERROR: unrecognized type in InputsOutputsForm.addNode()");
//            return null;
//        }
//        Node node = new Node(id, nodeShape, nodeType, nodeColor, label, suffix);
//        
//        /**Add the new node to the Map to avoid node duplication */
//        allNodes.put(label, node);
//        tgPanel.updateDrawPos(node); /** this sets node position to 0,0 */
//        tgPanel.addNode(node);
//        return node;
//    }
    
//    public void addIncomingNodesAndEdges(Node targetNode, DesignEntity targetEntity) {
//        Set incoming = targetEntity.getIncomingLinks();
//        
//        if(incoming!=null && !incoming.isEmpty()) {
//            for(Iterator i = incoming.iterator(); i.hasNext(); ) {
//                Link link = (Link)i.next();
//                LinkAdvertisement sourceAdv = link.getLinkAdv();
//                DesignEntity sourceEntity = (DesignEntity)appUser.getUserNamedEntities().getEntityByIDString(sourceAdv.getSourceID().toString());
//              
//                if(sourceEntity!=null) {
//                    //System.out.println("Con name: " + sourceAdv.getConstraintName());
//                    addOneIncomingEdge(sourceEntity, targetNode, sourceAdv.getConstraintName());
//                }
//                else {
//                    System.out.println("ERROR: sourceEntity is null in addIncomingNodesAndEdges()");
//                }
//            }
//        }
//        else {
//            //            System.out.println("incoming null or empty: " +
//            //            targetEntity.getEntityType() + " " + targetEntity.getName());
//        }
//        //System.out.println("Finished adding incoming nodes and edges: " +
//        //targetEntity.getEntityType() + " " + targetEntity.getName());
//    }
    
//    public void addOutgoingNodesAndEdges(Node sourceNode, DesignEntity sourceEntity) {
//        Set outgoing = sourceEntity.getOutgoingLinks();
//        
//        if(outgoing!=null && !outgoing.isEmpty()) {
//            for(Iterator i = outgoing.iterator(); i.hasNext(); ) {
//                Link link = (Link)i.next();
//                LinkAdvertisement targetAdv = link.getLinkAdv();
//                DesignEntity targetEntity = (DesignEntity)appUser.getUserNamedEntities().getEntityByIDString(targetAdv.getTargetID().toString());
//                if(targetEntity!=null) {
//                    //System.out.println("Con name: " + targetAdv.getConstraintName());
//                    addOneOutgoingEdge(sourceNode, targetEntity, targetAdv.getConstraintName());
//                }
//                else {
//                    //System.out.println("ERROR: targetEntity is null in addOutgoingNodesAndEdges()");
//                }
//            }
//        }
//        else {
//            //            System.out.println("outgoing null or empty: " +
//            //            sourceEntity.getEntityType() + " " + sourceEntity.getName());
//        }
//        //        System.out.println("Finished adding outgoing nodes and edges: " +
//        //        sourceEntity.getEntityType() + " " + sourceEntity.getName());
//    }
    
//    public void addOneIncomingEdge(DesignEntity sourceEntity, Node targetNode, String edgeLabel) {
//        try {
//            Node sourceNode = addNode(sourceEntity);
//            /**If edges already exist to targetNode */
//            if(sourceNode.numEdgesTo(targetNode) > 0) {
//                Edge existingEdge = sourceNode.getEdgeTo(targetNode);
//                existingEdge.appendLabel(edgeLabel);
//                return;
//            }
//            tgPanel.addEdge(sourceNode, targetNode, Edge.DEFAULT_LENGTH, edgeLabel);
//        }
//        catch (TGException e) {
//            e.printStackTrace();
//        }
//    }
    
//    public void addOneOutgoingEdge(Node sourceNode, DesignEntity targetEntity, String edgeLabel) {
//        try {
//            Node targetNode = addNode(targetEntity);
//            /**If edges already exist to targetNode */
//            if(sourceNode.numEdgesTo(targetNode) > 0) {
//                Edge existingEdge = sourceNode.getEdgeTo(targetNode);
//                existingEdge.appendLabel(edgeLabel);
//                return;
//            }
//            tgPanel.addEdge(sourceNode, targetNode, Edge.DEFAULT_LENGTH, edgeLabel);
//        }
//        catch (TGException e) {
//            e.printStackTrace();
//        }
//    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        graphPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        getContentPane().add(graphPanel, new java.awt.GridBagConstraints());

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        closeButton.setFont(new java.awt.Font("Arial", 1, 12));
        closeButton.setText("Close Window");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(closeButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        getContentPane().add(buttonsPanel, gridBagConstraints);

        pack();
    }//GEN-END:initComponents
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // Add your handling code here:
        this.dispose();
    }//GEN-LAST:event_closeButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        //System.exit(0);
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    /** Getter for property tgUIManager.
     * @return Value of property tgUIManager.
     *
     */
//    public com.touchgraph.graphlayout.interaction.TGUIManager getTgUIManager() {
//        return tgUIManager;
//    }
    
    /** Setter for property tgUIManager.
     * @param tgUIManager New value of property tgUIManager.
     *
     */
//    public void setTgUIManager(com.touchgraph.graphlayout.interaction.TGUIManager tgUIManager) {
//        this.tgUIManager = tgUIManager;
//    }
    
    /**
     * @param args the command line arguments
     */
    //    public static void main(String args[]) {
    //        new InputsOutputsForm().show();
    //    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel graphPanel;
    // End of variables declaration//GEN-END:variables
    
}
