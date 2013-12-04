/*
 * StringComparator.java
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
public class StringComparator implements Comparator {
    
    /** Creates a new instance of StringComparator */
    public StringComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        String s1 = (String)o1;
        String s2 = (String)o2;
        
        return s1.compareTo(s2);
    }
}
