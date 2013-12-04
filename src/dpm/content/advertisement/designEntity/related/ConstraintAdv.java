package dpm.content.advertisement.designEntity.related;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
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


/**ConstraintAdvs apply to one particular transition--'transName' of a DesignEntity
 * Superclass is designEntityRelatedAdvs */
public class ConstraintAdv extends DesignEntityRelatedAdv {
    public static final String constraintNameTag = "ConstraintName";
    public static final String targetIDTag = "TargetID";
    /**Name of the targetEntity */
    public static final String targetBaseNameTag = "TargetBaseName";
    public static final String targetIterationTag = "TargetIteration";
    /**Type of the targetEntity */
    public static final String targetTypeTag = "TargetType";
    /**Target transition in the target entity */
    public static final String targetTransTag = "TargetTransition";
    
    protected String constraintName;
    protected String transName; //aka: targetTrans
    //protected ID targetID = designEntityID;
    //protected String targetName = name;
    //protected String targetType = entityType;
    
    /**NOTE: designEntity is the targetEntity for the constraint */
    private static final String[] fields =
    {advIDTag, designEntityIDTag, baseNameTag, iterationTag, descTag, dateCreateTag, authorIDTag, authorNameTag};
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:ConstraintAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new ConstraintAdv();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new ConstraintAdv(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected ConstraintAdv() {
    }
    
    protected ConstraintAdv(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:ConstraintAdv";
    }
    
    /** Builds a Java object from a structured text document (the adv) */
    private void initialize(Element root) {
    }
    
    /** Builds a structured text document from a Java object */
    public Document getDocument(MimeMediaType mediaType) {
        StructuredTextDocument adv = (StructuredTextDocument)
        StructuredDocumentFactory.newStructuredDocument(mediaType, getAdvertisementType());
        return adv;
    }
    
    /** {@inheritDoc} */
    public String[] getIndexFields() {
        return fields;
    }
    
    public synchronized ID getID() {
        return advID;
    }
    
    /** Getter for property constraintName.
     * @return Value of property constraintName.
     *
     */
    public java.lang.String getConstraintName() {
        return constraintName;
    }
    
    /** Setter for property constraintName.
     * @param constraintName New value of property constraintName.
     *
     */
    public void setConstraintName(java.lang.String constraintName) {
        this.constraintName = constraintName;
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
    
}
