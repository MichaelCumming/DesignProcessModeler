/*
 * TransitionInputPanel.java
 *
 * Created on February 28, 2004, 11:29 AM
 */

package dpm.dpmApp.desktop.forms.input;

import dpm.dpmApp.desktop.DpmTerms;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import dpm.content.constraint.Link;


/**
 *
 * @author  cumming
 */
public abstract class TransitionInputPanel extends JPanel implements DpmTerms {
    /**NOTE: categoryName refers to either transitions or states */
    protected String categoryName;
    protected Set inputSet;
    protected boolean inputSetEmpty = false;
    protected Iterator inputSetIterator;
    protected JTextField textField;
    protected static int PREFERRED_HSIZE = 315;
    protected static int PREFERRED_VSIZE = 20;
    protected static int MAX_HSIZE = 500;
    protected static int MAX_VSIZE = 20;
    /**Some unchecked items remain */
    private boolean someDeleted = false;
    /**No unchecked items remain */
    private boolean allDeleted = true;
    
    
    /** Creates new form TransitionInputPanel */
    /** Note: one panel for each transition */
    public TransitionInputPanel(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public void setUpInputSetIterator(Set inputSet) {
        if(inputSet == null || inputSet.isEmpty()) {
            this.inputSetEmpty = true;
            System.out.println("inputSet is enpty/null: " + categoryName);
        }
        else {
            this.inputSetIterator = inputSet.iterator();
        }
    }
    
    /**Must be implemented in subclasses of TransitionInputPanel */
    protected abstract Set getAllUserInput();
    
    
    public boolean isEmpty(String s) {
        return s.equals(EMPTY_STRING);
    }
    
    //ok
    protected void addTransitionHeader() {
        if(categoryName != null) {
            addOneBoldLabel("Transition:  " + categoryName);
        }
    }
    
    protected Set getTextInputOnly() {
        Set result = new HashSet();
        Set inputStrings = getTextInputStringSet();
        if(inputStrings != null && !inputStrings.isEmpty()) {
            result.addAll(inputStrings);
        }
        return result;
    }
    
    protected Set getAllCheckedInput() {
        if(inputSetEmpty) {
            return null;
        }
        Set namesChecked = new HashSet();
        Set namesUnChecked = new HashSet();
        Component[] components = this.getComponents();
        for (int i = 0; i < components.length; i++) {
            Object obj = components[i];
            if(obj instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox)obj;
                if(cb.isSelected()) {
                    String name = cb.getText();
                    namesChecked.add(name);
                }
                else {
                    String name = cb.getText();
                    namesUnChecked.add(name);
                }
            }
        }
        return namesChecked;
    }
    
    /**Retrieves input only from check boxes */
    /**If this set has items, then there exist roles to delete */
    public Set getAllUnCheckedInput() {
        if(inputSetEmpty) {
            return null;
        }
        Set rolesToKeep = new HashSet();
        Component[] components = this.getComponents();
        for (int i = 0; i < components.length; i++) {
            Object obj = components[i];
            if(obj instanceof JCheckBox) {
                JCheckBox cb = (JCheckBox)obj;
                if(cb.isSelected()) {
                    this.someDeleted = true;
                }
                /**check box is NOT selected */
                else {
                    String roleNameToKeep = cb.getText();
                    rolesToKeep.add(roleNameToKeep);
                    /**If anything not checked, then allDeleted */
                    this.allDeleted = false;
                }
            }
        }
        System.out.println("Roles to keep-" + categoryName + ": " + stringSet2String(rolesToKeep));
        System.out.println("someDeleted: " + someDeleted);
        return rolesToKeep;
    }
    
    protected Set getTextInputStringSet() {
        /** Set to hold the results */
        Set set = new HashSet();
        String s = textField.getText();
        if(s.equals(EMPTY_STRING)) {
            return null;
        }
        while (s != null) {
            int finalCommaPos = s.lastIndexOf(',');
            /** if no more commas */
            if(finalCommaPos < 0) {
                set.add(s.trim());
                return set;
            }
            String nameAtEnd = (s.substring(finalCommaPos + 1)).trim();
            set.add(nameAtEnd);
            s = s.substring(0, finalCommaPos);
        }
        return set;
    }
    
    //ok
    protected void addOneCheckBox(String text, String toolTipText, boolean checked) {
        if(text != null && !isEmpty(text)) {
            JCheckBox cb = new JCheckBox(text);
            cb.setSelected(checked);
            if(toolTipText != null) {
                cb.setToolTipText(toolTipText);
            }
            setSize(cb);
            cb.setFont(NORMAL_PLAIN);
            add(cb);
        }
    }
    
    //ok
    protected void addOneTextInputField() {
        addOnePlainLabel("Add additional roles (separate names using commas):");
        this.textField = new JTextField();
        textField.setEditable(true);
        textField.setFont(NORMAL_PLAIN);
        setSize(textField);
        add(textField);
    }
    
    //ok
    protected void addOneBoldLabel(String name) {
        JLabel label = new  JLabel(name);
        setSize(label);
        label.setFont(NORMAL_BOLD);
        add(label);
    }
    
    //ok
    protected void addOnePlainLabel(String name) {
        if(name != null) {
            JLabel label = new  JLabel(name);
            setSize(label);
            label.setFont(NORMAL_PLAIN);
            add(label);
        }
    }
    
    protected void setSize(javax.swing.JComponent component) {
        component.setMaximumSize(new Dimension(MAX_HSIZE, MAX_VSIZE));
        component.setPreferredSize(new Dimension(PREFERRED_HSIZE, PREFERRED_VSIZE));
    }
    
    /**Utility methods */
    
    protected String testS(String s) {
        int finalCommaPos = s.lastIndexOf(',');
        return s.substring(0, finalCommaPos);
    }
    
    protected void printSet(Set set) {
        for(Iterator i = set.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            System.out.println(s);
        }
    }
    
    protected void printContents() {
        System.out.println("transName: " + categoryName + "------------");
        for(Iterator i = inputSetIterator; i.hasNext(); ) {
            String role = (String)i.next();
            System.out.println("roleName: " + role);
        }
    }
    
    protected String stringSet2String(Set set) {
        String result = new String();
        for(Iterator i = set.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            if(i.hasNext()) {
                result += s + ", ";
            }
            else {
                result += s;
            }
        }
        return result;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.GridLayout(0, 1));

        setName("PolicyInputPanel");
    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    
    /** Getter for property somethingDeleted.
     * @return Value of property somethingDeleted.
     *
     */
    public boolean someDeleted() {
        return someDeleted;
    }
    
    /** Getter for property allDeleted.
     * @return Value of property allDeleted.
     *
     */
    public boolean allDeleted() {
        return allDeleted;
    }
    
    /** Getter for property categoryName.
     * @return Value of property categoryName.
     *
     */
    public java.lang.String getCategoryName() {
        return categoryName;
    }
    
    
    
}
