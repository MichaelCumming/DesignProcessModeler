/*
 * PeerAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import java.util.Comparator;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;

/**
 *
 * @author  cumming
 */
public class NetAdvComparator implements Comparator {
    
    /** Creates a new instance of PeerAdvComparator */
    public NetAdvComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        /**@since 24 Nov 2004: to avoid net name duplication */
        String n1 = ((NetAdvertisement)o1).getNetName();
        String n2 = ((NetAdvertisement)o2).getNetName();
        
        //        String id1 = ((NetAdvertisement)o1).getAdvID().toString();
        //        String id2 = ((NetAdvertisement)o2).getAdvID().toString();
        
        return n1.compareTo(n2);
    }
}
