package dpm.content.advertisement;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.peer.*;


/** Used for deleteing any type of advertisement
 * @author cumming */
public class DesignEntityAdv extends Advertisement implements DeleteChecker {
    
    public static final String advIDTag = "AdvID";
    public static final String deleteAdvIDTag = "DeleteAdvID";
    public static final String designEntityIDTag = "DesignEntityID";
    //public static final String nameTag = "Name";
    public static final String baseNameTag = "BaseName";
    public static final String iterationTag = "Iteration";
    public static final String descTag = "Description";
    public static final String entityTypeTag = "DesignEntityType";
    public static final String dateCreateTag = "DateCreated";
    public static final String authorIDTag = "AuthorID";
    public static final String authorNameTag = "AuthorName";
    public static final String dateDueTag = "DateDue";
    /**Name of the loopNetAdv for the entity */
    public static final String netNameTag = "NetName";
    
    private static final String[] fields =
    {advIDTag, designEntityIDTag, baseNameTag, iterationTag, descTag, dateCreateTag, authorIDTag, authorNameTag, dateDueTag, netNameTag};
    
    protected ID advID =  ID.nullID;
    protected ID designEntityID = ID.nullID;
    protected String baseName = null;
    protected String iteration = null;
    protected String description = null;
    protected String entityType = null;
    protected Date dateCreate = null;
    protected Date dateDue = null;
    protected PeerID authorID = null;
    protected String authorName = null;
    protected String netName = null;
    //protected static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
    protected static DateFormat dateFormat =
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    public boolean isDeleted(Peer appUser) {
        String storageKey = getDesignEntityID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    public String getFullName() {
        return combineNames(baseName, iteration);
    }
    
    public String combineNames(String a, String b) {
        return a +  "_" + b;
    }
    
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:DesignEntityAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new DesignEntityAdv();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new DesignEntityAdv(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected DesignEntityAdv() {
    }
    
    protected DesignEntityAdv(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:DesignEntityAdv";
    }
    
    /** Builds a Java object from a structured text document (the adv) */
    private void initialize(Element root) {
    }
    
    /** Builds a structured text document from a Java object */
    public Document getDocument(MimeMediaType mediaType) {
        StructuredTextDocument adv = (StructuredTextDocument)
        StructuredDocumentFactory.newStructuredDocument(mediaType, getAdvertisementType());
        return adv;
    }
    
    /** {@inheritDoc} */
    public String[] getIndexFields() {
        return fields;
    }
    
    public synchronized ID getID() {
        return advID;
    }
    
    public String getStorageKey() {
        return null;
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
    
    /** Getter for property advID.
     * @return Value of property advID.
     *
     */
    public ID getAdvID() {
        return advID;
    }
    
    /** Setter for property advID.
     * @param advID New value of property advID.
     *
     */
    public void setAdvID(ID advID) {
        this.advID = advID;
    }
    
    
    /** Getter for property dateCreate.
     * @return Value of property dateCreate.
     *
     */
    public Date getDateCreate() {
        return dateCreate;
    }
    
    /** Setter for property dateCreate.
     * @param dateCreate New value of property dateCreate.
     *
     */
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }
    
    /** Getter for property designEntityID.
     * @return Value of property designEntityID.
     *
     */
    public net.jxta.id.ID getDesignEntityID() {
        return designEntityID;
    }
    
    /** Setter for property designEntityID.
     * @param designEntityID New value of property designEntityID.
     *
     */
    public void setDesignEntityID(net.jxta.id.ID designEntityID) {
        this.designEntityID = designEntityID;
    }
    
    /** Getter for property description.
     * @return Value of property description.
     *
     */
    public java.lang.String getDescription() {
        return description;
    }
    
    /** Setter for property description.
     * @param description New value of property description.
     *
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    /** Getter for property dateDue.
     * @return Value of property dateDue.
     *
     */
    public java.util.Date getDateDue() {
        return dateDue;
    }
    
    /** Setter for property dateDue.
     * @param dateDue New value of property dateDue.
     *
     */
    public void setDateDue(java.util.Date dateDue) {
        this.dateDue = dateDue;
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
    
    /** Getter for property netName.
     * @return Value of property netName.
     *
     */
    public java.lang.String getNetName() {
        return netName;
    }
    
    /** Setter for property netName.
     * @param netName New value of property netName.
     *
     */
    public void setNetName(java.lang.String netName) {
        this.netName = netName;
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
    
}
