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
import de.renew.simulator.NetInstance;
import de.renew.simulator.Net;
import de.renew.shadow.ShadowNet;
import java.text.ParseException;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;


/**  */
public class NetAdvertisement extends Advertisement {
    public static final String advIDTag = "AdvID";
    public static final String netNameTag = "NetName";
    public static final String netContentTag = "NetContent";
    public static final String dateCreateTag = "DateCreated";
    public static final String authorIDTag = "AuthorID";
    public static final String authorNameTag = "AuthorName";
    //
    private static final String[] fields =
    {advIDTag, netNameTag, netContentTag, dateCreateTag, authorIDTag, authorNameTag};
    //
    protected ID advID =  ID.nullID;
    protected String netName = null;
    protected String netContent = null;
    protected Date dateCreate = null;
    protected PeerID authorID = null;
    protected String authorName = null;
    
    //protected static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
    protected static DateFormat dateFormat = 
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:NetAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new NetAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new NetAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected NetAdvertisement() {
    }
    
    protected NetAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:NetAdv";
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
    
    /** Getter for property netContent.
     * @return Value of property netContent.
     *
     */
    public java.lang.String getNetContent() {
        return netContent;
    }
    
    /** Setter for property netContent.
     * @param netContent New value of property netContent.
     *
     */
    public void setNetContent(java.lang.String netContent) {
        this.netContent = netContent;
    } 
  
    
}
