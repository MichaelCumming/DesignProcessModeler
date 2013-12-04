/*
 * NewDesignEntityForm.java
 *
 * Created on December 31, 2003, 9:20 AM
 */

package dpm.dpmApp.desktop.forms;

import dpm.content.ContentStorage;
import dpm.content.DesignEntity;
import dpm.content.advertisement.AdvUtilities;
import dpm.content.advertisement.designEntity.UserNamedEntityAdv;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.content.designEntity.UserNamedEntity;
import dpm.content.prototype.Prototype;
import dpm.content.state.DesignEntityInputPolicy;
import dpm.content.state.policy.SimpleInputPolicy;
import dpm.dpmApp.desktop.DpmAppTopFrame;
import dpm.dpmApp.desktop.DpmTerms;
import dpm.dpmApp.desktop.forms.input.TransitionInputPanel;
import dpm.dpmApp.desktop.forms.input.panels.LinksInputPanel;
import dpm.dpmApp.desktop.forms.input.panels.PolicyInputPanelNewEntity;
import dpm.peer.Peer;
import java.awt.Component;
import java.awt.GridLayout;
import java.text.DateFormatSymbols;
import java.util.*;
import javax.swing.*;
import net.jxta.peergroup.PeerGroup;
import dpm.container.tree.*;
import dpm.dpmApp.desktop.subpages.*;



/** The user input form for new Tasks that is: TaskAdvertisements.
 * @author cumming
 * @since December 31, 2003, 9:20 AM
 */
public class CloneLinksAndEntitiesForm extends JFrame implements DpmTerms {
    protected PeerGroup parentPG;
    protected DpmAppTopFrame topFrame;
    protected Prototype initPrototype;
    protected Peer appUser;
    protected AdvUtilities advUtils;
    protected String nameSuggestion;
    //protected String baseName;
    //protected String iteration;
    /**The value of the prototype radio buttons */
    protected boolean thisPeerOnly;
    protected boolean finishedStartup = false;
    
    
    /** Creates new form NewForm */
    public CloneLinksAndEntitiesForm(PeerGroup parentPG, DpmAppTopFrame topFrame, Prototype initPrototype) {
        /**loopNetAdv omitted from constructor: 30 Mar 2004 */
        /**entityType omitted from constructor 13 Sept. 2004 */
        this.parentPG = parentPG;
        this.topFrame = topFrame;
        this.initPrototype = initPrototype;
        this.appUser = topFrame.getAppUser();
        this.advUtils = topFrame.getAdvUtils();
        
        initComponents();
        /** (rows, cols). '0' means that any number of rows can be added */
        policiesPanel.setLayout(new GridLayout(0, 1));
        linksPanel.setLayout(new GridLayout(0, 1));
        
        /**1. Pre-prototype setup */
        allPeersRadioButton.setSelected(true);
        setEntityTypeComboModel();
        entityTypeComboBox.setSelectedItem(TOI_ASSIGNMENT);
        setLoopComboModel();
        loopComboBox.setSelectedItem(TOI_ASSIGNMENT_LOOP_NAME);
        setProtoAlgorithmComboBoxModel();
        protoAlgorithmComboBox.setSelectedItem(LATEST);
        
        /**2. Input panels setup: based on prototype, if possible */
        setupPoliciesPanel();
        setupLinksPanel(); //set name field after it gets prototype
        
        /**If there are no nets in storage yet, this creates one */
        setTitle("New Entity: Add Name, and Description");
        spinnerSetUp();
        /** pack after adding panels */
        pack();
        topFrame.setPosition(this.getWidth(), this.getHeight(), this);
        setNameFieldUsingPrototype(getPrototype());
        show();
        this.finishedStartup = true;
    }
    
    
    /**getLoopAdv starts up first thing */
    private void setupPoliciesPanel() {
        /**Remove all before adding new ones */
        removeAllInputPanels(policiesPanel);
        int pNum = 0;
        /**Gets transRoles either from the prototype or SimpleInputPolicy */
        ContentStorage transRoles = getTransRolesFromProtoOrPolicy();
        if(transRoles != null) {
            for (int i = 0; i < transRoles.size(); i++) {
                Integer curI = new Integer(i);
                String transNameWPrefix = transRoles.getNameWPrefix(curI);
                Set roles = transRoles.getRolesSet(curI);
                PolicyInputPanelNewEntity newPanel = new PolicyInputPanelNewEntity(transNameWPrefix, roles);
                policiesPanel.add(newPanel);
                pNum++;
            }
            //topFrame.test("Num of policy panels added", pNum, transRoles.size());
        }
        pack();
    }
    
    /**@since 26 Oct. 2004 */
    private void setupLinksPanel() {
        /**Remove all before adding new ones */
        removeAllInputPanels(linksPanel);
        /**Gets transRoles either from the prototype or SimpleInputPolicy */
        Prototype proto = getPrototype();
        if(proto != null) {
            /**Add incoming links: that end in transitions */
            if(proto.getIncomingLinks() != null) {
                addLinkPanels(proto.getTransLinksIncomingLinks(), true); //incoming = true
            }
            /**Add outgoing links: that begin in states */
            if(proto.getOutgoingLinks() != null) {
                addLinkPanels(proto.getStatesLinksOutgoingLinks(), false); //incoming = false
            }
        }
        pack();
    }
    
    /**@since 26 Oct. 2004 */
    private void addLinkPanels(ContentStorage cs, boolean incoming) {
        if(cs != null) {
            int pNum = 0;
            for (Iterator i = cs.getKeySet().iterator(); i.hasNext(); ) {
                String categoryName = (String)i.next();
                Set links = cs.getOneRowSet(categoryName);
                LinksInputPanel newPanel = new LinksInputPanel(categoryName, links, parentPG, appUser, incoming);
                linksPanel.add(newPanel);
                pNum++;
            }
            //System.out.println("Just added: " + pNum + "LinksInputPanels");
            //topFrame.test("Num of link panels added", pNum, cs.size());
        }
    }
    
    public void removeAllInputPanels(JPanel panel) {
        Component[] components = panel.getComponents();
        int pNum = 0;
        for (int i = 0; i < components.length; i++) {
            Object obj = components[i];
            if(obj instanceof TransitionInputPanel) {
                TransitionInputPanel p = (TransitionInputPanel)obj;
                panel.remove(p);
                pNum++;
            }
        }
        panel.repaint();
        //System.out.println("Just removed: " + pNum + " LinksInputPanels");
        //topFrame.test("Num of Pip panels removed", pNum, getLoopAdv().getNumberOfTransitions());
    }
    
    private void refreshInputPanels() {
        if(finishedStartup) {
            setupPoliciesPanel();
            setupLinksPanel();
        }
    }
    
    private Prototype getPrototype() {
        if(initPrototype != null) {
            setNameFieldUsingPrototype(initPrototype);
            return initPrototype;
        }
        /**Else, make a new prototype */
        String protoAlgorithm = getProtoAlgorithmNameSelected();
        String entityType = getEntityTypeSelected();
        String loopName = getLoopNameSelected();
        LoopNetAdvertisement loopNetAdv = appUser.getLoopNets().getLoopNetByName(loopName);
        /**If components for making a prototype are not available */
        if(protoAlgorithm == null || entityType == null || loopNetAdv == null) {
            setNameFieldUsingPrototype(null);
            return null;
        }
        Prototype proto = new Prototype(protoAlgorithm, entityType, loopNetAdv, thisPeerOnly, appUser);
        setNameFieldUsingPrototype(proto);
        return proto;
    }
    
    private void setNameFieldUsingPrototype(Prototype proto) {
        if(proto == null) {
            baseNameField.setText(EMPTY_STRING);
            iterationField.setText("1");
            protoNameField.setText(SPACE + NONE);
            return;
        }
        String protoBaseName = proto.getBaseName();
        /**If prototype has no usable name treat as null */
        if(protoBaseName == null) {
            setNameFieldUsingPrototype(null);
            return;
        }
        /**Else, non-null prototype with usable name */
        baseNameField.setText(protoBaseName);
        /**Increment iteration from prototype */
        iterationField.setText(appUser.updateIteration(proto.getIteration()));
        /**Set proto name field for information purposes */
        protoNameField.setText(SPACE + combineNames(protoBaseName, proto.getIteration()));
    }
    
    public String combineNames(String a, String b) {
        return a +  "_" + b;
    }
    
    
    /**Retrieves the first user named entity that has the loop name specified,
     * then returns its entity type, e.g. "DesignTask", or "TOI_Course".
     * Called on UserNamedEntities. */
    public String getEntityTypeFromLoopName(String loopName) {
        Set allEntities = appUser.getUserNamedEntities().collapseAll();
        for(Iterator i = allEntities.iterator(); i.hasNext(); ) {
            DesignEntity curEntity = (DesignEntity)i.next();
            String curLoopName = curEntity.getLoopNetAdv().getNetName();
            if(curLoopName.equals(loopName)) {
                return curEntity.getEntityType();
            }
        }
        return null;
    }
    
    /**Retrieves the first user named entity that has the entityType specified,
     * then returns its loop name, e.g. "TaskStateLoop", or "CourseStateLoop".
     * Called on UserNamedEntities. */
    public String getLoopNameFromEntityType(String entityType) {
        Set allEntities = appUser.getUserNamedEntities().collapseAll();
        for(Iterator i = allEntities.iterator(); i.hasNext(); ) {
            DesignEntity curEntity = (DesignEntity)i.next();
            String curEntityType = curEntity.getEntityType();
            if(curEntityType.equals(entityType)) {
                return curEntity.getLoopNetAdv().getNetName();
            }
        }
        return null;
    }
    
    /**Refreshes the loop ComboBox based on the entityType comboBox input */
    public void refreshLoopComboBoxFromEntityType(String entityType) {
        String loopName = null;
        String basicLoopName = getBasicLoopNameFromEntityType(entityType);
        if(basicLoopName != null) {
            loopName = basicLoopName;
        }
        else {
            loopName= getLoopNameFromEntityType(entityType);
        }
        if(loopName != null) {
            loopComboBox.setSelectedItem(loopName);
        }
    }
    
    /**Refreshes the loop ComboBox based on the entityType comboBox input */
    public void refreshEntityComboBoxFromLoopName(String loopName) {
        String entityType = null;
        String basicEntityType = getBasicEntityTypeFromLoopName(loopName);
        if(basicEntityType != null) {
            entityType = basicEntityType;
        }
        else {
            entityType = getEntityTypeFromLoopName(loopName);
        }
        if(entityType != null) {
            entityTypeComboBox.setSelectedItem(entityType);
        }
    }
    
    /**Specifies the data shown in the entity types comboBox */
    private void setEntityTypeComboModel() {
        /**Gets all the entity types discovered */
        Set entityTypes = getExistingEntityTypes();
        /**Make sure that basic entity types are present */
        entityTypes.addAll(advUtils.getBasicEntities());
        entityTypeComboBox.setModel(new DefaultComboBoxModel(new Vector(entityTypes)));
    }
    
    /**Specifies the data shown in the loop comboBox */
    private void setLoopComboModel() {
        /**Gets all the nets discovered */
        Set netNames = getExistingNetNames();
        if(netNames.size() == 0) {
            System.out.println("Error: found no existing loops");
            /**If no loopNetAdvs present, make the basic ones */
            netNames = advUtils.makeBasicNetAdvs(this);
        }
        loopComboBox.setModel(new DefaultComboBoxModel(new Vector(netNames)));
    }
    
    /**When prototype is updated, this model is updated */
    private void updateLoopComboModel() {
        Prototype proto = getPrototype();
        if(proto != null) {
            String prototypeLoopName = proto.getLoopName();
            DefaultComboBoxModel model = (DefaultComboBoxModel)loopComboBox.getModel();
            /**If name is not already in the model, then add it */
            if(model.getIndexOf(prototypeLoopName) < 0) {
                model.addElement(prototypeLoopName);
            }
            /**Select suggested loop */
            loopComboBox.setSelectedItem(prototypeLoopName);
        }
    }
    
    /**Specifies the data shown in the prototype comboBox */
    private void setProtoAlgorithmComboBoxModel() {
        Set prototypeNames = new HashSet();
        prototypeNames.add(LATEST);
        prototypeNames.add(MOST_ACTIVE);
        /**@since 28 Oct. 2004 */
        prototypeNames.add(SUM_OF_EXISTING);
        protoAlgorithmComboBox.setModel(new DefaultComboBoxModel(new Vector(prototypeNames)));
    }
    
    public String getEntityTypeSelected() {
        return (String)entityTypeComboBox.getSelectedItem();
    }
    public String getLoopNameSelected() {
        return (String)loopComboBox.getSelectedItem();
    }
    public String getProtoAlgorithmNameSelected() {
        return (String)protoAlgorithmComboBox.getSelectedItem();
    }
    
    /**Retreives transRoles either from the prototype (preferred) or from a SimpleInputPolicy */
    private ContentStorage getTransRolesFromProtoOrPolicy() {
        Prototype proto = getPrototype();
        if(proto != null && proto.getTransRoles() != null) {
            //System.out.println(">>prototype protoType: " + proto.getProtoAlgorithm());
            ContentStorage protoTransRoles = proto.getTransRoles();
            LoopNetAdvertisement protoLoopNetAdv = proto.getLoopNetAdv();
            /**Assures all transitions are present */
            return getTransRolesAllTransitions(protoTransRoles, protoLoopNetAdv);
        }
        /**Else, need to get transRoles from SimpleInputPolicy */
        String loopNameSelected = getLoopNameSelected();
        //System.out.println(">>Loop name selected: " + loopNameSelected);
        LoopNetAdvertisement loopAdv = advUtils.getLoopAdv(loopNameSelected);
        
        /**If prototype doesn't provide transRoles, then use SimpleInputPolicy instead */
        if(loopAdv != null) {
            String entityType = getEntityTypeSelected();
            DesignEntityInputPolicy simplePolicy = new SimpleInputPolicy(entityType, loopAdv, appUser);
            return simplePolicy.getTransRoles();
        }
        return null;
    }
    
    public ContentStorage getTransRolesAllTransitions(ContentStorage inputCS, LoopNetAdvertisement netAdv) {
        ContentStorage result = new ContentStorage("java.lang.String", appUser);
        for(Iterator i = netAdv.getTransitions().iterator(); i.hasNext(); ) {
            String transName = (String)i.next();
            Set roles = inputCS.getOneRowSet(transName);
            if(roles == null) {
                result.addString(transName, EMPTY_STRING);
            }
            else {
                result.addStringSet(transName, roles);
            }
        }
        return result;
    }
    
    public void addEmptyPolicyMessage() {
        JLabel emptyLabel = new JLabel();
        emptyLabel.setFont(NORMAL_PLAIN);
        emptyLabel.setText("Error: no policies have been suggested for this entity");
        policiesPanel.add(emptyLabel);
    }
    
    /** Iterates through all policy panels and gets user input*/
    public ContentStorage getAllUserInputPolicy() {
        /** keyed by transName, with roles as values */
        ContentStorage transRoles = new ContentStorage("java.lang.String", appUser);
        Component[] components = policiesPanel.getComponents();
        int pipNum = 0;
        for (int i = 0; i < components.length; i++) {
            Object obj = components[i];
            if(obj instanceof PolicyInputPanelNewEntity) {
                pipNum++;
                PolicyInputPanelNewEntity pip = (PolicyInputPanelNewEntity)obj;
                String transName = pip.getCategoryName();
                Set roles = pip.getAllUserInput();
                if(roles != null && !roles.isEmpty()) {
                    transRoles.addStringSet(transName, roles);
                }
            }
        }
        String entityName = baseNameField.getText();
        //topFrame.test("Number of pips panels found when getting input: " + entityName, pipNum, getLoopAdv().getNumberOfTransitions());
        return transRoles;
    }
    
    /** Iterates through all policy panels and gets user input*/
    //    public ContentStorage getAllUserInputLinks() {
    //        /** keyed by transName, with roles as values */
    //        ContentStorage transRoles = new ContentStorage("java.lang.String", appUser);
    //        Component[] components = policiesPanel.getComponents();
    //        int pipNum = 0;
    //        for (int i = 0; i < components.length; i++) {
    //            Object obj = components[i];
    //            if(obj instanceof PolicyInputPanelNewEntity) {
    //                pipNum++;
    //                PolicyInputPanelNewEntity pip = (PolicyInputPanelNewEntity)obj;
    //                String transName = pip.getCategoryName();
    //                Set roles = pip.getAllUserInput();
    //                if(roles != null && !roles.isEmpty()) {
    //                    transRoles.addStringSet(transName, roles);
    //                }
    //            }
    //        }
    //        String entityName = baseNameField.getText();
    //        //topFrame.test("Number of pips panels found when getting input: " + entityName, pipNum, getLoopAdv().getNumberOfTransitions());
    //        return transRoles;
    //    }
    
    public Set getAllUserInputLinks(boolean incoming) {
        Set result = new HashSet();
        Component[] components = linksPanel.getComponents();
        int pNum = 0;
        for (int i = 0; i < components.length; i++) {
            Object obj = components[i];
            if(obj instanceof LinksInputPanel) {
                LinksInputPanel panel = (LinksInputPanel)obj;
                pNum++;
                if(panel.incoming() == incoming) {
                    /**getAllUserInput() for LinksInputPanels returns a set of Links */
                    result.addAll(panel.getAllUserInput());
                }
            }
        }
        return result;
    }
    
    /**
     * A 1.4 application that demonstrates using spinners.
     */
    public void spinnerSetUp() {
        Calendar calendar = Calendar.getInstance();
        
        /** Add daySpinner */
        int currentDay = calendar.get(Calendar.DATE); //today's date
        //int currentMonth = calendar.get(Calendar.MONTH); //today's date
        /** Get the number of days in that month */
        int numDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        /** SpinnerNumberModel: initial value, min, max, step */
        SpinnerModel dayModel = new SpinnerNumberModel(currentDay, 1, numDays, 1);
        daySpinner.setModel(dayModel);
        //daySpinner = new javax.swing.JSpinner(dayModel);
        
        /** Add monthSpinner */
        String[] monthStrings = getMonthStrings(); //get month names
        SpinnerModel monthModel = new SpinnerListModel(monthStrings);
        int currentMonth = calendar.get(Calendar.MONTH);
        monthModel.setValue(monthStrings[currentMonth]);
        monthSpinner.setModel(monthModel);
        //SpinnerModel monthModel = new SpinnerNumberModel(currentMonth, 0, 11, 1);
        //monthSpinner = new javax.swing.JSpinner(monthModel);
        
        /** Add yearSpinner */
        int currentYear = calendar.get(Calendar.YEAR);
        /** SpinnerNumberModel: initial value, min, max, step */
        SpinnerModel yearModel = new SpinnerNumberModel(currentYear, currentYear - 5, currentYear + 10, 1);
        yearSpinner.setModel(yearModel);
    }
    
    /**
     * DateFormatSymbols returns an extra, empty value at the
     * end of the array of months.  Remove it.
     */
    static protected String[] getMonthStrings() {
        String[] months = new DateFormatSymbols(Locale.US).getMonths();
        int lastIndex = months.length - 1;
        
        if (months[lastIndex] == null
        || months[lastIndex].length() <= 0) { //last item empty
            String[] monthStrings = new String[lastIndex];
            System.arraycopy(months, 0, monthStrings, 0, lastIndex);
            return monthStrings;
        } else { //last item not empty
            return months;
        }
    }
    
    public int monthToInt(String monthName) {
        String[] months = new DateFormatSymbols(Locale.US).getMonths();
        int i = -1;
        for(i = 0; i < months.length; i++) {
            String curMonth = months[i];
            if (curMonth.equals(monthName)) {
                return i;
            }
        }
        return i;
    }
    
    /**@since Sept.13 2004 */
    public Set getExistingEntityTypes() {
        Set allEntities = appUser.getUserNamedEntities().collapseAll();
        Set result = new HashSet();
        if(allEntities != null && !allEntities.isEmpty()) {
            for(Iterator i = allEntities.iterator(); i.hasNext(); ) {
                DesignEntity entity = (DesignEntity)i.next();
                result.add(entity.getEntityType());
            }
        }
        return result;
    }
    
    public Set getExistingNetNames() {
        Set allNets = appUser.getLoopNets().collapseAll();
        Set result = new HashSet();
        if(allNets.iterator() != null) {
            for(Iterator i = allNets.iterator(); i.hasNext(); ) {
                LoopNetAdvertisement loopAdv = (LoopNetAdvertisement)i.next();
                result.add(loopAdv.getNetName());
            }
        }
        return result;
    }
    
    //    public String getBasicLoopNameFromEntityType(String entityType) {
    //        String loopName = null;
    //        if(
    //        entityType.equals(DESIGN_TASK) ||
    //        entityType.equals(DESIGN_PRODUCT) ||
    //        entityType.equals(TOI_COURSE) ||
    //        entityType.equals(TOI_ASSIGNMENT) ||
    //        entityType.equals(TOI_EXAM)) {
    //            loopName = entityType + "_LOOP_NAME";
    //        }
    //        return loopName;
    //    }
    
    public String getBasicLoopNameFromEntityType(String entityType) {
        if(entityType.equals(DESIGN_TASK)) {
            return DESIGN_TASK_LOOP_NAME;
        }
        else if(entityType.equals(DESIGN_PRODUCT)) {
            return DESIGN_PRODUCT_LOOP_NAME;
        }
        else if(entityType.equals(TOI_COURSE)) {
            return TOI_COURSE_LOOP_NAME;
        }
        else if(entityType.equals(TOI_ASSIGNMENT)) {
            return TOI_ASSIGNMENT_LOOP_NAME;
        }
        else if(entityType.equals(TOI_EXAM)) {
            return TOI_EXAM_LOOP_NAME;
        }
        return null;
    }
    
    public String getBasicEntityTypeFromLoopName(String loopName) {
        if(loopName.equals(DESIGN_TASK_LOOP_NAME)) {
            return DESIGN_TASK;
        }
        else if(loopName.equals(DESIGN_PRODUCT_LOOP_NAME)) {
            return DESIGN_PRODUCT;
        }
        else if(loopName.equals(TOI_COURSE_LOOP_NAME)) {
            return TOI_COURSE;
        }
        else if(loopName.equals(TOI_ASSIGNMENT_LOOP_NAME)) {
            return TOI_ASSIGNMENT;
        }
        else if(loopName.equals(TOI_EXAM_LOOP_NAME)) {
            return TOI_EXAM;
        }
        return null;
    }
    
    
    /**Refreshes the entityType comboBox based on the loopComboBox input */
    //    public void refreshPolsPanelFromLoopName() {
    //        String loopName = (String)loopComboBox.getSelectedItem();
    //        String suitableEntityType = null;
    //        String basicEntityName = getBasicEntityTypeFromLoopName(loopName);
    //        if(basicEntityName == null) {
    //            suitableEntityType = getEntityTypeFromLoopName(loopName);
    //        }
    //        else {
    //            suitableEntityType = basicEntityName;
    //        }
    //        /**If null, do nothing */
    //        if(suitableEntityType == null) {
    //            return;
    //        }
    //        //entityTypeComboBox.setSelectedItem(suitableEntityType);
    //    }
    
    //    //FINISH use prototype or not?
    //    //should prototype be made according to a loop name?
    //    /**refresh the policies panel when users change the loop manually */
    //    public ContentStorage getTransRolesFromLoopName(String loopName) {
    //        if(loopName.equals(prototype.getLoopName())) {
    //            return prototype.getTransRoles();
    //        }
    //        /**Find transRoles */
    //        else {
    //
    //
    //        }
    //        return null;
    //    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        dateChooserPanel = new javax.swing.JPanel();
        monthSpinner = new javax.swing.JSpinner();
        dayLabel = new javax.swing.JLabel();
        monthLabel = new javax.swing.JLabel();
        yearLabel = new javax.swing.JLabel();
        yearSpinner = new javax.swing.JSpinner();
        daySpinner = new javax.swing.JSpinner();
        thisPeerOnlyButtonGroup = new javax.swing.ButtonGroup();
        fieldsPanel = new javax.swing.JPanel();
        baseNameLabel = new javax.swing.JLabel();
        descriptionLabel = new javax.swing.JLabel();
        baseNameField = new javax.swing.JTextField();
        iterationField = new javax.swing.JTextField();
        descriptionField = new javax.swing.JTextField();
        iterationLabel = new javax.swing.JLabel();
        entityTypePanel = new javax.swing.JPanel();
        entityTypeLabel = new javax.swing.JLabel();
        entityTypeComboBox = new javax.swing.JComboBox();
        protoPanel = new javax.swing.JPanel();
        allPeersRadioButton = new javax.swing.JRadioButton();
        thisPeerOnlyRadioButton = new javax.swing.JRadioButton();
        protoAlgorithmLabel = new javax.swing.JLabel();
        protoAlgorithmComboBox = new javax.swing.JComboBox();
        protoNameLabel = new javax.swing.JLabel();
        protoNameField = new javax.swing.JTextField();
        loopChooserPanel = new javax.swing.JPanel();
        loopLabel = new javax.swing.JLabel();
        loopComboBox = new javax.swing.JComboBox();
        scrollPanePolicies = new javax.swing.JScrollPane();
        policiesPanel = new javax.swing.JPanel();
        scrollPaneLinks = new javax.swing.JScrollPane();
        linksPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        createEntityButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        dateChooserPanel.setLayout(new java.awt.GridBagLayout());

        dateChooserPanel.setBorder(new javax.swing.border.TitledBorder(null, "Complete by Date", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.ABOVE_TOP, new java.awt.Font("Arial", 1, 12)));
        dateChooserPanel.setFont(new java.awt.Font("Arial", 0, 12));
        dateChooserPanel.setName("dateChooserPanel");
        dateChooserPanel.setPreferredSize(new java.awt.Dimension(325, 65));
        monthSpinner.setFont(new java.awt.Font("Arial", 1, 12));
        monthSpinner.setPreferredSize(new java.awt.Dimension(90, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 10);
        dateChooserPanel.add(monthSpinner, gridBagConstraints);

        dayLabel.setFont(new java.awt.Font("Arial", 1, 12));
        dayLabel.setText("Day");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 5, 2);
        dateChooserPanel.add(dayLabel, gridBagConstraints);

        monthLabel.setFont(new java.awt.Font("Arial", 1, 12));
        monthLabel.setText("Month");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 2);
        dateChooserPanel.add(monthLabel, gridBagConstraints);

        yearLabel.setFont(new java.awt.Font("Arial", 1, 12));
        yearLabel.setText("Year");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 2);
        dateChooserPanel.add(yearLabel, gridBagConstraints);

        yearSpinner.setFont(new java.awt.Font("Arial", 1, 12));
        yearSpinner.setPreferredSize(new java.awt.Dimension(60, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 5);
        dateChooserPanel.add(yearSpinner, gridBagConstraints);

        daySpinner.setFont(new java.awt.Font("Arial", 1, 12));
        daySpinner.setPreferredSize(new java.awt.Dimension(35, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 5, 10);
        dateChooserPanel.add(daySpinner, gridBagConstraints);

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setName("NewTaskFormFrame");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        fieldsPanel.setLayout(new java.awt.GridBagLayout());

        fieldsPanel.setBorder(new javax.swing.border.MatteBorder(new java.awt.Insets(1, 1, 1, 1), new java.awt.Color(204, 204, 204)));
        fieldsPanel.setFont(new java.awt.Font("Arial", 0, 12));
        fieldsPanel.setName("fieldsPanel");
        fieldsPanel.setPreferredSize(new java.awt.Dimension(327, 65));
        baseNameLabel.setFont(new java.awt.Font("Arial", 1, 12));
        baseNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        baseNameLabel.setText("Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        fieldsPanel.add(baseNameLabel, gridBagConstraints);

        descriptionLabel.setFont(new java.awt.Font("Arial", 1, 12));
        descriptionLabel.setText("Description");
        descriptionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        fieldsPanel.add(descriptionLabel, gridBagConstraints);

        baseNameField.setFont(new java.awt.Font("Arial", 0, 12));
        baseNameField.setPreferredSize(new java.awt.Dimension(100, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        fieldsPanel.add(baseNameField, gridBagConstraints);

        iterationField.setEditable(false);
        iterationField.setFont(new java.awt.Font("Arial", 0, 12));
        iterationField.setBorder(null);
        iterationField.setMinimumSize(new java.awt.Dimension(25, 19));
        iterationField.setPreferredSize(new java.awt.Dimension(25, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        fieldsPanel.add(iterationField, gridBagConstraints);

        descriptionField.setFont(new java.awt.Font("Arial", 0, 12));
        descriptionField.setMinimumSize(new java.awt.Dimension(125, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        fieldsPanel.add(descriptionField, gridBagConstraints);

        iterationLabel.setFont(new java.awt.Font("Arial", 0, 11));
        iterationLabel.setText("Iteration");
        iterationLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        fieldsPanel.add(iterationLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        getContentPane().add(fieldsPanel, gridBagConstraints);

        entityTypePanel.setLayout(new java.awt.GridBagLayout());

        entityTypeLabel.setFont(new java.awt.Font("Arial", 1, 11));
        entityTypeLabel.setText("Entity Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        entityTypePanel.add(entityTypeLabel, gridBagConstraints);

        entityTypeComboBox.setBackground(new java.awt.Color(255, 255, 255));
        entityTypeComboBox.setEditable(true);
        entityTypeComboBox.setFont(new java.awt.Font("Arial", 0, 11));
        entityTypeComboBox.setMinimumSize(new java.awt.Dimension(31, 20));
        entityTypeComboBox.setPreferredSize(new java.awt.Dimension(150, 20));
        entityTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                entityTypeComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        entityTypePanel.add(entityTypeComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 4, 2);
        getContentPane().add(entityTypePanel, gridBagConstraints);

        protoPanel.setLayout(new java.awt.GridBagLayout());

        allPeersRadioButton.setFont(new java.awt.Font("Arial", 0, 11));
        allPeersRadioButton.setSelected(true);
        allPeersRadioButton.setText("All Peers");
        thisPeerOnlyButtonGroup.add(allPeersRadioButton);
        allPeersRadioButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        allPeersRadioButton.setMaximumSize(new java.awt.Dimension(68, 15));
        allPeersRadioButton.setMinimumSize(new java.awt.Dimension(68, 15));
        allPeersRadioButton.setPreferredSize(new java.awt.Dimension(68, 15));
        allPeersRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allPeersRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        protoPanel.add(allPeersRadioButton, gridBagConstraints);

        thisPeerOnlyRadioButton.setFont(new java.awt.Font("Arial", 0, 11));
        thisPeerOnlyRadioButton.setText("This Peer Only");
        thisPeerOnlyButtonGroup.add(thisPeerOnlyRadioButton);
        thisPeerOnlyRadioButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        thisPeerOnlyRadioButton.setMaximumSize(new java.awt.Dimension(100, 15));
        thisPeerOnlyRadioButton.setMinimumSize(new java.awt.Dimension(100, 15));
        thisPeerOnlyRadioButton.setPreferredSize(new java.awt.Dimension(100, 15));
        thisPeerOnlyRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thisPeerOnlyRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        protoPanel.add(thisPeerOnlyRadioButton, gridBagConstraints);

        protoAlgorithmLabel.setFont(new java.awt.Font("Arial", 1, 11));
        protoAlgorithmLabel.setText("New entity based on:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        protoPanel.add(protoAlgorithmLabel, gridBagConstraints);

        protoAlgorithmComboBox.setBackground(new java.awt.Color(255, 255, 255));
        protoAlgorithmComboBox.setFont(new java.awt.Font("Arial", 0, 11));
        protoAlgorithmComboBox.setMinimumSize(new java.awt.Dimension(31, 20));
        protoAlgorithmComboBox.setPreferredSize(new java.awt.Dimension(150, 20));
        protoAlgorithmComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                protoAlgorithmComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        protoPanel.add(protoAlgorithmComboBox, gridBagConstraints);

        protoNameLabel.setFont(new java.awt.Font("Arial", 1, 11));
        protoNameLabel.setText("Prototype Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        protoPanel.add(protoNameLabel, gridBagConstraints);

        protoNameField.setEditable(false);
        protoNameField.setFont(new java.awt.Font("Arial", 0, 11));
        protoNameField.setBorder(null);
        protoNameField.setMinimumSize(new java.awt.Dimension(50, 19));
        protoNameField.setPreferredSize(new java.awt.Dimension(50, 19));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        protoPanel.add(protoNameField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 10, 2);
        getContentPane().add(protoPanel, gridBagConstraints);

        loopChooserPanel.setLayout(new java.awt.GridBagLayout());

        loopLabel.setFont(new java.awt.Font("Arial", 1, 11));
        loopLabel.setText("State Loop Model");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        loopChooserPanel.add(loopLabel, gridBagConstraints);

        loopComboBox.setBackground(new java.awt.Color(255, 255, 255));
        loopComboBox.setFont(new java.awt.Font("Arial", 0, 11));
        loopComboBox.setMinimumSize(new java.awt.Dimension(31, 20));
        loopComboBox.setPreferredSize(new java.awt.Dimension(150, 20));
        loopComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopComboBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        loopChooserPanel.add(loopComboBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        getContentPane().add(loopChooserPanel, gridBagConstraints);

        scrollPanePolicies.setBorder(new javax.swing.border.TitledBorder(null, "Select Roles for each Transition", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.ABOVE_TOP, new java.awt.Font("Arial", 1, 11)));
        scrollPanePolicies.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPanePolicies.setDoubleBuffered(true);
        scrollPanePolicies.setMinimumSize(new java.awt.Dimension(350, 125));
        scrollPanePolicies.setName("scrollPane");
        scrollPanePolicies.setPreferredSize(new java.awt.Dimension(350, 125));
        policiesPanel.setLayout(new java.awt.GridLayout(0, 1));

        policiesPanel.setFont(new java.awt.Font("Arial", 0, 12));
        policiesPanel.setName("policiesPanel");
        scrollPanePolicies.setViewportView(policiesPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        getContentPane().add(scrollPanePolicies, gridBagConstraints);

        scrollPaneLinks.setBorder(new javax.swing.border.TitledBorder(null, "Select Linked Entities to be Recreated", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.ABOVE_TOP, new java.awt.Font("Arial", 1, 11)));
        scrollPaneLinks.setDoubleBuffered(true);
        scrollPaneLinks.setMinimumSize(new java.awt.Dimension(350, 125));
        scrollPaneLinks.setName("scrollPane");
        scrollPaneLinks.setPreferredSize(new java.awt.Dimension(350, 125));
        linksPanel.setLayout(new java.awt.GridLayout(0, 1));

        linksPanel.setFont(new java.awt.Font("Arial", 0, 12));
        linksPanel.setName("policiesPanel");
        scrollPaneLinks.setViewportView(linksPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        getContentPane().add(scrollPaneLinks, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setFont(new java.awt.Font("Arial", 0, 12));
        buttonsPanel.setName("buttonsPanel");
        createEntityButton.setFont(new java.awt.Font("Arial", 1, 11));
        createEntityButton.setText("Create Entity");
        createEntityButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        createEntityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createEntityButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(createEntityButton, new java.awt.GridBagConstraints());

        cancelButton.setFont(new java.awt.Font("Arial", 1, 11));
        cancelButton.setText("Cancel");
        cancelButton.setMargin(new java.awt.Insets(2, 10, 2, 10));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonsPanel.add(cancelButton, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        getContentPane().add(buttonsPanel, gridBagConstraints);

        pack();
    }//GEN-END:initComponents
    
    private void thisPeerOnlyRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thisPeerOnlyRadioButtonActionPerformed
        // Add your handling code here:
        //System.out.println("thisPeerOnly = true");
        this.thisPeerOnly = true;
        //System.out.println("update prototype: thisPeerOnlyRadioButton");
        refreshInputPanels();
    }//GEN-LAST:event_thisPeerOnlyRadioButtonActionPerformed
    
    private void allPeersRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allPeersRadioButtonActionPerformed
        // Add your handling code here:
        //System.out.println("thisPeerOnly = false");
        this.thisPeerOnly = false;
        //System.out.println("update prototype: allPeersRadioButton");
        refreshInputPanels();
    }//GEN-LAST:event_allPeersRadioButtonActionPerformed
    
    private void protoAlgorithmComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_protoAlgorithmComboBoxActionPerformed
        // Add your handling code here:
        if((String)protoAlgorithmComboBox.getSelectedItem() != null) {
            //System.out.println("update prototype: protoAlgorithmComboBox");
            refreshInputPanels();
        }
    }//GEN-LAST:event_protoAlgorithmComboBoxActionPerformed
    
    private void entityTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entityTypeComboBoxActionPerformed
        // Add your handling code here:
        if((String)entityTypeComboBox.getSelectedItem() != null) {
            String entityType = (String)entityTypeComboBox.getSelectedItem();
            refreshLoopComboBoxFromEntityType(entityType);
            refreshInputPanels();
        }
    }//GEN-LAST:event_entityTypeComboBoxActionPerformed
    
    private void loopComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopComboBoxActionPerformed
        // Add your handling code here:
        if((String)loopComboBox.getSelectedItem() != null) {
            String loopName = (String)loopComboBox.getSelectedItem();
            refreshEntityComboBoxFromLoopName(loopName);
            refreshInputPanels();
        }
    }//GEN-LAST:event_loopComboBoxActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // Add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    private void createEntityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createEntityButtonActionPerformed
        // Add your handling code here:
        if(baseNameField.getText() == null || baseNameField.getText().equals(EMPTY_STRING)) {
            topFrame.showErrorDialog("Please enter a name", this);
            return;
        }
        //        if(descriptionField.getText() == null || descriptionField.getText().equals(EMPTY_STRING)) {
        //            topFrame.showErrorDialog("Please enter a description", this);
        //            return;
        //        }
        String netName = (String)loopComboBox.getSelectedItem();
        LoopNetAdvertisement loopAdv = appUser.getLoopNets().getLoopNetByName(netName);
        if(loopAdv == null) {
            System.out.println("ERROR: loopAdv is null in new design entity form");
            return;
        }
        String baseName = baseNameField.getText();
        String iteration = iterationField.getText();
        String desc = descriptionField.getText();
        Integer year = (Integer)yearSpinner.getValue();
        String monthName = (String)monthSpinner.getValue();
        int month = monthToInt(monthName);
        Integer day = (Integer)daySpinner.getValue();
        Date dateDue = new GregorianCalendar(year.intValue(), month, day.intValue()).getTime();
        ContentStorage userInput = getAllUserInputPolicy();
        
        //        /**If this is the first time iterating */
        //        if(iteration.equals("1") || baseName.equals(EMPTY_STRING) || baseName==null) {
        //            baseName = name;
        //        }
        try {
            String entityType = getEntityTypeSelected();
            UserNamedEntityAdv newAdv = advUtils.createUserNamedEntityAdvertisement(
            baseName, iteration, desc, dateDue, loopAdv, parentPG, entityType);
            
            if(newAdv != null) {
                advUtils.createAllPolicies(newAdv, userInput, parentPG);
            }
            /**Refresh the tree with the new leaf */
            topFrame.refreshTreeWithNodeSelected();
            
            /**Now, recreate all the links and linked entities, if checked in LinksInputPanels */
            Set incomingLinks = getAllUserInputLinks(true);
            Set outgoingLinks = getAllUserInputLinks(false);
            DesignEntity entityJustCreated = new UserNamedEntity(newAdv, loopAdv, appUser);
            
            advUtils.cloneAllLinksAndEntities(incomingLinks, entityJustCreated, parentPG, true);
            advUtils.cloneAllLinksAndEntities(outgoingLinks, entityJustCreated, parentPG, false);
            /**Refresh the topFrame */
            topFrame.refreshTreeWithNodeSelected();
            dispose();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_createEntityButtonActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        this.dispose();
    }//GEN-LAST:event_exitForm
    
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allPeersRadioButton;
    private javax.swing.JTextField baseNameField;
    private javax.swing.JLabel baseNameLabel;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton createEntityButton;
    private javax.swing.JPanel dateChooserPanel;
    private javax.swing.JLabel dayLabel;
    private javax.swing.JSpinner daySpinner;
    private javax.swing.JTextField descriptionField;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JComboBox entityTypeComboBox;
    private javax.swing.JLabel entityTypeLabel;
    private javax.swing.JPanel entityTypePanel;
    private javax.swing.JPanel fieldsPanel;
    private javax.swing.JTextField iterationField;
    private javax.swing.JLabel iterationLabel;
    private javax.swing.JPanel linksPanel;
    private javax.swing.JPanel loopChooserPanel;
    private javax.swing.JComboBox loopComboBox;
    private javax.swing.JLabel loopLabel;
    private javax.swing.JLabel monthLabel;
    private javax.swing.JSpinner monthSpinner;
    private javax.swing.JPanel policiesPanel;
    private javax.swing.JComboBox protoAlgorithmComboBox;
    private javax.swing.JLabel protoAlgorithmLabel;
    private javax.swing.JTextField protoNameField;
    private javax.swing.JLabel protoNameLabel;
    private javax.swing.JPanel protoPanel;
    private javax.swing.JScrollPane scrollPaneLinks;
    private javax.swing.JScrollPane scrollPanePolicies;
    private javax.swing.ButtonGroup thisPeerOnlyButtonGroup;
    private javax.swing.JRadioButton thisPeerOnlyRadioButton;
    private javax.swing.JLabel yearLabel;
    private javax.swing.JSpinner yearSpinner;
    // End of variables declaration//GEN-END:variables
    
    
}
