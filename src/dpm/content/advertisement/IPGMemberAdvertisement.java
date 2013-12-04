package dpm.content.advertisement;

import dpm.content.DeleteChecker;
import dpm.peer.Peer;
import java.text.DateFormat;
import java.util.Date;
import net.jxta.document.Advertisement;
import net.jxta.document.*;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.peer.*;
import net.jxta.id.*;
import net.jxta.peergroup.*;
import java.util.*;
import java.net.*;
import java.text.*;



/** Once a task attains a certain state, this is advertised using a TaskDoBeforeAdvertisement
 * @author cumming */
public class IPGMemberAdvertisement extends Advertisement implements DeleteChecker {
    
    public static final String advIDTag = "AdvID";
    public static final String peerIDTag = "PeerID";
    public static final String peerNameTag = "PeerName";
    public static final String peerGroupIDTag = "PeerGroupID";
    public static final String dateCreateTag = "DateCreated";
    
    private static final String[] fields =
    {advIDTag, peerIDTag, peerNameTag, peerGroupIDTag, dateCreateTag};
    
    protected ID advID =  ID.nullID;
    protected ID peerID =  ID.nullID;;
    protected String peerName = null;
    protected ID peerGroupID = ID.nullID;
    protected Date dateCreate = null;
    
    protected static DateFormat dateFormat =
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    public boolean isDeleted(Peer appUser) {
        String storageKey = getID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:IPGMemberAdvertisement";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new IPGMemberAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new IPGMemberAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected IPGMemberAdvertisement() {
    }
    
    protected IPGMemberAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:IPGMemberAdvertisement";
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
            if (elem.getName().equals(peerIDTag)) {
                try {
                    URL peerID = IDFactory.jxtaURL(elem.getTextValue());
                    setPeerID((PeerID)IDFactory.fromURL(peerID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable peerID in MemberAdv");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad peerID in MemberAdv");
                }
                continue;
            }
            if (elem.getName().equals(peerNameTag)) {
                setPeerName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(peerGroupIDTag)) {
                try {
                    URL peerGroupID = IDFactory.jxtaURL(elem.getTextValue());
                    setPeerGroupID((PeerGroupID)IDFactory.fromURL(peerGroupID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable peerGroupID in MemberAdv");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad peerGroupID in MemberAdv");
                }
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
            throw new IllegalStateException("MemberAdv has no assigned advID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        
        if (getPeerID().equals(ID.nullID))
            throw new IllegalStateException("MemberAdv has no assigned peerID");
        e = adv.createElement(peerIDTag, getPeerID().toString());
        adv.appendChild(e);
        
        e = adv.createElement(peerNameTag, getPeerName());
        adv.appendChild(e);
        
        if (getPeerGroupID().equals(ID.nullID))
            throw new IllegalStateException("MemberAdv has no assigned peergroup ID");
        e = adv.createElement(peerGroupIDTag, getPeerGroupID().toString());
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
    
    /**
     * Getter for property peerID.
     * @return Value of property peerID.
     */
    public ID getPeerID() {
        return peerID;
    }
    
    /**
     * Setter for property peerID.
     * @param peerID New value of property peerID.
     */
    public void setPeerID(ID peerID) {
        this.peerID = peerID;
    }
    
    /**
     * Getter for property peerName.
     * @return Value of property peerName.
     */
    public java.lang.String getPeerName() {
        return peerName;
    }
    
    /**
     * Setter for property peerName.
     * @param peerName New value of property peerName.
     */
    public void setPeerName(java.lang.String peerName) {
        this.peerName = peerName;
    }
    
    /**
     * Getter for property peerGroupID.
     * @return Value of property peerGroupID.
     */
    public ID getPeerGroupID() {
        return peerGroupID;
    }
    
    /**
     * Setter for property peerGroupID.
     * @param peerGroupID New value of property peerGroupID.
     */
    public void setPeerGroupID(ID peerGroupID) {
        this.peerGroupID = peerGroupID;
    }
    
}
