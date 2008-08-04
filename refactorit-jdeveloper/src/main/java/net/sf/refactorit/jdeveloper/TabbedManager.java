/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper;

import oracle.ide.docking.DockStation;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;


/**
 * @author  jaanek
 */
public class TabbedManager {
  private static TabbedManager manager;

  private RefactorItWindow dockableWindow;

  /** Creates new TabbedManager */
  private TabbedManager() {
    //
    JTabbedPane tabbedPane = new JTabbedPane();

    // Create the window which holds the refactorit result JComponent
    this.dockableWindow = new RefactorItWindow("Refactorings", tabbedPane);

    // Dock this created window into south area of screen
    AbstractionUtils.dock(dockableWindow, DockStation.SOUTH, true);

    // Create the window which holds the refactorit result JComponent
    //RefactorItWindow dockableWindow2 = new RefactorItWindow (title, component);
    // Dock this created window into south area of screen
    //Ide.getDockStation().dock(dockableWindow2, dockableWindow, DockStation.CENTER, true);
  }

  public static Object addTab(String title, JComponent component) {
    getInstance().getTabPane().addTab(title, null, component, title);
    showTab(component);
    return component;
  }

  public static void removeTab(Object category) {
    if (JComponent.class.isAssignableFrom(category.getClass())) {
      getInstance().getTabPane().remove((JComponent) category);
      //System.out.println("removed");
    }
  }

  public static boolean showTab(Object category) {
    if (JComponent.class.isAssignableFrom(category.getClass())) {
      AbstractionUtils.setDockableVisible(
          getInstance().getRefactorItWindow(), true);

      try {
        getInstance().getTabPane().setSelectedComponent((JComponent) category);
      } catch (NullPointerException e) {
        e.printStackTrace(System.err);
        return false;
      }

      return true;
    }

    return false;
  }

  private static TabbedManager getInstance() {
    if (manager == null) {
      manager = new TabbedManager();
    }

    return manager;
  }

  private JTabbedPane getTabPane() {
    // FIXME: This caused an NPE (bug #1675)
    return (JTabbedPane) dockableWindow.getHostedComponent();
  }

  private RefactorItWindow getRefactorItWindow() {
    return dockableWindow;
  }
}
