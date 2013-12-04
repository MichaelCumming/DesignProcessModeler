package dpm.content.advertisement.net;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import net.jxta.document.*;
import net.jxta.document.AdvertisementFactory.Instantiator;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import dpm.content.advertisement.*;
import dpm.content.advertisement.net.*;
import dpm.content.advertisement.designEntity.*;
import dpm.content.advertisement.designEntity.related.*;
import dpm.content.*;
import dpm.peer.*;


/** Once a task attains a certain state, this is advertised using a DoBeforeAdvertisement
 * @author cumming */
public class LoopNetAdvertisement extends NetAdvertisement implements DeleteChecker {
    
    /** tags specific to LoopNetAdvs (but not to StateNetAdvs) */
    public static final String statesTag = "States";
    public static final String stateNameTag = "StateName";
    public static final String transitionsTag = "Transitions";
    public static final String transNameTag = "TransitionName";
    public static final String commentsTag = "Comments";
    public static final String commentNameTag = "CommentName";
    
    
    private static final String[] fields =
    {advIDTag, netNameTag, netContentTag, dateCreateTag, authorIDTag, authorNameTag, statesTag, stateNameTag, transitionsTag, transNameTag, commentsTag, commentNameTag};
    
    /** attributes specific to LoopNetAdvs (but not to StateNetAdvs) */
    /** Names require prefixes - TreeSet orders the elements in their 'natural' order */
    protected TreeSet states = new TreeSet();
    protected TreeSet transitions = new TreeSet();
    protected TreeSet comments = new TreeSet();
    
    //protected static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
    protected static DateFormat dateFormat =
    DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.US);
    
    
    public boolean isDeleted(Peer appUser) {
        String storageKey = getAdvID().toString();
        return appUser.getDeleteAdvs().storageKeyExists(storageKey);
    }
    
    /** {@inheritDoc} */
    public static class Instantiator implements AdvertisementFactory.Instantiator {
        /** {@inheritDoc} */
        public String getAdvertisementType() {
            return "jxta:LoopNetAdv";
        }
        /** {@inheritDoc} */
        public Advertisement newInstance() {
            return new LoopNetAdvertisement();
        }
        /** {@inheritDoc} */
        public Advertisement newInstance(Element root) {
            return new LoopNetAdvertisement(root);
        }
    };
    
    /** Private Constructor, use the instantiator */
    protected LoopNetAdvertisement() {
    }
    
    protected LoopNetAdvertisement(Element root) {
        this();
        initialize(root);
    }
    
    public static String getAdvertisementType() {
        return "jxta:LoopNetAdv";
    }
    
    /** Builds a Java object from a structured text document (the adv) */
    private void initialize(Element root) {
        if (!TextElement.class.isInstance(root))
            throw new IllegalArgumentException(getClass().getName() + " only supports TextElement");
        TextElement doc = (TextElement)root;
        if (!doc.getName().equals(getAdvertisementType()))
            throw new IllegalArgumentException("Could not construct : " + getClass().getName() + " from doc containing a '" +
            doc.getName() + "'. Should be : " + getAdvertisementType());
        Enumeration elements = doc.getChildren();
        //
        while (elements.hasMoreElements()) {
            TextElement elem = (TextElement)elements.nextElement();
            //
            if (elem.getName().equals(advIDTag)) {
                try {
                    URL advID = IDFactory.jxtaURL(elem.getTextValue());
                    setAdvID(IDFactory.fromURL(advID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable advID in loop net advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad advID in loop net advertisement");
                }
                continue;
            }
            if (elem.getName().equals(netNameTag)) {
                setNetName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(dateCreateTag)) {
                try {
                    setDateCreate(dateFormat.parse(elem.getTextValue()));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (elem.getName().equals(authorIDTag)) {
                try {
                    URL authorID = IDFactory.jxtaURL(elem.getTextValue());
                    setAuthorID((PeerID)IDFactory.fromURL(authorID));
                } catch (UnknownServiceException badID) {
                    throw new IllegalArgumentException("Unusable author ID in loop net advertisement");
                } catch (MalformedURLException badID) {
                    throw new IllegalArgumentException("Bad author ID in loop net advertisement");
                }
                continue;
            }
            if (elem.getName().equals(authorNameTag)) {
                setAuthorName(elem.getTextValue());
                continue;
            }
            if (elem.getName().equals(statesTag)) {
                for(Enumeration statesEnum = elem.getChildren(); statesEnum.hasMoreElements(); ) {
                    elem = (TextElement)statesEnum.nextElement();
                    if (elem.getName().equals(stateNameTag)) {
                        states.add(elem.getTextValue());
                    }
                }
                continue;
            }
            if (elem.getName().equals(transitionsTag)) {
                for(Enumeration transEnum = elem.getChildren(); transEnum.hasMoreElements(); ) {
                    elem = (TextElement)transEnum.nextElement();
                    if (elem.getName().equals(transNameTag)) {
                        transitions.add(elem.getTextValue());
                    }
                }
                continue;
            }
            if (elem.getName().equals(commentsTag)) {
                for(Enumeration commentsEnum = elem.getChildren(); commentsEnum.hasMoreElements(); ) {
                    elem = (TextElement)commentsEnum.nextElement();
                    if (elem.getName().equals(commentNameTag)) {
                        comments.add(elem.getTextValue());
                    }
                }
                continue;
            }
        }
    }
    
    /** Builds a structured text document from a Java object */
    public Document getDocument(MimeMediaType mediaType) {
        StructuredTextDocument adv = (StructuredTextDocument)
        StructuredDocumentFactory.newStructuredDocument(mediaType, getAdvertisementType());
        if (adv instanceof Attributable) {
            ((Attributable)adv).addAttribute("xmlns:jxta", "http://jxta.org");
        }
        if (getAdvID().equals(ID.nullID))
            throw new IllegalStateException("Loop net adv has no assigned advID");
        TextElement e = adv.createElement(advIDTag, getAdvID().toString());
        adv.appendChild(e);
        e = adv.createElement(netNameTag, getNetName());
        adv.appendChild(e);
        e = adv.createElement(dateCreateTag, dateFormat.format(getDateCreate()));
        adv.appendChild(e);
        if (getAuthorID().equals(ID.nullID))
            throw new IllegalStateException("Loop net adv has no assigned author ID");
        e = adv.createElement(authorIDTag, getAuthorID().toString());
        adv.appendChild(e);
        e = adv.createElement(authorNameTag, getAuthorName());
        adv.appendChild(e);
        //
        /** Add states */
        TextElement s = adv.createElement(statesTag);
        adv.appendChild(s);
        for (Iterator i = states.iterator(); i.hasNext(); ) {
            String stateName = (String)i.next();
            e = adv.createElement(stateNameTag, stateName);
            s.appendChild(e);
        }
        /** Add transitions */
        TextElement t = adv.createElement(transitionsTag);
        adv.appendChild(t);
        for (Iterator i = transitions.iterator(); i.hasNext(); ) {
            String transName = (String)i.next();
            e = adv.createElement(transNameTag, transName);
            t.appendChild(e);
        }
        /** Add comments */
        TextElement c = adv.createElement(commentsTag);
        adv.appendChild(c);
        for (Iterator i = comments.iterator(); i.hasNext(); ) {
            String commentName = (String)i.next();
            e = adv.createElement(commentNameTag, commentName);
            c.appendChild(e);
        }
        //NOTE!
        //e = adv.createElement(netContentTag, getNetContent());
        //adv.appendChild(e);
        
        return adv;
    }
    
    /** NOTE: States, transitions, and comments ORDERING assumptions:
     * 1. They are ordered according to their numeric prefix. Therefore, each must be named properly.
     * 2. All are stored in TreeSet which orders the collection according to their numeric prefix
     * 3. All names come from a petri net loop model. This allows other loops to be used in future,
     * without altering the code.
     */
    
    /** Determines if a transition name is part of the definition loop */
    public boolean transNameOK(String transName) {
        return elementNameOK(transName, transitions);
    }
    /** Determines if a place name is part of the definition loop */
    public boolean stateNameOK(String stateName) {
        return elementNameOK(stateName, states);
    }
    private boolean elementNameOK(String elementName, TreeSet ts) {
        return ts.contains(elementName);
    }
    
    /** assumes first element is ordered due to its prefix */
    public String getFirstState() {
        //return getFirst(states);
        return (String)states.first();
    }
    /** assumes first element is ordered due to its prefix */
    public String getFirstTransition() {
        return (String)transitions.first();
    }
    /** assumes last element is ordered due to its prefix */
    public String getLastState() {
        return (String)states.last();
    }
    /** assumes last element is ordered due to its prefix */
    public String getLastTransition() {
        return (String)transitions.last();
    }
    
    public String getNextState(String currentState) {
        return getNextElement(currentState, states);
    }
    public String getNextTransition(String currentTrans) {
        return getNextElement(currentTrans, transitions);
    }
    /** get the next element from a sorted set */
    private String getNextElement(String current, TreeSet ts) {
        if (elementNameOK(current, ts)) {
            if(current.equals(ts.last())) {
                return (String)ts.first();
            }
            /** Else current is not at the end */
            int curIdx = getIndex(current, ts);
            return getValue(curIdx + 1, ts);
        }
        return "ERROR in getNextElement";
    }
    
    /** Returns the index position of the specific string, from the sorted set */
    public int getIndex(String s, TreeSet ts) {
        int idx = 0;
        for(Iterator i = ts.iterator(); i.hasNext(); ) {
            String cur = (String)i.next();
            if(cur.equals(s)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }
    
    /** returns the String at specified index, from the collection */
    public String getValue(int index, TreeSet ts) {
        int idx = 0;
        for(Iterator i = ts.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            if(idx == index) {
                return s;
            }
            idx++;
        }
        return null;
    }
    
    /** Rule: states and transitions are at corresponding positions,
     * in respective sorted sets */
    public String getNextTransFromState(String currentState) {
        if (stateNameOK(currentState)) {
            int stateIdx = getIndex(currentState, states);
            /** same position in transitions */
            return getValue(stateIdx, transitions);
        }
        return null;
    }
    
    /** Rule: states are at an incremented position to transition,
     * in respective sorted sets */
    public String getNextStateFromTrans(String currentTrans) {
        if (transNameOK(currentTrans)) {
            /** last trans connects to first state */
            if(currentTrans.equals(getLastTransition())) {
                return getFirstState();
            }
            int transIdx = getIndex(currentTrans, transitions);
            /** same position in transitions */
            return getValue(transIdx + 1, states);
        }
        return null;
    }
    
    /** Return true if 'nameA' is less than 'nameB' */
    /** NOTE: assumes that nextTrans are given the same prefix number as their preceding states */
    public boolean before(String stateA, String stateB) {
        if(stateNameOK(stateA) && stateNameOK(stateB)) {
            
            if(stateA.equals(stateB)) {
                return false;
            }
            /** assume states stored in a sorted set */
            for(Iterator i = states.iterator(); i.hasNext(); ) {
                String cur = (String)i.next();
                if (cur.equals(stateA)) {
                    return true;
                }
                if (cur.equals(stateB)) {
                    return false;
                }
            }
        }
        return false;
    }
    
    public int getNumberOfStates() {
        return states.size();
    }
    public int getNumberOfTransitions() {
        return transitions.size();
    }
    
    /** {@inheritDoc} */
    public String[] getIndexFields() {
        return fields;
    }
    
    public synchronized ID getID() {
        return advID;
    }
    
    /** Getter for property authorID.
     * @return Value of property authorID.
     *
     */
    public PeerID getAuthorID() {
        return authorID;
    }
    
    /** Setter for property authorID.
     * @param authorID New value of property authorID.
     *
     */
    public void setAuthorID(PeerID authorID) {
        this.authorID = authorID;
    }
    
    /** Getter for property authorName.
     * @return Value of property authorName.
     *
     */
    public java.lang.String getAuthorName() {
        return authorName;
    }
    
    /** Setter for property authorName.
     * @param authorName New value of property authorName.
     *
     */
    public void setAuthorName(java.lang.String authorName) {
        this.authorName = authorName;
    }
    
    /** Getter for property advID.
     * @return Value of property advID.
     *
     */
    public ID getAdvID() {
        return advID;
    }
    
    /** Setter for property advID.
     * @param advID New value of property advID.
     *
     */
    public void setAdvID(ID advID) {
        this.advID = advID;
    }
    
    /** Getter for property dateCreate.
     * @return Value of property dateCreate.
     *
     */
    public Date getDateCreate() {
        return dateCreate;
    }
    
    /** Setter for property dateCreate.
     * @param dateCreate New value of property dateCreate.
     *
     */
    public void setDateCreate(Date dateCreate) {
        this.dateCreate = dateCreate;
    }
    
    /** Getter for property netName.
     * @return Value of property netName.
     *
     */
    public java.lang.String getNetName() {
        return netName;
    }
    
    /** Setter for property netName.
     * @param netName New value of property netName.
     *
     */
    public void setNetName(java.lang.String netName) {
        this.netName = netName;
    }
    
    /** Getter for property netContent.
     * @return Value of property netContent.
     *
     */
    public java.lang.String getNetContent() {
        return netContent;
    }
    
    /** Setter for property netContent.
     * @param netContent New value of property netContent.
     *
     */
    public void setNetContent(java.lang.String netContent) {
        this.netContent = netContent;
    }
    
    /** Getter for property states.
     * @return Value of property states.
     *
     */
    public java.util.TreeSet getStates() {
        return states;
    }
    
    /** Setter for property states.
     * @param states New value of property states.
     *
     */
    public void setStates(java.util.TreeSet states) {
        this.states = states;
    }
    
    /** Getter for property transitions.
     * @return Value of property transitions.
     *
     */
    public java.util.TreeSet getTransitions() {
        return transitions;
    }
    
    /** Setter for property transitions.
     * @param transitions New value of property transitions.
     *
     */
    public void setTransitions(java.util.TreeSet transitions) {
        this.transitions = transitions;
    }
    
    /** Getter for property comments.
     * @return Value of property comments.
     *
     */
    public java.util.TreeSet getComments() {
        return comments;
    }
    
    /** Setter for property comments.
     * @param comments New value of property comments.
     *
     */
    public void setComments(java.util.TreeSet comments) {
        this.comments = comments;
    }
    
    
    
}
