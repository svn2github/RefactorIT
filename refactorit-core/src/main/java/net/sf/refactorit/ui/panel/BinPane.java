/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.panel;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.standalone.BrowserContext;
import net.sf.refactorit.standalone.JBrowserPanel;
import net.sf.refactorit.ui.JErrorDialog;
import net.sf.refactorit.ui.ParsingMessageDialog;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.ui.tree.BinTree;
import net.sf.refactorit.ui.tree.PackageTreeModel;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.utils.ParsingInterruptedException;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


/**
 * It shows/contains component to scroll.
 *
 * @author Anton Safonov
 * @author Jaanek Oja
 */
public class BinPane extends JScrollPane implements ResultAreaListener {
  private static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(UIResources.class, "LocalizedStrings");

  // The component for holding the results of refactoring
  private ResultArea component;

  // The toolbar for this instance of BinPane
  private BinPaneToolBar toolbar = null;

  // creates the "rerun" button to be displayed on the toolbar.
  private JButton reRunButton = null;

  private List componentChangeListeners = new ArrayList();

  /**
   * Create BinPane.
   */
  public BinPane() {
    super();
    getViewport().setBackground(UIManager.getColor("Table.background"));
    setUpScrollbarPolicies();

    // creates a brand new toolbar for this intance of BinPane
    this.toolbar = createToolbar();
  }

  /**
   * Creates a brand new ToolBar for this BinPane
   *
   * @return {@link BinPaneToolBar}, a brand new Toolbar.
   */
  private BinPaneToolBar createToolbar() {
    // FIXME: here should be default from Options
    // boolean singleTabSelected = true;
    // create a new ToolBar for this instance of BinPane

    BinPaneToolBar toolBar = new BinPaneToolBar(this);
    // initially add "Rerun" button into toolbar for this BinPane.
    // call it here because now the "rerun" information is available.
    toolBar.addToolbarButton(getRerunButton());
    return toolBar;
  }

  /**
   * Return the toolbar for this instance of BinPane.
   */
  public BinPaneToolBar getToolbar() {
    return this.toolbar;
  }

  /**
   * Sets the toolbar for this instance of BinPane.
   *
   * @param toolBar to be set.
   */
  public void setToolBar(BinPaneToolBar toolBar) {
    this.toolbar = toolBar;
  }

  /**
   * Sets the result component to show to the user.
   *
   * @param another component to set for this instance of BinPane. If component
   * specified is null, then ...
   */
  public void setComponent(ResultArea another) {
    doSetComponent(another);

    for(Iterator i = componentChangeListeners.iterator(); i.hasNext(); ) {
      ((ComponentChangeListener) i.next()).componentChanged(this);
    }
  }

  /**
   * @param another
   */
  private void doSetComponent(final ResultArea another) {
    // if another component is provided in, then we must update
    // our BinPaneToolBar because it holds information on old component.
    if (this.toolbar != null) {
      // remove all abstract buttons that were added by the users of this instance of BinPane.
      // it is needed to release resources that these abstract buttons may hold in shape of
      // references they have against old ResultArea. For example, indirect references to
      // classmodel.Bin* objects.
      this.toolbar.removeAddInButtons();
    }

    // before setting a new ResultArea object for this instance of
    // BinPane remove the current one. This remove... call also removes
    // this instance as a listener for old component.
    if (this.component != null) {
      this.component.removeResultAreaListener(this);
      setViewportView(null);
      this.component = null;
    }

    // register this instance for events inside this new ResultArea
    if (another != null) {
      this.component = another;
      setViewportView(this.component.getUI());

      this.component.getUI().requestDefaultFocus();

      // add "Rerun" button into toolbar for this BinPane, because it is removed
      // by call to removeAddInButtons(..)
      this.toolbar.addToolbarButton(getRerunButton());

      this.toolbar.setUnhideButtonEnabled(
          this.component.getUI() instanceof BinTree
          || this.component.getUI() instanceof BinTreeTable);

      //
      BinPanel previousToolbarListener = this.toolbar.getToolBarListener();
      this.toolbar.setToolbarListener(previousToolbarListener);
      //
      this.component.addResultAreaListener(this);

      setUpScrollbarPolicies();
    }
  }

  private void setUpScrollbarPolicies() {
    if (this.component == null || !(this.component.getUI() instanceof JTree)
        && !(this.component.getUI() instanceof JTable)) {
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    } else {
      setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
  }

  public ResultArea getComponent() {
    return this.component;
  }

  public void requestFocus() {
    // super.requestFocus();
    if (this.component != null) {
      this.component.getUI().requestFocusInWindow();
    }
  }

  /**
   * Returns the "Rerun" button.
   *
   * "Rerun" button calls the refresh() function on ResultArea it holds
   * to trigger the refresh on ResultArea.
   */
  private AbstractButton getRerunButton() {
    // create the "rerun" button to be displayed on the toolbar.
    this.reRunButton = new JButton(UIResources.getRefreshIcon());
    this.reRunButton.setToolTipText(resLocalizedStrings.getString(
        "button.rerun.tooltip"));

    // Add actionListener to this button, so the user can invoke the
    // refresh for old data.
    this.reRunButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        // notify the ResultArea this instance of BinPane holds
        // that it is requested to refresh itself
        ResultArea component = BinPane.this.getComponent();
        if (component == null) {
          String message = "Cannot Rerun, result area is null!";
          RitDialog.showMessageDialog(
              IDEController.getInstance().createProjectContext(),
              message, "Information",
              JOptionPane.INFORMATION_MESSAGE);
          return;
        }

        if (component.getComponentContext() instanceof TreeRefactorItContext) {
          component.saveTreeExpansionAndScrollState((TreeRefactorItContext)
              component.getComponentContext());
        }

        // the following function call is needed to make the AST
        // up to date with sources. It forces to rebuild the AST
        // if needed, so, the actions take their values from up to
        // date data.
        boolean successful = validateProject();

        // if validated successfully, then now we can execute refresh
        // P.S. Right hand expression in IF condition is needed because
        // Error tab doesn't need refreshing its content. It is removed each time
        // the project is rebuild (and Errors tab BinPanel has its action set to
        // null).
        // So refresh content only if validation was successful and ResultArea
        // has an action associated with it.
        if (successful && component.hasAction()) {
          BinPanel.setReloadPane(BinPane.this);
          BinPanel.getBinPanelManager().moveToLast((BinPanel) getParent());
          try {
            component.refreshContent(BinPane.this);
          } finally {
            BinPanel.setReloadPane(null);
          }
        }
      }
    });

    // return it.
    return this.reRunButton;
  }

  public void invokeReRun() {
    this.reRunButton.doClick();
  }

  /**
   * It is called when the JComponent inside ResultArea is replaced with
   * another one. So, the container who is responsible for displaying
   * it can be notified on this event.
   *
   * @param currentContent a new one JComponent instance that was replaced
   */
  public void contentChanged(JComponent currentContent) {
    setViewportView(currentContent);
    currentContent.requestDefaultFocus();
  }

  /**
   * It is called when processing error occurs inside ResultArea.
   *
   * For example when ReRun is in process, or when the refresh doesn't
   * succeed.
   *
   * @param error JComponent instance that liteners should show to the user in
   * that case.
   */
  public void errorOccurred(String error) {
    RitDialog.showMessageDialog(
        IDEController.getInstance().createProjectContext(),
        error, "Info", JOptionPane.INFORMATION_MESSAGE);
    return;
  }

  /**
   * It is called when a new project generation is going to be rebuilt by the
   * Project instance.
   *
   * Implement this function to release any resources you have against the old
   * project generations. For example, release direct/indirect references to
   * old project, so it could be garbage collected.
   *
   * @param component which notifies about the start of a new project generation.
   */
  public void rebuildStarted(ResultArea component) {
    BinPaneToolBar toolbar = this.getToolbar();
    // remove all abstract buttons
    // that were added by the users of this instance of BinPane.
    // it is needed to release resources that these abstract buttons
    // may hold in shape of references they have. For example, indirect references
    // to classmodel.Bin* objects.
    toolbar.removeAddInButtons();
  }

  /**
   * It is called when the ResultArea detects that project has been rebuilt.
   *
   * @param component this is the component this instance of BinPane has been
   * registered itself as a listener for.
   */
  public void rebuildPerformed(ResultArea component) {
    BinPaneToolBar toolbar = this.getToolbar();

    // add "Rerun" button into toolbar for this BinPane, because it was removed
    // inside the function call to rebuildStarted(..)
    toolbar.addToolbarButton(getRerunButton());
  }

  /**
   * Makes the AST up to date with sources if sources have been changed
   * meanwhile.
   *
   * Actually it would be better if this function be somehow called by
   * {@link net.sf.refactorit.classmodel.Project} itself, so we just call Project.verify() and this
   * instance of BinPane be just listener if project changed.
   *
   * @return boolean, if project was succeeded to validate or false if
   * some error occurred while validating a project.
   */
  boolean validateProject() {
    // FIXME The following code fragment is copied from
    // {@link net.sf.refactorit.jdev.RefactorItController.prepareProject(..)}
    // it needs Refactoring (all these references to one place). Smells badly.
    try {
      // FIXME it should call saveAll() of IDE somewhere probably here!

      RefactorItContext context = getComponent().getComponentContext();

      ParsingMessageDialog dlg = new ParsingMessageDialog(context);
      dlg.setDialogTask(new ParsingMessageDialog.RebuildProjectTask(context.
          getProject()));

      TreeModel oldModel = null;
      if (context instanceof BrowserContext) {
        oldModel = JBrowserPanel.lastInstance.getTree().getModel();
        JBrowserPanel.lastInstance.getTree().rebuild(PackageTreeModel.EMPTY);
      }

      try {
        dlg.show(true);
      } catch (ParsingInterruptedException ex) {
        return false;
      }
      //JRefactorItFrame.hack.startParsing(false);
      //context.reload();
      if (context instanceof BrowserContext) {
        if (oldModel == null) {
          oldModel = new PackageTreeModel(context.getProject(), "Project");
        } else {
          ((PackageTreeModel) oldModel).rebuild();
        }
        JBrowserPanel.lastInstance.getTree().setModel(oldModel);
      }

      if (context.getProject().getProjectLoader().getErrorCollector().hasUserFriendlyErrors()) {
        ErrorsTab.addNew(context);
      }

      Exception e = dlg.getException();
      if (e != null) {
        JErrorDialog err = new JErrorDialog(context, "Error");
        err.setException(e);
        err.show();
        AppRegistry.getExceptionLogger().error(e, this);
        // the project wasn't suceeded to validate.
        return false;
      }
    } finally {
      //classpath.release();
    }

    // the project was succeeded to validate.
    return true;
  }

  void clear() {
    if (this.toolbar != null) {
      this.toolbar.clear();
      this.toolbar = null;
    }
    if (this.component != null) {
      setComponent(null);
    }
  }

  public static interface ComponentChangeListener {
    void componentChanged(BinPane source);
  }

  public void addComponentChangeListener(ComponentChangeListener l) {
    componentChangeListeners.add(l);
  }
  
  public void setRerunEnabled(boolean enabled) {
    reRunButton.setEnabled(enabled);
  }
}
