/*
 * DpmTerms.java
 *
 * Created on March 16, 2004, 10:37 AM
 */

package dpm.dpmApp.desktop;

import java.awt.Color;
import java.awt.Font;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Collects all the static String terms in one place
 * @author  cumming
 */
public interface DpmTerms {
    public static final String APP_NAME = "Design Process Modeler";
    public static final String VERSION = "0.76";
    
    //best startup performance?: false,false,false
    //most likely to be practical?: false, true, false?
    public static boolean SEARCHER_ON_AT_NODE_CONSTRUCTION = true;
    public static boolean TURN_SEARCHER_ON_AT_TREE_EXPANSION = false; //keep searching on always see: CST:
    public static boolean TURN_SEARCHER_OFF_AT_TREE_COLLAPSE = false; //keep searching on always
    
    public static final Date EARLIEST_DATE = new GregorianCalendar(2004, Calendar.JANUARY, 1).getTime();
    
    /**Prototype names*/
    public static final String LATEST = "latest entity created";
    public static final String MOST_ACTIVE = "most active entity";
    public static final String SUM_OF_EXISTING = "sum of existing entities";
    //public static final String AVERAGE = "average";
    public static final String SYNTHETIC_ENTITY = "synthetic entity";
    public static final String DEFAULT_PROTO_TYPE = LATEST;
    
    public static final String ALL = "all";
    public static final String PGS = "peerGroups";
    public static final String PEERS = "peers";
    public static final String NETS = "nets";
    public static final String LINKS = "links";
    public static final String MESSAGES = "chat messages";
    public static final String SPACE = " ";
    public static final String NONE = "none";
    public static final String EMPTY_ENTRY = "empty entry";
    
    public static final String ABANDON = "abandon";
    public static int TIMEOUT = 3000; // = 3 seconds. Can be adjusted
    public static int WAIT_TIME = 30 * 1000; //time to wait after remote message
    
    public static final String NEWLINE = "\n";
    public static final String DOTTED_LINE = 
    "----------------------------------------------------------------------------------";
    /***/
    public static final int PAGE_WIDTH = 500;
    public static final int PAGE_HEIGHT = 300;
    public static final int MAX_RECURSION_DEPTH = 10;
    /**See: DisplayUserObject */
    public static final int LINE_LENGTH = PAGE_WIDTH/7;
    //public static final String NEWLINE_HTML = "<p>";
    //public static final String HTML_LINE = "<HR>";
    
    public static final String YES_CHOICE = "yesChoice";
    public static final String NO_CHOICE = "noChoice";
    
    /**doc styles used in history viewer */
    public static final String REGULAR = "regular";
    public static final String ITALIC = "italic";
    public static final String BOLD = "bold";
    public static final String SMALL = "small";
    public static final String LARGE = "large";
    
    public final static String DPM_PGNAME = "dpmNet";
    public final static String DPM_PGDESC = "a peergroup for users of the DPM application";
    public static final String DPM_URL = "urn:jxta:uuid-3FD35406FFC44A81A406B8C1C7C79E1302";
    public final static String DPM_LOOPS_PGNAME = "dpmLoops";
    public final static String DPM_LOOPS_PGDESC = "a peergroup containing process loop models";
    public static final String DPM_LOOPS_URL = "urn:jxta:uuid-92A8997DE7294ACFA741C4A823225AC302";
    
    /**Basic content */
    public static final String USER_NAMED_ENTITY = "UserNamedEntity";
    public static final String DESIGN_TASK = "DesignTask";
    public static final String DESIGN_PRODUCT = "DesignProduct";
    //public static final String CHOICE_POINT = "ChoicePoint";
    public static final String TOI_COURSE = "TOI_Course";
    public static final String TOI_ASSIGNMENT = "TOI_Assignment";
    public static final String TOI_EXAM = "TOI_Exam";
    /**Basic content loops */
    public static final String DESIGN_TASK_LOOP_FILE = "DesignTaskStateLoop.rnw";
    public static final String DESIGN_TASK_LOOP_NAME = "DesignTaskStateLoop";
    public static final String DESIGN_PRODUCT_LOOP_FILE = "DesignProductStateLoop.rnw";
    public static final String DESIGN_PRODUCT_LOOP_NAME = "DesignProductStateLoop";
    public static final String CHOICE_POINT_LOOP_FILE = "ChoicePointStateLoop.rnw";
    public static final String CHOICE_POINT_LOOP_NAME = "ChoicePointStateLoop";
    public static final String TOI_COURSE_LOOP_FILE = "TOI_CourseStateLoop.rnw";
    public static final String TOI_COURSE_LOOP_NAME = "TOI_CourseStateLoop";
    public static final String TOI_ASSIGNMENT_LOOP_FILE = "TOI_AssignmentStateLoop.rnw";
    public static final String TOI_ASSIGNMENT_LOOP_NAME = "TOI_AssignmentStateLoop";
    public static final String TOI_EXAM_LOOP_FILE = "TOI_ExamStateLoop.rnw";
    public static final String TOI_EXAM_LOOP_NAME = "TOI_ExamStateLoop";
    
    
    /**Client, performer types for various entity types
     * see: SimpleInputPolicy */
    public static final String TOI_CLIENT = "toi_admin";
    public static final String TOI_PERFORMER = "student";
    public static final String NON_TOI_CLIENT = "client";
    public static final String NON_TOI_PERFORMER = "performer";
    
    public static final String ANY = "Any"; /** Means: one of: PEER, PRODUCT, TASK */
    public static final String ENTITIES = "entities";
    
    public static final String DO_BEFORE = "doBefore";
    public static final String IS_SUB_ENTITY_OF = "IsSubEntityOf";
    public static final String SEQ_ENTITY = "sequential";
    
    //public static final String DO_AFTER = "doAfter";
    public static final String NOT_RELEVANT = "not relevant";
    public static String CREATE_TRANS = "design_entity_creation";
    
    
    public static final String STATIC_APP_NAME = "PNKApp";
    public static final String DEFAULT_PERSON_DIR = System.getProperty("user.dir");
    public static final String EMPTY_STRING = "";
    //public static final String ANY_ROLE = "any role";
    public static final String CLIENT = "client";
    public static final String PERFORMER = "performer";
    public static final String AUTHOR = "author";
    public static final String OBSERVER = "observer";
    public static final String ENTITY_RECYCLER = "entity recycler";
    public static final String [] ROLES = {CLIENT, PERFORMER, AUTHOR, OBSERVER};
    
    public static final String INCOMING = "incoming";
    public static final String OUTGOING = "outgoing";
    
    //private static String CONSIDERED_DONE_STRING;
    //public static String STATE_CHANGER_NET_FILE = "StateChangerNet.rnw";
    //public static String STATE_CHANGER_NET_NAME = "StateChangerNet";
    public final static int RECTANGLE = 1; //Node's type (=shape) is a Rectangle
    public final static int ROUNDRECT = 2; //Node's type (=shape) is a RoundRectangle
    public final static int ELLIPSE = 3; //Node's type (=shape) is an Ellipse.
    public final static int CIRCLE = 4; //Node's type (=shape) is a Circle.
    
    public final static int ENTITY_TYPE = RECTANGLE; //Node's type (=shape) is a Rectangle
    public final static int ENTITY_SHAPE = RECTANGLE; //Node's type (=shape) is a Rectangle
    
    public final static Color ENTITY_COLOR = Color.white; //FFFFFF
    public final static Color SILVER = new Color(153, 255, 204); //99FFCC
    //public final static Color LIGHT_YELLOW = new Color(255, 255, 153); //FFFF99
    public final static Color LIGHT_YELLOW = new Color(255, 255, 204); //FFFFCC
    public final static Color LIGHTER_YELLOW = new Color(255, 255, 220); //FFFFDC
    public final static Color LIGHT_GREEN = new Color(204, 255, 204); //use #CCFFCC in html
    public final static Color MED_GREY = new Color(204, 204, 204); //use #CCCCCC in html
    
    /**Logical colors */
    public final static Color USER_INPUT_COLOR = Color.white;
    public final static Color INFO_DISPLAY_COLOR = LIGHT_GREEN;
    public final static Color IMPORTANT_INFO_DISPLAY_COLOR = LIGHT_YELLOW;
    
    public final static Color PRIVATE_CHAT_MESSAGE_COLOR = IMPORTANT_INFO_DISPLAY_COLOR;
    public final static String PRIVATE_CHAT_MESSAGE_HEX = "FFFFCC"; //private chat messages; see: CWG p.151
    public final static Color CHAT_MESSAGE_COLOR = INFO_DISPLAY_COLOR; //similar INFO_DISPLAY
    public final static String CHAT_MESSAGE_HEX = "CCFFCC"; //chat messages; light_yellow; see: CWG p.151
    
    
    /**IMPORTANT: Delete check display items. Allows developer to switch off checking, in this one location only.
     * See usage: PGTree amd LinkTree */
    public final static boolean LINKS_CHECK = true;
    public final static boolean ENTITIES_CHECK = true;
    public final static boolean PEERGROUPS_CHECK = true;
    public final static boolean NETS_CHECK = true;
    
    public final static int LINK_PAGE_WIDTH = 300;
    public final static int LINK_PAGE_HEIGHT = 225;
    
    
    public final static Font ARIAL_BOLD_10 = new Font("Arial", Font.BOLD, 10);
    public final static Font ARIAL_BOLD_11 = new Font("Arial", Font.BOLD, 11);
    public final static Font ARIAL_BOLD_12 = new Font("Arial", Font.BOLD, 12);
    public final static Font ARIAL_PLAIN_11 = new Font("Arial", Font.PLAIN, 11);
    public final static Font ARIAL_PLAIN_12 = new Font("Arial", Font.PLAIN, 12);
    public final static Font ARIAL_PLAIN_ITALIC_11 = new Font("Arial", Font.PLAIN | Font.ITALIC, 11);
    public final static Font ARIAL_PLAIN_ITALIC_12 = new Font("Arial", Font.PLAIN | Font.ITALIC, 12);
    //public final static Font ARIAL_BOLD_11_BLUE = new Font("Arial", Font.PLAIN | Font.ITALIC, 11 | Font);
    
    /**Logical fonts*/
    public final static Font NORMAL_PLAIN = ARIAL_PLAIN_12;
    public final static Font NORMAL_BOLD = ARIAL_BOLD_11;
    public final static Font NORMAL_BOLD_LARGE = ARIAL_BOLD_12;
    public final static Font NORMAL_ITALIC = ARIAL_PLAIN_ITALIC_12;
    
    public final static Font PG_NODE_SELECTED = NORMAL_BOLD;
    public final static Font PG_NODE_UNSELECTED = NORMAL_BOLD;
    public final static Font PG_LEAF_SELECTED = NORMAL_BOLD;
    public final static Font PG_LEAF_UNSELECTED = NORMAL_PLAIN;
    
   
    
}
