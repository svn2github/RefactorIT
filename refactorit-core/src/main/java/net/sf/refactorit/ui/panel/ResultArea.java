/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.panel;

import net.sf.refactorit.audit.ReconcileActionDecorator;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.loader.ProjectChangedListener;
import net.sf.refactorit.query.dependency.GraphPanel;
import net.sf.refactorit.ui.SystemClipboard;
import net.sf.refactorit.ui.audit.AuditTreeTable;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.tree.MultilineRowTree;
import net.sf.refactorit.ui.tree.UITreeNode;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;
import net.sf.refactorit.ui.treetable.writer.PlainTextTableFormat;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;


/**
 * It wraps the JComponent object into where various RefactorIT modules generate
 * their refactoring results.
 *
 * If you want to show some results of refactoring, then use this class to
 * create an instance of this class providing the JComponent (the result of
 * refactoring) you want to show on screen and also, ...
 *
 * @author  Jaanek Oja
 * @author  Anton Safonov
 */
public class ResultArea implements ProjectChangedListener {
  // The component to show on the screen. The show(...) method is called on
  // this object later to show the results of refactoring.
  JComponent component;

  // the context for ResultArea(JComponent). The context on
  // where to show the feedback actions (i.e. locations in source files
  // on the screen, ...). See also create(...) method for description about
  // this one.
  RefactorItContext context;

  // this is the list containing instances of ResultAreaListener class,
  // who are registered themselves via addResultAreaListener(...) method
  // for notifications about this instance events.
  private ArrayList listeners = new ArrayList();

  // This variable keeps a state whether this instance of ResultArea
  // is registered as listening for ProjectChanges or not.
  // see {@link Project.addProjectChangedListener()}
  private boolean isListeningProjectChanges;

  // The action which is executed on "Rerun". It is the same action instance
  // that which was executed on firts run(...) on that action. It is needed
  // to "Rerun" the same action if "rebuildPerformed(...)" event occurs to
  // display refreshed content.
  private RefactorItAction targetAction;

  // The reference object to BinXXX class for what this result was produced.
  // It is a lightweight object, i.e. it doesn't reference to a (huge)
  // sources tree and therefore it doesn't hold the memory up.
  private BinItemReference targetObject;

  // the tree selection listener for this.component object if it is a type of
  // BinTreeTable.
  private TreeSelectionListener treeSelectionListener;

  /**
   * Creates a new instance of ResultArea. See the create(...) function for
   * descriptions about parameters.
   *
   * We do not allow to create objects directly, use create(...) function.
   * It's because we may change the runtime type returned by create(...) someday.
   */
  private ResultArea(
      JComponent component,
      RefactorItContext context,
      RefactorItAction targetAction
  ) {
    this.context = context;
    this.targetAction = targetAction;

    setContent(component);
  }

  /**
   * This is the factory method for this class. Use it to obtain ResultArea
   * class instances.
   *
   * @param component the component you want to show on the screen. This
   * component instance must show some refactoring results. The show() method
   * is called on this component later on to show the refactoring results
   * on the screen.
   *
   * @param context the context object for ResultArea(JComponent). The context on
   * where to show the feedback actions (i.e. locations in source files
   * on the screen, ...). RefactorItContext object containing Project instance
   * and methods to show sources on the screen, ...
   * @param targetAction  The target Bin object for what this result in
   * this ResultArea was produced.
   */
  public static ResultArea create(
      JComponent component,
      RefactorItContext context,
      RefactorItAction targetAction
  ) {
    return new ResultArea(component, context, targetAction);
  }

  /**
   * Returns the user interface (UI) component, the refactoring result
   * which is rendered to the user.
   *
   * @return JComponent object. It is the refactoring result what is rendered
   * to the user. RefactorItAction's provide these objects when creating these
   * ones.
   */
  public JComponent getUI() {
    return this.component;
  }

  /**
   * Returns the component context to the caller.
   *
   * @return RefactorItContext to the caller.
   * FIXME: add more information about how it is related to this class.
   */
  public RefactorItContext getComponentContext() {
    return this.context;
  }

  /**
   * Returns the RefactorItAction which is executed on "Rerun".
   *
   * This should be associated with this instance until "Rerun" is actually
   * done. Because the BinPanel collapses and expand's (throwing away BinPanes
   * and creating new ones), so the target doesn't get lost.
   *
   * @return RefactorItAction, the action which is executed on "Rerun".
   */
  private RefactorItAction getAction() {
    return this.targetAction;
  }

  /**
   * @return true if this ResultArea has RefactorItAction associated
   * with it at the moment, otherwise returns false.
   */
  public boolean hasAction() {
    return this.getAction() != null;
  }

  /**
   * Sets the target BinXXX object for what the results are to be generated
   * (executing the RefactorItAction provided as constructor parameter) when
   * "rerun" (refresh) is performed for this instance.
   */
  public void setTargetBinObject(Object targetObject) {
    this.targetObject = BinItemReference.create(targetObject);
  }

  /**
   * Returns the target bin object. See the setTargetBinObject(...) function
   * for more information.
   */
  public BinItemReference getTargetBinObjectReference() {
    return this.targetObject;
  }

  /**
   * Sets a new JComponent to be shown to the user. And of course it sets
   * a new JComponent for this instance.
   *
   * + Move those statements in this function into class BinTreeTable
   *
   * @param component the component to show to the user. It's show(...)
   * method is called to do it.
   */
  public void setContent(final JComponent component) {
    // Without this check BinTreeTable would loose its vital listeners
    if (component == this.component) {
      return;
    }

    // unregister all listeners added to this.component before setting a new component.
    if ((this.component != null)) {
      // reset keyboard actions to break dependencies from this instance
      // of ResultArea and between keyboard listener objects.
      resetKeyboardActions();

      // get all mouselisteners from this.component that was added to it.
      // and remove them all.
      EventListener[] mouseListeners = this.component.getListeners(
          MouseListener.class);
      for (int i = 0; i < mouseListeners.length; i++) {
        this.component.removeMouseListener((MouseListener) (mouseListeners[i]));
      }
    }

    // unregister all listeners added to this.component before setting a new component.
    if (this.component != null && this.component instanceof BinTreeTable) {
      BinTreeTable treeTable = (BinTreeTable)this.component;
      // remove tree selection listeners also.
      treeTable.getTree().removeTreeSelectionListener(
          this.treeSelectionListener);
    }

    if (component != null && component instanceof BinTreeTable) {
      // register listeners for this.component
      this.treeSelectionListener = new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
          TreePath[] paths = e.getPaths();
          if (paths == null || paths.length == 0) {
            return;
          }

          if (paths.length == 1) {
            //TreePath path = paths[0];

            //UITreeNode node = (UITreeNode) path.getLastPathComponent();
            //registerKeys(node.getBin(), getUI());
          } else {
            int size = paths.length;
            Object[] bins = new Object[size];
            for (int i = 0; i < size; i++) {
              bins[i] = ((UITreeNode) paths[i].getLastPathComponent()).getBin();
            }
            //registerKeys(bins, getUI());
          }

        }
      };

      ((BinTreeTable) component).getTree().addTreeSelectionListener(
          this.treeSelectionListener);

      component.addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          handlePopupEvent(e);
        }

        public void mouseReleased(MouseEvent e) {
          handlePopupEvent(e);
        }

        private void handlePopupEvent(MouseEvent e) {
          ResultArea.this.handlePopupEvent(e, component);
        }
      });
    }

    // set the component for this instance.
    if (component == null) {
      this.component = new JLabel("No Content to display");
    } else {
      this.component = component;
    }

    // call it to notify all listener of this instance about content changed
    // event, so they can take appropriate tasks against this event.
    fireContentChangedEvent();

    // restore tree exapnsion & scroll state if neccessary
    if (context instanceof TreeRefactorItContext) {
      TreeRefactorItContext treeContext = (TreeRefactorItContext) context;

      restoreTreeExpansionAndScrollState(treeContext);
    }

    updateVisibilityOfHiddenTreeNodes();
  }

  private void handlePopupEvent(MouseEvent e, final JComponent component) {
//System.err.println("event: " + e + " - " + e.isPopupTrigger());
    if (!e.isPopupTrigger()) {
      return;
    }

    TreePath[] paths = ((BinTreeTable) component).getTree().
        getSelectionPaths();
    if (paths == null || paths.length == 0) {
      return;
    }

    Point point = SwingUtilities.convertPoint(
        component,
        (int) e.getPoint().getX(),
        (int) e.getPoint().getY(),
        getUI());

    JPopupMenu menu = null;
    if (paths.length == 1) {
      TreePath path1 = ((BinTreeTable) component).getTree().
          getPathForLocation(e.getX(), e.getY());
      int row = ((BinTreeTable) component).rowAtPoint(e.getPoint());
      TreePath path = ((BinTreeTable) component).getTree().getPathForRow(row);
//System.err.println("row: " + row);
//System.err.println("row2: " + ((BinTreeTable) component).getTree().
//                      getRowForLocation(e.getX(), e.getY()));
//      if (path != path1) {
//        System.err.println("paths doesn't match: " + path1 + " - " + path);
//      }

      if (path == null) {
        return;
      }

      ((BinTreeTable) component).getTree().setSelectionPath(path);

      UITreeNode node = (UITreeNode) path.getLastPathComponent();
      menu = createPopup(new UITreeNode[] {node}, getUI(), point);
    } else {
      int size = paths.length;
      UITreeNode[] nodes = new UITreeNode[size];
      for (int i = 0; i < size; i++) {
        nodes[i] = (UITreeNode) paths[i].getLastPathComponent();
      }

      menu = createPopup(nodes, getUI(), point);
    }

    if (menu != null) {
      menu.show(component, e.getX(),
          e.getY() - (int) menu.getPreferredSize().getHeight());
      menu.requestFocus();
    }
  }

  /*
    private final class BinActionListener implements ActionListener {
      public void actionPerformed(ActionEvent action) {
        //if (BinPanel.this.getCurrentPane().getComponent().getUI() instanceof BinTreeTable) {
        if (getUI() instanceof BinTreeTable) {
          //BinTreeTable table = (BinTreeTable) BinPanel.this.getCurrentPane().getComponent().getUI();
          BinTreeTable table = (BinTreeTable) getUI();

          TreePath[] paths = table.getTree().getSelectionPaths();
          if (paths == null || paths.length == 0) return;

          int row = table.getTree().getRowForPath(paths[paths.length - 1]);
          Rectangle rect = table.getTree().getRowBounds(row);

          Point point = SwingUtilities.convertPoint(
              table,
              (int) rect.getCenterX(),
              (int) rect.getCenterY(),
              //BinPanel.this
              getUI()
          );

          JPopupMenu popup = null;
          if (paths.length == 1) {
            UITreeNode node = (UITreeNode) paths[0].getLastPathComponent();
            popup = createPopup(new UITreeNode[] {node}, getUI(), point);
          } else {
            int size = paths.length;
            UITreeNode[] nodes = new UITreeNode[size];
            for (int i = 0; i < size; i++) {
              nodes[i] = (UITreeNode) paths[i].getLastPathComponent();
            }
            popup = createPopup(nodes, getUI(), point);
          }

          // Do not show empty PopupMenu
          if (popup != null) {
            popup.show(table, (int) rect.getCenterX(), (int) rect.getCenterY() - (int) popup.getPreferredSize().getHeight());
            popup.requestFocus();
          }
        }
      }
    }
   */

  /** Only touches tree nodes, does *not* update toolbar button toggled state */
  public void updateVisibilityOfHiddenTreeNodes() {
    if (this.component instanceof BinTreeTable) {
      TreeRefactorItContext treeContext = (TreeRefactorItContext) context;
      BinTreeTable table = (BinTreeTable)this.component;
      ParentTreeTableNode rootNode = (ParentTreeTableNode) table.
          getBinTreeTableModel().getRoot();

      rootNode.setShowHiddenChildren(treeContext.hiddenNodesVisible());
    }
  }

  private static Object getBinsFromTreeNodes(UITreeNode[] nodes) {
    if (nodes.length == 1) {
      return nodes[0].getBin();
    }

    Object[] result = new Object[nodes.length];
    for (int i = 0; i < nodes.length; i++) {
      result[i] = nodes[i].getBin();
    }

    return result;
  }

  protected JPopupMenu createPopup(
      final UITreeNode[] nodes, final Component parent, final Point point
  ) {
    if (context instanceof AWTContext) {
      Window window = ((AWTContext) context).getWindow();
      context.setPoint(SwingUtilities.convertPoint(parent, point, window));
    }

    final List customActions;
    if (this.component instanceof BinTreeTable) {
      customActions = ((BinTreeTable)this.component)
          .getApplicableActions(nodes, context);
    } else {
      customActions = Collections.EMPTY_LIST;
    }

    Object bins = getBinsFromTreeNodes(nodes);
    return createPopup(bins, customActions, parent, point);
  }

  JPopupMenu createPopup(
      Object obj, List customActions, Component parent, Point point
  ) {
    if (context instanceof AWTContext) {
      Window window = ((AWTContext) context).getWindow();
      context.setPoint(SwingUtilities.convertPoint(parent, point, window));
    }

    final JPopupMenu popup =
        createPopupForBinItem(obj, parent, context, customActions);

    if (popup.getSubElements().length > 0) {
      popup.addSeparator();
    }

    if (this.component instanceof BinTreeTable) {
      // FIXME: implement row hiding support for MultilineRowTree
      // -- for Fixme Scanner $$$
      // (look for comments ending with "$$$")

      BinTreeTable table = (BinTreeTable)this.component;

      if (table.getSelectedRow() != 0) {
        popup.add(getMenuItemForHidingRows());
      }

      popup.add(getMenuItemForCopyingTreeContents());
    }

    // Return NULL if popup contains no items
    return (popup.getSubElements().length > 0) ? popup : null;
  }

  /**
   * Acts very cimilar to ResultArea.addActionsToPopup() method (see it for
   * comments)
   */
  public static void addActionsToSubMenu(
      JMenu popup, List actions,
      final IdeWindowContext context, final Object obj,
      Font boldFont, Font plainFont
  ) {
    for (int i = 2; i < actions.size(); i++) {
      Object element = actions.get(i);
      if (element instanceof List){
        List subActions = (List) element;
        JMenu subMenu
            = new JMenu((String) subActions.get(0));
        if (((Boolean) subActions.get(1)).booleanValue()){
          subMenu.setFont(boldFont);
        } else {
          subMenu.setFont(plainFont);
        }
        addActionsToSubMenu(subMenu, subActions, context, obj, boldFont, plainFont);
        popup.add(subMenu);
      } else {
        final RefactorItAction act = (RefactorItAction) actions.get(i);

        if (act == null) {
          popup.addSeparator();
        } else {
          JMenuItem item = createMenuItem(act, context, obj);
          popup.add(item);
        }
      }
    }
  }

  public static void addActionsToPopup(
      JPopupMenu popup, List actions,
      final IdeWindowContext context, final Object obj
  ) {
    if (actions != null && actions.size() > 0) {
      Font font = popup.getFont();
      Font boldFont = new Font(font.getName(), Font.BOLD, font.getSize());
      Font plainFont = new Font(font.getName(), Font.PLAIN, font.getSize());

      JMenu moreSubMenu = null;

      for (int i = 0; i < actions.size(); i++) {
        Object element = actions.get(i);

        if (i == 19 && actions.size() - i > 3) {
          moreSubMenu = new JMenu("More...");
          popup.add(moreSubMenu);
        }

        /*
         * If list has come, we will handle it like a submenu.
         * First element in the list must be a String that contains the display
         * name for this submenu (maybe a FIXME - not a beautiful solution).
         */
        if (element instanceof List){
          List subActions = (List) element;
          // create a submenu item with name taken from the end of the list
          JMenu subMenu
              = new JMenu((String) subActions.get(0));
          if (((Boolean) subActions.get(1)).booleanValue()){
            subMenu.setFont(boldFont);
          } else {
            subMenu.setFont(plainFont);
          }

          // fill submenu with actions, contained in the list
          addActionsToSubMenu(subMenu, subActions,
              context, obj, boldFont, plainFont);
          if (moreSubMenu != null) {
            moreSubMenu.add(subMenu);
          } else {
            popup.add(subMenu);
          }
        } else {

          final RefactorItAction act = (RefactorItAction) element;

          if (act == null) {
            if (moreSubMenu != null) {
              moreSubMenu.addSeparator();
            } else {
              popup.addSeparator();
            }
          } else {
            JMenuItem item = createMenuItem(act, context, obj);
            if (moreSubMenu != null) {
              moreSubMenu.add(item);
            } else {
              popup.add(item);
            }
          }
        }
      }
    }
  }

  /**
   * Creates JMenuItem for given RefactorItAction
   */
  private static JMenuItem createMenuItem(
      final RefactorItAction act,
      final IdeWindowContext context,
      final Object obj
  ) {
    JMenuItem item = new JMenuItem(act.getName());
    if (act instanceof ReconcileActionDecorator){
      item.setEnabled(((ReconcileActionDecorator) act).isEnabled());
    }

    item.setName(act.getKey()); // to find specific action later in the menu

    /*
        item.setAccelerator( Shortcuts.getKeyStrokeByAction(
        act.getKey() ) );
     */

    ActionListener aListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onMenuClick(context, act, obj);
      }
    };
    /*
               registerKeyboardAction(aListener,
        Shortcuts.getKeyStrokeByAction(act.getKey()),
        getUI().WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     */
    item.addActionListener(aListener);

    return item;
  }

  public static void onMenuClick(IdeWindowContext context, RefactorItAction act, Object obj) {
    try {
      RefactorItContext newContext = context.copy();
      newContext.setState(null);
      if (RefactorItActionUtils.run(act, newContext, obj)) {
        act.updateEnvironment(newContext);
      } else {
        act.raiseResultsPane(newContext);
      }
    } catch(Exception ex) {
      IDEController.getInstance().showAndLogInternalError(ex);
    }
  }

  public static JPopupMenu createPopupForBinItem(
      final Object obj, final Component parent,
      final IdeWindowContext context, final List customActions
  ) {
    boolean separatorNeeded = false;
    JPopupMenu popup = new JPopupMenu();
    popup.setInvoker(parent);
    popup.setLightWeightPopupEnabled(false);

    addActionsToPopup(popup, customActions, context, obj);

    if (obj == null) {
      return popup;
    }

    if (customActions != null && !customActions.isEmpty()) {
      separatorNeeded = true;
    }

    /*
        resetKeyboardActions();
        registerKeyboardAction(new BinActionListener(),
        KeyStroke.getKeyStroke(KeyEvent.VK_M, Main.shortcutKeyMask),
          getUI().WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     */

    List actions;
    if (obj instanceof Object[]) {
      actions = ModuleManager.getActions((Object[]) obj);
    } else {
      actions = ModuleManager.getActions(obj);
    }

    if ((separatorNeeded) && (actions != null) && (!actions.isEmpty())) {
      popup.addSeparator();
      separatorNeeded = false;
    }

    addActionsToPopup(popup, actions, context, obj);

    return popup;
  }

  String getTreeContentsAsText() {
    if (component instanceof BinTreeTable) {
      return ((BinTreeTable) component)
          .getClipboardText(new PlainTextTableFormat());
    }

    return "";
  }

  private JMenuItem getMenuItemForCopyingTreeContents() {
    JMenuItem result = new JMenuItem("Copy to clipboard");
    result.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        SystemClipboard.setContents(getTreeContentsAsText());
      }
    });

    return result;
  }

  private JMenuItem getMenuItemForHidingRows() {
    JMenuItem result = new JMenuItem("Hide/Unhide");
    result.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (ResultArea.this.component instanceof BinTreeTable) {
          BinTreeTable table = (BinTreeTable) ResultArea.this.component;
          TreeRefactorItContext treeContext = (TreeRefactorItContext) context;

          if (!treeContext.selectedNodeCanBeHidden(table.getTree())) {
            return;
          }

          treeContext.getResultsTreeDisplayState()
              .saveExpansionAndScrollState(table.getTree());

          treeContext.hideSelectedRow(table.getTree());

          treeContext.getResultsTreeDisplayState()
              .restoreExpansionAndScrollState(table,
              ResultArea.this.context.getProject());
        } else if (ResultArea.this.component instanceof MultilineRowTree) {
          MultilineRowTree tree = (MultilineRowTree) ResultArea.this.
              component;
          TreeRefactorItContext treeContext = (TreeRefactorItContext) context;

          if (!treeContext.selectedNodeCanBeHidden(tree)) {
            return;
          }

          treeContext.getResultsTreeDisplayState()
              .saveExpansionAndScrollState(tree);

          treeContext.hideSelectedRow(tree);

          treeContext.getResultsTreeDisplayState()
              .restoreExpansionAndScrollState(tree,
              ResultArea.this.context.getProject());
        }
      }
    });

    return result;
  }

  public void showHiddenRows() {
    // FIXME: row hiding should also be implented some day
    // for MultlineRowTree (FIXME Scanner) $$$

    if (component instanceof BinTreeTable) {
      ((TreeRefactorItContext) context)
          .showHiddenRows(((BinTreeTable) component).getTree());
    }
  }

  public void hideHiddenRows() {
    // FIXME: row hiding should also be implented some day
    // for MultlineRowTree (FIXME Scanner) $$$

    if (this.component instanceof BinTreeTable) {
      ((TreeRefactorItContext) context)
          .hideHiddenRows(((BinTreeTable) component).getTree());
    }
  }

  /**
   * FIXME: Currently it registers the keys to component object this instance
   * of ResultArea holds.
   */
  /*private void registerKeys(final Object obj, final Component parent) {
    resetKeyboardActions();
    registerKeyboardAction(new BinActionListener(),
        KeyStroke.getKeyStroke(KeyEvent.VK_M, Main.shortcutKeyMask),
        //this.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        getUI().WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    List actions = null;
    if (obj instanceof Object[]) {
      actions = ModuleManager.getActions((Object[]) obj);
    } else {
      actions = ModuleManager.getActions(obj);
    }
    if (actions == null || actions.size() == 0) return;

    for (int i = 0; i < actions.size(); i++) {
      final RefactorItAction act = (RefactorItAction) actions.get(i);

      ActionListener aListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          // FIXME: into run() function must be provided a new Context() object
          // if the run is made on new target BinXXX object. Currently it is not
          // new.
          context.setState(null);
          act.run(context, parent, obj);
        }
      };

      registerKeyboardAction(aListener,
          Shortcuts.getKeyStrokeByAction(act.getKey()),
          //this.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
          getUI().WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }
     }*/

  /**
   * Shortcut method, part of refactoring to register keyboard actions to
   * this.component
   */
  /*public void registerKeyboardAction(ActionListener anAction,
                                     KeyStroke aKeyStroke,
                                     int aCondition) {
    this.component.registerKeyboardAction(anAction, aKeyStroke, aCondition);
     }*/

  /**
   * Shortcut method, part of refactoring to register keyboard actions to
   * this.component
   */
  public void resetKeyboardActions() {
    this.component.resetKeyboardActions();
  }

  /**
   * It is called by this instance when the rebuild is performed on Project
   * this instance holds. It is required to fire this event, so the listeners
   * can take appropriate actions against it (for example, to free this object
   * and recreate a new one with updated results).
   *
   * The listeners are notified on this event, who
   * are registered themselves via addResultAreaListener(...) method.
   */
  void fireRebuildPerformedEvent() {
    // get the list of listeners and notify each one about this event
    ArrayList listeners = getResultAreaListeners();
    for (int i = 0; i < listeners.size(); i++) {
      ResultAreaListener listener = (ResultAreaListener) listeners.get(i);
      listener.rebuildPerformed(this);
    }
  }

  /**
   * It is called by this instance when the rebuild is started on Project
   * this instance holds. i.e. it is in the start phase in project rebuild.
   * It is required to fire this event, so the listeners can take appropriate
   * actions against it (for example, to free the resources they hold against
   * the old project generation).
   *
   * The listeners are notified on this event, who
   * are registered themselves via addResultAreaListener(...) method.
   */
  void fireRebuildStartedEvent() {
    // get the list of listeners and notify each one about this event
    ArrayList listeners = getResultAreaListeners();
    for (int i = 0; i < listeners.size(); i++) {
      ResultAreaListener listener = (ResultAreaListener) listeners.get(i);
      listener.rebuildStarted(this);
    }
  }

  /**
   * It is called by this instance when the content (JComponent) is changed/
   * replaced by another one. So, the listeners are notified on this event, who
   * are registered themselves via addResultAreaListener(...) method.
   */
  private void fireContentChangedEvent() {

    // get the list of listeners and notify each one about this event
    ArrayList listeners = getResultAreaListeners();
    for (int i = 0; i < listeners.size(); i++) {
      ResultAreaListener listener = (ResultAreaListener) listeners.get(i);
      listener.contentChanged(getUI());
    }
  }

  /**
   * Returns all listeners for this instance of ResultArea.
   *
   * Return a list containing all listeners who are registered themselves
   * through addResultAreaListener(...) method for notifications about
   * this instance events.
   *
   * @return ArrayList of all listeners to be notified on events.
   */
  private ArrayList getResultAreaListeners() {
    return this.listeners;
  }

  /**
   * Adds a new listener to be notified for this instance events.
   *
   * @param listener the target listener instance to be notified on events.
   */
  public void addResultAreaListener(ResultAreaListener listener) {
    if (listener != null) {
      getResultAreaListeners().add(listener);
      // register this instance of ResultArea as ProjectChangeListener
      // it deregisters itself in removeResultAreaListener() function
      // if there is no more ResultAreaListeners left.
      // So, register itself only if there is at least one listener added
      // into listeners list.
      if ((this.isListeningProjectChanges == false) &&
          (this.listeners.size() != 0)) {
        this.context.getProject().getProjectLoader().addProjectChangedListener(this);
        this.isListeningProjectChanges = true;
      }
    }
  }

  /**
   * Removes the specified listener from the list of target listeners, so
   * after it the specified target doesn't get any notifications about this
   * instance events.
   *
   * @param listener to be removed from targets list.
   */
  public void removeResultAreaListener(ResultAreaListener listener) {
    if (listener != null) {
      getResultAreaListeners().remove(listener);
      // also remove itself from ProjectChangedListener if there is no more
      // ResultArea198Listener's left.
      if ((this.isListeningProjectChanges == true) &&
          (this.listeners.size() == 0)) {
        this.context.getProject().getProjectLoader().removeProjectChangedListener(this);
        this.isListeningProjectChanges = false;
        // so, nobody listens anymore this instance of ResultArea.
        // we set the content to "null" to free resources.
        this.setContent(null);
      }
    }
  }

  /**
   * This function is called when the project rebuild is in the start phase. No actions are
   * yet executed to rebuild a new project generation.
   *
   * Implement this function to release any resources that you hold in on the old project
   * generation.
   *
   * @param project the old project generation that is going to be replaced by a new one by
   * this function caller.
   */
  public void rebuildStarted(final Project project) {
    //System.out.println("Rebuild started event!");

    try {
      SwingUtil.invokeAndWaitFromAnyThread(new Runnable() {
        public void run() {
          if (context instanceof TreeRefactorItContext) {
            saveTreeExpansionAndScrollState((TreeRefactorItContext) context);
          }

          // FIXME: Graying not supported for MultilineRowTrees (FIXME Scanner has that tree). $$$

          // set the another JComponent to notify user that rebuild has been started
          if (ResultArea.this.component instanceof BinTreeTable) {
            setContent(
                ((BinTreeTable) ResultArea.this.component).createGrayCopy(context));
          } else if (ResultArea.this.component instanceof GraphPanel) {
            // TODO make graph gray
            if (ResultArea.this.component instanceof GraphPanel) {
              ((GraphPanel) ResultArea.this.component).saveNodePoints();
            }
//            setContent(new JLabel(
//                "ReBuild performed, click on 'Run again' button to refresh results."));
          } else {
            setContent(new JLabel(
                "ReBuild performed, click on 'Run again' button to refresh results."));
          }

          // also, fire rebuild started event to notify the listeners of this
          // instance of ResultArea so they can take appropriate actions against it.
          // for example to free resources that they hold against the old project generation.
          fireRebuildStartedEvent();
        }
      });
    } catch (Exception ignore) {}
  }

  /**
   * This function is called when rebuild has been performed on the whole
   * project.
   *
   * After rebuild to the project, all listeners are notified on that
   * event by this function. The rebuild means that source files that has
   * been changed are parsed by the Parser and CompilationUnit array (the Project
   * holds it) is rebuilt. So, any references you have queried from Project
   * object for CompilationUnit objects has been freed by Project itself.
   * So, you have to free these references to objects and redisplay any
   * references to these objects using new Project CompilationUnit objects.
   *
   * @param project the project on what the rebuild was performed.
   */
  public void rebuildPerformed(Project project) {
    //System.out.println("Rebuild performed event!");

    try {
      SwingUtil.invokeAndWaitFromAnyThread(new Runnable() {
        public void run() {
          // also, fire rebuild performed event to notify the listeners of this instance
          // of BinCompoent so they can take appropriate actions against it. For example
          // to free some resources.
          fireRebuildPerformedEvent();
        }
      });
    } catch (Exception ignore) {
    }
  }

  /**
   * Asks this instance of ResultArea to refresh its content(JComponent).
   * I.e. to perform "ReRun". The listeners of this instance usually are
   * calling this function. BinPane is one of them.
   *
   * @param requester is some object of ResultAreaListener who
   * requests this instance of ResultArea to refresh itself.
   * I.e. BinPane is such kind of ResultAreaListener.
   */
  public void refreshContent(ResultAreaListener requester) {
    // Check whether the reference to target is null.
    // If it is null then we cannot query the real BinXXX object
    // from target BinItemReference object.
    // See BinReference class for more information.
    BinItemReference targetObject = null;

    RefactorItContext context = getComponentContext();

    // this is for DrawDependencies to get a new list of targets
    // TODO: this all targets and states system must be fixed/refactored to use just states
//    if (context != null && context.getState() instanceof BinItemReference) {
//      targetObject = (BinItemReference) context.getState();
//    }

    if (targetObject == null) {
      targetObject = getTargetBinObjectReference();
    }

    if (targetObject == null) {
      String message = "Cannot Rerun on this result!";
      requester.errorOccurred(message);
      return;
    }

    // Query targetObject of class BinItemReference for real BinXXX
    // object. We need it before we can execute run(...) function
    // on action.
    Object target = targetObject.restore(context.getProject());
    if (target == null) {
      String message = "Cannot rediscover the target. Cannot ReRun!";
      requester.errorOccurred(message);
      return;
    }

    // The action in current situation can be null, because we
    // haven't changed all of them currently.
    // Remove this if(...) statement if it cannot be null any more.
    RefactorItAction action = getAction();
    if (action == null) {
      String message = "Cannot ReRun this action. (action == null) !";
      requester.errorOccurred(message);
      return;
    }

    // Replace content of ResultArea.
    // And notify listeners about content changes, so they can
    // refresh themselves.
    JLabel reRunLabel = new JLabel("Performing ReRun!");
    if (!(ResultArea.this.component instanceof GraphPanel)) {
      this.setContent(reRunLabel);
    }

    // and finally ReRun the action
    // We provide the old context object into run, because we want
    // to refresh the old results. Check the javadoc spec. of
    // RefactorItAction.run(...) method.
    //
    // FIXME: (Jaanek) Also, YES I know, it is a bad practise
    // to cast ResultAreaListener to (BinPane) here but as ReRun
    // functionality fits better to ResultArea then now I have
    // no better/fastest way to do it in other way.
    //
    // SOLUTION: (Jaanek) One way to fix this FIXME would be to
    // notify the "requester" of refresh() by calling for example
    // runAction(...) on requester interface and providing needed
    // components to it. So the requester themselves can call
    // run() on action.

    if (ResultArea.this.component instanceof GraphPanel) {
      ((GraphPanel) ResultArea.this.component).rebuild((List) target);
      action.raiseResultsPane(context);
    } else {
      if (RefactorItActionUtils.run(action, context, target)) {
        action.updateEnvironment(context);
      } else {
        action.raiseResultsPane(context);
      }
    }
  }

  ///////////// Scroll and expansion state savers

  public void saveTreeExpansionAndScrollState(TreeRefactorItContext context) {
    try {
      if (component instanceof BinTreeTable) {
        context.reset(); // clear hidden rows list before saving
        context.getResultsTreeDisplayState().saveExpansionAndScrollState(
            ((BinTreeTable) component).getTree());
        context.saveRowHideState(((BinTreeTable) component)
            .getBinTreeTableModel());
      } else if (component instanceof MultilineRowTree) {
        context.getResultsTreeDisplayState()
            .saveExpansionAndScrollState((MultilineRowTree) component);
        context.saveRowHideState(((MultilineRowTree) component).getModel());
      }
      
      if (component instanceof AuditTreeTable){
        context.getResultsTreeDisplayState().saveActiveColumnState(
            (AuditTreeTable) component);
      }
    } catch (Exception e) {
      System.err.println("EXCEPTION -- PLEASE REPORT");
      e.printStackTrace();
    }
  }

  private void restoreTreeExpansionAndScrollState(TreeRefactorItContext context) {
    try {
      if (component instanceof AuditTreeTable){
        context.getResultsTreeDisplayState().restoreActiveColumnState(
            (AuditTreeTable) component);
      }
      
      if (component instanceof BinTreeTable) {
        BinTreeTable table = (BinTreeTable) component;
        context.restoreRowHideState(table.getTree());
        context.getResultsTreeDisplayState()
            .restoreExpansionAndScrollState(table, context.getProject());
      } else if (component instanceof MultilineRowTree) {
        MultilineRowTree tree = (MultilineRowTree) component;
        context.restoreRowHideState(tree);
        context.getResultsTreeDisplayState()
            .restoreExpansionAndScrollState(tree, context.getProject());
      }
    } catch (Exception e) {
      System.err.println("EXCEPTION -- PLEASE REPORT");
      e.printStackTrace();
    }
  }
}
