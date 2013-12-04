/*
 * PeerGroupComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import java.util.Comparator;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.peergroup.PeerGroup;

/**
 *
 * @author  cumming
 */
public class PeerGroupComparator implements Comparator {
    
    /** Creates a new instance of PeerGroupComparator */
    public PeerGroupComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        if ((!(o1 instanceof PeerGroup)) || (!(o2 instanceof PeerGroup))) {
            return -1;
        }
        String s1 = ((PeerGroup)o1).getPeerGroupID().toString();
        String s2 = ((PeerGroup)o2).getPeerGroupID().toString();
        
        return s1.compareTo(s2);
    }
    
     //STORE_PG
//    public int compareX(Object o1, Object o2) {
//        if ((!(o1 instanceof PeerGroup)) || (!(o2 instanceof PeerGroup))) {
//            return -1;
//        }
//        String n1 = ((PeerGroup)o1).getPeerGroupNameX();
//        String n2 = ((PeerGroup)o2).getPeerGroupNameX();
//        //
//        if (!(n1.equals(n2))) {
//            /** the normal case: when peergroups have different names */
//            return n1.compareTo(n2);
//        }
//        String id1 = ((PeerGroup)o1).getPeerGroupID().toString();
//        String id2 = ((PeerGroup)o2).getPeerGroupID().toString();
//        
//        return id1.compareTo(id2);
//    }
}
