/*
 * Prototype.java
 *
 * Created on October 16, 2004, 10:51 AM
 */

package dpm.content.prototype;

import dpm.content.ContentStorage;
import dpm.content.DesignEntity;
import dpm.content.advertisement.NetAdvertisement;
import dpm.content.constraint.Link;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.jxta.peer.PeerID;
import dpm.content.comparator.designEntity.LinkComparator;
import java.util.*;
import dpm.content.advertisement.net.LoopNetAdvertisement;


/**
 *
 * @author  cumming
 */
public class Prototype implements DpmTerms {
    /**A description of the prototype: e.g. ONE_PEER_LATEST, ALL_PEERS_LATEST */
    protected DesignEntity protoEntity;
    protected String protoAlgorithm;
    protected String entityType;
    protected LoopNetAdvertisement loopNetAdv;
    protected Peer appUser;
    protected PeerID appUserID;
    /**Entities filtered for entityType and loopName */
    protected Set filteredEntities;
    /**Whether prototype considers information from all peers, or just this appUser (current user) */
    protected boolean thisPeerOnly;
    /**Whether the prototype references a real entity, or if data is assembled from many real entities */
    protected boolean synthetic;
    protected Comparator linkComparator = new LinkComparator();
    
    
    /** Creates a new instance of Prototype */
    /**loopName added constructor 20 Oct. 2004 */
    public Prototype(String protoAlgorithm, String entityType, LoopNetAdvertisement loopNetAdv,
    boolean thisPeerOnly, Peer appUser) {
        this.protoEntity = null;
        this.protoAlgorithm = protoAlgorithm;
        this.entityType = entityType;
        this.loopNetAdv = loopNetAdv;
        this.thisPeerOnly = thisPeerOnly;
        this.appUser = appUser;
        this.appUserID = appUser.getPeerID();
        this.filteredEntities = getFilteredEntities();
        setSynthetic();
    }
    
    /**Use an existing entity to construct the prototype */
    public Prototype(DesignEntity protoEntity, Peer appUser) {
        this.protoEntity = protoEntity;
        this.appUser = appUser;
        this.appUserID = appUser.getPeerID();
        this.loopNetAdv = protoEntity.getLoopNetAdv();
        
        /**Attributes that are not relevant if protoEntity is non-null */
        this.synthetic = false;
        //this.thisPeerOnly = false;
        this.protoAlgorithm = null;
        this.entityType = null;
        this.filteredEntities = null;
    }

    public boolean isSynthetic() {
        return synthetic;
    }  
    
    /**@since 28 Oct. 2004 */
    private void setSynthetic() {
        if(protoAlgorithm.equals(SUM_OF_EXISTING)) {
            this.synthetic = true;
        }
        else {
            this.synthetic = false;
        }
    }
    
    /**@since 28 Oct. 2004 */
    public Set getFilteredEntities() {
        Set result = new HashSet();
        if(getUnfilteredEntities() != null) {
            for(Iterator i = getUnfilteredEntities().iterator(); i.hasNext(); ) {
                DesignEntity entity = (DesignEntity)i.next();
                if(
                entityTypeOK(entity) &&
                loopOK(entity) &&
                authorOK(entity)) {
                    result.add(entity);
                }
            }
        }
        return result;
    }
    
    /**@since 14 Oct. 2004 */
    /** */
    public String getBaseName() {
        if(synthetic || getPrototypicalEntity() == null) {
            return null;
        }
        return getPrototypicalEntity().getBaseName();
    }
    
    public String getIteration() {
        if(synthetic || getPrototypicalEntity() == null) {
            return "1";
        }
        return getPrototypicalEntity().getIteration();
    }
    
    public String getLoopName() {
        return getLoopNetAdv().getNetName();
    }
    
    public LoopNetAdvertisement getLoopNetAdv() {
        return loopNetAdv;
    }
    
    /**Retrieves appropriate policies, based on loop */
    public ContentStorage getTransRoles() {
        if(synthetic) {
            return getCombinedTransRolesAllFilterdEntities();
        }
        if(getPrototypicalEntity() == null) {
            return null;
        }
        return getPrototypicalEntity().getTransRoles();
    }
    
    /**Returns a set of incoming links linked into prototypical entity */
    public Set getIncomingLinks() {
        if(synthetic) {
            return getIncomingLinksAllFilteredEntities();
        }
        if(getPrototypicalEntity() == null) {
            return null;
        }
        return getPrototypicalEntity().getIncomingLinks();
    }
    
    /**Returns a set of outgoing links linked out from prototypical entity */
    public Set getOutgoingLinks() {
        if(synthetic) {
            //System.out.println("protoEntity is synthetic in getOutgoingLinks()");
            return getOutgoingLinksAllFilteredEntities();
        }
        if(getPrototypicalEntity() == null) {
            //System.out.println("protoEntity is null in getOutgoingLinks()");
            return null;
        }
        //System.out.println("protoEntity is OK in getOutgoingLinks()");
        return getPrototypicalEntity().getOutgoingLinks();
    }
    
    /**@return A content storage that stores links indexed by target transition
     * @since 26 Oct. 2004 */
    public ContentStorage getTransLinksIncomingLinks() {
        if(getIncomingLinks() == null) {
            return null;
        }
        ContentStorage transLinks = new ContentStorage("dpm.content.constraint.Link", appUser);
        /**tested for synthetic */
        for(Iterator i = getIncomingLinks().iterator(); i.hasNext(); ) {
            Link link = (Link)i.next();
            /**Add non-deleted doBefore links */
            if(!link.isDeleted(appUser) && link.isDoBefore()) {
                String targetTrans = link.getLinkAdv().getTransName();
                transLinks.addItem(targetTrans, link, linkComparator);
            }
        }
        return transLinks;
    }
    
    /**@return A content storage that stores links indexed by source states
     * @since 26 Oct. 2004 */
    public ContentStorage getStatesLinksOutgoingLinks() {
        if(getOutgoingLinks() == null) {
            return null;
        }
        ContentStorage statesLinks = new ContentStorage("dpm.content.constraint.Link", appUser);
        statesLinks.printLinkCS();
        /**tested for synthetic in getOutgoingLinks() */
        for(Iterator i = getOutgoingLinks().iterator(); i.hasNext(); ) {
            Link link = (Link)i.next();
            /**Add non-deleted doBefore links */
            if(!link.isDeleted(appUser) && link.isDoBefore()) {
                String sourceState = link.getLinkAdv().getSourceState();
                statesLinks.addItem(sourceState, link, linkComparator);
            }
        }
        return statesLinks;
    }
    
    /**PRIVATE methods: only used locally  */
    
    private Set getUnfilteredEntities() {
        return appUser.getUserNamedEntities().collapseAll();
    }
    
    /**Get all incoming links for entities of one entityType */
    private Set getIncomingLinksAllFilteredEntities() {
        Set result = new HashSet();
        for(Iterator i = filteredEntities.iterator(); i.hasNext(); ) {
            DesignEntity entity = (DesignEntity)i.next();
            String entityIDS = entity.getDesignEntityID().toString();
            Set toAdd = appUser.getEntityRelatives().getIncomingLinks().getOneRowSet(entityIDS);
            if(toAdd != null) {
                result.addAll(toAdd);
            }
        }
        return result;
    }
    
    /**Get all incoming links for entities of one entityType */
    private Set getOutgoingLinksAllFilteredEntities() {
        Set result = new HashSet();
        for(Iterator i = filteredEntities.iterator(); i.hasNext(); ) {
            DesignEntity entity = (DesignEntity)i.next();
            String entityIDS = entity.getDesignEntityID().toString();
            result.addAll(appUser.getEntityRelatives().getOutgoingLinks().getOneRowSet(entityIDS));
        }
        return result;
    }
    
    /**Finds the prototype from which new entities are copied from, according to type of prototype: 'protoType' */
    private DesignEntity getPrototypicalEntity() {
        if(synthetic) {
            return null;
        }
        /**@since 1 Nov. 2004 */
        if(protoEntity != null) {
            return protoEntity;
        }
        if(protoAlgorithm.equals(LATEST)) {
            return getLatestEntity();
        }
        else if(protoAlgorithm.equals(MOST_ACTIVE)) {
            return getMostActiveEntity();
        }
        else {
            System.out.println("Error: unhandled protoAlgorithm in getProtypticalEntity(): " + entityType);
        }
        return null;
    }
    
    /** @since 14 Oct. 2004 */
    private DesignEntity getLatestEntity() {
        /**Assume all entity dates will be after 1 Jan 2004 */
        Date latestDate = EARLIEST_DATE;
        if(filteredEntities != null) {
            DesignEntity latestEntity = null;
            for (Iterator i = filteredEntities.iterator(); i.hasNext(); ) {
                DesignEntity entity = (DesignEntity)i.next();
                
                if(entityAfter(entity, latestDate)) {
                    latestDate = entity.getAdv().getDateCreate();
                    latestEntity = entity;
                }
            }
            return latestEntity;
        }
        return null;
    }
    
    /**One simple measure of the activity of this entity based on number of history advs
     * @since 20 Oct. 2004 */
    private DesignEntity getMostActiveEntity() {
        int greatestActivity = -1;
        if (filteredEntities != null) {
            DesignEntity mostActiveEntity = null;
            for (Iterator i = filteredEntities.iterator(); i.hasNext(); ) {
                DesignEntity entity = (DesignEntity)i.next();
                /**Measures entity based on number of its history advs */
                int entityActivity = entity.getActivity();
                
                if(entityActivity > greatestActivity) {
                    greatestActivity = entityActivity;
                    mostActiveEntity = entity;
                }
            }
            return mostActiveEntity;
        }
        return null;
    }
    
    /**@since 28 Oct. 2004 */
    private ContentStorage getCombinedTransRolesAllFilterdEntities() {
        ContentStorage resultTransRoles = new ContentStorage("java.lang.String", appUser);
        if(filteredEntities != null) {
            for(Iterator i = filteredEntities.iterator(); i.hasNext(); ) {
                DesignEntity entity = (DesignEntity)i.next();
                resultTransRoles.combineTwoStringSets(entity.getTransRoles());
            }
        }
        return resultTransRoles;
    }
    
    /**All entities are filtered for this constraint at start */
    private boolean entityTypeOK(DesignEntity entity) {
        return entity.getEntityType().equals(entityType);
    }
    /**All entities are filtered for this constraint at start */
    private boolean loopOK(DesignEntity entity) {
        return entity.getLoopNetAdv().getNetName().equals(getLoopName());
    }
    private boolean authorOK(DesignEntity entity) {
        if(thisPeerOnly) {
            return entity.getAdv().getAuthorID().equals(appUserID);
        } /**Else, any author is OK */
        return true;
    }
    
    /**Tests if an entity satisfies constraints implied in the 'protoType' attribute of Prototype */
    private boolean entityAfter(DesignEntity entity, Date latestDate) {
        return entity.getAdv().getDateCreate().after(latestDate);
    }
    
    /** Getter for property entityType.
     * @return Value of property entityType.
     *
     */
    public java.lang.String getEntityType() {
        return entityType;
    }
    
    /** Setter for property entityType.
     * @param entityType New value of property entityType.
     *
     */
    public void setEntityType(java.lang.String entityType) {
        this.entityType = entityType;
    }
    
    /** Getter for property thisPeerOnly.
     * @return Value of property thisPeerOnly.
     *
     */
    public boolean isThisPeerOnly() {
        return thisPeerOnly;
    }
    
    /** Setter for property thisPeerOnly.
     * @param thisPeerOnly New value of property thisPeerOnly.
     *
     */
    public void setThisPeerOnly(boolean thisPeerOnly) {
        this.thisPeerOnly = thisPeerOnly;
    }
    
    /** Getter for property protoAlgorithm.
     * @return Value of property protoAlgorithm.
     *
     */
    public java.lang.String getProtoAlgorithm() {
        return protoAlgorithm;
    }
    
    /** Setter for property protoAlgorithm.
     * @param protoAlgorithm New value of property protoAlgorithm.
     *
     */
    public void setProtoAlgorithm(java.lang.String protoAlgorithm) {
        this.protoAlgorithm = protoAlgorithm;
    }
    
    /** Setter for property loopNetAdv.
     * @param loopNetAdv New value of property loopNetAdv.
     *
     */
    public void setLoopNetAdv(dpm.content.advertisement.net.LoopNetAdvertisement loopNetAdv) {
        this.loopNetAdv = loopNetAdv;
    }
    
    /** Getter for property synthetic.
     * @return Value of property synthetic.
     *
     */
    
    /** Setter for property synthetic.
     * @param synthetic New value of property synthetic.
     *
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }
    
    /** Getter for property loopNetAdv.
     * @return Value of property loopNetAdv.
     *
     */
    
    
}
