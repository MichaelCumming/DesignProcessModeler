/*
 * Link.java
 *
 * Created on March 24, 2004, 8:58 AM
 */

package dpm.content.constraint;

import dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement;
import dpm.peer.Peer;
import dpm.content.*;
import net.jxta.id.ID;
import dpm.dpmApp.desktop.*;


/**
 *
 * @author  cumming
 *A link represents any kind of link (possibly constraining) between two design entities.
 *e.g. A doBefore B
 */
public class Link extends Constraint implements DeleteChecker, DpmTerms {
    
    /** Creates a new instance of Link */
    public Link(LinkAdvertisement linkAdv, Peer appUser) {
        super(linkAdv, appUser);
    }
    
    public synchronized ID getID() {
        return getLinkAdv().getAdvID();
    }
    
    public boolean isDeleted(Peer appUser) {
        return getLinkAdv().isDeleted(appUser);
    }
    
    public boolean similarTo(Link otherLink) {
        LinkAdvertisement thisAdv = getLinkAdv();
        LinkAdvertisement otherAdv = otherLink.getLinkAdv();
        
        String thisSourceType = thisAdv.getSourceType();
        String thisTargetType = thisAdv.getEntityType();
        String thisSourceState = thisAdv.getSourceState();
        String thisTargetTrans = thisAdv.getTransName();
        
        String otherSourceType = otherAdv.getSourceType();
        String otherTargetType = otherAdv.getEntityType();
        String otherSourceState = otherAdv.getSourceState();
        String otherTargetTrans = otherAdv.getTransName();
        
        return
        thisSourceType.equals(otherSourceType) &&
        thisTargetType.equals(otherTargetType) &&
        thisSourceState.equals(otherSourceState) &&
        thisTargetTrans.equals(otherTargetTrans);
    }
    
    /**@since 12 Oct. 2004 */
    public DesignEntity getSourceEntity() {
        String sourceIDs = getLinkAdv().getSourceID().toString();
        return (DesignEntity)appUser.getUserNamedEntities().getEntityByIDString(sourceIDs);
    }
    
    /**@since 12 Oct. 2004 */
    public DesignEntity getTargetEntity() {
        String targetIDs = getLinkAdv().getTargetID().toString();
        return (DesignEntity)appUser.getUserNamedEntities().getEntityByIDString(targetIDs);
    }
    
    public LinkAdvertisement getLinkAdv() {
        return (LinkAdvertisement)getAdv();
    }
    
    public String getDescriptionBasic() {
        return getLinkAdv().getDescriptionBasic();
    }
    
    public String getSourceBaseName() {
        return getLinkAdv().getSourceBaseName();
    }
    public String getSourceIteration() {
        return getLinkAdv().getSourceIteration();
    }
    
    public String getSourceFullName() {
        return adv.combineNames(getSourceBaseName(), getSourceIteration());
    }
    
    public String getTargetBaseName() {
        return getLinkAdv().getBaseName();
    }
    public String getTargetIteration() {
        return getLinkAdv().getIteration();
    }
    
    public String getTargetFullName() {
        return adv.combineNames(getTargetBaseName(), getTargetIteration());
    }
    
    public ID getSourceID() {
        return getLinkAdv().getSourceID();
    }
    
    public ID getTargetID() {
        return getLinkAdv().getTargetID();
    }
    
    private boolean inputLinksDeleted(Link compare) {
        if(this.isDeleted(appUser) || compare.isDeleted(appUser)) {
            return true;
        }
        return false;
    }
    
    
    //----------------------------------------------------------------
    /**Methods that test for similarity and non-logical precedent relations */
    
    /**Whether compare describes the same relation as this*/
    public boolean sameLink(Link compare) {
        if(inputLinksDeleted(compare)) {
            return false;
        }
        boolean result =
        /**AxB <=> AxB */
        getDescriptionBasic().equals(compare.getDescriptionBasic());
        //||
        /**A doBefore B <=> B doAfter A */
        //(entityNamesReversed(compare) && oneDoAfterOneDoBefore(compare)));
        //printResult(result, "sameLink", compare);
        return result;
    }
    
    /**describes circular dependencies that are self-contradictory */
    public boolean conflictsLogicallyWithExisting(Link compare) {
        if(inputLinksDeleted(compare)) {
            return false;
        }
        boolean result;
        /**Only links having to with doBefore/doAfter are checked for their logic */
        if(concernsPrecedence(compare)) {
            result =
            /**A doBefore B conflicts with: B doBefore A */
            entityNamesReversed(compare) && bothDoBefores(compare);
            //||
            /**A doAfter B conflicts with: B doAfter A */
            //(entityNamesReversed(compare) && bothDoAfters(compare)) ||
            /**A doBefore B conflicts with: A doAfter B and v.v.*/
            //(entityNamesSame(compare) && oneDoAfterOneDoBefore(compare));
            //printResult(result, "conflictsLogicallyWithExisting", compare);
            return result;
        }
        result = false; //false = logically acceptable
        //printResult(result, "conflictsLogicallyWithExisting", compare);
        return result;
    }
    
    public boolean concernsPrecedence(Link compare) {
        boolean result =
        /**Both linkAdvs deal with doBefore or doAfter */
        isDoBefore() && compare.isDoBefore();
        //printResult(result, "concernsPrecedence", compare);
        return result;
    }
    
    public boolean isDoBefore() {
        return getLinkAdv().isDoBefore();
    }
    
    /** AxB <=> ByA */
    public boolean entityNamesReversed(Link compare) {
        boolean result =
        /**AxB <=> CyA*/
        getSourceFullName().equals(compare.getTargetFullName()) &&
        /**BxA <=> AyA*/
        getTargetFullName().equals(compare.getSourceFullName());
        //printResult(result, "entityNamesReversed", compare);
        return result;
    }
    
    public boolean entityNamesSame(Link compare) {
        boolean result =
        /**AxB <=> AyB */
        getSourceFullName().equals(compare.getSourceFullName()) &&
        getTargetFullName().equals(compare.getTargetFullName());
        //printResult(result, "entityNamesSame", compare);
        return result;
    }
    
    public boolean bothDoBefores(Link compare) {
        boolean result =
        /**A doBefore B <=> C doBefore D */
        getAdv().getConstraintName().equals(DO_BEFORE) &&
        compare.getAdv().getConstraintName().equals(DO_BEFORE);
        //printResult(result, "bothDoBefores", compare);
        return result;
    }
    
    //    public boolean bothDoAfters(Link compare) {
    //        boolean result =
    //        /**A doAfter B <=> C doAfter D */
    //        getAdv().getConstraintName().equals(DO_AFTER) &&
    //        compare.getAdv().getConstraintName().equals(DO_AFTER);
    //        printResult(result, "bothDoAfters", compare);
    //        return result;
    //    }
    
    //    public boolean oneDoAfterOneDoBefore(Link compare) {
    //        boolean result =
    //        /**A doAfter B / C doBefore D */
    //        ((getAdv().getConstraintName().equals(DO_AFTER) &&
    //        compare.getAdv().getConstraintName().equals(DO_BEFORE))
    //        ||
    //        (getAdv().getConstraintName().equals(DO_BEFORE) &&
    //        compare.getAdv().getConstraintName().equals(DO_AFTER)));
    //        printResult(result, "oneDoAfterOneDoBefore", compare);
    //        return result;
    //    }
    
    public void printResult(boolean result, String methodName, Link compare) {
        System.out.println(
        "TEST LinkAdv: " + methodName + ": " + result + NEWLINE +
        getDescriptionBasic() + NEWLINE +
        compare.getDescriptionBasic() + NEWLINE +
        "-------------------");
    }
    
    
}
