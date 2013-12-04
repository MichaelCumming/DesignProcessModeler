/*
 * PeerGroupAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import java.util.Comparator;
import net.jxta.protocol.PeerGroupAdvertisement;
import dpm.content.*;

/**
 *
 * @author  cumming
 */
public class PeerGroupAdvComparator implements Comparator {
    
    /** Creates a new instance of PeerGroupAdvComparator */
    public PeerGroupAdvComparator() {
    }
    
    //STORE_PG
    public int compareX(Object o1, Object o2) {
        if ((!(o1 instanceof PeerGroupAdvertisement)) || (!(o2 instanceof PeerGroupAdvertisement))) {
            return -1;
        }
        //PeerGroupAdvertisement a1 = (PeerGroupAdvertisement)o1;
        //PeerGroupAdvertisement a2 = (PeerGroupAdvertisement)o2;
        String n1 = ((PeerGroupAdvertisement)o1).getName();
        String n2 = ((PeerGroupAdvertisement)o2).getName();
        //
        if(n1 == null) {
            PeerGroupAdvertisement adv1 = (PeerGroupAdvertisement)o1;
            adv1.setName("NO_NAME_PEERGROUP");
            n1 = "NO_NAME_PEERGROUP";
        }
        
        if(n2 == null) {
            PeerGroupAdvertisement adv2 = (PeerGroupAdvertisement)o2;
            adv2.setName("NO_NAME_PEERGROUP");
            n2 = "NO_NAME_PEERGROUP";
        }
        if (!(n1.equals(n2))) {
            /** the normal case: when peers have different names */
            return n1.compareTo(n2);
        }
        String id1 = ((PeerGroupAdvertisement)o1).getPeerGroupID().toString();
        String id2 = ((PeerGroupAdvertisement)o2).getPeerGroupID().toString();
        
        return id1.compareTo(id2);
    }
    
    public int compare(Object o1, Object o2) {
        if ((!(o1 instanceof PeerGroupAdvertisement)) || (!(o2 instanceof PeerGroupAdvertisement))) {
            return -1;
        }
        String s1 = ((PeerGroupAdvertisement)o1).getPeerGroupID().toString();
        String s2 = ((PeerGroupAdvertisement)o2).getPeerGroupID().toString();
        return s1.compareTo(s2);
    }
}
