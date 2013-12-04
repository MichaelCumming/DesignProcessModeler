package dpm.content.advertisement.designEntity;

import dpm.content.advertisement.DesignEntityAdv;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.id.ID;


/**Superclass for designEntityRelatedAdvs */
public class DesignEntityRelatedAdv extends DesignEntityAdv {
    private static final String[] fields =
    {advIDTag, designEntityIDTag, baseNameTag, iterationTag, descTag, dateCreateTag, authorIDTag, authorNameTag};
  
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:DesignEntityRelatedAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new DesignEntityRelatedAdv();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new DesignEntityRelatedAdv(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected DesignEntityRelatedAdv() {
    }
    
    protected DesignEntityRelatedAdv(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:DesignEntityRelatedAdv";
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
    
}
