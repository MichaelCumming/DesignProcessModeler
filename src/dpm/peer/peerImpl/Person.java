/*
 * Person.java
 *
 * Created on September 10, 2003, 2:33 PM
 */

package dpm.peer.peerImpl;

import dpm.content.comparator.PersonComparator;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.peer.Peer;
import java.util.Comparator;
import java.util.Vector;
import net.jxta.peergroup.PeerGroup;

/**
 * Current sole implementation of Peer.
 * @author  cumming
 * @since September 10, 2003, 2:33 PM
 */
public class Person extends Peer {
    //private String email = null;
    //private Vector tasks, designRoles = null; //vectors containing Tasks, DesignRoles
   // private Comparator comparator = null;
    
    /** Creates a new instance of Person */
    public Person(PeerGroup rootPG, DpmAppTopFrame topFrame) {
        super(rootPG, topFrame); //construct an instance of the superclass
        super.setComparator(new PersonComparator());
    }
   
    /** Getter for property tasks.
     * @return Value of property tasks.
     *
     */
//    public dpm.content.ContentStorage getTasks() {
//        return tasks;
//    }
    
    /** Setter for property tasks.
     * @param tasks New value of property tasks.
     *
     */
//    public void setTasks(Vector tasks) {
//        this.tasks = tasks;
//    }
    
    /** Getter for property designRoles.
     * @return Value of property designRoles.
     *
     */
//    public Vector getDesignRoles() {
//        return designRoles;
//    }
    
    /** Setter for property designRoles.
     * @param designRoles New value of property designRoles.
     *
     */
//    public void setDesignRoles(Vector designRoles) {
//        this.designRoles = designRoles;
//    }
    
    /** Getter for property topFrame.
     * @return Value of property topFrame.
     *
     */
//    public DpmAppTopFrame getTopFrame() {
//        return topFrame;
//    }
    
    /** Setter for property topFrame.
     * @param topFrame New value of property topFrame.
     *
     */
//    public void setTopFrame(DpmAppTopFrame topFrame) {
//        this.topFrame = topFrame;
//    }
    
}
