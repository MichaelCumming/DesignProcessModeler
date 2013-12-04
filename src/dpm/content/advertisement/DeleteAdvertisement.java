package dpm.content.advertisement;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;



/** Once a task attains a certain state, this is advertised using a TaskDoBeforeAdvertisement
 * @author cumming */
public class DeleteAdvertisement extends Advertisement {
    public static final String advIDTag = "AdvID";
    public static final String deleteAdvIDTag = "DeleteAdvID";
    public static final String dateCreateTag = "DateCreated";
    public static final String authorIDTag = "AuthorID";
    public static final String authorNameTag = "AuthorName";
    
    private static final String[] fields =
    {advIDTag, deleteAdvIDTag, dateCreateTag, authorIDTag, authorNameTag};
    
    protected ID advID =  ID.nullID;
    protected ID deleteAdvID =  ID.nullID;
    protected Date dateCreate = null;
    protected PeerID authorID = null;
    protected String authorName = null;
    
    protected static DateFormat dateFormat =
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:DeleteAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new DeleteAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new DeleteAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected DeleteAdvertisement() {
    }
    
    protected DeleteAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:DeleteAdv";
    }
    
    /** Builds a Java object from a structured text document (the adv) */
    private void initialize(Element root) {
        if (!TextElement.class.isInstance(root))
            throw new IllegalArgumentException(getClass().getName() + " only supports TextElement");
        TextElement doc = (TextElement)root;
        if (!doc.getName().equals(getAdvertisementType()))
            throw new IllegalArgumentException("Could not construct : " + getClass().getName() + " from doc containing a '" +
            doc.getName() + "'. Should be : " + getAdvertisementType());
        Enumeration elements = doc.getChildren();
        //
        while (elements.hasMoreElements()) {
            TextElement elem = (TextElement)elements.nextElement();
            //
            if (elem.getName().equals(advIDTag)) {
                try {
                    URL advID = IDFactory.jxtaURL(elem.getTextValue());
                    setAdvID(IDFactory.fromURL(advID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable advID in history advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad advID in history advertisement");
                }
                continue;
            }
            if (elem.getName().equals(deleteAdvIDTag)) {
                try {
                    URL deleteAdvID = IDFactory.jxtaURL(elem.getTextValue());
                    setDeleteAdvID(IDFactory.fromURL(deleteAdvID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable deleteAdvID in delete advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad deleteAdvID in delete advertisement");
                }
                continue;
            }
            
            if (elem.getName().equals(authorIDTag)) {
                try {
                    URL authorID = IDFactory.jxtaURL(elem.getTextValue());
                    setAuthorID((PeerID)IDFactory.fromURL(authorID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable author ID in history advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in history advertisement");
                }
                continue;
            }
            if (elem.getName().equals(authorNameTag)) {
                setAuthorName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(dateCreateTag)) {
                try {
                    setDateCreate(dateFormat.parse(elem.getTextValue()));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                continue;
            }
        }
    }
    
    /** Builds a structured text document from a Java object */
    public Document getDocument(MimeMediaType mediaType) {
        StructuredTextDocument adv = (StructuredTextDocument)
        StructuredDocumentFactory.newStructuredDocument(mediaType, getAdvertisementType());
        if (adv instanceof Attributable) {
            ((Attributable)adv).addAttribute("xmlns:jxta", "http://jxta.org");
        }
        if (getAdvID().equals(ID.nullID))
            throw new IllegalStateException("DeleteAdv has no assigned advID");
        TextElement e = adv.createElement(DeleteAdvertisement.advIDTag, getAdvID().toString());
        adv.appendChild(e);
        
        if (getDeleteAdvID().equals(ID.nullID))
            throw new IllegalStateException("DeleteAdv has no assigned deleteAdvID");
        e = adv.createElement(DeleteAdvertisement.deleteAdvIDTag, getDeleteAdvID().toString());
        adv.appendChild(e);
        
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("DeleteAdv has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        
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
    
    /** Getter for property deleteAdvID.
     * @return Value of property deleteAdvID.
     *
     */
    public ID getDeleteAdvID() {
        return deleteAdvID;
    }
    
    /** Setter for property deleteAdvID.
     * @param deleteAdvID New value of property deleteAdvID.
     *
     */
    public void setDeleteAdvID(ID deleteAdvID) {
        this.deleteAdvID = deleteAdvID;
    }
    
}
