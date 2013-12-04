/*
 * ContentStorage.java
 *
 * Created on November 17, 2003, 8:57 PM
 */

package dpm.content;

import dpm.container.tree.*;
import dpm.content.advertisement.AdvUtilities;
import dpm.content.advertisement.designEntity.DesignEntityRelatedAdv;
import dpm.content.advertisement.designEntity.related.HistoryAdvertisement;
import dpm.content.advertisement.designEntity.related.InputAdvertisement;
import dpm.content.advertisement.designEntity.related.RoleAdvertisement;
import dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement;
import dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement;
import dpm.content.comparator.DesignEntityComparator;
import dpm.content.comparator.designEntity.DesignEntityRelatedAdvComparator;
import dpm.content.comparator.designEntity.HistoryAdvComparator;
import dpm.content.comparator.designEntity.LinkComparator;
import dpm.content.comparator.designEntity.PolicyComparator;
import dpm.content.constraint.Link;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.util.Comparator;
import java.util.Iterator;
import net.jxta.peergroup.PeerGroup;
import dpm.content.constraint.*;
import dpm.content.designEntity.*;


/**
 * Basic storage for a variety of items including peergroups and advertisements.
 * Implemented as a hashmap of sorted collections. A very important part of the DPM application.
 * Used for transient data storage.
 * @author  Michael Cumming
 * @since November 17, 2003, 8:57 PM
 */
public class EntityRelatedContentStorage implements DpmTerms {
    private Peer appUser;
    private DpmAppTopFrame topFrame;
    
    private ContentStorage policies;
    private ContentStorage roles;
    private ContentStorage inputs;
    private ContentStorage histories;
    /**Stores polAdvs: keyed by sourceID string */
    private ContentStorage abandonedEntities;
    
    /**All Links that either point towards, or point from, a DesignEntity. Keyed by designEntityID */
    private ContentStorage allLinks;
    /**Stores linkAdvs: keyed by targetID string */
    private ContentStorage incomingLinks;
    /**Stores linkAdvs: keyed by sourceID string */
    private ContentStorage outgoingLinks;
    
    private Comparator relatedAdvComparator = new DesignEntityRelatedAdvComparator();
    private Comparator historyAdvComparator = new HistoryAdvComparator();
    private Comparator linkComparator = new LinkComparator();
    
    
    /** Creates a new instance of EntityRelatedContentStorage */
    public EntityRelatedContentStorage(Peer appUser, DpmAppTopFrame topFrame) {
        this.appUser = appUser;
        this.topFrame = topFrame;
        
        this.policies = new ContentStorage("dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement", appUser);
        this.roles = new ContentStorage("dpm.content.advertisement.designEntity.related.RoleAdvertisement", appUser);
        this.inputs = new ContentStorage("dpm.content.advertisement.designEntity.related.InputAdvertisement", appUser);
        this.histories = new ContentStorage("dpm.content.advertisement.designEntity.related.HistoryAdvertisement", appUser);
        this.abandonedEntities = new ContentStorage("dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement", appUser);
        
        /**NOTE: Links are added to the three below, not linkAdvs */
        /** All links in which design entity is either a source or a target */
        this.allLinks = new ContentStorage("dpm.content.constraint.Link", appUser);
        /**Keyed by target entity ID */
        this.incomingLinks = new ContentStorage("dpm.content.constraint.Link", appUser);
        /**Keyed by source entity ID */
        this.outgoingLinks = new ContentStorage("dpm.content.constraint.Link", appUser);
    }
    
    /** Add various types of RelatedAdv advs
     * baseNode added: Dec 22. 2004 */
    public void addRelative(DesignEntityRelatedAdv adv, PGTreeNode baseNode) {
        /** Stored according to the entityID of the task or product */
        String storageKey = adv.getDesignEntityID().toString();
        
        if(adv.isDeleted(appUser)) {
            return;
        }
        if(adv instanceof LinkAdvertisement) {
            System.out.println("ERROR: linkAdv in wrong location in ERCS");
            return;
        }
        if(adv instanceof PolicyAdvertisement) {
            PolicyAdvertisement policyAdv = (PolicyAdvertisement)adv;
            if(policyAdv.getRoles().contains(ABANDON)) {
                /**Add to a special CS if abandoned. Also add to normal policy advs */
                //System.out.println("abandoned polAdv found");
                abandonedEntities.addItem(storageKey, policyAdv, relatedAdvComparator);
            }
            if(!policyAdv.isDeleted(appUser)) {
                policies.addItem(storageKey, policyAdv, relatedAdvComparator);
                return;
            }
        }
        if(adv instanceof RoleAdvertisement) {
            RoleAdvertisement roleAdv = (RoleAdvertisement)adv;
            if(!roleAdv.isDeleted(appUser)) {
                roles.addItem(storageKey, roleAdv, relatedAdvComparator);
                return;
            }
        }
        if(adv instanceof InputAdvertisement) {
            InputAdvertisement inputAdv = (InputAdvertisement)adv;
            inputs.addItem(storageKey, inputAdv, relatedAdvComparator);
            return;
        }
        if(adv instanceof HistoryAdvertisement) {
            HistoryAdvertisement historyAdv = (HistoryAdvertisement)adv;
            /**First check, then add to CS @since Dec 22, 2004 */
           
            /** Note the use of historyAdvComparator: to order by dates */
            histories.addItem(storageKey, historyAdv, historyAdvComparator); 
            if(baseNode != null) {
                baseNode.checkIfNodeRefreshNeeded(historyAdv);
            }
            return;
        }
    }
     
    /**Adds a linkAdv to where the entity is either the source or the target.
     * This method takes care of all operations.
     * NOTE: precedence relations (DO_BEFORE) are stored in special content storages;
     * Tasks and Products are stored in these storages: e.g. incomingProducts stores Products
     * OTHER relations (user definable) are stored in 'allLinks' */
    public void addLinkToAllLinks(Link link, PeerGroup parentPG) {
        if(link.isDeleted(appUser)) {
            return;
        }
        /**Add to allLinks in any case, which is keyed by parentPG ID string */
        String storageKey = parentPG.getPeerGroupID().toString();
        allLinks.addItem(storageKey, link, linkComparator);
    }
    
    /**Adds links both from the perspective of the source and the target entity */
    public void addLinkToIncomingOutgoing(Link link) {
        if(link.isDeleted(appUser)) {
            return;
        }
        incomingLinks.addItem(link.getTargetID().toString(), link, linkComparator);
        outgoingLinks.addItem(link.getSourceID().toString(), link, linkComparator);
    }
    
    /**Retrieves an existing linkAdv that is similar to a proposed linkAdv. */
    public Link getExistingSame(Link link) {
        /**If not ok, then consider it deleted */
        if(link.isDeleted(appUser)) {
            return null;
        }
        for(Iterator i = allLinks.collapseAll().iterator(); i.hasNext(); ) {
            Link existLink = (Link)i.next();
            if(link.sameLink(existLink)) {
                return existLink;
            }
        }
        return null;
    }
    
    /**Retrieves an existing linkAdv that has a non-logical relation to a proposed linkAdv */
    public Link getExistingNonLogical(Link link) {
        if(link.isDeleted(appUser)) {
            return null;
        }
        for(Iterator i = allLinks.collapseAll().iterator(); i.hasNext(); ) {
            Link existLink = (Link)i.next();
            if(link.conflictsLogicallyWithExisting(existLink)) {
                return existLink;
            }
        }
        return null;
    }
    
    /** Getter for property policies.
     * @return Value of property policies.
     *
     */
    public dpm.content.ContentStorage getPolicies() {
        return policies;
    }
    
    /** Setter for property policies.
     * @param policies New value of property policies.
     *
     */
    public void setPolicies(dpm.content.ContentStorage policies) {
        this.policies = policies;
    }
    
    /** Getter for property roles.
     * @return Value of property roles.
     *
     */
    public dpm.content.ContentStorage getRoles() {
        return roles;
    }
    
    /** Setter for property roles.
     * @param roles New value of property roles.
     *
     */
    public void setRoles(dpm.content.ContentStorage roles) {
        this.roles = roles;
    }
    
    /** Getter for property inputs.
     * @return Value of property inputs.
     *
     */
    public dpm.content.ContentStorage getInputs() {
        return inputs;
    }
    
    /** Setter for property inputs.
     * @param inputs New value of property inputs.
     *
     */
    public void setInputs(dpm.content.ContentStorage inputs) {
        this.inputs = inputs;
    }
    
    /** Getter for property histories.
     * @return Value of property histories.
     *
     */
    public dpm.content.ContentStorage getHistories() {
        return histories;
    }
    
    /** Setter for property histories.
     * @param histories New value of property histories.
     *
     */
    public void setHistories(dpm.content.ContentStorage histories) {
        this.histories = histories;
    }
    
    /** Getter for property allLinks.
     * @return Value of property allLinks.
     *
     */
    public dpm.content.ContentStorage getAllLinks() {
        return allLinks;
    }
    
    /** Setter for property allLinks.
     * @param allLinks New value of property allLinks.
     *
     */
    public void setAllLinks(dpm.content.ContentStorage allLinks) {
        this.allLinks = allLinks;
    }
    
    /** Getter for property incomingLinks.
     * @return Value of property incomingLinks.
     *
     */
    public dpm.content.ContentStorage getIncomingLinks() {
        return incomingLinks;
    }
    
    /** Setter for property incomingLinks.
     * @param incomingLinks New value of property incomingLinks.
     *
     */
    public void setIncomingLinks(dpm.content.ContentStorage incomingLinks) {
        this.incomingLinks = incomingLinks;
    }
    
    /** Getter for property outgoingLinks.
     * @return Value of property outgoingLinks.
     *
     */
    public dpm.content.ContentStorage getOutgoingLinks() {
        return outgoingLinks;
    }
    
    /** Setter for property outgoingLinks.
     * @param outgoingLinks New value of property outgoingLinks.
     *
     */
    public void setOutgoingLinks(dpm.content.ContentStorage outgoingLinks) {
        this.outgoingLinks = outgoingLinks;
    }
    
    /** Getter for property abandonedEntities.
     * @return Value of property abandonedEntities.
     *
     */
    public dpm.content.ContentStorage getAbandonedEntities() {
        return abandonedEntities;
    }
    
    /** Setter for property abandonedEntities.
     * @param abandonedEntities New value of property abandonedEntities.
     *
     */
    public void setAbandonedEntities(dpm.content.ContentStorage abandonedEntities) {
        this.abandonedEntities = abandonedEntities;
    }
    
}
