/*
 * AdvertisementUtilities.java
 *
 * Created on January 16, 2004, 10:47 AM
 */

package dpm.content.advertisement;

import CH.ifa.draw.framework.Drawing;
import CH.ifa.draw.util.*;
import de.renew.gui.Demonstrator;
import dpm.content.ContentStorage;
import dpm.content.DesignEntity;
import dpm.content.advertisement.chat.ChatAdvertisement;
import dpm.content.advertisement.chat.PrivateChatAdvertisement;
import dpm.content.advertisement.designEntity.DesignEntityRelatedAdv;
import dpm.content.advertisement.designEntity.UserNamedEntityAdv;
import dpm.content.advertisement.designEntity.related.HistoryAdvertisement;
import dpm.content.advertisement.designEntity.related.InputAdvertisement;
import dpm.content.advertisement.designEntity.related.RoleAdvertisement;
import dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement;
import dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.content.advertisement.net.StateNetAdvertisement;
import dpm.content.constraint.Link;
import dpm.content.designEntity.UserNamedEntity;
import dpm.content.prototype.*;
import dpm.content.state.LoopNetReader;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.dpmApp.desktop.forms.*;
import dpm.peer.Peer;
import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.Date;
import java.util.Set;
import javax.swing.JFrame;
import net.jxta.discovery.*;
import net.jxta.document.*;
import net.jxta.document.Advertisement;
import net.jxta.id.*;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.*;
import net.jxta.protocol.PeerAdvertisement;



/**
 * Holds methods for creating DPM advertisements
 * @author  cumming
 * @since January 16, 2004, 10:47 AM
 */
public class AdvUtilities implements DpmTerms {
    private DpmAppTopFrame topFrame;
    private Peer appUser;
    private PeerID authorID;
    private String authorName;
    //private static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
    protected static DateFormat dateFormat =
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    
    /** Creates a new instance of AdvertisementUtilities */
    public AdvUtilities(DpmAppTopFrame topFrame) {
        this.topFrame = topFrame;
        this.appUser = topFrame.getAppUser();
        this.authorID = appUser.getPeerID();
        this.authorName = appUser.getName();
    }
    
    /**Retrieves a role adv associated with a particular role name */
    public RoleAdvertisement getRoleAdvToDelete(String roleNameToDelete, DesignEntity entity) {
        HashSet allRoleAdvs = entity.getAssumedRoleAdvsThisAppUser(topFrame);
        for (Iterator i = allRoleAdvs.iterator(); i.hasNext(); ) {
            RoleAdvertisement roleAdv = (RoleAdvertisement)i.next();
            /**If role name is the correct one */
            if(roleAdv.getRoleName().equals(roleNameToDelete)) {
                return roleAdv;
            }
        }
        return null;
    }
    
    /**Deletes all roles advs associated with one entity, and one app user */
    public void deleteAllRoleAdvs(DesignEntity entity, PeerGroup parentPG) {
        HashSet allRolesAdvs = entity.getAssumedRoleAdvsThisAppUser(topFrame);
        for (Iterator i = allRolesAdvs.iterator(); i.hasNext(); ) {
            deleteOneRoleAdv((RoleAdvertisement)i.next(), parentPG);
        }
    }
    
    /**Deletes one role adv,
     * except ones which are named 'author' or have already been deleted */
    public void deleteOneRoleAdv(RoleAdvertisement roleAdvToDelete, PeerGroup parentPG) {
        if(roleAdvToDelete == null || roleAdvToDelete.getRoleName().equals(AUTHOR)) {
            return;
        }
        if(!roleAdvToDelete.isDeleted(appUser)) {
            createDeleteAdvertisement(roleAdvToDelete, parentPG);
        }
    }
    
    public ChatAdvertisement createChatAdvertisement(PeerGroup parentPG, String message) throws Exception {
        ChatAdvertisement adv =
        (ChatAdvertisement)AdvertisementFactory.newAdvertisement(ChatAdvertisement.getAdvertisementType());
        adv.setAdvID(getNewCondatID(parentPG));
        adv.setMessage(message);
        adv.setDateCreate(new Date());
        adv.setAuthorID(authorID);
        adv.setAuthorName(authorName);
        
        try {
            parentPG.getDiscoveryService().publish(adv, DiscoveryService.ADV);
            parentPG.getDiscoveryService().remotePublish(adv, DiscoveryService.ADV);
            appUser.getChatAdvs().addChatAdv(adv, parentPG);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return adv;
    }
    
    public PrivateChatAdvertisement createPrivateChatAdvertisement(
    IPGMemberAdvertisement recipientAdv, PeerGroup parentPG, String message)
    throws Exception {
        PrivateChatAdvertisement adv =
        (PrivateChatAdvertisement)AdvertisementFactory.newAdvertisement(PrivateChatAdvertisement.getAdvertisementType());
        adv.setAdvID(getNewCondatID(parentPG));
        adv.setMessage(message);
        adv.setDateCreate(new Date());
        adv.setAuthorID(authorID);
        adv.setAuthorName(authorName);
        adv.setRecipientID((PeerID)recipientAdv.getPeerID());
        adv.setRecipientName(recipientAdv.getPeerName());
        
        try {
            parentPG.getDiscoveryService().publish(adv, DiscoveryService.ADV);
            parentPG.getDiscoveryService().remotePublish(adv, DiscoveryService.ADV);
            /**privateChatAdv is a sub-class of chatAdv */
            appUser.getPrivateChatAdvs().addPrivateChatAdv(adv, parentPG);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return adv;
    }
    
    /**One is needed for each choice point */
    //    public void createChoicePointAdvertisements(
    //    DesignEntity yesEntity, PeerGroup yesPG, String yesTrans, DesignEntity noEntity, PeerGroup noPG, String noTrans)
    //    throws Exception {
    //        LoopNetAdvertisement choiceLoopNetAdv = appUser.getLoopNets().getLoopNetByName(CHOICE_POINT_LOOP_NAME);
    //        /**Initial state of the choice loop */
    //        String choice_NOT_MADE_state = choiceLoopNetAdv.getFirstState();
    //        /**Second state of the choice loop */
    //        String choice_MADE_state = choiceLoopNetAdv.getLastState();
    //        String makeChoiceTrans = choiceLoopNetAdv.getFirstTransition();
    //        String yesName = YES_CHOICE + "_" + yesEntity.getFullName();
    //        String yesDesc = "Yes choice for: " + yesEntity.getFullName();
    //        String yesBaseName = yesName;
    //        String noName = NO_CHOICE + "_" + noEntity.getFullName();
    //        String noDesc = "No choice for: " + noEntity.getFullName();
    //        String noBaseName = noName;
    //        String iteration = "1";
    //        String entityType = CHOICE_POINT;
    //
    //        try {
    //            /**A. Yes choice entity */
    //            UserNamedEntityAdv yesChoiceAdv = createUserNamedEntityAdvertisement(
    //            yesName, yesDesc, null, choiceLoopNetAdv, yesBaseName, iteration, yesPG, entityType);
    //            UserNamedEntity yesChoiceEntity = new UserNamedEntity(yesChoiceAdv, choiceLoopNetAdv, appUser);
    //
    //            /**B. No choice entity */
    //            UserNamedEntityAdv noChoiceAdv = createUserNamedEntityAdvertisement(
    //            noName, noDesc, null, choiceLoopNetAdv, noBaseName, iteration, noPG, entityType);
    //            UserNamedEntity noChoiceEntity = new UserNamedEntity(noChoiceAdv, choiceLoopNetAdv, appUser);
    //
    //            /**Now all the constraints links between entities above: */
    //
    //            /**1. Creates a constraint link from the yesChoiceEntity to the yesEntity.
    //             * Constrains the yesTrans until YES chosen */
    //            LinkAdvertisement yesConLink = createLinkAdvertisement(
    //            (DesignEntity)yesChoiceEntity, yesEntity, DO_BEFORE, choice_MADE_state, yesTrans, yesPG, noPG);
    //
    //            /**2. Creates a constraint link from the noChoiceEntity to the noEntity.
    //             * Constrains the noTrans until NO chosen */
    //            LinkAdvertisement noConLink = createLinkAdvertisement(
    //            (DesignEntity)noChoiceEntity, noEntity, DO_BEFORE, choice_MADE_state, noTrans, noPG, yesPG);
    //
    //            /**3. Creates a constraint link from the yesChoiceEntity to the noEntity.
    //             * Prevents no entity progressing, if YES chosen */
    //            LinkAdvertisement yesCrossLink = createLinkAdvertisement(
    //            (DesignEntity)yesChoiceEntity, noEntity, DO_BEFORE, choice_NOT_MADE_state, noTrans, yesPG, noPG);
    //
    //            /**4. Creates a constraint link from the noChoiceEntity to the yesEntity.
    //             * Prevents yes entity progressing, if NO chosen */
    //            LinkAdvertisement noCrossLink = createLinkAdvertisement(
    //            (DesignEntity)noChoiceEntity, yesEntity, DO_BEFORE, choice_NOT_MADE_state, yesTrans, yesPG, noPG);
    //
    //            /**5. Creates a constraint link from the yesChoiceEntity to the noChoiceEntity.
    //             * Prevents NO choice, if YES chosen */
    //            LinkAdvertisement noCrossChoiceLink = createLinkAdvertisement(
    //            (DesignEntity)noChoiceEntity, (DesignEntity)yesChoiceEntity, DO_BEFORE, choice_NOT_MADE_state, makeChoiceTrans, noPG, yesPG);
    //
    //            /**6. Creates a constraint link from the yesChoiceEntity to the noChoiceEntity.
    //             * Prevents NO choice, if YES chosen */
    //            LinkAdvertisement yesCrossChoiceLink = createLinkAdvertisement(
    //            (DesignEntity)yesChoiceEntity, (DesignEntity)noChoiceEntity, DO_BEFORE, choice_NOT_MADE_state, makeChoiceTrans, yesPG, noPG);
    //        }
    //        catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //    }
    
    public void abandonEntity(DesignEntity entity, PeerGroup parentPG) {
        DesignEntityAdv entityAdv = entity.getAdv();
        String targetTrans = entity.getLoopNetAdv().getNextTransFromState(entity.getCurrentState());
        /**Abandon is a specially recognized roleName */
        createOnePolicyAdvertisement(entityAdv, targetTrans, ABANDON, parentPG);
    }
    
    /**@since 27 Oct. 2004 */
    public DesignEntity createIteratedEntity(DesignEntity entity, PeerGroup parentPG) throws Exception {
        UserNamedEntityAdv newAdv = createIteratedEntityAdvertisement(entity, parentPG);
        return new UserNamedEntity(newAdv, entity.getLoopNetAdv(), appUser);
    }
    
    /**Creates an adv for a new entity based on an existing one.
     * @since 6 Oct. 2004 */
    public UserNamedEntityAdv createIteratedEntityAdvertisement(DesignEntity entity, PeerGroup parentPG)
    throws Exception {
        if(entity == null) {
            return null;
        }
        ContentStorage existingPolicies = entity.getTransRoles();
        DesignEntityAdv oldAdv = entity.getAdv();
        /**Iteration is incremented here */
        String baseName = oldAdv.getBaseName();
        String newIteration = appUser.updateIteration(oldAdv.getIteration());
        //String newName = baseName + "_" + newIteration;
        String desc = oldAdv.getDescription();
        Date dateDue = oldAdv.getDateDue();
        LoopNetAdvertisement loopNetAdv = entity.getLoopNetAdv();
        
        String entityType = oldAdv.getEntityType();
        
        UserNamedEntityAdv newAdv = createUserNamedEntityAdvertisement(
        baseName, newIteration, desc, dateDue, loopNetAdv, parentPG, entityType);
        if(newAdv != null) {
            /**Create new policies by copying existing entity's policies (transRoles) */
            createAllPolicies(newAdv, existingPolicies, parentPG);
        }
        return newAdv;
    }
    
    /**@since 27 Oct. 2004 */
    public void cloneAllLinksAndEntities(Set links, DesignEntity parentEntity, PeerGroup parentPG, boolean incoming)
    throws Exception {
        if(links != null) {
            for(Iterator i = links.iterator(); i.hasNext(); ) {
                Link linkToClone = (Link)i.next();
                cloneLinkAndEntity(parentEntity, linkToClone, parentPG, incoming);
            }
        }
    }
    
    /**Recreates a graph structure from prototype-derived linnks and entities
     * @since 27 Oct 2004 */
    public void cloneLinkAndEntity(
    DesignEntity parentEntity, Link linkToClone, PeerGroup parentPG, boolean incoming)
    throws Exception {
        /**1. clone the linkedEntity */
        DesignEntity linkedEntity = getLinkedEntity(linkToClone, incoming);
        PeerGroup linkedEntityPG = getPeerGroupUsingChildEntity(linkedEntity);
        DesignEntity clonedLinkedEntity = createIteratedEntity(linkedEntity, parentPG);
        System.out.println(">>GOOD: Created a clonedLinkedEntity: " + clonedLinkedEntity.getFullName());
        LinkAdvertisement linkAdv = linkToClone.getLinkAdv();
        String sourceState = linkAdv.getSourceState();
        String targetTrans = linkAdv.getTransName();
        String constraintName = linkAdv.getConstraintName();
        DesignEntity sourceEntity, targetEntity;
        
        if(incoming) {
            sourceEntity = clonedLinkedEntity;
            targetEntity = parentEntity;
        }
        else {
            sourceEntity = parentEntity;
            targetEntity = clonedLinkedEntity;
        }
        /**2. create a new link between new source and target entities */
        createLinkAdvertisement(
        sourceEntity, targetEntity, constraintName, sourceState, targetTrans, linkedEntityPG, parentPG);
        System.out.println(">>GOOD: Created a cloned link for new parentEntity: " + parentEntity.getFullName());
    }
    
    /**@since 27 Oct. 2004 */
    public DesignEntity getLinkedEntity(Link link, boolean incoming) {
        if(incoming) {
            return link.getSourceEntity();
        }
        return link.getTargetEntity();
    }
    
    /**@since 27 Oct. 2004 */
    public PeerGroup getPeerGroupUsingChildEntity(DesignEntity childEntity) {
        String pgID_string = getPeerGroupIDStringUsingChildEntity(childEntity);
        return getPeerGroupUsingIDString(pgID_string);
    }
    
    /**@since 27 Oct. 2004 */
    public String getPeerGroupIDStringUsingChildEntity(DesignEntity childEntity) {
        ContentStorage allEntities = appUser.getUserNamedEntities();
        if(allEntities != null) {
            /** Keyed by parentPG_ID.toString() */
            for(Iterator i = allEntities.getKeySet().iterator(); i.hasNext(); ) {
                String curPgID_String = (String)i.next();
                Set oneRow = allEntities.getOneRowSet(curPgID_String);
                
                if(oneRow.contains(childEntity)) {
                    return curPgID_String;
                }
            }
        }
        return null;
    }
    
    /**@since 27 Oct. 2004. Revised Dec 13, 2004 */
    public PeerGroup getPeerGroupUsingIDString(String pgID_String) {
        Set allPgs = appUser.getCsPG().collapseAll();
        if(allPgs != null) {
            for(Iterator i = allPgs.iterator(); i.hasNext(); ) {
                PeerGroup curPG = (PeerGroup)i.next();
                String curPgID_String = curPG.getPeerGroupID().toString();
                if(curPgID_String.equals(pgID_String)) {
                    return curPG;
                }
            }
        }
        return null;
    }
    
    /**Join a peergroup in DPM = create a IPGMemberAdvertisement in a peergroup
     * @since 25 Nov. 2004 */
    public IPGMemberAdvertisement createMemberAdvertisement(Peer peer, PeerGroup parentPG)
    throws Exception {
        IPGMemberAdvertisement adv =
        (IPGMemberAdvertisement)AdvertisementFactory.newAdvertisement(IPGMemberAdvertisement.getAdvertisementType());
        adv.setAdvID(getNewCondatID(parentPG));
        adv.setPeerID(peer.getPeerID());
        /**Member's name */
        adv.setPeerName(peer.getName());
        /**ID of the peergroup the peer is a member of */
        adv.setPeerGroupID(parentPG.getPeerGroupID());
        /** The date at construction */
        adv.setDateCreate(new Date());
        
        /**Publish it */
        try {
            parentPG.getDiscoveryService().publish(adv, DiscoveryService.ADV);
            parentPG.getDiscoveryService().remotePublish(adv, DiscoveryService.ADV);
            /** Put it into the local cache */
            appUser.getMembers().addMemberAdv(adv, parentPG);
            return adv;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**In order to leave a peergroup in DPM, peer must first retreive a memberAdv to delete.
     * @since 25 Nov. 2004 */
    public Set retrieveExistingMemberAdvs(Peer peer, PeerGroup parentPG) {
        Set result = new HashSet();
        //String storageKey = parentPG.getPeerGroupNameX();
        //STORE_PG
        String storageKey = parentPG.getPeerGroupID().toString();
        if(appUser.getMembers().iterator(storageKey) != null) {
            for(Iterator i = appUser.getMembers().iterator(storageKey); i.hasNext(); ) {
                IPGMemberAdvertisement memberAdv = (IPGMemberAdvertisement)i.next();
                if(peer.getPeerID().equals(memberAdv.getPeerID()) &&
                !memberAdv.isDeleted(appUser)) {
                    /**Note: one peer may have multiple memberAdvs in a peergroup */
                    result.add(memberAdv);
                }
            }
        }
        return result;
    }
    
    /** Creates a UserNamedEntityAdv and publishes it [locally] in the parentPG */
    public UserNamedEntityAdv createUserNamedEntityAdvertisement(
    String baseName, String iteration, String desc, Date dateDue, LoopNetAdvertisement loopNetAdv, PeerGroup parentPG, String entityType)
    throws Exception {
        /** Be sure to make jxta aware of the new Advertisement class: see topFrame.startJxta() */
        UserNamedEntityAdv adv =
        (UserNamedEntityAdv)AdvertisementFactory.newAdvertisement(UserNamedEntityAdv.getAdvertisementType());
        adv.setDesignEntityID(getNewCondatID(parentPG));
        adv.setBaseName(baseName);
        /**The first time iterated. All subsequent uses are cloned from previous */
        adv.setIteration(iteration);
        /**Note: needed for user named entities: */
        adv.setEntityType(entityType);
        adv.setDescription(desc);
        
        /** The date at construction */
        adv.setDateCreate(new Date());
        if(dateDue != null) {
            adv.setDateDue(dateDue);
        }
        adv.setAuthorID(authorID);
        adv.setAuthorName(authorName);
        adv.setNetName(loopNetAdv.getNetName());
        
        /** Put it into the local cache */
        try {
            if(adv == null) {
                System.out.println("ERROR: created null uneAdv");
                return null;
            }
            parentPG.getDiscoveryService().publish(adv, DiscoveryService.ADV);
            parentPG.getDiscoveryService().remotePublish(adv, DiscoveryService.ADV);
            
            if(loopNetAdv != null) {
                UserNamedEntity newUNE = new UserNamedEntity(adv, loopNetAdv, appUser);
                appUser.getUserNamedEntities().addUserNamedEntity(newUNE, parentPG);
                /** Register as author for this entity */
                createRoleAdvertisement(newUNE, AUTHOR, parentPG);
                //
                ContentStorage peerRoles = new ContentStorage("java.lang.String", appUser);
                peerRoles.addString(AUTHOR, authorName);
                createHistoryAdvertisement
                (newUNE, CREATE_TRANS, loopNetAdv.getFirstState(), peerRoles, parentPG);
            }
            else {
                System.out.println("ERROR: null loopNetAdv in advUtils.createUserNamedEntityAdvertisement");
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return adv;
    }
    
    public LoopNetAdvertisement getLoopAdv(String loopName) {
        if(loopName == null) {
            System.out.println(">>Error: loopName is null");
            return null;
        }
        LoopNetAdvertisement loopAdv =
        appUser.getLoopNets().getLoopNetByName(loopName);
        if(loopAdv != null) {
            return loopAdv;
        }
        /**Else, need to make loopAdv using a loop drawing */
        return makeLoopNetAdv(loopName);
    }
    
    
    //not dependent on entity type
    //Want a choice of entities
    /**only used if nothing exists in system */
    public Set getBasicEntities() {
        Set set = new HashSet();
        set.add(DESIGN_TASK);
        set.add(DESIGN_PRODUCT);
        //set.add(CHOICE_POINT);
        set.add(TOI_COURSE);
        set.add(TOI_ASSIGNMENT);
        set.add(TOI_EXAM);
        return set;
    }
    
    /**Makes the basic loopNetAdvs */
    public Set makeBasicNetAdvs(JFrame frame) {
        Set loopNames = new HashSet();
        topFrame.showInfoDialog("Need to make basic loops." + NEWLINE +
        "This takes time but occurs only at first startup", frame);
        
        /**First makes sure that the basic loop names are present */
        loopNames.addAll(getBasicLoopNames());
        for (Iterator i = loopNames.iterator(); i.hasNext(); ) {
            String curLoopName = (String)i.next();
            makeLoopNetAdv(curLoopName);
        }
        return loopNames;
    }
    
    //dependent on entity type
    /**only used if nothing exists in system */
    public final static Set getBasicLoopNames() {
        Set set = new HashSet();
        set.add(DESIGN_TASK_LOOP_NAME);
        set.add(DESIGN_PRODUCT_LOOP_NAME);
        set.add(TOI_COURSE_LOOP_NAME);
        set.add(TOI_ASSIGNMENT_LOOP_NAME);
        set.add(TOI_EXAM_LOOP_NAME);
        //set.add(CHOICE_POINT_LOOP_NAME);
        return set;
    }
    
    public LoopNetAdvertisement makeLoopNetAdv(String loopName) {
        System.out.println("Attempting to make a loopNetAdv for loop: " + loopName);
        PeerGroup parentPG = topFrame.getDpmLoopsNet();
        LoopNetReader loopReader = new LoopNetReader(loopName, topFrame, parentPG, true); //true = open drawing
        if(loopReader != null) {
            LoopNetAdvertisement loopNetAdv = loopReader.getLoopNetAdv();
            if(loopNetAdv != null) {
                System.out.println("Made a loopNetAdv for loop: " + loopName);
                return loopNetAdv;
            }
        }
        else {
            System.out.println("Couldn't make a loopNetAdv for: " + loopName);
        }
        return null;
    }
    
    public synchronized ID getNewCondatID(PeerGroup parentPG) {
        try {
            ID newID = IDFactory.newCodatID(parentPG.getPeerGroupID());//, seed, in);
            if(newID!=null) {
                //System.out.println("Successfully created a new TaskAdv ID");
                return newID;
            }
        } catch (Exception ez) {
            System.out.println("Trouble making a condatID");
            return ID.nullID;
        }
        return null;
    }
    
    
    // PipeID pipeID = IDFactory.newPipeID((PeerGroupID)peerGroup.getPeerGroupID());
    
    /** Creates a TaskPolicyAdv and publishes it [locally] in the parentPG */
    /** NOTE: note that roleNames are a set */
    /** NOTE: first parameter is now an adv, not an entity */
    public PolicyAdvertisement createPolicyAdvertisement(DesignEntityAdv entityAdv, String targetTrans, Set roleNames, PeerGroup parentPG) {
        /** Be sure to make jxta aware of the new Advertisement class: see topFrame.startJxta() */
        PolicyAdvertisement policyAdv =
        (PolicyAdvertisement)AdvertisementFactory.newAdvertisement(PolicyAdvertisement.getAdvertisementType());
        
        policyAdv.setAdvID(getNewCondatID(parentPG));
        /**eliminate all EMPTY_STRINGS and add EMPTY_ENTRY if no contents */
        policyAdv.setRoles(cleanRoleNames(roleNames));
        policyAdv.setTransName(targetTrans);
        /**Target information */
        policyAdv.setDesignEntityID(entityAdv.getDesignEntityID());
        policyAdv.setBaseName(entityAdv.getBaseName());
        policyAdv.setIteration(entityAdv.getIteration());
        policyAdv.setEntityType(entityAdv.getEntityType());
        /**Other information */
        policyAdv.setDateCreate(new Date());
        policyAdv.setAuthorID(authorID);
        policyAdv.setAuthorName(authorName);
        
        /** Put it into the local cache */
        try {
            if(!entityAdv.isDeleted(appUser)) {
                parentPG.getDiscoveryService().publish(policyAdv, DiscoveryService.ADV);
                parentPG.getDiscoveryService().remotePublish(policyAdv, DiscoveryService.ADV);
                appUser.getEntityRelatives().addRelative(policyAdv, null);
                return policyAdv;
            }
            else {
                System.out.println("ERROR: user tried to add policy to deleted design entity");
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Set cleanRoleNames(Set roleNames) {
        if(roleNames == null || roleNames.isEmpty()) {
            roleNames.add(EMPTY_ENTRY);
            return roleNames;
        }
        for (Iterator i = roleNames.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            if(s.equals(EMPTY_STRING)) {
                roleNames.remove(s);
            }
        }
        return roleNames;
    }  
    
    public void setCommonAttributesFromDesignEntity(DesignEntityRelatedAdv adv, DesignEntity entity, PeerGroup parentPG) {
        adv.setAdvID(getNewCondatID(parentPG));
        adv.setDesignEntityID(entity.getDesignEntityID());
        adv.setBaseName(entity.getBaseName());
        adv.setIteration(entity.getIteration());
        adv.setDateCreate(new Date());
        adv.setAuthorID(authorID);
        adv.setAuthorName(authorName);
    }
    
    /** Creates a RoleAdv and publishes it [locally] in the parentPG
     * */
    public RoleAdvertisement createRoleAdvertisement(DesignEntity entity, String roleName, PeerGroup parentPG) {
        /** Be sure to make jxta aware of the new Advertisement class: see topFrame.startJxta() */
        RoleAdvertisement roleAdv =
        (RoleAdvertisement)AdvertisementFactory.newAdvertisement(RoleAdvertisement.getAdvertisementType());
        setCommonAttributesFromDesignEntity(roleAdv, entity, parentPG);
        /** The attribute of interest for this relatedAdv: */
        roleAdv.setRoleName(roleName);
        /** Put it into the local cache */
        try {
            if(!entity.isDeleted(appUser)) {
                parentPG.getDiscoveryService().publish(roleAdv, DiscoveryService.ADV);
                parentPG.getDiscoveryService().remotePublish(roleAdv, DiscoveryService.ADV);
                appUser.getEntityRelatives().addRelative(roleAdv, null);
                return roleAdv;
            }
            else {
                System.out.println("ERROR: user tried to add role to deleted design entity");
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Creates a InputAdv and publishes it [locally] in the parentPG
     * Need: */
    public InputAdvertisement createInputAdvertisement(DesignEntity entity, String transName, String roleName, PeerGroup parentPG) {
        /** Be sure to make jxta aware of the new Advertisement class: see topFrame.startJxta() */
        InputAdvertisement inputAdv =
        (InputAdvertisement)AdvertisementFactory.newAdvertisement(InputAdvertisement.getAdvertisementType());
        setCommonAttributesFromDesignEntity(inputAdv, entity, parentPG);
        /** The attribute of interest for this relatedAdv: */
        inputAdv.setTransName(transName);
        inputAdv.setRoleName(roleName);
        /** Put it into the local cache */
        try {
            if(!entity.isDeleted(appUser)) {
                parentPG.getDiscoveryService().publish(inputAdv, DiscoveryService.ADV);
                parentPG.getDiscoveryService().remotePublish(inputAdv, DiscoveryService.ADV);
                appUser.getEntityRelatives().addRelative(inputAdv, null);
                return inputAdv;
            }
            else {
                System.out.println("ERROR: user tried to add input to deleted design entity");
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Creates a HistoryAdv and publishes it [locally] in the parentPG
     * Need: */
    public HistoryAdvertisement createHistoryAdvertisement(DesignEntity entity, String transName, String stateName, ContentStorage peerRoles, PeerGroup parentPG) {
        HistoryAdvertisement historyAdv =
        (HistoryAdvertisement)AdvertisementFactory.newAdvertisement(HistoryAdvertisement.getAdvertisementType());
        setCommonAttributesFromDesignEntity(historyAdv, entity, parentPG);
        /** The attribute of interest for this relatedAdv: */
        historyAdv.setTransName(transName);
        historyAdv.setState(stateName);
        /** peerRoles is a content storage keyed by roleNames, with peerNames as values */
        historyAdv.setPeerRoles(peerRoles);
        /** Put it into the local cache */
        try {
            if(!entity.isDeleted(appUser)) {
                parentPG.getDiscoveryService().publish(historyAdv, DiscoveryService.ADV);
                parentPG.getDiscoveryService().remotePublish(historyAdv, DiscoveryService.ADV);
                appUser.getEntityRelatives().addRelative(historyAdv, null);
                return historyAdv;
            }
            else {
                System.out.println("ERROR: user tried to add history adv to deleted design entity");
                return null;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**Changes a entity's state if possible. Publishes a HistoryAdv when state change occurs */
    public void changeDesignEntityStateIfPossible(DesignEntity entity, PeerGroup parentPG) {
        if(entity.isDeleted(appUser)) {
            return;
        }
        /** if sufficient valid inputs exist */
        if(entity.stateCanChange()) {
            String curState = entity.getCurrentState();
            LoopNetAdvertisement loopNetAdv = entity.getLoopNetAdv();
            String nextTrans = loopNetAdv.getNextTransFromState(curState);
            String nextState = loopNetAdv.getNextState(curState);
            ContentStorage peerRoles = entity.getValidPeerRoles(nextTrans);
            createHistoryAdvertisement(entity, nextTrans, nextState, peerRoles, parentPG);
            
            /**If recycling this entity */
            if(loopNetAdv.getLastState().equals(curState)) {
                String incr = appUser.updateIteration(entity.getAdv().getIteration());
                String baseName = entity.getBaseName();
                try {
                    if(entity instanceof UserNamedEntity) {
                        /**Use entity as the prototype */
                        Prototype proto = new Prototype(entity, appUser);
                        NewDesignEntityForm form = new NewDesignEntityForm(parentPG, topFrame, proto, incr);
                        return;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            /**Show state change information to the app user */
            topFrame.showInfoDialog("DesignEntity " + entity.getFullName() + " has changed state" + NEWLINE +
            "From: " + curState + NEWLINE +
            "To: " + nextState + NEWLINE +
            "A new history advertisement has been created", topFrame);
        }
    }
    
    /** Creates a NetAdv and publishes it [locally] in the parentPG
     * This advertisement contains all the info needed to open a drawing in Renew,
     * as well as string descriptions of all states, transitions, and comments */
    public LoopNetAdvertisement createLoopNetAdvertisement(LoopNetReader loopReader, PeerGroup parentPG) {
        /** Be sure to make jxta aware of the new Advertisement class: see topFrame.startJxta() */
        LoopNetAdvertisement loopNetAdv =
        (LoopNetAdvertisement)AdvertisementFactory.newAdvertisement(LoopNetAdvertisement.getAdvertisementType());
        loopNetAdv.setAdvID(getNewCondatID(parentPG));
        loopNetAdv.setDateCreate(new Date());
        loopNetAdv.setAuthorID(authorID);
        loopNetAdv.setAuthorName(authorName);
        /** Loop information: */
        loopNetAdv.setNetName(loopReader.getLoopName());
        /** Note: an important step: add all the petriNet drawing content to the adv */
        loopNetAdv.setNetContent(drawingContentToString(loopReader.getLoopDrawing()));
        /** The following information duplicates the most important info from the drawing */
        loopNetAdv.setStates(new TreeSet(loopReader.getStates()));
        loopNetAdv.setTransitions(new TreeSet(loopReader.getTransitions()));
        loopNetAdv.setComments(new TreeSet(loopReader.getComments()));
        
        /** Put it into the local cache */
        try {
            parentPG.getDiscoveryService().publish(loopNetAdv, DiscoveryService.ADV);
            parentPG.getDiscoveryService().remotePublish(loopNetAdv, DiscoveryService.ADV);
            appUser.getLoopNets().addLoopNetAdv(loopNetAdv, parentPG);
            return loopNetAdv;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public StateNetAdvertisement createStateNetAdvertisement(Drawing drawing, PeerGroup parentPG) {
        /** Be sure to make jxta aware of the new Advertisement class: see topFrame.startJxta() */
        StateNetAdvertisement stateNetAdv =
        (StateNetAdvertisement)AdvertisementFactory.newAdvertisement(StateNetAdvertisement.getAdvertisementType());
        stateNetAdv.setAdvID(getNewCondatID(parentPG));
        stateNetAdv.setNetName(drawing.getName());
        /** Note: this important step */
        stateNetAdv.setNetContent(drawingContentToString(drawing));
        stateNetAdv.setDateCreate(new Date());
        stateNetAdv.setAuthorID(authorID);
        stateNetAdv.setAuthorName(authorName);
        
        /** Put it into the local cache */
        try {
            parentPG.getDiscoveryService().publish(stateNetAdv, DiscoveryService.ADV);
            parentPG.getDiscoveryService().remotePublish(stateNetAdv, DiscoveryService.ADV);
            //author.getStateNets().addStateNetAdv(stateNetAdv, parentPG);
            return stateNetAdv;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Gets a petriNet drawing from an open Renew Demonstrator application,
     * and converts its content to a String */
    public String drawingContentToString(Drawing drawing) {
        try {
            if(drawing != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                StorableOutput so = new StorableOutput(new DataOutputStream(baos));
                /** write the current drawing's content to output */
                so.writeStorable(drawing);
                /** convert this output to a string */
                return baos.toString();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Makes a file to the user's directory, that contains a netAdv's petriNet content */
    public void netAdvToFile(NetAdvertisement netAdv, Demonstrator petriNetApp) {
        try {
            String fileName =  System.getProperty("user.dir") + netAdv.getNetName() + ".rnw";
            /** If file exists already, do nothing. Correct? */
            if(new File(fileName).exists()) {
                return;
            }
            File file = new File(fileName);
            PrintWriter out = new PrintWriter(new FileWriter(file));
            /** Print the net content into the file */
            out.println(netAdv.getNetContent());
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /** NetAdv indicates which drawing file to open in the user directory */
    public void openDrawingUsingNetAdv(NetAdvertisement netAdv, Demonstrator petriNetApp) {
        /** assumes drawings get placed in this directory */
        String fileName =  System.getProperty("user.dir") + netAdv.getNetName() + ".rnw";
        petriNetApp.loadAndOpenDrawing(fileName);
    }
    
    /** Creates a linkAdv and publishes it [locally] in the parentPG
     * Need: */
    public LinkAdvertisement createLinkAdvertisement(DesignEntity sourceEntity, DesignEntity targetEntity, String constraintName, String sourceState, String targetTrans, PeerGroup sourcePG, PeerGroup targetPG) {
        /** Be sure to make jxta aware of the new Advertisement class: see topFrame.startJxta() */
        LinkAdvertisement linkAdv =
        (LinkAdvertisement)AdvertisementFactory.newAdvertisement(LinkAdvertisement.getAdvertisementType());
        
        linkAdv.setAdvID(getNewCondatID(sourcePG));
        linkAdv.setConstraintName(constraintName);
        linkAdv.setTransName(targetTrans);
        linkAdv.setSourceState(sourceState);
        
        /**Source information */
        linkAdv.setSourceID(sourceEntity.getDesignEntityID());
        linkAdv.setSourceBaseName(sourceEntity.getBaseName());
        linkAdv.setSourceIteration(sourceEntity.getIteration());
        linkAdv.setSourceType(sourceEntity.getEntityType());
        
        /**Target information */
        linkAdv.setTargetID(targetEntity.getDesignEntityID());
        linkAdv.setBaseName(targetEntity.getBaseName());
        linkAdv.setIteration(targetEntity.getIteration());
        linkAdv.setEntityType(targetEntity.getEntityType());
        
        /**Other information */
        linkAdv.setDateCreate(new Date());
        linkAdv.setAuthorID(authorID);
        linkAdv.setAuthorName(authorName);
        
        /**Only publish and send content storage if new link is ok */
        Link newLink = new Link(linkAdv, appUser);
        if(newLinkOK(newLink)) {
            try {
                String linkDesc;
                /**Checks if sources and target entities have not been deleted */
                if(newLink.isDeleted(appUser)) {
                    return null;
                }
                if(constraintName.equals(DO_BEFORE)) {
                    linkDesc = linkAdv.getStringDescriptionDoBeforeLong();
                }
                else {
                    linkDesc = linkAdv.getDescriptionBasic();
                }
                //String desc = linkAdv.getDescriptionBasic();
                topFrame.showInfoDialog("Link Created:" + NEWLINE + linkDesc, topFrame);
                
                /**Publish in both source and target PGs */
                sourcePG.getDiscoveryService().publish(linkAdv, DiscoveryService.ADV);
                sourcePG.getDiscoveryService().remotePublish(linkAdv, DiscoveryService.ADV);
                targetPG.getDiscoveryService().publish(linkAdv, DiscoveryService.ADV);
                targetPG.getDiscoveryService().remotePublish(linkAdv, DiscoveryService.ADV);
                /**Add to both source and target PGs. Used for tree displays */
                appUser.getEntityRelatives().addLinkToAllLinks(newLink, sourcePG);
                appUser.getEntityRelatives().addLinkToAllLinks(newLink, targetPG);
                
                /**Add to incoming and outgoing storage for entities. Used for Input/Output form */
                appUser.getEntityRelatives().addLinkToIncomingOutgoing(newLink);
                
                topFrame.refreshTreeWithNodeSelected();
                return linkAdv;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /** Checks for existing similar links, and
     * whether new link conflicts logically with existing ones */
    public boolean newLinkOK(Link link) {
        Link existingSame = appUser.getEntityRelatives().getExistingSame(link);
        /**To avoid duplicating existing links */
        if(existingSame != null) {
            topFrame.showErrorDialog("Cannot add new link: " + NEWLINE +
            link.getDescriptionBasic() + NEWLINE +
            "A similar one already exists: " + NEWLINE +
            existingSame.getDescriptionBasic(), topFrame);
            return false;
        }
        Link logicalConflict = appUser.getEntityRelatives().getExistingNonLogical(link);
        /**To avoid links that logically contradict existing links */
        if(logicalConflict != null) {
            topFrame.showErrorDialog("Cannot add new link: " + NEWLINE +
            link.getDescriptionBasic() + NEWLINE +
            "It logically conflicts with the existing link: " + NEWLINE +
            logicalConflict.getDescriptionBasic(), topFrame);
            return false;
        }
        return true;
    }
    
    /** Handles all the things that occur when the StateChangerNet fires its final transition */
    public void changeDesignEntityStateFromNet(DesignEntity entity, String transName, String stateName, ContentStorage peerRoles, PeerGroup parentPG) {
        createHistoryAdvertisement(entity, transName, stateName, peerRoles, parentPG);
        
        topFrame.showInfoDialog("NOTE: DesignEntity " + entity.getFullName() + " has changed state" +
        "A new history advertisement has been created", topFrame);
    }
    
    public void printMessage(String s) {
        topFrame.printMessage(s);
    }
    
    /** For everything in the requiredInputs content storage, make a PolicyAdv */
    public void createAllPolicies(DesignEntityAdv entityAdv, ContentStorage transRoles, PeerGroup parentPG) {
        if(entityAdv.isDeleted(appUser)) {
            //System.out.println("Error: attempted to create all policies for deleted design entity");
            return;
        }
        //Set allRolesInput = getAllPolicyRoles();
        if(transRoles != null) {
            for (Iterator i = transRoles.getKeySet().iterator(); i.hasNext(); ) {
                /** For each role name: */
                String transName = (String)i.next();
                Set roles = transRoles.getStringSet(transName);
                /** Make an advertisement and publish it in the parentPG */
                if(!roles.isEmpty()) {
                    PolicyAdvertisement policyAdv =
                    createPolicyAdvertisement(entityAdv, transName, roles, parentPG);
                    /** This also stores it in content storage */
                }
            }
        }
    }
    
    
    /** For all check boxes checked by users, makes a DeleteAdv
     * Only transitions named in transRolesToKeep are deleted, then recreated */
    public void deleteAndRecreateAllPolicies(DesignEntity entity, ContentStorage updatedTransRoles, PeerGroup parentPG) {
        if(entity.isDeleted(appUser)) {
            System.out.println("Error: entity.isDeleted(appUser) in advUtils.deleteAndRecreateAllPolicies");
            return;
        }
        //topFrame.test("Size of updatedTransRoles in advUtils", updatedTransRoles.size(), entity.getLoopNetAdv().getNumberOfTransitions());
        for(Iterator i = updatedTransRoles.getKeySet().iterator(); i.hasNext(); ) {
            /** Delete and recreate for each transition name: */
            String transName = (String)i.next();
            /**First delete the existing polAdv */
            Set polAdvs = getAllPolAdvsThisTransition(entity, transName);
            if(polAdvs != null) {
                System.out.println(">>Deleting/creating for transName: " + transName);
                deleteAllPolAdvs(polAdvs, parentPG);
                Set newRoleNames = updatedTransRoles.getOneRowSet(transName);
                
                if(newRoleNames != null &&
                !newRoleNames.isEmpty() &&
                !containsOnlyEmptyString(newRoleNames)) {
                    createPolicyAdvertisement(entity.getAdv(), transName, newRoleNames, parentPG);
                }
            }
        }
    }
    
    public boolean containsOnlyEmptyString(Set set) {
        return
        set != null &&
        set.size() == 1 &&
        set.iterator().next().equals(EMPTY_STRING);
    }
    
    /**@since 15 Oct. 2004 */
    public void deleteAllPolAdvs(Set allPolAdvs, PeerGroup parentPG) {
        for(Iterator i = allPolAdvs.iterator(); i.hasNext(); ) {
            PolicyAdvertisement polAdv = (PolicyAdvertisement)i.next();
            if(polAdv != null) {
                createDeleteAdvertisement(polAdv, parentPG);
            }
        }
    }
    
    /**@since 13 Oct. 2004 */
    public Set getAllPolAdvsThisTransition(DesignEntity entity, String transName) {
        Set polAdvsFound = new HashSet();
        if(appUser.policiesExist(entity)) {
            for (Iterator i = appUser.getPoliciesIterator(entity); i.hasNext(); ) {
                PolicyAdvertisement polAdv = (PolicyAdvertisement)i.next();
                String curTransName = polAdv.getTransName();
                if(curTransName.equals(transName) && !polAdv.isDeleted(appUser)) {
                    polAdvsFound.add(polAdv);
                }
            }
        }
        return polAdvsFound;
    }
    
    public void createOnePolicyAdvertisement(
    DesignEntityAdv entityAdv, String targetTrans, String roleName, PeerGroup parentPG) {
        HashSet roleSet = new HashSet();
        roleSet.add(roleName);
        createPolicyAdvertisement(entityAdv, targetTrans, roleSet, parentPG);
    }
    
    /** Geta the prefix, as an Integer, of a Name with a numeric prefix: e.g 0.x, 3.y, etc. */
    public Integer getPrefixNum(String nameWithPrefix) {
        int idx = nameWithPrefix.indexOf('.');
        return new Integer(nameWithPrefix.substring(0, idx));
    }
    
    /**Used in createDeleteAdv.
     * @since 28 Sept 2004 */
    public void flushAdvFromCS(Advertisement adv) {
        ContentStorage cs = null;
        if(adv instanceof ChatAdvertisement) {
            cs = appUser.getChatAdvs();
        }
        else if(adv instanceof PrivateChatAdvertisement) {
            cs = appUser.getPrivateChatAdvs();
        }
        else if(adv instanceof LoopNetAdvertisement) {
            cs = appUser.getLoopNets();
        }
        cs.removeItem(adv);
        /**If unhandled, do nothing */
    }
    
    public void flushEntityFromCS(UserNamedEntity entity) {
        ContentStorage cs = appUser.getUserNamedEntities();
        cs.removeItem(entity);
    }
    
    /**Creates an advertisment that deletes another adv */
    public DeleteAdvertisement createDeleteAdvertisement(Advertisement advToDelete, PeerGroup basePG) {
        /**Only create a DeleteAdvertisement if the deleter is also the author of the thing to delete */
        if(!appUserIsAuthor(advToDelete)) {
            topFrame.showErrorDialog("Only authors of objects are [currently] allowed to delete them" + NEWLINE +
            "Can't delete adv w/ ID: "  + advToDelete.getID().toString(), topFrame);
            return null;
        }
        /**Removes the adv from local content storage */
        //flushAdvFromCS(advToDelete);
        DeleteAdvertisement deleteAdv =
        (DeleteAdvertisement)AdvertisementFactory.newAdvertisement(DeleteAdvertisement.getAdvertisementType());
        
        ID idToDelete = advToDelete.getID();
        if(idToDelete.equals(ID.nullID)) {
            System.out.println("ERROR: new deleteAdv has a null idToDelete");
            return null;
        }
        //String idString = idToDelete.toString();
        //System.out.println("New DA. IdToDelete: " + idString);
        deleteAdv.setDeleteAdvID(idToDelete);
        
        /**ID of the deleteAdv created here */
        deleteAdv.setAdvID(getNewCondatID(basePG));
        deleteAdv.setDateCreate(new Date());
        deleteAdv.setAuthorID(authorID);
        deleteAdv.setAuthorName(authorName);
        /**The ID of the object being deleted */
        
        try {
            DiscoveryService discSvc = basePG.getDiscoveryService();
            /**Flush the advToDelete. Found in JXTA Wiki: Flushing advertisements from cache */
            //discSvc.flushAdvertisement(advToDelete);
            /**Publish in base PG. If found in any PG, if will end up in peer's deleteAdvs */
            discSvc.publish(deleteAdv, DiscoveryService.ADV);
            discSvc.remotePublish(deleteAdv, DiscoveryService.ADV);
            
            /**Add to content storage; Keyed by ObjToDelete's ID string */
            appUser.getDeleteAdvs().addDeleteAdv(deleteAdv);
            /**Remove the adv referred to in the deleteAdv */
            /**NOTE should this be used? */
            //removeAppropriateObjectFromCS(advToDelete);
            topFrame.refreshTreeWithNodeSelected();
            if(advToDelete instanceof RoleAdvertisement) {
                System.out.println("Just created a deleteAdv based on a roleAdv!");
            }
            return deleteAdv;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**Used?: new approach: don't actively remove from cs - just don't display, or add to cs in future...
     * may need to be reintroduced...*/
    /**Removes the adv referred to in a deleteAdv.
     * @since August 11, 2004 */
    //    public void removeAppropriateObjectFromCS(Advertisement advToDelete) {
    //        /**Make sure these types correspond to those displayed in PGTree */
    //        if(advToDelete instanceof LoopNetAdvertisement) {
    //            //NetAdvertisement advToDelete = (NetAdvertisement)advToDelete;
    //            appUser.getLoopNets().removeItem(advToDelete);
    //            return;
    //        }
    //
    //        /**Note: linkAdvs are not added to CS, or ERCS */
    //        //        if(advToDelete instanceof LinkAdvertisement) {
    //        //        }
    //        System.out.println("ERROR: unhandled object type in advUtils.removeAppropriateAdvFromCS()");
    //    }
    
    /**Note: checks if appUser is the author of the object */
    public boolean appUserIsAuthor(Object obj) {
        /**'authorID' in AdvUtils refers to the appUser - not the author of an adv */
        PeerID appUserID = this.authorID;
        
        /**Currently anyone can delete PeerGroupAdvertisements and IPGAdvs,
         * since these don't have authorID attributes */
        if(obj instanceof PeerGroupAdvertisement ||
        obj instanceof IPGAdvertisement) {
            return true;
        }
        /**List of objects that currently can be deleted: */
        if(obj instanceof DesignEntityAdv) {
            DesignEntityAdv adv = (DesignEntityAdv)obj;
            return adv.getAuthorID().equals(appUserID);
        }
        if(obj instanceof DesignEntity) {
            DesignEntity de = (DesignEntity)obj;
            return de.getAuthorID().equals(appUserID);
        }
        if(obj instanceof NetAdvertisement) {
            NetAdvertisement adv = (NetAdvertisement)obj;
            return adv.getAuthorID().equals(appUserID);
        }
        if(obj instanceof LinkAdvertisement) {
            LinkAdvertisement adv = (LinkAdvertisement)obj;
            return adv.getAuthorID().equals(appUserID);
        }
        if(obj instanceof Link) {
            Link link = (Link)obj;
            return link.getLinkAdv().getAuthorID().equals(appUserID);
        }
        if(obj instanceof ChatAdvertisement) {
            ChatAdvertisement chatAdv = (ChatAdvertisement)obj;
            return chatAdv.getAuthorID().equals(appUserID);
        }
        if(obj instanceof PrivateChatAdvertisement) {
            PrivateChatAdvertisement chatAdv = (PrivateChatAdvertisement)obj;
            return chatAdv.getAuthorID().equals(appUserID);
        }
        if(obj instanceof IPGMemberAdvertisement) {
            IPGMemberAdvertisement memberAdv = (IPGMemberAdvertisement)obj;
            return memberAdv.getPeerID().equals(appUserID);
        }
        System.out.println("ERROR: unhandled object type in AdvUtils.appUserIsAuthor()");
        return false;
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
