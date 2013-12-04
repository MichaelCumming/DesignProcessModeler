/*
 * UseChecker.java
 *
 * Created on October 7, 2004, 10:34 AM
 */

package dpm.content;

import dpm.dpmApp.desktop.*;
import dpm.peer.*;


/**
 *
 * @author  cumming
 */
public interface DeleteChecker {
   /**Some peer has deleted this entity */
   boolean isDeleted(Peer appUser);
   
}
