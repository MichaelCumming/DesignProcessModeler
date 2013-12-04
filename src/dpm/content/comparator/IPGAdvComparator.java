/*
 * IPGAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import java.util.Comparator;
import net.jxta.protocol.PeerGroupAdvertisement;
import dpm.content.advertisement.*;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;

/**
 *
 * @author  cumming
 */
public class IPGAdvComparator implements Comparator {
    
    /** Creates a new instance of IPGAdvComparator */
    public IPGAdvComparator() {
    }    
    
    public int compare(Object o1, Object o2) {
        if ((!(o1 instanceof IPGAdvertisement)) || (!(o2 instanceof IPGAdvertisement))) {
            return -1;
        }
        String s1 = ((IPGAdvertisement)o1).getPeerGroupID().toString();
        String s2 = ((IPGAdvertisement)o2).getPeerGroupID().toString();
        return s1.compareTo(s2);
    }
    
    //STORE_PG
    //    public int compareX(Object o1, Object o2) {
    //        if ((!(o1 instanceof IPGAdvertisement)) || (!(o2 instanceof IPGAdvertisement))) {
    //            return -1;
    //        }
    //        //PeerGroupAdvertisement a1 = (PeerGroupAdvertisement)o1;
    //        //PeerGroupAdvertisement a2 = (PeerGroupAdvertisement)o2;
    //        String s1 = ((IPGAdvertisement)o1).getName();
    //        String s2 = ((IPGAdvertisement)o2).getName();
    //        //
    //        if(s1 == null) {
    //            IPGAdvertisement adv1 = (IPGAdvertisement)o1;
    //            adv1.setName("NO_NAME_IPG");
    //            s1 = "NO_NAME_IPG";
    //        }
    //
    //        if(s2 == null) {
    //            IPGAdvertisement adv2 = (IPGAdvertisement)o2;
    //            adv2.setName("NO_NAME_IPG");
    //            s2 = "NO_NAME_IPG";
    //        }
    //        return s1.compareTo(s2); // sort by description, ascending
    //    }
}
