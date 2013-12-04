/*
 * ContentStorageComparator.java
 *
 * Created on January 16, 2004
 */

package dpm.content.comparator;

import java.util.Comparator;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.peergroup.PeerGroup;
import dpm.content.ContentStorage;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;

/**
 *
 * @author  cumming
 * @since January 16, 2004
 */
public class ContentStorageComparator implements Comparator {
    
    /** Creates a new instance of ContentStorageComparator */
    public ContentStorageComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        if ((!(o1 instanceof ContentStorage)) || (!(o2 instanceof ContentStorage))) {
            return -1;
        }
        String n1 = ((ContentStorage)o1).getItemClassName();
        String n2 = ((ContentStorage)o2).getItemClassName();
        //
        if (!(n1.equals(n2))) {
            /** the normal case: when peergroups have different names */
            return n1.compareTo(n2);
        }
        return -1;
    }
}
