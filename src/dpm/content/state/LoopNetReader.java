/*
 * DesignEntityStateLoop.java
 *
 * Created on February 18, 2004, 10:55 AM
 */

package dpm.content.state;


import CH.ifa.draw.framework.Drawing;
import de.renew.simulator.Transition;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import de.renew.gui.CPNSimulation;
import de.renew.gui.Demonstrator;
import de.renew.simulator.Net;
import de.renew.simulator.NetInstance;
import de.renew.simulator.Place;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.*;
import dpm.peer.Peer;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import net.jxta.peergroup.PeerGroup;
import java.util.Vector;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.dpmApp.desktop.*;
import java.net.URL;



/**
 * Takes information from a petri net loop needed to make a loopNetAdv.
 * Use method: getLoopNetAdv() to retrieve the loopNetAdv
 * @author  cumming
 */
public class LoopNetReader {
    private DpmAppTopFrame topFrame;
    private PeerGroup parentPG;
    private Demonstrator petriNetApp;
    private Drawing loopDrawing;
    private String loopName;
    private boolean openDrawing;
    /** The petri net that describes the names of the states and transitions: e.g. performComplete etc. */
    private Net loopNet;
    private LoopNetAdvertisement loopNetAdv;
    private AdvUtilities advUtils;
    
    
    /** Creates a new instance of LoopNetReader.
     * Used to create a [single] loopNetAdv */
    public LoopNetReader(String loopName, DpmAppTopFrame topFrame, PeerGroup parentPG, boolean openDrawing) {
        this.loopName = loopName;
        this.topFrame = topFrame;
        this.advUtils = topFrame.getAdvUtils();
        this.petriNetApp = topFrame.getPetriNetApp();
        this.parentPG = parentPG;
        this.openDrawing = openDrawing;
        
        /**If the loopNetReader has to open the drawing */
        if(openDrawing) {
            /**Note: absolute path name */
            URL url = this.getClass().getResource("/loops/" + loopName + ".rnw");
            /**See in: CH.ifa.draw.application.DrawApplication */
            petriNetApp.loadAndOpenDrawing(url, loopName);
        }
        /** Current drawing */
        Drawing loopDrawing = getOpenDrawing(loopName);
        if(loopDrawing == null) {
            System.out.println("Error: loopDrawing is null in LoopNetReader");
            return;
        }
        else {
            this.loopDrawing = loopDrawing;
        }
        
        try {
            CPNSimulation sim = petriNetApp.getMode().getSimulation();
            NetInstance loopInstance = sim.initSimulation(loopName);
            Net loopNet = loopInstance.getNet();
            if(loopNet == null) {
                System.out.println("Error: loopNet is null in LoopNetReader");
                return;
            }
            else {
                this.loopNet = loopNet;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**used for testing @since 16 Nov. 2004 */
    public static void main(String args[]) {
        String toRemove = "file:/";
        String input = "file:/P:/cumming/research/java/jars/dpm/test02/dpmDemo.jar!/loops/TOI_CourseStateLoop.rnw";
        //String result = input.substring(6, input.length());
        String result = input.replaceFirst(toRemove, "");
        System.out.println("input string: " + input);
        System.out.println("result string: " + result);
    }
    
    public LoopNetAdvertisement getLoopNetAdv() {
        if(loopDrawing == null) {
            System.out.println("Loop drawing is null in LoopNetReader");
            return null;
        }
        return advUtils.createLoopNetAdvertisement(this, parentPG);
    }
    
    public Drawing getOpenDrawing(String drawingName) {
        for(Enumeration e = petriNetApp.drawings(); e.hasMoreElements(); ) {
            Drawing d = (Drawing)e.nextElement();
            if(d.getName().equals(drawingName)) {
                return d;
            }
        }
        return null;
    }
    
    /** An array of place (=state) names, ordered by their prefix
     * Note: loop must be loaded in Renew */
    public Collection getStates() {
        if(loopNet != null) {
            /** for storing names and their prefixes */
            HashMap map = new HashMap();
            Enumeration places = loopNet.places();
            while(places.hasMoreElements()) {
                Place p = (Place)places.nextElement();
                Integer prefix = advUtils.getPrefixNum(p.toString());
                String name = p.toString();
                //String name = stripPrefix(p.toString());
                map.put(prefix, name);
            }
            //System.out.println("Size of states map: " + String.valueOf(map.size()));
            return getOrderedCollection(map);
        }
        //System.out.println("ERROR: loopNet null in LoopNet");
        return null;
    }
    
    /** An array of transition names, ordered by their prefix */
    public Collection getTransitions() {
        if(loopNet != null) {
            /** for storing names and their prefixes */
            HashMap map = new HashMap();
            Enumeration transitions = loopNet.transitions();
            while(transitions.hasMoreElements()) {
                Transition t = (Transition)transitions.nextElement();
                Integer prefix = advUtils.getPrefixNum(t.toString());
                String name = getTransName(t);
                //String name = "test";
                map.put(prefix, name);
            }
            //System.out.println("Size of transitions map: " + String.valueOf(map.size()));
            return getOrderedCollection(map);
        }
        //System.out.println("ERROR: loopNet null in LoopNet");
        return null;
    }
    
    /** An array of transition names, ordered by their prefix */
    public Collection getComments() {
        if(loopNet != null) {
            /** for storing names and their prefixes */
            HashMap map = new HashMap();
            /** Comments are stored in the transition names after '=' */
            Enumeration transitions = loopNet.transitions();
            while(transitions.hasMoreElements()) {
                Transition t = (Transition)transitions.nextElement();
                Integer prefix = advUtils.getPrefixNum(t.toString());
                //String comment = "test";
                String comment = getTransComment(t);
                map.put(prefix, prefix + "." + comment);
            }
            //System.out.println("Size of comments map: " + String.valueOf(map.size()));
            return getOrderedCollection(map);
        }
        //System.out.println("ERROR: loopNet null in LoopNet");
        return null;
    }
    
    public Collection getOrderedCollection(HashMap map) {
        Vector v = new Vector();
        //System.out.println("Ordering hash map: ");
        for (int i = 0; i < map.size(); i++) {
            String value = (String)map.get(new Integer(i));
            //System.out.println(String.valueOf(i) + ": " + value);
            v.add(value);
        }
        return v;
    }
    
    /** assumes transitions are named e.g. 0.transName=transComment */
    public String getTransName(Transition t) {
        String s = t.toString();
        /** Name lies between '.' and '=' */
        //int begin = s.indexOf('.') + 1;
        /** keep the prefix */
        int begin = 0;
        int end = s.indexOf('=');
        return s.substring(begin, end);
    }
    
    /** assumes transitions are named e.g. 0.transName=transComment */
    public String getTransComment(Transition t) {
        String s = t.toString();
        return s.substring(s.indexOf('=') + 1);
    }
    
    
    
    /** Getter for property loopDrawing.
     * @return Value of property loopDrawing.
     *
     */
    public Drawing getLoopDrawing() {
        return loopDrawing;
    }
    
    /** Setter for property loopDrawing.
     * @param loopDrawing New value of property loopDrawing.
     *
     */
    public void setLoopDrawing(Drawing loopDrawing) {
        this.loopDrawing = loopDrawing;
    }
    
    /** Getter for property loopName.
     * @return Value of property loopName.
     *
     */
    public java.lang.String getLoopName() {
        return loopName;
    }
    
    /** Setter for property loopName.
     * @param loopName New value of property loopName.
     *
     */
    public void setLoopName(java.lang.String loopName) {
        this.loopName = loopName;
    }
    
    /** Getter for property loopNet.
     * @return Value of property loopNet.
     *
     */
    public Net getLoopNet() {
        return loopNet;
    }
    
    
    
    //    public static void main(String args[]) {
    //        new LoopNet();
    //    }
    
    /** only works with modification of constructor etc. */
    //    public void test() {
    //        String s = "0.firstContact=you are aware of this task";
    //        System.out.println(
    //        "Original: " + s +
    //        " Name: " + getName(s) +
    //        " Comment: " + getComment(s));
    //    }
    
    
}
