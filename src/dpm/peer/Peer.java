/*
 * Peer.java
 *
 * Created on September 10, 2003, 2:32 PM
 */

package dpm.peer;

import dpm.content.ContentStorage;
import dpm.content.DesignEntity;
import dpm.content.EntityRelatedContentStorage;
import dpm.content.advertisement.IPGMemberAdvertisement;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.*;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.OutputPipeListener;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;


/**
 *
 * @author  cumming
 */
public class Peer implements PipeMsgListener, OutputPipeListener, DpmTerms { //NOTE: not a subclass
    private PeerID peerID;
    private PeerGroup rootPG;
    private PeerAdvertisement peerAdv;
    private String name;
    private String localAddress;
    private DpmAppTopFrame topFrame;
    private int port = -1;
    private Comparator comparator = null; //used for ordering Peers
    private EndpointAddress address;
    private String storageKey; //for storing in ContentStorage
    /**for holding peerAdvs */
    private ContentStorage peers;
    /**for holding member adv. Keyed by parentPG_ID.toString() */
    private ContentStorage members;
    /** for holding pgAdvs */
    private ContentStorage csAdvPG;
    /** for holding instantiated PGs advs */
    private ContentStorage csAdvIPG;
    /** for holding instantiated PGs */
    private ContentStorage csPG;
    /** for holding tasks */
    //private ContentStorage tasks;
    /** for holding products */
    //private ContentStorage products;
    /** for holding userNamedEntities */
    private ContentStorage userNamedEntities;
    /** for holding linkAdvs */
    //    private ContentStorage links;
    /** for holding loopNetAdvs */
    private ContentStorage loopNets;
    /** for holding deleteAdvs */
    private ContentStorage deleteAdvs;
    //     /** for holding abandonedEntities */
    //    private ContentStorage abandonedEntities;
    /** for holding chatAdvs */
    private ContentStorage chatAdvs;
    /** for holding privateChatAdvs */
    private ContentStorage privateChatAdvs;
    /** for holding stateNetAdvs */
    //private ContentStorage stateNets;
    private EntityRelatedContentStorage entityRelatives;
    private Set joinedPGs;
    
    
    /** Creates a new instance of Peer */
    public Peer(PeerGroup rootPG, DpmAppTopFrame topFrame) {
        this.rootPG = rootPG;
        this.topFrame = topFrame;
        this.name = rootPG.getPeerName(); //gets set automatically
        this.peerID = rootPG.getPeerID();
        this.peerAdv = rootPG.getPeerAdvertisement();
        //this.description = description;
        this.localAddress = getLocalIPaddress();
        this.port = getAvailablePort();
        this.joinedPGs = new HashSet();
        /** Required content storages for the Peer */
        this.peers = new ContentStorage("net.jxta.impl.protocol.PeerAdv", this);
        /**Members of all peergroups */
        this.members = new ContentStorage("dpm.content.advertisement.IPGMemberAdvertisement", this);
        this.csAdvPG = new ContentStorage("net.jxta.impl.protocol.PeerGroupAdv", this);
        this.csAdvIPG = new ContentStorage("dpm.content.advertisement.IPGAdvertisement", this);
        this.csPG = new ContentStorage("net.jxta.impl.peergroup.PeerGroupInterface", this);
        
        /** Keyed by id.toString() of the advToDelete */
        this.deleteAdvs = new ContentStorage("dpm.content.advertisement.DeleteAdvertisement", this);
        /** Keyed by entityID.toString() */
        this.entityRelatives = new EntityRelatedContentStorage(this, topFrame);
        
        /**@since Sept.10.2004 */
        /** Keyed by parentPG_ID.toString() */
        this.userNamedEntities = new ContentStorage("dpm.content.designEntity.UserNamedEntity", this);
        /** Keyed by parentPG_ID.toString() */
        this.loopNets = new ContentStorage("dpm.content.advertisement.net.LoopNetAdvertisement", this);
        /** Keyed by parentPG_ID.toString() */
        this.chatAdvs = new ContentStorage("dpm.content.advertisement.chat.ChatAdvertisement", this);
        /** Keyed by parentPG_ID.toString() */
        this.privateChatAdvs = new ContentStorage("dpm.content.advertisement.chat.PrivateChatAdvertisement", this);
    }
    
    /**NOTE: Convenience location only. Checks if a pgAdv has been deleted */
    public boolean isDeleted(PeerGroupAdvertisement pgAdv) {
        String storageKey = pgAdv.getPeerGroupID().toString();
        return getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    public boolean policiesExist(DesignEntity entity) {
        return getPoliciesIterator(entity) != null;
    }
    public boolean rolesExist(DesignEntity entity) {
        return getRolesIterator(entity) != null;
    }
    public boolean inputsExist(DesignEntity entity) {
        return getInputsIterator(entity) != null;
    }
    public boolean linksExist(DesignEntity entity) {
        return getLinksIterator(entity) != null;
    }
    public boolean historiesExist(DesignEntity entity) {
        return getHistoriesIterator(entity) != null;
    }
  
    
    /** Gets one 'row' of policies table */
    public Iterator getPoliciesIterator(DesignEntity entity) {
        return getEntityRelatives().getPolicies().iterator(entity.getDesignEntityID().toString());
    }
    /** Gets one 'row' of roles table related to this designEntity */
    public Iterator getRolesIterator(DesignEntity entity) {
        return getEntityRelatives().getRoles().iterator(entity.getDesignEntityID().toString());
    }
    /** Gets one 'row' of inputs table related to this designEntity */
    public Iterator getInputsIterator(DesignEntity entity) {
        return getEntityRelatives().getInputs().iterator(entity.getDesignEntityID().toString());
    }
    /** Gets one 'row' of links table related to this designEntity */
    public Iterator getLinksIterator(DesignEntity entity) {
        return getEntityRelatives().getAllLinks().iterator(entity.getDesignEntityID().toString());
    }
    /** Gets one 'row' of history table related to this designEntity */
    public Iterator getHistoriesIterator(DesignEntity entity) {
        return getEntityRelatives().getHistories().iterator(entity.getDesignEntityID().toString());
    }
  
    /** for debugging purposes */
    private void printPeerAdvDoc(PeerAdvertisement peerAdv) {
        if(peerAdv.getDocument(MimeMediaType.XMLUTF8)==null) {
            return;
        }
        System.out.println("peerAdv printout: " + peerAdv.getDocument(MimeMediaType.XMLUTF8).toString());
    }
    
    /** @deprecated */
    private void addPGToPeerAdv(PeerGroup pg, String parentName) {
        //the thing that keys it:
        ModuleClassID key = rootPG.peerGroupClassID;
        String pgName = pg.getPeerGroupName();
        StructuredDocument oldDoc = peerAdv.getServiceParam(key);
        Element newDoc = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, pgName, parentName);
        if(oldDoc==null) {
            peerAdv.putServiceParam(key, newDoc); //keyed by ModuleClassID
        }
        else { //oldDoc exists
            Element e = oldDoc.createElement(pgName, parentName); //element is keyed by pgName
            oldDoc.appendChild(e);
            peerAdv.putServiceParam(key, oldDoc);
        }
    }
    
    /** @deprecated */
    public void savePeerAdvLocally(PeerAdvertisement pAdv) {
        try {
            //save it to the local cache
            DiscoveryService discSvc = rootPG.getDiscoveryService();
            if (discSvc == null) {
                System.out.println("Null discSvc");
                return;
            }
            discSvc.publish(pAdv, DiscoveryService.PEER);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /** needed by OutputPipeListener */
    public void outputPipeEvent(OutputPipeEvent event) {
    }
    
    /** needed by PipeMsgListener */
    public void pipeMsgEvent(PipeMsgEvent event) {
        //see: JxtaBidiPipeExample.java in Sun jxta tutorials
        Message message = null;
        MessageElement pnContent = null; //the petrinet part of the message
        MessageElement textContent = null; //the descriptive text part of the message
        //
        try {
            // grab the message from the event
            message = event.getMessage();
            if (message == null) {
                printMessage("Received an empty message, returning");
                return;
            }
            printMessage("Received a message response :" + message);
            // get the message elements
            pnContent = message.getMessageElement(null, "pnContent");
            textContent = message.getMessageElement(null, "textContent");
            // Get message
            if ((pnContent.toString() == null) || (textContent.toString() == null)) {
                printMessage("Parts of the received message are null" );
            } else {
                Date date = new Date(System.currentTimeMillis());
                printMessage("Message received at :" + date.toString());
                printMessage("Message : " + textContent.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
    
    public void printMessage(String s) {
        topFrame.printMessage(s);
    }
    
    public String getLocalIPaddress() {
        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            return localAddress.getHostAddress(); //returns a String in form "130.161.162.233"
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public int getAvailablePort() {
        try {
            //open up a new socket just to get an available port number
            ServerSocket ssock = new ServerSocket(0); // 0 to generate auto number
            int portNum = ssock.getLocalPort();
            //System.out.println(portNum); output correctly ie 45665
            ssock.close();
            return portNum;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public boolean joinedPGAlready(PeerGroup pg) {
        return joinedPGs.contains(pg);
    }
    
    public boolean memberAdvForCorrectPG(IPGMemberAdvertisement memberAdv, PeerGroup pg) {
        return memberAdv.getPeerGroupID().equals(pg.getPeerGroupID());
    }
    
    /**Leave a peergroup in DPM = delete all IPGMemberAdvertisements in a peergroup.
     *NOTE: does not leave w.r.t. JXTA.
     *Therefore, all members who have viewed a DPM peergroup remain members.
     *@since 25 Nov. 2004 */
    public void leavePeerGroup(Set memberAdvsToDelete, PeerGroup pg) {
        /**Does deleting a memberAdv mean that peer can't join later?
         * No just join again and create a new memberAdv */
        if(memberAdvsToDelete != null) {
            for(Iterator i = memberAdvsToDelete.iterator(); i.hasNext(); ) {
                IPGMemberAdvertisement memberAdv = (IPGMemberAdvertisement)i.next();
                if(memberAdvForCorrectPG(memberAdv, pg)) {
                    topFrame.getAdvUtils().createDeleteAdvertisement(memberAdv, pg);
                }
            }
        }
    }
    
    /**@since 25 Nov 2004 */
    public boolean  peerIsAlreadyMember(PeerGroup pg) {
        /**If there are existing memberAdvs, then peer is already a member of pg */
        return !topFrame.getAdvUtils().retrieveExistingMemberAdvs(this, pg).isEmpty();
    }
    
    /**Join a peergroup in DPM = create a IPGMemberAdvertisement in a peergroup
     * @since 25 Nov. 2004 */
    public void joinPeerGroup(PeerGroup pg) {
        try {
            topFrame.getAdvUtils().createMemberAdvertisement(this, pg);
            /**Join peergroup as per JXTA */
            joinPGJxta(pg);
            topFrame.showMessageDialog(name + " has successfully joined peergroup " +
            pg.getPeerGroupName(), topFrame);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**JXTA method of joinging a peergroup */
    private void joinPGJxta(PeerGroup pg) {
        String pgName = pg.getPeerGroupName();
        
        if(joinedPGAlready(pg)) {
            topFrame.showMessageDialog("Peer: " + name + " is already a member of " + pgName, topFrame);
            return;
        }
        //see: Jxta Programming Guide p.55
        printMessage(name + " is joining peergroup " + pgName);
        StructuredDocument creds = null;
        try {
            //Generate the credentials for the Peergroup
            AuthenticationCredential authCred = new AuthenticationCredential(pg, null, creds);
            //get the MembershipService from the peergroup
            MembershipService membership = pg.getMembershipService();
            //get the Authenticator from the Authentication creds
            Authenticator authenticator = membership.apply(authCred);
            //check if everything is okay to join the group
            if(authenticator.isReadyForJoin()) {
                Credential myCred = membership.join(authenticator);
                joinedPGs.add(pg);
                DiscoveryService discoverySvc = pg.getDiscoveryService();
                if(discoverySvc != null) {
                    discoverySvc.publish(peerAdv, DiscoveryService.PEER);
                    discoverySvc.remotePublish(peerAdv, DiscoveryService.PEER);
                }
            }
            else
                topFrame.showErrorDialog(name + " has failed to join peergroup " + pgName, topFrame);
        }
        catch (Exception e) {
            topFrame.showErrorDialog("Failure in authentication when trying to join peergroup " + pgName, topFrame);
            e.printStackTrace();
        }
    }
    
    public String getJoinedPGString(PeerGroup pg) {
        if (joinedPGAlready(pg)) {
            return new String(" [joined]");
        }
        return EMPTY_STRING;
    }
    
    /**Converts a string representing iteration to an int, incrementing it, then turning it back into a string */
    public String updateIteration(String current) {
        //System.out.println("Current iteration: " + current);
        int curInt = Integer.valueOf(current).intValue();
        String next = (String)String.valueOf(curInt + 1);
        //System.out.println("Next iteration: " + next);
        return next;
    }
    
    public Comparator getComparator() {
        return comparator;
    }
    
    /** Setter for property comparator.
     * @param comparator New value of property comparator.
     *
     */
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public java.lang.String getName() {
        return name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    
    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public int getPort() {
        return port;
    }
    
    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /** Getter for property localAddress.
     * @return Value of property localAddress.
     *
     */
    public java.lang.String getLocalAddress() {
        return localAddress;
    }
    
    /** Setter for property localAddress.
     * @param localAddress New value of property localAddress.
     *
     */
    public void setLocalAddress(java.lang.String localAddress) {
        this.localAddress = localAddress;
    }
    
    /** Getter for property peerID.
     * @return Value of property peerID.
     *
     */
    public PeerID getPeerID() {
        return peerID;
    }
    
    /** Setter for property peerID.
     * @param peerID New value of property peerID.
     *
     */
    public void setPeerID(PeerID peerID) {
        this.peerID = peerID;
    }
    
    /** Getter for property csAdvPG.
     * @return Value of property csAdvPG.
     *
     */
    public ContentStorage getCsAdvPG() {
        return csAdvPG;
    }
    
    /** Setter for property csAdvPG.
     * @param csAdvPG New value of property csAdvPG.
     *
     */
    public void setCsAdvPG(ContentStorage csAdvPG) {
        this.csAdvPG = csAdvPG;
    }
    
    
    /** Getter for property topFrame.
     * @return Value of property topFrame.
     *
     */
    public DpmAppTopFrame getTopFrame() {
        return topFrame;
    }
    
    /** Setter for property topFrame.
     * @param topFrame New value of property topFrame.
     *
     */
    public void setTopFrame(DpmAppTopFrame topFrame) {
        this.topFrame = topFrame;
    }
    
    /** Getter for property csAdvIPG.
     * @return Value of property csAdvIPG.
     *
     */
    public ContentStorage getCsAdvIPG() {
        return csAdvIPG;
    }
    
    /** Setter for property csAdvIPG.
     * @param csAdvIPG New value of property csAdvIPG.
     *
     */
    public void setCsAdvIPG(ContentStorage csAdvIPG) {
        this.csAdvIPG = csAdvIPG;
    }
    
    /** Getter for property csPG.
     * @return Value of property csPG.
     *
     */
    public ContentStorage getCsPG() {
        return csPG;
    }
    
    /** Setter for property csPG.
     * @param csPG New value of property csPG.
     *
     */
    public void setCsPG(ContentStorage csPG) {
        this.csPG = csPG;
    }
    
    /** Getter for property peers.
     * @return Value of property peers.
     *
     */
    public ContentStorage getPeers() {
        return peers;
    }
    
    /** Setter for property peers.
     * @param peers New value of property peers.
     *
     */
    public void setPeers(ContentStorage peers) {
        this.peers = peers;
    }
    
    /** Getter for property loopNets.
     * @return Value of property loopNets.
     *
     */
    public ContentStorage getLoopNets() {
        return loopNets;
    }
    
    /** Setter for property loopNets.
     * @param loopNets New value of property loopNets.
     *
     */
    public void setLoopNets(ContentStorage loopNets) {
        this.loopNets = loopNets;
    }
    
    /** Getter for property entityRelatives.
     * @return Value of property entityRelatives.
     *
     */
    public EntityRelatedContentStorage getEntityRelatives() {
        return entityRelatives;
    }
    
    /** Setter for property entityRelatives.
     * @param entityRelatives New value of property entityRelatives.
     *
     */
    public void setEntityRelatives(EntityRelatedContentStorage entityRelatives) {
        this.entityRelatives = entityRelatives;
    }
    
    /** Getter for property deleteAdvs.
     * @return Value of property deleteAdvs.
     *
     */
    public ContentStorage getDeleteAdvs() {
        return deleteAdvs;
    }
    
    /** Setter for property deleteAdvs.
     * @param deleteAdvs New value of property deleteAdvs.
     *
     */
    public void setDeleteAdvs(ContentStorage deleteAdvs) {
        this.deleteAdvs = deleteAdvs;
    }
    
    /** Getter for property userNamedEntities.
     * @return Value of property userNamedEntities.
     *
     */
    public ContentStorage getUserNamedEntities() {
        return userNamedEntities;
    }
    
    /** Setter for property userNamedEntities.
     * @param userNamedEntities New value of property userNamedEntities.
     *
     */
    public void setUserNamedEntities(ContentStorage userNamedEntities) {
        this.userNamedEntities = userNamedEntities;
    }
    
    /** Getter for property chatAdvs.
     * @return Value of property chatAdvs.
     *
     */
    public ContentStorage getChatAdvs() {
        return chatAdvs;
    }
    
    /** Setter for property chatAdvs.
     * @param chatAdvs New value of property chatAdvs.
     *
     */
    public void setChatAdvs(ContentStorage chatAdvs) {
        this.chatAdvs = chatAdvs;
    }
    
    /** Getter for property privateChatAdvs.
     * @return Value of property privateChatAdvs.
     *
     */
    public ContentStorage getPrivateChatAdvs() {
        return privateChatAdvs;
    }
    
    /** Setter for property privateChatAdvs.
     * @param privateChatAdvs New value of property privateChatAdvs.
     *
     */
    public void setPrivateChatAdvs(ContentStorage privateChatAdvs) {
        this.privateChatAdvs = privateChatAdvs;
    }
    
    /**
     * Getter for property members.
     * @return Value of property members.
     */
    public ContentStorage getMembers() {
        return members;
    }
    
    /**
     * Setter for property members.
     * @param members New value of property members.
     */
    public void setMembers(ContentStorage members) {
        this.members = members;
    }
    
}
