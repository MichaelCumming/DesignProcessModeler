/*
 * Searcher.java
 *
 * Created on November 26, 2003, 4:29 PM
 */

package dpm.content;

import dpm.container.tree.LinkTree;
import dpm.container.tree.LinkTreeNode;
import dpm.container.tree.PGTree;
import dpm.container.tree.PGTreeNode;
import dpm.content.advertisement.DeleteAdvertisement;
import dpm.content.advertisement.IPGAdvertisement;
import dpm.content.advertisement.IPGMemberAdvertisement;
import dpm.content.advertisement.chat.ChatAdvertisement;
import dpm.content.advertisement.chat.PrivateChatAdvertisement;
import dpm.content.advertisement.designEntity.DesignEntityRelatedAdv;
import dpm.content.advertisement.designEntity.UserNamedEntityAdv;
import dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.content.constraint.Link;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.dpmApp.desktop.subpages.*;
import dpm.peer.Peer;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.tree.*;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;



/**
 * The utility that searches locally and remotely for JXTA/DPM content.
 * @author  cumming
 * @since November 26, 2003, 4:29 PM
 */
//Searches for something. keeps results in various ContentStorage
//duplicates the functionality of DpmPeerPages etc...
public class ContentSearcherTree implements Runnable, DiscoveryListener, DpmTerms {
    private PGTreeNode baseNode;
    private LinkTreeNode baseLinkNode;
    private DpmAppTopFrame topFrame;
    private Peer appUser;
    private PeerGroup basePG;
    private DiscoveryService discSvc;
    private PGTree tree;
    private LinkTree linkTree;
    private Thread thread;
    protected boolean remoteSearching;
    
    
    /** Creates a new instance of Searcher */
    //standard usage: starts and looks for remote advertisments, then prints them out for each cycle
    //at contruction: remoteSearching is OFF
    public ContentSearcherTree(PGTreeNode baseNode, PGTree tree, DpmAppTopFrame topFrame, boolean remoteSearching) {
        this.baseNode = baseNode;
        this.basePG = ((DisplayUserObject)baseNode.getUserObject()).getPeerGroup();
        this.discSvc = basePG.getDiscoveryService();
        this.tree = tree;
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
        this.remoteSearching = false; //remoteSearching; //whether running or not
        //this.thread = new Thread(this);
        //startThread();
    }
    
    /**Simplified constructor used for LinkTreeNodes */
    public ContentSearcherTree(PeerGroup basePG, DpmAppTopFrame topFrame) {
        this.basePG = basePG;
        this.topFrame = topFrame;
        this.discSvc = basePG.getDiscoveryService();
    }
    
    private void startThread() {
        if(thread == null) {
            topFrame.printMessage(">>ERROR: attempted to startThread() with CST.thread == null");
            return;
        }
        thread.start();
        int numT = topFrame.getNumThreads();
        numT++;
        topFrame.setNumThreads(numT);
        topFrame.printMessage(">>Number of active threads in CST: " + numT);
    }
    
    /**@since Dec 10, 2004 */
    //    public void updateContentsOnce() {
    //        getRemoteAdvsAllTypes();
    //        localCache_To_csAdvAllTypes(basePG);
    //    }
    
    //the main loop that looks for advs that populate the cache
    public void run() {
        System.out.println(">>CST entering run() method");
        try {
            /** add ourselves as a DiscoveryListener for DiscoveryResponse events */
            if(discSvc != null) {
                discSvc.addDiscoveryListener(this);
            }
            //printMessage("Remote searcher now running in PG: " + basePG.getPeerGroupNameX());
            while(remoteSearching) { //loop when remoteSearching
                getRemoteAdvsAllTypes();
                //wait before sending out next discovery message
                try {
                    //wait 30 seconds
                    Thread.sleep(WAIT_TIME);
                    localCache_To_csAdvAllTypes();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**This takes time */
    public void getRemoteAdvsAllTypes() {
        if(discSvc == null) {
            System.out.println("ERROR: null discSvc when getting remote advs");
            return;
        }
        discSvc.getRemoteAdvertisements(null, DiscoveryService.ADV, null, null, 10, this);
        discSvc.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, 10, this);
        discSvc.getRemoteAdvertisements(null, DiscoveryService.GROUP, null, null, 10, this);
    }
    
    public void localCache_To_csAdvAllTypes() {
        
        /**Give priority to delete advs */
        populateDeleteAdvs();
        localCache_To_csAdv(DiscoveryService.ADV);
        localCache_To_csAdv(DiscoveryService.PEER);
        localCache_To_csAdv(DiscoveryService.GROUP);
    }
    
    /**Populates the content storage DeleteAdvs */
    public void populateDeleteAdvs() {
        try {
            Enumeration localAdvs = discSvc.getLocalAdvertisements(DiscoveryService.ADV, null, null);
            if(localAdvs != null) {
                /**First step: populate the deleteAdvs: this tells you what is allowed into other CSs */
                for(Enumeration e = localAdvs; e.hasMoreElements(); ) {
                    Object obj = e.nextElement();
                    if(obj instanceof DeleteAdvertisement) {
                        DeleteAdvertisement deleteAdv = (DeleteAdvertisement)obj;
                        sendObjToContentStorage(deleteAdv);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /** find LOCAL advs and puts them into ContentStorage
     * for a single basePG
     * adds to the existing advs in storage
     * does only advs; therefore FAST */
    //OK
    public void localCache_To_csAdv(int jxtaType) {
        try {
            Enumeration localAdvs = discSvc.getLocalAdvertisements(jxtaType, null, null);
            if(localAdvs != null) {
                for(Enumeration e = localAdvs; e.hasMoreElements(); ) {
                    Object obj = e.nextElement();
                    sendObjToContentStorage(obj);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**Forces a broadcast of all advertisements in a peergroup
     * @since 26 nov 2004 */
    public void broadcaseLocalCacheAllTypes() {
        broadcastLocalCache(DiscoveryService.ADV);
        broadcastLocalCache(DiscoveryService.PEER);
        broadcastLocalCache(DiscoveryService.GROUP);
    }
    
    /**Forces a broadcast of all advertisements in a peergroup
     * @since 26 nov 2004 */
    public void broadcastLocalCache(int jxtaType) {
        try {
            Enumeration localAdvs = discSvc.getLocalAdvertisements(jxtaType, null, null);
            if(localAdvs != null) {
                for(Enumeration e = localAdvs; e.hasMoreElements(); ) {
                    Advertisement adv = (Advertisement)e.nextElement();
                    if(adv != null) {
                        discSvc.publish(adv, jxtaType);
                        discSvc.remotePublish(adv, jxtaType);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /** send any kind of advertisement to content storage */
    private void sendObjToContentStorage(Object obj) {
        if (obj == null) {
            return;
        }
        Peer appUser = topFrame.getAppUser();
        
        /**It is important to add DeleteAdvertisements first */
        if (obj instanceof DeleteAdvertisement) {
            DeleteAdvertisement deleteAdv = (DeleteAdvertisement)obj;
            appUser.getDeleteAdvs().addDeleteAdv(deleteAdv);
            return;
        }
        if (obj instanceof LoopNetAdvertisement) {
            LoopNetAdvertisement loopNetAdv = (LoopNetAdvertisement)obj;
            if(!loopNetAdv.isDeleted(appUser)) {
                appUser.getLoopNets().addLoopNetAdv(loopNetAdv, basePG);
            }
            return;
        }
        /**@since 25 Nov. 2004 */
        if (obj instanceof IPGMemberAdvertisement) {
            IPGMemberAdvertisement memberAdv = (IPGMemberAdvertisement)obj;
            if(!memberAdv.isDeleted(appUser)) {
                appUser.getMembers().addMemberAdv(memberAdv, basePG);
            }
            return;
        }
        if (obj instanceof IPGAdvertisement) {
            IPGAdvertisement ipgAdv = (IPGAdvertisement)obj;
            if(!ipgAdv.isDeleted(appUser)) {
                appUser.getCsAdvIPG().addIPGAdv(ipgAdv, basePG);
            }
            return;
        }
        if (obj instanceof PeerGroupAdvertisement) {
            PeerGroupAdvertisement pgAdv = (PeerGroupAdvertisement)obj;
            if(!appUser.isDeleted(pgAdv)) {
                appUser.getCsAdvPG().addPgAdv(pgAdv, basePG);
            }
            return;
        }
        /**@since Sept.10.2004 */
        if (obj instanceof UserNamedEntityAdv) {
            UserNamedEntityAdv uneAdv = (UserNamedEntityAdv)obj;
            String netName = uneAdv.getNetName();
            String entityType = uneAdv.getEntityType();
            LoopNetAdvertisement loopAdv = appUser.getLoopNets().getLoopNetByName(netName);
            if(loopAdv == null) {
                //System.out.println("ERROR: loopAdv is null in content searcher tree");
                return;
            }
            if(!uneAdv.isDeleted(appUser)) {
                appUser.getUserNamedEntities().addUserNamedEntityAdv(uneAdv, loopAdv, basePG, entityType, baseNode);
            }
            return;
        }
        /**NOTE: LinkAdvertisement must come before DesignEntityRelatedAdv,
         * since linkAdv is a sub-class of DesignEntityRelatedAdv */
        if (obj instanceof LinkAdvertisement) {
            LinkAdvertisement linkAdv = (LinkAdvertisement)obj;
            Link newLink = new Link(linkAdv, appUser);
            
            if(!linkAdv.isDeleted(appUser)) {
                appUser.getEntityRelatives().addLinkToAllLinks(newLink, basePG);
                appUser.getEntityRelatives().addLinkToIncomingOutgoing(newLink);
            }
            return;
        }
        if (obj instanceof DesignEntityRelatedAdv) {
            DesignEntityRelatedAdv entityRelatedAdv = (DesignEntityRelatedAdv)obj;
            /** Note that entity relatives are not stored according to basePG's name, but by designEntityID */
            if(!entityRelatedAdv.isDeleted(appUser)) {
                appUser.getEntityRelatives().addRelative(entityRelatedAdv, baseNode);
            }
            return;
        }
        /**@since 24 Sept 2004 */
        if (obj instanceof ChatAdvertisement) {
            ChatAdvertisement chatAdv = (ChatAdvertisement)obj;
            /** Note that entity relatives are not stored according to basePG's name, but by designEntityID */
            
            if(!chatAdv.isDeleted(appUser)) {
                appUser.getChatAdvs().addChatAdv(chatAdv, basePG);
            }
            return;
        }
        if (obj instanceof PrivateChatAdvertisement) {
            PrivateChatAdvertisement chatAdv = (PrivateChatAdvertisement)obj;
            /** Note that entity relatives are not stored according to basePG's name, but by designEntityID */
            
            if(chatAdv.isForThisAppUser(appUser) &&
            !chatAdv.isDeleted(appUser)) {
                appUser.getPrivateChatAdvs().addPrivateChatAdv(chatAdv, basePG);
            }
            return;
        }
    }
    
    /**Handles advs that arrive and adds them into adv ContentStorage */
    public void discoveryEvent(DiscoveryEvent event) {
        //System.out.println("Discovered incoming advs for peergroup: " + basePG.getPeerGroupNameX());
        //topFrame.printMessage("Discovered incoming advs for peergroup: " + basePG.getPeerGroupNameX());
        DiscoveryResponseMsg response = event.getResponse();
        
        if (response.getDiscoveryType() == DiscoveryService.PEER){
            //get the responding peer's advertisement
            PeerAdvertisement peerAdv = response.getPeerAdvertisement();
            sendObjToContentStorage(peerAdv);
        }
        else {
            Enumeration e = response.getAdvertisements();
            int num = 0;
            if(e != null) {
                while(e.hasMoreElements()) {
                    Object obj = e.nextElement();
                    /** Send all advertisement EXCEPT PeerAdvertisements to content storage.
                     ** PeerAdvs are handled above. */
                    if(!(obj instanceof PeerAdvertisement)) {
                        sendObjToContentStorage(obj);
                        num++;
                    }
                }
            }
            topFrame.printMessage(
            "Discovered " + num + " incoming advs for peergroup: " + basePG.getPeerGroupName());
        }
    }
    
    public String getStatusDescription() {
        if(remoteSearching) {
            return new String("remoteSearching");
        }
        return new String("inactive");
    }
    
    public void printMessage(String s) {
        if(topFrame == null) {
            System.out.println("ERROR: null topFrame in ContentSearcherTree: printMessage");
        }
        else {
            topFrame.printMessage(s);
        }
    }
    
    /** Getter for property thread.
     * @return Value of property thread.
     *
     */
    public java.lang.Thread getThread() {
        return thread;
    }
    
    /** Setter for property thread.
     * @param thread New value of property thread.
     *
     */
    public void setThread(java.lang.Thread thread) {
        this.thread = thread;
    }
    
    
    
    /** Getter for property basePG.
     * @return Value of property basePG.
     *
     */
    public PeerGroup getBasePG() {
        return basePG;
    }
    
    /** Setter for property basePG.
     * @param basePG New value of property basePG.
     *
     */
    public void setBasePG(PeerGroup basePG) {
        this.basePG = basePG;
    }
    
    
    /** Getter for property remoteSearching.
     * @return Value of property remoteSearching.
     *
     */
    public boolean isRemoteSearching() {
        return remoteSearching;
    }
    
    /** Setter for property remoteSearching.
     * @param remoteSearching New value of property remoteSearching.
     *
     */
    //NOTE: important
    public void setRemoteSearching(boolean setToActive) {
        boolean wasActive = this.isRemoteSearching();
        this.remoteSearching = setToActive;
        
        if(wasActive && setToActive) {
            //if turning ON already remoteSearching thread
            //printMessage("ERROR: attempted to turn on already active thread for pg: " + basePG.getPeerGroupNameX());
            return;
        }
        if(!wasActive && !setToActive) {
            //if turning OFF inactive thread
            //printMessage("ERROR: attempted to turn off already inactive thread for pg: " + basePG.getPeerGroupNameX());
            return;
        }
        if(wasActive && !setToActive) {
            //if turning OFF active thread
            //thread.destroy();
            //thread = null;
            thread = new Thread(this);
            //printMessage("Turned off remote searching in pg: " + basePG.getPeerGroupNameX());
            return;
        }
        if(!wasActive && setToActive) {
            //if turning ON inactive thread
            //startThread();
            //printMessage("Turned on remote searching in pg: " + basePG.getPeerGroupNameX());
            return;
        }
    }
    
    /** Getter for property tree.
     * @return Value of property tree.
     *
     */
    public PGTree getTree() {
        return tree;
    }
    
    /** Setter for property tree.
     * @param tree New value of property tree.
     *
     */
    public void setTree(PGTree tree) {
        this.tree = tree;
    }
    
    /**
     * Getter for property baseNode.
     * @return Value of property baseNode.
     */
    public dpm.container.tree.PGTreeNode getBaseNode() {
        return baseNode;
    }
    
    /**
     * Setter for property baseNode.
     * @param baseNode New value of property baseNode.
     */
    public void setBaseNode(dpm.container.tree.PGTreeNode baseNode) {
        this.baseNode = baseNode;
    }
    
}
