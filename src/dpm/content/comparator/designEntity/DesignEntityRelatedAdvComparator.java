/*
 * DesignEntityRelatedAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator.designEntity;

import java.util.Comparator;
import java.util.Date;
import net.jxta.document.Advertisement;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;


/**
 *
 * @author  cumming
 */
public class DesignEntityRelatedAdvComparator implements Comparator {
    
    /** Creates a new instance of DesignEntityRelatedAdvComparator */
    public DesignEntityRelatedAdvComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        /** advID will always be unique */
        String id1 = ((DesignEntityRelatedAdv)o1).getAdvID().toString();
        String id2 = ((DesignEntityRelatedAdv)o2).getAdvID().toString();     
        
        return id1.compareTo(id2); // sort by task name ascending      
    }
    
}
