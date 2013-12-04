/*
 * TaskAdvComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator.designEntity;

import java.util.Comparator;
import java.util.Date;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;

/**
 *
 * @author  cumming
 */
public class DesignEntityAdvComparator implements Comparator {
    
    /** Creates a new instance of DesignEntityAdvComparator */
    public DesignEntityAdvComparator() {
    }
    
    public int compare(Object o1, Object o2) {
        /** Sorting by task name is most useful for the user*/
        String tn1 = ((DesignEntityAdv)o1).getFullName();
        String tn2 = ((DesignEntityAdv)o2).getFullName();
        
        if (!(tn1.equals(tn2))) {
            /** the normal case when tasks have different names */
            return tn1.compareTo(tn2); // sort by task name ascending
        }
        /** If task names are the same, then taskID is guaranteed to be unique */
        String id1 = ((DesignEntityAdv)o1).getDesignEntityID().toString();
        String id2 = ((DesignEntityAdv)o2).getDesignEntityID().toString();
        /** if the tasks name are the same name, then check the task's id */
        return id1.compareTo(id1);
    }
}
