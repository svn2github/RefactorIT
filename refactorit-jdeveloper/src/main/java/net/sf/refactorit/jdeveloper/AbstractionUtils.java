/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import oracle.ide.ContextMenu;
import oracle.ide.Ide;
import oracle.ide.IdeAction;
import oracle.ide.addin.Controller;
import oracle.ide.addin.View;
import oracle.ide.docking.DockStation;
import oracle.ide.docking.Dockable;
import oracle.ide.editor.EditorManager;
import oracle.ide.explorer.ExplorerManager;
import oracle.ide.model.Node;
import oracle.ide.navigator.NavigatorManager;

import javax.swing.JFrame;
import javax.swing.JMenu;

import java.awt.Component;
import java.lang.reflect.Method;


/**
 * @author Anton Safonov
 */
public final class AbstractionUtils {
  private AbstractionUtils() {
  }

  static final int getNewCmdId(String key) {
    try {
      return Ide.newCmd(key);
    } catch (NoSuchMethodError e) {
      try {
        Method method = Ide.class.getMethod("createCmdID", new Class[] {String.class});
        return ((Integer) method.invoke(Ide.class, new Object[] {key
        })).intValue();
      } catch (Exception e1) {
        System.err.println("RefactorIT: Can't create command Id: ");
        e1.printStackTrace();
        return -1;
      }
    }
  }

  public static final void addController(IdeAction action,
      Controller controller) {
    try {
      action.setController(controller);
    } catch (NoSuchMethodError e) {
      try {
        Method method = IdeAction.class.getMethod(
            "addController", new Class[] {Controller.class});
        method.invoke(action, new Object[] {controller});
      } catch (Exception e1) {
        System.err.println("RefactorIT: Can't register controller: ");
        e1.printStackTrace();
      }
    }
  }

  public static final JFrame getMainWindow() {
    try {
      Method method = Ide.class.getMethod("getMainWindow", new Class[0]);
      return (JFrame) method.invoke(Ide.class, new Object[0]);
    } catch (Exception e) {
      return null;
    }
  }

  public static final EditorManager getEditorManager() {
    try {
      return Ide.getEditorManager();
    } catch (Error e) {
      try {
        Method method = EditorManager.class.getMethod("getEditorManager",
            new Class[] {});
        return (EditorManager) method.invoke(EditorManager.class, new Object[] {});
      } catch (Exception e1) {
        System.err.println("RefactorIT: can't get EditorManager: ");
        e1.printStackTrace();
        return null;
      }
    }
  }

  public static final ContextMenu getEditorContextMenu() {
    EditorManager manager = getEditorManager();
    try {
      Method method = manager.getClass().getMethod("getContextMenu", new Class[] {});
      return (ContextMenu) method.invoke(manager, new Object[] {});
    } catch (Exception e1) {
      System.err.println("RefactorIT: can't get editor context menu: ");
      e1.printStackTrace();
      return null;
    }
  }

  public static final void openDefaultEditorInFrame(Node node) {
    EditorManager manager = getEditorManager();
    try {
      Method method = manager.getClass().getMethod(
          "openDefaultEditorInFrame", new Class[] {Node.class});
      method.invoke(manager, new Object[] {node});
    } catch (Exception e1) {
      System.err.println("RefactorIT: can't get open default editor: ");
      e1.printStackTrace();
    }
  }

  public static final ExplorerManager getExplorerManager() {
    try {
      return Ide.getExplorerManager();
    } catch (Error e) {
      try {
        Method method = ExplorerManager.class.getMethod("getExplorerManager",
            new Class[] {});
        return (ExplorerManager) method.invoke(ExplorerManager.class,
            new Object[] {});
      } catch (Exception e1) {
        System.err.println("RefactorIT: can't get ExplorerManager: ");
        e1.printStackTrace();
        return null;
      }
    }
  }

  public static final ContextMenu getExplorerContextMenu() {
    ExplorerManager manager = getExplorerManager();
    try {
      Method method = manager.getClass().getMethod("getContextMenu", new Class[] {});
      return (ContextMenu) method.invoke(manager, new Object[] {});
    } catch (Exception e1) {
      System.err.println("RefactorIT: can't get explorer context menu: ");
      e1.printStackTrace();
      return null;
    }
  }

  public static final NavigatorManager getNavigatorManager() {
    try {
      return Ide.getNavigatorManager();
    } catch (Error e) {
      try {
        Method method = NavigatorManager.class.getMethod(
            "getApplicationNavigatorManager", new Class[] {});
        return (NavigatorManager) method.invoke(NavigatorManager.class,
            new Object[] {});
      } catch (Exception e1) {
        System.err.println("RefactorIT: can't get NavigatorManager: ");
        e1.printStackTrace();
        return null;
      }
    }
  }

  public static final ContextMenu getNavigatorContextMenu() {
    NavigatorManager manager = getNavigatorManager();
    try {
      Method method = manager.getClass().getMethod("getContextMenu", new Class[] {});
      return (ContextMenu) method.invoke(manager, new Object[] {});
    } catch (Exception e1) {
      System.err.println("RefactorIT: can't get navigator context menu: ");
      e1.printStackTrace();
      return null;
    }
  }

  public static final DockStation getDockStation() {
    try {
      return Ide.getDockStation();
    } catch (Error e) {
      try {
        Method method = DockStation.class.getMethod(
            "getDockStation", new Class[] {});
        return (DockStation) method.invoke(DockStation.class, new Object[] {});
      } catch (Exception e1) {
        System.err.println("RefactorIT: can't get DockStation: ");
        e1.printStackTrace();
        return null;
      }
    }
  }

  public static final void dock(Dockable dockable, int i, boolean flag) {
    DockStation manager = getDockStation();
    try {
      Method method = manager.getClass().getMethod("dock",
          new Class[] {Dockable.class, Integer.TYPE, Boolean.TYPE});
      method.invoke(manager, new Object[] {dockable, new Integer(i),
          new Boolean(flag)});
    } catch (Exception e1) {
      System.err.println("RefactorIT: can't get navigator context menu: ");
      e1.printStackTrace();
    }
  }

  public static final void setDockableVisible(Dockable dockable, boolean flag) {
    DockStation manager = getDockStation();
    try {
      Method method = manager.getClass().getMethod("setDockableVisible",
          new Class[] {Dockable.class, Boolean.TYPE});
      method.invoke(manager, new Object[] {dockable, new Boolean(flag)});
    } catch (Exception e1) {
      System.err.println("RefactorIT: can't get navigator context menu: ");
      e1.printStackTrace();
    }
  }

  public static Controller getIdeController() {
    try {
      return Ide.getInstance();
    } catch (Error e) {
//      return Ide.getLastActiveNavigator().getController(); --> null in 9.0.5.1
//      return ((View) AbstractionUtils.getMainWindow()).getController(); --> null
      try {
        Method method = Ide.class.getMethod("getLastActiveNavigator", new Class[] {});
        Controller controller = ((View) method.invoke(Ide.class, new Object[] {}))
            .getController();
        return controller;
      } catch (Exception e1) {
//        System.err.println("RefactorIT: can't get ide controller: ");
//        e1.printStackTrace();

        return null;
      }
    }
  }

  public static void add(Component what, JMenu where, float section) {
    try {
      Method method = Ide.getMenubar().getClass().getMethod("add",
          new Class[] {Component.class, JMenu.class, Float.TYPE});
      method.invoke(Ide.getMenubar(),
          new Object[] {what, where, new Float(section)});
    } catch (Exception e) {
      where.add(what);
    } catch (Error e) {
      where.add(what);
    }
  }
}
