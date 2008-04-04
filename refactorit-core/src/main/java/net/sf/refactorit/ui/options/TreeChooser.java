/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author juri
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TreeChooser {
  boolean okPressed;

  /**
   * causes <code>okPressed</code> property to be set to <code><b>true</b></code>
   */
  private JButton okButton = new JButton("Ok");

  /**
   * causes <code>okPressed</code> property to be set to <code><b>false</b></code>
   */
  private JButton cancelButton = new JButton("Cancel");

//  private Model dialogModel;

  /**
   * visualization component
   */
  private JTree tree;

  /**
   * the actual window that will contain this <code>TreeChooser</code>
   */
  final RitDialog dialog;

  /**
   * Opens a dialog with a specified title allowing the user to select a number of objects
   * represented as a tree with each element rendered with a specified <code>TreeCellRenderer</code>
   * and backed by the provided <code>TreeModel</code>
   *
   * @param parent
   * @param title
   * @param model
   * @param renderer
   * @return
   */
  public static Object[] getNewDataObjectReferences(
      IdeWindowContext context, String title,
      TreeModel model, TreeCellRenderer renderer
  ) {
    TreeChooser dialog = new TreeChooser(context, title, model, renderer);
    dialog.show();

    if (dialog.okPressed) {
      return dialog.getSelectedDataObjects();
    }

    return null;
  }

  private void show() {
    dialog.show();
  }

  /**
   * Opens a dialog with a specified title allowing the user to select a number of objects
   * represented as a tree and backed by the provided <code>TreeModel</code>
   * (the same as calling <code>getNewDataObjectReferences(parent,title,model,null)</code> )
   *
   * @param context - something to satisfy need for parent :)
   * @param title - a title for the dialog
   * @param model - something not very nice that will be eradicated from the code eventually :)
   * @return an array of user-selected objects
   */
  public static Object[] getNewDataObjectReferences(
      IdeWindowContext context, String title, TreeModel model
  ) {
    return getNewDataObjectReferences(
        context, title, model, new DefaultTreeCellRenderer());
  }

  /**
   * Opens a dialog with no title allowing the user to select a number of objects
   * represented as a tree and backed by the provided <code>TreeModel</code>
   * The same effect as calling <code>getNewDataObjectReferences(parent,"",model,new DefaultTreeCellRenderer())</code>
   *
   * @param context - something to satisfy need for parent :)
   * @param model - <code>TreeModel</code> probably an adapter for some existing object model
   * @return an array of user-selected objects
   */
  public static Object[] getNewDataObjectReferences(
      IdeWindowContext context, TreeModel model
  ) {
    return getNewDataObjectReferences(
        context, "", model, new DefaultTreeCellRenderer());
  }

//  /**
//   * @deprecated use <code>getNewDataObjectReferences(Window parent, TreeModel model)</code>
//   * instead
//   * @param context - something to satisfy need for parent :)
//   * @param model - something not very nice that will be eradicated from the code eventually :)
//   * @return the array of user-selected objects
//   */
//  public static Object[] getNewDataObjectReferences(Window parent, final Model model) {
//    TreeChooser dialog;
//
//    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
//      public Component getTreeCellRendererComponent(JTree tree, Object value,
//          boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
//        Component component = super.getTreeCellRendererComponent(
//            tree, value, sel, expanded, leaf, row, hasFocus);
//        //System.out.println(value.getClass().getName());
//        if (!(value instanceof String)) {
//          setText(model.getDisplayableString(value));
//        }
//        return component;
//      }
//    };
//    renderer.setLeafIcon(renderer.getClosedIcon());
//
//    dialog = new TreeChooser(parent,
//        model.getDialogTitle(), new ModelToTreeModelAdapter(model), renderer);
//
//    dialog.setVisible(true);
//
//    if (dialog.okPressed) {
//      return dialog.getSelectedDataObjects();
//    }
//
//    return null;
//  }

  private TreeChooser(
      IdeWindowContext context, String title,
      TreeModel model, TreeCellRenderer renderer
  ) {
//    this.dialogModel = model;

    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed = true;
        dialog.dispose();
      }
    });

    final ActionListener cancelActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    };
    
    cancelButton.addActionListener(cancelActionListener);
    
    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());

    this.tree = new JTree(model);
    this.tree.setCellRenderer(renderer);

    contentPane.add(new JScrollPane(tree), BorderLayout.CENTER);

    JPanel buttonsPanel = new JPanel();
    buttonsPanel.add(okButton);
    buttonsPanel.add(cancelButton);

    contentPane.add(buttonsPanel, BorderLayout.SOUTH);

    dialog = RitDialog.create(context);
    dialog.setTitle(title);
    dialog.setContentPane(contentPane);
    dialog.setSize(300, 400);
    
    SwingUtil.initCommonDialogKeystrokes(dialog, okButton, cancelButton, 
        cancelActionListener);
  }

  private Object[] getSelectedDataObjects() {
    TreePath[] selectedPaths = this.tree.getSelectionPaths();
    if (selectedPaths == null) {
      selectedPaths = new TreePath[0];
    }

    List result = new ArrayList();

    for (int i = 0; i < selectedPaths.length; i++) {
      //if (selectedPaths[i].getLastPathComponent() instanceof ModelNode) {
      // Root node is not an instance of ModelNode
      Object selectedNode = selectedPaths[i].getLastPathComponent();
      if (selectedNode instanceof String) {
        continue;
      }
      result.add(selectedNode);
    }
    return result.toArray(new Object[0]);
  }

  /*  private TreeModel createTreeModel() {
      DefaultMutableTreeNode rootNode =
          new DefaultMutableTreeNode(dialogModel.getRootNodeTitle());

   for (Iterator i = dialogModel.getRootDataObjects().iterator(); i.hasNext();) {
        rootNode.add(new ModelNode(i.next(), dialogModel));
      }

      return new DefaultTreeModel(rootNode);
    }*/

  /*
     static class ModelNode extends DefaultMutableTreeNode {
    private Object dataObject;
    private Model model;

    private List childDataObjects = new ArrayList();

    public ModelNode(Object dataObject, Model model) {
      this.dataObject = dataObject;
      this.model = model;

      this.childDataObjects = model.getChildDataObjects( dataObject );
    }

    public int getChildCount() {
      return childDataObjects.size();
    }

    public boolean isLeaf() {
      return getChildCount() == 0;
    }

    public TreeNode getChildAt( int index ) {
      return new ModelNode( childDataObjects.get(index), model );
    }

    public int getIndexOfChild( Object child ) {
      for (int i=0; i<getChildCount(); i++) {
        if (child.equals(getChildAt(i))) {
          return i;
        }
      }
      return -1;
    }

    public Object getDataObject() {
      return dataObject;
    }

    public String toString() {
      return model.getDisplayableString( dataObject );
    }
     }
   */

//  /**
//   * A temporary means to let the old redundant code still work with the
//   * TreeChooser.Model (formely AddFromFilesystems.Model) (the main problem with
//   * this approach was increased complexity due to introducing this unnecessary
//   * <code>Model</code> class which is fully covered with and not
//   * easier to implement than <code>TreeModel</code>)
//   *
//   * @author juri
//   *
//   * TODO To change the template for this generated type comment go to Window -
//   * Preferences - Java - Code Style - Code Templates
//   */
//  static class ModelToTreeModelAdapter implements TreeModel{
//
//    private Model model;
//
//    public ModelToTreeModelAdapter(Model model){
//      this.model = model;
//    }
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#getRoot()
//     */
//    public Object getRoot() {
//      return model.getRootNodeTitle();
//    }
//
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
//     */
//    public int getChildCount(Object parent) {
//      if (parent instanceof String){
//        return model.getRootDataObjects().size();
//      }
//      return model.getChildDataObjects(parent).size();
//    }
//
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
//     */
//    public boolean isLeaf(Object node) {
//      if (node instanceof String){
//        return false;
//      }
//      return getChildCount(node) == 0;
//    }
//
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
//     */
//    public void addTreeModelListener(TreeModelListener l) {}
//
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
//     */
//    public void removeTreeModelListener(TreeModelListener l) {}
//
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
//     */
//    public Object getChild(Object parent, int index) {
//      if (parent instanceof String) {
//        return model.getRootDataObjects().get(index);
//      }
//      return model.getChildDataObjects(parent).get(index);
//    }
//
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
//     */
//    public int getIndexOfChild(Object parent, Object child) {
//      if (parent instanceof String) {
//        return model.getRootDataObjects().indexOf(child);
//      }
//      return model.getChildDataObjects(parent).indexOf(child);
//    }
//
//    /* (non-Javadoc)
//     * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
//     */
//    public void valueForPathChanged(TreePath path, Object newValue) {}
//
//  }

//  /**
//   * the legacy interface (from the time when this enclosing class carrying the
//   * name of <code>AddFromFilesystemsDialog</code>) that I'm trying to
//   * eradicate from refactorIT code (juri)
//   *
//   * @author juri
//   * @deprecated use <code>TreeModel</code> instead
//   */
//  public interface Model {
//    List getRootDataObjects();
//    List getChildDataObjects( Object parentDataObject );
//
//    String getDisplayableString( Object dataObject );
//
//    String getDialogTitle();
//    String getRootNodeTitle();
//  }
}
