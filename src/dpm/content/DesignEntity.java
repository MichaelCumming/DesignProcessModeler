/*
 * DesignEntity.java
 *
 * Created on March 10, 2004, 1:36 PM
 */

package dpm.content;

import dpm.content.advertisement.DesignEntityAdv;
import dpm.content.advertisement.designEntity.UserNamedEntityAdv;
import dpm.content.advertisement.designEntity.related.HistoryAdvertisement;
import dpm.content.advertisement.designEntity.related.InputAdvertisement;
import dpm.content.advertisement.designEntity.related.RoleAdvertisement;
import dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.content.comparator.designEntity.HistoryAdvComparator;
import dpm.content.constraint.Link;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.text.DateFormat;
import java.util.*;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;



/**
 *
 * @author  mc9p
 * @since January 25, 2004, 1:36 PM
 */
public abstract class DesignEntity implements DpmTerms, DeleteChecker, AbandonChecker {
    protected DesignEntityAdv adv;
    protected ID designEntityID;
    //protected DpmAppTopFrame topFrame;
    /**The baseName to which iteration annotations are added. e.g. T1 */
    protected String baseName;
    protected String iteration;
    protected String entityType;
    
    /**Describes how many times around the state loop this entity has travelled */
    //protected int iteration;
    protected PeerID authorID;
    protected String authorName;
    /** Each design entity needs one of these to know its possible states */
    protected LoopNetAdvertisement loopNetAdv;
    /** this refers to the user of the application */
    protected Peer appUser;
    /**@since 16 Oct. 2004 */
    //protected Prototype prototype;
    /**True if entity has no Link constraints (i.e. doBefores), AND no Policy constraints.
     * If noConstraints == true, then entity is not able to change state */
    protected boolean noConstraints;
    /** the DONE_STRING refers to when a design entity is deemed to be 'completed' */
    /** IN_POSTNEG_STATE means that performance has been completed. Could also be: IN_RETIRED_STATE */
    /**@since 5 Oct 2004 */
    protected Comparator historyAdvComparator = new HistoryAdvComparator();
    protected static DateFormat dateFormat =
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    
    /** Normal constructor. Creates a new instance of a design entity  */
    public DesignEntity(DesignEntityAdv adv, LoopNetAdvertisement loopNetAdv, Peer appUser) {
        this.adv = adv;
        /** loopNetAdv contains all the information needed to know the design entity's states and transitions */
        this.loopNetAdv = loopNetAdv;
        this.appUser = appUser;
        if(adv instanceof UserNamedEntityAdv) {
            entityType = USER_NAMED_ENTITY;
        }
        this.designEntityID = adv.getDesignEntityID();
        this.baseName = adv.getBaseName();
        this.iteration = adv.getIteration();
        this.authorID = adv.getAuthorID();
        this.authorName = adv.getAuthorName();
    }
    
    public boolean equalsAccordingToEntityID(DesignEntity entity) {
        return designEntityID.equals(entity.getDesignEntityID());
    }
    
    public String getFullName() {
        return adv.getFullName();
    }
    
    public boolean atFirstIteration() {
        return iteration.equals("1");
    }
    
    public String getDescription() {
        return adv.getDescription();
    }
    
    
    /**One simple measure of the activity of this entity based on number of history advs */
    public int getActivity() {
        String storageKey = getDesignEntityID().toString();
        /**return the size of histAdvs keyed to this entity */
        return appUser.getEntityRelatives().getHistories().getOneRowSet(storageKey).size();
    }
    
    /**'Abandoned' means that a user has authored an abandon polAdv to one of its transitions.
     * @since 7 Oct. 2004 */
    public boolean isAbandoned(Peer appUser) {
        String storageKey = getDesignEntityID().toString();
        //        if(appUser==null) System.out.println("appUser is null");
        //        if(appUser.getEntityRelatives()==null) System.out.println("entRels are null");
        //        if(appUser.getEntityRelatives().getAbandonedEntities()==null) System.out.println("abonEnts are null");
        
        return appUser.getEntityRelatives().getAbandonedEntities().storageKeyExists(storageKey);
    }
    
    /**'Deleted' means that a user has authored a deleteAdv.
     * @since 7 Oct. 2004 */
    public boolean isDeleted(Peer appUser) {
        String storageKey = getDesignEntityID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    /**Returns a set of polAdvs */
    public Set getAbandonedPolAdvs() {
        return appUser.getEntityRelatives().getAbandonedEntities().getOneRowSet(designEntityID.toString());
    }
    
    public PolicyAdvertisement getAbandonedAdv() {
        if(isAbandoned(appUser)) {
            Set polAdvs = getAbandonedPolAdvs();
            for(Iterator i = polAdvs.iterator(); i.hasNext(); ) {
                PolicyAdvertisement polAdv = (PolicyAdvertisement)i.next();
                return polAdv;
            }
        }
        return null;
    }
    
    public String getAbandonedString() {
        if(isAbandoned(appUser)) {
            PolicyAdvertisement polAdv = getAbandonedAdv();
            if(polAdv == null) {
                return null;
            }
            String authorName = polAdv.getAuthorName();
            String date = dateFormat.format(polAdv.getDateCreate());
            String message = "Note: entity " + getFullName() + " was abandoned by: " + authorName + NEWLINE +
            "on " + date + NEWLINE +
            NEWLINE;
            return message;
        }
        return EMPTY_STRING;
    }
    
    
    
    /**Considers whether state can change from both a local perspective (entity's inputs and roles),
     * and links from other design entities */
    public boolean stateCanChange() {
        /**If NOT OK to use this entity (= has been deleted), don't bother to check state change */
        if(this.isDeleted(appUser)) {
            System.out.println("in stateCanChange(): " + getFullName() + " found in deleteAdvs");
            return false;
        }
        if(noCurrentConstraints()) {
            return false;
        }
        return
        /**No user inputs are needed that prevent it from changing state */
        stateCanChangeLinks()
        &&
        /**No incoming links are present that prevent it from changing state */
        stateCanChangeInputs();
    }
    
    /**Prevents entity from changing state if there are no constraints.
     * Prevents runaway state change.
     * @since 25 Oct. 2004 */
    public boolean noCurrentConstraints() {
        return
        noInputConstraintsNextTrans() &&
        noLinkConstraintsNextTrans();
    }
    
    /**Considers existence of policy constraints, not whether they have been satisfied
     * @since Dec 20, 2004 */
    public boolean noInputConstraintsNextTrans() {
        String nextTrans = loopNetAdv.getNextTransFromState(getCurrentState());
        Set inputReqts = getRequiredRolesAnyPeer(nextTrans);
        
        return inputReqts.isEmpty() || inputReqts == null;
    }
    
    /**This considers existence of link constraints, not whether they have been satisfied
     * @since Dec 20, 2004 */
    public boolean noLinkConstraintsNextTrans() {
        Set incomingLinks = new HashSet();
        if(getIncomingLinks() != null && !getIncomingLinks().isEmpty()) {
            for(Iterator i = getIncomingLinks().iterator(); i.hasNext(); ) {
                Link curLink = (Link)i.next();
                /**If link transName concerns the entity's next transition */
                if(linkRelevantToNextTrans(curLink)) {
                    incomingLinks.add(curLink);
                }
            }
        }
        /**If empty, therefore no incoming links next trans */
        return incomingLinks.isEmpty() || incomingLinks == null;
    }
    
    public boolean hasNotBeenCompleted(String completionState) {
        /**true if currentState is before, false if equal or later */
        return loopNetAdv.before(getCurrentState(), completionState);
    }
    
    /**Determines if a constraining (doBefore) is relevant w.r.t. current state of entity */
    public boolean linkRelevantToNextTrans(Link link) {
        String linkTrans = link.getLinkAdv().getTransName();
        String nextEntityTrans = loopNetAdv.getNextTransFromState(getCurrentState());
        return (linkTrans.equals(nextEntityTrans));
    }
    
    
    
    /**Find the incoming entities, defined by doBefore links, and checks whether they are complete.
     * NOTE: only those entities relevant to current state of entity */
    public Set getRelevantIncompleteIncomingEntities() {
        Set incompletes = new HashSet();
        if(getIncomingLinks() != null && !getIncomingLinks().isEmpty()) {
            for(Iterator i = getIncomingLinks().iterator(); i.hasNext(); ) {
                Link curLink = (Link)i.next();
                /**If link transName concerns the entity's next transition */
                if(linkRelevantToNextTrans(curLink)) {
                    DesignEntity sourceEntity = curLink.getSourceEntity();
                    if(sourceEntity != null) {
                        /**'Source state' is the state that signifies completion, for the purpose of the link */
                        if(sourceEntity.hasNotBeenCompleted(curLink.getLinkAdv().getSourceState())) {
                            incompletes.add(sourceEntity);
                        }
                    }
                }
            }
        }
        return incompletes;
    }
    
    /**Retrieves all incomplete entities, not only those relevant to current state */
    public Set getAllIncompleteIncomingEntities() {
        Set incompletes = new HashSet();
        if (getIncomingLinks() != null && !getIncomingLinks().isEmpty()) {
            for(Iterator i = getIncomingLinks().iterator(); i.hasNext(); ) {
                Link curLink = (Link)i.next();
                DesignEntity sourceEntity = curLink.getSourceEntity();
                if(sourceEntity != null) {
                    /**'Source state' is the state that signifies completion, for the purpose of the link */
                    if(sourceEntity.hasNotBeenCompleted(curLink.getLinkAdv().getSourceState())) {
                        incompletes.add(sourceEntity);
                    }
                }
            }
        }
        return incompletes;
    }
    
    public Set getAllIncompleteNamesSet() {
        return getNamesSet(getAllIncompleteIncomingEntities());
    }
    public Set getRelevantIncompleteNamesSet() {
        return getNamesSet(getRelevantIncompleteIncomingEntities());
    }
    
    public Set getNamesSet(Set inputSet) {
        Set outputSet = new HashSet();
        for(Iterator i = inputSet.iterator(); i.hasNext(); ) {
            DesignEntity entity = (DesignEntity)i.next();
            outputSet.add(entity.getFullName());
        }
        if(outputSet.isEmpty()) {
            outputSet.add(NONE);
        }
        return outputSet;
    }
    
    /**Determines whether the Entity has any links into it that prevent it from changing state */
    public boolean stateCanChangeLinks() {
        /**If entity has been deleted, don't bother to check state change */
        if(isDeleted(appUser)) {
            return false;
        }
        /**whether links constrain state change for entity at its current state */
        Set incompletes = getRelevantIncompleteIncomingEntities();
        boolean result = (incompletes == null || incompletes.isEmpty());
        //System.out.println(getFullName() + ": stateCanChangeLinks(): " + result);
        return result;
    }
    
    /**Returns a set of incoming links associated with entity */
    public Set getIncomingLinks() {
        return appUser.getEntityRelatives().getIncomingLinks().getOneRowSet(designEntityID.toString());
    }
    /**Returns a set of outgoing links associated with entity */
    public Set getOutgoingLinks() {
        return appUser.getEntityRelatives().getOutgoingLinks().getOneRowSet(designEntityID.toString());
    }
    
    /**Returns a set of incoming or outgoing links
     * according to a constraint getFullName() (e.g. doBefore, friendOf, etc.) */
    public Set getLinksByConstraintName(String linkType, boolean incoming) {
        Set allLinks = null;
        if(incoming == true) {
            allLinks = appUser.getEntityRelatives().getIncomingLinks().getOneRowSet(designEntityID.toString());
        }
        else {
            allLinks = appUser.getEntityRelatives().getOutgoingLinks().getOneRowSet(designEntityID.toString());
        }
        if(allLinks == null) {
            return null;
        }
        Set filteredLinks = new HashSet();
        for(Iterator i = allLinks.iterator(); i.hasNext(); ) {
            Link curLink = (Link)i.next();
            String curLinkType = curLink.getAdv().getConstraintName();
            if(curLinkType.equals(linkType)) {
                filteredLinks.add(curLink);
            }
        }
        return filteredLinks;
    }
    
    /**Checks if current inputs from peers, allow state to change */
    public boolean stateCanChangeInputs() {
        //boolean result = false;
        String nextTrans = loopNetAdv.getNextTransFromState(getCurrentState());
        /** Check if state has already changed */
        if(historyAdvAlreadyExists(nextTrans)) {
            return false;
        }
        /**If NOT OK to use this entity (= has been deleted), don't bother to check state change */
        if(isDeleted(appUser)) {
            return false;
        }
        /** set of all input requirements, as defined by DesignEntityPolicyAdvs (set of role names) */
        Set requirements = getRequiredRolesAnyPeer(nextTrans);
        //System.out.println("Requirements-roles: " + getName() + ": "+ appUser.getUserNamedEntities().stringSet2String(requirements));
        /**If there are no requirements, then state can change */
        //System.out.println("Requirements-roles size: " + getName() + ": " + requirements.size());
        if(requirements == null ||
        requirements.isEmpty() ||
        (requirements.size()==1 && requirements.contains(EMPTY_STRING))) {
            //System.out.println("reqs null OR empty: " + getName());
            return true;
        }
        else {
            //System.out.println("reqs not empty: " + getName());
        }
        /** set of all inputted role names (set of role names) */
        Set validInputs = getValidInputsAnyPeer(nextTrans);
        
        //result = validInputs.containsAll(requirements);
        //System.out.println(getFullName() + ": stateCanChangeInputs(): " + validInputs.containsAll(requirements));
        return validInputs.containsAll(requirements);
    }
    
    
    /** Makes a 2D content storage table for this design entity, used to determine tasks's current state */
    //okx
    public String getCurrentState() {
        /** content storage used to 'add a dimension' back to tashHistAdvs for this design entity */
        ContentStorage tempHistoryTable =
        new ContentStorage("dpm.content.advertisement.designEntity.related.HistoryAdvertisement", appUser);
        
        if(appUser.historiesExist(this)) {
            for(Iterator i = appUser.getHistoriesIterator(this); i.hasNext(); ) {
                HistoryAdvertisement historyAdv = (HistoryAdvertisement)i.next();
                String stateName = historyAdv.getState();
                /** add to cs: table ordered by storageKey X date of adv */
                tempHistoryTable.addItem(stateName, historyAdv, historyAdvComparator);
            }
        }
        else {
            return "No historyAdvs available to determine state";
        }
        /** Retreive the latest histAdv (determined by date), with the greatest (=latest) state */
        HistoryAdvertisement latest =
        (HistoryAdvertisement)tempHistoryTable.getFirst(tempHistoryTable.getGreatestKeyValue());
        return latest.getState();
    }
    
    //    public String getIteratedNameString() {
    //        return new String(getFullName() + "_" + adv.getIteration());
    //    }
    
    public String getLoopNetString() {
        if(loopNetAdv==null) {
            return "null loopNetAdv";
        }
        return "ok loopNetAdv";
    }
    
    public String getNextTransition() {
        String curState = getCurrentState();
        return loopNetAdv.getNextTransFromState(curState);
    }
    
    
    /** For one design entity, all the policyAdvs pertaining to one transition */
    /** policies = roles NEEDED for one transition */
    //ok---
    public Set getRequiredRolesAnyPeer(String transName) {
        Set roles = new HashSet();
        if(appUser.policiesExist(this)) {
            for (Iterator i = appUser.getPoliciesIterator(this); i.hasNext(); ) {
                PolicyAdvertisement polAdv = (PolicyAdvertisement)i.next();
                if(!polAdv.isDeleted(appUser)) {
                    /** if this adv refers to this transition */
                    if(polAdv.getTransName().equals(transName)) {
                        roles.addAll(polAdv.getRoles());
                    }
                }
            }
        }
        //System.out.println(getFullName() + ": getRequiredRolesAnyPeer(" + transName + "): " + stringSet2String(roles));
        return roles;
    }
    
    
    
    /** For one design entity, all the roleAdvs created by one peer */
    /** Note: roles apply to the whole design entity - not specific transitions */
    /** roles = roles assumed by ONE peer, for the WHOLE design entity */
    //ok---
    public HashSet getAssumedRoleNamesThisAppUser() {
        HashSet roles = new HashSet();
        /** 'appUser' here refers to the user of the application */
        if(appUser.rolesExist(this)) {
            /** iterator through all roleAdvs discovered by the appUser */
            for (Iterator i = appUser.getRolesIterator(this); i.hasNext(); ) {
                RoleAdvertisement roleAdv = (RoleAdvertisement)i.next();
                /** if this appUser created this adv and therefore assumed this role */
                if(roleAdv.getAuthorID().equals(appUser.getPeerID())) {
                    if(!roleAdv.isDeleted(appUser)) {
                        roles.add(roleAdv.getRoleName());
                    }
                }
            }
        }
        //System.out.println(getFullName() + ": getAssumedRoleNamesThisAppUser(): " + stringSet2String(roles));
        return roles;
    }
    
    /**@since 4 Oct 2004 */
    public HashSet getAssumedRoleAdvsThisAppUser(DpmAppTopFrame topFrame) {
        HashSet roleAdvs = new HashSet();
        /** 'appUser' here refers to the user of the application */
        if(appUser.rolesExist(this)) {
            /** iterator through all roleAdvs discovered by the appUser */
            for (Iterator i = appUser.getRolesIterator(this); i.hasNext(); ) {
                RoleAdvertisement roleAdv = (RoleAdvertisement)i.next();
                /** if this appUser created this adv and therefore assumed this role */
                if(roleAdv.getAuthorID().equals(appUser.getPeerID())) {
                    if(!roleAdv.isDeleted(appUser)) {
                        roleAdvs.add(roleAdv);
                    }
                }
            }
        }
        //System.out.println(getFullName() + ": getAssumedRoleNamesThisAppUser(): " + stringSet2String(roles));
        return roleAdvs;
    }
    
    /** Returns roles that both required by the design entity, and are assumed by the appUser */
    //ok---
    public Set getUsefulRolesAssumedThisAppUser(String transName) {
        Set required = getRequiredRolesAnyPeer(transName); //ok---
        Set assumed = getAssumedRoleNamesThisAppUser(); //ok---
        /**For transitions that do not require any specific role input: anyone can make an input */
        //assumed.add(ANY_ROLE);
        /** Represents the intersection of the two sets */
        required.retainAll(assumed);
        //System.out.println(getFullName() + ": getUsefulRolesAssumedThisAppUser(" + transName + "): " + stringSet2String(required));
        return required;
    }
    
    //Inputs authored by ALL peers, for one transition
    //ok---
    public Set getValidInputsAnyPeer(String transName) {
        Set roles = new HashSet();
        if(appUser.inputsExist(this)) {
            for (Iterator i = appUser.getInputsIterator(this); i.hasNext(); ) {
                InputAdvertisement inputAdv = (InputAdvertisement)i.next();
                /** if this adv refers to this transition */
                if(inputAdv.getTransName().equals(transName) &&
                /** if input adv provides a valid input */
                inputIsValidAnyPeer(transName, inputAdv)) { //ok---
                    roles.add(inputAdv.getRoleName());
                }
            }
        }
        //System.out.println(getFullName() + ": getValidInputsAnyPeer(" + transName + "): " + stringSet2String(roles));
        return roles;
    }
    
    /**Creates a cs table of role names and names of peer who fulfil them.
     * CS: keyed by role names. with peer names as values */
    public ContentStorage getValidPeerRoles(String transName) {
        ContentStorage peerRoles = new ContentStorage("java.lang.String", appUser);
        if(appUser.inputsExist(this)) {
            for (Iterator i = appUser.getInputsIterator(this); i.hasNext(); ) {
                InputAdvertisement inputAdv = (InputAdvertisement)i.next();
                /** if this adv refers to this transition */
                if(inputAdv.getTransName().equals(transName) &&
                /** if input adv provides a valid input */
                inputIsValidAnyPeer(transName, inputAdv)) { //ok---
                    peerRoles.addString(inputAdv.getRoleName(), inputAdv.getAuthorName());
                }
            }
        }
        return peerRoles;
    }
    
    /** Determines whether a role input provided by a peer is for a role he has assumed */
    //applies to any taskInputAdv. used mostly to check inputAdvs prior to publishing them
    //ok---
    public boolean inputIsValidThisAppUser(String transName, InputAdvertisement taskInputAdv) {
        String inputRoleName = taskInputAdv.getRoleName();
        Set genuineResponsibilities = getUsefulRolesAssumedThisAppUser(transName); //ok---
        /**For transitions that do not require any specific role input: in which anyone can make an input */
        //genuineResponsibilities.add(ANY_ROLE);
        /** Determines whether the inputrole is both assumed and required */
        return genuineResponsibilities.contains(inputRoleName);
    }
    
    //ok---
    public boolean inputIsValidAnyPeer(String transName, InputAdvertisement taskInputAdv) {
        String inputRoleName = taskInputAdv.getRoleName();
        Set genuineResponsibilities = getRequiredRolesAnyPeer(transName); //ok---
        /** Determines whether the inputrole is both assumed and required */
        return genuineResponsibilities.contains(inputRoleName);
    }
    
    /** Inputs authored by one appUser, for one transition */
    //ok---
    public Set getValidInputsThisAppUser(String transName) {
        Set roles = new HashSet();
        if(appUser.inputsExist(this)) {
            for (Iterator i = appUser.getInputsIterator(this); i.hasNext(); ) {
                InputAdvertisement inputAdv = (InputAdvertisement)i.next();
                /** if this adv refers to this transition */
                if(inputAdv.getTransName().equals(transName) &&
                /** if this appUser authored this adv */
                inputAdv.getAuthorID().equals(appUser.getPeerID()) &&
                /** if input adv provides a valid input */
                inputIsValidThisAppUser(transName, inputAdv)) { //ok---
                    roles.add(inputAdv.getRoleName());
                }
            }
        }
        //System.out.println(getFullName() + ": getValidInputsThisAppUser(" + transName + "): " + stringSet2String(roles));
        return roles;
    }
    
    /** For one design entity, all the role inputs still required to be added by one appUser,
     * pertaining to one transition */
    //ok---
    public Set getStillNeededInputsThisAppUser(String transName) {
        Set requiredFromPeer = getUsefulRolesAssumedThisAppUser(transName); //ok---
        /** here 'appUser' refers the user of the application */
        Set receivedFromPeer = getValidInputsThisAppUser(transName); //ok---
        /** remove all elements from needed that have been received */
        requiredFromPeer.removeAll(receivedFromPeer);
        /** if required is now empty, then 'stillNeeded' is an empty set - which is good */
        
        //System.out.println(getFullName() + ": getStillNeededInputsThisAppUser(" + transName + "): " + stringSet2String(requiredFromPeer));
        return requiredFromPeer;
    }
    
    public boolean noPoliciesNextTrans() {
        String nextTrans = loopNetAdv.getNextTransFromState(getCurrentState());
        //Set roles = getRequiredRolesAnyPeer(nextTrans);
        return getRequiredRolesAnyPeer(nextTrans).isEmpty();
    }
    
    public boolean linksOnlyPreventStateChange() {
        /**State can't change due to links, but can change due to inputs */
        return !stateCanChangeLinks() && stateCanChangeInputs();
    }
    
    //    public boolean nextTransHasANY_ROLEPolicy() {
    //        String nextTrans = loopNetAdv.getNextTransFromState(getCurrentState());
    //        return getRequiredRolesAnyPeer(nextTrans).contains(ANY_ROLE);
    //    }
    
    /** Describes inputs NEEDED. Returns a string describing required roles for ALL transitions */
    //ok---
    public String getAllInputsNeededStringAnyPeer() {
        String s = new String();
        if(appUser.policiesExist(this)) {
            ContentStorage transRoles = getTransRoles();
            TreeSet transitions = loopNetAdv.getTransitions();
            
            /** all possible transitions */
            for(int i = 0; i < transitions.size(); i++ ) {
                Set roles = new HashSet();
                String curTrans = loopNetAdv.getValue(i, transitions);
                s += "Transition: " + curTrans + NEWLINE +
                "Input needed from roles: ";
                if(transRoles.iterator(curTrans) != null) {
                    for (Iterator j = transRoles.iterator(curTrans); j.hasNext(); ) {
                        String role = (String)j.next();
                        roles.add(role);
                    }
                    s += stringSet2String(roles);
                }
                else {
                    s += "none";
                }
                s += NEWLINE + NEWLINE;
            }
        }
        return s;
    }
    
    /**Prints out all content of a design entity for a JTextPane */
    public String getAllEntityContent() {
        String result = new String();
        String storageKey = getDesignEntityID().toString();
        String abandonS = EMPTY_STRING;
        
        if(isAbandoned(appUser)) {
            abandonS = "Abandoned:" + NEWLINE +
            appUser.getEntityRelatives().getAbandonedEntities().getOneRowString(storageKey) + NEWLINE +
            NEWLINE;
        }
        
        result =
        abandonS +
        "Policies:" + NEWLINE +
        appUser.getEntityRelatives().getPolicies().getOneRowString(storageKey) + NEWLINE +
        NEWLINE +
        "Roles:" + NEWLINE +
        appUser.getEntityRelatives().getRoles().getOneRowString(storageKey) + NEWLINE +
        NEWLINE +
        "Inputs:" + NEWLINE +
        appUser.getEntityRelatives().getInputs().getOneRowString(storageKey) + NEWLINE +
        NEWLINE +
        "Histories:" + NEWLINE +
        appUser.getEntityRelatives().getHistories().getOneRowString(storageKey) + NEWLINE +
        NEWLINE +
        "IncomingLinks:" + NEWLINE +
        appUser.getEntityRelatives().getIncomingLinks().getOneRowString(storageKey) + NEWLINE +
        NEWLINE +
        "OutgoingLinks:" + NEWLINE +
        appUser.getEntityRelatives().getOutgoingLinks().getOneRowString(storageKey);
        
        return result;
    }
    
    /** Describes inputs NEEDED. Returns a content storage with transition names as keys */
    //ok---
    public ContentStorage getTransRoles() {
        ContentStorage transRoles = new ContentStorage("java.lang.String", appUser);
        if (appUser.policiesExist(this)) {
            for (Iterator i = appUser.getPoliciesIterator(this); i.hasNext(); ) {
                PolicyAdvertisement polAdv = (PolicyAdvertisement)i.next();
                if(!polAdv.isDeleted(appUser)) {
                    String transName = polAdv.getTransName();
                    Set roles =  polAdv.getRoles();
                    /** First sort them using content storage as temp storage */
                    transRoles.addStringSet(transName, roles);
                }
            }
        }
        return transRoles;
    }
    
    /** Returns a set of strings describing required roles */
    //ok---
    public String getInputsReceivedStringAnyPeer(String nextTrans) {
        Set roles = getValidInputsAnyPeer(nextTrans); //ok---
        if (roles.isEmpty()) {
            roles.add("none");
        }
        return "Next transition: " + nextTrans + NEWLINE +
        "> input received from roles: " + stringSet2String(roles);
    }
    
    public Set entitySet2StringSet(Set entitySet) {
        HashSet outputSet = new HashSet();
        if (entitySet != null) {
            for (Iterator i = entitySet.iterator(); i.hasNext(); ) {
                DesignEntity de = (DesignEntity)i.next();
                outputSet.add(de.getFullName());
            }
        }
        return outputSet;
    }
    
    /**Useful method */
    public String getDesignEntityPrintOut(Set deInputSet, String messageAtStart) {
        Set outputSet = entitySet2StringSet(deInputSet);
        if (outputSet==null) {
            outputSet = new HashSet();
        }
        if (outputSet.isEmpty()) {
            outputSet.add("none");
        }
        return messageAtStart + NEWLINE + stringSet2String(outputSet);
    }
    
    /** Returns a set of strings describing required roles */
    //ok---
    public String getInputsNeededStringAnyPeer(String nextTrans) {
        Set roles = getRequiredRolesAnyPeer(nextTrans); //ok---
        if (roles.isEmpty()) {
            roles.add("none");
        }
        return "Next transition: " + nextTrans + NEWLINE +
        "> " + stringSet2String(roles);
    }
    
    public String stringSet2String(Set set) {
        String result = new String();
        for(Iterator i = set.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            if(i.hasNext()) {
                result += s + ", ";
            }
            else {
                result += s;
            }
        }
        return result;
    }
    
    public String designEntitySet2String(Set set) {
        String result = new String();
        for(Iterator i = set.iterator(); i.hasNext(); ) {
            DesignEntity de = (DesignEntity)i.next();
            if(i.hasNext()) {
                result += de.toString() + ", ";
            }
            else {
                result += de.toString();
            }
        }
        return result;
    }
    
    public boolean historyAdvAlreadyExists(String transName) {
        if(appUser.historiesExist(this)) {
            for(Iterator i = appUser.getHistoriesIterator(this); i.hasNext(); ) {
                HistoryAdvertisement histAdv = (HistoryAdvertisement)i.next();
                if(histAdv.getTransName().equals(transName)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /** Getter for property authorID.
     * @return Value of property authorID.
     *
     */
    public PeerID getAuthorID() {
        return authorID;
    }
    
    /** Setter for property authorID.
     * @param authorID New value of property authorID.
     *
     */
    public void setAuthorID(PeerID authorID) {
        this.authorID = authorID;
    }
    
    /** Getter for property authorName.
     * @return Value of property authorName.
     *
     */
    public java.lang.String getAuthorName() {
        return authorName;
    }
    
    /** Setter for property authorName.
     * @param authorName New value of property authorName.
     *
     */
    public void setAuthorName(java.lang.String authorName) {
        this.authorName = authorName;
    }
    
    /** Getter for property loopNetAdv.
     * @return Value of property loopNetAdv.
     *
     */
    public LoopNetAdvertisement getLoopNetAdv() {
        return loopNetAdv;
    }
    
    /** Setter for property loopNetAdv.
     * @param loopNetAdv New value of property loopNetAdv.
     *
     */
    public void setLoopNetAdv(LoopNetAdvertisement loopNetAdv) {
        this.loopNetAdv = loopNetAdv;
    }
    
    /** Getter for property appUser.
     * @return Value of property appUser.
     *
     */
    public Peer getAppUser() {
        return appUser;
    }
    
    /** Setter for property appUser.
     * @param appUser New value of property appUser.
     *
     */
    public void setAppUser(Peer appUser) {
        this.appUser = appUser;
    }
    
    /** Getter for property designEntityID.
     * @return Value of property designEntityID.
     *
     */
    public ID getDesignEntityID() {
        return designEntityID;
    }
    
    /** Setter for property designEntityID.
     * @param designEntityID New value of property designEntityID.
     *
     */
    public void setDesignEntityID(ID designEntityID) {
        this.designEntityID = designEntityID;
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
    
    /** Getter for property adv.
     * @return Value of property adv.
     *
     */
    public DesignEntityAdv getAdv() {
        return adv;
    }
    
    /** Setter for property adv.
     * @param adv New value of property adv.
     *
     */
    public void setAdv(DesignEntityAdv adv) {
        this.adv = adv;
    }
    
    /** Getter for property baseName.
     * @return Value of property baseName.
     *
     */
    public java.lang.String getBaseName() {
        return baseName;
    }
    
    /** Setter for property baseName.
     * @param baseName New value of property baseName.
     *
     */
    public void setBaseName(java.lang.String baseName) {
        this.baseName = baseName;
    }
    
    /** Getter for property iteration.
     * @return Value of property iteration.
     *
     */
    public java.lang.String getIteration() {
        return iteration;
    }
    
    /** Setter for property iteration.
     * @param iteration New value of property iteration.
     *
     */
    public void setIteration(java.lang.String iteration) {
        this.iteration = iteration;
    }
    
    /** Getter for property iteration.
     * @return Value of property iteration.
     *
     */
    
    
}
