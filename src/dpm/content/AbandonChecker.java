/*
 * UseChecker.java
 *
 * Created on October 7, 2004, 10:34 AM
 */

package dpm.content;

import dpm.peer.*;

/**
 *
 * @author  cumming
 */
public interface AbandonChecker {
   /**Some peer has abandoned this entity */
   boolean isAbandoned(Peer appUser);
   
}
