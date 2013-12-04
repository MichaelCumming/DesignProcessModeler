/*
 * IPGAdvertisement.java
 *
 * Created on January 3, 2004, 7:51 PM
 */

package dpm.content.advertisement;

/**
 *
 * @author  mc9p
 */

import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.id.IDFactory;
import net.jxta.document.*;
import java.util.Enumeration;
import java.net.URL;
import java.util.Hashtable;
import java.net.MalformedURLException;
import java.net.UnknownServiceException;
import net.jxta.id.ID;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.peer.*;


public class IPGAdvertisement extends Advertisement implements DeleteChecker {
    //public class PeerGroupAdv extends PeerGroupAdvertisement {
    private PeerGroupID gid = null;
    private ModuleSpecID specId = null;
    private String name = null;
    private String description = null;
    private Hashtable serviceParams = new Hashtable();
    //
    private static final String nameTag = "Name";
    private static final String gidTag = "GID";
    private static final String descTag = "Desc";
    private static final String msidTag = "MSID";
    private static final String svcTag = "Svc";
    private static final String mcidTag = "MCID";
    private static final String paramTag = "Parm";
    private static final String[] fields = {nameTag, gidTag, descTag};
    
    public boolean isDeleted(Peer appUser) {
        String storageKey = getPeerGroupID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /**
         * Returns the identifying type of this Advertisement.
         * @return String the type of advertisement
         * @since JXTA 1.0
         */
        public String getAdvertisementType() {
            return IPGAdvertisement.getAdvertisementType();
        }
        
        /**
         * Constructs an instance of <CODE>Advertisement</CODE> matching the type
         * specified by the <CODE>advertisementType</CODE> parameter.
         * @param advertisementType Specifies the mime media type to be associated with the
         * <CODE>StructuredDocument</CODE> to be created.
         * @return The instance of <CODE>Advertisement</CODE> or null if it could not be created.
         * @exception InvocationTargetException error invoking target constructor
         * @since JXTA 1.0
         */
        public Advertisement newInstance() {
            return new IPGAdvertisement();
        }
        
        /**
         * Constructs an instance of <CODE>Advertisement</CODE> matching the type
         * specified by the <CODE>advertisementType</CODE> parameter.
         * @param root Specifies a portion of a StructuredDocument which will be converted into an Advertisement.
         * @return The instance of <CODE>Advertisement</CODE> or null if it could not be created.
         * @exception InvocationTargetException error invoking target constructor
         * @since JXTA 1.0
         */
        public Advertisement newInstance(net.jxta.document.Element root) {
            return new IPGAdvertisement(root);
        }
    };
    
    
    public IPGAdvertisement() {
    }
    
    public IPGAdvertisement(Element root) {
        initialize(root);
    }
    
    
    public void initialize(Element root) {
        if (!TextElement.class.isInstance(root))
            throw new IllegalArgumentException(getClass().getName() + " only supports TextElement");
        TextElement doc = (TextElement)root;
        if (!doc.getName().equals(getAdvertisementType()))
            throw new IllegalArgumentException("Could not construct : " + getClass().getName() +
            "from doc containing a " + doc.getName());
        // set defaults
        setDescription(null);
        setName(null);
        setModuleSpecID(null);
        setPeerGroupID(null);
        Enumeration elements = doc.getChildren();
        while (elements.hasMoreElements()) {
            TextElement elem = (TextElement)elements.nextElement();
            if (elem.getName().equals(nameTag)) {
                setName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(descTag)) {
                setDescription(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(gidTag)) {
                try {
                    URL grID = IDFactory.jxtaURL(elem.getTextValue());
                    setPeerGroupID((PeerGroupID)IDFactory.fromURL(grID));
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad pipe ID in advertisement");
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable ID in advertisement");
                }
                continue;
            }
            if (elem.getName().equals(msidTag)) {
                try {
                    URL specID = IDFactory.jxtaURL(elem.getTextValue());
                    setModuleSpecID((ModuleSpecID)IDFactory.fromURL(specID));
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad pipe ID in advertisement");
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable ID in advertisement");
                }
                continue;
            }
            if (elem.getName().equals(svcTag)) {
                Enumeration elems = elem.getChildren();
                String classID = null;
                Element param = null;
                while (elems.hasMoreElements()) {
                    TextElement e = (TextElement)elems.nextElement();
                    if (e.getName().equals(mcidTag)) {
                        classID = e.getTextValue();
                        continue;
                    }
                    if (e.getName().equals(paramTag)) {
                        param = e;
                        continue;
                    }
                }
                if (classID != null && param != null) {
                    // Add this param to the table. putServiceParam()
                    // clones param into a standalone document automatically.
                    // (classID gets cloned too).
                    try {
                        putServiceParam(IDFactory.fromURL(IDFactory.jxtaURL(classID)), param);
                    } catch (MalformedURLException badID) {
                        throw new IllegalArgumentException("Bad pipe ID in advertisement");
                    } catch (UnknownServiceException badID) {
                        throw new IllegalArgumentException("Unusable ID in advertisement");
                    }
                }
                continue;
            }
        }
    }
    
    public Document getDocument(MimeMediaType encodeAs) {
        StructuredTextDocument adv = null;
        adv = (StructuredTextDocument)StructuredDocumentFactory.newStructuredDocument(encodeAs, getAdvertisementType());
        if (adv instanceof Attributable) {
            ((Attributable)adv).addAttribute("xmlns:jxta", "http://jxta.org");
        }
        Element e;
        e = adv.createElement(gidTag, getID().toString());
        adv.appendChild(e);
        e = adv.createElement(msidTag, getModuleSpecID().toString());
        adv.appendChild(e);
        e = adv.createElement(nameTag, getName());
        adv.appendChild(e);
        e = adv.createElement(descTag, getDescription());
        adv.appendChild(e);
//        String description = getDescription();
//        if (null != description) {
//            e = adv.createElement(descTag, description);
//            adv.appendChild(e);
//        }
        // FIXME: this is inefficient - we force our base class to make
        // a deep clone of the table.
        Hashtable serviceParams = getServiceParams();
        Enumeration classIds = serviceParams.keys();
        while (classIds.hasMoreElements()) {
            ModuleClassID classId = (ModuleClassID)classIds.nextElement();
            Element s = adv.createElement(svcTag);
            adv.appendChild(s);
            e = adv.createElement(mcidTag, classId.toString());
            s.appendChild(e);
            e = (Element)serviceParams.get(classId);
            StructuredDocumentUtils.copyElements(adv, s, e, paramTag);
        }
        return adv;
    }
    
    public String[] getIndexFields() {
        return fields;
    }
    
    public ID getID() {
        return gid;
    }
    
    public static String getAdvertisementType() {
        return "jxta:IPGAdv";
    }
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public java.lang.String getName() {
        return name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /** Getter for property description.
     * @return Value of property description.
     *
     */
    public java.lang.String getDescription() {
        return description;
    }
    
    /** Setter for property description.
     * @param description New value of property description.
     *
     */
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    /** Getter for property moduleSpecId.
     * @return Value of property moduleSpecId.
     *
     */
    public net.jxta.platform.ModuleSpecID getModuleSpecID() {
        return specId;
    }
    
    /** Setter for property moduleSpecId.
     * @param moduleSpecId New value of property moduleSpecId.
     *
     */
    public void setModuleSpecID(net.jxta.platform.ModuleSpecID moduleSpecId) {
        this.specId = moduleSpecId;
    }
    
    /** Getter for property peerGroupID.
     * @return Value of property peerGroupID.
     *
     */
    public net.jxta.peergroup.PeerGroupID getPeerGroupID() {
        return gid;
    }
    
    /** Setter for property peerGroupID.
     * @param peerGroupID New value of property peerGroupID.
     *
     */
    public void setPeerGroupID(net.jxta.peergroup.PeerGroupID peerGroupID) {
        this.gid = peerGroupID;
    }
    
    public void setServiceParams(Hashtable params) {
        if (params == null) {
            serviceParams = new Hashtable();
            return;
        }
        Hashtable copy = new Hashtable();
        Enumeration keys = params.keys();
        while (keys.hasMoreElements()) {
            ID key = (ID)keys.nextElement();
            Element e = (Element)params.get(key);
            Element newDoc = StructuredDocumentUtils.copyAsDocument(e);
            copy.put(key, newDoc);
        }
        serviceParams = copy;
    }
    
    public void putServiceParam(ID key, Element param) {
        if (param == null) {
            serviceParams.remove(key);
            return;
        }
        Element newDoc = StructuredDocumentUtils.copyAsDocument(param);
        serviceParams.put(key, newDoc);
    }
    
    public StructuredDocument removeServiceParam(ID key) {
        Element param = (Element)serviceParams.remove(key);
        if (param == null) return null;
        // It sound silly to clone it, but remember that we could be sharing
        // this element with a clone of ours, so we have the duty to still
        // protect it.
        StructuredDocument newDoc = StructuredDocumentUtils.copyAsDocument(param);
        return newDoc;
    }
    
    public Hashtable getServiceParams() {
        Hashtable copy = new Hashtable();
        Enumeration keys = serviceParams.keys();
        while (keys.hasMoreElements()) {
            ID key = (ID)keys.nextElement();
            Element e = (Element)serviceParams.get(key);
            Element newDoc = StructuredDocumentUtils.copyAsDocument(e);
            copy.put(key, newDoc);
        }
        return copy;
    }
    
}
