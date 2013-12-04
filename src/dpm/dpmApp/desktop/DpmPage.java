/*
 * DpmPage.java
 *
 * Created on November 4, 2003, 2:51 PM
 */

package dpm.dpmApp.desktop;

import dpm.content.DesignEntity;
import dpm.content.comparator.PeerAdvComparator;
import dpm.content.comparator.PeerGroupAdvComparator;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Comparator;
import javax.swing.JPanel;
import net.jxta.peergroup.PeerGroup;

/**
 *
 * @author  cumming
 */
public class DpmPage extends JPanel implements DpmTerms {
    protected DpmAppTopFrame topFrame;
    protected PeerGroup netPG;
    protected String name;
    //protected Thread thread; //set up this thread in subpages, since DpmPage not runnable
    protected int jxtaType; //one of PEER, GROUP , or other...
    protected Comparator pgComparator = new PeerGroupAdvComparator();
    protected Comparator peerComparator = new PeerAdvComparator();
    //protected ContentSearcher searcher; //each DpmPage searches for one thing--in various basePGs
    protected PeerGroup basePG;
    protected DesignEntity baseEntity;
    
    
    /** Creates a new instance of DpmPage */
    public DpmPage(String name, int jxtaType, DpmAppTopFrame topFrame, PeerGroup basePG) {
        this.name = name;
        this.topFrame = topFrame;
        this.netPG = topFrame.getNetPG();
        this.jxtaType = jxtaType;
        this.basePG = basePG;
        setSize();
    }
    
    /** Needed for LinkTreePages
     * @since July, 29, 2004 */
    public DpmPage(String name, int jxtaType, DpmAppTopFrame topFrame, DesignEntity baseEntity) {
        this.name = name;
        this.topFrame = topFrame;
        this.netPG = topFrame.getNetPG();
        this.jxtaType = jxtaType;
        this.baseEntity = baseEntity;
        setSize();
    } 
    
 
    public void printMessage(String s) {
        if(topFrame == null) {
            System.out.println("ERROR: null topFrame in DpmPage: printMessage");
        }
        else {
            topFrame.printMessage(s);
        }
    }
    
    public void printMessageContinue(String s) {
        topFrame.printMessageContinue(s);
    }
    
    public void setSize() {
        int width = 500;
        int height = 500;
        Dimension frameSize = this.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        //pack();
    }
    
    public void clear() {}
    //public Object getContentModel() {}
    
    /** Getter for property topFrame.
     * @return Value of property topFrame.
     *
     */
    public DpmAppTopFrame getTopFrame() {
        return topFrame;
    }
    
    /** Setter for property topFrame.
     * @param topFrame New value of property topFrame.
     *
     */
    public void setTopFrame(DpmAppTopFrame topFrame) {
        this.topFrame = topFrame;
    }
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public java.lang.String getName() {
        return name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /** Getter for property jxtaType.
     * @return Value of property jxtaType.
     *
     */
    public int getJxtaType() {
        return jxtaType;
    }
    
    /** Setter for property jxtaType.
     * @param jxtaType New value of property jxtaType.
     *
     */
    public void setJxtaType(int jxtaType) {
        this.jxtaType = jxtaType;
    }
    
    /** Getter for property basePG.
     * @return Value of property basePG.
     *
     */
    public PeerGroup getBasePG() {
        return basePG;
    }
    
    /** Setter for property basePG.
     * @param basePG New value of property basePG.
     *
     */
    public void setBasePG(PeerGroup basePG) {
        this.basePG = basePG;
    }
     
    /** Getter for property baseEntity.
     * @return Value of property baseEntity.
     *
     */
    public dpm.content.DesignEntity getBaseEntity() {
        return baseEntity;
    }
    
    /** Setter for property baseEntity.
     * @param baseEntity New value of property baseEntity.
     *
     */
    public void setBaseEntity(dpm.content.DesignEntity baseEntity) {
        this.baseEntity = baseEntity;
    }
    
}
