/*
 * DpmAppTopFrame.java
 *
 * Created on November 4, 2003, 11:57 AM
 */

package dpm.dpmApp.desktop;

import CH.ifa.draw.application.DrawingViewFrame;
import CH.ifa.draw.framework.Drawing;
import de.renew.gui.Demonstrator;
import dpm.container.PGUtilities;
import dpm.container.tree.PGTree;
import dpm.container.tree.PGTreeLeaf;
import dpm.container.tree.PGTreeNode;
import dpm.content.*;
import dpm.content.DesignEntity;
import dpm.content.advertisement.AdvUtilities;
import dpm.content.advertisement.DesignEntityAdv;
import dpm.content.advertisement.IPGAdvertisement;
import dpm.content.advertisement.chat.ChatAdvertisement;
import dpm.content.advertisement.chat.PrivateChatAdvertisement;
import dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement;
import dpm.content.advertisement.net.LoopNetAdvertisement;
import dpm.content.designEntity.UserNamedEntity;
import dpm.content.state.*;
import dpm.dpmApp.desktop.forms.*;
import dpm.dpmApp.desktop.forms.LinkTreeForm;
import dpm.dpmApp.desktop.forms.NewDesignEntityForm;
import dpm.dpmApp.desktop.forms.ResignRoleForm;
import dpm.dpmApp.desktop.forms.information.*;
import dpm.dpmApp.desktop.forms.information.SimpleInformationForm;
import dpm.dpmApp.desktop.forms.input.*;
import dpm.dpmApp.desktop.forms.input.DeletePoliciesForm;
import dpm.dpmApp.desktop.forms.links.*;
import dpm.dpmApp.desktop.forms.links.NewSequentialLinkChildOnlyForm;
import dpm.dpmApp.desktop.forms.links.NewSubEntityChildOnlyForm;
import dpm.dpmApp.desktop.forms.links.NewSubEntityParentChildForm;
import dpm.dpmApp.desktop.forms.messages.NewChatMessageForm;
import dpm.dpmApp.desktop.forms.messages.NewPrivateChatMessageForm;
import dpm.dpmApp.desktop.subpages.ListPage;
import dpm.dpmApp.desktop.subpages.PeerGroupsTreePage;
import dpm.peer.Peer;
import dpm.peer.peerImpl.Person;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.*;
import net.jxta.exception.*;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import dpm.content.advertisement.*;




/** Top frame of the Design Process Modeler application that contains the 'main'
 * class.
 * @author cumming
 */
public class DpmAppTopFrame extends JFrame implements DpmTerms {
    private int panelCount = 0;
    private PeerGroup netPG = null; //NetPeerGroup = worldwide
    /**The peergroup in which all DPM activity takes place */
    private PeerGroup dpmNet = null;
    /**A peergroup in which dpm loops are placed */
    private PeerGroup dpmLoopsNet = null;
    private Peer appUser; //the Peer that owns this page
    private ListPage peersPage;
    private ListPage peerGroupsPage;
    private ListPage tasksPage;
    private PeerGroupsTreePage treePageWorld;
    private PeerGroupsTreePage treePageDPM;
    private PGUtilities pgUtilities;
    private AdvUtilities advUtils;
    private Demonstrator petriNetApp;
    private int messageCount = 0; //number of messages in the scrolling message pane
    //private final static Logger LOG = Logger.getLogger(DpmAppTopFrame.class.getName());
    /**show options for main tree panel */
    private String show = ALL; //what to show when displaying children of nodes
    private boolean finishedStartup = false;
    private int numThreads = 0;
    
    
    /** Creates new form DpmAppTopFrame */
    public DpmAppTopFrame() {
        System.out.println(getUserDirDescription());
        System.out.println("startup 21: creating GUI components for app");
        initComponents();
        messageArea.setBackground(INFO_DISPLAY_COLOR);
        System.out.println("startup 20: adding welcome message");
        /** setPosition: width, height, thisFrame */
        printMessage("Welcome to DesignProcessModeler " + VERSION);
        printMessage("Please wait while the JXTA system starts");
        System.out.println("startup 19: finished constructing top frame ");
    }
    
    public String getUserDirDescription() {
        return "This application is running from directory: " + NEWLINE +
        System.getProperty("user.dir");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.out.println("startup 22: At beginning of startup - constructing top frame");
        DpmAppTopFrame thisApp = new DpmAppTopFrame();
        System.out.println("startup 18: starting Jxta");
        thisApp.startJxta(thisApp);
    }
    
    public void startJxta(DpmAppTopFrame thisApp) {
        //System.out.println("startup 18: configuring log file");
        //copy this to any class where you need logging
        //URL url = this.getClass().getResource("log4j.config.SIMPLE.txt");
        //PropertyConfigurator.configure(url);
        System.out.println("startup 17: getting World peergroup");
        
        try {
            PeerGroup worldPG = PeerGroupFactory.newNetPeerGroup();
            System.out.println("startup 16: setting World peergroup found");
            printMessage("Found the net peergroup called: " + worldPG.getPeerGroupName());
            this.netPG = worldPG;
            System.out.println("startup 15: checking whether netPG is null");
            if(netPG == null) {
                showErrorDialog("ERROR: netPG is null, must exit", this);
                return;
            }
        }
        catch (PeerGroupException e) {
            showErrorDialog("Couldn't find the net peergroup...must quit", this);
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("startup 14: registering new adv types");
        registerNewAdvTypes();
        System.out.println("startup 13: getting local peer");
        this.appUser = new Person(netPG, thisApp); //netPG knows who the local peer is (= owner of this app)
        System.out.println("startup 12: creating PGUtils");
        this.pgUtilities = new PGUtilities(thisApp); //makes all PGs--needed at start
        System.out.println("startup 11: creating advUtils");
        this.advUtils = new AdvUtilities(thisApp); //makes all PGs--needed at start
        System.out.println("startup 10: adding netPG to storage");
        appUser.getCsPG().addPG(netPG, null);
        
        System.out.println("startup 09: finding DPM peergroup");
        this.dpmNet = pgUtilities.findStaticPG(DPM_PGNAME, DPM_PGDESC, DPM_URL, netPG);
        System.out.println("startup 08: finding DPM loops peergroup");
        if(dpmNet == null) {
            showErrorDialog("ERROR: " + DPM_PGNAME + " is null, must exit", this);
            return;
        }
        this.dpmLoopsNet = pgUtilities.findStaticPG(
        DPM_LOOPS_PGNAME, DPM_LOOPS_PGDESC, DPM_LOOPS_URL, dpmNet);
        System.out.println("startup 07: setting peer in top frame");
        
        appUser.setTopFrame(this);
        System.out.println("startup 06: setting pgUtils in top frame");
        pgUtilities.setTopFrame(this);
        System.out.println("startup 05: creating World peergroup page");
        
        //        this.treePageWorld = (PeerGroupsTreePage)addPanel("Tree: World", DiscoveryService.GROUP, netPG);
        //        /**Stop this one, but keep all DPM ones going at construction */
        //        System.out.println("startup 18a");
        //        treePageWorld.stopRemoteSearching();
        System.out.println("startup 04: creating DPM peergroup page");
        this.treePageDPM = (PeerGroupsTreePage)addPanel("Tree: DPM", DiscoveryService.GROUP, dpmNet);
        System.out.println("startup 03: setting position and title of top frame");
        
        setPosition(this.getWidth(), this.getHeight(), this);
        setTitle(APP_NAME + " " + VERSION +  ". Peer Name: " + appUser.getName());
        System.out.println("startup 02: packing JFrame");
        thisApp.pack();
        System.out.println("startup 01a: creating initial nets, if required");
        createInitialNetAdvs();
        System.out.println("startup 01: finished startup");
        //tabbedPane.setSelectedIndex(0);
        
        //Start-up sequence:
        //1. main
        //2. DpmAppTopFrame constructor (in main)
        //3. startJxta
        //End
        
        System.out.println("startup 00: end of startup");
        treePageDPM.getRoot().addChildrenAndExpand();
        thisApp.show();
        this.finishedStartup = true;
        /**This appears to have an effect on startup time */
        //treePageDPM.startRemoteSearchingRoot();
    }
    
    /**Creates basic nets on initial startup */
    public void createInitialNetAdvs() {
        Set netNames = getExistingNetNames();
        if(netNames.size() == 0) {
            /**If no loopNetAdvs present, make the basic ones */
            netNames = advUtils.makeBasicNetAdvs(this);
            /**@since 12 Nov. 2004 */
            minimizeAllDrawings();
            // these don't work:
            //petriNetApp = null;
            //getPetriNetApp().exit();
            //getPetriNetApp().closeAllSimulationDrawings();
        }
    }
    
    /**@since 12 Nov. 2004 */
    public void minimizeAllDrawings() {
        Demonstrator app = getPetriNetApp();
        for(Enumeration e = app.drawings(); e.hasMoreElements(); ) {
            Drawing d = (Drawing)e.nextElement();
            DrawingViewFrame frame = app.getViewFrame(d);
            frame.setState(frame.ICONIFIED);
        }
    }
    
    public Drawing getFirstDrawing() {
        for(Enumeration e = getPetriNetApp().drawings(); e.hasMoreElements(); ) {
            return (Drawing)e.nextElement();
        }
        return null;
    }
    
    public PeerGroup findNetPG() {
        try {
            PeerGroup netPG = PeerGroupFactory.newNetPeerGroup();
            printMessage("Found the net peergroup called: " + netPG.getPeerGroupName());
            System.out.println("Found the netPG");
            //NOTE:
            //csPG.addPG("World", netPG);
            return netPG;
        }
        catch (PeerGroupException e) {
            //couldn't find netPG, can't continue...
            showErrorDialog("Couldn't find the net peergroup...must quit", this);
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
    
    /**Used in PGTree and LinkTree to enable tool tips for nodes.
     * Tool tip text set in DisplayUserObject */
    public void toolTipRegister(JComponent comp) {
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.registerComponent(comp);
        //System.out.println("tool tip initial delay: " + ttm.getInitialDelay()); //default 750 ms
        //System.out.println("tool tip reshow delay: " + ttm.getReshowDelay()); //default 500 ms
        ttm.setInitialDelay(50);
        ttm.setReshowDelay(50);
        ttm.setDismissDelay(10000);
    }
    
    public void registerNewAdvTypes() {
        dpm.content.advertisement.IPGAdvertisement.Instantiator ipgInst =
        new dpm.content.advertisement.IPGAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(ipgInst.getAdvertisementType(), ipgInst);
        //
        dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement.Instantiator policyInst =
        new dpm.content.advertisement.designEntity.related.constraint.PolicyAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(policyInst.getAdvertisementType(), policyInst);
        //
        dpm.content.advertisement.designEntity.related.RoleAdvertisement.Instantiator roleInst =
        new dpm.content.advertisement.designEntity.related.RoleAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(roleInst.getAdvertisementType(), roleInst);
        //
        dpm.content.advertisement.designEntity.related.InputAdvertisement.Instantiator inputInst =
        new dpm.content.advertisement.designEntity.related.InputAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(inputInst.getAdvertisementType(), inputInst);
        //
        dpm.content.advertisement.designEntity.related.HistoryAdvertisement.Instantiator historyInst =
        new dpm.content.advertisement.designEntity.related.HistoryAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(historyInst.getAdvertisementType(), historyInst);
        //
        dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement.Instantiator linkInst =
        new dpm.content.advertisement.designEntity.related.constraint.LinkAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(linkInst.getAdvertisementType(), linkInst);
        //
        dpm.content.advertisement.net.LoopNetAdvertisement.Instantiator loopNetInst =
        new dpm.content.advertisement.net.LoopNetAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(loopNetInst.getAdvertisementType(), loopNetInst);
        //
        dpm.content.advertisement.DeleteAdvertisement.Instantiator deleteInst =
        new dpm.content.advertisement.DeleteAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(deleteInst.getAdvertisementType(), deleteInst);
        //
        dpm.content.advertisement.designEntity.UserNamedEntityAdv.Instantiator uneInst =
        new dpm.content.advertisement.designEntity.UserNamedEntityAdv.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(uneInst.getAdvertisementType(), uneInst);
        //
        dpm.content.advertisement.chat.ChatAdvertisement.Instantiator chatInst =
        new dpm.content.advertisement.chat.ChatAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(chatInst.getAdvertisementType(), chatInst);
        //
        dpm.content.advertisement.chat.PrivateChatAdvertisement.Instantiator privateChatInst =
        new dpm.content.advertisement.chat.PrivateChatAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(privateChatInst.getAdvertisementType(), privateChatInst);
        //
        dpm.content.advertisement.IPGMemberAdvertisement.Instantiator memberInst =
        new dpm.content.advertisement.IPGMemberAdvertisement.Instantiator();
        AdvertisementFactory.registerAdvertisementInstance(memberInst.getAdvertisementType(), memberInst);
    }
    
    public DpmPage addPanel(String name, int jxtaType, PeerGroup basePG) {
        if(basePG != null) {
            DpmPage inputPage = null; //page being added
            
            if (jxtaType == DiscoveryService.GROUP && name.equals("Tree: World")) {
                inputPage = new PeerGroupsTreePage(name, this, basePG, null, PAGE_WIDTH, PAGE_HEIGHT);
            }
            else if (jxtaType == DiscoveryService.GROUP && name.equals("Tree: DPM")) {
                inputPage = new PeerGroupsTreePage(name, this, basePG, null, PAGE_WIDTH, PAGE_HEIGHT);
            }
            if(inputPage != null) {
                panelCount++; //number of panel in tabbedPane
                tabbedPane.addTab(name, inputPage);
                inputPage.setVisible(true);
                tabbedPane.setSelectedComponent(inputPage); //select the newly added tab
                this.pack();
                //NOTE: still needed
                inputPage.setBasePG(basePG);
                return inputPage;
            }
            return null;
        }
        else {
            printMessage("ERROR: Null basePG in addPanel");
            System.out.println("ERROR: Null basePG in addPanel");
            return null;
        }
    }
    
    /** Sets position */
    public void setPosition(int width, int height, JFrame frame) {
        Dimension frameSize = frame.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
    }
    
    public String setAsString(Set set) {
        String message = new String(NEWLINE);
        if(set.iterator().hasNext()) {
            for (Iterator i = set.iterator(); i.hasNext(); ) {
                String cur = (String)i.next();
                message += cur + " ";
            }
        }
        else {
            message += NONE;
        }
        return message;
    }
    
    public void showErrorDialog(String message, Component component) {
        JOptionPane.showMessageDialog(component, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }
    
    public void showInfoDialog(String message, Component component) {
        JOptionPane.showMessageDialog(component, message, "For your information", JOptionPane.PLAIN_MESSAGE);
    }
    
    public void showMessageDialog(String message, Component component) {
        JOptionPane.showMessageDialog(component, message, "Message to user", JOptionPane.PLAIN_MESSAGE);
    }
    
    /**return true if OK tp proceed */
    public boolean showConfirmDialog(String message, Component component) {
        int returnValue = JOptionPane.showConfirmDialog(component, message, "Please confirm", JOptionPane.OK_CANCEL_OPTION);
        if(returnValue == JOptionPane.OK_OPTION) {
            return true;
        }
        return false;
    }
    
    /**Finds the basePG of a selected leaf (designEntity) and refreshes this PG's content */
    public void refreshContentofSelectedNode() {
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup basePG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        parentNode.getContentSearcherTree().localCache_To_csAdvAllTypes();
    }
    
    public void refreshTreeWithNodeSelected() {
        PeerGroupsTreePage treePage = getSelectedPGTreePage();
        if(treePage.pgNodeSelected()) {
            PGTreeNode selNode = treePage.getSelectedPGTreeNode();
            /** now redraw the tree showing added content */
            selNode.addChildrenAndExpand();
        }
    }
    
    public void refreshTreeWithLeafSelected() {
        PeerGroupsTreePage treePage = getSelectedPGTreePage();
        if(treePage.pgLeafSelected()) {
            PGTreeLeaf selLeaf = treePage.getSelectedPGTreeLeaf();
            PGTreeNode parentNode = treePage.getParentNodeOfSelectedLeaf(selLeaf);
            parentNode.addChildrenAndExpand();
        }
    }
    
    /**@since 14 Oct. 2004 */
    public void test(String testName, Object actual, Object expected) {
        if(actual.equals(expected)) {
            printPassMessage(testName);
            return;
        }
        printFailMessage(testName, actual, expected);
    }
    public void test(String testName, int actual, int expected) {
        test(testName, new Integer(actual), new Integer(expected));
    }
    
    public void printPassMessage(String testName) {
        System.out.println("PASSED: " + testName);
    }
    public void printFailMessage(String testName, Object actual, Object expected) {
        System.out.println("FAILED: " + testName + " Actual:" + actual + " Expected:" + expected);
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
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        showButtonGroup = new javax.swing.ButtonGroup();
        tabbedPane = new javax.swing.JTabbedPane();
        messageScrollPane = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newUserNamedEntityMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        newPeerGroupWithoutAdvMenuItem = new javax.swing.JMenuItem();
        postChatMessageMenuItem = new javax.swing.JMenuItem();
        postPrivateChatMessageMenuItem = new javax.swing.JMenuItem();
        newPGUsingAdvMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        newRoleAdvMenuItem = new javax.swing.JMenuItem();
        newPolicyAdvMenuItem = new javax.swing.JMenuItem();
        newInputAdvMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        newSubEntityChildOnlyMenuItem = new javax.swing.JMenuItem();
        newSequentialEntityLinkChildOnlyMenuItem = new javax.swing.JMenuItem();
        newSubEntityParentChildMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        newConstraintLinkMenuItem = new javax.swing.JMenuItem();
        newInfoLinkMenuItem = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JSeparator();
        exitDpmMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        deleteEntityMenuItem = new javax.swing.JMenuItem();
        deleteNetMenuItem = new javax.swing.JMenuItem();
        deleteLinkMenuItem = new javax.swing.JMenuItem();
        deleteMessageMenuItem = new javax.swing.JMenuItem();
        deletePrivateMessageMenuItem = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JSeparator();
        deletePeerGroupMenuItem = new javax.swing.JMenuItem();
        deleteAllAdvsInPG = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JSeparator();
        deletePoliciesMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        resignOneRoleMenuItem = new javax.swing.JMenuItem();
        resignAllRolesMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        abandonEntityMenuItem = new javax.swing.JMenuItem();
        abandonEntityAndIterateMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        expandOneNodeMenuItem = new javax.swing.JMenuItem();
        expandTreeMenuItem = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JSeparator();
        collapseTreeMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        showAllRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        showMenu = new javax.swing.JMenu();
        showEntitiesRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        showLinksRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        showPgsRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        showPeersRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        showNetsRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        showMessagesRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        peerGroupsMenu = new javax.swing.JMenu();
        joinPGMenuItem = new javax.swing.JMenuItem();
        leavePGMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        broadcastPGContentsMenuItem = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JSeparator();
        searchForLocalAdvsMenuItem = new javax.swing.JMenuItem();
        searchForRemoteAdvsMenuItem = new javax.swing.JMenuItem();
        stopSearcherMenuItem = new javax.swing.JMenuItem();
        entitiesMenu = new javax.swing.JMenu();
        showStateChangeInfoMenuItem = new javax.swing.JMenuItem();
        showAllEntityContentMenuitem = new javax.swing.JMenuItem();
        showInputPolicyMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        viewLinksMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        viewEntityHistoryMenuItem = new javax.swing.JMenuItem();
        viewInputsOutputsMenuItem = new javax.swing.JMenuItem();
        petriNetsMenu = new javax.swing.JMenu();
        notesToUserRePNetsMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        openPNetofSelectedNetAdvMenuItem = new javax.swing.JMenuItem();
        saveDrawingAsNetAdvMenuItem = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        openRenewMenuItem = new javax.swing.JMenuItem();
        exitRenewMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutDpmMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        numThreadsMenuItem = new javax.swing.JMenuItem();
        nodeExpandedMenuItem = new javax.swing.JMenuItem();
        getDisplayStateOfNodeMenuItem = new javax.swing.JMenuItem();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        setName("DpmAppTopFrame");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        tabbedPane.setFont(new java.awt.Font("Arial", 1, 11));
        tabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabbedPaneMouseClicked(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(tabbedPane, gridBagConstraints);

        messageScrollPane.setFont(new java.awt.Font("Arial", 0, 12));
        messageScrollPane.setMinimumSize(new java.awt.Dimension(15, 75));
        messageScrollPane.setName("messageScrollPane");
        messageScrollPane.setPreferredSize(new java.awt.Dimension(15, 75));
        messageScrollPane.setAutoscrolls(true);
        messageArea.setBackground(new java.awt.Color(204, 255, 204));
        messageArea.setEditable(false);
        messageArea.setFont(new java.awt.Font("Arial", 0, 12));
        messageArea.setLineWrap(true);
        messageArea.setToolTipText("Message Area");
        messageScrollPane.setViewportView(messageArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(messageScrollPane, gridBagConstraints);

        menuBar.setToolTipText("");
        menuBar.setFont(new java.awt.Font("Arial", 1, 12));
        fileMenu.setText("File");
        fileMenu.setFont(new java.awt.Font("Arial", 0, 12));
        newUserNamedEntityMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newUserNamedEntityMenuItem.setText("New User Named Entity");
        newUserNamedEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newUserNamedEntityMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newUserNamedEntityMenuItem);

        fileMenu.add(jSeparator15);

        newPeerGroupWithoutAdvMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newPeerGroupWithoutAdvMenuItem.setText("New PeerGroup");
        newPeerGroupWithoutAdvMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPeerGroupWithoutAdvMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newPeerGroupWithoutAdvMenuItem);

        postChatMessageMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        postChatMessageMenuItem.setText("Post Message to Peergroup");
        postChatMessageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postChatMessageMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(postChatMessageMenuItem);

        postPrivateChatMessageMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        postPrivateChatMessageMenuItem.setText("Post Private Message to one Peer");
        postPrivateChatMessageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                postPrivateChatMessageMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(postPrivateChatMessageMenuItem);

        newPGUsingAdvMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newPGUsingAdvMenuItem.setText("Instantiate Existing PeerGroup");
        newPGUsingAdvMenuItem.setEnabled(false);
        newPGUsingAdvMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPGUsingAdvMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newPGUsingAdvMenuItem);

        fileMenu.add(jSeparator1);

        newRoleAdvMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newRoleAdvMenuItem.setText("New Role (sign up for a role)");
        newRoleAdvMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newRoleAdvMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newRoleAdvMenuItem);

        newPolicyAdvMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newPolicyAdvMenuItem.setText("New Policy (add a role-based constraint)");
        newPolicyAdvMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newPolicyAdvMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newPolicyAdvMenuItem);

        newInputAdvMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newInputAdvMenuItem.setText("New Input (make an input)");
        newInputAdvMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newInputAdvMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newInputAdvMenuItem);

        fileMenu.add(jSeparator11);

        newSubEntityChildOnlyMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newSubEntityChildOnlyMenuItem.setLabel("New SubEntity Link (start Selected > complete subE > return to Selected)");
        newSubEntityChildOnlyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSubEntityChildOnlyMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newSubEntityChildOnlyMenuItem);

        newSequentialEntityLinkChildOnlyMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newSequentialEntityLinkChildOnlyMenuItem.setLabel("New Sequential Link (complete Selected > start X)");
        newSequentialEntityLinkChildOnlyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSequentialEntityLinkChildOnlyMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newSequentialEntityLinkChildOnlyMenuItem);

        newSubEntityParentChildMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newSubEntityParentChildMenuItem.setText("New SubEntity Link - Select Peergroup");
        newSubEntityParentChildMenuItem.setEnabled(false);
        newSubEntityParentChildMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newSubEntityParentChildMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newSubEntityParentChildMenuItem);

        fileMenu.add(jSeparator3);

        newConstraintLinkMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newConstraintLinkMenuItem.setText("New Custom Constraint Link");
        newConstraintLinkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newConstraintLinkMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newConstraintLinkMenuItem);

        newInfoLinkMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        newInfoLinkMenuItem.setText("New Information Link");
        newInfoLinkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newInfoLinkMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(newInfoLinkMenuItem);

        fileMenu.add(jSeparator18);

        exitDpmMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        exitDpmMenuItem.setLabel("Exit");
        exitDpmMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitDpmMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitDpmMenuItem);

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        editMenu.setFont(new java.awt.Font("Arial", 0, 12));
        deleteEntityMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        deleteEntityMenuItem.setText("Delete Selected Entity");
        deleteEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEntityMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(deleteEntityMenuItem);

        deleteNetMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        deleteNetMenuItem.setText("Delete Selected Loop Net");
        deleteNetMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNetMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(deleteNetMenuItem);

        deleteLinkMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        deleteLinkMenuItem.setText("Delete Selected Link");
        deleteLinkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLinkMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(deleteLinkMenuItem);

        deleteMessageMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        deleteMessageMenuItem.setText("Delete Selected Message");
        deleteMessageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMessageMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(deleteMessageMenuItem);

        deletePrivateMessageMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        deletePrivateMessageMenuItem.setText("Delete Selected Private Message");
        deletePrivateMessageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePrivateMessageMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(deletePrivateMessageMenuItem);

        editMenu.add(jSeparator20);

        deletePeerGroupMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        deletePeerGroupMenuItem.setText("Delete Selected PeerGroup");
        deletePeerGroupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePeerGroupMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(deletePeerGroupMenuItem);

        deleteAllAdvsInPG.setFont(new java.awt.Font("Arial", 0, 12));
        deleteAllAdvsInPG.setText("Delete ALL Advertisements from Peergroup");
        deleteAllAdvsInPG.setEnabled(false);
        deleteAllAdvsInPG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllAdvsInPGActionPerformed(evt);
            }
        });

        editMenu.add(deleteAllAdvsInPG);

        editMenu.add(jSeparator17);

        deletePoliciesMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        deletePoliciesMenuItem.setText("Delete Policy Constraints for Entity");
        deletePoliciesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePoliciesMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(deletePoliciesMenuItem);

        editMenu.add(jSeparator6);

        resignOneRoleMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        resignOneRoleMenuItem.setText("Resign from one Role in Entity");
        resignOneRoleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resignOneRoleMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(resignOneRoleMenuItem);

        resignAllRolesMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        resignAllRolesMenuItem.setText("Resign from all Roles in Entity");
        resignAllRolesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resignAllRolesMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(resignAllRolesMenuItem);

        editMenu.add(jSeparator10);

        abandonEntityMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        abandonEntityMenuItem.setText("Abandon Entity");
        abandonEntityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abandonEntityMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(abandonEntityMenuItem);

        abandonEntityAndIterateMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        abandonEntityAndIterateMenuItem.setText("Abandon Entity and Iterate");
        abandonEntityAndIterateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abandonEntityAndIterateMenuItemActionPerformed(evt);
            }
        });

        editMenu.add(abandonEntityAndIterateMenuItem);

        menuBar.add(editMenu);

        viewMenu.setText("View");
        viewMenu.setFont(new java.awt.Font("Arial", 0, 12));
        expandOneNodeMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        expandOneNodeMenuItem.setText("Expand One Node");
        expandOneNodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandOneNodeMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(expandOneNodeMenuItem);

        expandTreeMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        expandTreeMenuItem.setLabel("Expand Tree");
        expandTreeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                expandTreeMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(expandTreeMenuItem);

        viewMenu.add(jSeparator19);

        collapseTreeMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        collapseTreeMenuItem.setLabel("Collapse Tree");
        collapseTreeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                collapseTreeMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(collapseTreeMenuItem);

        viewMenu.add(jSeparator5);

        showAllRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showAllRadioButtonMenuItem.setSelected(true);
        showAllRadioButtonMenuItem.setText("Show Everything");
        showButtonGroup.add(showAllRadioButtonMenuItem);
        showAllRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllRadioButtonMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(showAllRadioButtonMenuItem);

        showMenu.setText("Show only");
        showMenu.setFont(new java.awt.Font("Arial", 0, 12));
        showEntitiesRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showEntitiesRadioButtonMenuItem.setText("Entities");
        showButtonGroup.add(showEntitiesRadioButtonMenuItem);
        showEntitiesRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showEntitiesRadioButtonMenuItemActionPerformed(evt);
            }
        });

        showMenu.add(showEntitiesRadioButtonMenuItem);

        showLinksRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showLinksRadioButtonMenuItem.setText("Links");
        showButtonGroup.add(showLinksRadioButtonMenuItem);
        showLinksRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLinksRadioButtonMenuItemActionPerformed(evt);
            }
        });

        showMenu.add(showLinksRadioButtonMenuItem);

        showMenu.add(jSeparator14);

        showPgsRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showPgsRadioButtonMenuItem.setText("PeerGroups");
        showButtonGroup.add(showPgsRadioButtonMenuItem);
        showPgsRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPgsRadioButtonMenuItemActionPerformed(evt);
            }
        });

        showMenu.add(showPgsRadioButtonMenuItem);

        showPeersRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showPeersRadioButtonMenuItem.setText("Peers");
        showButtonGroup.add(showPeersRadioButtonMenuItem);
        showPeersRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPeersRadioButtonMenuItemActionPerformed(evt);
            }
        });

        showMenu.add(showPeersRadioButtonMenuItem);

        showNetsRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showNetsRadioButtonMenuItem.setText("Loops");
        showButtonGroup.add(showNetsRadioButtonMenuItem);
        showNetsRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNetsRadioButtonMenuItemActionPerformed(evt);
            }
        });

        showMenu.add(showNetsRadioButtonMenuItem);

        showMessagesRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showMessagesRadioButtonMenuItem.setText("Chat Messages");
        showButtonGroup.add(showMessagesRadioButtonMenuItem);
        showMessagesRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showMessagesRadioButtonMenuItemActionPerformed(evt);
            }
        });

        showMenu.add(showMessagesRadioButtonMenuItem);

        viewMenu.add(showMenu);

        viewMenu.add(jSeparator13);

        menuBar.add(viewMenu);

        peerGroupsMenu.setText("PeerGroups");
        peerGroupsMenu.setFont(new java.awt.Font("Arial", 0, 12));
        joinPGMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        joinPGMenuItem.setText("Join PeerGroup");
        joinPGMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinPGMenuItemActionPerformed(evt);
            }
        });

        peerGroupsMenu.add(joinPGMenuItem);

        leavePGMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        leavePGMenuItem.setText("Leave PeerGroup");
        leavePGMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leavePGMenuItemActionPerformed(evt);
            }
        });

        peerGroupsMenu.add(leavePGMenuItem);

        peerGroupsMenu.add(jSeparator4);

        broadcastPGContentsMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        broadcastPGContentsMenuItem.setText("Broadcast Contents of PeerGroup");
        broadcastPGContentsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                broadcastPGContentsMenuItemActionPerformed(evt);
            }
        });

        peerGroupsMenu.add(broadcastPGContentsMenuItem);

        peerGroupsMenu.add(jSeparator16);

        searchForLocalAdvsMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        searchForLocalAdvsMenuItem.setText("Show Local Resources of Selected PeerGroup");
        searchForLocalAdvsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchForLocalAdvsMenuItemActionPerformed(evt);
            }
        });

        peerGroupsMenu.add(searchForLocalAdvsMenuItem);

        searchForRemoteAdvsMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        searchForRemoteAdvsMenuItem.setText("Search for Remote Resources in PeerGroup (Start Searcher)");
        searchForRemoteAdvsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchForRemoteAdvsMenuItemActionPerformed(evt);
            }
        });

        peerGroupsMenu.add(searchForRemoteAdvsMenuItem);

        stopSearcherMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        stopSearcherMenuItem.setText("Stop Searcher");
        stopSearcherMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopSearcherMenuItemActionPerformed(evt);
            }
        });

        peerGroupsMenu.add(stopSearcherMenuItem);

        menuBar.add(peerGroupsMenu);

        entitiesMenu.setText("Entities");
        entitiesMenu.setFont(new java.awt.Font("Arial", 0, 12));
        showStateChangeInfoMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showStateChangeInfoMenuItem.setText("Show Current State-Change Information");
        showStateChangeInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showStateChangeInfoMenuItemActionPerformed(evt);
            }
        });

        entitiesMenu.add(showStateChangeInfoMenuItem);

        showAllEntityContentMenuitem.setFont(new java.awt.Font("Arial", 0, 12));
        showAllEntityContentMenuitem.setText("Show All Discovered Content for Selected Entity");
        showAllEntityContentMenuitem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllEntityContentMenuitemActionPerformed(evt);
            }
        });

        entitiesMenu.add(showAllEntityContentMenuitem);

        showInputPolicyMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        showInputPolicyMenuItem.setText("Show Input Policy (All Transitions)");
        showInputPolicyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showInputPolicyMenuItemActionPerformed(evt);
            }
        });

        entitiesMenu.add(showInputPolicyMenuItem);

        entitiesMenu.add(jSeparator9);

        viewLinksMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        viewLinksMenuItem.setText("Show Links to/from Entity");
        viewLinksMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewLinksMenuItemActionPerformed(evt);
            }
        });

        entitiesMenu.add(viewLinksMenuItem);

        entitiesMenu.add(jSeparator8);

        viewEntityHistoryMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        viewEntityHistoryMenuItem.setText("Show State Change History of Entity");
        viewEntityHistoryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewEntityHistoryMenuItemActionPerformed(evt);
            }
        });

        entitiesMenu.add(viewEntityHistoryMenuItem);

        viewInputsOutputsMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        viewInputsOutputsMenuItem.setText("Launch Input/Output Viewer for Entity");
        viewInputsOutputsMenuItem.setEnabled(false);
        viewInputsOutputsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewInputsOutputsMenuItemActionPerformed(evt);
            }
        });

        entitiesMenu.add(viewInputsOutputsMenuItem);

        menuBar.add(entitiesMenu);

        petriNetsMenu.setText("PetriNets");
        petriNetsMenu.setFont(new java.awt.Font("Arial", 0, 12));
        notesToUserRePNetsMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        notesToUserRePNetsMenuItem.setText("About PetriNet Requirements");
        notesToUserRePNetsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notesToUserRePNetsMenuItemActionPerformed(evt);
            }
        });

        petriNetsMenu.add(notesToUserRePNetsMenuItem);

        petriNetsMenu.add(jSeparator7);

        openPNetofSelectedNetAdvMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        openPNetofSelectedNetAdvMenuItem.setText("Show PetriNet of Selected NetAdvertisement ");
        openPNetofSelectedNetAdvMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPNetofSelectedNetAdvMenuItemActionPerformed(evt);
            }
        });

        petriNetsMenu.add(openPNetofSelectedNetAdvMenuItem);

        saveDrawingAsNetAdvMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        saveDrawingAsNetAdvMenuItem.setText("Save Current PetriNet as a NetAdvertisement");
        saveDrawingAsNetAdvMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveDrawingAsNetAdvMenuItemActionPerformed(evt);
            }
        });

        petriNetsMenu.add(saveDrawingAsNetAdvMenuItem);

        petriNetsMenu.add(jSeparator12);

        openRenewMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        openRenewMenuItem.setText("Open Renew PetriNet Application");
        openRenewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openRenewMenuItemActionPerformed(evt);
            }
        });

        petriNetsMenu.add(openRenewMenuItem);

        exitRenewMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        exitRenewMenuItem.setText("Exit Renew");
        exitRenewMenuItem.setEnabled(false);
        exitRenewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitRenewMenuItemActionPerformed(evt);
            }
        });

        petriNetsMenu.add(exitRenewMenuItem);

        menuBar.add(petriNetsMenu);

        helpMenu.setText("Info");
        helpMenu.setFont(new java.awt.Font("Arial", 0, 12));
        aboutDpmMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        aboutDpmMenuItem.setText("About this Application");
        aboutDpmMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutDpmMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutDpmMenuItem);

        helpMenu.add(jSeparator2);

        numThreadsMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        numThreadsMenuItem.setText("Number of active threads");
        numThreadsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numThreadsMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(numThreadsMenuItem);

        nodeExpandedMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        nodeExpandedMenuItem.setText("Node Expanded?");
        nodeExpandedMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nodeExpandedMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(nodeExpandedMenuItem);

        getDisplayStateOfNodeMenuItem.setFont(new java.awt.Font("Arial", 0, 12));
        getDisplayStateOfNodeMenuItem.setText("Display State of Selected Entity Leaf");
        getDisplayStateOfNodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getDisplayStateOfNodeMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(getDisplayStateOfNodeMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }//GEN-END:initComponents
    
    private void getDisplayStateOfNodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getDisplayStateOfNodeMenuItemActionPerformed
        // TODO add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        System.out.println("Display state of entity leaf: " + selLeaf.getDisplayState());
    }//GEN-LAST:event_getDisplayStateOfNodeMenuItemActionPerformed
    
    private void expandOneNodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandOneNodeMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        selNode.addChildrenAndExpand();
    }//GEN-LAST:event_expandOneNodeMenuItemActionPerformed
    
    private void nodeExpandedMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nodeExpandedMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        System.out.println(">>" +
        selNode.getPeerGroup().getPeerGroupName() + "'s node expanded = " + selNode.isExpanded());
        //this also works:
        //        System.out.println(">>" +
        //        selNode.getPeerGroup().getPeerGroupName() + "'s node expanded = " +
        //        selNode.getTreeSearcher().getBaseNode().isExpanded());
    }//GEN-LAST:event_nodeExpandedMenuItemActionPerformed
    
    private void numThreadsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numThreadsMenuItemActionPerformed
        // Add your handling code here:
        printMessage("Number of active remote searching threads: "  + String.valueOf(getNumThreads()));
    }//GEN-LAST:event_numThreadsMenuItemActionPerformed
    
    private void broadcastPGContentsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_broadcastPGContentsMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        selNode.getContentSearcherTree().broadcaseLocalCacheAllTypes();
    }//GEN-LAST:event_broadcastPGContentsMenuItemActionPerformed
    
    private void leavePGMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leavePGMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PeerGroup pg = selNode.getPeerGroup();
        Set advsToDelete = advUtils.retrieveExistingMemberAdvs(appUser, pg);
        if(advsToDelete == null || advsToDelete.isEmpty()) {
            showErrorDialog("You are not currently a member of this peergroup", this);
            return;
        }
        System.out.println("Num of memberAdvs found: " + advsToDelete.size());
        if(showConfirmDialog("OK for " + appUser.getName() + " to leave " +
        pg.getPeerGroupName() + "?", this) == true) {
            /**Create delete advs for these memberAdvs */
            appUser.leavePeerGroup(advsToDelete, pg);
            refreshTreeWithNodeSelected();
        }
    }//GEN-LAST:event_leavePGMenuItemActionPerformed
    
    private void expandTreeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expandTreeMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PGTree tree = (PGTree)selTreePage.getTree();
        tree.expandAll();
    }//GEN-LAST:event_expandTreeMenuItemActionPerformed
    
    private void collapseTreeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_collapseTreeMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PGTree tree = (PGTree)selTreePage.getTree();
        tree.collapseAll();
    }//GEN-LAST:event_collapseTreeMenuItemActionPerformed
    
    private void deletePoliciesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePoliciesMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup parentPG = parentNode.getPeerGroup();
        
        DeletePoliciesForm form = new DeletePoliciesForm(selEntity, parentPG, this);
    }//GEN-LAST:event_deletePoliciesMenuItemActionPerformed
    
    private void deletePrivateMessageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePrivateMessageMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a Private Message", this);
            return;
        }
        Object objToDelete = selLeaf.getObj();
        if(!(objToDelete instanceof PrivateChatAdvertisement)) {
            showErrorDialog("Please select a Private Message", this);
            return;
        }
        PrivateChatAdvertisement advToDelete = (PrivateChatAdvertisement)objToDelete;
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup parentPG = parentNode.getPeerGroup();
        
        if(showConfirmDialog("OK to delete selected private Message by " + advToDelete.getAuthorName() + "?", this) == true) {
            if(advToDelete != null && parentPG != null) {
                advUtils.createDeleteAdvertisement(advToDelete, parentPG);
                /**ChatAdvs are handled below */
                advUtils.flushAdvFromCS(advToDelete);
                /**Refresh the tree */
                parentNode.addChildrenAndExpand();
            }
        }
    }//GEN-LAST:event_deletePrivateMessageMenuItemActionPerformed
    
    private void postPrivateChatMessageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postPrivateChatMessageMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a Peer", this);
            return;
        }
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup basePG = parentNode.getPeerGroup();
        
        Object obj = selLeaf.getObj();
        if(!(obj instanceof IPGMemberAdvertisement)) {
            showErrorDialog("Please select a Peer", this);
            return;
        }
        IPGMemberAdvertisement memberAdv = (IPGMemberAdvertisement)obj;
        NewPrivateChatMessageForm form = new NewPrivateChatMessageForm(memberAdv, basePG, this);
    }//GEN-LAST:event_postPrivateChatMessageMenuItemActionPerformed
    
    private void showStateChangeInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showStateChangeInfoMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selEntity = getSelectedDesignEntityFromTreePage();
        if(selEntity == null) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        String nextTrans = selEntity.getNextTransition();
        String abandonS = selEntity.getAbandonedString();
        
        String title = "Selected entity: " + selEntity.getFullName();
        String header = abandonS +
        "State change information for entity: " + selEntity.getFullName();
        
        String content =
        "Inputs received (from peers who have assumed these roles): " + NEWLINE +
        selEntity.getInputsReceivedStringAnyPeer(nextTrans) + NEWLINE +
        DOTTED_LINE + NEWLINE +
        "Inputs needed (from any peer who assume these roles): " + NEWLINE +
        selEntity.getInputsNeededStringAnyPeer(nextTrans) + NEWLINE +
        DOTTED_LINE + NEWLINE +
        "State-change constraints exist for entity's next transition: " + !selEntity.noCurrentConstraints() + NEWLINE +
        "Inputs allow entity's current state to change: " + selEntity.stateCanChangeInputs() + NEWLINE +
        "Linked-in entities allow entity's current state to change: " + selEntity.stateCanChangeLinks() + NEWLINE +
        DOTTED_LINE + NEWLINE +
        "Incomplete linked-in entities (in general): " + NEWLINE +
        appUser.getMembers().stringSet2String(selEntity.getAllIncompleteNamesSet()) + NEWLINE +
        DOTTED_LINE + NEWLINE +
        "Incomplete linked-in entities (relevant to current state): " + NEWLINE +
        /**Note: getMembers used simply to get to ContentStorage */
        appUser.getMembers().stringSet2String(selEntity.getRelevantIncompleteNamesSet());
        
        
        SimpleInformationForm form = new SimpleInformationForm(this);
        form.setSimpleMessage(title, header, content);
    }//GEN-LAST:event_showStateChangeInfoMenuItemActionPerformed
    
    private void abandonEntityAndIterateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abandonEntityAndIterateMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        if(showConfirmDialog("OK to abandon entity " + selEntity.getFullName() + ", then Iterate it?", this) == true) {
            PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selTreePage.getSelectedPGTreeLeaf());
            /**First, record existing policies, before abandonment */
            //ContentStorage existTransRoles = selEntity.getTransRoles();
            advUtils.abandonEntity(selEntity, parentPG);
            try {
                advUtils.createIteratedEntityAdvertisement(selEntity, parentPG);
                /**Refresh the tree */
                parentNode.addChildrenAndExpand();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_abandonEntityAndIterateMenuItemActionPerformed
    
    private void abandonEntityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abandonEntityMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        if(showConfirmDialog("OK to abandon entity " + selEntity.getFullName() + "?", this) == true) {
            PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selTreePage.getSelectedPGTreeLeaf());
            advUtils.abandonEntity(selEntity, parentPG);
            /**Refresh the tree */
            parentNode.addChildrenAndExpand();
        }
    }//GEN-LAST:event_abandonEntityMenuItemActionPerformed
    
    private void resignAllRolesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resignAllRolesMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Delete All Roles' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        if(showConfirmDialog("OK to delete all roles you have assumed for entity " + NEWLINE +
        selEntity.getFullName() + "?", this) == true) {
            advUtils.deleteAllRoleAdvs(selEntity, parentPG);
        }
    }//GEN-LAST:event_resignAllRolesMenuItemActionPerformed
    
    private void resignOneRoleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resignOneRoleMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Delete One Role' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        //PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        
        ResignRoleForm form = new ResignRoleForm(selEntity, parentPG, this);
    }//GEN-LAST:event_resignOneRoleMenuItemActionPerformed
    
    private void newSubEntityChildOnlyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSubEntityChildOnlyMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a design entity to serve as parent", this);
            return;
        }
        PeerGroup basePG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        
        /**User needs to select a designEntity */
        DesignEntity parentEntity = selTreePage.getSelectedDesignEntity();
        if(parentEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        /**Note: form is initialized with a selected design entity */
        NewSubEntityChildOnlyForm form = new NewSubEntityChildOnlyForm(parentEntity, basePG, this);
    }//GEN-LAST:event_newSubEntityChildOnlyMenuItemActionPerformed
    
    private void deleteMessageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMessageMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a Message", this);
            return;
        }
        Object objToDelete = selLeaf.getObj();
        if(!(objToDelete instanceof ChatAdvertisement)) {
            showErrorDialog("Please select a Message", this);
            return;
        }
        ChatAdvertisement advToDelete = (ChatAdvertisement)objToDelete;
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup parentPG = parentNode.getPeerGroup();
        
        if(showConfirmDialog("OK to delete selected Message by " + advToDelete.getAuthorName() + "?", this) == true) {
            if(advToDelete != null && parentPG != null) {
                advUtils.createDeleteAdvertisement(advToDelete, parentPG);
                /**ChatAdvs are handled below */
                advUtils.flushAdvFromCS(advToDelete);
                /**Refresh the tree */
                parentNode.addChildrenAndExpand();
            }
        }
    }//GEN-LAST:event_deleteMessageMenuItemActionPerformed
    
    private void showMessagesRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showMessagesRadioButtonMenuItemActionPerformed
        // Add your handling code here:
        setShow(MESSAGES);
    }//GEN-LAST:event_showMessagesRadioButtonMenuItemActionPerformed
    
    private void postChatMessageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_postChatMessageMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PeerGroup basePG = selNode.getPeerGroup();
        NewChatMessageForm form = new NewChatMessageForm(basePG, this);
    }//GEN-LAST:event_postChatMessageMenuItemActionPerformed
    
    private void deleteAllAdvsInPGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAllAdvsInPGActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PeerGroup basePG = selNode.getPeerGroup();
        if(showConfirmDialog(
        "OK to delete ALL Advertisements from Peergroup: " +
        basePG.getPeerGroupName() + "?", this) == true) {
            DiscoveryService discSvc = basePG.getDiscoveryService();
            /**Taken from: http://wiki.java.net/bin/view/Jxta/FlushingAdvertisements */
            try {
                /**Retrieves all advs except: PeerAdvs, PGAdvs, or IPGAdvs */
                Enumeration en = discSvc.getLocalAdvertisements(DiscoveryService.ADV, null, null);
                while (en.hasMoreElements()) {
                    Advertisement advToDelete = (Advertisement)en.nextElement();
                    /**IPG advertisements are required for DPM peergroups */
                    /**COMPLETE? by only deleting DPM created advs? */
                    if(!(advToDelete instanceof IPGAdvertisement)) {
                        /**this method also flushes the adv */
                        advUtils.createDeleteAdvertisement(advToDelete, basePG);
                    }
                }
                /**Refresh the selected node */
                selNode.addChildrenAndExpand();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }//GEN-LAST:event_deleteAllAdvsInPGActionPerformed
    
    private void newSequentialEntityLinkChildOnlyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSequentialEntityLinkChildOnlyMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PeerGroup basePG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        
        /**User needs to select a designEntity */
        DesignEntity parentEntity = selTreePage.getSelectedDesignEntity();
        if(parentEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        /**Note: form is initialized with a selected design entity */
        NewSequentialLinkChildOnlyForm form = new NewSequentialLinkChildOnlyForm(parentEntity, basePG, this);
    }//GEN-LAST:event_newSequentialEntityLinkChildOnlyMenuItemActionPerformed
    
    private void newSubEntityParentChildMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newSubEntityParentChildMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PeerGroup basePG = selNode.getPeerGroup();
        /**Note: form is not initialized with a selected design entity */
        NewSubEntityParentChildForm form = new NewSubEntityParentChildForm(basePG, this);
    }//GEN-LAST:event_newSubEntityParentChildMenuItemActionPerformed
    
    private void deleteNetMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNetMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a LoopNet", this);
            return;
        }
        Object objToDelete = selLeaf.getObj();
        if(!(objToDelete instanceof LoopNetAdvertisement)) {
            showErrorDialog("Please select a LoopNet", this);
            return;
        }
        LoopNetAdvertisement advToDelete = (LoopNetAdvertisement)objToDelete;
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup parentPG = parentNode.getPeerGroup();
        
        if(showConfirmDialog("OK to delete selected LoopNet " + advToDelete.getNetName() + "?", this) == true) {
            if(advToDelete != null && parentPG != null) {
                advUtils.createDeleteAdvertisement(advToDelete, parentPG);
                /**LoopNetAdvs are handled below */
                advUtils.flushAdvFromCS(advToDelete);
                /**Refresh the tree */
                parentNode.addChildrenAndExpand();
            }
        }
    }//GEN-LAST:event_deleteNetMenuItemActionPerformed
    
    private void deleteEntityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEntityMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        Object objToDelete = selLeaf.getObj();
        if(!(objToDelete instanceof UserNamedEntity)) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        UserNamedEntity entity = (UserNamedEntity)objToDelete;
        DesignEntityAdv advToDelete = entity.getAdv();
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup parentPG = parentNode.getPeerGroup();
        
        if(showConfirmDialog(
        "OK to delete selected " + entity.getEntityType() + " " + entity.getFullName() + "?", this) == true) {
            if(advToDelete != null && parentPG != null) {
                advUtils.createDeleteAdvertisement(advToDelete, parentPG);
                advUtils.flushEntityFromCS(entity);
                /**Refresh the tree */
                parentNode.addChildrenAndExpand();
            }
        }
    }//GEN-LAST:event_deleteEntityMenuItemActionPerformed
    
    private void newUserNamedEntityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newUserNamedEntityMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'New User Named Entity' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PeerGroup parentPG = selTreePage.getSelectedPGTreeNode().getPeerGroup();
        if(parentPG == null) {
            showErrorDialog("Please select a peergroup node", this);
            return;
        }
        /**parameters: PeerGroup, topFrame, initPrototype, initIteration */
        NewDesignEntityForm form = new NewDesignEntityForm(parentPG, this, null, null);
    }//GEN-LAST:event_newUserNamedEntityMenuItemActionPerformed
    
    private void deletePeerGroupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deletePeerGroupMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        /**User must select a PGTreeNode */
        if(selNode == null) {
            showErrorDialog("Please select a peerGroup node", this);
            return;
        }
        PGTreeNode parentNode = selNode.getParentNode();
        /**User cannot delete dpmNet peergroup (at root) */
        if(parentNode == null) {
            showErrorDialog("Cannot delete root PeerGroup", this);
            return;
        }
        PeerGroup selPG = selNode.getPeerGroup();
        /**Need to get the parentPG of the selected PG */
        PeerGroup parentPG = selNode.getParentPeerGroup();
        PeerGroupAdvertisement advToDelete = selPG.getPeerGroupAdvertisement();
        
        if(showConfirmDialog("OK to delete selected Peergroup " +
        selPG.getPeerGroupName() + "?", this) == true) {
            if(advToDelete != null && parentPG != null) {
                /**Note: must publish this new adv in parentPG, not PG being deleted */
                advUtils.createDeleteAdvertisement(advToDelete, parentPG);
                /**Refresh the parent node */
                parentNode.addChildrenAndExpand();
            }
        }
    }//GEN-LAST:event_deletePeerGroupMenuItemActionPerformed
    
    private void deleteLinkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLinkMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a link", this);
            return;
        }
        Advertisement advToDelete = selLeaf.getAdv();
        if(!(advToDelete instanceof LinkAdvertisement)) {
            showErrorDialog("Please select a link", this);
            return;
        }
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        
        if(showConfirmDialog("OK to delete selected Link?", this) == true) {
            if(advToDelete != null && parentPG != null) {
                advUtils.createDeleteAdvertisement(advToDelete, parentPG);
                String linkDesc = null;
                /**Refresh the tree */
                parentNode.addChildrenAndExpand();
            }
        }
    }//GEN-LAST:event_deleteLinkMenuItemActionPerformed
    /**See all links of the selectedEntity; shown as children in a tree */
    private void viewLinksMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewLinksMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PGTreeNode parentNode = selTreePage.getParentNodeOfSelectedLeaf(selLeaf);
        PeerGroup basePG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        /**User needs to select a designEntity */
        DesignEntity selDesignEntity = selTreePage.getSelectedDesignEntity();
        
        if(selDesignEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        /** parameters: (String name, DpmAppTopFrame topFrame, DesignEntity baseDe, boolean incoming, int width, int height) */
        LinkTreeForm linksForm = new LinkTreeForm(this, selDesignEntity, basePG);
    }//GEN-LAST:event_viewLinksMenuItemActionPerformed
    
    private void notesToUserRePNetsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notesToUserRePNetsMenuItemActionPerformed
        // Add your handling code here:
        SimpleInformationForm form = new SimpleInformationForm(this);
        form.addPetriNetMessage();
    }//GEN-LAST:event_notesToUserRePNetsMenuItemActionPerformed
    
    private void exitRenewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitRenewMenuItemActionPerformed
        // Add your handling code here:
        if(petriNetApp != null) {
            /**ERROR: Closes the DPM app too */
            petriNetApp.exit();
        }
    }//GEN-LAST:event_exitRenewMenuItemActionPerformed
    
    private void newInfoLinkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newInfoLinkMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PeerGroup basePG = selNode.getPeerGroup();
        /**Note: form is not initialized with a selected design entity */
        NewInfoLinkForm form = new NewInfoLinkForm(basePG, this); //false means the form doesn't handle doBefores
    }//GEN-LAST:event_newInfoLinkMenuItemActionPerformed
    
    private void showEntitiesRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showEntitiesRadioButtonMenuItemActionPerformed
        // Add your handling code here:
        setShow(ENTITIES);
    }//GEN-LAST:event_showEntitiesRadioButtonMenuItemActionPerformed
    
    private void showLinksRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLinksRadioButtonMenuItemActionPerformed
        // Add your handling code here:
        setShow(LINKS);
    }//GEN-LAST:event_showLinksRadioButtonMenuItemActionPerformed
    
    private void viewInputsOutputsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewInputsOutputsMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selDesignEntity = selTreePage.getSelectedDesignEntity();
        
        InputsOutputsForm form = new InputsOutputsForm(selDesignEntity, this);
    }//GEN-LAST:event_viewInputsOutputsMenuItemActionPerformed
    
    private void newConstraintLinkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newConstraintLinkMenuItemActionPerformed
        // Add your handling code here:
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a PeerGroup Node", this);
            return;
        }
        PeerGroup basePG = selNode.getPeerGroup();
        /**Note: form is not initialized with a selected design entity */
        NewConstraintLinkForm form = new NewConstraintLinkForm(basePG, this); //true means the form handles doBefores
    }//GEN-LAST:event_newConstraintLinkMenuItemActionPerformed
    
    private void newPolicyAdvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPolicyAdvMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'New DesignEntity Policy' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        
        NewPolicyForm form = new NewPolicyForm(selEntity, this, parentPG);
    }//GEN-LAST:event_newPolicyAdvMenuItemActionPerformed
    
    private void showNetsRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNetsRadioButtonMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Show Nets' radioButton */
        setShow(NETS);
        
    }//GEN-LAST:event_showNetsRadioButtonMenuItemActionPerformed
    
    private void openPNetofSelectedNetAdvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPNetofSelectedNetAdvMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Open PetriNet of Selected NetAdvertisement' */
        Demonstrator petriNetApp = getPetriNetApp();
        LoopNetAdvertisement loopAdv = getSelectedLoopNetAdvFromTreePage();
        if(loopAdv == null) {
            showErrorDialog("Please select a loop net", this);
            return;
        }
        String loopName = loopAdv.getNetName();
        URL url = this.getClass().getResource("/loops/" + loopName + ".rnw");
        /**See in: CH.ifa.draw.application.DrawApplication */
        if(url != null) {
            petriNetApp.loadAndOpenDrawing(url, loopName);
        }
        else {
            showErrorDialog("Couldn't find selected loop drawing", this);
        }
    }//GEN-LAST:event_openPNetofSelectedNetAdvMenuItemActionPerformed
    
    private void saveDrawingAsNetAdvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveDrawingAsNetAdvMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Save Current PetriNet as a NetAdvertisement' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        /**Assumes PGTreeNode is selected */
        PGTreeNode node = selTreePage.getSelectedPGTreeNode();
        
        if(node != null) {
            String netName = getFirstDrawing().getName();
            if (netName != null) {
                System.out.println("Making netAdv for net: " + netName);
            }
            else {
                System.out.println("Error: could not get drawing from petri net application");
                return;
            }
            PeerGroup parentPG = node.getPeerGroup();
            /**Assumes that Demostrator is open, and the first drawing is the appropriate one */
            LoopNetReader loopReader = new LoopNetReader(netName, this, parentPG, false);
            /**This creates the netAdv (using advUtils) and publishes it */
            loopReader.getLoopNetAdv();
        }
    }//GEN-LAST:event_saveDrawingAsNetAdvMenuItemActionPerformed
    
    private void viewEntityHistoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewEntityHistoryMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'View History of Selected Entity' */
        DesignEntity selEntity = getSelectedDesignEntityFromTreePage();
        if(selEntity == null) {
            showErrorDialog("Please select a design entity", this);
            return;
        }
        HistoryInformationForm viewer = new HistoryInformationForm(selEntity, this);
    }//GEN-LAST:event_viewEntityHistoryMenuItemActionPerformed
    
    private void openRenewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openRenewMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Open Renew PetriNet Application' */
        getPetriNetApp();
    }//GEN-LAST:event_openRenewMenuItemActionPerformed
    
    private void showInputPolicyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showInputPolicyMenuItemActionPerformed
        // Add your handling code here:
        DesignEntity selEntity = getSelectedDesignEntityFromTreePage();
        if(selEntity == null) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        String nextTrans = selEntity.getNextTransition();
        String abandonS = selEntity.getAbandonedString();
        
        String title = "Selected entity: " + selEntity.getFullName();
        String header = abandonS +
        "Input policies for all transitions of entity: " + selEntity.getFullName();
        String content = selEntity.getAllInputsNeededStringAnyPeer();
        
        SimpleInformationForm form = new SimpleInformationForm(this);
        form.setSimpleMessage(title, header, content);
    }//GEN-LAST:event_showInputPolicyMenuItemActionPerformed
    
    private void showAllEntityContentMenuitemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllEntityContentMenuitemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Print all Content of Selected DesignEntity' */
        DesignEntity selEntity = getSelectedDesignEntityFromTreePage();
        if(selEntity == null) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        String abandonS = selEntity.getAbandonedString();
        String title = "Selected entity: " + selEntity.getFullName();
        String header = abandonS +
        "All discovered content for entity: " + selEntity.getFullName();
        String content = selEntity.getAllEntityContent();
        
        SimpleInformationForm form = new SimpleInformationForm(this);
        form.setSimpleMessage(title, header, content);
    }//GEN-LAST:event_showAllEntityContentMenuitemActionPerformed
    
    private void newInputAdvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newInputAdvMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'New DesignEntity Input' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        
        NewInputForm form = new NewInputForm(selEntity, parentPG, this);
    }//GEN-LAST:event_newInputAdvMenuItemActionPerformed
    
    private void newRoleAdvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newRoleAdvMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'New DesignEntity Role' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeLeaf selLeaf = selTreePage.getSelectedPGTreeLeaf();
        if(selLeaf == null) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        DesignEntity selEntity = selTreePage.getSelectedDesignEntity();
        if(selEntity == null) {
            showErrorDialog("Please select an entity", this);
            return;
        }
        PeerGroup parentPG = selTreePage.getParentPGOfSelectedLeaf(selLeaf);
        
        NewRoleForm form = new NewRoleForm(selEntity, parentPG, this);
    }//GEN-LAST:event_newRoleAdvMenuItemActionPerformed
    
    private void showPeersRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPeersRadioButtonMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Show Peers Only' radioButton */
        setShow(PEERS);
    }//GEN-LAST:event_showPeersRadioButtonMenuItemActionPerformed
    
    private void showPgsRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPgsRadioButtonMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Show PeerGroups Only' radioButton */
        setShow(PGS);
    }//GEN-LAST:event_showPgsRadioButtonMenuItemActionPerformed
    
    private void showAllRadioButtonMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllRadioButtonMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Show All' radioButton */
        setShow(ALL);
    }//GEN-LAST:event_showAllRadioButtonMenuItemActionPerformed
    
    private void joinPGMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinPGMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Join PeerGroup' */
        PGTreeNode selNode = getSelectedPGTreePage().getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a peergroup", this);
            return;
        }
        PeerGroup selPG = selNode.getPeerGroup();
        if(selPG != null) {
            if(appUser.peerIsAlreadyMember(selPG)) {
                showErrorDialog("Peer is already a member of " + selPG.getPeerGroupName(), this);
                return;
            }
            appUser.joinPeerGroup(selPG);
            refreshTreeWithNodeSelected();
        }
    }//GEN-LAST:event_joinPGMenuItemActionPerformed
    
    private void newPGUsingAdvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPGUsingAdvMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Instantiate Existing PeerGroup' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        /**this makes the node from a selected pgAdv, then redraws the tree: */
        selTreePage.addToTreeNewPG_UsingAdv();
    }//GEN-LAST:event_newPGUsingAdvMenuItemActionPerformed
    
    private void stopSearcherMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopSearcherMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Stop Searcher' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a peergroup node", this);
            return;
        }
        //ContentSearcherTree ts = selNode.getContentSearcherTree();
        selNode.getContentSearcherTree().setRemoteSearching(false);
        selTreePage.setSearcherStatusField();
    }//GEN-LAST:event_stopSearcherMenuItemActionPerformed
    
    private void searchForRemoteAdvsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchForRemoteAdvsMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: ' Search for Remote Resources in PeerGroup (Start Searcher)' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a peergroup node", this);
            return;
        }
        //ContentSearcherTree ts = selNode.getContentSearcherTree();
        ContentSearcherTree treeSearcher = selNode.getContentSearcherTree();
        treeSearcher.getRemoteAdvsAllTypes();
        //        treeSearcher.getThread().start();
        //        selTreePage.setSearcherStatusField();
    }//GEN-LAST:event_searchForRemoteAdvsMenuItemActionPerformed
    
    private void searchForLocalAdvsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchForLocalAdvsMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Show Local Resources of Selected PeerGroup' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode selNode = selTreePage.getSelectedPGTreeNode();
        if(selNode == null) {
            showErrorDialog("Please select a peergroup node", this);
            return;
        }
        selNode.addChildrenAndExpand();
    }//GEN-LAST:event_searchForLocalAdvsMenuItemActionPerformed
    
    private void newPeerGroupWithoutAdvMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newPeerGroupWithoutAdvMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'New PeerGroup' */
        PeerGroupsTreePage selTreePage = getSelectedPGTreePage();
        PGTreeNode parentNode = selTreePage.getSelectedPGTreeNode();
        if(parentNode == null) {
            showErrorDialog("Please select a peergroup node", this);
            return;
        }
        PeerGroup parentPG = parentNode.getPeerGroup();
        NewPeerGroupForm form = new NewPeerGroupForm(this, parentPG);
    }//GEN-LAST:event_newPeerGroupWithoutAdvMenuItemActionPerformed
    
    private void tabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabbedPaneMouseClicked
        // Add your handling code here:
        /**MenuItem: none--used for testing */
        //int currIndex = tabbedPane.getSelectedIndex();
        //DpmPage p = (DpmPage)tabbedPane.getSelectedComponent();
        //printMessage("Current pane is: " + p.getName());
        //activateThread(p.getName()); //given same name as pane
    }//GEN-LAST:event_tabbedPaneMouseClicked
    
    
    private void aboutDpmMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutDpmMenuItemActionPerformed
        // Add your handling code here:
        SimpleInformationForm form = new SimpleInformationForm(this);
        form.addDpmMessage();
    }//GEN-LAST:event_aboutDpmMenuItemActionPerformed
    
    private void exitDpmMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitDpmMenuItemActionPerformed
        // Add your handling code here:
        /**MenuItem: 'Exit Application' */
        netPG.stopApp();
        System.exit(0);
    }//GEN-LAST:event_exitDpmMenuItemActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    
    public DesignEntity getSelectedDesignEntityFromTreePage() {
        return getSelectedPGTreePage().getSelectedDesignEntity();
    }
    
    public LoopNetAdvertisement getSelectedLoopNetAdvFromTreePage() {
        return getSelectedPGTreePage().getSelectedLoopNetAdv();
    }
    
    /**A heavily used method */
    public PeerGroupsTreePage getSelectedPGTreePage() {
        DpmPage page = (DpmPage)tabbedPane.getSelectedComponent();
        if(!(page instanceof PeerGroupsTreePage)) {
            return null;
        }
        return (PeerGroupsTreePage)page;
    }
    
    public void printMessage(String s) {
        //clearMessage();
        String countS = String.valueOf(messageCount);
        messageArea.setText(messageArea.getText() + countS + ". " + s + NEWLINE);
        //messageArea.append(countS + ". " + s + NEWLINE); //put at the end
        //messageArea.insert(countS + ". " + s + NEWLINE, 0); //insert at the beginning
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
        //messageArea.setText(messageArea.getText());
        messageArea.repaint();
        this.messageCount++;
    }
    
    //prints without putting a newline at the end
    public void printMessageContinue(String s) {
        //clearMessage();
        //messageArea is the scrolling text area for messages
        messageArea.append(s + ";  ");
    }
    
    private void clearMessage() {
        messageArea.setText(EMPTY_STRING);
    }
    
    //    public void rendezvousEvent(RendezvousEvent event) {
    //    }
    
    /** Getter for property panelCount.
     * @return Value of property panelCount.
     *
     */
    public int getPanelCount() {
        return panelCount;
    }
    
    /** Setter for property panelCount.
     * @param panelCount New value of property panelCount.
     *
     */
    public void setPanelCount(int panelCount) {
        this.panelCount = panelCount;
    }
    
    /** Setter for property netPG.
     * @param netPG New value of property netPG.
     *
     */
    public void setNetPG(PeerGroup netPG) {
        this.netPG = netPG;
    }
    
    /** Getter for property peersPage.
     * @return Value of property peersPage.
     *
     */
    public DpmPage getPeersPage() {
        return peersPage;
    }
    
    /** Setter for property peersPage.
     * @param peersPage New value of property peersPage.
     *
     */
    public void setPeersPage(ListPage peersPage) {
        this.peersPage = peersPage;
    }
    
    
    
    /** Getter for property netPG.
     * @return Value of property netPG.
     *
     */
    public PeerGroup getNetPG() {
        return netPG;
    }
    
    /** Getter for property dpmNet.
     * @return Value of property dpmNet.
     *
     */
    public PeerGroup getDpmNet() {
        return dpmNet;
    }
    
    /** Setter for property dpmNet.
     * @param dpmNet New value of property dpmNet.
     *
     */
    public void setDpmNet(PeerGroup dpmNet) {
        this.dpmNet = dpmNet;
    }
    
    /** Getter for property peerGroupsPage.
     * @return Value of property peerGroupsPage.
     *
     */
    public ListPage getPeerGroupsPage() {
        return peerGroupsPage;
    }
    
    
    /** Getter for property pgUtilities.
     * @return Value of property pgUtilities.
     *
     */
    public PGUtilities getPgUtilities() {
        return pgUtilities;
    }
    
    /** Setter for property pgUtilities.
     * @param pgUtilities New value of property pgUtilities.
     *
     */
    public void setPgUtilities(PGUtilities pgUtilities) {
        this.pgUtilities = pgUtilities;
    }
    
    /** Setter for property peerGroupsPage.
     * @param peerGroupsPage New value of property peerGroupsPage.
     *
     */
    public void setPeerGroupsPage(ListPage peerGroupsPage) {
        this.peerGroupsPage = peerGroupsPage;
    }
    
    /** Getter for property tasksPage.
     * @return Value of property tasksPage.
     *
     */
    public ListPage getDesignEntitysPage() {
        return tasksPage;
    }
    
    /** Setter for property tasksPage.
     * @param tasksPage New value of property tasksPage.
     *
     */
    public void setDesignEntitysPage(ListPage tasksPage) {
        this.tasksPage = tasksPage;
    }
    
    
    
    /** Getter for property show.
     * @return Value of property show.
     *
     */
    public java.lang.String getShow() {
        return show;
    }
    
    /** Setter for property show.
     * @param show New value of property show.
     *
     */
    public void setShow(java.lang.String show) {
        this.show = show;
        getSelectedPGTreePage().getRoot().addChildrenAndExpand();
    }
    
    /** Getter for property loopNetAdv.
     * @return Value of property loopNetAdv.
     *
     */
    //    public dpm.content.advertisement.net.LoopNetAdvertisement getLoopNetAdv() {
    //        return loopNetAdv;
    //    }
    
    /** Setter for property loopNetAdv.
     * @param loopNetAdv New value of property loopNetAdv.
     *
     */
    //    public void setLoopNetAdv(dpm.content.advertisement.net.LoopNetAdvertisement loopNetAdv) {
    //        this.loopNetAdv = loopNetAdv;
    //    }
    
    /** Getter for property petriNetApp.
     * @return Value of property petriNetApp.
     *
     */
    public Demonstrator getPetriNetApp() {
        if (petriNetApp == null) {
            this.petriNetApp = new Demonstrator();
            return petriNetApp;
        }
        else {
            return petriNetApp;
        }
    }
    
    /** Setter for property petriNetApp.
     * @param petriNetApp New value of property petriNetApp.
     *
     */
    public void setPetriNetApp(Demonstrator petriNetApp) {
        this.petriNetApp = petriNetApp;
    }
    
    /** Setter for property treePageDPM.
     * @param treePageDPM New value of property treePageDPM.
     *
     */
    public void setTreePageDPM(PeerGroupsTreePage treePageDPM) {
        this.treePageDPM = treePageDPM;
    }
    
    /** Getter for property treePageDPM.
     * @return Value of property treePageDPM.
     *
     */
    public PeerGroupsTreePage getTreePageDPM() {
        return treePageDPM;
    }
    
    /** Getter for property dpmLoopsNet.
     * @return Value of property dpmLoopsNet.
     *
     */
    public PeerGroup getDpmLoopsNet() {
        return dpmLoopsNet;
    }
    
    /** Setter for property dpmLoopsNet.
     * @param dpmLoopsNet New value of property dpmLoopsNet.
     *
     */
    public void setDpmLoopsNet(PeerGroup dpmLoopsNet) {
        this.dpmLoopsNet = dpmLoopsNet;
    }
    
    /** Getter for property treePageWorld.
     * @return Value of property treePageWorld.
     *
     */
    public PeerGroupsTreePage getTreePageWorld() {
        return treePageWorld;
    }
    
    /** Setter for property treePageWorld.
     * @param treePageWorld New value of property treePageWorld.
     *
     */
    public void setTreePageWorld(PeerGroupsTreePage treePageWorld) {
        this.treePageWorld = treePageWorld;
    }
    
    /** Getter for property finishedStartup.
     * @return Value of property finishedStartup.
     *
     */
    public boolean finishedStartup() {
        return finishedStartup;
    }
    
    /** Getter for property appUser.
     * @return Value of property appUser.
     *
     */
    public Peer getAppUser() {
        return appUser;
    }
    
    /** Setter for property appUser.
     * @param appUser New value of property appUser.
     *
     */
    public void setAppUser(Peer appUser) {
        this.appUser = appUser;
    }
    
    /**
     * Getter for property advUtils.
     * @return Value of property advUtils.
     */
    public AdvUtilities getAdvUtils() {
        return advUtils;
    }
    
    /**
     * Setter for property advUtils.
     * @param advUtils New value of property advUtils.
     */
    public void setAdvUtils(AdvUtilities advUtils) {
        this.advUtils = advUtils;
    }
    
    /**
     * Getter for property numThreads.
     * @return Value of property numThreads.
     */
    public int getNumThreads() {
        return numThreads;
    }
    
    /**
     * Setter for property numThreads.
     * @param numThreads New value of property numThreads.
     */
    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem abandonEntityAndIterateMenuItem;
    private javax.swing.JMenuItem abandonEntityMenuItem;
    private javax.swing.JMenuItem aboutDpmMenuItem;
    private javax.swing.JMenuItem broadcastPGContentsMenuItem;
    private javax.swing.JMenuItem collapseTreeMenuItem;
    private javax.swing.JMenuItem deleteAllAdvsInPG;
    private javax.swing.JMenuItem deleteEntityMenuItem;
    private javax.swing.JMenuItem deleteLinkMenuItem;
    private javax.swing.JMenuItem deleteMessageMenuItem;
    private javax.swing.JMenuItem deleteNetMenuItem;
    private javax.swing.JMenuItem deletePeerGroupMenuItem;
    private javax.swing.JMenuItem deletePoliciesMenuItem;
    private javax.swing.JMenuItem deletePrivateMessageMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu entitiesMenu;
    private javax.swing.JMenuItem exitDpmMenuItem;
    private javax.swing.JMenuItem exitRenewMenuItem;
    private javax.swing.JMenuItem expandOneNodeMenuItem;
    private javax.swing.JMenuItem expandTreeMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem getDisplayStateOfNodeMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JMenuItem joinPGMenuItem;
    private javax.swing.JMenuItem leavePGMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JScrollPane messageScrollPane;
    private javax.swing.JMenuItem newConstraintLinkMenuItem;
    private javax.swing.JMenuItem newInfoLinkMenuItem;
    private javax.swing.JMenuItem newInputAdvMenuItem;
    private javax.swing.JMenuItem newPGUsingAdvMenuItem;
    private javax.swing.JMenuItem newPeerGroupWithoutAdvMenuItem;
    private javax.swing.JMenuItem newPolicyAdvMenuItem;
    private javax.swing.JMenuItem newRoleAdvMenuItem;
    private javax.swing.JMenuItem newSequentialEntityLinkChildOnlyMenuItem;
    private javax.swing.JMenuItem newSubEntityChildOnlyMenuItem;
    private javax.swing.JMenuItem newSubEntityParentChildMenuItem;
    private javax.swing.JMenuItem newUserNamedEntityMenuItem;
    private javax.swing.JMenuItem nodeExpandedMenuItem;
    private javax.swing.JMenuItem notesToUserRePNetsMenuItem;
    private javax.swing.JMenuItem numThreadsMenuItem;
    private javax.swing.JMenuItem openPNetofSelectedNetAdvMenuItem;
    private javax.swing.JMenuItem openRenewMenuItem;
    private javax.swing.JMenu peerGroupsMenu;
    private javax.swing.JMenu petriNetsMenu;
    private javax.swing.JMenuItem postChatMessageMenuItem;
    private javax.swing.JMenuItem postPrivateChatMessageMenuItem;
    private javax.swing.JMenuItem resignAllRolesMenuItem;
    private javax.swing.JMenuItem resignOneRoleMenuItem;
    private javax.swing.JMenuItem saveDrawingAsNetAdvMenuItem;
    private javax.swing.JMenuItem searchForLocalAdvsMenuItem;
    private javax.swing.JMenuItem searchForRemoteAdvsMenuItem;
    private javax.swing.JMenuItem showAllEntityContentMenuitem;
    private javax.swing.JRadioButtonMenuItem showAllRadioButtonMenuItem;
    private javax.swing.ButtonGroup showButtonGroup;
    private javax.swing.JRadioButtonMenuItem showEntitiesRadioButtonMenuItem;
    private javax.swing.JMenuItem showInputPolicyMenuItem;
    private javax.swing.JRadioButtonMenuItem showLinksRadioButtonMenuItem;
    private javax.swing.JMenu showMenu;
    private javax.swing.JRadioButtonMenuItem showMessagesRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem showNetsRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem showPeersRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem showPgsRadioButtonMenuItem;
    private javax.swing.JMenuItem showStateChangeInfoMenuItem;
    private javax.swing.JMenuItem stopSearcherMenuItem;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JMenuItem viewEntityHistoryMenuItem;
    private javax.swing.JMenuItem viewInputsOutputsMenuItem;
    private javax.swing.JMenuItem viewLinksMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
    
    
    
}
