/*
 * PeerAdvComparator.java
 *
 * Created on August 11, 2004
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
public class DeleteAdvComparator implements Comparator {
    
    /** Creates a new instance of PeerAdvComparator */
    public DeleteAdvComparator() {
    }
    
    public int compare(Object o1, Object o2) {
       
        String id1 = ((DeleteAdvertisement)o1).getAdvID().toString();
        String id2 = ((DeleteAdvertisement)o2).getAdvID().toString();
        
        return id1.compareTo(id2); 
    }
}
