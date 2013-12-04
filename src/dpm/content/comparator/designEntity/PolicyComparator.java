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
import dpm.content.advertisement.designEntity.related.constraint.*;
import dpm.content.*;
import dpm.content.constraint.*;


/**
 *
 * @author  cumming
 */
public class PolicyComparator implements Comparator {
    
    /** Creates a new instance of DesignEntityRelatedAdvComparator */
    public PolicyComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        /** advID will always be unique */
        Policy pol1 = (Policy)o1;
        Policy pol2 = (Policy)o2;
        
        PolicyAdvertisement polAdv1 = (PolicyAdvertisement)pol1.getAdv();
        PolicyAdvertisement polAdv2 = (PolicyAdvertisement)pol2.getAdv();
        
        String id1 = polAdv1.getAdvID().toString();
        String id2 = polAdv2.getAdvID().toString();     
        
        return id1.compareTo(id2); // sort by task name ascending
        
    }
}
