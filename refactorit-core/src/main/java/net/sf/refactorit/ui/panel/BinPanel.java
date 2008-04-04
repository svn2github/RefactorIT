/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.panel;


import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.OptionsChangeListener;
import net.sf.refactorit.ui.TunableComponent;
import net.sf.refactorit.ui.help.HelpViewer;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.TreeRefactorItContext;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Calls RefactorItContext.addTab and showTab to show itself.
 *
 * It contains ToolBar and BinPane components.
 * BinPane contains JComponent.
 * Also this class is responsible for the treatment of its toolbar buttons.
 *
 * @author Anton Safonov
 * @author Jaanek Oja
 */

public class BinPanel extends JPanel implements OptionsChangeListener {
  // This is a static intance of BinPanelManager, used by BinPanels.
  static BinPanelManager manager = new BinPanelManager();

  private static BinPane reloadPane;

  // The context what was used to add this panel instance to the IDE
  // and what is used to remove this instance from the IDE.
  private IdeWindowContext context;

  // The IDE component object that is representing this instance of BinPanel
  // in the IDE. This is used to remove this instance of BinPanel from the IDE.
  private Object ideComponent;

  // to differ panels from IDE and from Browser ???
  private BinPanelKey panelKey;

  // it holds the current (active) BinPane in this instance of BinPanel.
  // this currentPane is also in the list of panes.
  private BinPane currentPane = null;
  private List panes = new ArrayList();

  private boolean removed = false;

  private EscapeListener escapeListener = new EscapeListener();

  public static void setReloadPane(BinPane pane) {
    reloadPane = pane;
  }

  /**
   * Panel factory - creates an appropriate panel with given component
   * or adds a pane to an existing panel (depends on reusability checkbox)
   * @param context action context
   * @param name usually module action name
   * @param component BinTreeTable or JTree etc
   */
  public static final BinPanel getPanel(
      RefactorItContext context, String name, JComponent component
  ) {
    // Provide "null" for target Bin object. Because we do not have it now.
    // The "rerun" button notifies about it on action.
    return getPanel(context, name, ResultArea.create(component, context, null));
  }

  /**
   * Searches for BinPanel by provided "panelName" and if found then
   * returns it (or if not found then return newly created BinPanel)
   * with newly created BinPane holding the {@link net.sf.refactorit.ui.panel.ResultArea}.
   * @param panelName the name of panel.
   * @param result the result to show to the user.
   */
  public static final BinPanel getPanel(
      IdeWindowContext context, String panelName, ResultArea result
  ) {
    BinPanel aPanel = getPanelImpl(context, panelName, result);

    if (aPanel.isRemoved()) {
      aPanel.getContext().addTab(panelName, aPanel);
    }

    return aPanel;
  }

  private static final BinPanel getPanelImpl(
      IdeWindowContext context, String panelName, ResultArea result
  ) {
    if (reloadPane != null) {
      // "ReRun" was performed and we return the same BinPanel back.

      BinPane pane = reloadPane;
      reloadPane = null;

      // we ask BinPanel manager whether the BinPane
      // is in group of "panelName" of BinPanels.
      if (manager.isInPanelsGroup(context, panelName, pane)) {
        pane.setComponent(result);

        BinPanel panel = (BinPanel) pane.getParent();
        panel.reload();

        return panel;
      }
    }

    BinPanel panel = manager.getFirstPanelInGroup(context, panelName);

    // If panel is not "null" then there was a first BinPanel in the list
    // of that group of BinPanels, so check the "reusable" condition and
    // add ...
    if (panel != null) {
      //System.out.println("Didn't get the first panel from group of !"+panelName);
      if (panel.isSingleTabOption()) {
        //System.out.println("Panel is reusable!");
        panel.createPane(result);
        panel.reload();
        return panel;
      }
    }

    // If panel is "null" (then there were no BinPanel's in that group
    // of BinPanels) or if the first BinPanel in that group wasn't
    // "reusable" , then in that case we make a new BinPanel and add it
    // into BinPanel manager to hold it, and return it to the caller.
    panel = new BinPanel(result.getComponentContext(), panelName);
    manager.addPanel(panel);
    panel.createPane(result);
    // Add this panel to IDE context, so it appears int IDE tab for example
    // panel.addToIDEContext(result.getComponentContext());
    panel.reload();
    return panel;
  }

  /**
   * This one called through panel factory
   */
  protected BinPanel(IdeWindowContext context, String name) {
    super();

    setLayout(new BorderLayout());
    setMinimumSize(new Dimension(0, 0));
    setName(name);

    this.context = context;

    // FIXME: set it back
    /*
         registerKeyboardAction(actListener,
      KeyStroke.getKeyStroke(KeyEvent.VK_M, Main.shortcutKeyMask),
      this.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     */

    try {
      registerKeyboardAction(new EscapeActionListener(),
          KeyStroke.getKeyStroke("ESCAPE"),
          WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    } catch (Exception e) {
      // failed to register, let's live without
    }
    addKeyListener(escapeListener);

    this.panelKey = BinPanelKey.create(context, name);

    GlobalOptions.registerOptionChangeListener(this);
  }

  /**
   * Returns the RefactorItContext object throught what this instance of
   * BinPanel was put into the IDE.
   *
   * @return RefactorItContext for this Panel instance.
   */
  public IdeWindowContext getContext() {
    return this.context;
  }

  /**
   * Returns the IDE component object into where this instance of
   * BinPanel was put when this insance of BinPanel was added into IDE.
   *
   * @return Object of IDE component representing the area that is displayed
   * to the user.
   */
  public Object getIDEComponent() {
    return this.ideComponent;
  }

  /**
   * It sets a IDE Component for this instance of BinPanel.
   * WARNING! It is used only by BinPanelManager. Do not call it directly.
   *
   * @param ideComponent to set for this instance of BinPanel.
   */
  public void setIDEComponent(Object ideComponent) {
    this.ideComponent = ideComponent;
  }

  /**
   * JComponent removeNotify() implementation. Releases all panes associated
   * with this panel.
   *
   **/
  /*
   THIS WAS CAUSING PROBLEMS BECAUSE IT IS CALLED BOTH WHEN WINDOW IS CLOSED AND WHEN Edit tab is changed to Debug
   *
   *then put it back because without problems were worse
   *
   *need to check how it was before tanels changes
   */
  /*
   public void removeNotify() {

    super.removeNotify();
    if (!isRestructuring) {
      while (!removeCurrentPane());
    }
     }
   */

  public void removeNotify() {
    if (removed) {
      return;
    }

    GlobalOptions.unregisterOptionChangeListener(this);
    removeKeyListener(this.escapeListener);

    this.removed = true;
    super.removeNotify();
  }

  public void addNotify() {
    this.removed = false;
    super.addNotify();

    GlobalOptions.registerOptionChangeListener(this);
    addKeyListener(this.escapeListener);
  }

  public static void removeAllPanels() {
    try {
      manager.removeAllPanels();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void requestFocus() {
    //		super.requestFocus();

    if (getCurrentPane() != null) {
      getCurrentPane().requestFocus();
    } else {
      //open.requestFocus();
    }
  }

  public boolean isRemoved() {
    return removed;
  }

  public void setRemoved(boolean to) {
    removed = to;
  }

  /**
   * Reloads all the visible parts of this panel.
   *
   * If something has been added into the panel meanwhile, for example
   * some button, then it rearranges or paints it as needed.
   */
  public void reload() {
    if (getCurrentPane() == null) {
      return;
    }

    // first remove old pane
    //remove(currentPane.getToolbar());
    //remove(currentPane);

    // next, make the BACK, NEXT buttons correct. FIXME: kind a HACK
    // set the toolbar listener
    shiftCurrentPaneBy(0);

    //BinPane currentPane = getCurrentPane();
    //currentPane.getToolbar().setToolbarListener(this);

    Object obj = this.getIDEComponent();
    if (obj instanceof BinPanel) {
      BinPanel bp = (BinPanel) obj;
      if (bp.getName().equals("Errors")) {
        return; // skip this selection
      }
    }

    //FIXME: if the foloowing line is called, it partly locks JDeVeloper
    //getCurrentPane().requestFocus();
    this.getContext().showTab(obj);
    this.setVisible(true);

    // not sure that all of them are needed
    invalidate();
    validate();
    repaint();
  }

  /**
   * Inserts/Sets given component to the current pane
   *
   * @param component to insert, e.g. {@link net.sf.refactorit.ui.treetable.BinTreeTable} or
   * {@link javax.swing.JTree}
   */
  public void reload(final JComponent component) {

    //component.setNextFocusableComponent(this.optionsButton);

    component.addKeyListener(new EscapeListener());

    // Sets a new JComponent for current pane's ResultArea object.
    if (context instanceof TreeRefactorItContext) {
      getCurrentPane().getComponent().saveTreeExpansionAndScrollState((
          TreeRefactorItContext) context);
    }
    getCurrentPane().getComponent().setContent(component);

    reload();
  }

  /**
   * Creates a new BinPane object
   */
  private BinPane createPane(final ResultArea component) {
    // provided this component as parent to BinPane.
    final BinPane pane = new BinPane();
    pane.setComponent(component);
    addPane(pane);
    setCurrentPane(pane);
    return pane;
  }

  public void setFilterActionListener(final ActionListener listener) {
    getCurrentPane().getToolbar().setFilterActionListener(listener);
    getCurrentPane().getToolbar().setFilterButtonEnabled(true);
  }

  public void addToolbarButton(AbstractButton button) {
    getCurrentPane().getToolbar().addToolbarButton(button);
    //reload();
  }

  private AbstractButton getHelpButton() {
    return getCurrentPane().getToolbar().getHelpButton();
  }

  /**
   * Sets default help support for this instance of BinPanel.
   * The help button is added to the ToolBar and the help topic
   * as specified with parameter value is associated with that
   * help button.
   *
   * @param helpTopic the default topic to show to the user if
   * user clicks on help button that is on toolbar.
   */
  public void setDefaultHelp(final String helpTopic) {
    // Registers help button for panel, so if
    // user clicks on it, the help is displayed for the user
    if (helpTopic == null || helpTopic.length() == 0) {
      return;
    }

    AbstractButton helpButton = this.getHelpButton();
    this.addToolbarButton(helpButton);

    HelpViewer.addHelpShowActionListener(context, helpButton, helpTopic);
  }

  /**
   * Collapses the panels that belong to the group of this instance
   * of BinPanel.
   */
  private void collapsePanels() {
    //System.out.println("collapsePanels() in BinPanel!");
    BinPanel current = manager.collapsePanels(this.getKey());
    current.reload();
  }

  /**
   * Expands the panels that belong to the group of this instance
   * of BinPanel.
   */
  private void expandPanes() {
    //System.out.println("expandPanes() in BinPanel!");
    BinPanel current = manager.expandPanes(this.getKey());
    current.reload();
  }

  void setSingleTabOption(final boolean selected) {
    GlobalOptions.setOption(
        "singleTab." + StringUtil.replace(getName(), " ", ""),
        selected ? "true" : "false");
    GlobalOptions.save();

    if (selected) {
      this.collapsePanels();
    } else {
      if (panes.size() > 1) {
        this.expandPanes();
      }
    }
  }

  boolean isSingleTabOption() {
    return IDEController.runningEclipse()
        || "true".equals(GlobalOptions.getOption(
        "singleTab." + StringUtil.replace(getName(), " ", ""), "true"));
  }

  /**
   * Adds a new pane into the list of this panes of this instance of BinPanel.
   *
   * @param pane to be added into the list of panes.
   */
  public void addPane(BinPane pane) {
    this.panes.add(pane);

    // if this instance of BinPanel doesn't have yet the current pane
    // then set this first pane to be as current pane.
    if (getCurrentPane() == null) {
      setCurrentPane(pane);
    }
    // set this instance of BinPanel to listen the pane's toolbar
    pane.getToolbar().setToolbarListener(this);
    //reload();
  }

  /**
   * Moves the pane this instance of BinPanel should hold into the
   * panel as sepcified in function arguments.
   *
   * @param pane the source pane this isntance of BinPanel should hold that
   * must to be moved.
   * @param targetPanel into where to move the specified pane that this
   * intance of BinPanel should hold.
   * @return boolean, true if the pane existed in this instance of BinPanel
   * and it could move it to target panel, otherwise false.
   */
  public boolean movePaneTo(BinPane pane, BinPanel targetPanel) {
    // remove the specifed pane from this instance of BinPanel if it
    // exists.
    boolean existedAndRemoved = this.panes.remove(pane);
    if (existedAndRemoved) {
      if (pane == getCurrentPane()) {
        // remove all pane components (pane itself and its toolbar)
        // from this instance of BinPanel.
        removeAll();
      }
      pane.getToolbar().setToolbarListener(null);
      targetPanel.addPane(pane);
    }
    return existedAndRemoved;
  }

  /**
   * Removes the specified pane from the list of panes of this instance
   * of BinPanel.
   *
   * @param pane we want to remove from the list of panes.
   * @return boolean, true if the specified pane existed inside of this instance
   * of BinPanel and it was removed, otherwise false.
   */
  private boolean removePane(BinPane pane) {
    if (pane == getCurrentPane()) {
      // remove all pane components (pane itself and its toolbar)
      // from this instance of BinPanel.
      removeAll();
      // set reference to current pane "null"
      setCurrentPane(null);
    }
    boolean existedAndRemoved = this.panes.remove(pane);

    // release all references to project generation, so memory could be
    // garbage collected
    if (existedAndRemoved) {
      pane.clear();
    }
    return existedAndRemoved;
  }

  /**
   * Removes all panes from this instance of BinPanel.
   */
  public synchronized void removeAllPanes() {
    // add all panes this instance of BinPanel holds first into temp array
    // of removables, so next we can one by one remove them from list.
    List removablePanes = new ArrayList();
    removablePanes.addAll(this.panes);

    // Now, remove them all one by one.
    for (int i = 0; i < removablePanes.size(); i++) {
      BinPane removeable = (BinPane) removablePanes.get(i);
      this.removePane(removeable);
    }
  }

  /**
   * Returns the current BinPane object that is active for this instance
   * of BinPanel.
   *
   * @return BinPane that is a current (active) pane in this instance
   * of BinPanel.
   */
  public BinPane getCurrentPane() {
    return this.currentPane;
  }

  /**
   * Sets the current pane for this intance of BinPanel.
   *
   * @param pane to be set for current pane for this instance of BinPanel.
   */
  private void setCurrentPane(BinPane pane) {
    // remove all components from this container
    removeAll();
    this.currentPane = pane;

    if (pane != null) {
      // add toolbar to the WEST of this intance of BinPanel
      add(currentPane.getToolbar(), BorderLayout.WEST);
      // add pane to the CENTER of this intance of BinPanel
      add(currentPane, BorderLayout.CENTER);
    }
  }

  /**
   * Returns all BinPanes this intance of BinPanel contains.
   *
   * @return List containing the BinPanes.
   */
  public Iterator getAllPanes() {
    return this.panes.iterator();
  }

  /**
   * Shifts the current pane by the degree as specified with "shift"
   * parameter.
   */
  private void shiftCurrentPaneBy(int shift) {
    // first save the current pane as reference for later.

    // next get the list of panes and set the next pane to be
    // as current pane.

    int indexOfCurrent = panes.indexOf(getCurrentPane());
    int targetPaneIndex = indexOfCurrent + shift;

    boolean enableBackButton = targetPaneIndex > 0;
    boolean enableNextButton = targetPaneIndex < panes.size() - 1;

    // if levels not exceeded then set the target pane.
    // otherwise let it be the current pane like previously
    if (targetPaneIndex >= 0 && targetPaneIndex <= (panes.size() - 1)) {
      currentPane = (BinPane) panes.get(targetPaneIndex);
      setCurrentPane(currentPane);
    }

    //System.err.println("currentPane == null: " + (currentPane == null));
    //System.err.println("toolbar == null: " + (currentPane.getToolbar() == null));
    //System.err.println("back == null: " + (currentPane.getToolbar().back == null));
    // set the enable status for each button on targer BinPane.
    currentPane.getToolbar().back.setEnabled(enableBackButton);
    currentPane.getToolbar().next.setEnabled(enableNextButton);
  }

  /**
   * It is called when User clicks on toolbar NEXT(>) button.
   */
  public void stepNext() {
    shiftCurrentPaneBy( +1);
    reload();
  }

  /**
   * It is called when User clicks on toolbar BACK < button.
   */
  public void stepBack() {
    shiftCurrentPaneBy( -1);
    reload();
  }

  /**
   * It is called when User clicks on toolbar CLOSE button.
   */
  public void close() {
    manager.removePanel(this);
    this.context = null;
  }

  public void closeSingleView() {
    if (panes.size() == 1) {
      close();
      return;
    }

    BinPane toBeRemoved = getCurrentPane();

    if (!tryShiftingCurrentPaneBy(1)) {
      tryShiftingCurrentPaneBy( -1);
    }

    removePane(toBeRemoved);
    reload();
  }

  private boolean canShiftCurrentPaneBy(int shift) {
    int nextIndex = panes.indexOf(getCurrentPane()) + shift;
    return nextIndex >= 0 && nextIndex < panes.size();
  }

  private boolean tryShiftingCurrentPaneBy(int shift) {
    if (canShiftCurrentPaneBy(shift)) {
      shiftCurrentPaneBy(shift);
      return true;
    }

    return false;
  }

  /*private BinPane getNextBinPaneInOrder() {
    if( panes.size() <= 1 ) {
      return null;
    }

    int previousPanelIndex = panes.indexOf( getCurrentPane() ) - 1;
    if( previousPanelIndex >= 0 ) {
      return (BinPane) panes.get( previousPanelIndex );
    }
    else {
      return (BinPane) panes.get( panes.indexOf( getCurrentPane() ) + 1 );
    }
     }
   */

  // Listeners

  private final class EscapeListener extends KeyAdapter {
    public void keyPressed(KeyEvent ke) {
      switch (ke.getKeyCode()) {

        case KeyEvent.VK_ESCAPE:
          if (ke.isShiftDown()) {
            return;
          }
          ke.consume();
          BinPanel.manager.removePanel(BinPanel.this);
          break;
      }
    }
  }

  private final class EscapeActionListener implements java.awt.event.
      ActionListener {
    public void actionPerformed(ActionEvent action) {
      BinPanel.manager.removePanel(BinPanel.this);
    }
  }

  /**
   * Return the BinPanelKey indentifying this instance of BinPanel.
   *
   * @return BinPanelKey identifying this intance of BinPanel.
   */
  public BinPanelKey getKey() {
    return this.panelKey;
  }

  public void invokeReRun() {
    currentPane.invokeReRun();
  }

  /**
   * Returns the BinPanelManager for BinPanels.
   *
   * @return BinPanelManager
   */
  public static BinPanelManager getBinPanelManager() {
    return manager;
  }

  public void optionChanged(String key, String newValue) {
    //System.out.println("BinPane.optionChanged key=" + key );
    if (key.startsWith("tree.")) {
      optionsChanged();
    }
  }

  public void optionsChanged() {
    for (int i = 0, max = panes.size(); i < max; i++) {
      JComponent comp = ((BinPane) panes.get(i)).getComponent().getUI();
      if (comp instanceof TunableComponent) {
        ((TunableComponent) comp).optionsChanged();
      }
    }
  }
}
