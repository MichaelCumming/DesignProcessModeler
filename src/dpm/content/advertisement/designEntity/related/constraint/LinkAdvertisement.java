package dpm.content.advertisement.designEntity.related.constraint;

import dpm.content.DeleteChecker;
import dpm.content.advertisement.designEntity.related.ConstraintAdv;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import net.jxta.document.Advertisement;
import net.jxta.document.Document;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.peergroup.*;
import net.jxta.peer.*;
import java.util.*;
import java.net.*;
import java.text.*;
import net.jxta.id.*;



/**Each link describes one edge in a petri-net graph
 * @author cumming */
public class LinkAdvertisement extends ConstraintAdv implements DeleteChecker, DpmTerms {
    /**currentState() (determined dynamically)
     * must be later or equal to sourceState, otherwise this link is a constraint */
    public static final String sourceStateTag = "SourceState";
    public static final String sourceIDTag = "SourceID";
    public static final String sourceBaseNameTag = "SourceBaseName";
    public static final String sourceIterationTag = "SourceIteration";
    public static final String sourceTypeTag = "SourceType";
    
    private ID sourceID = ID.nullID;
    private String sourceBaseName = null;
    private String sourceIteration = null;
    private String sourceType = null;
    private String sourceState = null;
    
    private static final String[] fields =
    {advIDTag, constraintNameTag, targetTransTag, sourceIDTag, sourceBaseNameTag, sourceIterationTag, sourceTypeTag, targetIDTag, targetBaseNameTag, targetIterationTag, targetTypeTag, dateCreateTag, authorIDTag, authorNameTag};
    
    
    public boolean isDeleted(Peer appUser) {
        String linkKey = getID().toString();
        String sourceEntityKey = getTargetID().toString();
        String targetEntityKey = getSourceID().toString();
        
        return (appUser.getDeleteAdvs().storageKeyExists(linkKey) ||
        appUser.getDeleteAdvs().storageKeyExists(sourceEntityKey) ||
        appUser.getDeleteAdvs().storageKeyExists(targetEntityKey));
    }
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:LinkAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new LinkAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new LinkAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    private LinkAdvertisement() {
    }
    
    private LinkAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:LinkAdv";
    }
    
    public synchronized ID getID() {
        return advID;
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
                    throw new IllegalArgumentException("Unusable advID in link advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad advID in link advertisement");
                }
                continue;
            }
            if (elem.getName().equals(constraintNameTag)) {
                setConstraintName(elem.getTextValue());
                continue;
            }
            /** */
            if (elem.getName().equals(sourceStateTag)) {
                setSourceState(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(targetTransTag)) {
                setTransName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(sourceIDTag)) {
                try {
                    URL sourceID = IDFactory.jxtaURL(elem.getTextValue());
                    setSourceID(IDFactory.fromURL(sourceID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable sourceID in link advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad sourceID in link advertisement");
                }
                continue;
            }
            if (elem.getName().equals(sourceBaseNameTag)) {
                setSourceBaseName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(sourceIterationTag)) {
                setSourceIteration(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(sourceTypeTag)) {
                setSourceType(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(targetIDTag)) {
                try {
                    URL designEntityID = IDFactory.jxtaURL(elem.getTextValue());
                    setTargetID(IDFactory.fromURL(designEntityID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable designEntityID in link advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad designEntityID in link advertisement");
                }
                continue;
            }
            if (elem.getName().equals(targetBaseNameTag)) {
                setBaseName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(targetIterationTag)) {
                setIteration(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(targetTypeTag)) {
                setEntityType(elem.getTextValue());
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
                    throw new IllegalArgumentException("Unusable author ID in link advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in link advertisement");
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
        /** source information */
        if (getAdvID().equals(ID.nullID))
            throw new IllegalStateException("Link adv has no assigned advID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        e = adv.createElement(constraintNameTag, getConstraintName());
        adv.appendChild(e);
        e = adv.createElement(sourceStateTag, getSourceState());
        adv.appendChild(e);
        e = adv.createElement(targetTransTag, getTransName());
        adv.appendChild(e);
        
        /**Source information */
        if (getSourceID().equals(ID.nullID))
            throw new IllegalStateException("link adv has no assigned sourceID");
        e = adv.createElement(sourceIDTag, getSourceID().toString());
        adv.appendChild(e);
        e = adv.createElement(sourceBaseNameTag, getSourceBaseName());
        adv.appendChild(e);
        e = adv.createElement(sourceIterationTag, getSourceIteration());
        adv.appendChild(e);
        e = adv.createElement(sourceTypeTag, getSourceType());
        adv.appendChild(e);
        
        /**Target information */
        if (getTargetID().equals(ID.nullID))
            throw new IllegalStateException("link adv has no assigned designEntityID");
        e = adv.createElement(targetIDTag, getTargetID().toString());
        adv.appendChild(e);
        e = adv.createElement(targetBaseNameTag, getBaseName());
        adv.appendChild(e);
        e = adv.createElement(targetIterationTag, getIteration());
        adv.appendChild(e);
        e = adv.createElement(targetTypeTag, getEntityType());
        adv.appendChild(e);
        
        /** other information */
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("link adv has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        
        
        return adv;
    }
    
    
    /** Getter for property sourceID.
     * @return Value of property sourceID.
     *
     */
    public ID getSourceID() {
        return sourceID;
    }
    
    /** Setter for property sourceID.
     * @param sourceID New value of property sourceID.
     *
     */
    public void setSourceID(ID sourceID) {
        this.sourceID = sourceID;
    }
    
    /** Getter for property designEntityID.
     * @return Value of property designEntityID.
     *
     */
    public ID getTargetID() {
        return designEntityID;
    }
    
    /** Setter for property designEntityID.
     * @param designEntityID New value of property designEntityID.
     *
     */
    public void setTargetID(ID designEntityID) {
        this.designEntityID = designEntityID;
    }
    
    /** Getter for property sourceType.
     * @return Value of property sourceType.
     *
     */
    public java.lang.String getSourceType() {
        return sourceType;
    }
    
    /** Setter for property sourceType.
     * @param sourceType New value of property sourceType.
     *
     */
    public void setSourceType(java.lang.String sourceType) {
        this.sourceType = sourceType;
    }
    
    public String getNameAndTypeString(String name, String type) {
        return name + ": " + squareBracket(type);
    }
    
    public String getDescriptionBasic() {
        return
        combineNames(sourceBaseName, sourceIteration) + SPACE +
        constraintName + SPACE +
        combineNames(baseName, iteration);
    }
    
    public String getDescriptionTypes() {
        return
        getNameAndTypeString(combineNames(sourceBaseName, sourceIteration), getSourceType()) + SPACE +
        constraintName + SPACE +
        getNameAndTypeString(combineNames(baseName, iteration), getEntityType());
    }
    
    public String getDescriptionStateTrans() {
        return
        combineNames(sourceBaseName, sourceIteration) + SPACE +
        roundBracket(sourceState) + SPACE +
        constraintName + SPACE +
        combineNames(baseName, iteration) + SPACE +
        roundBracket(transName);
    }
    
    public String getDescriptionTypeAndStateTrans() {
        return
        getNameAndTypeString(combineNames(sourceBaseName, sourceIteration), getSourceType()) + SPACE +
        roundBracket(sourceState) + SPACE +
        constraintName + SPACE +
        getNameAndTypeString(combineNames(baseName, iteration), getEntityType()) + SPACE + 
        roundBracket(transName);
    }
    
    public String squareBracket(String s) {
        return "[" + s + "]";
    }
    
    public String roundBracket(String s) {
        return "(" + s + ")";
    }
    
    public String getStringDescriptionDoBeforeLong() {
        /**Suitable for doBefores only. All others the transNames/sourceStates are not relevant */
        return
        ("Source " + combineNames(sourceBaseName, sourceIteration) + " must be in state: " + NEWLINE +
        sourceState + NEWLINE +
        "To Enable Target " + combineNames(baseName, iteration) + "'s Transition: " + NEWLINE +
        transName);
    }
    
    
    
    public boolean isDoBefore() {
        return getConstraintName().equals(DO_BEFORE);
    }
    
    /** Getter for property sourceState.
     * @return Value of property sourceState.
     *
     */
    public java.lang.String getSourceState() {
        return sourceState;
    }
    
    /** Setter for property sourceState.
     * @param sourceState New value of property sourceState.
     *
     */
    public void setSourceState(java.lang.String sourceState) {
        this.sourceState = sourceState;
    }
    
    /** Getter for property sourceBaseName.
     * @return Value of property sourceBaseName.
     *
     */
    public java.lang.String getSourceBaseName() {
        return sourceBaseName;
    }
    
    /** Setter for property sourceBaseName.
     * @param sourceBaseName New value of property sourceBaseName.
     *
     */
    public void setSourceBaseName(java.lang.String sourceBaseName) {
        this.sourceBaseName = sourceBaseName;
    }
    
    /** Getter for property sourceIteration.
     * @return Value of property sourceIteration.
     *
     */
    public java.lang.String getSourceIteration() {
        return sourceIteration;
    }
    
    /** Setter for property sourceIteration.
     * @param sourceIteration New value of property sourceIteration.
     *
     */
    public void setSourceIteration(java.lang.String sourceIteration) {
        this.sourceIteration = sourceIteration;
    }
    
}
