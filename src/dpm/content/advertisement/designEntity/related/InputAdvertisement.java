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



/** Once a design entity attains a certain state, this is advertised using a InputAdvertisement
 * @author cumming */
public class InputAdvertisement extends DesignEntityRelatedAdv {
    public static final String transNameTag = "TransitionName";
    public static final String roleNameTag = "RoleName";
    private String transName;
    private String roleName;
    
    private static final String[] fields =
    {advIDTag, designEntityIDTag, baseNameTag, iterationTag, descTag, dateCreateTag, authorIDTag, authorNameTag, transNameTag, roleNameTag};
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:InputAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new InputAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new InputAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    private InputAdvertisement() {
    }
    
    private InputAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:InputAdv";
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
                    throw new IllegalArgumentException("Unusable advID in role advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad advID in role advertisement");
                }
                continue;
            }
            if (elem.getName().equals(designEntityIDTag)) {
                try {
                    URL designEntityID = IDFactory.jxtaURL(elem.getTextValue());
                    setDesignEntityID(IDFactory.fromURL(designEntityID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable task ID in policy advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad task ID in policy advertisement");
                }
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
            if (elem.getName().equals(authorIDTag)) {
                try {
                    URL authorID = IDFactory.jxtaURL(elem.getTextValue());
                    setAuthorID((PeerID)IDFactory.fromURL(authorID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable author ID in task state advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in task state advertisement");
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
            if (elem.getName().equals(transNameTag)) {
                setTransName(elem.getTextValue());
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
            throw new IllegalStateException("DesignEntity role has no assigned advID");
        TextElement e = adv.createElement(RoleAdvertisement.advIDTag, getAdvID().toString());
        adv.appendChild(e);
        if (getDesignEntityID().equals(ID.nullID))
            throw new IllegalStateException("DesignEntity history adv has no assigned ID");
        e = adv.createElement(designEntityIDTag, getDesignEntityID().toString());
        adv.appendChild(e);
        e = adv.createElement(baseNameTag, getBaseName());
        adv.appendChild(e);
         e = adv.createElement(iterationTag, getIteration());
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("DesignEntity input has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        e = adv.createElement(transNameTag, getTransName());
        adv.appendChild(e);
        e = adv.createElement(roleNameTag, getRoleName());
        adv.appendChild(e);
        
        return adv;
    }
    
    /** Getter for property transName.
     * @return Value of property transName.
     *
     */
    public java.lang.String getTransName() {
        return transName;
    }
    
    /** Setter for property transName.
     * @param transName New value of property transName.
     *
     */
    public void setTransName(java.lang.String transName) {
        this.transName = transName;
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
