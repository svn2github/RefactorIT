/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;



import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.text.Occurrence;
import net.sf.refactorit.source.SourceHolder;
import net.sf.refactorit.transformations.SimpleSourceHolder;
import net.sf.refactorit.ui.Shortcuts;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.writer.TableFormat;
import net.sf.refactorit.ui.treetable.writer.TableLayout;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Common look-and-feel JTreeTable for refactoring modules.<br>
 * <br>
 * Adds an additional functionality by using {@link BinTreeTableModel}
 * (which uses {@link BinTreeTableNode}'s) which automates most operations
 * when working with {@link net.sf.refactorit.classmodel.BinItem BinItem}'s,
 * e.g. automatic names, source lines, icons.
 *
 * @author Anton Safonov
 */
public class BinTreeTable extends JTreeTable {
  protected final RefactorItContext context;

  private boolean listenForEnterKey = true;

  /**
   * Is displayed as grayed out; must not contain any references to BinItems; must tell the user
   * to click the "Refresh" button.
   */
  public static class GrayCopyOfModel extends BinTreeTableModel {
    private String[] columnNames;
    private Class[] columnClasses;

    public GrayCopyOfModel(final BinTreeTableModel original,
        final Project project) {
      super(new GrayRootNode());

      BinTreeTableNode root = (BinTreeTableNode) getRoot();
      root.addChild(
          new GrayCopyOfNode((BinTreeTableNode) original.getRoot(), project,
          original)
          );

      root.setShowHiddenChildren(((BinTreeTableNode) original.getRoot()).
          isShowHiddenChildren());

      copyColumnNames(original);
      copyColumnClasses(original);
    }

    private void copyColumnNames(BinTreeTableModel original) {
      columnNames = new String[original.getColumnCount()];
      for (int i = 0; i < original.getColumnCount(); i++) {
        columnNames[i] = original.getColumnName(i);
      }
    }

    private void copyColumnClasses(BinTreeTableModel original) {
      columnClasses = new Class[original.getColumnCount()];
      for (int i = 0; i < original.getColumnCount(); i++) {
        columnClasses[i] = original.getColumnClass(i);
      }
    }

    public int getColumnCount() {
      return columnNames.length;
    }

    public String getColumnName(int column) {
      return columnNames[column];
    }

    public Object getValueAt(Object node, int column) {
      return ((ColumnValueProvider) node).getValueAt(column);
    }

    public Class getColumnClass(int column) {
      return columnClasses[column];
    }
  }

  private interface ColumnValueProvider {
    Object getValueAt(int column);
  }


  private static class GrayRootNode extends BinTreeTableNode implements
      ColumnValueProvider {
    public GrayRootNode() {
      super("Results are expired, click 'Run again' to update", false);
    }

    public Object getValueAt(int column) {
      if (column == 0) {
        return this;
      }

      return "";
    }
  }


  /** Is displayed as grayed out; must not reference any BinItems */
  public static class GrayCopyOfNode extends BinTreeTableNode implements
      ColumnValueProvider {
    private final int lineNumberInt;
    private final String lineSource;
    private final String compilationUnitName;
    private final int type;

    private final Project project;

    private Object[] values;

    public GrayCopyOfNode(
        final BinTreeTableNode originalNode,
        final Project project,
        final BinTreeTableModel originalModel
        ) {
      super(originalNode.getDisplayName(), originalNode.isShowSource());

      this.lineNumberInt = originalNode.queryLineNumber();
      this.lineSource = originalNode.getLineSource();
      this.type = originalNode.getType();

      setHidden(originalNode.isHidden());

      if (originalNode.getSource() == null) {
        this.compilationUnitName = null;
      } else {
        this.compilationUnitName = originalNode.getSource().getSource().
            getRelativePath();
      }

      this.project = project;

      copyValues(originalNode, originalModel);

      copyChildren(originalNode, originalModel);
    }

    private void copyValues(
        final BinTreeTableNode originalNode,
        final BinTreeTableModel originalModel
        ) {
      values = new Object[originalModel.getColumnCount() - 1];
      for (int column = 1; column < originalModel.getColumnCount(); column++) {
        Object value = originalModel.getValueAt(originalNode, column);

        if (value != null) {
          if (value instanceof BinItem) {
            values[column - 1] = value.toString();
          } else {
            values[column - 1] = value;
          }
        }
      }
    }

    private void copyChildren(
        final BinTreeTableNode originalNode,
        final BinTreeTableModel originalModel
        ) {
      for (int i = 0; i < originalModel.getChildCount(originalNode); i++) {
        BinTreeTableNode originalChild =
            (BinTreeTableNode) originalModel.getChild(originalNode, i);

        addChild(new GrayCopyOfNode(originalChild, project, originalModel));
      }
    }

    public int queryLineNumber() {
      return lineNumberInt;
    }

    public String getLineSource() {
      return lineSource;
    }

    public SourceHolder getSource() {
      if (compilationUnitName == null) {
        return null;
      }

      return project.getCompilationUnitForName(compilationUnitName);
    }

    public int getType() {
      return type;
    }

    public Object getValueAt(int column) {
      if (column == 0) {
        return getDisplayName();
      }

      return values[column - 1];
    }
  }

  public BinTreeTable(
      final BinTreeTableModel model, final RefactorItContext context) {
    // HACK: Need this l&f stuff to support BinTreeTable under Mac
    super(changeLookAndFeelIfNeeded(model));

    this.context = context;

    init(context);

    // HACK: Need this l&f stuff to support BinTreeTable under Mac
// ARS   RuntimePlatform.restoreLookAndFeelIfNeeded();
  }

  public BinTreeTable(
      final BinTreeTableModel model, final int style,
      final RefactorItContext context
      ) {
    // HACK: Need this l&f stuff to support BinTreeTable under Mac
    super(changeLookAndFeelIfNeeded(model), style);

    this.context = context;

    init(context);

    // HACK: Need this l&f stuff to support BinTreeTable under Mac
// ARS   RuntimePlatform.restoreLookAndFeelIfNeeded();
  }

  public void setListenForEnterKey(boolean b) {
    this.listenForEnterKey = b;
  }

  /** Allows changing L&F before invoking the super constructor */
  private static BinTreeTableModel changeLookAndFeelIfNeeded(
      final BinTreeTableModel model
      ) {
// ARS   RuntimePlatform.changeLookAndFeelIfNeeded();
    return model;
  }

  private void init(final RefactorItContext context) throws
      NumberFormatException {
//  this.setSelectionMode(DefaultListSelectionModel.SINGLE_INTERVAL_SELECTION);

    this.addKeyListener(new KeyAdapter() {
      private void checkShortcuts(final KeyEvent ke) {
        final TreePath[] paths = getTree().getSelectionPaths();
        if (paths == null || paths.length == 0) {
          return;
        }

        final KeyStroke ks = KeyStroke
            .getKeyStroke(ke.getKeyCode(), ke.getModifiers());

        ShortcutAction shortcutAction = Shortcuts.getAction(ks);

        if (shortcutAction instanceof IdeAction) {
          RefactorItActionUtils.run((IdeAction) shortcutAction);
          return;
        }

        final RefactorItAction act = (RefactorItAction) shortcutAction;
        //System.out.println( "act: " + act );
        if (act == null) {
          return;
        }
        ke.consume();

        final Object obj;
        if (paths.length == 1) {
          obj = ((BinTreeTableNode) paths[0].getLastPathComponent()).getBin();
        } else {
          final int size = paths.length;
          final Object[] bins = new Object[size];
          for (int i = 0; i < size; i++) {
            bins[i] = ((BinTreeTableNode)
                paths[i].getLastPathComponent()).getBin();
          }
          obj = bins;
        }

        List actions;
        if (obj instanceof Object[]) {
          actions = ModuleManager.getActions((Object[]) obj);
        } else {
          actions = ModuleManager.getActions(obj);
        }

        if (actions == null || actions.size() == 0 || !actions.contains(act)) {
          return;
        }

        //System.out.println( "obj: " + obj );
        //System.out.println( "class: " + obj.getClass() );
        context.setState(null);
        if (RefactorItActionUtils.run(act, context, obj)) {
          act.updateEnvironment(context);
        } else {
          act.raiseResultsPane(context);
        }
      }

      public void keyPressed(final KeyEvent ke) {
        final int[] rows = BinTreeTable.this.getSelectedRows();
        if (rows == null || rows.length != 1) {
          return;
        }

        final int row = rows[0];

        switch (ke.getKeyCode()) {
          case KeyEvent.VK_ENTER:
            if(listenForEnterKey) {
              ke.consume();
              final TreePath path = BinTreeTable.this.getTree().getPathForRow(row);
              if (path == null) {
                return;
              }

              BinTreeTable.this.openCompilationUnit(
                  (ParentTreeTableNode) path.getLastPathComponent(), context);
            } else {
              getParent().dispatchEvent(ke);
            }
            break;

          case KeyEvent.VK_LEFT:
            ke.consume();
            if (BinTreeTable.this.getTree().isExpanded(row)) {
              BinTreeTable.this.getTree().collapseRow(row);
              //BinTreeTable.this.setRowSelectionInterval((row==0)?0:row-1, row);
              BinTreeTable.this.setRowSelectionInterval(row, row);
            } else {
              final TreePath newpath = BinTreeTable.this
                  .getTree().getPathForRow(row).getParentPath();
              if (newpath != null) {
                BinTreeTable.this.getTree().setSelectionPath(newpath);
              }
            }
            break;

          case KeyEvent.VK_RIGHT:
            ke.consume();
            // FIXME: ? isCollapsed is true for leaves???
            if (BinTreeTable.this.getTree().isCollapsed(row)) {
              BinTreeTable.this.getTree().expandRow(row);
              //BinTreeTable.this.setRowSelectionInterval((row==0)?0:row-1, row);
              BinTreeTable.this.setRowSelectionInterval(row, row);
            } else {
              BinTreeTable.this.setRowSelectionInterval(row, row);
              //if (BinTreeTable.this.getRowCount() >= row+1)
              //BinTreeTable.this.setRowSelectionInterval((row==0)?0:row, row+1);
            }
            break;

          case KeyEvent.VK_ADD:
          case KeyEvent.VK_PLUS: // doesn't know when this one is called
            if (ke.getModifiers() == KeyEvent.CTRL_MASK) {
              ke.consume();
              expandAll();
              break;
            }

          case KeyEvent.VK_SUBTRACT:
          case KeyEvent.VK_MINUS: // doesn't know when this one is called
            if (ke.getModifiers() == KeyEvent.CTRL_MASK) {
              ke.consume();
              collapseAll();
              break;
            }

          default:
            checkShortcuts(ke);
            break;
        }
      }
    });

    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent e) {
        handleDoubleclick(e);
      }

      public void mousePressed(final MouseEvent e) {
        //handleDoubleclick(e);
      }

      public void mouseReleased(final MouseEvent e) {
        //handleDoubleclick(e);
      }

      private void handleDoubleclick(final MouseEvent e) {
        final int count = e.getClickCount();
        if (count >= 2
            && e.getModifiers() == MouseEvent.BUTTON1_MASK) {

          //int row = BinTreeTable.this.rowAtPoint(e.getPoint());
          //TreePath path = BinTreeTable.this.getTree().getPathForRow(row);
          final TreePath path = getTree().getSelectionPath();
          if (path == null) {
            return;
          }
          BinTreeTable.this.openCompilationUnit(
              (ParentTreeTableNode) path.getLastPathComponent(), context);
        }
      }
    });

    this.addAncestorListener(new AncestorListener() {
      public void ancestorAdded(final AncestorEvent event) {
        // HACK: (Re)set the background color of BinPane to match the background of this treetable.
        // See method BinTreeTable.setBackground for more info.
        BinTreeTable.this.setBackground(BinTreeTable.this.getBackground());
      }

      public void ancestorMoved(final AncestorEvent event) {
      }

      public void ancestorRemoved(final AncestorEvent event) {
      }
    });

  }


  protected void openCompilationUnit(
      final ParentTreeTableNode node, final RefactorItContext context
      ) {
    if (context == null) {
      return;
    }

    if (node instanceof BinTreeTableNode) {
      BinTreeTableNode binNode = (BinTreeTableNode) node;
      final SourceHolder source = binNode.getSource();
      if (source == null) {
        return;
      }

      if (binNode.queryLineNumber() <= 0) {
        context.open(source);
      } else {
        context.show(source, binNode.queryLineNumber(),
            GlobalOptions.getOption("source.selection.highlight").equals("true"));
      }
    } else if (node instanceof NonJavaTreeTableNode) {
      NonJavaTreeTableNode nonJavaNode = (NonJavaTreeTableNode) node;
      Occurrence occurrence = (Occurrence) nonJavaNode.getBin();
      context.show(
          new SimpleSourceHolder(occurrence.getLine().getSource(), context.getProject()),
          occurrence.getLine().getLineNumber(), true);
    }
  }

  public final void expandAll() {
    final Object root = getBinTreeTableModel().getRoot();
    if (root instanceof ParentTreeTableNode) {
      expandRecursively(
          new TreePath(((ParentTreeTableNode) root).getPath()));
    }
  }

  void expandRecursively(final TreePath path) {
    if (path == null) {
      return;
    }

    final Object node = path.getLastPathComponent();
    if (node instanceof ParentTreeTableNode) {
      final ParentTreeTableNode in = (ParentTreeTableNode) node;

      getTree().expandPath(path);

      for (int i = 0, max = in.getChildCount(); i < max; i++) {
        expandRecursively(path.pathByAddingChild(in.getChildAt(i)));
      }
    }
  }

  public final void selectNode(final ParentTreeTableNode node) {
    if (node != null) {
      final TreePath selection = new TreePath(node.getPath());
      getTree().scrollPathToVisible(selection);
      getTree().setSelectionPath(selection);
    }

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        requestFocus();
      }
    });
  }

  public final void requestFocus() {
    if (!SwingUtilities.isEventDispatchThread()) {
      AppRegistry.getLogger(this.getClass()).debug(
          new Exception("Called fron non-event dispatch thread"));
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          BinTreeTable.super.requestFocus();
        }
      });
    }
    super.requestFocus();
//    if (SwingUtilities.isEventDispatchThread()) {
//      super.requestFocus();
//    } else {
//      SwingUtilities.invokeLater(new Runnable() {
//        public void run() {
//          BinTreeTable.super.requestFocus();
//        }
//      });
////      // FIXME: calling super.requestFocus() inside non-event dispatching thread is causing focus
////      // problems (read-only source area, etc) under *JBuilder*. That's why this focusing
////      // code here is turned off under JBuilder.
////
////      if (!IDEController.runningJBuilder()) {
////        super.requestFocus();
////      }
//    }
//
//    /*
//        // NOTE: here assumed that our tree is always SINGLE_SELECTION
//        if (getRowCount() > 0) {
//          int selected = getSelectedRow();
//          if (selected < 0) {
//            selected = 0;
//          }
//     setRowSelectionInterval(selected > 0 ? selected - 1 : selected, selected);
//        }
//     */
  }

  public final BinTreeTableModel getBinTreeTableModel() {
    // FIXME: return TreeTableModel, i.e. without cast?
    return (BinTreeTableModel) getTreeTableModel();
  }

  public final List getSelectedNodes() {
    List result = new ArrayList();

    final TreePath[] paths = getTree().getSelectionPaths();
    if (paths != null) {
      for (int i = 0; i < paths.length; i++) {
        final Object node = paths[i].getLastPathComponent();

        result.add(node);
      }
    }

    return result;
  }

  public final String getClipboardText(TableFormat format) {
    return getClipboardText(getBinTreeTableModel(), getSelectedNodes(), format);
  }

  public static String getClipboardText(
      BinTreeTableModel model, List selectedNodes, TableFormat format
      ) {
    return TableLayout.collectClipboardText(
        format, model, "", selectedNodes).toString();
  }

  /**
   * HACK: When background is set for the tree, it must also
   * set background of the BinPane it is in.
   */
  public final void setBackground(final Color c) {
    super.setBackground(c);

    // HACK: BinPanel is located by searching for it in the list of parent
    // components in the Swing component containment hierarchy.

    final BinPanel binPanel = (BinPanel)
        SwingUtilities.getAncestorOfClass(BinPanel.class, this);

    if (binPanel != null) { // if not inside BinPane then return
      binPanel.getCurrentPane().getViewport().setBackground(c);
    } else {
      final JScrollPane pane = (JScrollPane)
          SwingUtilities.getAncestorOfClass(JScrollPane.class, this);

      if (pane != null) {
        pane.getViewport().setBackground(this.getBackground());
      }
    }
  }

  /*
   * Expands tree if there are less than 8 leaves
   */
  public void smartExpand() {
    final int count = getLeafCount((ParentTreeTableNode) getBinTreeTableModel().getRoot());
    if (count <= 7) {
      expandAll();
    }
  }

  private int getLeafCount(final ParentTreeTableNode node) {
    final int children = node.getChildCount();
    if (children == 0) {
      return 1; // leaf itself
    }

    int count = 0;
    for (int i = 0; i < children; i++) {
      count += getLeafCount((ParentTreeTableNode) node.getChildAt(i));
      if (count > 7) { // shortcut
        break;
      }
    }

    return count;
  }

  public final void collapseAll() {
    final Object root = getBinTreeTableModel().getRoot();
    if (root instanceof ParentTreeTableNode) {
      collapsRecursively(
          new TreePath(((ParentTreeTableNode) root).getPath()));
    }
  }

  private void collapsRecursively(final TreePath path) {
    if (path == null) {
      return;
    }

    final Object node = path.getLastPathComponent();
    if (node instanceof ParentTreeTableNode) {
      final ParentTreeTableNode in = (ParentTreeTableNode) node;

      for (int i = 0, max = in.getChildCount(); i < max; i++) {
        collapsRecursively(path.pathByAddingChild(in.getChildAt(i)));
      }

      if (in.getParent() != null) {
        getTree().collapsePath(path);
      }
    }
  }

  public List getApplicableActions(
      final UITreeNode[] nodes, final IdeWindowContext context
      ) {
    return Collections.EMPTY_LIST;
  }

  /**
   * Gives actions specific to the that node (to be appended or
   * prepended to the popup menu).
   *
   * @return actions ({@link net.sf.refactorit.ui.module.RefactorItAction}).
   *         Default actions (like Where Used, etc.) should not be returned.
   *
   */
  public final List getApplicableActions(
      final UITreeNode node, final IdeWindowContext context
      ) {
    return Collections.EMPTY_LIST;
  }

  public final BinTreeTable createGrayCopy(RefactorItContext context) {
    // If already grayed, don't gray further (otherwise we'd have too many extra label root nodes)
    if (getTreeTableModel() instanceof GrayCopyOfModel) {
      return this;
    }

    BinTreeTableModel grayCopyOfOldModel = new GrayCopyOfModel(
        this.getBinTreeTableModel(), context.getProject());

    BinTreeTable grayTable = new BinTreeTable(grayCopyOfOldModel, context);

    // Preserve column hiding
    for (int i = 1, max = getColumnModel().getColumnCount(); i < max; i++) {
      final TableColumn grayColumn = grayTable.getColumnModel().getColumn(i);
      if (getColumnModel().getColumn(i).getWidth() == 0) {
        grayColumn.setMinWidth(0);
        grayColumn.setMaxWidth(0);
        grayColumn.setWidth(0);
        grayColumn.setPreferredWidth(0);
        grayColumn.setResizable(false);
      }
    }

    // Preserve width information of columns
    for (int i = 0, max = grayTable.getColumnModel().getColumnCount(); i < max; i++) {
      TableColumn grayColumn = grayTable.getColumnModel().getColumn(i);
      TableColumn column = getColumnModel().getColumn(i);
      grayColumn.setMinWidth(column.getMinWidth());
      grayColumn.setPreferredWidth(column.getPreferredWidth());
      grayColumn.setMaxWidth(column.getMaxWidth());
      grayColumn.setWidth(column.getWidth());
    }

    int extraRootNodeCount = 1;

    // Preserve expansion state of rows
    JTree tree = getTree();
    JTree grayTree = grayTable.getTree();
    for (int row = 0, max = tree.getRowCount(); row < max; row++) {
      if (tree.isExpanded(row)) {
        grayTree.expandRow(row + extraRootNodeCount);
      }
    }

    // Preserve selection state (scroll state is not preserved *on purpose*
    // to make the user look at the root node that says 'results are expired'
    grayTree.setSelectionRow(
        tree.getMinSelectionRow() + extraRootNodeCount);

    // Gray out background
    grayTable.setBackground(new Color(230, 230, 230));

    return grayTable;
  }
}
