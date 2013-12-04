/*
 * PeerAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import dpm.content.advertisement.*;
import java.util.Comparator;

/**
 *
 * @author  cumming
 */
public class MemberAdvComparator implements Comparator {
    
    /** Creates a new instance of MemberAdvComparator */
    public MemberAdvComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        if ((!(o1 instanceof IPGMemberAdvertisement)) || (!(o2 instanceof IPGMemberAdvertisement))) {
            return -1;   
        }
        String id1 = ((IPGMemberAdvertisement)o1).getPeerID().toString();
        String id2 = ((IPGMemberAdvertisement)o2).getPeerID().toString();
        
        return id1.compareTo(id2);
    }
}
