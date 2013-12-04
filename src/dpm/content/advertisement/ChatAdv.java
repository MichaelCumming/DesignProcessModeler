package dpm.content.advertisement;

import dpm.content.DeleteChecker;
import dpm.peer.Peer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;


/**  */
public abstract class ChatAdv extends Advertisement implements DeleteChecker {
    public static final String advIDTag = "AdvID";
    public static final String messageTag = "MessageName";
    public static final String dateCreateTag = "DateCreated";
    public static final String authorIDTag = "AuthorID";
    public static final String authorNameTag = "AuthorName";
    //
    private static final String[] fields =
    {advIDTag, messageTag, dateCreateTag, authorIDTag, authorNameTag};
    //
    protected ID advID =  ID.nullID;
    protected String message = null;
    protected Date dateCreate = null;
    protected PeerID authorID = null;
    protected String authorName = null;
    
    //protected static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
    protected static DateFormat dateFormat =
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);

    
    public boolean isDeleted(Peer appUser) {
        String storageKey = getID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    /** {@inheritDoc} */
    public abstract static class Instantiator implements AdvertisementFactory.Instantiator {
    };
    
    /** Private Constructor, use the instantiator */
    protected ChatAdv() {
    }
    
    protected ChatAdv(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:ChatAdv";
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
    
    /** Getter for property message.
     * @return Value of property message.
     *
     */
    public java.lang.String getMessage() {
        return message;
    }
    
    /** Setter for property message.
     * @param message New value of property message.
     *
     */
    public void setMessage(java.lang.String message) {
        this.message = message;
    }
    
}
