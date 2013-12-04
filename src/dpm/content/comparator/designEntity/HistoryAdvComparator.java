/*
 * TaskAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator.designEntity;

import java.util.Comparator;
import java.util.Date;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;

/**
 *
 * @author  cumming
 */
public class HistoryAdvComparator implements Comparator {
    
    /** Creates a new instance of AdvComparator */
    public HistoryAdvComparator() {
    }
    
    /**also found in AdvUtils */
    public Integer getPrefixNum(String nameWithPrefix) {
        int idx = nameWithPrefix.indexOf('.');
        return new Integer(nameWithPrefix.substring(0, idx));
    }
    
    public int compare(Object o1, Object o2) {
        HistoryAdvertisement histAdv1 = (HistoryAdvertisement)o1;
        HistoryAdvertisement histAdv2 = (HistoryAdvertisement)o2;
        
        /** Sorting by task date */
        Date d1 = histAdv1.getDateCreate();
        Date d2 = histAdv2.getDateCreate();
        
        /** if dates are different, order by date*/
        if(!d1.equals(d2)) {
            return d1.compareTo(d2);
        } 
        /**get prefix num from state*/             
        Integer i1 = getPrefixNum(histAdv1.getState());
        //System.out.println("Int 1: " + i1);
        Integer i2 = getPrefixNum(histAdv2.getState());
        //System.out.println("Int 2: " + i2);
        
        /** If state prefixes are different, order by prefix*/
        if(!i1.equals(i2)) {
           return String.valueOf(i1).compareTo(String.valueOf(i2)); 
        }
        /** Else, order by advID (which are always different) */
        String id1 = histAdv1.getAdvID().toString();
        String id2 = histAdv2.getAdvID().toString();      
        return id1.compareTo(id2);       
    }
}
