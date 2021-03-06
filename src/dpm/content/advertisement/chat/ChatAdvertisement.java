package dpm.content.advertisement.chat;

import dpm.content.DeleteChecker;
import dpm.content.advertisement.ChatAdv;
import dpm.peer.Peer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.text.ParseException;
import java.util.Enumeration;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;



public class ChatAdvertisement extends ChatAdv  implements DeleteChecker {
    private static final String[] fields =
    {advIDTag, messageTag, dateCreateTag, authorIDTag, authorNameTag};
    
    public boolean isDeleted(Peer appUser) {
        String storageKey = getID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:ChatAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new ChatAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new ChatAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected ChatAdvertisement() {
    }
    
    protected ChatAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:ChatAdv";
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
            
            if (elem.getName().equals(advIDTag)) {
                try {
                    URL advID = IDFactory.jxtaURL(elem.getTextValue());
                    setAdvID(IDFactory.fromURL(advID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable ID in advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad ID in advertisement");
                }
                continue;
            }
            if (elem.getName().equals(messageTag)) {
                setMessage(elem.getTextValue());
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
                    throw new IllegalArgumentException("Unusable author ID in advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in advertisement");
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
        if (getID().equals(ID.nullID))
            throw new IllegalStateException("Chat advertisement has no assigned ID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        e = adv.createElement(messageTag, getMessage());
        adv.appendChild(e);
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("Entity advertisement has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        
        return adv;
    }
    
    /** {@inheritDoc} */
    public String[] getIndexFields() {
        return fields;
    }
    
}
