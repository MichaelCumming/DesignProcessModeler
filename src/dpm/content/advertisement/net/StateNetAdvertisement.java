package dpm.content.advertisement.net;

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


/** 
 * @author cumming */
public class StateNetAdvertisement extends NetAdvertisement {
    
    private static final String[] fields =
    {advIDTag, netNameTag, netContentTag, dateCreateTag, authorIDTag, authorNameTag};
    
    //protected static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
    protected static DateFormat dateFormat = 
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:StateChangerNetAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new StateNetAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new StateNetAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected StateNetAdvertisement() {
    }
    
    protected StateNetAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:StateChangerNetAdv";
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
                    throw new IllegalArgumentException("Unusable advID in state net advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad advID in state net advertisement");
                }
                continue;
            }
            if (elem.getName().equals(netNameTag)) {
                setNetName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(netContentTag)) {
                setNetContent(elem.getTextValue());
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
            if (elem.getName().equals(authorIDTag)) {
                try {
                    URL authorID = IDFactory.jxtaURL(elem.getTextValue());
                    setAuthorID((PeerID)IDFactory.fromURL(authorID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable author ID in net advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in net advertisement");
                }
                continue;
            }
            if (elem.getName().equals(authorNameTag)) {
                setAuthorName(elem.getTextValue());
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
            throw new IllegalStateException("Net adv has no assigned advID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        e = adv.createElement(netNameTag, getNetName());
        adv.appendChild(e);
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("Net adv has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        //NOTE!
        //        e = adv.createElement(netContentTag, getNetContent());
        //        adv.appendChild(e);
        
        return adv;
    }
    
    /** {@inheritDoc} */
    public String[] getIndexFields() {
        return fields;
    }
    
    public synchronized ID getID() {
        return advID;
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
