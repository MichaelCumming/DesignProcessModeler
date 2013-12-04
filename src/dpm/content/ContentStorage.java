/*
 * ContentStorage.java
 *
 * Created on November 17, 2003, 8:57 PM
 */

package dpm.content;

import dpm.container.tree.*;
import dpm.content.advertisement.DeleteAdvertisement;
import dpm.content.advertisement.IPGAdvertisement;
import dpm.content.advertisement.IPGMemberAdvertisement;
import dpm.content.advertisement.NetAdvertisement;
import dpm.content.advertisement.chat.ChatAdvertisement;
import dpm.content.advertisement.chat.PrivateChatAdvertisement;
import dpm.content.advertisement.designEntity.UserNamedEntityAdv;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.content.advertisement.net.StateNetAdvertisement;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.designEntity.UserNamedEntity;
import dpm.content.advertisement.designEntity.related.constraint.*;
import dpm.content.comparator.*;
import dpm.content.constraint.*;
import dpm.content.comparator.designEntity.*;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.util.*;
import java.io.*;
import javax.swing.JList;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.id.*;


/**
 * Basic storage for a variety of items including peergroups and advertisements.
 * Implemented as a hashmap of sorted collections. A very important part of the DPM application.
 * Used for transient data storage.
 * @author  Michael Cumming
 * @since November 17, 2003, 8:57 PM
 */
public class ContentStorage implements DpmTerms {
    protected String itemClassName;
    private Peer appUser;
    protected HashMap content; //contains rows of: 'storageKey' X StorageSet
    
    protected Comparator pgAdvComparator = new PeerGroupAdvComparator();
    protected Comparator peerAdvComparator = new PeerAdvComparator();
    protected Comparator pgComparator = new PeerGroupComparator();
    protected Comparator designEntityAdvComparator = new DesignEntityAdvComparator();
    protected Comparator designEntityRelatedAdvComparator = new DesignEntityRelatedAdvComparator();
    
    protected Comparator csComparator = new ContentStorageComparator();
    protected Comparator ipgAdvComparator = new IPGAdvComparator();
    protected Comparator stringComparator = new StringComparator();
    protected Comparator designEntityComparator = new DesignEntityComparator();
    protected Comparator netAdvComparator = new NetAdvComparator();
    protected Comparator deleteAdvComparator = new DeleteAdvComparator();
    protected Comparator chatAdvComparator = new ChatAdvComparator();
    protected Comparator memberAdvComparator = new MemberAdvComparator();
    
    
    /** Creates a new instance of ContentManager */
    public ContentStorage(String itemClassName, Peer appUser) {
        /** Only types of item class can be added to the collection */
        this.itemClassName = itemClassName;
        this.appUser = appUser;
        this.content = new HashMap();
    }
    
    /** Inner class for each 'row' of the database */
    private class StorageSet {
        Collection collection; //the type of list or set used
        Comparator comparator; //used for sorting elements in sets
        //
        private StorageSet(Comparator comparator) {
            this.comparator = comparator;
            //assume all collections are TreeSets for now (=SortedSet)
            this.collection = Collections.synchronizedSortedSet(new TreeSet(comparator));
        }
        private void add(Object obj) {
            if (obj != null)
                collection.add(obj);
        }
        private void remove(Object obj) { collection.remove(obj); }
        private boolean isEmpty() { return collection.isEmpty(); }
        private boolean contains(Object obj) { return collection.contains(obj); }
        private void clear() { collection.clear(); }
        private int size() { return collection.size(); }
        
        public Collection getCollection() {
            return collection;
        }
        public void setCollection(Collection collection) {
            this.collection = collection;
        }
        public Comparator getComparator() {
            return comparator;
        }
        public void setComparator(Comparator comparator) {
            this.comparator = comparator;
        }
    }
    
    /**Revised Dec 13, 2004 */
    public String getStorageKeyFromPG_ID(PeerGroup pg) {
        return pg.getPeerGroupID().toString();
    }
    
    /** Returns the number of key-value mapping in the HashMap 'content'
     * or: rows in the table */
    public int size() {
        return content.size();
    }
    
    public boolean isEmpty() {
        return (size() <= 0);
    }
    
    public Iterator iterator(String storageKey) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if (ss != null)
            return ss.collection.iterator();
        else {
            //System.out.println("ContentStorage collection is null. Storage storageKey: " + storageKey);
            return null;
        }
    }
    
    public Object getFirst(String storageKey) {
        if(iterator(storageKey) != null) {
            StorageSet ss = (StorageSet)content.get(storageKey);
            Collection c = ss.getCollection();
            Object[] a = c.toArray();
            return a[0];
        }
        return null;
    }
    
    public Object getLast(String storageKey) {
        if(iterator(storageKey) != null) {
            StorageSet ss = (StorageSet)content.get(storageKey);
            Collection c = ss.getCollection();
            Object[] a = c.toArray();
            return a[c.size() - 1];
        }
        return null;
    }
    
    /** Assumes keyValues have a 'natural order'-- e.g. have number prefixes: 0.x, 1.y, etc.*/
    public String getGreatestKeyValue() {
        String greatest = String.valueOf(0);
        for (Iterator i = getKeySet().iterator(); i.hasNext(); ) {
            String curKeyValue = (String)i.next();
            /** if key value is greater than greatest */
            if(curKeyValue.compareTo(greatest) > 0) {
                greatest = curKeyValue;
            }
        }
        return greatest;
    }
    
    /** Makes a set (no repeats) of all objects in a content storage */
    public HashSet collapseAll() {
        HashSet set = new HashSet();
        for (Iterator i = getKeySet().iterator(); i.hasNext();) {
            String storageKey = (String)i.next();
            if (iterator(storageKey) != null) {
                /** For all tasks entered under that storage key */
                for (Iterator j = iterator(storageKey); j.hasNext(); ) {
                    Object obj = j.next();
                    if (obj != null) {
                        set.add(obj);
                    }
                }
            }
        }
        return set;
    }
    
    public boolean removeItem(String storageKey, Object objToDelete) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(ss == null) {
            //this set doesn't exist yet
            System.out.println(storageKey + " StorageSet does not exist");
            return false; //removal failed
        }
        else {
            /**this s.set already exists */
            if(ss.contains(objToDelete)) {
                ss.remove(objToDelete);
                return true; //removal succeeded
            }
        }
        return false;
    }
    
    /**Removes an item locate anywhere in a CS */
    public boolean removeItem(Object objToDelete) {
        String storageKey = getStorageKeyName(objToDelete);
        if(storageKey == null) {
            return false; //removal failed
        }
        return removeItem(storageKey, objToDelete);
    }
    
    /**reverse of normal practice--goes from advs to parentsNames */
    public String getStorageKeyName(Object obj) {
        //set of all String keys
        Set keys = getKeySet();
        for(Iterator i = keys.iterator(); i.hasNext(); ) {
            String storageKey = (String)i.next();
            //if object is contained in this db
            if(contains(storageKey, obj)) {
                return storageKey;
            }
        }
        return null;
    }
    
    public Set getKeySet() {
        return content.keySet();
    }
    
    /**Return a set of objects keyed at one row location */
    public Set getOneRowSet(String storageKey) {
        if(iterator(storageKey) != null) {
            HashSet set = new HashSet();
            for(Iterator i = iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                set.add(obj);
            }
            return set;
        }
        return null;
    }
    
    /** Makes sure that items added are of the type specified at construction */
    public boolean addedObjOfCorrectClass(Object obj) {
        return itemClassName.equals(obj.getClass().getName());
    }
    
    public boolean csHoldsThisClass(String className) {
        return itemClassName.equals(className);
    }
    
    public boolean storageKeyMissing(String storageKey) {
        //return ((StorageSet)content.get(storageKey) == null);
        return !getKeySet().contains(storageKey);
    }
    
    public boolean storageKeyExists(String storageKey) {
        //return ((StorageSet)content.get(storageKey) != null);
        return getKeySet().contains(storageKey);
    }
    
    /**Should delete checker check all entries? Might slow down system considerably... */
    synchronized public void addItem(String storageKey, Object obj, Comparator comparator) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(ss == null) {
            /**this set doesn't exist yet */
            StorageSet newSS = new StorageSet(comparator);
            if(obj != null) {
                if (addedObjOfCorrectClass(obj)) {
                    newSS.add(obj);
                    content.put(storageKey, newSS);
                }
                else {
                    System.out.println("ERROR: attempted to add item to CS of wrong class. Expected class: " +
                    itemClassName + " Actual class: " + obj.getClass().getName());
                }
            }
        }
        else {
            /**this set already exists */
            ss.add(obj);
            //System.out.println("Added item into existing storage db: " + storageKey);
        }
    }
    
    public boolean isEmpty(String storageKey) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        //true if: null, or empty
        if(ss == null)
            return true;
        else
            return ss.isEmpty();
    }
    
    public boolean contains(String storageKey, Object obj) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(ss == null)
            return false;
        else
            return ss.contains(obj);
    }
    
    public void clear(String storageKey) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(ss == null)
            return;
        else
            ss.clear();
    }
    
    public int numValues(String storageKey) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(ss == null)
            return -1;
        else
            return ss.size();
    }
    
    /** UserNamedEntity advs are first converted into UserNamedEntities and then added to ContentStorage */
    /**Note that entity type must be supplied
     * parentNode added: Dec 21, 2004 */
    public void addUserNamedEntityAdv(
    UserNamedEntityAdv adv, LoopNetAdvertisement loopNetAdv, PeerGroup parentPG,
    String entityType, PGTreeNode parentNode) {
        if(adv == null) {
            return;
        }
        UserNamedEntity newEntity = new UserNamedEntity(adv, loopNetAdv, appUser);
        if(newEntity == null) {
            System.out.println("ERROR: newEntity is null in cs.addUserNamedEntityAdv()");
            return;
        }
        addUserNamedEntity(newEntity, parentPG);
        /**This refreshes the PGTreeNode when a new design entity is added */
        parentNode.checkIfNodeRefreshNeeded(newEntity);
    }
    
    /** keyed by parent PG's name */
    /**@since Sept 13.2004 */
    public void addUserNamedEntity(UserNamedEntity entity, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        addItem(storageKey, entity, designEntityComparator);
    }
    
    /** keyed by parent PG's name */
    public void addPeerAdv(PeerAdvertisement peerAdv, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        if(peerAdv == null) {
            System.out.println("ERROR: null peerAdv in addPeerAdv");
            return;
        }
        if(peerAdv.getName() == null) {
            peerAdv.setName("NO_NAME_PEER");
        }
        addItem(storageKey, peerAdv, peerAdvComparator);
    }
    
    /** keyed by parent PG's name */
    public void addPgAdv(PeerGroupAdvertisement pgAdv, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        if(pgAdv == null) {
            System.out.println("ERROR: null pgAdv in addPgAdv");
            return;
        }
        if(pgAdv.getName() == null) {
            pgAdv.setName("NO_NAME_PEERGROUP");
        }
        addItem(storageKey, pgAdv, pgAdvComparator);
    }
    
    public void addChatAdv(ChatAdvertisement chatAdv, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        if(chatAdv == null) {
            System.out.println("ERROR: null chatAdv in addChatAdv");
            return;
        }
        addItem(storageKey, chatAdv, chatAdvComparator);
    }
    
    public void addPrivateChatAdv(PrivateChatAdvertisement chatAdv, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        if(chatAdv == null) {
            System.out.println("ERROR: null chatAdv in addPrivateChatAdv");
            return;
        }
        addItem(storageKey, chatAdv, chatAdvComparator);
    }
    
    /** keyed by parent PG's name */
    public void addIPGAdv(IPGAdvertisement ipgAdv, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        if(ipgAdv == null) {
            System.out.println("ERROR: null ipgADV in addIPGAdv");
            return;
        }
        if(ipgAdv.getName() == null) {
            ipgAdv.setName("NO_NAME_IPG");
        }
        addItem(storageKey, ipgAdv, ipgAdvComparator);
    }
    
    /** keyed by parent PG's name */
    public void addPG(PeerGroup pg, PeerGroup parentPG) {
        String storageKey;
        if (parentPG == null) {
            storageKey = "NO_PARENT";
        }
        else {
            //storageKey = parentPG.getPeerGroupNameX();
            //STORE_PG
            storageKey = getStorageKeyFromPG_ID(parentPG);
        }
        if(pg == null) {
            System.out.println("ERROR: null pg in addPG. pgName: " + pg.getPeerGroupName());
            return;
        }
        //assumes pgs will always have a name
        addItem(storageKey, pg, pgComparator);
        //System.out.println("PG added to csPG: " + pg.getPeerGroupNameX());
    }
    
    /** keyed by parent PG's name */
    public void addLoopNetAdv(LoopNetAdvertisement loopNetAdv, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        addItem(storageKey, loopNetAdv, netAdvComparator);
    }
    
    public void addStateNetAdv(StateNetAdvertisement stateNetAdv, PeerGroup parentPG) {
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        addItem(storageKey, stateNetAdv, netAdvComparator);
    }
    
    /**Keyed by ID of the adv to delete
     * @since August 11, 2004 */
    public void addDeleteAdv(DeleteAdvertisement deleteAdv) {
        ID idToDelete = deleteAdv.getDeleteAdvID();
        if(idToDelete.equals(ID.nullID)) {
            System.out.println("ERROR: idToDelete is null in cs.addDeleteAdv()");
            return;
        }
        String storageKey = idToDelete.toString();
        addItem(storageKey, deleteAdv, deleteAdvComparator);
        //System.out.println("GOOD: Added a deleteAdv to content storage");//cs.addDeleteAdv()! deleteAdvID= " + storageKey);
    }
    
    /**Stores members of specific peergroups (specific to DPM peers)
     * @since 25 nov. 2004 */
    public void addMemberAdv(IPGMemberAdvertisement memberAdv, PeerGroup parentPG) {
        /**note that peergroup specific advs are keyed by parentPG name:
         * MISTAKE? revise to pgID.toString()? */
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = getStorageKeyFromPG_ID(parentPG);
        addItem(storageKey, memberAdv, memberAdvComparator);
    }
    
    /** General application: can be keyed by a variety of strings */
    public void addString(String categoryName, String s) {
        addItem(categoryName, s, stringComparator);
        //System.out.println("Name added to csPGNames: " + childName);
    }
    
    /** adds a set of string to one key value */
    public void addStringSet(String categoryName, Set stringSet) {
        for(Iterator i = stringSet.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            addString(categoryName, s);
        }
    }
    
    /**@since 28 Oct. 2004 */
    public void combineTwoStringSets(ContentStorage added) {
        if(added != null) {
            for(Iterator i = added.getKeySet().iterator(); i.hasNext(); ) {
                String categoryName = (String)i.next();
                addStringSet(categoryName, added.getOneRowSet(categoryName));
            }
        }
    }
    
    /***/
    public Set getStringSet(String storageKey) {
        Set set = new HashSet();
        if(iterator(storageKey) != null) {
            for(Iterator i = iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof String) {
                    set.add((String)obj);
                }
            }
        }
        return set;
    }
    
    /**Handles both string formateed and html formatted text: NEWLINE vs. HTML_NEWLINE */
    public String getAllContentsString() {
        String s = new String();
        for (Iterator i = getKeySet().iterator(); i.hasNext(); ) {
            String storageKey = (String)i.next();
            s += (storageKey + ":" + NEWLINE);
            s += getOneRowString(storageKey);
        }
        return s;
    }
    
    public String newLineIfRequired(Iterator i) {
        if(i.hasNext()) {
            return NEWLINE;
        }
        return EMPTY_STRING;
    }
    
    /**Handles both string formateed and html formatted text: NEWLINE vs. HTML_NEWLINE */
    public String getOneRowString(String storageKey) {
        String s = new String();
        if(iterator(storageKey) == null) {
            return "No entries";
        }
        for (Iterator i = iterator(storageKey); i.hasNext(); ) {
            Object obj = i.next();
            
            /**Items checked for deletion */
            if(obj instanceof PeerGroup) {
                PeerGroup pg = (PeerGroup)obj;
                if(!appUser.isDeleted(pg.getPeerGroupAdvertisement())) {
                    s += "PG: " + pg.getPeerGroupName() +
                    newLineIfRequired(i);
                }
            }
            else if(obj instanceof IPGAdvertisement) {
                IPGAdvertisement adv = (IPGAdvertisement)obj;
                if(!adv.isDeleted(appUser)) {
                    s += "IPGAdv: " + adv.getName() +
                    newLineIfRequired(i);
                }
            }
            else if(obj instanceof PeerGroupAdvertisement) {
                PeerGroupAdvertisement pgAdv = (PeerGroupAdvertisement)obj;
                if(!appUser.isDeleted(pgAdv)) {
                    s += "PgAdv: " + pgAdv.getName() +
                    newLineIfRequired(i);
                }
            }
            else if(obj instanceof UserNamedEntity) {
                UserNamedEntity entity = (UserNamedEntity)obj;
                if(!entity.isDeleted(appUser)) {
                    s += "Entity: " + entity.getFullName() + " " +
                    "[" + entity.getAuthorName() + "]" +
                    newLineIfRequired(i);
                }
            }
            else if(obj instanceof RoleAdvertisement) {
                RoleAdvertisement adv = (RoleAdvertisement)obj;
                if(!adv.isDeleted(appUser)) {
                    s += "RoleAdv: " + adv.getRoleName() + " " +
                    "[" + adv.getAuthorName() + "]" +
                    newLineIfRequired(i);
                }
            }
            else if(obj instanceof Link) {
                Link link = (Link)obj;
                if(!link.isDeleted(appUser)) {
                    s += "Link: " + link.getLinkAdv().getDescriptionBasic() + " " +
                    "[" + link.getLinkAdv().getAuthorName() + "]" +
                    newLineIfRequired(i);
                }
            }
            else if(obj instanceof LoopNetAdvertisement) {
                LoopNetAdvertisement adv = (LoopNetAdvertisement)obj;
                if(!adv.isDeleted(appUser)) {
                    s += "LoopNetAdv: " + adv.getNetName() + " " +
                    "[" + adv.getAuthorName() + "]" +
                    newLineIfRequired(i);
                }
            }
            else if(obj instanceof PolicyAdvertisement) {
                PolicyAdvertisement adv = (PolicyAdvertisement)obj;
                if(!adv.isDeleted(appUser)) {
                    s += "PolicyAdv: " + adv.getTransName() + " " +
                    "[" + adv.getAuthorName() + "]" + NEWLINE +
                    "Inputs needed: " + stringSet2String(adv.getRoles()) +
                    //selDesignEntity.getAllInputsNeededStringAnyPeer()) +
                    newLineIfRequired(i);
                }
            }
            
            /**Items not checked for deletion */
            else if(obj instanceof String) {
                String string = (String)obj;
                s += string +
                newLineIfRequired(i);
            }
            else if(obj instanceof PeerAdvertisement) {
                PeerAdvertisement adv = (PeerAdvertisement)obj;
                s += "PeerAdv: " + adv.getName() +
                newLineIfRequired(i);
            }
            else if(obj instanceof InputAdvertisement) {
                InputAdvertisement adv = (InputAdvertisement)obj;
                s += "InputAdv: " + adv.getTransName() + " " +
                "[" + adv.getAuthorName() + "]" +
                newLineIfRequired(i);
            }
            else if(obj instanceof HistoryAdvertisement) {
                HistoryAdvertisement adv = (HistoryAdvertisement)obj;
                s += "HistoryAdv: " + adv.getTransName() + " " +
                "[" + adv.getAuthorName() + "]" +
                newLineIfRequired(i);
            }
        }
        return s;
    }
    
    public String stringSet2String(Set set) {
        String result = new String();
        for(Iterator i = set.iterator(); i.hasNext(); ) {
            
            String s = (String)i.next();
            if(s!=null && s.equals(EMPTY_STRING)) {
                s = "ERROR";
            }
            if(i.hasNext()) {
                result += s + ", ";
            }
            else {
                result += s;
            }
        }
        return result;
    }
    
    public NetAdvertisement retrieveNetAdv(String storageKey, String netName) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(ss == null) {
            return null;
        }
        for(Iterator i = iterator(storageKey); i.hasNext(); ) {
            NetAdvertisement netAdv = (NetAdvertisement)i.next();
            if(netAdv.getNetName().equals(netName)) {
                return netAdv;
            }
        }
        return null;
    }
    
    /** Check storage before making a new PG */
    //OK
    public PeerGroup retrieveExistingPGLocal(PeerGroup parentPG, PeerGroupAdvertisement pgAdv) {
        String storageKey = parentPG.getPeerGroupID().toString();
        
        if(!csHoldsThisClass("net.jxta.impl.peergroup.PeerGroupInterface")) {
            System.out.println("ERROR: tried to retrieve obj from wrong cs");
            return null;
        }
        if(iterator(storageKey) != null) {
            for(Iterator i = iterator(storageKey); i.hasNext(); ) {
                PeerGroup pg = (PeerGroup)i.next();
                if (pg.getPeerGroupID().equals(pgAdv.getPeerGroupID())) {
                    return pg;
                }
            }
        }
        return null;
    }
    
    //OK
    public Advertisement retrieveAdv(String storageKey, String searchAdvName) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(ss == null) {
            System.out.println(storageKey + " database does not yet exist");
            return null;
        }
        else {
            for(Iterator i = iterator(storageKey); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof UserNamedEntityAdv) {
                    UserNamedEntityAdv adv = (UserNamedEntityAdv)obj;
                    if (adv.getFullName().equals(searchAdvName)) {
                        return adv;
                    }
                }
                if(obj instanceof UserNamedEntityAdv) {
                    UserNamedEntityAdv uneAdv = (UserNamedEntityAdv)obj;
                    if (uneAdv.getFullName().equals(searchAdvName)) {
                        return uneAdv;
                    }
                }
                if(obj instanceof InputAdvertisement) {
                    InputAdvertisement inputAdv = (InputAdvertisement)obj;
                    if (inputAdv.getTransName().equals(searchAdvName)) {
                        return inputAdv;
                    }
                }
                
                if(obj instanceof IPGAdvertisement) {
                    IPGAdvertisement ipgAdv = (IPGAdvertisement)obj;
                    if (ipgAdv.getName().equals(searchAdvName)) {
                        return ipgAdv;
                    }
                }
                if(obj instanceof PeerAdvertisement) {
                    PeerAdvertisement peerAdv = (PeerAdvertisement)obj;
                    if (peerAdv.getName().equals(searchAdvName)) {
                        return peerAdv;
                    }
                }
                
                if(obj instanceof PeerGroupAdvertisement) {
                    PeerGroupAdvertisement pgAdv = (PeerGroupAdvertisement)obj;
                    if (pgAdv.getName().equals(searchAdvName)) {
                        return pgAdv;
                    }
                }
            }
        }
        return null;
    }
    
    public synchronized void removePgAdv(String storageKey, String removeName) {
        StorageSet ss = (StorageSet)content.get(storageKey);
        if(iterator(storageKey) == null) {
            return;
        }
        for(Iterator i = iterator(storageKey); i.hasNext(); ) {
            Object obj = i.next();
            if(obj instanceof PeerGroupAdvertisement) {
                PeerGroupAdvertisement pgAdv = (PeerGroupAdvertisement)obj;
                if (pgAdv.getName().equals(removeName)) {
                    ss.remove(obj);
                    System.out.println("Removed pgAdv: " + removeName);
                }
            }
        }
    }
    
    public Set getAllEntitiesExceptOne(UserNamedEntity entityToOmit) {
        Set allEntities = collapseAll();
        allEntities.remove(entityToOmit);
        return allEntities;
    }
    
    /**@since Sept 11.2004 */
    public DesignEntity getEntityByIDString(String IDString) {
        Set allEntities = collapseAll();
        if (!allEntities.isEmpty()) {
            for (Iterator i = allEntities.iterator(); i.hasNext(); ) {
                UserNamedEntity une = (UserNamedEntity)i.next();
                if(une.getDesignEntityID().toString().equals(IDString)) {
                    return une;
                }
            }
        }
        return null;
    }
    
    public Set getEntitiesByEntityType(String entityType) {
        Set result = new HashSet();
        Set allEntities = collapseAll();
        if (!allEntities.isEmpty()) {
            for (Iterator i = allEntities.iterator(); i.hasNext(); ) {
                UserNamedEntity une = (UserNamedEntity)i.next();
                if(une.getEntityType().equals(entityType)) {
                    result.add(une);
                }
            }
        }
        return result;
    }
    
    /**Similar to that in DesignEntityInputPolicy */
    public String getNameWPrefix(Integer prefix) {
        for(Iterator i = getKeySet().iterator(); i.hasNext(); ) {
            String transNameWithPrefix = (String)i.next();
            Integer curPrefix = getPrefixNum(transNameWithPrefix);
            if(prefix.equals(curPrefix)) {
                return transNameWithPrefix;
            }
        }
        return null;
    }
    
    /** gets all the roles at a transition position */
    public Set getRolesSet(Integer prefix) {
        //Integer prefix = new Integer(transPrefix);
        for(Iterator i = getKeySet().iterator(); i.hasNext(); ) {
            String nameWithPrefix = (String)i.next();
            Integer curPrefix = getPrefixNum(nameWithPrefix);
            if(prefix.equals(curPrefix)) {
                return getStringSet(nameWithPrefix);
            }
        }
        return null;
    }
    
    public Integer getPrefixNum(String nameWithPrefix) {
        int i = nameWithPrefix.indexOf('.');
        return new Integer(nameWithPrefix.substring(0, i));
    }
    
    /**Gets a loopNetAdv from any peergroup */
    public LoopNetAdvertisement getLoopNetByName(String netName) {
        Set allLoops = collapseAll();
        if (!allLoops.isEmpty()) {
            for (Iterator i = allLoops.iterator(); i.hasNext(); ) {
                LoopNetAdvertisement loopAdv = (LoopNetAdvertisement)i.next();
                if(loopAdv.getNetName().equals(netName)) {
                    return loopAdv;
                }
            }
        }
        return null;
    }
    
    /**Applied to CsPG
     * @since 30 Sept 2004 */
    public PeerGroup getParentPG(PeerGroup childPG) {
        /**Avoid going higher than dpmNet */
        if(childPG.getPeerGroupName().equals(DPM_PGNAME)) {
            return null;
        }
        String parentPG_IDString = getStorageKeyName(childPG);
        if(parentPG_IDString == null) {
            return null;
        }
        Set allPGs = collapseAll();
        if (!allPGs.isEmpty()) {
            for (Iterator i = allPGs.iterator(); i.hasNext(); ) {
                PeerGroup curPG = (PeerGroup)i.next();
                String curIDString = curPG.getPeerGroupID().toString();
                if(curIDString.equals(parentPG_IDString)) {
                    return curPG;
                }
            }
        }
        return null;
    }
    
    public JList cs_To_JList(DesignEntity entityToOmit) {
        /** Jlist requires a Vector for construction: see cs_to_JList */
        Vector resultVector;
        if(entityToOmit instanceof UserNamedEntity) {
            UserNamedEntity entity = (UserNamedEntity)entityToOmit;
            resultVector = new Vector(getAllEntitiesExceptOne(entity));
            return new JList(resultVector);
        }
        return null;
    }
    
    //how many advs are found locally, using the discovery service
    public int localFoundSize(DiscoveryService discSvc, int jxtaType) {
        int count = 0;
        try {
            Enumeration e = discSvc.getLocalAdvertisements(jxtaType, null, null);
            while(e.hasMoreElements()) {
                e.nextElement();
                count++;
            }
            return count;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
    
    /** Getter for property itemClassName.
     * @return Value of property itemClassName.
     *
     */
    public java.lang.String getItemClassName() {
        return itemClassName;
    }
    
    /** Setter for property itemClassName.
     * @param itemClassName New value of property itemClassName.
     *
     */
    public void setItemClassName(java.lang.String itemClassName) {
        this.itemClassName = itemClassName;
    }
    
    /**@since 29 Oct. 2004 */
    public void printLinkCS() {
        if(!itemClassName.equals("dpm.content.constraint.Link")) {
            System.out.println("ERROR: printing out non-link cs");
            return;
        }
        for (Iterator i = getKeySet().iterator(); i.hasNext(); ) {
            String categoryName = (String)i.next();
            System.out.println("Category name: " + categoryName + ":");
            printLinkSet(getOneRowSet(categoryName));
        }
    }
    
    public void printLinkSet(Set set) {
        if(set != null) {
            for (Iterator i = set.iterator(); i.hasNext(); ) {
                Object obj = i.next();
                if(obj instanceof Link) {
                    Link link = (Link)obj;
                    String linkText = new DisplayUserObject(link.getLinkAdv(), appUser).getText();
                    System.out.println(linkText);
                }
            }
        }
    }
    
    public void printStringCS() {
        if(!itemClassName.equals("java.lang.String")) {
            System.out.println("ERROR: printing out non-String cs");
            return;
        }
        System.out.println("Printing string cs. Size: " + size());
        for (Iterator i = getKeySet().iterator(); i.hasNext(); ) {
            String curKeyValue = (String)i.next();
            System.out.println(curKeyValue + ": ");
            if(iterator(curKeyValue) != null) {
                Set items = new HashSet();
                for (Iterator j = iterator(curKeyValue); j.hasNext(); ) {
                    String cur = (String)j.next();
                    items.add(cur);
                }
                System.out.println(stringSet2String(items));
            }
        }
    }
}


//RECURSIVELY traverses from a rootPG downwards
//adds to a standard ContentStorage table
//recurses downwards from the basePG
//    public void csAdvPG_To_csPGTree(final PeerGroup rootPG) {
//        //this is the destination cs
//        if (jxtaType == DiscoveryService.GROUP && description.equals("PG")) {
//            //only looks for peerGROUPs, and sub-peerGROUPs
//            final int jxtaType = DiscoveryService.GROUP;
//            //a new thread for each level of the tree
//            //invokeLater is used when an application thread needs to update the GUI
//            //SwingUtilities.invokeLater(
//            //new Runnable() {
//            //public void run() {
//            if(rootPG != null) {
//                String storageKey = rootPG.getPeerGroupNameX();
//                //System.out.println("TREE: Started populating contentStorageTree with parentPG= " + storageKey);
//                try {
//                    cache_To_csAdv(rootPG, jxtaType);
//                    //System.out.println("TREE: Finished populating contentStorageTree with parentPG= " + storageKey);
//                    //use the CS that was just populated to iterate its child PGs
//                    if(!isEmpty(storageKey)) {
//                        for (Iterator i = iterator(storageKey); i.hasNext(); ) {
//                            Object adv = i.next();
//                            if(adv instanceof PeerGroupAdvertisement) {
//                                PeerGroupAdvertisement childPgAdv = (PeerGroupAdvertisement)adv;
//                                PeerGroup childPG = topFrame.getPgUtilities().newPG_UsingAdv(rootPG, childPgAdv);
//                                //now recurse breadth first--all children at each level are done iteratively
//                                if(childPG != null && childPG != topFrame.getNetPG()) {
//                                    csAdv_To_csPGTree(childPG);
//                                }
//                            }
//                        }
//                    }
//                    else {
//                        //System.out.println("TREE: this PG had no children in storage: " + storageKey);
//                    }
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        System.out.println("Error: illegal access in content storage");
//        //}
//        //});
//    }


