package dpm.content.advertisement.designEntity.related;

import dpm.content.ContentStorage;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
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


/**
 * @author cumming */
public class HistoryAdvertisement extends DesignEntityRelatedAdv {
    public static final String transNameTag = "TransName";
    public static final String stateTag = "State";
    public static final String roleNameTag = "RoleName";
    public static final String peerNameTag = "PeerName";
    public static final String rolesTag = "Roles"; //header tag
    public static final String roleTag = "Role"; //header tag
    public static final String peersTag = "Peers"; //header tag
    //
    private String transName;
    private String state;
    private String roleName;
    private String peerName;
    /** ContentStorage keyed by roleNames, with peerNames as values */
    private ContentStorage peerRoles = new ContentStorage("java.lang.String", null);
    
    private static final String[] fields =
    {advIDTag, designEntityIDTag, baseNameTag, iterationTag, entityTypeTag, descTag, dateCreateTag, authorIDTag, authorNameTag, transNameTag, stateTag, rolesTag, roleTag, roleNameTag, peersTag, peerNameTag};
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:HistoryAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new HistoryAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new HistoryAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    private HistoryAdvertisement() {
    }
    
    private HistoryAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:HistoryAdv";
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
            if (elem.getName().equals(designEntityIDTag)) {
                try {
                    URL designEntityID = IDFactory.jxtaURL(elem.getTextValue());
                    setDesignEntityID(IDFactory.fromURL(designEntityID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable ID in history advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad ID in history advertisement");
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
            if (elem.getName().equals(entityTypeTag)) {
                setEntityType(elem.getTextValue());
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
            if (elem.getName().equals(transNameTag)) {
                setTransName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(stateTag)) {
                setState(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(rolesTag)) {
                for(Enumeration rolesEnum = elem.getChildren(); rolesEnum.hasMoreElements(); ) {
                    TextElement roleData = (TextElement)rolesEnum.nextElement();
                    for(Enumeration roleEnum = roleData.getChildren(); roleEnum.hasMoreElements(); ) {
                        TextElement roleElem = (TextElement)roleEnum.nextElement();
                        if (roleElem.getName().equals(roleNameTag)) {
                            String roleName = roleElem.getTextValue();
                            
                            roleElem = (TextElement)roleEnum.nextElement();
                            if (roleElem.getName().equals(peersTag)) {
                                for(Enumeration peersEnum = roleElem.getChildren(); peersEnum.hasMoreElements(); ) {
                                    TextElement peerElem = (TextElement)peersEnum.nextElement();
                                    if (peerElem.getName().equals(peerNameTag)) {
                                        String peerName = peerElem.getTextValue();
                                        /**add to the content storage; Storagekey = roleName */
                                        peerRoles.addString(roleName, peerName);
                                    }
                                }
                            }
                        }
                    }
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
            throw new IllegalStateException("DesignEntity history adv has no assigned advID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        if (getDesignEntityID().equals(ID.nullID))
            throw new IllegalStateException("DesignEntity history adv has no assigned ID");
        e = adv.createElement(designEntityIDTag, getDesignEntityID().toString());
        adv.appendChild(e);
        e = adv.createElement(baseNameTag, getBaseName());
        adv.appendChild(e);
        e = adv.createElement(iterationTag, getIteration());
        adv.appendChild(e);
        e = adv.createElement(entityTypeTag, getEntityType());
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("DesignEntity history adv has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        e = adv.createElement(transNameTag, getTransName());
        adv.appendChild(e);
        e = adv.createElement(stateTag, getState());
        adv.appendChild(e);
        /** Print out peer roles */
        TextElement allRoles = adv.createElement(rolesTag);
        adv.appendChild(allRoles);
        for (Iterator i = peerRoles.getKeySet().iterator(); i.hasNext(); ) {
            /** For each role name: */
            String roleName = (String)i.next();
            TextElement roleData = adv.createElement(roleTag);
            allRoles.appendChild(roleData);
            
            if (peerRoles.iterator(roleName) != null) {
                TextElement oneRole = adv.createElement(roleNameTag, roleName);
                roleData.appendChild(oneRole);
                TextElement peers = adv.createElement(peersTag);
                roleData.appendChild(peers);
                /** For all entries in the required inputs content storage */
                for (Iterator j = peerRoles.iterator(roleName); j.hasNext(); ) {
                    String peerName = (String)j.next();
                    TextElement onePeer = adv.createElement(peerNameTag, peerName);
                    peers.appendChild(onePeer);
                }
            }
        }
        
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
    
    /** Getter for property peerName.
     * @return Value of property peerName.
     *
     */
    public java.lang.String getPeerName() {
        return peerName;
    }
    
    /** Setter for property peerName.
     * @param peerName New value of property peerName.
     *
     */
    public void setPeerName(java.lang.String peerName) {
        this.peerName = peerName;
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
    
    /** Getter for property peerRoles.
     * @return Value of property peerRoles.
     *
     */
    public ContentStorage getPeerRoles() {
        return peerRoles;
    }
    
    /** Setter for property peerRoles.
     * @param peerRoles New value of property peerRoles.
     *
     */
    public void setPeerRoles(ContentStorage peerRoles) {
        this.peerRoles = peerRoles;
    }
    
    /** Getter for property state.
     * @return Value of property state.
     *
     */
    public java.lang.String getState() {
        return state;
    }
    
    /** Setter for property state.
     * @param state New value of property state.
     *
     */
    public void setState(java.lang.String state) {
        this.state = state;
    }
    
}
