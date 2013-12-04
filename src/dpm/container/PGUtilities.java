/*
 * PeerGroupUtilities.java
 *
 * Created on December 5, 2003, 10:49 AM
 */

package dpm.container;

import dpm.content.ContentStorage;
import dpm.content.advertisement.IPGAdvertisement;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.io.IOException;
import java.util.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.PeerGroupAdvertisement;



/** Where all peergroups are constructed, using the standard JXTA peergroup
 * construction methods. All new peergroups need a parent peergroup to add as a
 * child peergroup.
 * @author cumming
 * @since December 5, 2003, 10:49 AM
 */
public class PGUtilities implements DpmTerms {
    protected DpmAppTopFrame topFrame;
    protected Peer appUser;
    protected Thread thread;
    
    /** Creates a new instance of PeerGroupUtilities */
    public PGUtilities(DpmAppTopFrame topFrame) {
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
    }
    
    public void printServicePs(PeerGroupAdvertisement pgAdv) {
        Hashtable h = pgAdv.getServiceParams();
        Enumeration e = h.keys();
        while(e.hasMoreElements()) {
            //            StructuredTextDocument doc = (StructuredTextDocument)e.nextElement();
            //            String key = (String)el.getKey();
            //            String value = (String)el.getValue();
            //            System.out.println("Key: " + key + " Value: " + value);
        }
    }
    
    public void createNewIPG(PeerGroup newPG, PeerGroupAdvertisement newPgAdv, PeerGroup parentPG) {
        /**If pgAdv has been deleted */
        if(appUser.isDeleted(newPgAdv)) {
            System.out.println("ERROR: attempted to store new IPG for deleted peergroup");
            return;
        }
        if(newPG==null) {
            System.out.println("ERROR: null newPG in createNewIPG");
            return;
        }
        if(newPgAdv==null) {
            System.out.println("ERROR: null pgAdv in createNewIPG");
            return;
        }
        if(parentPG==null) {
            System.out.println("ERROR: null parentPG in createNewIPG");
            return;
        }
        try {
            IPGAdvertisement ipgAdv = createIPGAdvertisement(newPgAdv, parentPG);
            if (ipgAdv == null) {
                System.out.println("ERROR: ipgAdv is null in createNewIPG");
                return;
            }
            /**Create and publish the IPGAdv locally (= semi-persistent storage): */
            parentPG.getDiscoveryService().publish(ipgAdv, DiscoveryService.ADV);
            parentPG.getDiscoveryService().remotePublish(ipgAdv, DiscoveryService.ADV);
            /**Also add it to non-persistent storage */
            topFrame.getAppUser().getCsAdvIPG().addIPGAdv(ipgAdv, parentPG);
            /**Also add PG to non-persistent storage */
            topFrame.getAppUser().getCsPG().addPG(newPG, parentPG);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**Revised Dec 13, 2004 */
    public PeerGroup retrieveExistingPG(PeerGroup parentPG, PeerGroupAdvertisement childPgAdv) {
        ContentStorage pgs = appUser.getCsPG();
        String childPgAdvID_String = childPgAdv.getPeerGroupID().toString();
        String parentPG_IDString = parentPG.getPeerGroupID().toString();
        
        if(pgs.iterator(parentPG_IDString) != null) {
            for (Iterator i = pgs.iterator(parentPG_IDString); i.hasNext(); ) {
                PeerGroup curPG = (PeerGroup)i.next();
                String curIDString = curPG.getPeerGroupID().toString();
                if(curIDString.equals(childPgAdvID_String)) {
                    return curPG;
                }
            }
        }
        return null;
    }
    
    /**Creates peerGroup from an existing childAdv--known to exist and to be a child of parentPG */
    public PeerGroup newPG_UsingAdv(PeerGroupAdvertisement childPgAdv, PeerGroup parentPG) {
        ContentStorage csPG = topFrame.getAppUser().getCsPG();
        PeerGroup existingPG = csPG.retrieveExistingPGLocal(parentPG, childPgAdv);
        if(existingPG != null) {
            return existingPG;
        }
        PeerGroup newpg = null;
        if(parentPG != null && childPgAdv != null && childPgAdv.getName()!= null) {
            try {
                ModuleSpecID msID = childPgAdv.getModuleSpecID();
                PeerGroupID pgID = childPgAdv.getPeerGroupID();
                //printMessage("XXModuleSpecID: " + msID.getURL().toString());
                if(pgID != null) {// && specIDOK(childPgAdv)) {
                    //newpg = parentPG.newGroup(pgID);
                    /**Every time a new peergroup is made in DPM, an IPGAdv is created */
                    newpg = parentPG.newGroup(childPgAdv);
                    if (newpg != null) {
                        printMessage("Successfully created the " + newpg.getPeerGroupName() + " peergroup, w/ adv");
                        createNewIPG(newpg, childPgAdv, parentPG);
                        return newpg;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
        //        String mcid = "urn:jxta:uuid-DEADBEEFDEAFBABAFEEDBABE0000000805";
        //        PeerGroupID mcID = topFrame.makePgIDFromURL(mcid);
        //        PeerGroupID parentID = parentPG.getPeerGroupID();
        //        if (mcID==parentID) {
        //            printMessage("TREE!!: mcID=parentID. ParentPG= " + parentPG.getPeerGroupNameX());
        //        }
    }
    
    /**Creates an all-new PeerGroup, checking first to see if it already exists
     * can also use existing urls (e.g. DPM_URL) */
    public PeerGroup newPG_NotUsingAdv(String pgName, String pgDesc, String url, PeerGroup parentPG) {
        /**first check if any pgAdv can be found */
        try {
            PeerGroupAdvertisement existingPgAdv = findExistingPGAdvLocalByNameAndDesc(
            pgName, pgDesc, parentPG);
            /** if existing pgAdv in local cache--use it */
            if(existingPgAdv != null) {
                return newPG_UsingAdv(existingPgAdv, parentPG);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        /** Need to build it from scratch */
        printMessage("Couldn't find an existing " + pgName + "adv. Must create one");
        PeerGroupID pgID = null;
        if(url != null) {
            pgID = makePgIDFromURL(url);
        }
        else {
            pgID = IDFactory.newPeerGroupID();
        }
        try {
            Advertisement implAdv = topFrame.getNetPG().getAllPurposePeerGroupImplAdvertisement();
            /**Every time a new peergroup is made in DPM, an IPGAdv is created */
            PeerGroup newpg = parentPG.newGroup(pgID, implAdv, pgName, pgDesc);
            if (newpg != null) {
                printMessage("Successfully created the " + pgName + " peergroup, w/out adv");
                createNewIPG(newpg, newpg.getPeerGroupAdvertisement(), parentPG);
                return newpg;
            }
        }
        catch (Exception e) {
            printMessage("Couldn't create a ModuleImplAdvertisment for " + pgName);
        }
        return null;
    }
  
    /**copied from: net.jxta.util.PeerGroupUtilities */
    public PeerGroupAdvertisement ipgAdv_To_pgAdv(IPGAdvertisement ipgAdv) {
        //ModuleImplAdvertisement moduleImplAdvertisement = parent.getAllPurposePeerGroupImplAdvertisement();
        // Publish the ModuleImplAdvertisement
        // otherwise we can't be sure others will be able to find this definition when trying to instantiate the group.
        //parent.getDiscoveryService().publish(moduleImplAdvertisement, DiscoveryService.ADV);
        //PeerGroupID peerGroupId = IDFactory.newPeerGroupID();
        PeerGroupAdvertisement pgAdv =
        (PeerGroupAdvertisement)AdvertisementFactory.newAdvertisement(PeerGroupAdvertisement.getAdvertisementType());
        pgAdv.setName(ipgAdv.getName());
        pgAdv.setDescription(ipgAdv.getDescription());
        pgAdv.setPeerGroupID(ipgAdv.getPeerGroupID());
        pgAdv.setModuleSpecID(ipgAdv.getModuleSpecID());
        return pgAdv;
    }
    
    //Current:
    /**Creates an IPG and publishes it (locally) in the parentPG */
    public IPGAdvertisement createIPGAdvertisement(PeerGroupAdvertisement pgAdv, PeerGroup parentPG)
    throws Exception {
        if(appUser.isDeleted(pgAdv)) {
            System.out.println("ERROR: attempted to create IPGAdv for deleted peergroup");
            return null;
        }
        if(pgAdv == null) {
            System.out.println("ERROR: null pgAdv in createIPGAdvertisement");
            return null;
        }
        IPGAdvertisement ipgAdv = (IPGAdvertisement)AdvertisementFactory.newAdvertisement(
        IPGAdvertisement.getAdvertisementType());
        ipgAdv.setName(pgAdv.getName());
        ipgAdv.setDescription(pgAdv.getDescription());
        ipgAdv.setPeerGroupID(pgAdv.getPeerGroupID());
        ipgAdv.setModuleSpecID(pgAdv.getModuleSpecID());
        
        if(ipgAdv==null) {
            System.out.println("ERROR: null in createIPGAdvertisement");
            return null;
        }
        //System.out.println("NOTE: successfully created IPGAdvertisement for: " + ipgAdv.getName());
        return ipgAdv;
    }
    
    public PeerGroupID makePgIDFromURL(String pgURL) {
        PeerGroupID pgID = null;
        try {
            pgID = (PeerGroupID)IDFactory.fromURL(IDFactory.jxtaURL(pgURL));
            if(pgID != null) {
                printMessage("Sucessfully created peergroupID");
                return pgID;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //no longer needed:
    //if ModuleSpecID is not on the 'bad' list
    //bad = ones known to cause runtime errors
    public boolean specIDOK(PeerGroupAdvertisement pgAdv) {
        String mSpecS = pgAdv.getModuleSpecID().getURL().toString();
        String pgUrl = pgAdv.getPeerGroupID().getURL().toString();
        //
        Vector badSpecs = new Vector();
        badSpecs.add("urn:jxta:uuid-DEADBEEFDEAFBABAFEEDBABE0000000505");
        badSpecs.add("urn:jxta:uuid-DEADBEEFDEAFBABAFEEDBABE000000010406");
        badSpecs.add("urn:jxta:uuid-CDA2B1D381F547568788B93CD6A1665505");
        badSpecs.add("urn:jxta:uuid-E34FB7476D724CCDA8239886FA15927005");
       
        for(Iterator i = badSpecs.iterator(); i.hasNext(); ) {
            String cur = (String)i.next();
            if(cur.equals(mSpecS) || cur.equals(pgUrl)) {
                //printMessage("Found bad specID: " + cur);
                printMessage("PG with bad spec: " + pgAdv.getName());
                return false;
            }
        }
        return true;
    }
    
    public PeerGroupAdvertisement findExistingIPGAdvLocalByNameAndDesc(
    String pgName, String pgDesc, PeerGroup parentPG) throws Exception {
        try {
            Enumeration e = null; //holds the discovered peergroups
            try {
                /**Retrieve by name */
                e = parentPG.getDiscoveryService().getLocalAdvertisements(DiscoveryService.ADV, "Name", pgName);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (e == null || !e.hasMoreElements()) {
                return null;
            }
            else { //Success! e is not empty
                IPGAdvertisement ipgAdv;
                for (Enumeration e1 = e; e1.hasMoreElements(); ) {
                    Object obj = e1.nextElement();
                    if (obj instanceof IPGAdvertisement) {
                        ipgAdv = (IPGAdvertisement)obj;
                        /**Also, check description */
                        if(ipgAdv.getDescription().equals(pgDesc)) {
                            printMessage("Found the existing " + pgName + " ipgAdv locally");
                            return ipgAdv_To_pgAdv(ipgAdv);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public PeerGroupAdvertisement findExistingPGAdvLocalByNameAndDesc(
    String pgName, String pgDesc, PeerGroup parentPG) throws Exception {
        /**First, see if an IPGAdv exists */
        PeerGroupAdvertisement pgAdv = findExistingIPGAdvLocalByNameAndDesc(pgName, pgDesc, parentPG);
        if(pgAdv != null) {
            return pgAdv;
        }
        try {
            
            Enumeration e = null; //holds the discovered peergroups
            try {
                /**Retrieve by name */
                e = parentPG.getDiscoveryService().getLocalAdvertisements(DiscoveryService.GROUP, "Name", pgName);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (e == null || !e.hasMoreElements()) {
                return null;
            }
            else { //Success! e is not empty
                pgAdv = (PeerGroupAdvertisement)e.nextElement();
                /**Also check description */
                if(pgAdv != null && pgAdv.getDescription().equals(pgDesc)) {
                    printMessage("Found the existing " + pgName + " pgAdv locally");
                    return pgAdv;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public PeerGroupAdvertisement findExistingPGAdvRemote(PeerGroup parentPG, String pgName) throws Exception {
        try {
            DiscoveryService discSvc = parentPG.getDiscoveryService();
            int count = 2; //max number of attempts to discover
            printMessage("Attempting to discover the " + pgName + " peergroup remotely");
            Enumeration e = null; //holds the discovered peergroups
            //loop until we discover the dpmNet or until we exhausted the desired number of attempts
            while(count-- > 0) { //look both locally and remotely, three times
                try {
                    e = discSvc.getLocalAdvertisements(DiscoveryService.GROUP, "Name", pgName);
                    //if e has something in it = it was found locally; JXTA IANS p.55
                    if((e != null) && e.hasMoreElements()) {
                        break;
                    }
                    //we didn't find it locally. send out a remote discovery request
                    printMessage("Attempting to discover the " + pgName + " peergroup remotely");
                    discSvc.getRemoteAdvertisements(null, DiscoveryService.GROUP, "Name", pgName, 1, null);
                    //sleep to allow time for peers to respond to the discovery request
                    try {
                        Thread.sleep(TIMEOUT);
                    }
                    catch (InterruptedException ex) { }
                }
                catch (IOException ioe) { }
            }
            //stopped looking
            //if still haven't found anything
            if (e == null || !e.hasMoreElements()) {
                return null;
            }
            else { //Success! e is not empty
                PeerGroupAdvertisement pgAdv = (PeerGroupAdvertisement)e.nextElement();
                if(pgAdv != null) {
                    printMessage("Found the existing " + pgName + " peergroup");
                    return pgAdv;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //now uses "newPG" method 3.12.2003 MC
    public PeerGroup findStaticPG(String pgName, String pgDesc, String url, PeerGroup parentPG) {
        /**newPG handles whether a DPM_URL exists (or not) */
        return newPG_NotUsingAdv(pgName, pgDesc, url, parentPG);
    }
    
    public void printMessage(String s) {
        if(topFrame == null) {
            System.out.println("ERROR: null topFrame in PGUtilities: printMessage");
        }
        else {
            topFrame.printMessage(s);
        }
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
    
}
