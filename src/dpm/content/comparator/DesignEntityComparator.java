/*
 * TaskComparator.java
 *
 * Created on September 12, 2003, 5:22 PM
 */

package dpm.content.comparator;

import java.util.Comparator;
import java.util.Date;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.content.*;
import dpm.content.designEntity.*;

/**
 *
 * @author  cumming
 */
public class DesignEntityComparator implements Comparator {
    
    /** Creates a new instance of TaskComparator */
    public DesignEntityComparator() {
    } 
    
    public int compare(Object o1, Object o2) {
        String tn1 = ((DesignEntity)o1).getFullName();
        String tn2 = ((DesignEntity)o2).getFullName();
        
        if(!tn1.equals(tn2)) {
            return tn1.compareTo(tn2);
        }
        String id1 = ((DesignEntity)o1).getDesignEntityID().toString();
        String id2 = ((DesignEntity)o2).getDesignEntityID().toString();
        /** if the entity's name are the same name, then check the entity's id */
        return id1.compareTo(id2);
    }
}
