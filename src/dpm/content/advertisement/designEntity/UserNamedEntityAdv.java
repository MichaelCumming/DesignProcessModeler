package dpm.content.advertisement.designEntity;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
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


/** Represents a description of what needs to be done. e.g. 'pour concrete' *
 * Needs a date: See: Calendar, DateFormat */
public class UserNamedEntityAdv extends DesignEntityAdv {
    
    private static final String[] fields =
    {designEntityIDTag, entityTypeTag, baseNameTag, iterationTag, descTag, dateCreateTag, authorIDTag, authorNameTag, netNameTag, dateDueTag};
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:UserNamedEntityAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new UserNamedEntityAdv();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new UserNamedEntityAdv(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected UserNamedEntityAdv() {
    }
    
   
    protected UserNamedEntityAdv(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:UserNamedEntityAdv";
    }
    
    public synchronized ID getID() {
        return designEntityID;
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
            
            if (elem.getName().equals(UserNamedEntityAdv.designEntityIDTag)) {
                try {
                    URL designEntityID = IDFactory.jxtaURL(elem.getTextValue());
                    setDesignEntityID(IDFactory.fromURL(designEntityID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable ID in advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad ID in advertisement");
                }
                continue;
            }
            if (elem.getName().equals(entityTypeTag)) {
                setEntityType(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(baseNameTag)) {
                setBaseName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(iterationTag)) {
                setIteration(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(descTag)) {
                setDescription(elem.getTextValue());
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
            if (elem.getName().equals(netNameTag)) {
                setNetName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(dateDueTag)) {
                try {
                    setDateDue(dateFormat.parse(elem.getTextValue()));
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
        if (getID().equals(ID.nullID))
            throw new IllegalStateException("User spec entity advertisement has no assigned ID");
        TextElement e = adv.createElement(designEntityIDTag, getDesignEntityID().toString());
        adv.appendChild(e);
        e = adv.createElement(entityTypeTag, getEntityType());
        adv.appendChild(e);
        e = adv.createElement(baseNameTag, getBaseName());
        adv.appendChild(e);
        e = adv.createElement(iterationTag, getIteration());
        adv.appendChild(e);
        if(getDescription() != null) {
            e = adv.createElement(descTag, getDescription());
            adv.appendChild(e);
        }
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("Entity advertisement has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        e = adv.createElement(netNameTag, getNetName());
        adv.appendChild(e);
        e = adv.createElement(dateDueTag, dateFormat.format(getDateDue()));
        adv.appendChild(e);
        
        return adv;
    }
    
}
