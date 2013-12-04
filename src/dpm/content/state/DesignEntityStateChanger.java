/*
 * TaskStateChanger.java
 *
 * Created on January 15, 2004, 10:47 AM
 */

package dpm.content.state;

import dpm.content.ContentStorage;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import de.renew.gui.*;
import de.renew.simulator.NetInstance;
import de.renew.simulator.Net;
import de.renew.shadow.ShadowNet;
import de.renew.gui.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Enumeration;
import de.renew.simulator.Transition;
import de.renew.simulator.Place;
import dpm.content.state.*;
import java.util.*;
import de.renew.simulator.Net;
import dpm.content.state.*;
import dpm.content.*;
import dpm.peer.Peer;
import CH.ifa.draw.framework.Drawing;
import java.io.*;
import CH.ifa.draw.util.StorableOutput;
import CH.ifa.draw.util.StorableInput;
import java.io.OutputStream;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.advertisement.designEntity.related.constraint.*;
import dpm.content.*;
import dpm.content.designEntity.*;
import dpm.dpmApp.desktop.*;


/**
 * Sees whether a design entity's state can be changed, given available inputs.
 * If so, it creates a new taskState advertisement and publishes it.
 * @author  cumming
 * @since January 15, 2004, 10:47 AM
 */
public class DesignEntityStateChanger implements DpmTerms {
    /** The design entity whose state is being determined */
    private DesignEntity designEntity;
    /** A net that describes the various states a design entity gets into */
    private LoopNetAdvertisement loopNetAdv;
    private StateNetAdvertisement stateNetAdv;
    /** The petri net that determines if the design entity can advance to the next state */
    private Net scNet;
    private StateChangerNetAccessor scNetAccessor;
    private String transName;
    private String nextStateName;
    private ContentStorage peerRoles;
    private PeerGroup parentPG;
    private DpmAppTopFrame topFrame;
    private Peer appUser;
    private AdvUtilities advUtils;
    /** the petri net app */
    private Demonstrator petriNetApp;
    
    
    /** Creates a new instance of TaskStateChanger */
    /** This changes the design entity */
    public DesignEntityStateChanger(DesignEntity designEntity, PeerGroup parentPG, DpmAppTopFrame topFrame) {
        this.designEntity = designEntity;
        this.parentPG = parentPG;
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
        /** Each design entity has its own loop type */
        this.loopNetAdv = designEntity.getLoopNetAdv();
        if(loopNetAdv != null) {
            this.transName = loopNetAdv.getNextTransFromState(designEntity.getCurrentState());
            this.nextStateName = loopNetAdv.getNextState(designEntity.getCurrentState());
        }
        else {
            System.out.println("ERROR: loopNetAdv not set in TaskStateChanger");
        }
        this.advUtils = topFrame.getAdvUtils();
        /** Holds roles and peer who fulfil them  at state changes. Needed for TaskHistoryAdvs */
        this.peerRoles = new ContentStorage("java.lang.String", appUser);
        this.petriNetApp = topFrame.getPetriNetApp();
        /** Don't bother opening up the net if there are no inputs */
        if(appUser.inputsExist(designEntity)) {
            openStateChangerDrawing();
        }
        else {
            topFrame.showInfoDialog("No inputs have been discovered for design entity " +
            designEntity.getFullName() + "." + NEWLINE +
            "State cannot change without inputs", topFrame);
            return;
        }
    }
    
    public void openStateChangerDrawing() {
        if(petriNetApp != null) {
            //petriNetApp.loadAndOpenDrawing(topFrame.STATE_CHANGER_NET_FILE);
            //Drawing curDrawing = topFrame.getDrawing(topFrame.STATE_CHANGER_NET_NAME);
            //System.out.println("curDrawing: " + curDrawing.getName());
        }
        else {
            System.out.println("ERROR: petriNetApp is null");
            return;
        }
        
        try {
            CPNSimulation sim = petriNetApp.getMode().getSimulation();
            //petriNetApp.getMode().getSimulation().initSimulation("StateChangerNet");
            //NetInstance scInstance = sim.initSimulation(topFrame.STATE_CHANGER_NET_NAME);
            //as per Olaf's (of Renew.de) instructions:
            NetInstance scInstance = sim.initSimulation(null);
            //this.scNetAccessor = new StateChangerNetAccessor(designEntity, parentPG, topFrame);
            
            /** this next step is counter-intuitive: you need to run the sim before you load data into the net,
             * otherwise the sim doesn't run automatically */
            sim.simulationRun();
            addAllData();
            petriNetApp.openInstanceDrawing(scInstance);
            //this.scNet = scInstance.getNet();
            //this.loopNet = loopInstance.getNet();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addAllData() {
        int polsCount = addAllPoliciesToNet();
        /** as policies are added, polsCount is incremented */
        scNetAccessor.setPolsNum(polsCount);
        addAllRolesToNet();
        addAllInputsToNet();
        scNetAccessor.addPeerRoles(peerRoles);
        scNetAccessor.addStateChanger(this);
    }
    
    /** Handles all the things that occur when and IF the StateChangerNet fires its final transition */
    public void changeEntityStateFromNet(ContentStorage peerRoles) {
        setPeerRoles(peerRoles);
        //advUtils.createTaskStateAdvertisement(designEntity, stateName, parentPG);
        advUtils.createHistoryAdvertisement(designEntity, transName, nextStateName, peerRoles, parentPG);
        //
        topFrame.printMessage("NOTE: Entity " + designEntity.getFullName() + " has changed state to: " + nextStateName);
        topFrame.printMessage("A new history advertisement has been created");
        //topFrame.createInfoDialog("", this);
    }
    
    public void test() {
        if(scNetAccessor != null) {
            scNetAccessor.addRole("r1", "r2", "r3", "r4");
            scNetAccessor.addInput("i1", "i2", "i3", "i4");
            scNetAccessor.addPol("p1", "p2");
            scNetAccessor.setPolsNum(5);
            //scNetAccessor.setCurTrans("curTransTest");
        }
        else {
            System.out.println("ERROR: scNetAccessor is null");
        }
    }
    
    public int addAllPoliciesToNet() {
        int polsCount = 0;
        if(appUser.policiesExist(designEntity)) {
            String nextTrans = designEntity.getNextTransition();
            for (Iterator i = appUser.getPoliciesIterator(designEntity); i.hasNext(); ) {
                PolicyAdvertisement policyAdv = (PolicyAdvertisement)i.next();
                /** Only polAdvs relevant to the nextTrans */
                /** this constraint could be omitted and pnet would still work */
                if(nextTrans.equals(policyAdv.getTransName())) {
                    for(Iterator j = policyAdv.getRoles().iterator(); j.hasNext(); ) {
                        String roleName = (String)j.next();
                        scNetAccessor.addOneTransRoleToNet(nextTrans, roleName);
                        System.out.println("TEST: found a transRole to add. TransName: " + nextTrans + ". RoleName: " + roleName);
                        polsCount++;
                    }
                }
            }
        }
        else {
            System.out.println("ERROR: policies don't exist for this design entity");
        }
        return polsCount;
    }
    
    /** add all roleAdv related to this design entity */
    public void addAllRolesToNet() {
        if(appUser.rolesExist(designEntity)) {
            for (Iterator i = appUser.getRolesIterator(designEntity); i.hasNext(); ) {
                RoleAdvertisement roleAdv = (RoleAdvertisement)i.next();
                System.out.println("TEST: found a roleAdv to add. Role: " + roleAdv.getRoleName());
                scNetAccessor.addOneDesignEntityRelatedAdvToNet(roleAdv);
            }
        }
        else {
            System.out.println("ERROR: roles don't exist for this design entity");
        }
    }
    
    /** Add only those inputs that relate to the current nextTrans for this design entity */
    public void addAllInputsToNet() {
        if(appUser.inputsExist(designEntity)) {
            for (Iterator i = appUser.getInputsIterator(designEntity); i.hasNext(); ) {
                InputAdvertisement inputAdv = (InputAdvertisement)i.next();
                /** this constraint could be omitted and pnet would still work */
                if (transName.equals(inputAdv.getTransName())) {
                    System.out.println("TEST: found a inputAdv to add. TransName: " + inputAdv.getTransName());
                    scNetAccessor.addOneDesignEntityRelatedAdvToNet(inputAdv);
                }
            }
        }
        else {
            System.out.println("ERROR: inputs don't exist for this task");
        }
    }
  
  
    /** Test things HERE */
    public static void main(String[] args) {
        //TaskStateChanger tsc = new TaskStateChanger();
    }
    
    
    /** Getter for property transName.
     * @return Value of property transName.
     *
     */
    public java.lang.String getTransName() {
        return transName;
    }
    
    /** Setter for property transName.
     * @param transName New value of property transName.
     *
     */
    public void setTransName(java.lang.String transName) {
        this.transName = transName;
    }
    
    /** Getter for property parentPG.
     * @return Value of property parentPG.
     *
     */
    public net.jxta.peergroup.PeerGroup getParentPG() {
        return parentPG;
    }
    
    /** Setter for property parentPG.
     * @param parentPG New value of property parentPG.
     *
     */
    public void setParentPG(net.jxta.peergroup.PeerGroup parentPG) {
        this.parentPG = parentPG;
    }
    
    /** Getter for property peerRoles.
     * @return Value of property peerRoles.
     *
     */
    public dpm.content.ContentStorage getPeerRoles() {
        return peerRoles;
    }
    
    /** Setter for property peerRoles.
     * @param peerRoles New value of property peerRoles.
     *
     */
    public void setPeerRoles(dpm.content.ContentStorage peerRoles) {
        this.peerRoles = peerRoles;
    }
    
    /** Getter for property nextStateName.
     * @return Value of property nextStateName.
     *
     */
    public java.lang.String getNextStateName() {
        return nextStateName;
    }
    
    /** Setter for property nextStateName.
     * @param nextStateName New value of property nextStateName.
     *
     */
    public void setNextStateName(java.lang.String nextStateName) {
        this.nextStateName = nextStateName;
    }
    
    /** Getter for property designEntity.
     * @return Value of property designEntity.
     *
     */
    public dpm.content.DesignEntity getDesignEntity() {
        return designEntity;
    }
    
    /** Setter for property designEntity.
     * @param designEntity New value of property designEntity.
     *
     */
    public void setDesignEntity(dpm.content.DesignEntity designEntity) {
        this.designEntity = designEntity;
    }
    
}
