/*
 * MessageForm.java
 *
 * Created on October 14, 2004, 9:02 AM
 */

package dpm.dpmApp.desktop.forms;

import dpm.content.advertisement.AdvUtilities;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import javax.swing.JFrame;
import net.jxta.peergroup.PeerGroup;


/**
 *
 * @author  cumming
 */
public class MessageForm extends JFrame implements DpmTerms {
    protected PeerGroup parentPG;
    protected DpmAppTopFrame topFrame;
    protected AdvUtilities advUtils;
    
    
    /** Creates a new instance of MessageForm */
    public MessageForm(PeerGroup parentPG, DpmAppTopFrame topFrame) {
        this.parentPG = parentPG;
        this.topFrame = topFrame;
        this.advUtils = topFrame.getAdvUtils();
    }
    
}
