/*
 * HistoryBuilder.java
 *
 * Created on February 18, 2004, 2:50 PM
 */

package dpm.content.history;

import dpm.content.ContentStorage;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import dpm.content.EntityRelatedContentStorage;
import de.renew.gui.*;
import de.renew.simulator.NetInstance;
import de.renew.simulator.Net;
import de.renew.shadow.ShadowNet;
import de.renew.gui.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Enumeration;
import de.renew.simulator.Transition;
import de.renew.simulator.Place;
import dpm.content.state.*;
import java.util.*;
import de.renew.simulator.Net;
import dpm.content.state.*;
import dpm.content.*;
import dpm.content.advertisement.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.history.*;
import de.renew.simulator.NetInstance;
import de.renew.simulator.Place;
import de.renew.simulator.Transition;
import de.renew.formalism.java.ArcFactory;
import de.renew.formalism.java.SimpleArcFactory;
import de.renew.simulator.Arc;
import de.renew.simulator.ConstantTokenSource;
import java.lang.String;
import de.renew.formalism.java.TimedExpression;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;


/**
 *
 * @author  cumming
 */
public class RenewGraphBuilder extends Net {
    /** The place that contains the HistoryData */
    private Place inputPlace = new Place(this, "inputPlace");
    private Transition transition = new Transition(this, "transition");
    /** The place that connects with the next HistoryNet */
    private Place outputPlace = new Place(this, "outputPlace");
    public SimpleArcFactory outArcF = new SimpleArcFactory(Arc.out, false); //false = allowsTime
    public SimpleArcFactory inArcF = new SimpleArcFactory(Arc.in, false); 
    
    /** Creates a new instance of HistoryBuilder */
    public RenewGraphBuilder() {
        super("HistoryNet");
        try {
            /** Signature of compileArc: (place, trans, trace, placeType, timedExpr) */
            /** Draw an arc out from inputPlace to the transition */
            outArcF.compileArc(inputPlace, transition, false, String.class, new TimedExpression(null,null));
            /** Draw an arc into the output place from the transition */
            inArcF.compileArc(outputPlace, transition, false, String.class, new TimedExpression(null,null));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** Add HistoryData to the input place */
//    public void addHistoryData(HistoryData history) {
//        inputPlace.add(new ConstantTokenSource(history));
//    }
    
    /** appends a new history net onto an existing net */
//    public void addNewChapter(HistoryData history) {
//        HistoryNetX newNet = new HistoryNetX();
//        newNet.addHistoryData(history);
//        
//        try {
//            /** Connect the old net with the new one */
//            outArcF.compileArc(this.outputPlace, newNet.transition, false, String.class, null);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    
    /** Getter for property inputPlace.
     * @return Value of property inputPlace.
     *
     */
    public de.renew.simulator.Place getInputPlace() {
        return inputPlace;
    }
    
    /** Setter for property inputPlace.
     * @param inputPlace New value of property inputPlace.
     *
     */
    public void setInputPlace(de.renew.simulator.Place inputPlace) {
        this.inputPlace = inputPlace;
    }
    
    /** Getter for property transition.
     * @return Value of property transition.
     *
     */
    public de.renew.simulator.Transition getTransition() {
        return transition;
    }
    
    /** Setter for property transition.
     * @param transition New value of property transition.
     *
     */
    public void setTransition(de.renew.simulator.Transition transition) {
        this.transition = transition;
    }
    
    /** Getter for property outputPlace.
     * @return Value of property outputPlace.
     *
     */
    public de.renew.simulator.Place getOutputPlace() {
        return outputPlace;
    }
    
    /** Setter for property outputPlace.
     * @param outputPlace New value of property outputPlace.
     *
     */
    public void setOutputPlace(de.renew.simulator.Place outputPlace) {
        this.outputPlace = outputPlace;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //HistoryNetX net = new HistoryNetX();
        //NetInstance instance = net.buildInstance();
        //export to file?
        Demonstrator petriNetApp = new Demonstrator();
        //petriNetApp.openInstanceDrawing(instance);
    }
    
}
