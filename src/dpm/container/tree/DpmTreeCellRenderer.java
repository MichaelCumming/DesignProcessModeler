/*
 * DpmTreeCellRenderer.java
 *
 * Created on December 16, 2003, 2:50 PM
 */

package dpm.container.tree;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 *
 * @author  cumming
 */
//Copied from: http://java.sun.com/docs/books/tutorial/uiswing/components/tree.html#display

public class DpmTreeCellRenderer extends DefaultTreeCellRenderer {
 
    /** Creates a new instance of DpmTreeCellRenderer */
    public DpmTreeCellRenderer() {
    }
    
    public Component getTreeCellRendererComponent(
    JTree tree, Object node, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, node, sel, expanded, leaf, row, hasFocus);
        //System.out.println("Rendering...");
        DefaultMutableTreeNode n = (DefaultMutableTreeNode)node;
        JLabel label = (JLabel)n.getUserObject();
       
        return label;
    }
   
}
