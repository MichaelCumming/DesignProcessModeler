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
import dpm.content.advertisement.designEntity.related.constraint.*;
import dpm.content.constraint.*;

/**
 *
 * @author  cumming
 */
public class LinkComparator implements Comparator {
    
    /** Creates a new instance of AdvComparator */
    public LinkComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        /**@return e.g. T1 > doBefore > P1 */
        String s1 = ((Link)o1).getAdv().getAdvID().toString();
        String s2 = ((Link)o2).getAdv().getAdvID().toString();
              
        /**if they describe same link, collapse them into one*/
        return s1.compareTo(s2);
    }
    
}
