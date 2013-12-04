/*
 * LinkForm.java
 *
 * Created on September 16, 2004, 4:14 PM
 */

package dpm.dpmApp.desktop.forms;

import dpm.container.tree.PGTreeLeaf;
import dpm.content.ContentStorage;
import dpm.content.DesignEntity;
import dpm.content.advertisement.AdvUtilities;
import dpm.content.constraint.Link;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.dpmApp.desktop.subpages.PeerGroupsTreePage;
import dpm.peer.Peer;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import net.jxta.peergroup.PeerGroup;
import dpm.container.tree.*;

/**
 *
 * @appUser  cumming
 */
public abstract class LinkForm extends JFrame implements DpmTerms {  
    protected PeerGroupsTreePage sourcePage;
    protected PeerGroupsTreePage targetPage;
    protected JTree sourceTree;
    protected JTree targetTree;
    protected Peer appUser;
    protected DpmAppTopFrame topFrame;
    protected ContentStorage entities;
    protected AdvUtilities advUtils;
    /**the PG in which the form was originally opened */
    protected PeerGroup parentPG;
    /**The PG in which the source of the link resides */
    protected PeerGroup sourcePG;
    /**The PG in which the target of the link resides */
    protected PeerGroup targetPG;
    
    
    /** Creates a new instance of LinkForm */
    protected LinkForm(PeerGroup parentPG, DpmAppTopFrame topFrame) {
        this.parentPG = parentPG;
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
        this.entities = appUser.getUserNamedEntities();
        this.advUtils = topFrame.getAdvUtils();
        /**Check if there is any content in parentPG to link */
//        if(noEntitiesInParentPG()) {
//            topFrame.showInfoDialog("There are no Entities in the Selected PeerGroup", this);
//            return;
//        }
    }
    
    /**Provides basic error checking */
    protected boolean sourceTargetOK(DesignEntity sourceEntity, DesignEntity targetEntity) {
        if(sourceEntity == targetEntity) {
            topFrame.showErrorDialog("Source and target must be different", this);
            return false;
        }
        if(sourceEntity == null) {
            topFrame.showErrorDialog("Source entity is null", this);
            return false;
        }
        if(targetEntity == null) {
            topFrame.showErrorDialog("Target entity is null", this);
            return false;
        }
        return true;
    }
    
    /**Returns true if there are no entities in the parentPG.
     * If true, then this form will not open, since there is nothing to link */
    protected boolean noEntitiesInParentPG() {
        return (entities.iterator(parentPG.getPeerGroupID().toString()) == null);
    }
    
    public DesignEntity getSelectedEntity(PeerGroupsTreePage page) {
        return page.getSelectedDesignEntity();
    }
    
    public PeerGroup getParentPG(PeerGroupsTreePage page) {
        PGTreeLeaf selLeaf = page.getSelectedPGTreeLeaf();
        return page.getParentPGOfSelectedLeaf(selLeaf);
    }
    
    protected PeerGroupsTreePage addTreePage(JPanel parentPanel, PeerGroup parentPG, String showType) {
        PeerGroupsTreePage inputPage;
        String parentName = parentPG.getPeerGroupName();
        if(parentPG == null) {
            printMessage("ERROR: Null parentPG in addTreePanel");
            System.out.println("ERROR: Null parentPG in addTreePanel");
            return null;
        }
        inputPage = new PeerGroupsTreePage(
        parentName, topFrame, parentPG, showType, LINK_PAGE_WIDTH, LINK_PAGE_HEIGHT);
        inputPage.setVisible(true);
        parentPanel.add(inputPage);
        this.pack();
        inputPage.setBasePG(parentPG);
        return inputPage;
    }
    
    protected PeerGroupsTreePage addTreePageMinusParent(JPanel parentPanel, DesignEntity parentEntity, 
    PeerGroup parentPG, String showType) {
        PeerGroupsTreePage inputPage;
        String parentName = parentPG.getPeerGroupName();
        if(parentPG == null) {
            printMessage("ERROR: Null parentPG in addTreePanel");
            System.out.println("ERROR: Null parentPG in addTreePanel");
            return null;
        }
        inputPage = new PeerGroupsTreePage(
        parentName, topFrame, parentPG, showType, LINK_PAGE_WIDTH, LINK_PAGE_HEIGHT);
        inputPage.setVisible(true);
        parentPanel.add(inputPage);
        this.pack();
        inputPage.setBasePG(parentPG);
        return inputPage;
    }
    
  
    /**Get all constraint (link) names, that are not doBefores */
    public HashSet getConstraintNames() {
        /** all required roles for all tasks */
        HashSet constraintNames = new HashSet();
        
        /**If not a doBefore form, add all existing terms that are not doBefores */
        HashSet allLinks = appUser.getEntityRelatives().getAllLinks().collapseAll();
        for(Iterator i = allLinks.iterator(); i.hasNext(); ) {
            Link link = (Link)i.next();
            String conName = link.getLinkAdv().getConstraintName();
            //System.out.println("Size of roles in tpAdv: " + String.valueOf(tpAdv.getRoles().size()));
            if(!conName.equals(DO_BEFORE)) {
                constraintNames.add(conName);
            }
        }
        return constraintNames;
    }
    
    public void printMessage(String s) {
        topFrame.printMessage(s);
    }
    
}
