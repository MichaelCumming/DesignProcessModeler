/*
 * Tests.java
 *
 * Created on March 4, 2004, 12:45 PM
 */

package dpm.content;

import java.util.*;

/**
 *
 * @author  cumming
 */
public class Tests {
    
    /** Creates a new instance of Tests */
    public Tests() {
        runTest();
    }
    
    public void printSet(String desc, Set set) {
        System.out.println(desc + ": ");
        String result = "";
        for (Iterator i = set.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            result += s + " ";
        }
        System.out.println(result);
    }
    
    public void runTest() {
        HashSet a = new HashSet();
        a.add("a");
        a.add("b");
        a.add("c");
        
        HashSet b = new HashSet();
        b.add("b");
        b.add("c");
        b.add("d");
        
        printSet("All of a", a);
        printSet("All of b", b);
        //b.retainAll(a);
        //printSet("b.retainAll(a)", b);
        a.removeAll(b);
        printSet("a.removeAll(b)", a);
     
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Tests t = new Tests();
        
    }
    
}
