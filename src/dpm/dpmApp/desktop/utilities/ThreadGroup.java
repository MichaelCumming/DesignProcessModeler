/*
 * Threads.java
 *
 * Created on December 11, 2003, 8:08 AM
 */

package dpm.dpmApp.desktop.utilities;

import java.util.Vector;
import java.util.Iterator;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.*;


/**
 *
 * @author  cumming
 */
public class ThreadGroup implements DpmTerms {
    protected String name;
    protected Vector threads;
    protected DpmAppTopFrame topFrame;
    
    /** Creates a new instance of Threads */
    public ThreadGroup(String name, DpmAppTopFrame topFrame) {
        this.name = name;
        this.topFrame = topFrame;
        threads = new Vector();
    }
    
    public void addThread(Thread t) {
        threads.add(t);
    }
    
    public void startThreads() {
        for (Iterator i = threads.iterator(); i.hasNext(); ) {
            Thread t = (Thread)i.next();
            t.start();
        }
    }
    
    public void sleepThreads() {
        for (Iterator i = threads.iterator(); i.hasNext(); ) {
            Thread t = (Thread)i.next();
            try {
                t.sleep(WAIT_TIME);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public void yieldThreads() {
        for (Iterator i = threads.iterator(); i.hasNext(); ) {
            Thread t = (Thread)i.next();
            t.yield();
        }
    }
    
}
