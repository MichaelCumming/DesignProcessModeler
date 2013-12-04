/*
 * TaskAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import java.util.Comparator;
import java.util.Date;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.chat.*;
import dpm.content.*;


/**
 *
 * @author  cumming
 */
public class ChatAdvComparator implements Comparator {
    private int SAME = 0;
    
    /** Creates a new instance of AdvComparator */
    public ChatAdvComparator() {
    }
    
    public int compare(Object o1, Object o2) {      
        String id1 = ((ChatAdv)o1).getAdvID().toString();
        String id2 = ((ChatAdv)o2).getAdvID().toString();
        
        if(id1.equals(id2)) {
            return SAME;
        }
        /**Else, order by date; different IDs */
        Date d1 = ((ChatAdv)o1).getDateCreate();
        Date d2 = ((ChatAdv)o2).getDateCreate();
        
        /**Reverse chronological order (latest at top) */
        return d2.compareTo(d1);
    }
}
