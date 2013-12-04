/*
 * Colors.java
 *
 * Created on August 14, 2003, 4:20 PM
 */

package dpm.dpmApp.desktop;

import java.awt.Color;

/** Currently not used.
 * @author cumming
 */
public class DpmColor {
    //Color resources to use below...
    public final static Color brightOrange = new Color(255,153,000);
    public final static Color dkGray = new Color(102,051,000);
    public final static Color silverGray = new Color(204,204,204);
    public final static Color darkerGray = new Color(051,051,000);
    public final static Color paleYellow0 = new Color(255,255,204); //ColoringWebGraphics.2 p.235
    public final static Color paleYellow1 = new Color(255,255,153);
    public final static Color paleYellow2 = new Color(255,255,102);
    public final static Color brown = new Color(153,153,000);
    public final static Color coolBlue0 = new Color(051,204,204);
    public final static Color coolBlue1 = new Color(0,255,255);
    public final static Color coolBlue2 = new Color(153,204,204);
    //
    //SET COLORS HERE
    //Functional categories---------------------------------
    public final static Color PEER_BACKGROUND = Color.orange;
    public final static Color PEERGROUP_BACKGROUND = Color.orange;
    //
    public final static Color USER_INPUT_BACKGROUND = Color.lightGray;
    public final static Color USER_OUTPUT_BACKGROUND = Color.white;
    //
    //set text using default text
    public final static Color DEFAULT_TEXT = Color.black;
    public final static Color LIGHT_TEXT = Color.white;
    public final static Color DARK_TEXT = Color.black;
    //
    //set app's background using default background
    public final static Color DEFAULT_BACKGROUND = silverGray;
    public final static Color LIGHT_BACKGROUND = silverGray;
    public final static Color LIGHT_FOREGROUND = Color.lightGray;
    //
    public final static Color DARK_BACKGROUND = Color.black;
    //public final static Color DARK_FOREGROUND = Color.black;
    //public final static Color BRIGHT_BACKGROUND = Color.orange;
    //
    //System colors-------------------------------------
    public final static Color INFO = Color.magenta.darker();
    public final static Color ALERT = Color.red;
    public final static Color ERROR = Color.red.darker();public final static Color fireRed = new Color(255,000,000);
    
    
    /** No instances allowed... */
    private DpmColor() {
    }
    
}
