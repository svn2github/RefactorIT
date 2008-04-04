/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.panel;

import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.tree.BinTree;
import net.sf.refactorit.ui.treetable.BinTreeTable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalBorders;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author  Jaanek Oja
 */
public class BinPaneToolBar extends JPanel {
  private static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(UIResources.class, "LocalizedStrings");

  // The parameters that are used by public users of this class while
  // calling getAddInButtons(..) function.
  public static int TOOLBAR_BUTTONS = 2;

  // Somebody please write javadoc for these three variables
  private static final int BACK = 0;
  private static final int NEXT = 1;
  private static final int CLOSE_ONE_VIEW = 3;

  private static final Insets defaultMargin = new Insets(2, 2, 2, 2);

  private static final ImageIcon iconLeft
      = ResourceUtil.getIcon(UIResources.class, "LeftArrow.gif");
  private static final ImageIcon iconRight
      = ResourceUtil.getIcon(UIResources.class, "RightArrow.gif");
  private static final ImageIcon iconFilter
      = ResourceUtil.getIcon(UIResources.class, "filter.gif");
  private static final ImageIcon iconCloseView
      = ResourceUtil.getIcon(UIResources.class, "CloseView.gif");
  private static final ImageIcon iconHelp
      = ResourceUtil.getIcon(UIResources.class, "help.gif");
  private static final ImageIcon iconUnhide
      = ResourceUtil.getIcon(UIResources.class, "Unhide.gif");
  private static final ImageIcon iconExpandAll
      = ResourceUtil.getIcon(UIResources.class, "ExpandAll.gif");
  private static final ImageIcon iconCollapseAll
      = ResourceUtil.getIcon(UIResources.class, "CollapseAll.gif");
  private static final ImageIcon iconSingleTabView
      = ResourceUtil.getIcon(UIResources.class, "SingleTabView.gif");

  // back(<), next(>) and help buttons.
  public JButton back;
  public JButton next;

  BinPane binPane;

  /** Popup list of options to tune view (SingleTab, Expand, Collapse etc.) */
  JButton optionsButton;

  // a container to hold the references to buttons that were added by users of
  // this instance of BinPaneToolBar using the method addToolbarButton(..).
  private List toolBarAddInButtons = new ArrayList();

  /** Opens filter dialog */
  private JButton filterShow;
  private ActionListener filterShowActionListener;

  private JToggleButton unhideButton;

  // this is a toolbar which is provided to outside users of this instance.
  // i.e. users of this Class can put additional buttons on it.
  private JToolBar extraBar;

  private JButton helpButton;

  private JToggleButton singleTabButton;

  private JButton collapseAllButton;

  private JButton expandAllButton;

  // The listener of this BinPanelToolBar instance.
  // this instance is notifying the listener about events that
  // take place in this toolbar.
  private BinPanel toolBarListener;

  private int gapHeight;

  /**
   * Creates a new instance of BinPanelToolBar
   *
   *@param singleTabSelected option. Indicates whether single tab option is true
   * initially or not.
   */
  public BinPaneToolBar(final BinPane binPane) {
    super(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0));

    this.binPane = binPane;

    gapHeight = 1;
// Disabled gap autodetection, 9/5/2002, Anton
//        JToolBar.Separator ethalon = new JToolBar.Separator();
//        gapHeight = (int) ethalon.getSeparatorSize().getWidth();
//        if (gapHeight < 0) {
//          gapHeight = 3;
//        }

    this.toolBarListener = null;

    // create a LEFT JToolBar and associate it with this instance of
    // BinPanelToolBar
    JToolBar left = new JToolBar(JToolBar.VERTICAL);
    this.add(left);
    left.setFloatable(false);
    left.setMargin(defaultMargin);
    left.setMinimumSize(new Dimension(0, 0));
    left.setBorder(BorderFactory.createEmptyBorder());

    // create a CLOSE SINGLE VIEW button
    left.add(createNavigateButton(CLOSE_ONE_VIEW));
    left.add(Box.createVerticalStrut(gapHeight));

    // create button "toggle single/multi tab view"
    createSingleTabButton();
    if (!IDEController.runningEclipse()) {
      left.add(singleTabButton);
      left.add(Box.createVerticalStrut(gapHeight));
    }

    // create a BACK navigate button and associate it with left instance
    this.back = createNavigateButton(BACK);
    left.add(this.back);
    left.add(Box.createVerticalStrut(gapHeight));

    // create a NEXT navigate button and associate it with left instance
    this.next = createNavigateButton(NEXT);
    left.add(this.next);
    left.add(Box.createVerticalStrut(gapHeight));

    // create button "Hide/unhide hidden rows"
    unhideButton = createUnhideButton();
    left.add(unhideButton);
    left.add(Box.createVerticalStrut(gapHeight));

    // create a RIGHT JToolBar and associate it with this instance of
    // BinPanelToolBar
    JToolBar right = new JToolBar(JToolBar.VERTICAL);
    this.add(right);
    right.setFloatable(false);
    right.setMargin(defaultMargin);
    right.setMinimumSize(new Dimension(0, 0));
    right.setBorder(BorderFactory.createEmptyBorder());

    right.add(createExpandAllButton());
    right.add(Box.createVerticalStrut(gapHeight));

    right.add(createCollapseAllButton());
    right.add(Box.createVerticalStrut(gapHeight));

    binPane.addComponentChangeListener(new BinPane.ComponentChangeListener() {
      public void componentChanged(BinPane source) {
        ResultArea component = binPane.getComponent();
        if(component == null) { // This happens when we close a single view
          return;
        }

        JComponent ui = component.getUI();
        boolean enableExpandCollapse = ui instanceof BinTreeTable ||
            ui instanceof BinTree;

        collapseAllButton.setEnabled(enableExpandCollapse);
        expandAllButton.setEnabled(enableExpandCollapse);
      }
    } );

    // create filter menu and associate it with this instance
    right.add(createFilterMenu());

    this.extraBar = new JToolBar(JToolBar.VERTICAL);
    this.extraBar.setFloatable(false);
    this.extraBar.setMargin(defaultMargin);
    this.extraBar.setMinimumSize(new Dimension(0, 0));
    this.extraBar.setBorder(BorderFactory.createEmptyBorder());

    right.add(this.extraBar);
  }

  private JToggleButton createUnhideButton() {
    final JToggleButton result = new JToggleButton(iconUnhide);
    result.setToolTipText(resLocalizedStrings.getString("unhideButton.tooltip"));

    result.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (result.isSelected()) {
          BinPaneToolBar.this.binPane.getComponent().showHiddenRows();
        } else {
          BinPaneToolBar.this.binPane.getComponent().hideHiddenRows();
        }
      }
    });

    tuneSmallButtonBorder(result);

    return result;
  }

  /**
   * Creates a navigation button for this instance of BinPanelToolbar.
   *
   * i.e. currently it creates the BACK (<), NEXT (>) and CLOSE (*) buttons
   * and associates ActionListener's with them.
   *
   * @param action is either BACK, NEXT or CLOSE
   */
  private JButton createNavigateButton(final int action) {
    String toolTip = null;
    ImageIcon icon = null;
    switch (action) {
      case BACK:
        icon = iconLeft;
        toolTip = resLocalizedStrings.getString("button.back.tooltip");
        break;

      case NEXT:
        icon = iconRight;
        toolTip = resLocalizedStrings.getString("button.next.tooltip");
        break;

      case CLOSE_ONE_VIEW:
        icon = iconCloseView;
        toolTip = resLocalizedStrings.getString("close.this.view");
        break;

      default:
        Assert.must(false, "Wrong action type!");
        break;
    }

    final JButton button = new JButton(icon);
    button.setEnabled(action == CLOSE_ONE_VIEW);
    button.setToolTipText(toolTip);
    tuneSmallButtonBorder(button);

    ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        switch (action) {
          case BACK:
            getToolBarListener().stepBack();
            break;
          case NEXT:
            getToolBarListener().stepNext();
            break;
          case CLOSE_ONE_VIEW:
            getToolBarListener().closeSingleView();
            break;
        }
      }
    };

    button.addActionListener(listener);
    try {
      button.registerKeyboardAction(listener,
          KeyStroke.getKeyStroke("ENTER"), JComponent.WHEN_FOCUSED);
    } catch (Exception e) {
      // failed to register, let's live without
    }

    return button;
  }

  public static void tuneSmallButtonBorder(final AbstractButton button) {
    button.setMargin(defaultMargin);

    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    button.setAlignmentY(Component.CENTER_ALIGNMENT);

    if (button instanceof JToggleButton) {
      if (UIManager.getLookAndFeel().getID().equals("Metal")) {
        // bug in Netbeans... crazy IDE!!!
        button.setBorder(
            BorderFactory.createCompoundBorder(
            new MetalBorders.Flush3DBorder(),
            new BasicBorders.MarginBorder()));
      } else {
        button.setBorder(BasicBorders.getToggleButtonBorder());
      }
    } else {
      button.setBorder(BasicBorders.getButtonBorder());
    }

    button.setBorderPainted(false);

    button.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent mouseEvent) {
        if (button.isEnabled()) {
          button.setBorderPainted(true);
        } else {
          mouseExited(null);
        }
      }

      public void mouseExited(MouseEvent mouseEvent) {
        button.setBorderPainted(false);
      }
    });
  }

  /**
   * Returns the listener of this BinPanelToolBar intance.
   */
  public BinPanel getToolBarListener() {
    return this.toolBarListener;
  }

  /**
   * Sets a new listener for this isntance of BinPanelToolBar.
   *
   * If provided null, then the toolbar releases all resources it holds.
   * i.e. references to buttons etc.
   *
   * @param newListener a new listener for this intance of BinPanelToolBar.
   */
  public void setToolbarListener(BinPanel newListener) {
    this.toolBarListener = newListener;

    if(newListener != null) {
      singleTabButton.setSelected(newListener.isSingleTabOption());
    }
  }

  private AbstractButton createSingleTabButton() {
    singleTabButton = new JToggleButton(iconSingleTabView);
    tuneSmallButtonBorder(singleTabButton);
    singleTabButton.setToolTipText("Single tab view");

    ActionListener singleTabOptionListener = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        getToolBarListener().setSingleTabOption(
            singleTabButton.isSelected());
      }
    };
    singleTabButton.addActionListener(singleTabOptionListener);

    return singleTabButton;
  }

  private AbstractButton createExpandAllButton() {
    expandAllButton = new JButton(iconExpandAll);
    tuneSmallButtonBorder(expandAllButton);
    expandAllButton.setToolTipText("Expand All");

    ActionListener expandAllActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JComponent ui = BinPaneToolBar.this.binPane.getComponent().getUI();
        if (ui instanceof BinTreeTable) {
          ((BinTreeTable) ui).expandAll();
        } else if (ui instanceof BinTree) {
          ((BinTree) ui).expandAll();
        }
      }
    };
    expandAllButton.addActionListener(expandAllActionListener);

    return expandAllButton;
  }

  private AbstractButton createCollapseAllButton() {
    collapseAllButton = new JButton(iconCollapseAll);
    tuneSmallButtonBorder(collapseAllButton);
    collapseAllButton.setToolTipText("Collapse All");

    ActionListener collapseAllActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JComponent ui = BinPaneToolBar.this.binPane.getComponent().getUI();
        if (ui instanceof BinTreeTable) {
          ((BinTreeTable) ui).collapseAll();
        } else if (ui instanceof BinTree) {
          ((BinTree) ui).collapseAll();
        }
      }
    };
    collapseAllButton.addActionListener(collapseAllActionListener);

    return collapseAllButton;
  }

  /**
   * Creates a filter menu for this intance of BinPanelToolbar.
   */
  private JButton createFilterMenu() {
    this.filterShow = new JButton(iconFilter);
    tuneSmallButtonBorder(this.filterShow);

    this.filterShow.setToolTipText(resLocalizedStrings.getString(
        "button.filters.tooltip"));
    this.filterShow.setEnabled(false);

    return this.filterShow;
  }

  /**
   * Adds a specified button to this instance of BinPanelToolBar
   *
   * @param button to be added to toolbar.
   */
  public void addToolbarButton(AbstractButton button) {
    tuneSmallButtonBorder(button);
    this.extraBar.add(button);
    this.extraBar.add(Box.createVerticalStrut(gapHeight));
    // add the button into an array, so the users of this class
    // can retrieve the buttons later they have been added into this
    // toolbar by calling getAddInButtons(..) function.
    this.toolBarAddInButtons.add(button);

    button.revalidate();
    button.repaint();
  }

  /**
   * Removes the specified abstract button from the list of
   * toolbar buttons of this instance of BinPaneToolBar.
   *
   * This method performs no function, nor does it throw an exception, if
   * the button specified by the argument wasn't previously added to this instance
   * of BinPaneToolBar. If button is null, no exception is thrown and no action is
   * performed.
   *
   * @param button the button to be removed from toolbar.
   */
  public void removeToolbarButton(AbstractButton button) {
    if (button == null) {
      return;
    }

    // remove strut first
    int index = this.extraBar.getComponentIndex(button);
    if (index > 0 && index < this.extraBar.getComponentCount() - 1) {
      if (!(this.extraBar.getComponentAtIndex(index + 1)
          instanceof AbstractButton)) {
        this.extraBar.remove(index + 1);
      }
    }

    this.extraBar.remove(button);
    // check whether the button specified as argument was added to toolbarAddInButtons array,
    // if so, then remove all listeners of the button before removing it from
    // container. It is needed to release resources the listeners may be referring to,
    // for example references to classmodel.Bin* objects.
    if (this.toolBarAddInButtons.contains(button)) {
      removeAllListeners(button);
      this.toolBarAddInButtons.remove(button);
    }
  }

  public void setFilterButtonEnabled(boolean b) {
    this.filterShow.setEnabled(b);
  }

  public void setUnhideButtonEnabled(boolean b) {
    this.unhideButton.setEnabled(b);
  }

  public void setFilterActionListener(ActionListener listener) {
    if (this.filterShowActionListener != null) {
      this.filterShow.removeActionListener(this.filterShowActionListener);
    }

    this.filterShow.addActionListener(listener);
    this.filterShowActionListener = listener;
  }

  /**
   * Removes all listeners of the listenerType that has been added to the specified button.
   *
   * @param button as a target from where to remove all listeners of type listenerType
   * @param listenerType the type of Listener. (ActionListener, ...)
   */
  private void removeListeners(AbstractButton button, Class listenerType) {
    EventListener[] listeners = button.getListeners(listenerType);
    for (int i = 0; i < listeners.length; i++) {
      if (listeners[i] instanceof ActionListener) {
        button.removeActionListener((ActionListener) listeners[i]);
      } else if (listeners[i] instanceof MouseListener) {
        button.removeMouseListener((MouseListener) listeners[i]);
      } else if (listeners[i] instanceof FocusListener) {
        button.removeFocusListener((FocusListener) listeners[i]);
      } else if (listeners[i] instanceof KeyListener) {
        button.removeKeyListener((KeyListener) listeners[i]);
      }
    }
  }

  /**
   * Removes all addin buttons the users of this instance of BinPaneToolBar has been
   * adding through addXXXButton(...)
   */
  public void removeAddInButtons() {
    AbstractButton[] toolBarButtons = getAddInButtons(BinPaneToolBar.
        TOOLBAR_BUTTONS);
    for (int i = 0; i < toolBarButtons.length; i++) {
      removeToolbarButton(toolBarButtons[i]);
    }
  }

  public void clear() {
    this.toolBarListener = null;
    removeAddInButtons();
    removeKeyboardListeners();
    removeListenersFromSubComponents(this);
//    removeAllListeners(this.filterShow);
//    removeAllListeners(this.optionsButton);
//    removeAllListeners(this.unhideButton);
//    removeAllListeners(this.back);
//    removeAllListeners(this.next);
//    removeAllListeners(this.helpButton);
    this.extraBar.removeAll();
    removeAll();
    this.binPane = null;
    this.filterShowActionListener = null;
  }

  private void removeListenersFromSubComponents(final Container cont) {
    final Component[] comps = cont.getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof AbstractButton) {
        removeAllListeners((AbstractButton) comps[i]);
      } else if (comps[i] instanceof Container) {
        removeListenersFromSubComponents((Container) comps[i]);
      }
    }
  }

  private void removeAllListeners(final AbstractButton button) {
    if (button == null) {return;
    }
    removeListeners(button, ActionListener.class);
    removeListeners(button, MouseListener.class);
    removeListeners(button, FocusListener.class);
    removeListeners(button, KeyListener.class);
    button.unregisterKeyboardAction(KeyStroke.getKeyStroke("ENTER"));
  }

  /**
   * Removes all keyboard action listeners this instance of BinpaneToolBar has been
   * registering for its own use.
   *
   * It is useful to break these referenses to release this instance of BinPaneToolBar
   * for garbage collection. Otherwise the Keyboard action listerers are still holding
   * references for this instance of BinPaneToolBar and this object is never released.
   */
  public void removeKeyboardListeners() {
  }

  /**
   * Returns an array of buttons for a specified (buttons) type.
   *
   * If users of this instance of BinPaneToolBar has been adding buttons using
   * public addXXXButton(..) methods, then they also can retrieve an array of those
   * buttons they were adding.
   *
   * For example: AbstractButton[] AddedToolBarButtons = getAddInButtons(BinPaneToolBar.TOOLBAR_BUTTONS);
   *
   * @param buttonsType the type of AddIn buttons the user wants to retrieve.
   * @return AbstractButton[], the array containing the AbstractButtons of type as
   * specified in parameters.
   * @throws java.lang.IllegalArgumentException if the buttons type specified as calling that method
   * is illegal type.
   */
  public AbstractButton[] getAddInButtons(int buttonsType) {
    if (buttonsType == TOOLBAR_BUTTONS) {
      AbstractButton[] toolBarAddIns = new AbstractButton[this.
          toolBarAddInButtons.size()];
      return (AbstractButton[])this.toolBarAddInButtons.toArray(toolBarAddIns);
    } else {
      throw new IllegalArgumentException(
          "No such buttons of this type supported.");
    }
  }

  /**
   * Returns the help button for this panel.
   */
  public AbstractButton getHelpButton() {
    // create the help hutton if it is not
    // created yet and return it
    if (this.helpButton == null) {
      this.helpButton = new JButton(iconHelp);
      String tooltipText = resLocalizedStrings.getString("helpButton.tooltip");
      this.helpButton.setToolTipText(tooltipText);
    }

    return this.helpButton;
  }
}
