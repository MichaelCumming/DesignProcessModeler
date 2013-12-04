package dpm.content.advertisement.designEntity.related;

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
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;


/** Note: role adv applies to the whole design entity - not specific transitions
 * @author cumming */
public class UnAssumeRoleAdvertisementX extends DesignEntityRelatedAdv {
    public static final String roleNameTag = "RoleName";
    private String roleName;
    
    private static final String[] fields =
    {advIDTag, designEntityIDTag, baseNameTag, descTag, dateCreateTag, authorIDTag, authorNameTag, roleNameTag};
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:UnAssumeRoleAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new UnAssumeRoleAdvertisementX();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new UnAssumeRoleAdvertisementX(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    private UnAssumeRoleAdvertisementX() {
    }
    
    private UnAssumeRoleAdvertisementX(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:UnAssumeRoleAdv";
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
                    URL aID = IDFactory.jxtaURL(elem.getTextValue());
                    setAdvID(IDFactory.fromURL(aID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable advID in design entity role advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad advID in design entity role advertisement");
                }
                continue;
            }
            if (elem.getName().equals(designEntityIDTag)) {
                try {
                    URL designEntityID = IDFactory.jxtaURL(elem.getTextValue());
                    setDesignEntityID(IDFactory.fromURL(designEntityID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable design entity ID in role advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad design entity ID in role advertisement");
                }
                continue;
            }
            if (elem.getName().equals(baseNameTag)) {
                setBaseName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(authorIDTag)) {
                try {
                    URL aID = IDFactory.jxtaURL(elem.getTextValue());
                    setAuthorID((PeerID)IDFactory.fromURL(aID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable author ID in role advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in role advertisement");
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
            if (elem.getName().equals(roleNameTag)) {
                setRoleName(elem.getTextValue());
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
            throw new IllegalStateException("Role adv has no assigned advID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        if (getDesignEntityID().equals(ID.nullID))
            throw new IllegalStateException("DesignEntity history adv has no assigned ID");
        e = adv.createElement(designEntityIDTag, getDesignEntityID().toString());
        adv.appendChild(e);
        e = adv.createElement(baseNameTag, getBaseName());
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("Role adv has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        e = adv.createElement(roleNameTag, getRoleName());
        adv.appendChild(e);
        
        return adv;
    }
    
    /** Getter for property roleName.
     * @return Value of property roleName.
     *
     */
    public java.lang.String getRoleName() {
        return roleName;
    }
    
    /** Setter for property roleName.
     * @param roleName New value of property roleName.
     *
     */
    public void setRoleName(java.lang.String roleName) {
        this.roleName = roleName;
    }
    
}
