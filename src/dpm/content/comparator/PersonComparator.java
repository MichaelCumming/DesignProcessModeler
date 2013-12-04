/*
 * PersonComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import dpm.peer.peerImpl.Person;
import java.util.Comparator;

/**
 *
 * @author  cumming
 */
public class PersonComparator implements Comparator {
    
    /** Creates a new instance of PersonComparator */
    public PersonComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        String n1 = ((Person)o1).getName();
        String n2 = ((Person)o2).getName();
        
        if (!(n1.equals(n2))) {
            /** the normal case: when people have different names */
            return n1.compareTo(n2);
        }
        String id1 = ((Person)o1).getPeerID().toString();
        String id2 = ((Person)o2).getPeerID().toString();
        
        return id1.compareTo(id2);
    }   
}
