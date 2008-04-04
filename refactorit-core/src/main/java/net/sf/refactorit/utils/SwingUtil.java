/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;


import net.sf.refactorit.common.exception.SystemException;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.exception.ErrorCodes;
import net.sf.refactorit.standalone.editor.JSourceArea;
import net.sf.refactorit.ui.dialog.RitDialog;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author  Tanel
 * @author  Anton Safonov
 * @author  Vlad
 * @author  Risto
 * @author  juri
 */
public final class SwingUtil {
  /** We do not allow to create any object of this class */
  private SwingUtil() {}

  /**
   *  Check whether GUI present or not.
   */
  public static boolean isGUI() {
    boolean retVal = false;
    try {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] gs = ge.getScreenDevices();
      if (gs.length > 0) {
        retVal = true;
      }
    } catch (Throwable t) {}
    return retVal;
  }

  /**
   * Adds JMenuItems into specified JMenu.
   */
  public static void addIntoMenu(JMenu menu, JMenuItem[] menuItems) {
    for (int i = 0; i < menuItems.length; i++) {
      menu.add(menuItems[i]);
    }
  }

  /**
   * Gets the current cursor position in the component or one of it's
   * subcomponents.
   *
   * @param component the component that is an EditorPane or some of its
   *      subcomponents is an EditorPane
   * @return current caret position
   */
  public static int getCaretPosition(Container component) {
    if (component.getClass().getName().indexOf("EditorPane") >= 0) {
      try {
        java.lang.reflect.Method caretGetter
            = component.getClass().getMethod("getCaretPosition", new Class[] {});
        Object result = caretGetter.invoke(component, new Object[] {});
        return ((Integer) result).intValue();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Method invocation exception caught");
      }
    }

    for (int i = 0; i < component.getComponentCount(); i++) {
      java.awt.Component childComponent = component.getComponent(i);
      if (childComponent instanceof javax.swing.JComponent) {
        int result = getCaretPosition((javax.swing.JComponent) childComponent);

        if (result >= 0) {
          return result;
        }
      }
    }

    return -1;
  }

  /**
   * Obtains selected text
   *
   * @param component the component that is an EditorPane or some of its
   *      subcomponents is an EditorPane
   * @return current caret position
   */
  public static String getSelectedText(Container component) {
    if (component.getClass().getName().indexOf("EditorPane") >= 0) {
      try {
        java.lang.reflect.Method caretGetter
            = component.getClass().getMethod("getSelectedText", new Class[] {});
        Object result = caretGetter.invoke(component, new Object[] {});
        if (result == null) {
          return null;
        }
        return (String) result;
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Method invocation exception caught");
      }
    }

    for (int i = 0; i < component.getComponentCount(); i++) {
      Component childComponent = component.getComponent(i);
      if (childComponent instanceof JComponent) {
        return getSelectedText((JComponent) childComponent);
      }
    }

    return null;
  }

  /**
   * Obtains start position of selected text
   *
   * @param component the component that is an EditorPane or some of its
   *      subcomponents is an EditorPane
   * @return current caret position
   */
  public static int getSelectionStart(Container component) {
    if (component.getClass().getName().indexOf("EditorPane") >= 0) {
      try {
        Method caretGetter = component
            .getClass().getMethod("getSelectionStart", new Class[] {});

        Object result = caretGetter.invoke(component, new Object[] {});

        return ((Integer) result).intValue();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Method invocation exception caught");
      }
    }

    for (int i = 0; i < component.getComponentCount(); i++) {
      Component childComponent = component.getComponent(i);
      if (childComponent instanceof JComponent) {
        int result = getSelectionStart((JComponent) childComponent);

        if (result >= 0) {
          return result;
        }
      }
    }

    return -1;
  }

  /**
   * Obtains end position of selected text
   *
   * @param component the component that is an EditorPane or some of its
   *      subcomponents is an EditorPane
   * @return current caret position
   */
  public static int getSelectionEnd(Container component) {
    if (component.getClass().getName().indexOf("EditorPane") >= 0) {
      try {
        Method caretGetter = component
            .getClass().getMethod("getSelectionEnd", new Class[] {});

        Object result = caretGetter.invoke(component, new Object[] {});

        return ((Integer) result).intValue();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Method invocation exception caught");
      }
    }

    for (int i = 0; i < component.getComponentCount(); i++) {
      Component childComponent = component.getComponent(i);
      if (childComponent instanceof JComponent) {
        int result = getSelectionEnd((JComponent) childComponent);

        if (result >= 0) {
          return result;
        }
      }
    }

    return -1;
  }

  public static Point positionToClickPoint(Container component,
      int caretPosition,
      Container invokedIn) {
    if (component == null) {
      return null;
    }

//System.err.println("Checking: " + component.getClass().getName());
    if (component.getClass().getName().indexOf("EditorPane") >= 0) {
      try {
        java.lang.reflect.Method pointGetter = component.getClass()
            .getMethod("modelToView", new Class[] {Integer.TYPE});
        Rectangle rec = (Rectangle) pointGetter.invoke(component,
            new Object[] {new Integer(caretPosition)});
//System.err.println("Before: " + (int)rec.getY());
        // FIXME: somehow it fails here to convert point from scrollable component
        Point point = SwingUtilities.convertPoint(
            component,
            (int) rec.getX(),
            (int) rec.getY() + 10,
            invokedIn);
        // FIXME: ugly hack :(
        if (point.getY() > 1024) {
          point = new Point((int) point.getX(), 250);
        }
//System.err.println("After: " + (int)point.getY());
        return point;
      } catch (Exception e) {
        System.err.println("Method invocation exception caught");
        e.printStackTrace();

        //FIXME: BUG
        return null;
        //throw new RuntimeException("Method invocation exception caught");
      }
    }

    for (int i = 0; i < component.getComponentCount(); i++) {
      java.awt.Component childComponent = component.getComponent(i);
      if (childComponent instanceof javax.swing.JComponent) {
        Point point = positionToClickPoint(
            (javax.swing.JComponent) childComponent, caretPosition, invokedIn);
        if (point != null) {
          return point;
        }
      }
    }

    return null;
  }

  /**
   * @return a new panel whose layout forces the original panel to shrink in
   *         size as much as possible.
   */
  public static JPanel wrapInMinimizer(JPanel panel) {
    JPanel result = new JPanel();
    result.setLayout(new BorderLayout());
    result.add(panel, BorderLayout.WEST);

    return result;
  }

  /**
   * Neccessary only for older JDK versions, but it works also
   * on the new ones. (For example, under Win2k
   * it is needed for 1.4.0_01, but not for 1.4.1_02.)
   */
  public static void ensureInViewableArea(JPopupMenu visibleMenu) {
    Rectangle currentBounds = new Rectangle(
        visibleMenu.getLocationOnScreen(), visibleMenu.getSize());
    Rectangle viewableLocation = JSourceArea.ensureRectIsVisible(currentBounds);

    if (!viewableLocation.equals(currentBounds)) {
      setLocationOnScreen(visibleMenu, viewableLocation.x, viewableLocation.y);
    }
  }

  /**
   * This method exists because popup menus can not be directly moved
   * (the have to be hidden and re-shown).
   */
  public static void setLocationOnScreen(JPopupMenu visibleMenu, int x, int y) {
    Point invokerLocation = visibleMenu.getInvoker().getLocationOnScreen();
    visibleMenu.setVisible(false);

    visibleMenu.show(visibleMenu.getInvoker(),
        x - invokerLocation.x, y - invokerLocation.y);
  }

  public static Color averageColor(Color c1, Color c2) {
    return new Color(
        (c1.getRed() + c2.getRed()) / 2,
        (c1.getGreen() + c2.getGreen()) / 2,
        (c1.getBlue() + c2.getBlue()) / 2
        );
  }

  /**
   *
   * @deprecated  Use automatic RitDialog centering facilities
   */
  public static void centerWindowOnScreen(Window window) {
    Dimension dim = window.getToolkit().getScreenSize();
    window.setLocation(
        (dim.width - window.getWidth()) / 2,
        (dim.height - (window.getHeight())) / 2);
  }

  /**
   * Creates a panel that contains all of the components on top of each other in north,
   * and tries to make them as small as possible (probably by using getPreferredSize()).
   *
   * @deprecated use proper layout, usually no need to use such complex/ugly layouting
   */
  public static JPanel combineInNorth(JComponent[] components) {
    JPanel result = new JPanel();
    if (components.length == 0) {
      return result;
    }
    result.setLayout(new BorderLayout());
    JPanel contentPanel=new JPanel();
    result.add(contentPanel,BorderLayout.NORTH);
    contentPanel.setLayout(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx=0;
    constraints.weightx=1.0;
    constraints.fill=GridBagConstraints.HORIZONTAL;
    for (int i = 0; i < components.length; i++) {
      contentPanel.add(components[i],constraints);
    }
    if(result.isVisible())
      result.doLayout();
    return result;
  }

  /**
   * In EDT just runs the runnable (so in that thread the pending AWT events
   * are _not_ dispatched before running the runnable).
   */
  public static void invokeAndWaitFromAnyThread(Runnable r) throws
      InterruptedException,
      InvocationTargetException {
    if (SwingUtilities.isEventDispatchThread()) {
      try {
        r.run();
      } catch (RuntimeException e) {
        throw new InvocationTargetException(e);
      }
    } else {
      SwingUtilities.invokeAndWait(r);
    }
  }

  public static void invokeAndWaitFromAnyThread_noCheckedExceptions(Runnable r) {
    try {
      invokeAndWaitFromAnyThread(r);
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, SwingUtil.class);
      throw new SystemException(ErrorCodes.INTERNAL_ERROR, e);
    }
  }

  public static void invokeInEdtUnderNetBeansAndJB(Runnable r) {
    if (IDEController.runningNetBeans() || IDEController.runningJBuilder()) {
      invokeAndWaitFromAnyThread_noCheckedExceptions(r);
    } else {
      r.run();
    }
  }

  public static boolean ignoreInvokeLater = false;

  public static void invokeLater(Runnable r) {
    if (!ignoreInvokeLater) {
      SwingUtilities.invokeLater(r);
    } else {
      r.run();
    }
  }

  public static List getChildJComponents(Container container) {
    List result = new ArrayList();

    Component[] children = container.getComponents();

    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof JComponent) {
        result.add(children[i]);
        result.addAll(getChildJComponents((JComponent) children[i]));
      }
    }

    return result;
  }
  
  public static void initCommonDialogKeystrokes(
      final RitDialog dialog, final JButton buttonOk) {
    
    addEscapeListener(dialog);
    initOkButton(dialog, buttonOk);
  }

  public static void initCommonDialogKeystrokes(
      final RitDialog dialog, final JButton buttonOk, ActionListener escapeListener) {
    
    addEscapeListener(dialog, escapeListener);
    initOkButton(dialog, buttonOk);
  }
  
  public static void initCommonDialogKeystrokes(
      final RitDialog dialog, final JButton buttonOk, final JButton buttonCancel
  ) {
    
    initCommonDialogKeystrokes(dialog, buttonOk);
    initCancelButton(buttonCancel);
  }
  
  public static void initCommonDialogKeystrokes(
      final RitDialog dialog, final JButton buttonOk, final JButton buttonCancel,
      ActionListener escapeListener) {
    
    initCommonDialogKeystrokes(dialog, buttonOk, escapeListener);
    initCancelButton(buttonCancel);
  }

  public static void initCommonDialogKeystrokes(
      final RitDialog dialog, final JButton buttonOk, final JButton buttonCancel,
      final JButton buttonHelp) {
    
    initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel);
    initHelpButton(buttonHelp);
  }

  public static void initCommonDialogKeystrokes(
      final RitDialog dialog, final JButton buttonOk, final JButton buttonCancel,
      final JButton buttonHelp, ActionListener escapeListener) {
    
    initCommonDialogKeystrokes(dialog, buttonOk, buttonCancel, escapeListener);
    initHelpButton(buttonHelp);
  }
  
  private static void initOkButton(final RitDialog dialog, final JButton buttonOk) {
    buttonOk.setMnemonic(KeyEvent.VK_O);
    buttonOk.setDefaultCapable(true);
    dialog.getRootPane().setDefaultButton(buttonOk);
  }

  private static void initCancelButton(final JButton buttonCancel) {
    buttonCancel.setMnemonic(KeyEvent.VK_C);
    buttonCancel.setDefaultCapable(false);
  }
  
  private static void initHelpButton(final JButton buttonHelp) {
    buttonHelp.setMnemonic(KeyEvent.VK_H);
    buttonHelp.setDefaultCapable(false);
  }

  public static void addEscapeListener(final RitDialog dialog) {
    addEscapeListener(dialog, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
  }

  public static void addEscapeListener(final RitDialog dialog, final ActionListener escapeListener) {
    final String key = "RefactorItEscapeListenerActionForADialog";
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    
    final Action act = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        escapeListener.actionPerformed(e);
      }
    };

    dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, key);
    dialog.getRootPane().getActionMap().put(key, act);
  }

  public static JMenuItem[] getMenuItems(JMenu menu) {
    JMenuItem[] result = new JMenuItem[menu.getItemCount()];

    for (int i = 0; i < result.length; i++) {
      result[i] = menu.getItem(i);
    }

    return result;
  }
}
