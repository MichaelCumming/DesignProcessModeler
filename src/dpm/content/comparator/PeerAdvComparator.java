/*
 * PeerAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import java.util.Comparator;
import net.jxta.protocol.PeerAdvertisement;

/**
 *
 * @author  cumming
 */
public class PeerAdvComparator implements Comparator {
    
    /** Creates a new instance of PeerAdvComparator */
    public PeerAdvComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        //null of objects checked in addPeerAdv...
        if ((!(o1 instanceof PeerAdvertisement)) || (!(o2 instanceof PeerAdvertisement))) {
            return -1;   
        }
//        String n1 = ((PeerAdvertisement)o1).getName();
//        String n2 = ((PeerAdvertisement)o2).getName();
//        
//        if(n1 == null) {
//            PeerAdvertisement adv1 = (PeerAdvertisement)o1;
//            adv1.setName("NO_NAME_PEER");
//            n1 = "NO_NAME_PEER";
//        }
//        
//        if(n2 == null) {
//            PeerAdvertisement adv2 = (PeerAdvertisement)o2;
//            adv2.setName("NO_NAME_PEER");
//            n2 = "NO_NAME_PEER";
//        }
//       if (!(n1.equals(n2))) {
//            /** the normal case: when peers have different names */
//            return n1.compareTo(n2);
//        }
        String id1 = ((PeerAdvertisement)o1).getPeerID().toString();
        String id2 = ((PeerAdvertisement)o2).getPeerID().toString();
        
        return id1.compareTo(id2);
    }
}
