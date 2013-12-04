/*
 * DisplayUserObject.java
 *
 * Created on December 5, 2003, 8:11 AM
 */

package dpm.content;

import dpm.content.advertisement.*;
import dpm.content.advertisement.chat.*;
import dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.peer.Peer;
import java.util.*;
import javax.swing.*;
import javax.swing.Icon;
import javax.swing.JLabel;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.*;


/**
 * The object that is displayed in peergroup trees. Where the display icons for various tree items are set.
 * @author  cumming
 * @since December 5, 2003, 8:11 AM
 */

//the "user object" to put into TreeNodes
//either a PeerGroup or an Advertisement can be put in--one or the other
public class DisplayUserObject extends JLabel implements DpmTerms {
    private Object obj;
    private PeerGroup peerGroup;
    private Peer appUser;
    private String displayState = null;
    
    //private Icon pgIcon = new ImageIcon("icons/0.PG_OPEN.png"); //NOTE: tree nodes don't have icons 11 Nov.2004
    public Icon PG_ADV_ICON = new ImageIcon(this.getClass().getResource("/icons/redDot.png"));
    public Icon PEER_ADV_ICON = new ImageIcon(this.getClass().getResource("/icons/0.ORANGE_DOT.png"));//ok
    public Icon ENTITY_ICON = new ImageIcon(this.getClass().getResource("/icons/0.BLUE_DOT.png"));//ok
    public Icon LINK_ICON = new ImageIcon(this.getClass().getResource("/icons/0.LINK.png")); //ok
    public Icon ABANDON_ICON = new ImageIcon(this.getClass().getResource("/icons/0.ABANDON.png"));
    public Icon NET_ADV_ICON = new ImageIcon(this.getClass().getResource("/icons/greyDot.png")); //ok
    
    private String START_HTML = "<html>";
    private String END_BODY = "</body>";
    private String END_HTML = "</html>";
    private String NEWLINE_HTML = "<p>";
    
    
    /** Creates a new instance of DisplayUserObject */
    public DisplayUserObject(Object obj, Peer appUser) {
        this.obj = obj;
        this.appUser = appUser;
        /**Note that tool tip manager must be activated on the tree: See PGTree/LinkTree */
        
        /**e.g. DesignTask, DesignProduct, TOI_Exam, TOI_Course, etc. */
        if(obj instanceof DesignEntity) {
            DesignEntity entity = (DesignEntity)obj;
            String name;
            /**If a new entity, then omit the iteration */
            if(entity.atFirstIteration()) {
                name = entity.getBaseName();
            }
            else {
                name = entity.getFullName();
            }
            /**@since Dec 22, 2004 */
            this.displayState = getStateString(entity);
            setText(getNameAndTypeString(name, entity.getEntityType()) + SPACE + displayState);
            
            setToolTipText(
            START_HTML +
            "Name: " + name + NEWLINE_HTML +
            "Description: " + entity.getDescription() + NEWLINE_HTML +
            "Author: " + entity.getAuthorName() + NEWLINE_HTML +
            "Loop used: " + entity.getLoopNetAdv().getNetName() + NEWLINE_HTML +
            getRolesStringsThisAppUser(entity) + NEWLINE_HTML +
            getInputsNeededThisTransition(entity) + NEWLINE_HTML +
            getInputsStillNeededStringThisAppUser(entity) +
            END_HTML);
            
            setFont(PG_LEAF_UNSELECTED);
            if(entity.isAbandoned(appUser)) {
                setIcon(ABANDON_ICON);
            }
            else {
                setIcon(ENTITY_ICON);
            }
        }
        if(obj instanceof PeerGroup) {
            this.peerGroup = (PeerGroup)obj;
            String pgName = peerGroup.getPeerGroupName();
            setText(pgName);
            
            setToolTipText(
            START_HTML +
            getNameAndTypeString(pgName, "PeerGroup") + NEWLINE_HTML +
            "Description: " + peerGroup.getPeerGroupAdvertisement().getDescription() +
            END_HTML);
            //need a PGNode for this:
            //+ "Remote Searching: " + peerGroup.getRemoteSearching());
            
            //setIcon(pgIcon);
            setFont(PG_NODE_UNSELECTED);
            setFocusable(true);
        }
        
        /**@since 25 Nov 2004 */
        if(obj instanceof IPGMemberAdvertisement) {
            IPGMemberAdvertisement memberAdv = (IPGMemberAdvertisement)obj;
            String name = memberAdv.getPeerName();
            setText(name);
            
            setToolTipText(
            START_HTML +
            getNameAndTypeString(name, "Peer") + NEWLINE_HTML +
            "Member of this peergroup since: " + memberAdv.getDateCreate().toString() +
            END_HTML);
            
            setIcon(PEER_ADV_ICON);
            setFont(PG_LEAF_UNSELECTED);
        }
        
        if(obj instanceof PeerGroupAdvertisement) {
            PeerGroupAdvertisement pgAdv = (PeerGroupAdvertisement)obj;
            String name = pgAdv.getName();
            setText(name);
            setToolTipText(getNameAndTypeString(name, "PeerGroupAdvertisement"));
            
            setIcon(PG_ADV_ICON);
            setFont(PG_LEAF_UNSELECTED);
        }
        /** Applies to both LoopNet and StateNet advertisements */
        if(obj instanceof NetAdvertisement) {
            //System.out.println("Making a display user object of a net adv");
            NetAdvertisement netAdv = (NetAdvertisement)obj;
            String name = netAdv.getNetName();
            setText(getNameAndTypeString(name, "NetAdvertisement"));
            setToolTipText(getStatesTransitionsString((LoopNetAdvertisement)netAdv));
            
            setIcon(NET_ADV_ICON);
            setFont(PG_LEAF_UNSELECTED);
        }
        if(obj instanceof LinkAdvertisement) {
            LinkAdvertisement linkAdv = (LinkAdvertisement)obj;
            
            String basicName = linkAdv.getDescriptionBasic();
            String typesName = linkAdv.getDescriptionTypes();
            /**See below */
            String stateTransName = getDescriptionStateTrans(linkAdv);
            String fullName = linkAdv.getDescriptionTypeAndStateTrans();
            
            if(linkAdv.isDoBefore()) {
                setText(typesName);
                setToolTipText(getStringDescriptionDoBeforeLong(linkAdv));
            }
            else {
                setText(typesName);
            }
            setIcon(LINK_ICON);
            setFont(PG_LEAF_UNSELECTED);
        }
        if(obj instanceof PrivateChatAdvertisement) {
            PrivateChatAdvertisement chatAdv = (PrivateChatAdvertisement)obj;
            
            /**This is only place that html is used in code
             * @since 8 Oct. 2004 */
            String START_CODE = "<html><body bgcolor=#" + PRIVATE_CHAT_MESSAGE_HEX + "><font color=BLUE>";
            //String START_HTML = "<html><font color=BLUE>";
            String AUTHOR = "Private message for " + chatAdv.getRecipientName() + " from " + chatAdv.getAuthorName();
            String SIGN = " > ";
            String BLACK_ON = "<font color=BLACK>";
            String CREATED = " [" + chatAdv.getDateCreate() + "]";
            String message = chatAdv.getMessage();
            
            if(message != null) {
                setText(
                START_CODE +
                AUTHOR + BLACK_ON +
                CREATED + SIGN + NEWLINE_HTML +
                addBreaks(message) +
                END_BODY + END_HTML);
                setToolTipText("[PrivateChatAdvertisement]");
            }
            setFont(NORMAL_PLAIN);
        }
        if(obj instanceof ChatAdvertisement) {
            ChatAdvertisement chatAdv = (ChatAdvertisement)obj;
            
            /**DisplayUserObject is only place that html is used in code
             * @since 8 Oct. 2004 */
            String START_CODE = "<html><body bgcolor=#" + CHAT_MESSAGE_HEX + "><font color=BLUE>";
            //String START_HTML = "<html><font color=BLUE>";
            String AUTHOR = "Message from " + chatAdv.getAuthorName();
            String SIGN = " > ";
            String BLACK_ON = "<font color=BLACK>";
            String CREATED = " [" + chatAdv.getDateCreate() + "]";
            String message = chatAdv.getMessage();
            
            if(message != null) {
                setText(
                START_CODE +
                AUTHOR + BLACK_ON +
                CREATED + SIGN + NEWLINE_HTML +
                addBreaks(message) +
                END_BODY + END_HTML);
                setToolTipText("[ChatAdvertisement]");
            }
            setFont(NORMAL_PLAIN);
        }
    }
    
    public String getDescriptionStateTrans(LinkAdvertisement linkAdv) {
        return
        START_HTML +
        "[Link]: " + NEWLINE_HTML +
        "Author: " + linkAdv.getAuthorName() + NEWLINE_HTML +
        linkAdv.combineNames(linkAdv.getSourceBaseName(), linkAdv.getSourceIteration()) + SPACE +
        linkAdv.roundBracket(linkAdv.getSourceState()) + NEWLINE_HTML +
        linkAdv.getConstraintName() + NEWLINE_HTML +
        linkAdv.combineNames(linkAdv.getBaseName(), linkAdv.getIteration()) + SPACE +
        linkAdv.roundBracket(linkAdv.getTransName()) +
        END_HTML;
    }
    
    public String getStringDescriptionDoBeforeLong(LinkAdvertisement linkAdv) {
        /**Suitable for doBefores only. All others the transNames/sourceStates are not relevant */
        return
        START_HTML +
        "[Link]: " + NEWLINE_HTML +
        "Author: " + linkAdv.getAuthorName() + NEWLINE_HTML +
        "SOURCE " + linkAdv.combineNames(linkAdv.getSourceBaseName(), linkAdv.getSourceIteration()) + " must be in state: " + NEWLINE_HTML +
        linkAdv.getSourceState() + NEWLINE_HTML +
        "To enable TARGET " + linkAdv.combineNames(linkAdv.getBaseName(), linkAdv.getIteration()) + "'s transition: " + NEWLINE_HTML +
        linkAdv.getTransName();
    }
    
    /**@since 8 Nov. 2004 */
    public String getStatesTransitionsString(LoopNetAdvertisement loopNetAdv) {
        String s = new String();
        TreeSet states = loopNetAdv.getStates();
        TreeSet transitions = loopNetAdv.getTransitions();
        
        s += START_HTML;
        s += loopNetAdv.getNetName() + ": " + NEWLINE_HTML;
        s += "Author: " + loopNetAdv.getAuthorName() + NEWLINE_HTML;
        /** all possible transitions */
        for(int i = 0; i < states.size(); i++ ) {
            String curState = loopNetAdv.getValue(i, states);
            String curTrans = loopNetAdv.getValue(i, transitions);
            
            s +=
            NEWLINE_HTML +
            "State: " + curState + NEWLINE_HTML +
            "Transition: " + curTrans + NEWLINE_HTML;
        }
        s += END_HTML;
        return s;
    }
    
    /**Used for all entities to standardize the display of name and type */
    public String getNameAndTypeString(String name, String type) {
        return name + ": [" + type + "]";
    }
    
    /** Gets the role advs that have been created by this appUser, for this design entity */
    public String getRolesStringsThisAppUser(DesignEntity entity) {
        Set roles = entity.getAssumedRoleNamesThisAppUser();
        if (roles.isEmpty()) {
            roles.add(NONE);
        }
        return "Your roles: " + entity.stringSet2String(roles);
    }
    
    public String getInputsStillNeededStringThisAppUser(DesignEntity entity) {
        LoopNetAdvertisement loopNetAdv = entity.getLoopNetAdv();
        if(loopNetAdv == null) {
            return "ERROR loopNetAdv is null";
        }
        String nextTrans = loopNetAdv.getNextTransFromState(entity.getCurrentState());
        Set stillNeeded = entity.getStillNeededInputsThisAppUser(nextTrans); //ok---
        if(stillNeeded.isEmpty()) {
            return EMPTY_STRING;
        }
        return " Your input needed for role[s]: "  + entity.stringSet2String(stillNeeded);
    }
    
    public String getInputsNeededThisTransition(DesignEntity entity) {
        LoopNetAdvertisement loopNetAdv = entity.getLoopNetAdv();
        if(loopNetAdv == null) {
            return "ERROR loopNetAdv is null";
        }
        String nextTrans = loopNetAdv.getNextTransFromState(entity.getCurrentState());
        Set inputsNeeded = entity.getRequiredRolesAnyPeer(nextTrans);
        if(inputsNeeded.isEmpty()) {
            return EMPTY_STRING;
        }
        return " Roles involved in next transition: "  + entity.stringSet2String(inputsNeeded);
    }
    
    /**For DesignEntities only */
    public String getStateString(DesignEntity entity) {
        return new String(entity.getCurrentState()); // + ")");
    }
     
    public static String addBreaks(String message) {
        StringTokenizer tokens = new StringTokenizer(message, SPACE, true); //true = returns delimiters
        String NEWLINE_HTML = "<p>";
        StringBuffer result = new StringBuffer();
        String word = null;
        int curLength = 0;
        
        while (tokens.hasMoreTokens()) {
            word = tokens.nextToken();
            curLength += word.length();
            if(curLength > LINE_LENGTH) {
                result.append(word + NEWLINE_HTML);
                curLength = 0;
            }
            else {
                result.append(word);
            }
        }
        return result.toString();
    }
    
    
    public String toString() {
        return getText();
    }
    
    /** Getter for property peerGroup.
     * @return Value of property peerGroup.
     *
     */
    public PeerGroup getPeerGroup() {
        return peerGroup;
    }
    
    /** Setter for property peerGroup.
     * @param peerGroup New value of property peerGroup.
     *
     */
    public void setPeerGroup(PeerGroup peerGroup) {
        this.peerGroup = peerGroup;
    }

    /** Getter for property obj.
     * @return Value of property obj.
     *
     */
    public java.lang.Object getObj() {
        return obj;
    }
    
    /** Setter for property obj.
     * @param obj New value of property obj.
     *
     */
    public void setObj(java.lang.Object obj) {
        this.obj = obj;
    }
    
    public DesignEntity getDesignEntity() {
        if (obj instanceof DesignEntity) {
            return (DesignEntity)obj;
        }
        return null;
    }
    
    /**
     * Getter for property displayState.
     * @return Value of property displayState.
     */
    public java.lang.String getDisplayState() {
        return displayState;
    }
    
    /**
     * Setter for property displayState.
     * @param displayState New value of property displayState.
     */
    public void setDisplayState(java.lang.String displayState) {
        this.displayState = displayState;
    }
    
}
